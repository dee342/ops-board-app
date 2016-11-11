package gov.nyc.dsny.smart.opsboard.commands.person.detach;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.AbstractDayBeforeCommand;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.CommandRemoveDayBefore;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * Command to temporarily detach a person from a board.
 */
@IMultiBoardCommandAnnotation(commandName = "DetachPerson")
public class CommandDetachPerson extends AbstractPersonAttachDetachCommand {

	public static final String COMMAND_NAME = "DetachPerson";

	private static final long serialVersionUID = 1L;

	private Set<Shift> nextDayUnassignedShifts = new HashSet<>();	

	public CommandDetachPerson() {
	}

	public CommandDetachPerson(LinkedHashMap<String, Object> map) {
		super(map);

	}

	public CommandDetachPerson(String boardId,  String boardPersonId, String personId, 
			Location from, Location to, Detachment detachment) {
		super(boardId, boardPersonId, personId, from, to, detachment);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		
		Map<String, Set<Shift>> personToTasksMap = new HashMap<>();
		BoardHelper.populateNextDayAssigned(board, personToTasksMap);
		
		//Execute logic
		BoardPersonAndTasks bpts = board.getPersonOps().detachPerson(getPersonId(), getDetachment());
		
		Map<String, Set<Shift>> newPersonToTasksMap = new HashMap<>();
		newPersonToTasksMap = BoardHelper.compareNextDayAssignments(board, personToTasksMap);	
		
		nextDayUnassignedShifts = newPersonToTasksMap.get(getPersonId());
		
		if(!CollectionUtils.isEmpty(nextDayUnassignedShifts)){
			board.unassignNextDay(getPersonId(), nextDayUnassignedShifts);
			List<AbstractDayBeforeCommand> nextDayCommands =  (List<AbstractDayBeforeCommand>) AbstractDayBeforeCommand.build(getBoardId(), board.getPersonnel().get(getPersonId()), true, nextDayUnassignedShifts);
			nextDayCommands.forEach(c -> this.addSubCommands((CommandRemoveDayBefore) c));
		}
		
		//Update command
		updatePersonAndTasks(board, bpts);
		
		// Create audit message
		createAuditMessage(board, bpts.getBoardPerson(), getFrom(), getTo());
		
		//Add command to history
		board.addCommandToHistory(this);
	}


	protected void createAuditMessage(Board board, BoardPerson bp, Location from, Location to) {
		StringBuilder sb = new StringBuilder();
		sb.append("Detached person ");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append("to [location:" + to.getCode() + "].");

		setAuditMessage(sb.toString());
	}

	public Set<Shift> getNextDayUnassignedShifts() {
		return nextDayUnassignedShifts;
	}

	public void setNextDayUnassignedShifts(Set<Shift> nextDayUnassignedShifts) {
		this.nextDayUnassignedShifts = nextDayUnassignedShifts;
	}

	
}
