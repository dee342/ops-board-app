package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;

import gov.nyc.dsny.smart.opsboard.domain.board.BoardQuota;


public class OpsBoardQuota implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int sanitationWorkers;
	private int supervisors;
	private int superintendents;
	private int rearLoaders;
	private int dualBins;
	private int mechanicalBrooms;
	private int roros;
	private int ezPacks;
	private String boardId;
	private String boardDate;

	public OpsBoardQuota(BoardQuota boardQuota) {
		if(boardQuota != null){
			this.sanitationWorkers = boardQuota.getSanitationWorkers();
			this.supervisors = boardQuota.getSupervisors();
			this.superintendents = boardQuota.getSuperintendents();
			this.rearLoaders = boardQuota.getRearLoaders();
			this.dualBins = boardQuota.getDualBins();
			this.mechanicalBrooms = boardQuota.getMechanicalBrooms();
			this.roros = boardQuota.getRoros();
			this.ezPacks = boardQuota.getEzPacks();
			this.boardId = boardQuota.getBoardId();
			this.boardDate = boardQuota.getBoardDate();
			
		}
	}

	@Override
	public String toString() {
		return "OpsBoardQuota [sanitationWorkers=" + sanitationWorkers + ", supervisors=" + supervisors  + ", superintendents=" + superintendents 
				+ ", rearLoaders=" + rearLoaders  + ", dualBins=" + dualBins  + ", mechanicalBrooms=" + mechanicalBrooms 
				+ ", roros=" + roros  + ", ezPacks=" + ezPacks + "]";
	}

	public int getSanitationWorkers() {
		return sanitationWorkers;
	}

	public void setSanitationWorkers(int sanitationWorkers) {
		this.sanitationWorkers = sanitationWorkers;
	}

	public int getSupervisors() {
		return supervisors;
	}

	public void setSupervisors(int supervisors) {
		this.supervisors = supervisors;
	}

	public int getSuperintendents() {
		return superintendents;
	}

	public void setSuperintendents(int superintendents) {
		this.superintendents = superintendents;
	}

	public int getRearLoaders() {
		return rearLoaders;
	}

	public void setRearLoaders(int rearLoaders) {
		this.rearLoaders = rearLoaders;
	}

	public int getDualBins() {
		return dualBins;
	}

	public void setDualBins(int dualBins) {
		this.dualBins = dualBins;
	}

	public int getMechanicalBrooms() {
		return mechanicalBrooms;
	}

	public void setMechanicalBrooms(int mechanicalBrooms) {
		this.mechanicalBrooms = mechanicalBrooms;
	}

	public int getRoros() {
		return roros;
	}

	public void setRoros(int roros) {
		this.roros = roros;
	}

	public int getEzPacks() {
		return ezPacks;
	}

	public void setEzPacks(int ezPacks) {
		this.ezPacks = ezPacks;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public String getBoardDate() {
		return boardDate;
	}

	public void setBoardDate(String boardDate) {
		this.boardDate = boardDate;
	}	
	
	


}