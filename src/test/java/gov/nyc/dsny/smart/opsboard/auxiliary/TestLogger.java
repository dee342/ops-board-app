package gov.nyc.dsny.smart.opsboard.auxiliary;


import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;


public class TestLogger implements Appender<ILoggingEvent> {

	
	private static final List<ILoggingEvent> log = new ArrayList<ILoggingEvent>();
	
	

    public static boolean logContainsStringStartedFrom(String pattern){
    	for(ILoggingEvent entry: getLog() ){
    		System.out.println(entry.getMessage());
    		if(entry.getMessage().startsWith(pattern)){
    			return true;
    			} 
    	}
    	return false;
    }
    
    public static void clear(){
    	 getLog().clear();
    }
    
    public static void show(){
		System.out.println("------------------- Messages in test log -------------------");
    	for(ILoggingEvent entry: getLog() ){
    		System.out.println(entry.getLevel()+","+entry.getMessage());
    	}
		System.out.println("------------------------------------------------------------");
    }

    
	public static List<ILoggingEvent> getLog() {
        return new ArrayList<ILoggingEvent>(log);
    }

	@Override
	public boolean isStarted() {
		System.out.println("isStarted()");
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		System.out.println("start()");
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("stop()");
	
	}

	@Override
	public void addError(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("addError("+arg0+")");
	
	}

	@Override
	public void addError(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		System.out.println("addError("+arg0+", "+arg1+")");
		
	}

	@Override
	public void addInfo(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("addInfo("+arg0+")");
		
	}

	@Override
	public void addInfo(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		System.out.println("addInfo("+arg0+", "+arg1+")");
		
	}

	@Override
	public void addStatus(Status arg0) {
		// TODO Auto-generated method stub
		System.out.println("addStatus("+arg0+")");
		
	}

	@Override
	public void addWarn(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("addWarn("+arg0+")");
	}

	@Override
	public void addWarn(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		System.out.println("addWarn("+arg0+", "+arg1+")");
		
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		System.out.println("getContext()");
		return null;
	}

	@Override
	public void setContext(Context arg0) {
		// TODO Auto-generated method stub
		System.out.println("setContext("+arg0+")");
	}

	@Override
	public void clearAllFilters() {
		// TODO Auto-generated method stub
		System.out.println("clearAllFilters()");
	}

	@Override
	public List getCopyOfAttachedFiltersList() {
		// TODO Auto-generated method stub
		System.out.println("getCopyOfAttachedFiltersList()");
		return null;
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		System.out.println("getName()");
		return "TestAppender";
	}

	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("setName("+arg0+")");
	
	}

	@Override
	public void addFilter(Filter<ILoggingEvent> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FilterReply getFilterChainDecision(ILoggingEvent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doAppend(ILoggingEvent arg0) throws LogbackException {
		// TODO Auto-generated method stub
		System.out.println("doAppend("+arg0+") arg0 is "+ arg0.getClass().getCanonicalName());
		System.out.println(arg0.getLevel()+","+arg0.getMessage());
		log.add(arg0);	
	}
}