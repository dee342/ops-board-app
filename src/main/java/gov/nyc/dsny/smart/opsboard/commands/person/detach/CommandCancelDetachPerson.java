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

@IMultiBoardCommandAnnotation(commandName = "CancelDetachPerson")
public class CommandCancelDetachPerson extends AbstractPersonAttachDetachCommand implements IFromLocationCommand,
IToLocationCommand{

	public static final String COMMAND_NAME = "CancelDetachPerson";

	private static final long serialVersionUID = 1L;

	public CommandCancelDetachPerson() {
	}

	public CommandCancelDetachPerson(LinkedHashMap<String, Object> map) {
		super(map);

	}

	public CommandCancelDetachPerson(String boardId, String systemUser, Date systemDateTime, String boardPersonId,
			String personId, Location from, Location to, Detachment detachment) {
		super(boardId, boardPersonId, personId, from, to, detachment);		
	}

	protected void createAuditMessage(Board board, BoardPerson bp, Location from, Location to) {
		StringBuilder sb = new StringBuilder();
		sb.append("Cancelled person detachment");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append("from [location:" + from.getCode() + "] ");
		sb.append("to [location:" + to.getCode() + "].");

		setAuditMessage(sb.toString());
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardPersonAndTasks bpts = board.getPersonOps().removePersonDetach( getPersonId(),getDetachment());

		// Update command
		updatePersonAndTasks(board, bpts);

		// Create audit message
		createAuditMessage(board, bpts.getBoardPerson(), getFrom(), getTo());

		// Add command to history
		board.addCommandToHistory(this);
	}

}