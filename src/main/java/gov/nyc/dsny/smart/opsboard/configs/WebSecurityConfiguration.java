package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.security.CustomLdapAuthoritiesPopulator;
import gov.nyc.dsny.smart.opsboard.security.LogoutSuccessHandler;
import gov.nyc.dsny.smart.opsboard.security.SmartUserDetailsMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
@Order(2)
@Profile({"!local", "!local2"})
public class WebSecurityConfiguration extends WebSecurityConfigurationAbstract {

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;
	
	@Autowired
	private Environment environment;
	
	@Value("#{ environment['ldap.url'] }")
	private String ldapUrl;
	
	@Value("#{ environment['ldap.service.username'] }")
	private String ldapUsername;
	
	@Value("#{ environment['ldap.service.password'] }")
	private String ldapPassword;
	
	@Autowired
	private PersonnelRepository personRepo;
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.ldapAuthentication()
		.contextSource()
			.managerDn(ldapUsername)
			.managerPassword(ldapPassword)
			.url(ldapUrl)
		.and()
		.userDnPatterns("cn={0},ou=USERS")
		.ldapAuthoritiesPopulator(new CustomLdapAuthoritiesPopulator())
		.userDetailsContextMapper(new SmartUserDetailsMapper(personRepo));
		

	}

}