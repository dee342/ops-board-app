/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;

/**
 * @author nasangameshwaran
 *
 */
public interface IBoardEquipmentCommand {
	void setBoardEquipment(BoardEquipment boardEquipment);
	BoardEquipment getBoardEquipment();
}
