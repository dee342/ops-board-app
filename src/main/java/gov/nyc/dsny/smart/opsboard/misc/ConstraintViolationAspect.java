package gov.nyc.dsny.smart.opsboard.misc;

import javax.validation.ConstraintViolationException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConstraintViolationAspect {

	@AfterThrowing(pointcut = "execution(public * gov.nyc.dsny.smart.opsboard.services.sorexecutors..*(..))", throwing = "e")
	public void handleConstraint(JoinPoint jp, Throwable e) throws Throwable {
		while(e.getCause() != null){
			Throwable t = e.getCause();
			
			if(ConstraintViolationException.class.isInstance(t)){
				throw t;
			}
			
			e = t;
		}
	
		throw e;
    }
		
}
