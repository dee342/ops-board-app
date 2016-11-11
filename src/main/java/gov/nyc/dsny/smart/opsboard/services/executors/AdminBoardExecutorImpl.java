package gov.nyc.dsny.smart.opsboard.services.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminBoardExecutorImpl extends AdminExecutorAbstract implements AdminBoardExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(AdminBoardExecutorImpl.class);
	
	//*********************************************************************************************************************
	//**************************************** Initialization Methods *****************************************************
	//*********************************************************************************************************************
    @Override
	public void init()
	{
    	super.init();
	}
  
} 
