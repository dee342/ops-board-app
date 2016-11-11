package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;
import gov.nyc.dsny.smart.opsboard.services.executors.TaskExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.AddPartialTaskRequest;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import util.BoardUtils;

/**
 * The entry point for actions taken on a task.
 *
 * When an action is requested, the controller verifies the data inputs, and delegates processing to Executor
 */
@RestController
public class PartialTaskController extends BoardController {

	private static final Logger log = LoggerFactory.getLogger(PartialTaskController.class);

	@Autowired
	BoardCacheService boardCache;

	@Autowired
	LocationCache locationCache;

	@Autowired
	private TaskExecutor taskExecutor;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@RequestMapping(value = "/SetPartialTask/{boardLocation}/{boardDate}", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public String createPartialTasks(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@Valid @RequestBody AddPartialTaskRequest addPartialTaskRequest, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws ParseException, OpsBoardError,
			OpsBoardValidationException {

		log.debug("Started setting up partial task");

		Date date= DateUtils.toBoardDateNoNull(boardDate);
		Location location = locationCache.getLocation(addPartialTaskRequest.getServiceLocationId(), date);
		Location boardLoc = locationCache.getLocation(boardLocation, date);
		BoardKey key = boardKeyFactory.createBoardKey(boardDate, boardLoc);
		Board board = boardCache.get(key).getBoard();

		boolean isValid = true;

		try {
			isValid = validatePartialTask(boardLocation, boardDate, addPartialTaskRequest.getServiceLocationId(),
					addPartialTaskRequest.getLocationShiftId(), addPartialTaskRequest.getPartialTasks(), board,
					location);
			// redundant
			if (!isValid) {
				OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.SERVER_VALIDATION_ERROR);
				throw obve;
			}

			// sort the tasks based on the sequence number sent by UI
			Collections.sort(addPartialTaskRequest.getPartialTasks(),
					(p1, p2) -> new Integer(p1.getSequence()).compareTo(p2.getSequence()));
			// set the first task as the start truck - the list has to have more than 1 tasks if it passed validation
			addPartialTaskRequest.getPartialTasks().get(0).setStartTruck(true);
			//addPartialTaskRequest.getPartialTasks().stream().forEach(action);
			Set<String> subcategories = new HashSet<String>();
			for(PartialTask partialTask : addPartialTaskRequest.getPartialTasks()){
				subcategories.add(partialTask.getSubCategoryId());
			}
			addPartialTaskRequest.getPartialTasks().get(0).setPartialTaskSubcategories(subcategories.size());

			// delegate processing to executor
			taskExecutor.setPartialTask(key, board, location, addPartialTaskRequest, new Date(), principal);

		} catch (OpsBoardError obe) {
			log.error("OpsBoardError inside setPartialTask", obe);
			throw obe;
		} catch (OpsBoardValidationException obve) {
			log.error("OpsBoardValidationException inside setPartialTask", obve);
			throw obve;
		}

		log.debug("Setting up a Partial Task processing completed.");
		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	// throw away method
	@RequestMapping(value = "/createTestData/{boardLocation}/{boardDate}", method = RequestMethod.GET)
	@ResponseBody
	public AddPartialTaskRequest createTestData(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate) throws ParseException, OpsBoardError {

		AddPartialTaskRequest req = new AddPartialTaskRequest();
		List<PartialTask> partialTasksList = new ArrayList<PartialTask>();
		req.setServiceLocationId(boardLocation);
		req.setPartialTasks(partialTasksList);
		int counter = 1;
		Date date= DateUtils.toBoardDateNoNull(boardDate);
		Location location = locationCache.getLocation(boardLocation, date);
		BoardKey key = boardKeyFactory.createBoardKey(boardDate, location);
		Board b = boardCache.get(key).getBoard();
		req.setBoardId(b.getId());
		List<TaskContainer> t = b.getTaskContainers();
		for (TaskContainer taskContainer : t) {
			Set<LocationShift> lss = taskContainer.getLocationShifts();
			for (LocationShift locationShift : lss) {
				req.setLocationShiftId(locationShift.getId());

				Set<ShiftCategory> sfs = locationShift.getShiftCategories();
				for (ShiftCategory shiftCategory : sfs) {
					Set<SubcategoryTask> scts = shiftCategory.getSubcategoryTasks();
					for (SubcategoryTask subcategoryTask : scts) {
						SortedSet<Task> tasks = subcategoryTask.getTasks();
						for (Task task : tasks) {
							if (counter <= 2) {
								PartialTask pt1 = new PartialTask();
								pt1.setId(task.getId());
								pt1.setSequence(counter);
								pt1.setHours(4);

								pt1.setCategoryId(shiftCategory.getId());
								pt1.setSubCategoryId(subcategoryTask.getId());
								pt1.setSectionId(null);
								partialTasksList.add(pt1);
							}
							counter++;
						}

					}
				}
			}
		}

		return req;

	}

	public boolean validatePartialTask(String boardLocation, String boardDate, String locationId, String shiftId,
			List<PartialTask> partialTasks, Board board, Location location) throws OpsBoardValidationException {

		boolean isValid = true;

		TaskContainer tc = BoardUtils.retrieveTaskContainerForLocation(board, location);
		// need to throw exceptions
		if (partialTasks == null || partialTasks.size() <= 1) {
			// partial tasks size cannot be zero
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.INSUFFICIENT_PARTIAL_TASKS);
			throw obve;
		}
		Collections.sort(partialTasks, (p1, p2) -> new Integer(p1.getSequence()).compareTo(p2.getSequence()));
		partialTasks.size();
		int counter = 1;
		// String lastParentId = null;
		for (PartialTask partialTask : partialTasks) {

			if (partialTask.getSequence() != counter) {
				// not sequential after sorting
				OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.INCORRECTLY_LINKED_PARTIAL_TASKS);
				throw obve;
			}
			/*
			 * if(counter >= 1){ //not linked properly after sorting based on sequence order if(partialTask.getId() ==
			 * null || !partialTask.getId().equals(lastParentId)){ return false; } }
			 */
			// check to see if it is a valid task id but not an existing partial task id
			validateTaskDoesNotExistAlready(partialTask.getId(), tc);
			counter++;
			// lastParentId = partialTask.getId();
		}
		// check if task already exists
		validateDurationOfTasks(partialTasks);
		if (!isValid) {
			// hours do not add up to 8
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.PARTIAL_TASKS_DONT_ADD_UPTO_SHIFT_TIME);
			throw obve;
		}
		return isValid;

	}

	public void validateTaskDoesNotExistAlready(String taskId, TaskContainer taskContainer)
			throws OpsBoardValidationException {
		/*
		 * if( taskContainer == null || taskContainer.getTasksMap() == null ){ throw new system error }
		 */
		Task task = taskContainer.getTasksMap().get(taskId);
		if (task == null) {
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.TASK_DOES_NOT_EXIST);
			throw obve;
		}
		// one of them cannot be null or empty

		if (task.getLinkedTaskChildId() != null && !"".equals(task.getLinkedTaskChildId().trim())
				|| task.getLinkedTaskParentId() != null && !"".equals(task.getLinkedTaskParentId().trim())) {
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.PARTIAL_TASK_ALREADY_EXISTS);
			throw obve;
		}

		if (task.getAssignedEquipment() != null && task.getAssignedEquipment().getEquipment() != null) {
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.EQUIPMENT_ALREADY_ASSIGNED_TO_TASK);
			throw obve;
		}
		if (task.getAssignedPerson1() != null && task.getAssignedPerson1().getPerson() != null
				|| task.getAssignedPerson2() != null && task.getAssignedPerson2().getPerson() != null) {
			OpsBoardValidationException obve = generateOpsBoardValidationException(ErrorMessage.PERSON_ALREADY_ASSIGNED_TO_TASK);
			throw obve;
		}

	}

	private boolean validateDurationOfTasks(List<PartialTask> partialTasks) {

		int hours = 0;
		for (PartialTask partialTask : partialTasks) {
			hours = hours + partialTask.getHours();
		}
		if (hours == 8) {
			return true;
		}
		return false;
	}

}
