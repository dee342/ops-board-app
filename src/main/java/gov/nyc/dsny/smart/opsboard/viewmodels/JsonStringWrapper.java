package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Wrapper class that wraps a Json String. The JsonConverter(Jackson)
 * would use the literal string value and not try to jsonify it again
 * by escaping doublequotes
 */
public class JsonStringWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;

	public JsonStringWrapper(String value) {
		this.value = value;
	}
	
	@JsonValue
	@JsonRawValue
	public String value(){
		return value;
	}
	
	
}
