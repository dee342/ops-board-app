package gov.nyc.dsny.smart.opsboard.misc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProfilingAspect {

	private static final Logger log = LoggerFactory.getLogger(ProfilingAspect.class);

	@Around("execution(public * gov.nyc.dsny.smart.opsboard.controllers.*.*(..))")
	public Object doProfiling1(ProceedingJoinPoint joinpoint) throws Throwable {
             return doBasicProfiling(joinpoint);
        }


	@Around("execution(public * gov.nyc.dsny.smart.opsboard.repos.*.*(..))")
	public Object doProfiling2(ProceedingJoinPoint joinpoint) throws Throwable {
             return doBasicProfiling(joinpoint);
        }

	@Around("execution(public * gov.nyc.dsny.smart.opsboard.services.*.*(..))")
	public Object doProfiling3(ProceedingJoinPoint joinpoint) throws Throwable {
             return doBasicProfiling(joinpoint);
        }

/*	
	@Around("execution(public * gov.nyc.dsny.smart.opsboard.cache.*.*(..))")
	public Object doProfiling4(ProceedingJoinPoint joinpoint) throws Throwable {
		return doBasicProfiling(joinpoint);
	}
*/
	@Around("execution(public * gov.nyc.dsny.smart.opsboard.domain.*.*(..))")
	public Object doProfiling5(ProceedingJoinPoint joinpoint) throws Throwable {
		return doBasicProfiling(joinpoint);
	}

// ALL > TRACE > DEBUG > INFO > WARN > ERROR > FATAL > OFF
        public Object doBasicProfiling(ProceedingJoinPoint joinpoint) throws Throwable {
	
                        String argStr = "";
		if (log.isDebugEnabled()) {
			for (int i = 0; i < joinpoint.getArgs().length; i++) {
				if (i != 0) {
					argStr = argStr + ">, <";
				}
                             argStr = argStr + joinpoint.getArgs()[i];	
                             }
			argStr = ", arguments are: <" + argStr + ">";
                        }
		log.debug("Profiling: Started. Method=({}){}", joinpoint.getSignature(), argStr);
			long start = System.currentTimeMillis();
			Object retVal=joinpoint.proceed();
			long end = System.currentTimeMillis();
			
			String retValStr = ""; 

		if (log.isDebugEnabled()) {
			if (retVal != null)
				retValStr = retVal.toString();
			else
				retValStr = "NULL";
			retValStr = ", returned <" + retValStr + ">";
                        } 

		log.debug("Profiling: Ended in {} ms. Method=({}){}", end - start, joinpoint.getSignature(), retValStr);
			return retVal;
        }
}
