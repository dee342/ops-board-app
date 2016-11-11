package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.commands.task.settings.CommandSetPartialTask;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;
import gov.nyc.dsny.smart.opsboard.persistence.repos.board.TaskRepository;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.AddPartialTaskRequest;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import util.BoardUtils;

@Service
public class TaskExecutor extends BoardExecutor {

	private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);

	@Autowired
	private ShiftCacheService shiftCacheService;

	@Autowired
	private TaskRepository taskRepository;

	public void setPartialTask(BoardKey key, Board board, Location tcLocation,
			AddPartialTaskRequest partialTaskRequest, Date operationDateTime, Principal principal) throws OpsBoardError {

		// synchronize on the board
		synchronized (board) {

			List<PartialTask> partialTasks = partialTaskRequest.getPartialTasks();

			// get the taskcontainer for this location
			TaskContainer taskContainer = BoardUtils.retrieveTaskContainerForLocation(board, tcLocation);
			LocationShift locationShift = BoardUtils.retrieveShiftLocation(taskContainer,
					partialTaskRequest.getLocationShiftId());

			// order the partialtask by sequence no
			Collections.sort(partialTasks, (p1, p2) -> new Integer(p1.getSequence()).compareTo(p2.getSequence()));

			// populate domain tasks
			populateTasksFromRequestAndLink(partialTasks, taskContainer, locationShift, principal);

			LinkedHashMap<String, Object> partialTaskContents = new LinkedHashMap<String, Object>();
			partialTaskContents.put("locationId", partialTaskRequest.getServiceLocationId());
			partialTaskContents.put("partialTasks", partialTaskRequest.getPartialTasks());
			partialTaskContents.put("locationShiftId", partialTaskRequest.getLocationShiftId());

			// create command
			CommandSetPartialTask commandSetPartialTask = new CommandSetPartialTask(board.getId(), principal.getName(),
					operationDateTime, tcLocation, shiftCacheService.getShiftById(partialTaskRequest.getShiftId()),
					partialTaskRequest.getLocationShiftId(), partialTaskRequest.getPartialTasks());

			// execute
			commandSetPartialTask.execute(board);

			// send command
			sendCommand(commandSetPartialTask.getName(), commandSetPartialTask, board, principal.getName());

		}
	}

	private void populateTasksFromRequestAndLink(List<PartialTask> partialTasks, TaskContainer taskContainer,
			LocationShift locationShift, Principal principal) {

		LocalDateTime startTime = locationShift.getShiftStartDate();
		// add hours to this to calculate end time
		LocalDateTime taskTime = startTime;
		// operation date
		Date operationTime = new Date();
		for (int i = 0; i < partialTasks.size(); i++) {
			PartialTask partialTask = partialTasks.get(i);
			Task task = taskContainer.getTasksMap().get(partialTask.getId());
			// Task newTask = (Task)CloneUtils.deepClone(oldTask);
			task.setLastModified(operationTime);

			ZonedDateTime zdt = taskTime.atZone(ZoneId.systemDefault());
			task.setStartDate(Date.from(zdt.toInstant()));

			taskTime = taskTime.plusHours(partialTasks.get(i).getHours());

			zdt = taskTime.atZone(ZoneId.systemDefault());
			task.setEndDate(Date.from(zdt.toInstant()));
			task.setLastModifiedBy(principal.getName());

			// if the task is anything but the last task
			if (i < partialTasks.size() - 1) {
				// set the child id
				task.setLinkedTaskChildId(partialTasks.get(i + 1).getId());
				partialTask.setLinkedTaskChildId(partialTasks.get(i + 1).getId());
			}
			// if the task is anything but the first task
			if (i > 0) {
				// set the parent id
				task.setLinkedTaskParentId(partialTasks.get(i - 1).getId());
				partialTask.setLinkedTaskParentId(partialTasks.get(i - 1).getId());
			}
		}
	}
}