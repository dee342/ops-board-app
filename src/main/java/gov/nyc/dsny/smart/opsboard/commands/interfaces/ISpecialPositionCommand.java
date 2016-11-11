package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;

public interface ISpecialPositionCommand {
	SpecialPosition getSpecialPosition();
	void setSpecialPosition(SpecialPosition specialPosition);
}
