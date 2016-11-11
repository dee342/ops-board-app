package gov.nyc.dsny.smart.opsboard.validation;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.personnel.VolunteerCounts;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.springframework.security.access.AccessDeniedException;

import reactor.util.CollectionUtils;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class ValidationUtils {

	public static final List<String> CANCEL_AVAILABILITY_CODES = Arrays.asList("CHART", "VACATION", "JURY DUTY");
	public static final String ACTIVE_STATUS = "A";
	public static final String CANCELED_ACTION = "C";

	private static boolean checkForNextDay(Date startDate, Date endDate, BoardPerson person, String location){
		Date startDateNoTime = DateUtils.removeTime(startDate);			
		Date tomorrow = DateUtils.getOneDayAfter(DateUtils.removeTime(person.getDate()));
		boolean hasNextDay = false;
		if(endDate != null){
			Date endDateNoTime = DateUtils.removeTime(endDate);
			hasNextDay = DateUtils.onOrBetween(tomorrow, startDateNoTime, endDateNoTime);
		}else{
			hasNextDay = DateUtils.sameDay(startDateNoTime, tomorrow);
		}

		return hasNextDay && !CollectionUtils.isEmpty(person.getAssignedNextDayShifts(location));		

	}

	public static boolean isValid(Long id, String code, Date startDate, Date endDate, String comments, List<?> typeList, BoardPerson person, String type, BoardKey key) 
	{
		switch (type) {

		case "ADD_UNAVAILABLE":

			if (StringUtils.isBlank(code)) {
				throw new UnavailabilityValidationException(
						"Cannot create record - unavailability type must be defined");
			}

			if (startDate == null) {
				throw new UnavailabilityValidationException("Cannot create " + code + " - start date must be defined");
			}

			if (endDate != null && DateUtils.after(startDate, endDate)) {
				throw new UnavailabilityValidationException("Cannot create " + code
						+ " - start date must be earlier than end date");
			}

			if ((CANCEL_AVAILABILITY_CODES.get(0).equalsIgnoreCase(code)
					|| CANCEL_AVAILABILITY_CODES.get(1).equalsIgnoreCase(code) || CANCEL_AVAILABILITY_CODES.get(2)
					.equalsIgnoreCase(code)) && endDate == null) {
				throw new UnavailabilityValidationException("Cannot create " + code + " as end date cannot be empty");
			}

			if (CANCEL_AVAILABILITY_CODES.get(0).equalsIgnoreCase(code) && !DateUtils.sameDay(startDate, endDate)) {
				throw new UnavailabilityValidationException("Cannot create " + code
						+ " - start and end days must be the same");
			}



			if(checkForNextDay(startDate, endDate, person, key.getLocation().getCode()))
				throw new UnavailabilityValidationException("Cannot create " + code + " - person must be removed from upcoming next day tasks.");

			@SuppressWarnings("unchecked")		
			List<UnavailabilityReason> addUnavailabilityHistoryList = (List<UnavailabilityReason>)typeList;
			for (UnavailabilityReason unavailabilityReason : addUnavailabilityHistoryList) 

			{
				if (unavailabilityReason.getCode().equals(code) && 
						ACTIVE_STATUS.equals(unavailabilityReason.getStatus()) &&
						(DateUtils.dayOnOrBetween(startDate, unavailabilityReason.getStart(), unavailabilityReason.getEnd()) ||
								DateUtils.dayOnOrBetween(endDate, unavailabilityReason.getStart(), unavailabilityReason.getEnd())))
				{
					throw new UnavailabilityValidationException("Cannot create " + code + " - a record for this same calendar date range already exists");
				}
			}

			return true;

		case "UPDATE_UNAVAILABLE":

			if (id == null || id == 0) {
				throw new UnavailabilityValidationException("Cannot update record - id is unavailable");
			}

			if (StringUtils.isBlank(code)) {
				throw new UnavailabilityValidationException(
						"Cannot update record - unavailability type must be defined");
			}

			if (startDate == null) {
				throw new UnavailabilityValidationException("Cannot update " + code + " - start date must be defined");
			}

			if (endDate != null && DateUtils.after(startDate, endDate)) {
				throw new UnavailabilityValidationException("Cannot update " + code
						+ " - start date must be earlier than end date");
			}

			if (CANCEL_AVAILABILITY_CODES.get(0).equals(code) && !DateUtils.sameDay(startDate, endDate)) {
				throw new UnavailabilityValidationException("Cannot update " + code
						+ " - start and end days must be the same");
			}


			if(checkForNextDay(startDate, endDate, person, key.getLocation().getCode()))
				throw new UnavailabilityValidationException("Cannot update " + code + " - person must be removed from upcoming next day tasks.");

			Map<Long, UnavailabilityReason> updateUnavailabilityReasonMap = new HashMap<Long, UnavailabilityReason>();
			UnavailabilityReason updateUnavailabilityReason = null;

			// Collect all history in the map, and find matching updateUnavailabilityReason
			@SuppressWarnings("unchecked")
			List<UnavailabilityReason> updateUnavailabilityHistoryList = (List<UnavailabilityReason>)typeList;
			for (UnavailabilityReason unavailabilityReason : updateUnavailabilityHistoryList) 
			{
				if (ACTIVE_STATUS.equals(unavailabilityReason.getStatus()))
				{
					updateUnavailabilityReasonMap.put(unavailabilityReason.getId(), unavailabilityReason);
					if (updateUnavailabilityReason == null && id.equals(unavailabilityReason.getId())) {
						updateUnavailabilityReason = unavailabilityReason;
					}
				}
			}

			if (updateUnavailabilityReason == null) {
				throw new UnavailabilityValidationException("Cannot update data - active " + code + " was not found");
			}

			if (!code.equalsIgnoreCase(updateUnavailabilityReason.getCode())) {
				throw new UnavailabilityValidationException("Cannot update code.");
			}

			// Validate that the same code does not exist on the same date

			for (UnavailabilityReason unavailabilityReason : updateUnavailabilityHistoryList) 
			{
				if (unavailabilityReason.getId().equals(updateUnavailabilityReason.getId()))
					continue;
				checkUnavailabilitiesOverlap(id, code, startDate, endDate, unavailabilityReason);
			}

			// Check is the record was canceled
			UnavailabilityReason lastUpdatedChildUnavailabilityReason = getLastChild(updateUnavailabilityReasonMap,
					updateUnavailabilityReason);
			if (lastUpdatedChildUnavailabilityReason != null) {
				// Apply rule for the canceled records
				if (CANCELED_ACTION.equalsIgnoreCase(lastUpdatedChildUnavailabilityReason.getAction())) {
					if (!code.equals(updateUnavailabilityReason.getCode()))
						throw new UnavailabilityValidationException("Cannot change "
								+ updateUnavailabilityReason.getCode() + " to " + code + " - "
								+ updateUnavailabilityReason.getCode() + " has a canceled day");

					// Same start-end dates checks
					if (updateUnavailabilityReason.getEnd() == null
							|| DateUtils.sameDay(updateUnavailabilityReason.getStart(),
									updateUnavailabilityReason.getEnd())) {
						if (!startDate.equals(updateUnavailabilityReason.getStart())) {
							throw new UnavailabilityValidationException("Cannot update " + code
									+ " - start date cannot be changed");
						}

						if (endDate != null) {
							if (!endDate.equals(updateUnavailabilityReason.getEnd())) {
								throw new UnavailabilityValidationException("Cannot update " + code
										+ " - end date cannot be changed");
							}
						} else {
							if (updateUnavailabilityReason.getEnd() != null) {
								throw new UnavailabilityValidationException("Cannot update " + code
										+ " - end date cannot be changed");
							}
						}
					} else {
						if (DateUtils.after(startDate, lastUpdatedChildUnavailabilityReason.getStart())) {
							throw new UnavailabilityValidationException("Cannot update " + code
									+ " - start date invalid");
						}

						if (DateUtils.before(endDate, lastUpdatedChildUnavailabilityReason.getStart())) {
							throw new UnavailabilityValidationException("Cannot update " + code + " - end date invalid");
						}

						if (!StringUtils.equals(comments, updateUnavailabilityReason.getComments())) {
							throw new UnavailabilityValidationException("Cannot update " + code
									+ " - comments cannot be changed on a canceled record");
						}
					}
				}
			}

			return true;

		case "REMOVE_UNAVAILABLE":

			if (id == null || id == 0) {
				throw new UnavailabilityValidationException("Cannot remove record - id is unavailable");
			}

			Map<Long, UnavailabilityReason> removeUnavailabilityReasonMap = new HashMap<Long, UnavailabilityReason>();
			UnavailabilityReason removedUnavailabilityReason = null;
			@SuppressWarnings("unchecked")
			List<UnavailabilityReason> removeUnavailabilityHistoryList = (List<UnavailabilityReason>)typeList;

			for (UnavailabilityReason unavailabilityReason : removeUnavailabilityHistoryList) 
			{
				if (removedUnavailabilityReason == null && id.equals(unavailabilityReason.getId()) && "A".equals(unavailabilityReason.getStatus()))
				{
					removedUnavailabilityReason = unavailabilityReason;
					continue;
				}
				removeUnavailabilityReasonMap.put(unavailabilityReason.getId(), unavailabilityReason);
			}

			if (removedUnavailabilityReason == null) {
				throw new UnavailabilityValidationException("Cannot delete record - active " + code + " was not found");
			}

			// Check is the record was canceled
			UnavailabilityReason lastRemovedChildUnavailabilityReason = getLastChild(removeUnavailabilityReasonMap,
					removedUnavailabilityReason);
			if (lastRemovedChildUnavailabilityReason != null) {
				// Apply rule for the canceled records
				if (CANCELED_ACTION.equalsIgnoreCase(lastRemovedChildUnavailabilityReason.getAction())) {
					throw new UnavailabilityValidationException("Cannot delete record - " + code
							+ " has a canceled day");
				}
			}

			return true;

		case "CANCEL_UNAVAILABLE": // SMARTOB-4783
			/*
			 * for cancel unavail chart the UI will pass 1 code and 1 date take the date and check that it falls between
			 * the date range in unavailHistory startDate>= betn <= EndDate then check for code = chart, Jury or
			 * Vacation then return true, that is they can cancel chart for all others codes they cannot cancel the
			 * chart
			 */
			startDate = DateUtils.removeTime(startDate);
			endDate = DateUtils.setEndDayTimeToTheMinute(startDate);

			if (!CANCEL_AVAILABILITY_CODES.contains(code)) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " day");
			}

			if (DateUtils.before(startDate, DateUtils.removeTime(new Date()))) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " on a past date");
			}

			if (id == null || id == 0) {
				throw new UnavailabilityValidationException("Cannot cancel record - id is unavailable");
			}

			if (StringUtils.isBlank(code)) {
				throw new UnavailabilityValidationException(
						"Cannot cancel record - unavailability type must be defined");
			}

			if (startDate == null) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " - start date must be defined");
			}

			if (endDate != null && (!DateUtils.sameDay(startDate, endDate) && DateUtils.after(startDate, endDate))) {
				throw new UnavailabilityValidationException("Cannot cancel " + code
						+ " - start date must be earlier than end date");
			}

			// Find the existing canceled record
			Map<Long, UnavailabilityReason> cancelUnavailabilityReasonMap = new HashMap<Long, UnavailabilityReason>();
			UnavailabilityReason existingCancelingUnavailabilityReason = null;
			UnavailabilityReason overlapingChart = null;
			UnavailabilityReason overlapingVacation = null;
			UnavailabilityReason overlapingJuryDuty = null;
			UnavailabilityReason overlapingOther = null;
			@SuppressWarnings("unchecked")
			List<UnavailabilityReason> cancelUnavailabilityHistoryList = (List<UnavailabilityReason>)typeList;
			for (UnavailabilityReason unavailabilityReason : cancelUnavailabilityHistoryList) 
			{
				// Register reason in the map
				cancelUnavailabilityReasonMap.put(unavailabilityReason.getId(), unavailabilityReason);

				if (unavailabilityReason.getId().equals(id) && ACTIVE_STATUS.equals(unavailabilityReason.getStatus())) {
					existingCancelingUnavailabilityReason = unavailabilityReason;
					continue;
				}

				// if entered code and and startDate do not match the values in
				// unavailabilityHistorys, throw ERROR
				if (ACTIVE_STATUS.equalsIgnoreCase(unavailabilityReason.getStatus())
						&& !CANCELED_ACTION.equalsIgnoreCase(unavailabilityReason.getAction())
						&& unavailabilityReason.getStart() != null
						&& (DateUtils.dayOnOrBetween(startDate, unavailabilityReason.getStart(),
								unavailabilityReason.getEnd())
								|| DateUtils.sameDay(endDate, unavailabilityReason.getEnd()) || DateUtils.sameDay(
										endDate, unavailabilityReason.getStart()))) {
					String currentCode = unavailabilityReason.getCode();
					if (CANCEL_AVAILABILITY_CODES.get(0).equalsIgnoreCase(currentCode)) {
						overlapingChart = unavailabilityReason;
					} else if (CANCEL_AVAILABILITY_CODES.get(1).equalsIgnoreCase(currentCode)) {
						overlapingVacation = unavailabilityReason;
					} else if (CANCEL_AVAILABILITY_CODES.get(2).equalsIgnoreCase(currentCode)) {
						overlapingJuryDuty = unavailabilityReason;
					} else {
						overlapingOther = unavailabilityReason;
					}
				}
			}

			if (existingCancelingUnavailabilityReason == null) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " - related record was not found");
			}

			if (CANCEL_AVAILABILITY_CODES.get(0).equals(existingCancelingUnavailabilityReason.getCode())) {
				// Chart can only be canceled on its own date
				if (!DateUtils.sameDay(startDate, existingCancelingUnavailabilityReason.getStart()))
					throw new UnavailabilityValidationException("Cannot cancel " + code + " - date is different");

				if (overlapingJuryDuty != null)
					throw new UnavailabilityValidationException("Cannot cancel Chart - employee is on Jury Duty");

				if (overlapingOther != null)
					throw new UnavailabilityValidationException("Cannot cancel Chart - employee is "
							+ overlapingOther.getCode());
			} else if (overlapingOther != null) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " - employee is "
						+ overlapingOther.getCode());
			} else if (overlapingVacation != null) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " - employee is "
						+ overlapingVacation.getCode());
			} else if (overlapingJuryDuty != null) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " - employee is "
						+ overlapingJuryDuty.getCode());
			}

			if (overlapingChart != null && !overlapingChart.equals(existingCancelingUnavailabilityReason)
					&& !code.equalsIgnoreCase(overlapingChart.getCode())) {
				throw new UnavailabilityValidationException("Cannot cancel " + code + " on a Chart day");
			}

			// Check if found reason has been already canceled
			validateUnavailabilityReason (existingCancelingUnavailabilityReason, cancelUnavailabilityHistoryList, overlapingChart, startDate, endDate);
			validateUnavailabilityReason (existingCancelingUnavailabilityReason, cancelUnavailabilityHistoryList, overlapingVacation, startDate, endDate);
			validateUnavailabilityReason (existingCancelingUnavailabilityReason, cancelUnavailabilityHistoryList, overlapingJuryDuty, startDate, endDate);

			return true;

		case "REVERSE_CANCEL_UNAVAILABLE": // SMARTOB-618 same as above

			if (!CANCEL_AVAILABILITY_CODES.contains(code))
				throw new UnavailabilityValidationException("Cannot reverse cancel " + code);

			if(checkForNextDay(startDate, endDate, person, key.getLocation().getCode()))
				throw new UnavailabilityValidationException("Cannot update " + code + " - person must be removed from upcoming next day tasks.");

			ConcurrentSkipListSet<UnavailabilityReason> unavailabilityHistoryList1 = person.getUnavailabilityHistory();
			for (UnavailabilityReason unavailabilityReason : unavailabilityHistoryList1) {
				// if entered code and and startDate do not match the values in
				// unavailabilityHistorys, throw ERROR
				if (unavailabilityReason.getId().equals(id)) {
					return true;
				}
			}
			throw new UnavailabilityValidationException("Cannot reverse cancel " + code);

		case "MDA":
			List<MdaStatus> mdaStatus = (List<MdaStatus>)typeList;
			for (MdaStatus mda: mdaStatus) {
				if (mda.getStatus() == null || !mda.getStatus().equalsIgnoreCase(ACTIVE_STATUS)
						|| id != null && id.equals(mda.getId())) {
					continue;
				}

				// Skip completed status
				if (mda.getEndDate() != null && DateUtils.before(mda.getEndDate(), new Date()))
					continue;

				if (mda.getEndDate() != null
						&& (DateUtils.onOrBetween(startDate, mda.getStartDate(), mda.getEndDate()) || DateUtils
								.onOrBetween(endDate, mda.getStartDate(), mda.getEndDate()))) {
					return false;
				}

				if (mda.getEndDate() == null
						&& (startDate.after(mda.getStartDate()) || startDate.before(mda.getStartDate())
								&& endDate != null && endDate.after(mda.getStartDate()))) {
					return false;
				}

			}
			return true;

		case "SPECIAL_POSITION":
			@SuppressWarnings("unchecked")
			List<SpecialPosition> specialPositionsHistory = (List<SpecialPosition>)typeList;
			for(SpecialPosition specialPosition : specialPositionsHistory){
				
				if (specialPosition.getStatus() == null || !specialPosition.getStatus().equalsIgnoreCase(ACTIVE_STATUS)
						|| id != null && id.equals(specialPosition.getId())) {
					continue;
				}

				if (specialPosition.getEndDate() != null
						&& (DateUtils.onOrBetween(startDate, specialPosition.getStartDate(),
								specialPosition.getEndDate()) || DateUtils.onOrBetween(endDate,
										specialPosition.getStartDate(), specialPosition.getEndDate()))) {
					return false;
				}

				if (specialPosition.getEndDate() == null
						&& (startDate.after(specialPosition.getStartDate()) || startDate.before(specialPosition
								.getStartDate()) && ((endDate != null && endDate.after(specialPosition.getStartDate()))))) {
					return false;
				}

			}

		}

		return true;
	}


	private static void validateUnavailabilityReason (UnavailabilityReason existingCancelingUnavailabilityReason, 
			List<UnavailabilityReason> unavailabilityHistorySet, UnavailabilityReason overlapingUnavailabilityReason, 
			Date startDate, Date endDate)
	{
		List<UnavailabilityReason> children = getAllActiveChildren ( unavailabilityHistorySet, existingCancelingUnavailabilityReason);
		for (UnavailabilityReason childUnavailabilityReason : children)
		{
			if (CANCELED_ACTION.equalsIgnoreCase(childUnavailabilityReason.getAction()) &&
					(DateUtils.dayOnOrBetween(startDate, childUnavailabilityReason.getStart(), childUnavailabilityReason.getEnd()) ||
							DateUtils.dayOnOrBetween(endDate, childUnavailabilityReason.getStart(), childUnavailabilityReason.getEnd())))
			{
				throw new UnavailabilityValidationException("Cannot cancel already canceled " + existingCancelingUnavailabilityReason.getCode() + " day");

			}
		}

	}

	private static UnavailabilityReason getLastChild(Map<Long, UnavailabilityReason> unavailabilityReasonMap,
			UnavailabilityReason unavailabilityReason) {
		if (unavailabilityReason.getReplacedBy() == null)
			return unavailabilityReason;

		UnavailabilityReason childUnavailabilityReason = unavailabilityReasonMap.get(unavailabilityReason
				.getReplacedBy());
		if (childUnavailabilityReason != null)
			return getLastChild(unavailabilityReasonMap, childUnavailabilityReason);

		return unavailabilityReason;
	}


	private static List<UnavailabilityReason> getAllActiveChildren(List<UnavailabilityReason> unavailabilityHistorySet, UnavailabilityReason unavailabilityReason)
	{
		List<UnavailabilityReason> allActiveChildren = new ArrayList<UnavailabilityReason>();
		for (UnavailabilityReason childUnavailabilityReason : unavailabilityHistorySet) {
			if (childUnavailabilityReason.getReplaces() == null)
				continue;

			if (ACTIVE_STATUS.equalsIgnoreCase(childUnavailabilityReason.getStatus())
					&& childUnavailabilityReason.getReplaces().equals(unavailabilityReason.getId())) {
				allActiveChildren.add(childUnavailabilityReason);
			}

		}
		return allActiveChildren;
	}

	private static void checkUnavailabilitiesOverlap(Long id, String code, Date startDate, Date endDate,
			UnavailabilityReason existingUnavailabilityReason) {
		if (code.equals(existingUnavailabilityReason.getCode())
				&& ACTIVE_STATUS.equals(existingUnavailabilityReason.getStatus())
				&& (DateUtils.dayOnOrBetween(startDate, existingUnavailabilityReason.getStart(),
						existingUnavailabilityReason.getEnd()) || DateUtils.dayOnOrBetween(endDate,
								existingUnavailabilityReason.getStart(), existingUnavailabilityReason.getEnd()))) {
			if (!existingUnavailabilityReason.getId().equals(id)
					&& !CANCELED_ACTION.equals(existingUnavailabilityReason.getAction()))
				throw new UnavailabilityValidationException("Cannot update record - overlaping "
						+ existingUnavailabilityReason.getCode() + " already exists for this same calendar date range");
		}
	}

	public static boolean isValidPartialTask(List<PartialTask> partialTasks) {
		int hours = 0;
		for (PartialTask partialTask : partialTasks) {
			hours = hours + partialTask.getHours();
		}
		if (hours == 8) {
			return true;
		} else
			return false;
	}

	public static boolean isInvalidPartialTask(List<PartialTask> partialTasks) {
		int hours = 0;
		for (PartialTask partialTask : partialTasks) {
			hours = hours + partialTask.getHours();
		}
		if (hours == 8) {
			return false;
		} else
			return true;
	}

	/**
	 * SMARTOB-4338 Add Volunteer Counts
	 * 
	 * @param volunteerCountsRequest
	 * @param boardPersonHashMap
	 * @return
	 */
	public static ErrorMessage validateVolunteerCounts(VolunteerCounts volunteerCountsRequest,
			HashMap<String, BoardPerson> boardPersonHashMap) {
		if (volunteerCountsRequest.getChartVolunteers() > 99 || volunteerCountsRequest.getMandatoryChart() > 99) {
			return ErrorMessage.CHECK_VOLUNTEER_COUNT;
		}
		int count = 0;
		for (String key : boardPersonHashMap.keySet()) {
			BoardPerson boardPerson = boardPersonHashMap.get(key);
			for (UnavailabilityReason reason : boardPerson.getUnavailabilityHistory()) {
				if (reason.getCode().equalsIgnoreCase(CANCEL_AVAILABILITY_CODES.get(1))
						&& reason.getStatus().equalsIgnoreCase("A")) {
					count++;
				}
			}
		}
		if (volunteerCountsRequest.getVacationVolunteers() > count) {
			return ErrorMessage.CHECK_VACATION_VOLUNTEER_COUNT;
		}
		return null;
	}

	/**
	 * SMARTOB-5644, SMARTOB-5647
	 * 
	 * @param date
	 * @param location
	 * @param locationCache
	 * @throws OpsBoardError
	 */
	public static void validateBoardDateLocation(String date, String location, LocationCache locationCache) {
		try {
			locationCache.checkLocation(location, DateUtils.toBoardDateNoNull(date));
			Calendar cal = Calendar.getInstance();
			cal.setTime(DateUtils.toBoardDate(date));
		} catch (Exception e) {
			throw new AccessDeniedException("The value you entered is not valid!");
		}
	}


	public static void validateDetachPerson(Location boardLoc, String boardDate, String from, String to,
			Date startDate, Date endDate, String comments, List<UnavailabilityReason> unavailabilityHistory,
			List<Detachment> presentAndFutureDetachmentList, BoardPerson bp, boolean hasUnfinishedTask) throws OpsBoardValidationException {


		if(hasUnfinishedTask)
		{
			//this is a temp way of working with how validation messages are handled in UI
			//TODO overhaul the validation and error handling on server and UI				
			List<String> extErrMessages = new ArrayList<String>();
			extErrMessages.add(ErrorMessage.DETACHED_PERSON_ASSIGNED.getMessage());
			throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR, extErrMessages));						
		}


		// ConcurrentSkipListSet<UnavailabilityReason> unavailabilityHistory =
		// bp.getPerson().getUnavailabilityHistory();
		int days = 0;

		if (endDate != null) {
			days = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
		}

		if(bp.isAvailableNextDay() || !CollectionUtils.isEmpty(bp.getAssignedNextDayShifts(boardLoc.getCode()))){
			if(!DateUtils.sameDay(startDate, endDate)){
				List<String> extErrMessages = new ArrayList<String>();
				extErrMessages.add(ErrorMessage.DETACHMENT_WORKING_DAY_AHEAD.getMessage());
				throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR, extErrMessages));
			}

		}

		boolean skipUnavailabilityValidation = false; 
		if(days >= 1 || endDate == null)
			skipUnavailabilityValidation = true;
		// Single day Detachment Validation
		if (!skipUnavailabilityValidation) {
			for (UnavailabilityReason reason : unavailabilityHistory) {
				if (reason.getStatus().equalsIgnoreCase("A")
						&& !("C").equalsIgnoreCase(reason.getAction())
						&& (DateUtils.onOrBetween(startDate, reason.getStart(), reason.getEnd())
								|| DateUtils.onOrBetween(endDate, reason.getStart(), reason.getEnd()) || (endDate == null && reason
								.getEnd() == null))) {
					// this is a temp way of working with how validation messages are handled in UI
					// TODO overhaul the validation and error handling on server and UI
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add(ErrorMessage.DETACHED_PERSON_UNAVAILABLE.getMessage());
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
			}
		}

		// List<Detachment> presentAndFutureDetachmentList =
		// bp.getPerson().getPresentAndFutureDetachments(DateUtils.getTodayWith12AM());
		boolean valid = isValidDetachmentDate(startDate, endDate, DateUtils.toBoardDateNoNull(boardDate),
				presentAndFutureDetachmentList, boardLoc.getCode(),
				boardLoc.isTheSameLocation(bp.getPerson().getHomeLocation().getCode()), bp.getStartDate(),
				bp.getEndDate());
		if (!valid) {
			// this is a temp way of working with how validation messages are handled in UI
			// TODO overhaul the validation and error handling on server and UI
			List<String> extErrMessages = new ArrayList<String>();
			extErrMessages.add(ErrorMessage.DETACHMENT_ALREADY_EXISTS.getMessage());
			throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
					extErrMessages));
		}

		/*
		 * List<Detachment> presentAndFutureDetachmentList =
		 * bp.getPerson().getPresentAndFutureDetachments(DateUtils.getTodayWith12AM()); boolean valid = false;
		 * if(hasFutureDetachments && endDate != null){ valid = isValidDetachmentDate(startDate, endDate,
		 * presentAndFutureDetachmentList, boardLoc.getCode(),
		 * boardLoc.getCode().equalsIgnoreCase(bp.getPerson().getHomeLocation().getCode())); if (!valid){ //this is a
		 * temp way of working with how validation messages are handled in UI //TODO overhaul the validation and error
		 * handling on server and UI List<String> extErrMessages = new ArrayList<String>();
		 * extErrMessages.add(ErrorMessage.DETACHMENT_ALREADY_EXISTS.getMessage()); throw new
		 * OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR, extErrMessages)); } }else
		 * if(hasFutureDetachments && endDate == null){ List<String> extErrMessages = new ArrayList<String>();
		 * extErrMessages.add(ErrorMessage.PERMANENT_DETACH_NOT_POSSIBLE.getMessage()); throw new
		 * OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR, extErrMessages)); }
		 */
	}

	/*
	 * desiredDetachmentStartDate - startDate of detachment to be performed desiredDetachmentEndDate - endDate of
	 * detachment to be performed existingFutureDetachments - list of future detachments starting today locationCode -
	 * the location on which the detachment action is taken isHomeLocation - is the location on which the detachment
	 * action is taken, the home location
	 */
	public static boolean isValidDetachmentDate(Date desiredDetachmentStartDate, Date desiredDetachmentEndDate,
			Date boardDate, List<Detachment> existingFutureDetachments, String locationCode, boolean isHomeLocation,
			Date boardStartDate, Date boardEndDate) {

		// Date availableStartDate = null;

		RangeSet<Date> rangeSet = TreeRangeSet.create();
		Date now = new Date();

		if (!DateUtils.onOrBetween(now, boardStartDate, boardEndDate)) {
			return false;
		}
		// starting with an open-ended date range for the home location
		if (isHomeLocation) {
			rangeSet.add(Range.downTo(DateUtils.removeTime(boardStartDate), BoundType.CLOSED));
		}
		// if it is detached to the passed in location, then add otherwise remove
		existingFutureDetachments.forEach(d -> {
			if (locationCode.equalsIgnoreCase(d.getTo().getCode())) {
				if (d.getEndDate() == null) {
					// add it from start to infinity
					rangeSet.add(Range.downTo(d.getStartDate(), BoundType.CLOSED));
				} else {
					rangeSet.add(Range.closed(d.getStartDate(), d.getEndDate()));
				}
			} else {
				if (d.getEndDate() == null) {
					// remove it from start to infinity
					rangeSet.remove(Range.downTo(d.getStartDate(), BoundType.CLOSED));
				} else {
					// dont like adding a day after and day before - but cannot get it working without that
					rangeSet.remove(Range.open(DateUtils.getOneDayBefore(d.getStartDate()),
							DateUtils.getOneDayAfter(d.getEndDate())));
				}
			}

		})	;

		// the rangeset calculated up until this point needs to be coalesced for example [1-5][6-10] needs to become
		// [1-5][5-6][6-10]
		// create a new rangeset
		RangeSet<Date> normalizedRangeSet = TreeRangeSet.create();
		Range<Date> prevRange = null;
		for (Iterator<Range<Date>> it = rangeSet.asRanges().iterator(); it.hasNext();) {
			Range<Date> thisRange = it.next();
			// if this is the first Range, ignore
			if (prevRange != null) {
				if (DateUtils.getDayDifference(prevRange.upperEndpoint(), thisRange.lowerEndpoint()) == 1) {
					normalizedRangeSet.add(Range.closed(prevRange.upperEndpoint(), thisRange.lowerEndpoint()));
				}
			}
			normalizedRangeSet.add(thisRange);
			prevRange = thisRange;
		}

		if (desiredDetachmentEndDate == null) {
			// check to see if the desired detachment date range is within the available date range
			return normalizedRangeSet.encloses(Range.downTo(desiredDetachmentStartDate, BoundType.CLOSED));
		}
		// check to see if the desired detachment date range is within the available date range
		return normalizedRangeSet.encloses(Range.closed(desiredDetachmentStartDate, desiredDetachmentEndDate));
	}
	public static void validateUpdateDetachment(BoardPerson bp, Date startDate, Date endDate, String boardDate, Location boardLoc, boolean hasUnfinishedTask, boolean cancelCommand) throws OpsBoardValidationException{
		
		//if end date in the past cannot edit or cancel
		if (DateUtils.before(endDate, DateUtils.removeTime(new Date()))) {
			List<String> extErrMessages = new ArrayList<String>();
			extErrMessages.add(ErrorMessage.DETACHMENT_PAST_CANCEL_UNALLOWED.getMessage());
			throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
					extErrMessages));
		}		

		//if not homeLocation, it has to be the recieving location ( in order to update/cancel, the person should be found in that location)
		Location homeLocation = bp.getHomeLocation();
		if(!boardLoc.equals(homeLocation) && DateUtils.before(startDate, endDate)){

			List<Detachment> presentAndFutureDetachmentList = bp.getPerson().getPresentAndFutureDetachments(DateUtils.getTodayWith12AM());
			boolean valid = ValidationUtils.isValidDetachmentDate(DateUtils.getDayAfter(startDate, 1), endDate,DateUtils.toBoardDateNoNull(boardDate),
					presentAndFutureDetachmentList, boardLoc.getCode(),  boardLoc.getCode().equalsIgnoreCase(bp.getPerson().getHomeLocation().getCode()), bp.getStartDate(), bp.getEndDate());		
			if(!valid){
				List<String> extErrMessages = new ArrayList<String>();
				extErrMessages.add(ErrorMessage.DETACHMENT_CANNOT_DETACH_FROM_LOCATION.getMessage());
				throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
						extErrMessages));			
			}
		}
		
		//check if boardDate between newEndDate and oldEndDate and if so check for unfinished tasks
		Date boardDt= DateUtils.toBoardDateNoNull(boardDate);
		boolean boardDayInbetween;
		if(cancelCommand){
			boardDayInbetween = DateUtils.dayOnOrBetween(boardDt, startDate, endDate);
		}else
			boardDayInbetween = DateUtils.dayOnOrBetween(boardDt, DateUtils.getDayAfter(startDate, 1), endDate);
		if(boardDayInbetween && hasUnfinishedTask && (cancelCommand || DateUtils.before(startDate, endDate))){
			List<String> extErrMessages = new ArrayList<String>();
			extErrMessages.add(ErrorMessage.DETACHMENT_CANNOT_DETACH_ACTIVE_TASKS.getMessage());
			throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
					extErrMessages));				
		}
		
		
	}	
	

}
