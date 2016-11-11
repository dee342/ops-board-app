package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;
import java.util.List;

public class MassChartRequest implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> cancelPersonIds; 
	private List<String> reverseCancelPersonIds;
	
	public List<String> getCancelPersonIds() {
		return cancelPersonIds;
	}
	public void setCancelPersonIds(List<String> cancelPersonIds) {
		this.cancelPersonIds = cancelPersonIds;
	}
	public List<String> getReverseCancelPersonIds() {
		return reverseCancelPersonIds;
	}
	public void setReverseCancelPersonIds(List<String> reverseCancelPersonIds) {
		this.reverseCancelPersonIds = reverseCancelPersonIds;
	} 
}
