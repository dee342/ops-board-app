package gov.nyc.dsny.smart.opsboard.services.integration.reconciliation;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.security.Principal;
import java.util.Date;

public interface EquipmentReconciliationService 
{
	void reconcileDetachment(BoardKey key, BoardEquipment be,
			Detachment detach,
			Principal principal, OpsBoardError error) throws OpsBoardError;
	
	void reconcileAttachment(BoardKey key, BoardEquipment be, Detachment attach,
			Principal principal, OpsBoardError error) throws OpsBoardError;
	
	void reconcileDown(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal, OpsBoardError error) throws OpsBoardError;
	void reconcileUp(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal, OpsBoardError error) 
			throws OpsBoardError;
	void reconcileUpdateDown(BoardKey key, BoardEquipment be, UpDown newUpDown, UpDown oldUpDown, Principal principal, 
			OpsBoardError error)  throws OpsBoardError;
}
