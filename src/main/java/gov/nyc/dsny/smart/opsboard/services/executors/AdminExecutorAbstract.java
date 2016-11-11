package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.integration.domain.RefDataUpdatesStatus;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.facade.OutgoingIntegrationFacade;
import gov.nyc.dsny.smart.opsboard.integration.repo.RefDataUpdatesStatusRepo;
import gov.nyc.dsny.smart.opsboard.integration.service.IntegrationErrorHandlingService;
import gov.nyc.dsny.smart.opsboard.misc.AdminCommandMessage;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public abstract class AdminExecutorAbstract
{
//	private static final Logger logger = LoggerFactory.getLogger(AdminExecutorAbstract.class);
	
	@Value("${first.board.date}")
	protected String firstBoardDate;
	
	@Value("#{ environment['prune.history'] ?: false}")
	protected boolean CUT_OUT_HISTORY;
	
	protected static final int HISTORY_DAYS = -30;
	
	private String DATA_LOAD_STATUS_QUEUE = "/topic/loadStatusUpdate";
	private String DATA_LOAD_ERROR_QUEUE = "/topic/loadError";

	@Autowired
	private SimpMessagingTemplate messenger;
	
	@Autowired
	protected OutgoingIntegrationFacade integrationFacade;
	
	@Autowired
	protected RefDataUpdatesStatusRepo refDataUpdatesStatusRepository;
	
	@Autowired
	protected IntegrationErrorHandlingService integrationErrorHandlingService;
	
	protected List<DataLoadException> dataLoadExceptions = new ArrayList<DataLoadException>();
    protected boolean failed;
    protected Date today, historical30DaysDate, pastDate;
    protected Long historyLinkId;
	//*********************************************************************************************************************
	//**************************************** Initialization Methods *****************************************************
	//*********************************************************************************************************************
	protected void init()
	{
    	dataLoadExceptions = new ArrayList<DataLoadException>();
		failed = false;
		historyLinkId = Calendar.getInstance().getTimeInMillis();
		
		today = DateUtils.removeTime(new Date());
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, HISTORY_DAYS);
		historical30DaysDate = DateUtils.removeTime(c.getTime()); 
		
		pastDate = DateUtils.removeTime(DateUtils.getPastDate());
	}

    protected void handleLoadException(ErrorMessage errorMessage, Throwable e, boolean shouldRethrow, String... additionalTextMessages) throws DataLoadException
    {
    	DataLoadException dataLoadException = integrationErrorHandlingService.handleDataLoadException (errorMessage, e, additionalTextMessages);
    	dataLoadExceptions.add(dataLoadException);
    	if (shouldRethrow)
    	{
    		failed = true;
    		throw dataLoadException;
    	}
    }
    
    protected Date getNewEffectiveStartDate(List<?> list)
    {
    	if (list != null && list.size() > 0)
  			return today; 
  		return pastDate;
    }
    
    
    protected void sendRefreshCacheCommand(CommandRefreshCaches adminCommand) 
    {
	    AdminCommandMessage message = new AdminCommandMessage(adminCommand.getName(), adminCommand.getCacheName(), DateUtils.toStringBoardDate(new Date()), "admin", adminCommand);
		messenger.convertAndSend(CommandRefreshCaches.RABBIT_REFRESH_CACHES_TOPIC_NAME, message);
    }

    
    protected void sendLoadPercentageMessageToUI(String text) 
    {
    	messenger.convertAndSend(DATA_LOAD_STATUS_QUEUE, text);
    }

    protected void sendLoadingErrorMessageToUI(String errorMessage) 
    {
    	messenger.convertAndSend(DATA_LOAD_ERROR_QUEUE, errorMessage);
    }
    
    protected String getStoredProcedureErrorText(Long spId)
    {
    	if (spId != null)
    	{
    		RefDataUpdatesStatus refDataUpdatesStatus = refDataUpdatesStatusRepository.findOne(spId);
    		if (refDataUpdatesStatus == null || RefDataUpdatesStatus.SUCCESS_STATUS.equals(refDataUpdatesStatus.getStatus()))
    		{
    			return null;
    		}
    		else
    		{
    			return (StringUtils.isNotBlank(refDataUpdatesStatus.getText())? refDataUpdatesStatus.getText() : "Unknown Error");
    		}
    	}
    	
    	return null;
    }

    protected String getFormattedMessage(String message, Object[] args)
    {
    	try
    	{
	    	if(ArrayUtils.isNotEmpty(args)){
	    		return MessageFormat.format(message, args);
	    	}
	    	return message;
    	}
    	catch (Throwable t)
    	{
    		return message;
    	}
    }

} 
