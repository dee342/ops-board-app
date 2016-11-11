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
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Attach;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttachmentConverter extends AbstractEquipmentConverter<Attach, Detachment>{


	private EquipmentPersistenceService persist;
	
	@Autowired
	public AttachmentConverter(EquipmentPersistenceService persist, BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
		this.persist = persist;
	}
	
	@Override
	public Detachment convert(Attach attach) {
		try{
			super.postConstruct(attach);

			Detachment current = persist.findLatestDetachmentByEquipmentId(attach.getEquipmentId());
			
			/*
			 * Duplicated in executor for now.
			 */
			if (current == null) {
				throw new OpsBoardError(ErrorMessage.INT_ATTACHMENT_WITHOUT_DETACHMENT);			
			}

			// Save to DB
			Date operationTime = new Date();
			Detachment detach = new Detachment(attach.getReceivedBy(), Utils.getUserId(), attach.getReceivedDatetime(), operationTime,
					boardEquipment.getEquipment(), attach.getRemarks(), attach.getReceivedBy(), current.getFrom(), DetachmentState.ACCEPTED.getCode(),
					current.getTo());
			
			return detach;
		
	
		}catch(OpsBoardError e){
			attach.handleErrorMessage(e.getCode());
			return null;
		}
		
	}

}
