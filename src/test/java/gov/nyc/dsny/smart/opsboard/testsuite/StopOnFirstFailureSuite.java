package gov.nyc.dsny.smart.opsboard.testsuite;


import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;


public class StopOnFirstFailureSuite extends Suite {


public StopOnFirstFailureSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
}

public StopOnFirstFailureSuite(Class<?> klass) throws InitializationError {
        super(klass, klass.getAnnotation(SuiteClasses.class).value());
}


@Override
public void run(RunNotifier runNotifier) {
        super.run(runNotifier);
}
}