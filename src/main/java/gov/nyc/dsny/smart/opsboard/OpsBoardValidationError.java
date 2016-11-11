package gov.nyc.dsny.smart.opsboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;

@ControllerAdvice
public class OpsBoardValidationError{
 
    private MessageSource messageSource;
    private ApplicationContext applicationContext;
 
    @Autowired
    public OpsBoardValidationError(MessageSource messageSource) {
    	 	this.messageSource = messageSource;
    }
    
    @Autowired
    private void setApplicationContext(ApplicationContext applicationContext){
    	this.applicationContext = applicationContext;
    }
    	
 
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public OpsBoardError processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
 
        return processFieldErrors(fieldErrors);
    }
    
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public OpsBoardError processConstraintViolationError(ConstraintViolationException ex) { 
        return processConstraintViolations(ex.getConstraintViolations());
    }
    
	private OpsBoardError processFieldErrors(List<FieldError> fieldErrors) {

		List<String> extErrMessages = new ArrayList<String>();
		for (FieldError fieldError : fieldErrors) {			
			String localizedErrorMessage = resolveLocalizedErrorMessage(fieldError);
			extErrMessages.add(localizedErrorMessage);
		}

		OpsBoardError obe = new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR, extErrMessages);
		return obe;
	}

	private OpsBoardError processConstraintViolations(Set<ConstraintViolation<?>> cvs) {

		List<String> extErrMessages = new ArrayList<String>();
		for (ConstraintViolation<?> cv : cvs) {
			extErrMessages.add(resolveLocalizedErrorMessage(cv));			
		}

		OpsBoardError obe = new OpsBoardError(ErrorMessage.BEAN_VALIDATION_ERROR, extErrMessages);
		return obe;
	}
 
    @ExceptionHandler({org.springframework.web.HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public void resolveException(Exception ex) {
       ex.printStackTrace();
    }
    
    @ExceptionHandler({OpsBoardValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public OpsBoardError resolveValidationException(OpsBoardValidationException ex) {
       return ex.getOpsBoardError();
    }
    
    @ExceptionHandler({OpsBoardError.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public OpsBoardError resolveOpsBoardError(OpsBoardError ex) {
       return ex;
    }    
    
    @ExceptionHandler({DataLoadException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public DataLoadException handleDataLoadException(HttpServletRequest request, DataLoadException ex) {
    	ex.setEndpoint(String.valueOf(request.getRequestURL()));
    	return ex;
    }    

	/**
	 * @return the messageSource
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource
	 *            the messageSource to set
	 */
	 public void setMessageSource(MessageSource messageSource) {
		 this.messageSource = messageSource;
	 }

	private String resolveLocalizedErrorMessage(FieldError fieldError) {
		Locale currentLocale = LocaleContextHolder.getLocale();
		String localizedErrorMessage = messageSource.getMessage(fieldError, currentLocale);
		

		// If the message was not found, return the most accurate field error code instead.
		// You can remove this check if you prefer to get the default error message.
		/*
		 * if (localizedErrorMessage.equals(fieldError.getDefaultMessage())) { String[] fieldErrorCodes =
		 * fieldError.getCodes(); localizedErrorMessage = fieldErrorCodes[0]; }
		 */

		return localizedErrorMessage;
	}
	
	private String resolveLocalizedErrorMessage(ConstraintViolation<?> cv) {
		Locale currentLocale = LocaleContextHolder.getLocale();

		String isExistsCode = new StringBuilder()
				.append(cv.getLeafBean().getClass().getSimpleName())
				.append(".")
				.append(cv.getPropertyPath().toString().contains(".") ? 
						cv.getPropertyPath().toString().substring(cv.getPropertyPath().toString().lastIndexOf(".") + 1) : 
						cv.getPropertyPath().toString())
				.append(".")
				.append(cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()).toString();
		
		String localizedErrorMessage = messageSource.getMessage(isExistsCode, cv.getExecutableParameters(), cv.getMessage(), currentLocale);
		
		return localizedErrorMessage;
	}
	
}