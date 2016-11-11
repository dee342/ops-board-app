package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;

public interface IDetachmentCommand extends IFromLocationCommand, IToLocationCommand{
	Detachment getDetachment();
	void setDetachment(Detachment detachment);
}
