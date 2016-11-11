package gov.nyc.dsny.smart.opsboard.utils;


import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.LocationCommandMessage;

import java.security.Principal;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class LogContext extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;


	public LogContext(){
		super();
		this.put("AppServer", LocationUtils.GetAppServerName());
	}
	
    public void initContext(Object... params){
    	this.clear();
		this.put("AppServer", LocationUtils.GetAppServerName());
		if (params.length > 0) {
           for(Object parameter: params){
        	   enrichLogContext(parameter);
           }
		}

    }
	
	public void enrichLogContext(Object parameter){

		if (parameter==null){return;}

		if (parameter instanceof Principal){
			Principal principal = (Principal)parameter; 
			this.put("User", principal.getName());
			if (principal instanceof UsernamePasswordAuthenticationToken){
	            Object details = ((UsernamePasswordAuthenticationToken)principal).getDetails(); 
				if (details instanceof WebAuthenticationDetails){
					this.put("ClientIP", ((WebAuthenticationDetails) details).getRemoteAddress());
					this.put("SessionID", ((WebAuthenticationDetails) details).getSessionId());
				}
			}
		}
		
		if (parameter instanceof CommandMessage){
			CommandMessage message = (CommandMessage)parameter;
			addEventToLogContext("CommandMessage", message.getCommandName(), message.getLocation());
			this.put("User", message.getUser());
		}
		
		if (parameter instanceof LocationCommandMessage){
			LocationCommandMessage message = (LocationCommandMessage)parameter;
			addEventToLogContext("LocationCommandMessage", message.getCommandName(), message.getLocation());
			this.put("User", message.getUser());
		}

		if (parameter instanceof AbstractBoardCommand){
			AbstractBoardCommand abCommand = (AbstractBoardCommand)parameter;
			this.put("User", abCommand.getSystemUser());
			this.put("CommandLocation", abCommand.getBoardId());
		}
		
		if (parameter instanceof AbstractMultiBoardCommand){
			AbstractMultiBoardCommand abCommand = (AbstractMultiBoardCommand)parameter;
			this.put("User", abCommand.getSystemUser());
			this.put("CommandLocation", abCommand.getBoardId());
		}
		
		
		if (parameter instanceof HttpServletRequest){
			HttpServletRequest request = (HttpServletRequest)parameter;
			enrichLogContext(request.getUserPrincipal());
			this.put("ServletRequestMethod", request.getMethod());
			this.put("ServletRequestURI", request.getRequestURI());
			this.put("ServletRemoteIp", request.getRemoteAddr());
			if (!this.containsKey("ClientIP")) this.put("ClientIP", request.getRemoteAddr() );
		}
		
		
	};
	
	public void addEventToLogContext(String eventType, String commandName, String commandLocation){
		this.put("CommandEventType", eventType);
		this.put("CommandName", commandName);
		this.put("CommandLocation", commandLocation);
	}

	public void addAssetToLogContext(String assetType, String assetId, String assetName){
		this.put("AssetType", assetType);
		this.put("AssetId", assetId);
		this.put("AssetName", assetName);
	}

}
