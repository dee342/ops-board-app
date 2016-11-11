package gov.nyc.dsny.smart.opsboard.converters;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.AbstractBaseDomainEntity;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Equipmentable;

import org.springframework.core.convert.converter.Converter;

public interface BaseConverter<S extends ViewModel<T>, T extends AbstractBaseDomainEntity> extends Converter<S, T>{	
}
