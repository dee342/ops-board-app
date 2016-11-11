package gov.nyc.dsny.smart.opsboard.services.integration.reconciliation;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.User;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.services.integration.reconciliation.impl.EquipmentReconciliationServiceImpl;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.EquipmentExecutor;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EquipmentReconciliationServiceTest {
	
	@Mock
	private EquipmentExecutor equipmentExecutor;
	
	@InjectMocks
	private EquipmentReconciliationService equipmentReconciliationService = new EquipmentReconciliationServiceImpl();
	
//	private BoardKey key = boardKeyFactory.createBoardKey("20150210", new Location("BKN01"));
	private Date time = new Date();
	private Equipment eq = new Equipment();
	private BoardEquipment be = new BoardEquipment(eq, "20150210", time, time);
	private Location from = new Location("BKN02");
	private Location to = new Location("BKN03");
	
	private String person = "SCAN";
	private Principal principal = User.getPrincipal("SCAN");
	
	
	
	@Test
	public void testAlreadyPendingDetach() throws OpsBoardError, InvalidFormatException, IOException{
	/*	OpsBoardError error = new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_PENDING_DETACH);
		equipmentReconciliationService.reconcileDetachment(key, be, from, to, person, time, "", principal, error);*/
//		Mockito.verify(equipmentExecutor, Mockito.times(1)).cancelDetach(key, be, principal);
//		Mockito.verify(equipmentExecutor, Mockito.times(1)).detach(key, be, from, to, person, time, principal);
	}
	
	@Test
	public void testAlreadyAttached() throws OpsBoardError{
		
/*		Detachment detachment = new Detachment();
		detachment.setFrom(new Location("BKS12"));
		detachment.setTo(new Location("BKS13"));
		detachment.setEquipment(be.getEquipment());
		be.getEquipment().addDetachment(detachment);
		
		OpsBoardError error = new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_ATTACHED);
		equipmentReconciliationService.reconcileAttachment(key, be, person, time, "", principal, error);*/
//		Mockito.verify(equipmentExecutor, Mockito.times(1)).detach(key, be, new Location("BKS13"), to, null, null, principal);
//		Mockito.verify(equipmentExecutor, Mockito.times(1)).attach(key, be, person, time, "", principal);
	}
}
