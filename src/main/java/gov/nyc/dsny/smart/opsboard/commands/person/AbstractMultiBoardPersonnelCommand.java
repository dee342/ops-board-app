package gov.nyc.dsny.smart.opsboard.commands.person;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Represents the base class for all multi-board commands for personnel. The class extends base
 * AbstractMutltiBoardCommand with person fields.
 */
public abstract class AbstractMultiBoardPersonnelCommand extends AbstractMultiBoardCommand {

	private static final long serialVersionUID = 1L;

	private boolean assigned;
	private boolean assignedAnywhere;
	private String boardPersonId;
	private String personId;
	private String state;
	private Set<TaskAssignment> tasks;

	public AbstractMultiBoardPersonnelCommand() {
		super();
	}

	public AbstractMultiBoardPersonnelCommand(LinkedHashMap<String, Object> map) {
		super(map);
		// made this optional
		boardPersonId = (String) map.get("boardPersonId");
		personId = map.get("personId").toString();
	}

	public AbstractMultiBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, String personId,
			boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, fromIntegration);
		this.personId = personId;
	}

	public AbstractMultiBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String equipmentId, boolean fromIntegration) {
		this(boardId, systemUser, systemDateTime, equipmentId, fromIntegration);
		this.boardPersonId = boardPersonId;
	}

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public boolean getAssigned() {
		return assigned;
	}

	public String getBoardPersonId() {
		return boardPersonId;
	}

	public String getPersonId() {
		return personId;
	}

	public String getState() {
		return state;
	}

	public Set<TaskAssignment> getTasks() {
		return tasks;
	}

	@Override
	public boolean matchBoard(Board board, Location location) {
		Date systemDateTime = DateUtils.removeTime(getSystemDateTime());
		Date boardDate = DateUtils.toBoardDate(board.getDate());

		boolean firstPass = board.getLocation().equals(location) && (DateUtils.onOrAfter(boardDate, systemDateTime)
				|| DateUtils.onOrBetween(systemDateTime, board.getShiftsStartDate(), board.getShiftsEndDate()));

		if (firstPass == false) {
			return false;
		}

		return board.getPersonnel().containsKey(getPersonId());
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public void setBoardPersonId(String boardPersonId) {
		this.boardPersonId = boardPersonId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTasks(Set<TaskAssignment> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assigned", assigned);
		builder.append("boardPersonId", boardPersonId);
		builder.append("personId", personId);
		builder.append("state", state);
		builder.append("tasks", tasks);
		builder.append(super.toString());

		return builder.toString();
	}

	protected Set<TaskAssignment> convertPersonTasksToTaskAssigments(BoardPersonAndTasks bpts) {
		Set<TaskAssignment> tas = new LinkedHashSet<TaskAssignment>();
		Set<Task> tasks = bpts.getTasks();

		if (tasks != null) {
			for (Task t : tasks) {
				PersonAssignment pa1 = t.getAssignedPerson1();
				if (pa1 != null && pa1.getPerson() == null) {
					tas.add(new TaskAssignment(t.getId(), pa1.getStartTime(), pa1.getEndTime(), pa1.getAssignmentTime(),
							pa1.isCompleted(), 1));
				}
				PersonAssignment pa2 = t.getAssignedPerson2();
				if (pa2 != null && pa2.getPerson() == null) {
					tas.add(new TaskAssignment(t.getId(), pa2.getStartTime(), pa2.getEndTime(), pa2.getAssignmentTime(),
							pa2.isCompleted(), 2));
				}

			}
		}

		for (String tId : bpts.getSupervisorTaskIds()) {
			TaskAssignment ta = new TaskAssignment(tId, bpts.getBoardPerson().getPerson().getId());
			tas.add(ta);
		}

		return tas;
	}

	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {

		// Set person details
		if (bpts.getBoardPerson() != null) {
			setState(bpts.getBoardPerson().getState(board.getLocation()).getState());
			setAssigned(bpts.getBoardPerson().isAssigned(board.getLocation().getCode()));
			setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
		}

		// Set task details
		if (bpts.getTasks() != null) {
			setTasks(convertPersonTasksToTaskAssigments(bpts));
		}
	}

	public boolean isAssignedAnywhere() {
		return assignedAnywhere;
	}

	public void setAssignedAnywhere(boolean assignedAnywhere) {
		this.assignedAnywhere = assignedAnywhere;
	}
	
	
}
