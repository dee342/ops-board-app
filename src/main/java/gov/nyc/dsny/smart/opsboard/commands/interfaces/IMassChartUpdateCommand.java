package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;

import java.util.Map;

public interface IMassChartUpdateCommand {

	Map<String, UnavailabilityReason> getReverseCancelledReasons();
	void setReverseCancelledReasons(Map<String, UnavailabilityReason> reverseCancelledReasons);
	Map<String, UnavailabilityReason> getCancelledReasons();
	void setCancelledReasons(Map<String, UnavailabilityReason> cancelledReasons);
}
