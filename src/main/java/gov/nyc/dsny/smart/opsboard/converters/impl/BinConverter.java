package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.MaterialTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.equipment.AbstractBin.BinStatus;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.MaterialType;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Bin;

import java.util.Date;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BinConverter extends AbstractEquipmentConverter<Bin, gov.nyc.dsny.smart.opsboard.domain.equipment.Bin>{

	private MaterialTypeCache materialTypeCache;
	
	@Autowired
	public BinConverter(MaterialTypeCache materialTypeCache, BoardEquipmentCacheService boardEquipmentCache, BoardKeyFactory boardKeyFactory, LocationCache locationCache) {
		super(boardKeyFactory, locationCache, boardEquipmentCache);
		this.materialTypeCache = materialTypeCache;
	}
	
	@Override
	public gov.nyc.dsny.smart.opsboard.domain.equipment.Bin convert(Bin binUI) {
		try{
			super.postConstruct(binUI);
			gov.nyc.dsny.smart.opsboard.domain.equipment.Bin bin = new gov.nyc.dsny.smart.opsboard.domain.equipment.Bin();		
			bin.setName(binUI.getName());
			bin.setStatus(StringUtils.isNotBlank(binUI.getStatus()) ? BinStatus.valueOf(binUI.getStatus()) : BinStatus.Empty);	
			bin.setEquipment(getBoardEquipment().getEquipment());
			
			if(StringUtils.isNotBlank(binUI.getMaterialType())){
				MaterialType materialType = materialTypeCache.getMaterialTypeByCode(binUI.getMaterialType(), new Date());
				
				if(materialType == null)
					throw new OpsBoardError(ErrorMessage.MATERIAL_TYPE_NOT_FOUND);
							
				bin.setMaterial(materialType);
			}
	
			return bin;
		}catch(OpsBoardError e){
			binUI.handleErrorMessage(e.getCode());
			return null;
		}
	}

}
