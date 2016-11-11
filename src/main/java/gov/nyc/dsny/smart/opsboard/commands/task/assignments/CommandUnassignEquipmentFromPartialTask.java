package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to un-assign a piece of Equipment from a partial task / multi route.
 */
@IBoardCommandAnnotation(commandName = "UnassignEquipmentFromPartialTask")
public class CommandUnassignEquipmentFromPartialTask extends AbstractTaskEquipmentCommand {

	private static final long serialVersionUID = 1L;

	private List<TaskAssignment> assignments = new ArrayList<TaskAssignment>();

	public CommandUnassignEquipmentFromPartialTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets = board.unassignEquipmentFromPartialTask(getTaskId(), getEquipmentId(),
				getSystemDateTime());
		setAssigned(bets.getBoardEquipment().isAssigned());		
		setStates(board.getLocation(), bets.getBoardEquipment());
		for (Task t : bets.getTasks()) {
			assignments.add(new TaskAssignment(t.getId(), t.getAssignedEquipment().getStartTime(), t
					.getAssignedEquipment().getEndTime(), t.getAssignedEquipment().getAssignmentTime(), t
					.getAssignedEquipment().isCompleted()));
		}

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public List<TaskAssignment> getAssignments() {
		return assignments;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setAssignments(List<TaskAssignment> assignments) {
		this.assignments = assignments;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assignments", assignments);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unassigned equipment ");
		sb.append("[equipment:" + getEquipmentId() + "] ");
		sb.append("from [task:" + getTaskId() + "].");

		setAuditMessage(sb.toString());
	};
}
