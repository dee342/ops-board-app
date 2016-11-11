package gov.nyc.dsny.smart.opsboard.controllers;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestDataUtil {
	
	  protected static final String DATE_FORMAT1="yyyy-MM-dd'T'HH:mm:ss'.000-0400'";
	  public static final String DATE_FORMAT2="yyyyMMdd";
	  public static Principal getTestPrincipal(){
		  Principal principal = new Principal() {
			  @Override
			  public String getName() {
				  return "TEST_PRINCIPAL";
			  }
		  };    
		  return principal;
	  }
	  
	  public static String constructUri(List<String> params){
		  String constructedUri = "";
		  for(String param: params){
			  constructedUri =constructedUri+ "/"+param; 
		  }
		  return constructedUri;
		  
	  }
	  
	  public static String convertObjectToJsonString(Object object) throws IOException {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	        return mapper.writeValueAsString(object);
	    }
	  
	  public static String dateToString(Date date, String dateFormat) {
			if (date == null)
				return null;
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			return sdf.format(date);
		}
	  public static Date stringToDate(String date, String dateFormat) throws ParseException {
			if (date == null)
				return null;
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			return sdf.parse(date);
		}
	  
}
