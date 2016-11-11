package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.domain.Role.ROLE_TYPE;
import gov.nyc.dsny.smart.opsboard.security.LogoutSuccessHandler;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

@Component
public abstract class WebSecurityConfigurationAbstract extends WebSecurityConfigurerAdapter {

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()					
			.antMatchers("/admin/**").hasRole(ROLE_TYPE.DSNYSMT_ADMINS.name())
			.antMatchers("/cache/**").hasAnyRole(ROLE_TYPE.DSNYSMT_ADMINS.name(), ROLE_TYPE.DSNYSMT_CACHE_ADMINS.name(), ROLE_TYPE.CACHE_ADMINS.name())
			.antMatchers("/services/**").permitAll() //used for PSFT and SCAN. Needs rework TODO
			.antMatchers("/**").hasAnyRole(Arrays.stream(ROLE_TYPE.values()).map(c -> c.name()).toArray(String[]::new))
			.anyRequest().authenticated();

		http.formLogin().defaultSuccessUrl("/").loginPage("/login").failureUrl("/login?error").permitAll().and()
				.logout().logoutSuccessUrl("/login?logout").logoutUrl("/logout").addLogoutHandler(logoutSuccessHandler)
		.permitAll();
		
	}
	
	@Override
	public void configure(WebSecurity web){
		web.ignoring()		
		.antMatchers("/fonts/**")
		.antMatchers("/images/**")
		.antMatchers("/bower_components/**")
		.antMatchers("/libs/**")
		.antMatchers("/control*/**")
		.antMatchers("/scripts/**")
		.antMatchers("/styles/**")
		.antMatchers("/views/**")
		.antMatchers("/test/*/*")
		.antMatchers("/static/**");
	}
}