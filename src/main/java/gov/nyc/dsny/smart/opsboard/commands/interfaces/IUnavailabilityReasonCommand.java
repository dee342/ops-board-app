package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;

public interface IUnavailabilityReasonCommand {
	UnavailabilityReason getUnavailableReason();
	void setUnavailableReason(UnavailabilityReason unavailabilityReason);
}
