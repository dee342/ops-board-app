package gov.nyc.dsny.smart.opsboard.validation;

import org.junit.Ignore;
import org.junit.Test;

public class LdapTest {
	
	@Test
	@Ignore
	public void test() throws Exception{

		/*DefaultSpringSecurityContextSource cs = new DefaultSpringSecurityContextSource("ldaps://ldaps-dev.nycid.nycnet:636/dc=DSNY,o=nycnet");

		cs.setUserDn("cn=SMARTPsftserviceuser,ou=accounts,o=services");
		cs.setPassword(PWUtils.DEV);
		cs.afterPropertiesSet();

		//STG
//		DefaultSpringSecurityContextSource cs = new DefaultSpringSecurityContextSource("ldaps://ldaps-stg.nycid.nycnet:636/dc=DSNY,o=nycnet");
//		cs.setUserDn("cn=SMARTPsftserviceuser,ou=accounts,o=services");
//		cs.setPassword(PWUtils.STAGE);
//		cs.afterPropertiesSet();
		
		//PROD
		DefaultSpringSecurityContextSource cs = new DefaultSpringSecurityContextSource("ldaps://ldaps.nycid.nycnet:636/dc=DSNY,o=nycnet");
		cs.setUserDn("cn=SmartOpsboardServiceUser,ou=accounts,o=services");
//		cs.setPassword(PWUtils.PROD);	
		cs.afterPropertiesSet();
		
		BindAuthenticator auth = new BindAuthenticator(cs);
		auth.setUserDnPatterns(new String[]{"cn={0},ou=USERS"});
	
		CustomLdapAuthoritiesPopulator pop = new CustomLdapAuthoritiesPopulator();
		
		LdapAuthenticationProvider provider = new LdapAuthenticationProvider(auth, pop);
		provider.setUserDetailsContextMapper(new SmartUserDetailsMapper());
		//DEV
		Authentication done = provider.authenticate(new UsernamePasswordAuthenticationToken("", ""));
		SmartUserDetails details = (SmartUserDetails) done.getPrincipal();
		
		//QA
//		Authentication done = provider.authenticate(new UsernamePasswordAuthenticationToken("smartojtstg01", PWUtils.OJT01));
		done.getAuthorities();*/
	}
}
