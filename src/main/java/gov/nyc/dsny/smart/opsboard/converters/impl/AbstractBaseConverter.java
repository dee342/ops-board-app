package gov.nyc.dsny.smart.opsboard.converters.impl;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.converters.BaseConverter;
import gov.nyc.dsny.smart.opsboard.domain.AbstractBaseDomainEntity;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

import java.util.Date;

public abstract class AbstractBaseConverter<S extends ViewModel<T>, T extends AbstractBaseDomainEntity> implements BaseConverter<S, T>{
	
	private final BoardKeyFactory boardKeyFactory;
	protected final LocationCache locationCache;
	protected BoardKey boardKey;
	
	public AbstractBaseConverter(BoardKeyFactory boardKeyFactory, LocationCache locationCache){
		this.boardKeyFactory = boardKeyFactory;
		this.locationCache = locationCache;
	}

	public void postConstruct(S vm) throws OpsBoardError{
		String boardLocation = vm.getBoardLocation();
		Date boardDate = DateUtils.toBoardDate(vm.getBoardDate());
		
		Location location = locationCache.getLocation(boardLocation, boardDate);
		this.boardKey = boardKeyFactory.createBoardKey(boardDate, location);

	}


}
