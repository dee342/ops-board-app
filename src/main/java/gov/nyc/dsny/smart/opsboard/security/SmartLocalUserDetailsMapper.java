package gov.nyc.dsny.smart.opsboard.security;

import java.util.Collection;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

public class SmartLocalUserDetailsMapper extends LdapUserDetailsMapper{
	  public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		  LdapUserDetails details = (LdapUserDetails) super.mapUserFromContext(ctx, username, authorities);
		  SmartUserDetails retVal = new SmartUserDetails(details);
		  
		  String payrollLocation = "QW05A";

		  
		  retVal.setPayrollLocation(payrollLocation);
		  return retVal;
	  }
}
