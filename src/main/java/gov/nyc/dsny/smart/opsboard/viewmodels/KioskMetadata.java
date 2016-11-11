package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;
import java.util.Date;

public class KioskMetadata implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private boolean status;
	private Date lastUpdatedTimestamp;
	private Date lastPublishedDate;
	private Date boardDate;
	
	public KioskMetadata(boolean status,Date lastUpdatedTimestamp, Date lastPublishedDate,Date boardDate) {
		super();
		this.status = status;
		this.lastPublishedDate = lastPublishedDate;
		this.setLastUpdatedTimestamp(lastUpdatedTimestamp);
		this.boardDate= boardDate;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public Date getLastPublishedDate() {
		return lastPublishedDate;
	}
	public void setLastPublishedDate(Date lastPublishedDate) {
		this.lastPublishedDate = lastPublishedDate;
	}
	public Date getBoardDate() {
		return boardDate;
	}
	public void setBoardDate(Date boardDate) {
		this.boardDate = boardDate;
	}
	public Date getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}
	public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

}
