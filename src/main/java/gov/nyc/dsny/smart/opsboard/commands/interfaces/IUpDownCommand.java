package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;

public interface IUpDownCommand {
	UpDown getUpDownData();
	void setUpDownData(UpDown upDown);
}
