package gov.nyc.dsny.smart.opsboard.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class SmartUserDetails implements LdapUserDetails{

	private LdapUserDetails details;
	private String payrollLocation;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4746456017734694022L;
	
	public SmartUserDetails(LdapUserDetails details){
		this.details = details;
	}

	public String getPayrollLocation() {
		return payrollLocation;
	}

	public void setPayrollLocation(String payrollLocation) {
		this.payrollLocation = payrollLocation;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.details.getAuthorities();
	}

	@Override
	public String getPassword() {
		return this.details.getPassword();
	}

	@Override
	public String getUsername() {
		 return this.details.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.details.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		 return this.details.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.details.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return this.details.isEnabled();
	}

	@Override
	public String getDn() {
		return this.details.getDn();
	}
	
	 
}
