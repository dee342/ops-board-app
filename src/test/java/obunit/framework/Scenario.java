package obunit.framework;

import java.util.ArrayList;
import java.util.List;

public class Scenario 
{
	private int scenarionId;
	private List<ScenarioRow> scenarioRows = new ArrayList<ScenarioRow>();
	private List<String> colummnNames  = new ArrayList<String>();
	
	public Scenario(int scenarionId, List<ColumnName> colummnNames) {
		super();
		this.scenarionId = scenarionId;
		if (colummnNames != null)
		{
			for (ColumnName columnName : colummnNames)
			{
				this.colummnNames.add(columnName.getCellName());
			}
		}
	}
	/**
	 * @return the scenarioRows
	 */
	public List<ScenarioRow> getScenarioRows() {
		return scenarioRows;
	}
	/**
	 * @param scenarioRows the scenarioRows to set
	 */
	public void setScenarioRows(List<ScenarioRow> scenarioRows) {
		this.scenarioRows = scenarioRows;
	}
	
	public void addScenarioRow(ScenarioRow scenarioRow) {
		scenarioRows.add(scenarioRow);
	}
	/**
	 * @return the colummnNames
	 */
	public List<String> getColummnNames() {
		return colummnNames;
	}
	
	/**
	 * @return the scenarionId
	 */
	public int getScenarionId() {
		return scenarionId;
	}

	public String getStrng(int rowNumber, String columnName)
	{
		return (String) getObject (rowNumber, columnName);
				
	}
	
	public Object getInteger(int rowNumber, String columnName)
	{
		return (Integer) getObject (rowNumber, columnName);
	}

	
	public Object getObject(int rowNumber, String columnName)
	{
		ScenarioRow row = scenarioRows.get(rowNumber -1);
		return row.getValue(columnName);
	}
	
}

