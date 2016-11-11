package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.domain.Kiosk;

import java.util.List;


public interface KioskExecutor {
	List<Kiosk> loadAllKiosks() throws Exception;
	Kiosk save(Kiosk k) throws Exception;
	void deleteKioskById(Kiosk kiosk);
	Kiosk getKioskByLocation(String location) throws Exception;
	public void updateBoardStatus(String remoteAddress, String location, boolean status) throws Exception;
	public void checkBoardStatus(String location);
	public void sendUpdateDashboardCommand();
	public boolean isKioskExist(Kiosk k) throws Exception;
}
