package gov.nyc.dsny.smart.opsboard.commands.interfaces;


public interface INextDayCommand {
	String getPersonId();
	void setPersonId(String personId);
	boolean isAssignedTomorrow();
	void setAssignedTomorrow(boolean assignedTomorrow);
}
