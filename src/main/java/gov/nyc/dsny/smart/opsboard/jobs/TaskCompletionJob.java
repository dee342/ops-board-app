package gov.nyc.dsny.smart.opsboard.jobs;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.EquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.commands.ILowPriorityCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SectionTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;
import gov.nyc.dsny.smart.opsboard.services.LocationService;
import gov.nyc.dsny.smart.opsboard.services.executors.TaskExecutor;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.EquipmentExecutor;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.PersonExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.context.WebApplicationContext;

public class TaskCompletionJob extends QuartzJobBean {
	private static final String JOB_USER = "system";

	private static final Logger logger = LoggerFactory.getLogger(TaskCompletionJob.class);

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private EquipmentCacheService equipmentCache;

	@Autowired(required = true)
	private EquipmentExecutor equipmentExecutor;
	
	@Autowired
	private LocationService locationService;

	@Autowired(required = true)
	private PersonExecutor personExecutor;

	@Autowired(required = true)
	private TaskExecutor taskExecutor;
	
	@Autowired
	private WebApplicationContext appContext;
	
	@Qualifier("generalTaskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor generalTaskExecutor;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			processActiveTaskAssignments();
		} catch (OpsBoardError e) {
			logger.error("Exception while checking the status equipment on active tasks.", e);
		}
	}

	private void processActiveTaskAssignments() throws OpsBoardError {

		// We'll use 5 minutes inactivity window (on both TaskCompletionJob & LocationService)to spawn new auto-completion cycle 
		Object lastAutoCompleteObject = appContext.getServletContext().getAttribute(ILowPriorityCommand.LAST_AUTO_COMPLETION_COMMAND_PROCESSED_TIME);
		if (lastAutoCompleteObject != null)
		{
			Date lastAutoCompleteDate = (Date)lastAutoCompleteObject;
			if ((new Date().getTime() - lastAutoCompleteDate.getTime()) < 300000L)
			{
				logger.debug("Previous Autocomplete process is still running in the low priority treads.");
				return;
			}
		}

		logger.debug("ProcessActiveTaskAssignments Job started.");

		Date now = new Date();
		Collection<BoardContainer> boardContainers = boardsCache.get();
		
		for (BoardContainer boardContainer : boardContainers) {

			if (!DateUtils.onOrBetween(now, boardContainer.getBoard().getShiftsStartDate(),
					DateUtils.getOneDayAfter(boardContainer.getBoard().getShiftsEndDate()))) { // add one day to end
																								// of shift to
																								// provide buffer in
																								// case of
																								// deployments,
																								// restart,
																								// maintenance
				continue; // skip b/c not a current board
			}
			
			// Skip if there is no location listener (i.e. )
			String queueName = locationService.getQueueName(boardContainer.getBoard().getLocation().getCode());
			if (!locationService.isQueueExist(queueName)) {
				continue;
			}
			
			// Continue processing in the low priority thread
			BoardContainerProcessor boardContainerProcessor = new BoardContainerProcessor(boardContainer, now);
			boardContainerProcessor.setPriority(Thread.MIN_PRIORITY);
			generalTaskExecutor.execute(boardContainerProcessor);
		}
	
		logger.debug("ProcessActiveTasksWithEquipmentAssignments Job completed.");

	}
	
	private class BoardContainerProcessor extends Thread
	{
		private BoardContainer boardContainer;
		private Date now;
		
		public BoardContainerProcessor(BoardContainer boardContainer, Date now) {
			super();
			this.boardContainer = boardContainer;
			this.now = now;
		}

		public void run()
		{
			appContext.getServletContext().setAttribute(ILowPriorityCommand.LAST_AUTO_COMPLETION_COMMAND_PROCESSED_TIME, new Date());

			synchronized (boardContainer) {
				logger.debug("ProcessActiveTaskAssignments Job is working on board {}", boardContainer.getBoard()
						.getId());

				List<TaskContainer> taskContainers = boardContainer.getBoard().getTaskContainers();
				for (TaskContainer taskContainer : taskContainers) {
					Set<LocationShift> locationShifts = taskContainer.getLocationShifts();
					for (LocationShift locationShift : locationShifts) {
						if (DateUtils.before(now, locationShift.getStartDate())) {
							continue; // skip shift b/c it has not yet started
						}

						// Collect all the tasks
						Map<String, ImmutablePair<TaskContainer, Task>> equipmentTasks = new HashMap<String, ImmutablePair<TaskContainer, Task>>();
						Map<String, ImmutablePair<TaskContainer, Task>> personTasks = new HashMap<String, ImmutablePair<TaskContainer, Task>>();
						Map<String, Boolean> snowTask = new HashMap<String, Boolean>();
						Set<ShiftCategory> shiftCategories = locationShift.getShiftCategories();
						for (ShiftCategory shiftCategory : shiftCategories) {
							boolean isSnowTask = shiftCategory.getCategory().getName().equals("Snow");
							Set<SubcategoryTask> subCategoryTasks = shiftCategory.getSubcategoryTasks();
							for (SubcategoryTask subCategoryTask : subCategoryTasks) {
								List<SectionTask> sectionTasks = subCategoryTask.getSections();
								for (SectionTask sectionTask : sectionTasks) {
									for (Task t : sectionTask.getTasks()) {
										if (isTaskEligibleForEquipmentProcessing(taskContainer, t,
												subCategoryTask.getSubcategory(), now)) {
											equipmentTasks.put(t.getId(), new ImmutablePair<TaskContainer, Task>(
													taskContainer, t));
											if(isSnowTask)
												snowTask.put(t.getId(), true);
										}
										if (isTaskEligibleForPersonProcessing(taskContainer, t,
												subCategoryTask.getSubcategory(), now)) {
											personTasks.put(t.getId(), new ImmutablePair<TaskContainer, Task>(
													taskContainer, t));
										}
									}
								}
								for (Task t : subCategoryTask.getTasks()) {

									if (isTaskEligibleForEquipmentProcessing(taskContainer, t,
											subCategoryTask.getSubcategory(), now)) {
										equipmentTasks.put(t.getId(), new ImmutablePair<TaskContainer, Task>(
												taskContainer, t));
										if(isSnowTask)
											snowTask.put(t.getId(), true);
									}
									if (isTaskEligibleForPersonProcessing(taskContainer, t,
											subCategoryTask.getSubcategory(), now)) {
										personTasks.put(t.getId(), new ImmutablePair<TaskContainer, Task>(
												taskContainer, t));
									}
								}
							}		
						}
						
						for (Map.Entry<String, ImmutablePair<TaskContainer, Task>> entry : equipmentTasks
								.entrySet()) {
							Task t = entry.getValue().getRight();
							BoardEquipment be = t.getAssignedEquipment().getEquipment();

							try {
								logger.debug("Sending autoCompleteAssignedEquipment request for equipment {}",
										be.getId());

								boolean hasUnfinishedPartialTasks = BoardHelper.hasUnfinishedPartialTasks(t, taskContainer.getTasksMap(), now, BoardEquipment.EXTRACT_EQUIPMENT_ID(be.getId()));									

								equipmentExecutor.autoCompleteAssignedEquipment(boardContainer.getBoard().getKey(),
										be, now, JOB_USER, hasUnfinishedPartialTasks, snowTask.containsKey(t.getId()));
							} catch (Exception e) {
								logger.error(
										"Error encountered while processing auto complete for task {} and board equipment {}. Exception message: {}.",
										t.getId(), be.getId(), e.getMessage(), e);
								logger.error(e.getLocalizedMessage());
							}

						}

						for (Map.Entry<String, ImmutablePair<TaskContainer, Task>> entry : personTasks.entrySet()) {
							Task t = entry.getValue().getRight();

							try {
								personExecutor.autoCompleteAssignedPersons(boardContainer.getBoard().getKey(), t,
										now, JOB_USER);
							} catch (Exception e) {
								logger.error(
										"Error encountered while processing auto complete for task {}. Exception message: {}.",
										t.getId(), e.getMessage(), e);
								logger.error(e.getLocalizedMessage());
							}
						}
					}
				}
			}
		}
		
		private boolean isTaskEligibleForEquipmentProcessing(TaskContainer tc, Task t, Subcategory sc, Date now)
		{
			if (t.getAssignedEquipment() == null || t.getAssignedEquipment().getEquipment() == null) {
				return false; // skip b/c task has no equipment assigned to it
			}

			if (t.getAssignedEquipment().isCompleted()) {
				return false; // skip b/c equipment assignment has already been completed
			}

			if (DateUtils.after(t.getStartDate(), now)) {
				return false; // skip b/c task has not yet started
			}

			return t.isTaskAlmostOver(now);
		}

		protected boolean isTaskEligibleForPersonProcessing(TaskContainer tc, Task t, Subcategory sc, Date now)
		{
			PersonAssignment pa1 = t.getAssignedPerson1();
			PersonAssignment pa2 = t.getAssignedPerson2();

			if ((pa1 == null || pa1.getPerson() == null) && (pa2 == null || pa2.getPerson() == null)) {
				return false; // skip b/c task has no person assigned to it
			}

			if (pa1 != null && pa1.isCompleted() && pa2 != null && pa2.isCompleted()) {
				return false; // skip b/c person assignment has already been completed
			}

			if (DateUtils.after(t.getStartDate(), now)) {
				return false; // skip b/c task has not yet started
			}

			return DateUtils.before(t.getEndDate(), now); // true if end date is now or later than now
		}
	}
}