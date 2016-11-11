package gov.nyc.dsny.smart.opsboard.commands.person.unavailable;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IMassChartUpdateCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPersonMetaData;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IMultiBoardCommandAnnotation(commandName = "MassChartUpdate")
public class CommandMassChartUpdate extends AbstractMultiBoardCommand implements IMassChartUpdateCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String,UnavailabilityReason> cancelledReasons = new HashMap<String,UnavailabilityReason>();
	private Map<String,UnavailabilityReason> reverseCancelledReasons = new HashMap<String,UnavailabilityReason>();
	private List<OpsBoardPersonMetaData> cancelledPersonsMetaData = new ArrayList<OpsBoardPersonMetaData>();
	private List<OpsBoardPersonMetaData> reverseCancelledPersonsMetaData = new ArrayList<OpsBoardPersonMetaData>();

	public CommandMassChartUpdate(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandMassChartUpdate(String id, String name,
			Map<String,UnavailabilityReason> cancelledReasons,
			Map<String,UnavailabilityReason> reverseCancelledReasons, Date systemTime) {
		super(id,name,systemTime,false);
		this.cancelledReasons = cancelledReasons;
		this.reverseCancelledReasons = reverseCancelledReasons;
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		//For Cancel Charts 
		if(getCancelledReasons().size() > 0){
			List<BoardPerson> cancelledBps = board.cancelMultipleCharts(getCancelledReasons());
			for(BoardPerson bp:cancelledBps){
				OpsBoardPersonMetaData cancelledPersonMetaData  = new OpsBoardPersonMetaData();
				cancelledPersonMetaData.setState(bp.getState(board.getLocation()).getState());
				cancelledPersonMetaData.setAssigned(bp.isAssigned(board.getLocation().getCode()));
				cancelledPersonMetaData.setAssignedAnywhere(bp.isAssigned());
				cancelledPersonMetaData.setBoardPersonId(bp.getId());
				cancelledPersonMetaData.setPersonId(bp.getPerson().getId());
				cancelledPersonMetaData.setActiveUnavailabilityReasons(bp.getActiveUnavailabilityReasons());
				cancelledPersonsMetaData.add(cancelledPersonMetaData);

			}
			setCancelledPersonsMetaData(cancelledPersonsMetaData);
		}
		//For Reverse Cancel Charts  
		List<BoardPersonAndTasks> bpts = new ArrayList<BoardPersonAndTasks>();;
		if(getReverseCancelledReasons().size() > 0){
			bpts= board.reverseCancelMultipleCharts(getReverseCancelledReasons());
			for(BoardPersonAndTasks bpt: bpts)
			{
				OpsBoardPersonMetaData revCancelledPersonMetaData = updatePersonAndTasks(board, bpt);	
				reverseCancelledPersonsMetaData.add(revCancelledPersonMetaData);				 
			}
			setReverseCancelledPersonsMetaData(reverseCancelledPersonsMetaData);
		}



		// Create audit message
		createAuditMessage(board, getCancelledReasons().keySet(),getReverseCancelledReasons().keySet());

		//Add command to history
		board.addCommandToHistory(this);
	}

	protected OpsBoardPersonMetaData updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {
		// Set person details
		OpsBoardPersonMetaData revCancelledPersonMetaData = new OpsBoardPersonMetaData();;		
		if (bpts.getBoardPerson() != null) {
			revCancelledPersonMetaData.setState(bpts.getBoardPerson().getState(board.getLocation()).getState());
			revCancelledPersonMetaData.setAssigned(bpts.getBoardPerson().isAssigned(board.getLocation().getCode()));
			revCancelledPersonMetaData.setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
			revCancelledPersonMetaData.setBoardPersonId(bpts.getBoardPerson().getId());
			revCancelledPersonMetaData.setPersonId(bpts.getBoardPerson().getPerson().getId());
			revCancelledPersonMetaData.setActiveUnavailabilityReasons(bpts.getBoardPerson().getActiveUnavailabilityReasons());
		}
		// Set task details
		if (bpts.getTasks() != null) {
			revCancelledPersonMetaData.setTasks(convertPersonTasksToTaskAssigments(bpts));
		}
		return revCancelledPersonMetaData;
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

	private void createAuditMessage(Board board, Set<String> cancelledPersonIds,
			Set<String> reverseCancelledPersonIds) {
		StringBuilder sb = new StringBuilder();
		if(cancelledPersonIds.size()>0)
			sb.append("Cancelled Chart for persons ");
		int cancelIndex=0;
		int revCancelIndex=0;
		for(String cancelledPersonId: cancelledPersonIds){
			cancelIndex++;
			if(!(cancelIndex==cancelledPersonIds.size())){
				sb.append("[person:" + cancelledPersonId + "] & ");
			}else{
				sb.append("[person:" + cancelledPersonId + "] on chart date: "+board.getDate());
			}
		}
		if(reverseCancelledPersonIds.size()>0)
			sb.append("Reverse Cancelled Chart for persons ");
		for(String reverseCancelPersonId: reverseCancelledPersonIds){
			revCancelIndex++;
			if(!(revCancelIndex==reverseCancelledPersonIds.size())){
				sb.append("[person:" + reverseCancelPersonId + "] & ");
			}else{
				sb.append("[person:" + reverseCancelPersonId + "] on chart date: "+board.getDate());
			}
		}
		setAuditMessage(sb.toString());
	}

	@Override
	public boolean matchBoard(Board board, Location location) {
		return board.getId().equals(getBoardId()); // only applicable to a single board
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append(super.toString());

		return builder.toString();
	}

	public Map<String, UnavailabilityReason> getCancelledReasons() {
		return cancelledReasons;
	}

	public void setCancelledReasons(
			Map<String, UnavailabilityReason> cancelledReasons) {
		this.cancelledReasons = cancelledReasons;
	}

	public Map<String, UnavailabilityReason> getReverseCancelledReasons() {
		return reverseCancelledReasons;
	}

	public void setReverseCancelledReasons(
			Map<String, UnavailabilityReason> reverseCancelledReasons) {
		this.reverseCancelledReasons = reverseCancelledReasons;
	}

	public List<OpsBoardPersonMetaData> getCancelledPersonsMetaData() {
		return cancelledPersonsMetaData;
	}

	public void setCancelledPersonsMetaData(
			List<OpsBoardPersonMetaData> cancelledPersonsMetaData) {
		this.cancelledPersonsMetaData = cancelledPersonsMetaData;
	}

	public List<OpsBoardPersonMetaData> getReverseCancelledPersonsMetaData() {
		return reverseCancelledPersonsMetaData;
	}

	public void setReverseCancelledPersonsMetaData(
			List<OpsBoardPersonMetaData> reverseCancelledPersonsMetaData) {
		this.reverseCancelledPersonsMetaData = reverseCancelledPersonsMetaData;
	}

}
