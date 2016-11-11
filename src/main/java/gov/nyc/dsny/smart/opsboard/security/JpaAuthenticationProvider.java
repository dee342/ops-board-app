package gov.nyc.dsny.smart.opsboard.security;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.Role.ROLE_TYPE;
import gov.nyc.dsny.smart.opsboard.domain.User;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.user.UserRepository;
import gov.nyc.dsny.smart.opsboard.utils.SecurityUtils;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JpaAuthenticationProvider implements AuthenticationProvider {

	public static final String MOCK_AUTH_PAYROLL_LOCATION = "BKN16";
	
	private static final Logger log = LoggerFactory.getLogger(User.class);
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private PersonnelRepository personnelRep;
	
	@Autowired
	private LocationCache locationCache;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();

		List<User> users = userRepo.findByUsernameAndPassword(username, password);

		if (0 == users.size()) {
			log.info("authenticate - Username not found or password is invalid.");
			throw new BadCredentialsException("Username not found or password is invalid.");
		}
		
		List<GrantedAuthority> authorities = null;
		
		try{
			
			if(users.get(0).getRoles().stream().anyMatch(p -> p.getRoleType().equals(ROLE_TYPE.DSNYSMT_DISTRICT_LEVEL) || p.getRoleType().equals(ROLE_TYPE.DSNYSMT_BORO_LEVEL)))
			{	
				authorities = SecurityUtils.createGrantedAuthorities(users.get(0).getRoles(), MOCK_AUTH_PAYROLL_LOCATION,
						locationCache.getLocation(MOCK_AUTH_PAYROLL_LOCATION, new Date()).getBoroughCode());
			}else{
				authorities = SecurityUtils.createGrantedAuthorities(users.get(0).getRoles(), null, null);
			}
				
		}
		catch(OpsBoardError e){
			throw new AuthenticationServiceException("Exception while getting Granted Authorities", e);
		}
		
		for (GrantedAuthority grantedAuthority : authorities) {
			log.info("authenticate - Role '{}' added to user",grantedAuthority.getAuthority() );
		}

		return new UsernamePasswordAuthenticationToken(username, password, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
