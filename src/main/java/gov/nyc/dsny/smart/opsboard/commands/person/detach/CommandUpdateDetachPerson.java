package gov.nyc.dsny.smart.opsboard.commands.person.detach;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IFromLocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IToLocationCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

@IMultiBoardCommandAnnotation(commandName = "UpdateDetachPerson")
public class CommandUpdateDetachPerson extends AbstractPersonAttachDetachCommand implements IFromLocationCommand,
IToLocationCommand{
	
	private Date originalEndDate = new Date();

	public static final String COMMAND_NAME = "UpdateDetachPerson";

	private static final long serialVersionUID = 1L;

	public CommandUpdateDetachPerson() {
	}

	public CommandUpdateDetachPerson(LinkedHashMap<String, Object> map) {
		super(map);
		if (map.get("originalEndDate") != null) {
			originalEndDate = new Date((long) map.get("originalEndDate"));
			}else{
			originalEndDate = null;
		}

	}

	public CommandUpdateDetachPerson(String boardId, String systemUser, Date systemDateTime, String boardPersonId,
			String personId, Location from, Location to, Detachment detachment, Date originalEndDate) {
		super(boardId, boardPersonId, personId, from, to, detachment);	
		setOriginalEndDate(originalEndDate);
	}

	protected void createAuditMessage(Board board, BoardPerson bp, Location from, Location to, Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated person detachment");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append("from [location:" + from.getCode() + "] ");
		sb.append("to [location:" + to.getCode() + "] ");
		sb.append("to [endDate:" + endDate + "].");
		
		setAuditMessage(sb.toString());
	}

	@Override
	public void execute(Board board) throws OpsBoardError {	
		
		// Execute logic
		BoardPersonAndTasks bpts=board.getPersonOps().updatePersonDetach(getPersonId(), getDetachment(),getOriginalEndDate());

		// Update command
		updatePersonAndTasks(board, bpts);

		// Create audit message
		createAuditMessage(board, bpts.getBoardPerson(), getFrom(), getTo(), getDetachment().getEndDate());

		// Add command to history
		board.addCommandToHistory(this);
	}

	public Date getOriginalEndDate() {
		return originalEndDate;
	}

	public void setOriginalEndDate(Date originalEndDate) {
		this.originalEndDate = originalEndDate;
	}

	
}