package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.security.SmartLocalUserDetailsMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
@Order(1)
@Profile({"local", "local2"})
public class WebSecurityConfigurationLocal extends WebSecurityConfigurationAbstract {
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.ldapAuthentication()
		.userDetailsContextMapper(new SmartLocalUserDetailsMapper())
		.userDnPatterns("uid={0},ou=people")
		.groupSearchBase("ou=groups")
		.contextSource()
		.root("dc=dsny,dc=nyc,dc=gov")	
		.ldif("classpath:test-server.ldif");

	}
}