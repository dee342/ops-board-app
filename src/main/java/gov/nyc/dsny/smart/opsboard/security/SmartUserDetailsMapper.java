package gov.nyc.dsny.smart.opsboard.security;

import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;

import java.util.Collection;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

public class SmartUserDetailsMapper extends LdapUserDetailsMapper{
	
	private PersonnelRepository personRepo;
	
	public SmartUserDetailsMapper(){
		super();
	}
	
	public SmartUserDetailsMapper(PersonnelRepository personRepo){
		this();
		this.personRepo = personRepo;
	}
	
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		LdapUserDetails details = (LdapUserDetails) super.mapUserFromContext(ctx, username, authorities);
		SmartUserDetails retVal = new SmartUserDetails(details);
		if(personRepo == null)
			retVal.setPayrollLocation(ctx.getStringAttribute("physicaldeliveryofficename"));
		else
			retVal.setPayrollLocation(personRepo.findPayrollLocation(ctx.getStringAttribute("workforceID")));
		
		return retVal;
	}
}
