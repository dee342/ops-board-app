package obunit.framework;

import java.util.Collection;

import obunit.framework.util.ScenarioReaderUtil;

import org.springframework.test.context.TestContextManager;

public abstract class OBUnitAbstractTest 
{
	protected Scenario scenario;
	
    protected static Collection getScenarios(Class clazz) throws Exception {
        return ScenarioReaderUtil.getScenariosList(clazz);
    }
	
	protected OBUnitAbstractTest(Scenario scenario) throws Exception
	{
		this.scenario = scenario;
		new TestContextManager(getClass()).prepareTestInstance(this);

	}
	
}
