package gov.nyc.dsny.smart.opsboard.commands.person.specialposition;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;

import java.util.Date;
import java.util.LinkedHashMap;

@IMultiBoardCommandAnnotation(commandName = "AddSpecialPosition")
public class CommandAddSpecialPosition extends AbstractSpecialPositionCommand {

	private static final long serialVersionUID = 1L;

	public CommandAddSpecialPosition() {
		super();
	}

	public CommandAddSpecialPosition(LinkedHashMap<String, Object> map) {
		super(map);
	}
	
	public CommandAddSpecialPosition(String boardId, String systemUser, Date lastModifiedSystem, String boardPersonId,
			String personId, SpecialPosition specialPosition) {
		this(boardId, systemUser, lastModifiedSystem, boardPersonId, personId, specialPosition, false);
	}

	public CommandAddSpecialPosition(String boardId, String systemUser, Date lastModifiedSystem, String boardPersonId,
			String personId, SpecialPosition specialPosition, boolean fromIntegration) {
		super(boardId, systemUser, lastModifiedSystem, boardPersonId, personId, specialPosition, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		//Execute logic
		BoardPerson bp = board.getPersonOps().addPersonSpecialPosition(getPersonId(), getSpecialPosition());
		
		setState(bp.getState(board.getLocation()).getState());
		setAssigned(bp.isAssigned(board.getLocation().getCode()));	
		setAssignedAnywhere(bp.isAssigned());
		setActiveSpecialPositions(bp.getActiveSpecialPositions());
		setSpecialPositionsHistory(bp.getSpecialPositionHistory());		

		// Create audit message
		createAuditMessage(board, bp, getSpecialPosition());
		
		//Add command to history
		board.addCommandToHistory(this);
	}

	@Override
	protected void createAuditMessage(Board board, BoardPerson bp,
			SpecialPosition specialPosition) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added special position for person ");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append(".");

		setAuditMessage(sb.toString());
	}

	
}
