package gov.nyc.dsny.smart.opsboard.misc;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.converters.Convert;
import gov.nyc.dsny.smart.opsboard.domain.AbstractBaseDomainEntity;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Equipmentable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.activity.InvalidActivityException;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;

@Aspect
@Component
@Validated
public class ConverterAspect {

	private static final Logger log = LoggerFactory.getLogger(ConverterAspect.class);
	
	@Autowired
	@Qualifier("mvcConversionService")
	private ConversionService conversionService;	

	@SuppressWarnings("unchecked")
	@Before("execution(public * gov.nyc.dsny.smart.opsboard.controllers.*.*(.., @gov.nyc.dsny.smart.opsboard.converters.Convert (*), ..))")
	public void doConversion(JoinPoint jp) throws Throwable {
		doConversion(jp, true);
    }
	
	public void doConversion(JoinPoint jp, boolean inferPathVariables) throws Throwable{
		final Signature signature = jp.getSignature();
	    if(signature instanceof MethodSignature){
	        final MethodSignature ms = (MethodSignature) signature;
	        
	        final Method method = ms.getMethod();
	        final Annotation[][] parameterAnnotations =
	            method.getParameterAnnotations();
	        
	        for(int i = 0; i < parameterAnnotations.length; i++){
	            final Annotation[] annotations = parameterAnnotations[i];
	            final Convert paramAnnotation =
	                getAnnotationByType(annotations, Convert.class);
	            if(paramAnnotation != null){
	               Object o = jp.getArgs()[i];
	               if(!ViewModel.class.isInstance(o) && !Collection.class.isInstance(o) && !Map.class.isInstance(o)){
	            	   log.error("@Convert is on an object that does not extend ViewModel class {}.", o);
	            	   throw new InvalidActivityException("@Convert is on an object that does not extend ViewModel class.");
	               }
	               
	               if(ViewModel.class.isInstance(o)){
	            	   convert(jp, o, inferPathVariables);
	               }else if(Collection.class.isInstance(o)){
	            	   Collection<?> objects = (Collection<?>) o;
	            	   convert(jp, objects, inferPathVariables);
	               }else{
	            	   Map<?, ?> map = (Map<?, ?>) o;
	            	   convert(jp, map.keySet(), inferPathVariables);
	               }
	            }
	        }
	    }
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractBaseDomainEntity> void addInferredProperties(JoinPoint jp, Object o){
		if(ViewModel.class.isInstance(o)){
			((ViewModel<T>) o).setBoardDate(findInferredPathVariable(jp, "boardDate", "date"));
			((ViewModel<T>) o).setBoardLocation(findInferredPathVariable(jp, "boardLocation", "location"));
		}
		
		if(Equipmentable.class.isInstance(o)){
			((Equipmentable) o).setEquipmentId(findInferredPathVariable(jp, "equipmentId"));
		}		
	}
	
	private String findInferredPathVariable(JoinPoint jp, String... propertyNames){
		final Signature signature = jp.getSignature();
	    if(signature instanceof MethodSignature){
	        final MethodSignature ms = (MethodSignature) signature;
	        final Method method = ms.getMethod();        
	        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		        
	        final List<String> parameters = Arrays.stream(ms.getParameterNames()).map(s -> StringUtils.lowerCase(s)).collect(Collectors.toList());
	        int index = -1;
	        
	        for(String propertyName : propertyNames){
	        	index = parameters.indexOf(StringUtils.lowerCase(propertyName));
	        	if(index >= 0)
	        		break;
	        }
	        
	        if(index >= 0){
	        	if(index < jp.getArgs().length){
	        		final Annotation[] annotations = parameterAnnotations[index];
	 	            final PathVariable paramAnnotation = getAnnotationByType(annotations, PathVariable.class);
	 	            if(paramAnnotation != null)
	 	            	return (String) jp.getArgs()[index];
	        	}
	        }
	    }
	    return null;
	}
	
	private void convert(JoinPoint jp, Collection<?> objects, boolean inferPathVariables) throws InvalidActivityException, OpsBoardError{
		for(Object obj : objects){
 		   convert(jp, obj, inferPathVariables);
 	   }
	}
	
	@SuppressWarnings("unchecked")
	private  <T extends AbstractBaseDomainEntity> void convert(JoinPoint jp, Object o, boolean inferPathVariables) throws InvalidActivityException, OpsBoardError{
		if(!ViewModel.class.isInstance(o))
			return;
		
		if(inferPathVariables)
			addInferredProperties(jp, o);
		
		ViewModel<T> vm = (ViewModel<T>) o;	               
        
        if(!conversionService.canConvert(o.getClass(), vm.getGenericClass())){
     	   log.error("Cannot convert from {} to {}. Make sure converter exists.", o.getClass(), vm.getGenericClass());
     	   throw new InvalidActivityException("Cannot convert view model to entity.");
     	   
        }
        
       convert(conversionService, o, vm);
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T getAnnotationByType(final Annotation[] annotations,
	    final Class<T> clazz){

	    T result = null;
	    for(final Annotation annotation : annotations){
	        if(clazz.isAssignableFrom(annotation.getClass())){
	            result = (T) annotation;
	            break;
	        }
	    }
	    return result;
	}
	
	public static <T extends AbstractBaseDomainEntity> T convert(ConversionService conversionService, Object o, ViewModel<T> vm) throws OpsBoardError{
		T entity = (T) conversionService.convert(o, vm.getGenericClass());
		if(vm.hasError() || entity == null)
			throw new OpsBoardError(vm.hasError() ? vm.getErrorMessage() : ErrorMessage.DATA_ERROR);
		
		vm.setEntity(entity);
		
		return entity;
	}

}
