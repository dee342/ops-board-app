package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;

public interface IShiftCommand {
	void setShift(Shift shift);
	Shift getShift();
}
