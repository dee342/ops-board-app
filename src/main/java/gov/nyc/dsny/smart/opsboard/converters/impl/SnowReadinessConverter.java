package gov.nyc.dsny.smart.opsboard.converters.impl;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import gov.nyc.dsny.smart.opsboard.converters.BaseConverter;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType.Load;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType.PlowType;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.SnowReadiness;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Component
public class SnowReadinessConverter implements BaseConverter<SnowReadiness, gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness>{

	@Override
	public gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness convert(SnowReadiness snowReadinessUI) {
		gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness snowReadiness = new gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness();
		snowReadiness.setChained(snowReadinessUI.isChained());
		snowReadiness.setLoad((StringUtils.isNotBlank(snowReadinessUI.getLoad())) ? Load.valueOf(snowReadinessUI.getLoad()) : Load.NONE);
		snowReadiness.setPlowType((StringUtils.isNotBlank(snowReadinessUI.getPlowType())) ? PlowType.valueOf(snowReadinessUI.getPlowType()) : PlowType.NO_PLOW);
	    snowReadiness.setPlowDirection(snowReadinessUI.getPlowDirection());
	    snowReadiness.setWorkingDown(snowReadinessUI.isWorkingDown());
	    snowReadiness.setSnowAssignment(snowReadinessUI.isSnowAssignment());
		return snowReadiness;
	}

}
