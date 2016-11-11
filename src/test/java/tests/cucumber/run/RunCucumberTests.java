package tests.cucumber.run;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(glue="tests.cucumber", features="src/test/resources", plugin = {"pretty", "html:target/cucumber"})
public class RunCucumberTests {

}
