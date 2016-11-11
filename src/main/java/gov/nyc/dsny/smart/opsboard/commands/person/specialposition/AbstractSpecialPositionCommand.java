package gov.nyc.dsny.smart.opsboard.commands.person.specialposition;

import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISpecialPositionCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSpecialPositionCommand extends AbstractMultiBoardPersonnelCommand implements  ISpecialPositionCommand{

	private static final long serialVersionUID = 1L;
	private List<SpecialPosition> activeSpecialPositions;
	private ConcurrentSkipListSet<SpecialPosition> specialPositionsHistory;
	private SpecialPosition specialPosition;

	private static final Logger log = LoggerFactory.getLogger(AbstractSpecialPositionCommand.class);
		
	public AbstractSpecialPositionCommand() {
		super();
	}

	public AbstractSpecialPositionCommand(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public AbstractSpecialPositionCommand(String boardId, String systemUser, Date lastModifiedSystem,
			String boardPersonId, String personId, SpecialPosition specialPosition, boolean fromIntegration) {
		super(boardId, systemUser, lastModifiedSystem, boardPersonId, personId, fromIntegration);
		this.specialPosition = specialPosition;
	}
	
	public AbstractSpecialPositionCommand(String boardId, String systemUser, Date lastModifiedSystem,
			String boardPersonId, String personId, SpecialPosition specialPosition) {
		this(boardId, systemUser, lastModifiedSystem, boardPersonId, personId, specialPosition, false);
	}
	

	public List<SpecialPosition> getActiveSpecialPositions() {
		return activeSpecialPositions;
	}

	public SpecialPosition getSpecialPosition() {
		return specialPosition;
	}

	public ConcurrentSkipListSet<SpecialPosition> getSpecialPositionsHistory() {
		return specialPositionsHistory;
	}

	public void setActiveSpecialPositions(List<SpecialPosition> activeSpecialPositions) {
		this.activeSpecialPositions = activeSpecialPositions;
	}

	public void setSpecialPosition(SpecialPosition specialPosition) {
		this.specialPosition = specialPosition;
	}

	public void setSpecialPositionsHistory(SortedSet<SpecialPosition> specialPositionsHistory) {
		this.specialPositionsHistory = new ConcurrentSkipListSet<>(specialPositionsHistory);
	}
	
	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {
		super.updatePersonAndTasks(board, bpts);
		setActiveSpecialPositions(bpts.getBoardPerson().getActiveSpecialPositions());
		setSpecialPositionsHistory(bpts.getBoardPerson().getSpecialPositionHistory());		
	}
	
	protected abstract void createAuditMessage(Board board, BoardPerson bp, SpecialPosition specialPosition );
}
