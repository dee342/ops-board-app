package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public interface IToLocationCommand {
	Location getTo();

	void setTo(Location location);
}
