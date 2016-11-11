package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.DownCode;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.DownEquipment;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DownEquipmentConverter extends AbstractEquipmentConverter<DownEquipment, UpDown>{

	private DownCodeCache downCodeCache;
	
	@Autowired
	public DownEquipmentConverter(DownCodeCache downCodeCache, BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
		this.downCodeCache = downCodeCache;
	}
	
	@Override
	public UpDown convert(DownEquipment downEquipment) {
		try{
			super.postConstruct(downEquipment);
			
			UpDown current = new UpDown();
			current.setDown(true);
			Date operationTime = new Date();
			for (gov.nyc.dsny.smart.opsboard.viewmodels.equipment.EquipmentCondition conditionVm : downEquipment.getConditions()) {
				if(StringUtils.isBlank(conditionVm.getDownCode()))
					continue;
				
				EquipmentCondition condition = new EquipmentCondition();
				condition.setDown(true);
				condition.setDownCode(conditionVm.getDownCode());
				condition.setActualUser(conditionVm.getReporter());
				condition.setSystemUser(Utils.getUserId());			
				

				condition.setLastModifiedActual(conditionVm.getDateTime());

				
				condition.setLastModifiedSystem(operationTime);
				condition.setMechanic(conditionVm.getMechanic());
				condition.setRepairLocation(conditionVm.getRepairLocation());
				condition.setComments(conditionVm.getComments());
				
				DownCode downCode = downCodeCache.getDownCode(conditionVm.getDownCode(), operationTime);
				
				if(downCode == null)
					throw new OpsBoardError(ErrorMessage.DOWN_CODE_TYPE_NOT_FOUND);
				
				condition.setDownCodeType(downCode);		
				current.getConditions().add(condition);
			}
			return current;
		}catch(OpsBoardError e){
			downEquipment.handleErrorMessage(e.getCode());
			return null;
		}
		
	}

}
