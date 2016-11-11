package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;

public interface IMdaStatusCommand {
	MdaStatus getMdaStatus();
	void setMdaStatus(MdaStatus mdaStatus);
}
