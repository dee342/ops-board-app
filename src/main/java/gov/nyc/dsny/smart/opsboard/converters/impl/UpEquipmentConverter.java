package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.UpEquipment;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpEquipmentConverter extends AbstractEquipmentConverter<UpEquipment, UpDown>{

	private EquipmentPersistenceService persist;
	
	@Autowired
	public UpEquipmentConverter(EquipmentPersistenceService persist, BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
		this.persist = persist;
	}
	
	@Override
	public UpDown convert(UpEquipment upEquipment) {
		try{
			super.postConstruct(upEquipment);
			UpDown prevUp = persist.findLatestUpDownByEquipmentId(boardEquipment.getEquipment().getId());
			
			if (prevUp == null) {
				throw new OpsBoardError(ErrorMessage.DATA_ERROR_UPDOWN_HISTORY);
			}
			
			UpDown curUp = new UpDown();
			curUp.setDown(false);
			Date operationTime = new Date();
			for (EquipmentCondition old : prevUp.getConditions()) {
				EquipmentCondition c = new EquipmentCondition();
				c.setReplaces(old.getId());
				c.setDown(false);
				c.setDownCode(old.getDownCode());
				c.setActualUser(upEquipment.getReporter());
				c.setSystemUser(Utils.getUserId());

				c.setLastModifiedActual(upEquipment.getDatetime());

				c.setLastModifiedSystem(operationTime);
				c.setMechanic(upEquipment.getMechanic());
				c.setRepairLocation(old.getRepairLocation());
				curUp.getConditions().add(c);
			}
			
			// Save to DB
			
			UpDown upDown = new UpDown(Utils.getUserId(), Utils.getUserId(), operationTime,
					operationTime, false, curUp.getConditions());
			
			return upDown;
		}catch(OpsBoardError e){
			upEquipment.handleErrorMessage(e.getCode());
			return null;
		}
		
	}

}
