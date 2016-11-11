package gov.nyc.dsny.smart.opsboard.utils;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.Role;
import gov.nyc.dsny.smart.opsboard.viewmodels.reference.Location;
import gov.nyc.dsny.smart.opsboard.viewmodels.reference.WorkUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;



public class SecurityUtils {
	
	public static final String BOROUGH_LOCATION_TYPE = "borough";
	
	public static final String DISTRICT_LOCATION_TYPE = "district";

	/**
	 * Get the active authentication object.
	 * @param strict Whether to throw an exception if no authentication object is found.
	 * @return Authentication object. Can be null only in non-strict mode.
	 */
	public static Authentication getAuthentication(boolean strict) {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (strict && authentication == null) {
	        throw new AuthenticationCredentialsNotFoundException("Missing authentication object.");
	    }
	    return authentication;
	}
	
	/*
	 * create GrantedAuthorities from user roles and other user info
	 */
	public static List<GrantedAuthority> createGrantedAuthorities(Set<Role> roles, String payrollLocation, String boroLocation){
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (Role role : roles) {
			String authority = null;

			switch (role.getRoleType()) {
			case DSNYSMT_HQ:
				authority = Role.ROLE_TYPE.DSNYSMT_HQ.getAuthority();
				break;
			case DSNYSMT_ALL_BOROS:
				authority = Role.ROLE_TYPE.DSNYSMT_ALL_BOROS.getAuthority();
				break;
			case DSNYSMT_BORO_LEVEL:
				authority = Role.ROLE_TYPE.DSNYSMT_BORO_LEVEL.getAuthority() + "|" + boroLocation;
				break;
			case DSNYSMT_DISTRICT_LEVEL:
				authority = Role.ROLE_TYPE.DSNYSMT_DISTRICT_LEVEL.getAuthority() + "|" + payrollLocation;
				break;				
			default:
				break;
			}
			authorities.add(new SimpleGrantedAuthority(authority));
		}

		return authorities;
	}
	
	
	/*
	 * checks if user has access to a board, throws an exception if the user does not have access
	 * @param board that needs to be authorized
	 * @locationType locationType of the boro - borough/district
	 * @boroCode boroCode of the board, if board is boro, board = boroCode
	 */
	public static void checkAccessPermissionsForResource(String board, String locationType, String boroCode) throws AccessDeniedException{
		
		boolean authenticated = false;
		
		String[] permissions = getPermissionsFromAuthentication();
		if(permissions == null || permissions.length == 0){
			//log that there is no authorization info available
			return;
		}		
		String role, realm = null;
		role = permissions[0];
		//some authorities are coupled with location
		if(permissions.length == 2){
			realm = permissions[1];
		}	
		if(Role.ROLE_TYPE.DSNYSMT_HQ.getAuthority().equals(role)){
			//access everything
			return;
		} else if(Role.ROLE_TYPE.DSNYSMT_ALL_BOROS.getAuthority().equals(role)){
			//if type is borough or district allow
			authenticated = (BOROUGH_LOCATION_TYPE.equals(locationType) || DISTRICT_LOCATION_TYPE.equals(locationType));	
		}else if(Role.ROLE_TYPE.DSNYSMT_BORO_LEVEL.getAuthority().equals(role)){
			//get user borough
			//is board user borough or a district inside that boro
			authenticated = realm.equals(boroCode);
		}else if(Role.ROLE_TYPE.DSNYSMT_DISTRICT_LEVEL.getAuthority().equals(role)){
			authenticated = realm.equals(board);
		}
		if(!authenticated){
			throw new AccessDeniedException("Insufficient Access Priveleges for Board " + board);			
		}
	}
	
	public static String[] getPermissionsFromAuthentication(){
		Authentication authentication = SecurityUtils.getAuthentication(true);
		if(authentication.getAuthorities().size() == 0){
			//log that there is no granted authority
			return null;
		}		
		GrantedAuthority authority = authentication.getAuthorities().iterator().next();
		//example authorities DSNYSMT_HQ, DSNYSMT_BORO_LEVEL|BKNBO
		String[] permissions = authority.getAuthority().split("\\|");

		return permissions;
	}	
	 
	
}
