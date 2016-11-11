package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.misc.AdminCommandMessage;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.WebApplicationContext;

public class AbstractAdminController {

	@Autowired
	private WebApplicationContext appContext;
	
	@Autowired
	protected SimpMessagingTemplate messenger;	
    protected void sendRefreshCacheCommand(CommandRefreshCaches adminCommand) 
    {
	    AdminCommandMessage message = new AdminCommandMessage(adminCommand.getName(), adminCommand.getCacheName(), DateUtils.toStringBoardDate(new Date()), "admin", adminCommand);
		messenger.convertAndSend(CommandRefreshCaches.RABBIT_REFRESH_CACHES_TOPIC_NAME, message);
    }
    
    protected ResponseEntity<String> refreshReferenceDataCaches(){
    	CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.ALL_CACHES);
		sendRefreshCacheCommand(adminCommand);
        return new ResponseEntity<String>("Reference data caches were successfully refreshed", HttpStatus.OK);
    }

}
