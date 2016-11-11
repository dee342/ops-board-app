package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public interface IFromLocationCommand {
	Location getFrom();

	void setFrom(Location location);
}
