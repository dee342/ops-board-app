package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractDayBeforeCommand extends AbstractBoardCommand{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1962736831087545475L;
	private String personId;
	private Set<Shift> shifts = new HashSet<Shift>();
	
	public AbstractDayBeforeCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}
	
	public AbstractDayBeforeCommand() {		
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public Set<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(Set<Shift> shifts) {
		this.shifts = shifts;
	}
	
	public void addShifts(Shift ... shifts){
		addShifts(Arrays.stream(shifts).collect(Collectors.toSet()));
	}
	
	public void addShifts(Set<Shift> shifts){
		this.shifts.addAll(shifts);
	}
	
	public static List<AbstractDayBeforeCommand> build(String boardId, BoardPerson bp, boolean isRemove, Shift... nextDayShifts){
		return build(boardId, bp, isRemove, Arrays.stream(nextDayShifts).collect(Collectors.toSet()));
	}
	
	public static List<AbstractDayBeforeCommand> build(String boardId, BoardPerson bp, boolean isRemove, Set<Shift> nextDayShifts){
		List<AbstractDayBeforeCommand> commands = new ArrayList<AbstractDayBeforeCommand>();
		Set<String> locations = Stream.of(Board.boardIdToLocation(boardId), bp.getWorkLocation().getCode(), bp.getHomeLocation().getCode()).collect(Collectors.toSet());
		locations.addAll(bp.getFutureWorkLocations().stream().map(l -> l.getCode()).collect(Collectors.toSet()));		
		
			locations.forEach(l -> {
				AbstractDayBeforeCommand nextDayCommand = (isRemove) ? new CommandRemoveDayBefore() : new CommandAddDayBefore();
				
				Date tomorrow = DateUtils.getOneDayAfter(DateUtils.toBoardDate(Board.boardIdToBoardDate(boardId)));	
				String newBoardId = Board.toBoardId(l, DateUtils.toStringBoardDate(tomorrow));
				nextDayCommand.setBoardId(newBoardId);
				nextDayCommand.setPersonId(BoardPerson.EXTRACT_PERSON_ID(bp.getId()));
				nextDayCommand.addShifts(nextDayShifts);
				nextDayCommand.setSystemDateTime(new Date());
				nextDayCommand.setSystemUser(Utils.getUserId());
				
				commands.add(nextDayCommand);
			});
		
		
		return commands;
	}
	
	
	
}
