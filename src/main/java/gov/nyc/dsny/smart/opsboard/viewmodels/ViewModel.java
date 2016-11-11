package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.AbstractBaseDomainEntity;

import java.io.Serializable;

import org.junit.Assert;

public abstract class ViewModel<T extends AbstractBaseDomainEntity> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7480558924026183492L;
	protected final Class<T> genericClass;
	protected T entity;
	private String boardDate;
	private String boardLocation;
	private ErrorMessage errorMessage;
	
	public ViewModel(Class<T> genericClass){
		Assert.assertNotNull(genericClass);
		this.genericClass = genericClass;
	};
	
	public T getEntity(){return entity;};
	public void setEntity(T entity){this.entity = entity;};
	public Class<T> getGenericClass(){return genericClass;}

	public String getBoardDate() {
		return boardDate;
	}

	public void setBoardDate(String boardDate) {
		this.boardDate = boardDate;
	}

	public String getBoardLocation() {
		return boardLocation;
	}

	public void setBoardLocation(String boardLocation) {
		this.boardLocation = boardLocation;
	}

	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(ErrorMessage errorMessage) {
		this.errorMessage = errorMessage;
	};
	
	
	public void handleErrorMessage(ErrorMessage message){
		this.errorMessage = message;	
	}
	
	public void handleErrorMessage(int code){
		handleErrorMessage(ErrorMessage.findByCode(code));
	}
	
	public boolean hasError(){
		return errorMessage!=null;
	}
}
