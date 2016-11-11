package obunit.framework;

public class ColumnName 
{
	private int cellNumber;
	private String cellName;
	
	public ColumnName(int cellNumber, String cellName) {
		super();
		this.cellNumber = cellNumber;
		this.cellName = cellName;
	}


	/**
	 * @return the cellNumber
	 */
	public int getCellNumber() {
		return cellNumber;
	}


	/**
	 * @return the cellName
	 */
	public String getCellName() {
		return cellName;
	}

}
