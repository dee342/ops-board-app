package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.Utils;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.CancelDetach;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelDetachmentConverter extends AbstractEquipmentConverter<CancelDetach, Detachment>{


	private EquipmentPersistenceService persist;
	
	@Autowired
	public CancelDetachmentConverter(EquipmentPersistenceService persist, BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
		this.persist = persist;
	}
	
	@Override
	public Detachment convert(CancelDetach cancelDetach) {
		try{
			super.postConstruct(cancelDetach);

			Detachment current = persist.findLatestDetachmentByEquipmentId(boardEquipment.getEquipment().getId());
			
			if (current == null) {
				throw new OpsBoardError(ErrorMessage.DATA_ERROR_DETACHMENT_HISTORY);
			}

			Date operationTime = new Date();
			Detachment detach = new Detachment(Utils.getUserId(), Utils.getUserId(), operationTime,
					operationTime, boardEquipment.getEquipment(), "", "", current.getFrom(),
					DetachmentState.CANCELLED.getCode(), current.getTo());
			
			return detach;

		}catch(OpsBoardError e){
			cancelDetach.handleErrorMessage(e.getCode());
			return null;
		}
		
	}

}
