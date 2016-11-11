package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

@IBoardCommandAnnotation(commandName = "UpdateDayBefore")
public class CommandUpdateDayBefore extends AbstractDayBeforeCommand{

	private Set<Shift> remove = new HashSet<Shift>();
	private Set<Shift> add = new HashSet<Shift>();

	/**
	 * 
	 */
	private static final long serialVersionUID = -6248448046084846497L;
	
	public CommandUpdateDayBefore(){}
		
	
	public CommandUpdateDayBefore(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		board.removeDayBefore(getPersonId(), remove);
		board.addDayBefore(getPersonId(), add);
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		
	}

	@Override
	protected void createAuditMessage(Board board) {

	}

	public Set<Shift> getRemove() {
		return remove;
	}


	public void setRemove(Set<Shift> remove) {
		this.remove = remove;
	}


	public Set<Shift> getAdd() {
		return add;
	}


	public void setAdd(Set<Shift> add) {
		this.add = add;
	}
	
	public void remove(Shift remove){
		this.remove.add(remove);
	}
	
	public void add(Shift add){
		this.add.add(add);
	}

}
