package gov.nyc.dsny.smart.opsboard.services.executors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.nyc.dsny.smart.opsboard.commands.KioskCommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.dashboard.CommandCheckStatus;
import gov.nyc.dsny.smart.opsboard.commands.dashboard.CommandUpdateDashboard;
import gov.nyc.dsny.smart.opsboard.domain.Kiosk;
import gov.nyc.dsny.smart.opsboard.persistence.repos.board.KioskRepository;
import gov.nyc.dsny.smart.opsboard.viewmodels.KioskMetadata;

@Component
public class KioskExecutorImpl implements KioskExecutor{
	
	@Autowired
	private KioskRepository kioskRepository;
	
	@Autowired
	private SimpMessagingTemplate messenger;
	
	private static final Logger log = LoggerFactory.getLogger(KioskExecutorImpl.class);
	
	private HashMap<String, KioskMetadata> kioskMetadataMap = new HashMap<String, KioskMetadata>();
	private HashMap<String, Kiosk> kioskMap = new HashMap<String, Kiosk>();
	private List<Kiosk> kiosks = new ArrayList<Kiosk>();

	public List<Kiosk> getKiosks() {
		return kiosks;
	}

	public void setKiosks(List<Kiosk> kiosks) {
		this.kiosks = kiosks;
	}

	@Override
	public List<Kiosk> loadAllKiosks() throws Exception {
		kiosks = kioskRepository.findAll();
		kioskMap = (HashMap<String, Kiosk>) kiosks.stream().collect(Collectors.toMap(k -> k.getDistrict(), k -> k));
		return kiosks;
	}
	
	@Override
	public void checkBoardStatus(String location) {
		CommandCheckStatus command = new CommandCheckStatus();
		// Send to current location only
		KioskCommandMessage message = new KioskCommandMessage(command.getName(), location, command);
		messenger.convertAndSend("/topic/displayboard." + location, message);
	}

	@Override
	@Transactional
	public Kiosk save(Kiosk k) throws Exception {
		Kiosk temp = kioskRepository.save(k);
		return temp;	
	}

	@Override
	public Kiosk getKioskByLocation(String location) throws Exception {
		return kioskRepository.getKioskByLocation(location);
	}
	
	@Override
	public void updateBoardStatus(String remoteAddress, String location, boolean status) throws Exception{
		Kiosk kiosk = kioskRepository.getKioskByRemoteAddress(remoteAddress,location);
		//System.out.println(kioskMap.get(kiosk.getDistrict()));
		if(kioskMap.size() > 0 && kiosk != null && kioskMap.get(kiosk.getDistrict()) != null){
			kioskMetadataMap.put(location, new KioskMetadata(true, new Date(),kiosk.getLastPublishedDate(),kiosk.getBoardDate()));
		}else if(kioskMap.size() == 0){
			loadAllKiosks();
			if(kiosk != null && kioskMap.get(kiosk.getDistrict()) != null){
				kioskMetadataMap.put(location, new KioskMetadata(true, new Date(),kiosk.getLastPublishedDate(),kiosk.getBoardDate()));
			}else{
				log.debug("Request is from a non-kiosk with IP::"+remoteAddress);
			}
		}
	}
	
	@Override
	public void sendUpdateDashboardCommand(){
		CommandUpdateDashboard command = new CommandUpdateDashboard(kioskMetadataMap);

		// Send to current location only
		KioskCommandMessage message = new KioskCommandMessage(command.getName(), null, command);
		messenger.convertAndSend("/topic/kioskDashboard", message);
	}


	@Override
	@Transactional
	public void deleteKioskById(Kiosk k) {
		kioskRepository.delete(k);	
	}

	@Override
	public boolean isKioskExist(Kiosk k) throws Exception {
		return getKioskByLocation(k.getDistrict())!=null;
	}
	
}
