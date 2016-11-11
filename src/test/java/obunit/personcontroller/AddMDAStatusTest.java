package obunit.personcontroller;

import gov.nyc.dsny.smart.opsboard.controllers.PersonController;

import java.util.Collection;

import obunit.framework.OBUnitAbstractTest;
import obunit.framework.Scenario;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(Parameterized.class)
@ContextConfiguration(classes=AddMDAStatusConfig.class, loader=AnnotationConfigContextLoader.class)
public class AddMDAStatusTest extends OBUnitAbstractTest
{
	// ***************************** Loading scenarios load form excel - start ********************************************************
	@Parameters
    public static Collection spreadsheetData() throws Exception	{return getScenarios(AddMDAStatusTest.class);}
	public AddMDAStatusTest(Scenario scenario) throws Exception{super(scenario);}
	// ***************************** Loading scenarios load form excel - end ********************************************************
	
	@Autowired
	private PersonController personController;
	
    @Test
    public void executeScennario() throws Exception 
    {
    	Assert.assertNotNull(personController);
    	Assert.assertNotNull(scenario);

    	
//    	personController.addMdaStatus(
//    			scenario.getStrng(1, "boardLocation"), 
//    			scenario.getStrng(1, "boardDate"),
//    			null, null, null, null, null);
    }	
	
}
