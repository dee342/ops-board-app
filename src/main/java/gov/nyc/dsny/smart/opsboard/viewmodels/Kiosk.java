package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;
import java.util.Date;

public class Kiosk implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String username;

	private String district;

	private String groupName;
		
	private String hostname;

	private String ipAddress;

	private String subnetMask;

	private String defaultGateway;

	private Date boardDate; 

	private Date lastPublishedDate; 
	
	private boolean status;


	public Kiosk(gov.nyc.dsny.smart.opsboard.domain.Kiosk kiosk){
		if(kiosk!=null){
			this.id = kiosk.getId();
			this.username = kiosk.getUsername();
			this.district= kiosk.getDistrict();
			this.groupName= kiosk.getGroupName();
			this.hostname= kiosk.getHostname();
			this.ipAddress=kiosk.getIpAddress();
			this.subnetMask=kiosk.getSubnetMask();
			this.defaultGateway= kiosk.getDefaultGateway();
			this.boardDate= kiosk.getBoardDate();
			this.lastPublishedDate = kiosk.getLastPublishedDate();
		}
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getDistrict() {
		return district;
	}


	public void setDistrict(String district) {
		this.district = district;
	}
	
	public String getGroupName() {
		return groupName;
	}


	public void setGroupName(String group) {
		this.groupName = group;
	}


	public String getHostname() {
		return hostname;
	}


	public void setHostname(String hostname) {
		this.hostname = hostname;
	}


	public String getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	public String getSubnetMask() {
		return subnetMask;
	}


	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}


	public String getDefaultGateway() {
		return defaultGateway;
	}


	public void setDefaultGateway(String defaultGateway) {
		this.defaultGateway = defaultGateway;
	}


	public Date getBoardDate() {
		return boardDate;
	}


	public void setBoardDate(Date boardDate) {
		this.boardDate = boardDate;
	}


	public Date getLastPublishedDate() {
		return lastPublishedDate;
	}


	public void setLastPublishedDate(Date lastPublishedDate) {
		this.lastPublishedDate = lastPublishedDate;
	}


	public boolean isStatus() {
		return status;
	}


	public void setStatus(boolean status) {
		this.status = status;
	}


	@Override
	public String toString() {
		return "Kiosk [id=" + id + ", username=" + username + ", district="
				+ district + ", groupName=" + groupName + ", hostname="
				+ hostname + ", ipAddress=" + ipAddress + ", subnetMask="
				+ subnetMask + ", defaultGateway=" + defaultGateway
				+ ", boardDate=" + boardDate + ", lastPublishedDate="
				+ lastPublishedDate + "]";
	}

}
