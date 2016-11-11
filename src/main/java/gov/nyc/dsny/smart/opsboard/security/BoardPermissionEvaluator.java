package gov.nyc.dsny.smart.opsboard.security;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.Role.ROLE_TYPE;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.SecurityUtils;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class BoardPermissionEvaluator {

	@Autowired
	private PersonnelRepository personnelRepository;
	
	@Autowired
	private LocationCache locationCache;

	public boolean hasPermission(Authentication authentication, String location, String date) throws OpsBoardError {
		 try{
			 SmartUserDetails principal = (SmartUserDetails) authentication.getPrincipal();
		 
			 Date boardDate = DateUtils.toBoardDateNoNull(date);
			 Location loc = locationCache.getLocation(location,boardDate);
			 
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_ADMINS.getAuthority()) || p.getAuthority().equals(ROLE_TYPE.DSNYSMT_HQ.getAuthority()) )){
				 return true;
			 }
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_ALL_BOROS.getAuthority()))){
				 return ((SecurityUtils.BOROUGH_LOCATION_TYPE.equals(loc.getType()) || SecurityUtils.DISTRICT_LOCATION_TYPE.equals(loc.getType())));
			 }
			 
			 //if boro, use borough code, otherwise, use location code	
			 Location payroll = locationCache.getLocation(principal.getPayrollLocation(), boardDate);
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_BORO_LEVEL.getAuthority()))){
				 return payroll.getCode().equals(loc.getCode()) || 
						 (payroll.getBorough() != null && loc.getBorough() != null && loc.getBorough().isBorough() && payroll.getBorough().getCode().equals(loc.getBorough().getCode())) ||
						 (payroll.getBorough() != null && payroll.getBorough().getCode().equals(loc.getCode())) ||  
						 (loc.getBorough() != null && loc.getBorough().getCode().equals(payroll.getCode())) ;
			 }
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_DISTRICT_LEVEL.getAuthority()))){
				 return payroll.getCode().equals(loc.getCode());
			 }
			 
			 return false;
		 }catch(Exception e){
			 return false;
		 }
	}

	public boolean hasPermission(Authentication authentication, gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit workUnit, String boardDate) throws OpsBoardError {
		 try{
			 SmartUserDetails principal = (SmartUserDetails) authentication.getPrincipal();
			 
			 Date date = DateUtils.toBoardDateNoNull(boardDate);
			 Location loc = locationCache.getLocation(workUnit.getWorkunitCode(), date);
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_ADMINS.getAuthority()) || p.getAuthority().equals(ROLE_TYPE.DSNYSMT_HQ.getAuthority()) )){
				 return true;
			 }
 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_ALL_BOROS.getAuthority()))){
				 return loc.isBorough();
			 }
			 
			 Location payroll = locationCache.getLocation(principal.getPayrollLocation(), date);	
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_BORO_LEVEL.getAuthority()))){
				 return payroll.getCode().equals(loc.getCode()) || 
						 (payroll.getBorough() != null && payroll.getBorough().getCode().equals(loc.getCode())) ||  
						 (loc.getBorough() != null && loc.getBorough().getCode().equals(payroll.getCode())) ;			
				 }
			 
			 if(principal.getAuthorities().stream().anyMatch(p -> p.getAuthority().equals(ROLE_TYPE.DSNYSMT_DISTRICT_LEVEL.getAuthority()))){
				 boolean retVal = workUnit.getLocations().stream().anyMatch(p -> p.getCode().equals(payroll.getCode()));
				 if(retVal == true)
					 workUnit.setLocations(workUnit.getLocations().stream().filter(p -> p.getCode().equals(payroll.getCode())).collect(Collectors.toCollection(LinkedHashSet::new)));
				 
				 return retVal;
			 }
			 
			 return false;
		 }catch(Exception e){
			 return false;
		 }
	}
	
	
}
