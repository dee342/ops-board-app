package gov.nyc.dsny.smart.opsboard.commands.person;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.ILowPriorityCommand;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.CommandAssignPersonFromTaskToTask;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.util.PersonLogger;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPersonMetaData;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command to update equipment to Pending Load status.
 */
@IMultiBoardCommandAnnotation(commandName = "AutoCompletePersonnel")
public class CommandAutoCompletePersonnel extends AbstractMultiBoardCommand implements ILowPriorityCommand  {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CommandAutoCompletePersonnel.class);

	private OpsBoardPersonMetaData opsBoardPerson1MetaData = new OpsBoardPersonMetaData();
	private OpsBoardPersonMetaData opsBoardPerson2MetaData = new OpsBoardPersonMetaData();

	public CommandAutoCompletePersonnel(LinkedHashMap<String, Object> map) {
		super(map);
		if (map.get("opsBoardPerson1MetaData") != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				opsBoardPerson1MetaData = mapper.readValue(map.get("opsBoardPerson1MetaData").toString(),
						OpsBoardPersonMetaData.class);
			} catch (Exception e) {
				// Do nothing
			}
		}
		if (map.get("opsBoardPerson2MetaData") != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				opsBoardPerson2MetaData = mapper.readValue(map.get("opsBoardPerson2MetaData").toString(),
						OpsBoardPersonMetaData.class);
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	public CommandAutoCompletePersonnel(String boardId, String systemUser, Date systemDate, String boardPersonId1,
			String personId1, String boardPersonId2, String personId2) {
		super(boardId, systemUser, systemDate, false);

		// Person 1
		opsBoardPerson1MetaData.setBoardPersonId(boardPersonId1);
		opsBoardPerson1MetaData.setPersonId(personId1);

		// Person 2
		opsBoardPerson2MetaData.setBoardPersonId(boardPersonId2);
		opsBoardPerson2MetaData.setPersonId(personId2);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		BoardPersonAndTasks bpts1 = null;
		BoardPersonAndTasks bpts2 = null;
		BoardPerson bp1 = null;
		BoardPerson bp2 = null;
		// First Person
		if (opsBoardPerson1MetaData.getPersonId() != null) {
			bpts1 = board.getPersonOps().autoCompletePerson(opsBoardPerson1MetaData.getPersonId(), getSystemUser(),
					getSystemDateTime());
			// Update command
			updatePersonAndTasks(board, bpts1, 1);

			bp1 = bpts1.getBoardPerson();
			PersonLogger.logBoardPerson("execute for P1 ", bp1, log);
		}

		// Second Person
		if (opsBoardPerson2MetaData.getPersonId() != null) {
			bpts2 = board.getPersonOps().autoCompletePerson(opsBoardPerson2MetaData.getPersonId(), getSystemUser(),
					getSystemDateTime());
			// Update command
			updatePersonAndTasks(board, bpts2, 2);

			bp2 = bpts2.getBoardPerson();
			PersonLogger.logBoardPerson("execute for P2 ", bp2, log);
		}
		
		// Create audit message
		createAuditMessage(board, bp1, bp2);

		// Add command to history
		board.addCommandToHistory(this);

	}

	public OpsBoardPersonMetaData getOpsBoardPerson1MetaData() {
		return opsBoardPerson1MetaData;
	}

	public OpsBoardPersonMetaData getOpsBoardPerson2MetaData() {
		return opsBoardPerson2MetaData;
	}

	@Override
	public boolean matchBoard(Board board, Location location) {
		return board.getId().equals(getBoardId()); // only applicable to a single board
	}

	public void setOpsBoardPerson1MetaData(OpsBoardPersonMetaData opsBoardPersonMetaData) {
		opsBoardPerson1MetaData = opsBoardPersonMetaData;
	}

	public void setOpsBoardPerson2MetaData(OpsBoardPersonMetaData opsBoardPersonMetaData) {
		opsBoardPerson2MetaData = opsBoardPersonMetaData;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append(super.toString());

		return builder.toString();
	}

	protected Set<TaskAssignment> convertPersonTasksToTaskAssigments(BoardPersonAndTasks bpts, int position) {
		bpts.getBoardPerson();
		Set<Task> tasks = bpts.getTasks();
		Set<TaskAssignment> tas = new LinkedHashSet<TaskAssignment>();
		if (tasks != null) {
			for (Task t : tasks) {
				PersonAssignment pa1 = t.getAssignedPerson1();
				if (pa1 != null && pa1.isCompleted() && position == 1) {
					tas.add(new TaskAssignment(t.getId(), pa1.getStartTime(), pa1.getEndTime(),
							pa1.getAssignmentTime(), pa1.isCompleted(), 1));
				} // since working on a single person, only one person assignment task can be created
				PersonAssignment pa2 = t.getAssignedPerson2();
				if (pa2 != null && pa2.isCompleted() && position == 2) {
					tas.add(new TaskAssignment(t.getId(), pa2.getStartTime(), pa2.getEndTime(),
							pa2.getAssignmentTime(), pa2.isCompleted(), 2));
				}

			}
		}

		return tas;
	}

	protected void createAuditMessage(Board board, BoardPerson bp1, BoardPerson bp2) {
		StringBuilder sb = new StringBuilder();
		if (bp1 != null && bp2 != null) {
			sb.append("Auto-completed persons ");
			sb.append("[person:" + bp1.getPerson().getId() + "] & ");
			sb.append("[person:" + bp2.getPerson().getId() + "].");
		} else if (bp1 != null && bp2 == null) {
			sb.append("Auto-completed person ");
			sb.append("[person:" + bp1.getPerson().getId() + "].");
		} else {
            sb.append("Auto-completed person ");
            sb.append("[person:" + bp2.getPerson().getId() + "].");
		}

		setAuditMessage(sb.toString());
	}

	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts, int position) {

		// Set equipment details
		if (position == 1) {
			if (bpts.getBoardPerson() != null) {
				opsBoardPerson1MetaData.setState(bpts.getBoardPerson().getState(board.getLocation()).getState());
				opsBoardPerson1MetaData.setAssigned(bpts.getBoardPerson().isAssigned(board.getLocation().getCode()));
				opsBoardPerson1MetaData.setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
			}

			// Set task details
			if (bpts.getTasks() != null) {
				opsBoardPerson1MetaData.setTasks(convertPersonTasksToTaskAssigments(bpts, 1));
			}
		} else {
			if (bpts.getBoardPerson() != null) {
				opsBoardPerson2MetaData.setState(bpts.getBoardPerson().getState(board.getLocation()).getState());
				opsBoardPerson2MetaData.setAssigned(bpts.getBoardPerson().isAssigned(board.getLocation().getCode()));
				opsBoardPerson2MetaData.setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
			}

			// Set task details
			if (bpts.getTasks() != null) {
				opsBoardPerson2MetaData.setTasks(convertPersonTasksToTaskAssigments(bpts, 2));
			}
		}
	}
}
