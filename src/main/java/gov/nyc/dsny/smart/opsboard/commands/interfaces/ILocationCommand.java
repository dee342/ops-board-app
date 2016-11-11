package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public interface ILocationCommand {
	void setLocation(Location location);
	Location getLocation();
}
