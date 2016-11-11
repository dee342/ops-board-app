package obunit.framework.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import obunit.framework.ColumnName;
import obunit.framework.Scenario;
import obunit.framework.ScenarioRow;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public abstract class ScenarioReaderUtil 
{
	private static final String RELATIVE_FILE_PATH = "/testdata/";
	
	public static List<Scenario> getScenariosList(Class clazz) throws Exception 
	{
			
		File file = getFile (clazz);
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		List<ColumnName> columnNames = getAllColumnNames(sheet);
		List<ScenarioRow> scenarioRows = getScenarioRows(sheet, columnNames);
		
		List<Scenario> scenarioList = new ArrayList<Scenario>();
		Scenario currentScenario = new Scenario(0, null);
		for (ScenarioRow scenarioRow : scenarioRows)
		{
			int scenarioId = scenarioRow.getScenarioId();
			if (currentScenario.getScenarionId() != scenarioRow.getScenarioId())
			{
				currentScenario = new Scenario(scenarioId, columnNames);
				scenarioList.add(currentScenario);
			}
			
			currentScenario.addScenarioRow(scenarioRow);
			
		}
			
		return scenarioList;
	}

	private static File getFile (Class clazz) throws Exception
	{
		return FileUtil.getFile(RELATIVE_FILE_PATH, clazz);
	}

	/**
	 * This method creates the list of boundary attribute=value pairs (HashMaps) 
	 * multi-row CSV file (first row should contain attribute names)
	 */
	public static List<ScenarioRow> getScenarioRows(XSSFSheet sheet, List<ColumnName> columnNames) throws Exception 
	{
		List<ScenarioRow> scenarioRows = new ArrayList<ScenarioRow>();
		
		int rowNumber = 1;
		while (true) 
		{
			ScenarioRow scenarioInputRow = extractScenarioRow(rowNumber++, columnNames, sheet);
			if (scenarioInputRow == null)
			{
				break;
			}
			
			scenarioRows.add(scenarioInputRow);
		}

		return scenarioRows;
	}
	
	private static List<ColumnName> getAllColumnNames(XSSFSheet sheet) throws Exception
	{
		List<ColumnName> columnNames = new ArrayList<ColumnName>();
		
		XSSFRow fieldRow = sheet.getRow(0);
		int columnCount = fieldRow.getPhysicalNumberOfCells();
		for(int i=0; i < columnCount; i++)
		{
			XSSFCell cell = fieldRow.getCell(i);
			String columnName = cell.getStringCellValue();
			columnNames.add(new ColumnName(i, columnName));
		}
		return columnNames;
	}

	private static ScenarioRow extractScenarioRow(int rowNumber, List<ColumnName> columnNames, XSSFSheet sheet) throws Exception
	{
		if (rowNumber == 0)
			throw new Exception ("The row number should be larger than zero");
		
		XSSFRow row = sheet.getRow(rowNumber);
		Integer scenarioId = null;
		try
		{
			XSSFCell cell = row.getCell(0);
			scenarioId =	Double.valueOf(cell.getNumericCellValue()).intValue();
		}
		catch (Exception ex)
		{
			scenarioId = null;
		}
		
		if (scenarioId == null)
			return null;
		
		ScenarioRow scenarioRow = new ScenarioRow(scenarioId);
		for (ColumnName columnName : columnNames)
		{
			Object value = null;
			XSSFCell cell = sheet.getRow(rowNumber).getCell(columnName.getCellNumber());
			if (cell == null)
				continue;
			switch (cell.getCellType()) {

				case HSSFCell.CELL_TYPE_STRING:
					value = cell.getRichStringCellValue();
				    break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					value = cell.getNumericCellValue();
				    break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				default:
					throw new Exception ("Unknown cell data type for column " + columnName.getCellName() + " in row " + rowNumber);
			}
	
			scenarioRow.addColumnValue(columnName.getCellName(),  value);
		}
		
		return scenarioRow;
	}

}
