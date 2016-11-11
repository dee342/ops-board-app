package gov.nyc.dsny.smart.opsboard.jobs;

import java.util.ArrayList;
import java.util.Collection;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.QuartzJobBean;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.Kiosk;
import gov.nyc.dsny.smart.opsboard.services.executors.KioskExecutor;

public class DashboardJob extends QuartzJobBean {

	private static final Logger logger = LoggerFactory.getLogger(DashboardJob.class);
	
	@Qualifier("generalTaskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor generalTaskExecutor;
	
	@Autowired
	private KioskExecutor kioskExecutor;
	
	private static Collection<Kiosk> kiosks = new ArrayList<Kiosk>();
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			kiosks = getKioskStatus(kiosks);
		} catch (OpsBoardError e) {
			logger.error("Exception while running dashboard job", e);
			logger.error(e.getLocalizedMessage());
		}
	}

	private Collection<Kiosk> getKioskStatus(Collection<Kiosk> kiosks) throws OpsBoardError {

		logger.info("DashBoard Job started.");

		try {
			if(kiosks.isEmpty()){
				kiosks = kioskExecutor.loadAllKiosks();
			}
			
		} catch (Exception e) {
			logger.error("Exception while loading kiosks", e);
			logger.error(e.getLocalizedMessage());
		}
		
		for (Kiosk kiosk : kiosks) {
			
			// Continue processing in the low priority thread
			DashboardProcessor dashboardProcessor = new DashboardProcessor(kiosk);
			dashboardProcessor.setPriority(Thread.MIN_PRIORITY);
			generalTaskExecutor.execute(dashboardProcessor);
		}
		
		kioskExecutor.sendUpdateDashboardCommand();
		
	
		logger.debug("DashBoard Job completed.");
		return kiosks;

	}
	
	private class DashboardProcessor extends Thread
	{
		private  Kiosk kiosk;
		
		public DashboardProcessor(Kiosk kiosk) {
			super();
			this.kiosk = kiosk;
		}

		public void run()
		{
				logger.debug("DashBoard Job is working", kiosk.getDistrict());
				kioskExecutor.checkBoardStatus(kiosk.getDistrict());
		}

	}
}