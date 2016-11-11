package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.VolunteerCounts;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskContainer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpsBoard implements Serializable {

	private static final long serialVersionUID = 1L;

	private Board board;
	private String remoteAddress;

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public OpsBoard(Board board) {
		this.board = board;
	}
	
	public OpsBoard(Board board, String remoteAddr) {
		this.board = board;
		this.remoteAddress = remoteAddr;
	}

	public List<CommandMessage> getCommandMessagesHistory() {
		return board.getCommandMessagesHistory();
	}

	public String getDate() {
		return board.getDate();
	}

	public Map<String, OpsBoardEquipment> getEquipment() {
		Map<String, OpsBoardEquipment> equipment = new HashMap<String, OpsBoardEquipment>();
		for (BoardEquipment e : board.getEquipment().values()) {
			if(e != null && e.getEquipment() != null)equipment.put(e.getEquipment().getId(), new OpsBoardEquipment(e, board.getLocation()));
		}
		return equipment;
	}

	public String getId() {
		return board.getId();
	}

	public String getLocation() {
		return board.getLocation().getCode();
	}

	public String getLocationType() {
		return board.getLocation().getType();
	}

	public Map<String, ActiveUserSession> getOnlineSessions() {
		return board.getOnlineSessions();
	}

	public Map<String, OpsBoardPerson> getPersonnel() {
		Map<String, OpsBoardPerson> personnel = new HashMap<String, OpsBoardPerson>();
		for (BoardPerson p : board.getPersonnel().values()) {
			if(p != null && p.getPerson() != null)personnel.put(p.getPerson().getId(), new OpsBoardPerson(p, board.getLocation()));
		}
		return personnel;
	}

	public Date getShiftsEndDate() {
		return board.getShiftsEndDate();
	}

	public Date getShiftsStartDate() {
		return board.getShiftsStartDate();
	}
	
	public Map<String, TaskContainer> getTaskContainers() {
		Map<String, TaskContainer> map = new HashMap<String, TaskContainer>();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer tc : board.getTaskContainers()){
			map.put(tc.getLocation().getCode(), new TaskContainer(tc, board.getLocation()));
		}	
		return map;
	}

	public VolunteerCounts getVolunteerCounts() {
		return board.getVolunteerCounts();
	}
	
	public OpsBoardQuota getBoardQuota(){
		return new OpsBoardQuota(board.getBoardQuota());
	}
	
	
}
