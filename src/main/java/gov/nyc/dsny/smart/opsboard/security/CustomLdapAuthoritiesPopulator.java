package gov.nyc.dsny.smart.opsboard.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator{

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(
			DirContextOperations userData, String username) {

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		String[] groupMemberships = userData.getStringAttributes("groupmembership");
		
		if(groupMemberships == null || groupMemberships.length < 1)
			return authorities;
		
		for(String groupMembership : groupMemberships){
			String[] attributes = StringUtils.split(groupMembership, ",");
			if(attributes == null || attributes.length < 1)
				continue;
			
			String roleCn = attributes[0];
			
			if(StringUtils.isBlank(roleCn))
				continue;
			
			String[] roleKeyVal = StringUtils.split(roleCn, "=");
			
			if(roleKeyVal == null || roleKeyVal.length < 2)
				continue;
			
			String role = StringUtils.upperCase("role_"+roleKeyVal[1]);
			authorities.add(new SimpleGrantedAuthority(role));			

		}
		
		return authorities;
		
				
	}

}
