package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.AbstractBaseDomainEntity;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Equipmentable;

public abstract class AbstractEquipmentConverter<S extends ViewModel<T> & Equipmentable, T extends AbstractBaseDomainEntity> extends AbstractBaseConverter<S, T>{
	
	protected final BoardEquipmentCacheService boardEquipmentCache;
	protected BoardEquipment boardEquipment;
	
	public AbstractEquipmentConverter(BoardKeyFactory boardKeyFactory, LocationCache locationCache, BoardEquipmentCacheService boardEquipmentCache){
		super(boardKeyFactory, locationCache);
		this.boardEquipmentCache = boardEquipmentCache;
	}

	public void postConstruct(S vm) throws OpsBoardError{
		super.postConstruct(vm);
		String equipmentId = vm.getEquipmentId();
		this.boardEquipment = boardEquipmentCache.get(boardKey, equipmentId);
		
	}

	public BoardEquipment getBoardEquipment() {
		return boardEquipment;
	}

	public void setBoardEquipment(BoardEquipment boardEquipment) {
		this.boardEquipment = boardEquipment;
	}

	
}
