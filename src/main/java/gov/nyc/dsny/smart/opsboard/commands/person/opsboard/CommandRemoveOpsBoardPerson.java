package gov.nyc.dsny.smart.opsboard.commands.person.opsboard;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.AbstractDayBeforeCommand;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.CommandRemoveDayBefore;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * Command to remove an OpsBoardEquipment from a board.
 */
@IMultiBoardCommandAnnotation(commandName = "RemovePerson")
public class CommandRemoveOpsBoardPerson extends AbstractOpsBoardPersonnelCommand {
	private static final long serialVersionUID = 1L;

	private Set<Shift> nextDayUnassignedShifts = new HashSet<>();
	private Set<Shift> dayBeforeUnassignedShifts = new HashSet<>();

	public CommandRemoveOpsBoardPerson() {
	}

	public CommandRemoveOpsBoardPerson(LinkedHashMap<String, Object> map) {
		super(map);
	}
	
	public CommandRemoveOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId) {
		this(boardId, systemUser, systemDateTime, personId, false);
	}	
	
	public CommandRemoveOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, personId, fromIntegration);
	}	
	
	public CommandRemoveOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId, boolean fromIntegration, boolean update) {
		super(boardId, systemUser, systemDateTime, personId, fromIntegration, update);
	}	

	public CommandRemoveOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp) {
		super(boardId, systemUser, systemDateTime, bp);
	}	
	
	public CommandRemoveOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp, boolean update) {
		super(boardId, systemUser, systemDateTime, bp, update);
	}	


	@Override
	protected void createAuditMessage(Board board, BoardPerson bp) {
		StringBuilder sb = new StringBuilder();
		sb.append("Removed person ");
		sb.append(bp.getPerson().getFullName());
		sb.append(".");

		setAuditMessage(sb.toString());
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		Map<String, Set<Shift>> personToTasksMap = new HashMap<>();
		BoardHelper.populateNextDayAssigned(board, personToTasksMap);

		// Execute logic
		BoardPersonAndTasks bpts = board.getPersonOps().removeBoardPerson(getPersonId());
		
		
		
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
		createAuditMessage(board, bpts.getBoardPerson());
		// Add command to history
		board.addCommandToHistory(this);
	}
	
	

}
