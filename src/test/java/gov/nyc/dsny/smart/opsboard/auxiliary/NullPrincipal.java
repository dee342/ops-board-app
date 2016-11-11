package gov.nyc.dsny.smart.opsboard.auxiliary;

import java.security.Principal;

public class NullPrincipal implements Principal {

private String name;
	
	public NullPrincipal(String name) {
	super();
	this.name = name;
}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

}
