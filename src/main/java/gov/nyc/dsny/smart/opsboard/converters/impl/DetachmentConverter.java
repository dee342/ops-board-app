package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.Utils;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Detach;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DetachmentConverter extends AbstractEquipmentConverter<Detach, Detachment>{

	@Autowired
	public DetachmentConverter(BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
	}
	
	@Override
	public Detachment convert(Detach detach) {
		try{
			super.postConstruct(detach);		
					
			Location from = locationCache.getLocation(detach.getFrom(), boardKey.toDate());
			Location to = locationCache.getLocation(detach.getTo(), boardKey.toDate());

			// Save to DB
			Date operationTime = new Date();
			Detachment detachment = new Detachment(Utils.getUserId(), Utils.getUserId(), detach.getDatetime(),
					operationTime, boardEquipment.getEquipment(), "", detach.getDriver(), from,
					DetachmentState.PENDING.getCode(), to);
			
			return detachment;
				
				
			
		}catch(OpsBoardError e){
			detach.handleErrorMessage(e.getCode());
			return null;
		}
		
	}

}
