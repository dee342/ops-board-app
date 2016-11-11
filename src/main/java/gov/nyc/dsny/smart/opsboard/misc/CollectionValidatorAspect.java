//package gov.nyc.dsny.smart.opsboard.misc;
//
//import gov.nyc.dsny.smart.opsboard.converters.Convert;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Method;
//import java.util.Collection;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.MethodParameter;
//import org.springframework.stereotype.Component;
//import org.springframework.validation.BeanPropertyBindingResult;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.Errors;
//import org.springframework.validation.Validator;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//
//@Aspect
//@Component
//public class CollectionValidatorAspect {
//
//	private static final Logger log = LoggerFactory.getLogger(CollectionValidatorAspect.class);
//		
//	@Autowired
//	private Validator validator;
//
//	@SuppressWarnings("unchecked")
//	@Before("execution(public * gov.nyc.dsny.smart.opsboard.controllers.*.*(.., @javax.validation.Valid (java.util.*), ..))")
//	public void doProfiling1(JoinPoint jp) throws MethodArgumentNotValidException {
//		final Signature signature = jp.getSignature();
//	    if(signature instanceof MethodSignature){
//	        final MethodSignature ms = (MethodSignature) signature;
//	        
//	        final Method method = ms.getMethod();
//	        final Annotation[][] parameterAnnotations =
//	            method.getParameterAnnotations();
//	        final String[] fieldNames = ms.getParameterNames();
//	        
//	        for(int i = 0; i < parameterAnnotations.length; i++){
//	            final Annotation[] annotations = parameterAnnotations[i];
//	            final Convert paramAnnotation =
//	                getAnnotationByType(annotations, Convert.class);
//	            if(paramAnnotation != null){
//	               Object o = jp.getArgs()[i];
//	               
//	               if(!Collection.class.isInstance(o))
//	            	   return;
//	               
//	               Collection<Object> objects = (Collection<Object>) o;
//	               	               
//	               
//	               int j = 0;
//	               for(Object obj : objects){
//	            	   BindingResult br = new BeanPropertyBindingResult(obj, fieldNames[i]);
//	            	   validator.validate(obj, br);
//	            	   if(br.hasErrors()){
//	            		   MethodArgumentNotValidException exception = new MethodArgumentNotValidException(new MethodParameter(method, i), br);
//	            		   log.error("Object at index {} failed validation.", j, exception);
//	            		   throw exception;
//	            	   }
//	            	   j++;
//	               }	             
//	            }
//
//	        }
//	    }
//    }
//	
//	@SuppressWarnings("unchecked")
//	private <T extends Annotation> T getAnnotationByType(final Annotation[] annotations,
//	    final Class<T> clazz){
//
//	    T result = null;
//	    for(final Annotation annotation : annotations){
//	        if(clazz.isAssignableFrom(annotation.getClass())){
//	            result = (T) annotation;
//	            break;
//	        }
//	    }
//	    return result;
//	}
//
//}
