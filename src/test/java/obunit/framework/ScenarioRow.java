package obunit.framework;

import java.util.HashMap;
import java.util.Map;


public class ScenarioRow
{
	private int scenarioId;
	private Map<String, Object> columnNameValueMap = new HashMap<String, Object>();
	
	public ScenarioRow(int scenarioId) {
		super();
		this.scenarioId = scenarioId;
	}

	/**
	 * @return the scenarioId
	 */
	public int getScenarioId() {
		return scenarioId;
	}


	/**
	 * @return the columnNameValueMap
	 */
	public Map<String, Object> getColumnNameValueMap() {
		return columnNameValueMap;
	}

	/**
	 * @param columnNameValueMap the columnNameValueMap to set
	 */
	public void setColumnNameValueMap(Map<String, Object> columnNameValueMap) {
		this.columnNameValueMap = columnNameValueMap;
	}

	public void addColumnValue(String name, Object value)
	{
		columnNameValueMap.put(name,  value);
	}
	
	public Object getValue(String name) {
		return columnNameValueMap.get(name);
	}
	
}
