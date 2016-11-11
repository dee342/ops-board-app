package gov.nyc.dsny.smart.opsboard.tools;

import gov.nyc.dsny.smart.opsboard.CategorySerializer;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class GenerateReferenceDataForSubcatgories {
	
	public static void main(String args[]) throws JsonProcessingException{

		FileInputStream file = readFile("data/reference/SubCategory_worksheet.xls");
		
		HSSFWorkbook workbook = getWorkbook(file);
		ArrayList<Category> categories = new ArrayList<Category>();
		for(int i=0;i<8;i++){
		//Get first sheet from the workbook
		HSSFSheet sheet = workbook.getSheetAt(i);
		Category category = new Category();
		ArrayList<Subcategory> subcategories = new ArrayList<Subcategory>();
		
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		rowIterator.next();// make sure first row is always skipped
		int columnNumber = 0;
		int[] requiredColumnNums = new int[6];
		int rowNumber = 0;
		ArrayList<Integer> list = null;
		while(rowIterator.hasNext()){
			rowNumber = rowNumber +1;
			Subcategory subcategory = new Subcategory();
			Row row = rowIterator.next();
			//Get iterator to all cells of current row
			Iterator<Cell> cellIterator = row.cellIterator();
			
			String[] requiredColumns = new String[6];
			requiredColumns[0] = "Subcategory ID";
			requiredColumns[1] = "Sections";
			requiredColumns[2] = "Long Description";
			requiredColumns[3] = "Seq.Number";
			requiredColumns[4] = "Category";
			requiredColumns[5] = "People Task Pos.";
			
			while(cellIterator.hasNext())
			{
				HSSFCell obj = (HSSFCell)cellIterator.next();
				columnNumber = columnNumber + 1;
				//System.out.println("obj"+obj.getCellType());
				//System.out.println(obj);
				if(rowNumber == 1)
				{
				if(obj.getStringCellValue().equals("Category")){
					requiredColumnNums[0] = columnNumber;
				}else if(obj.getStringCellValue().equals("Subcategory ID")){
					requiredColumnNums[1] = columnNumber;
				}else if(obj.getStringCellValue().equals("Long Description")){
					requiredColumnNums[2] = columnNumber;
				}else if(obj.getStringCellValue().equals("Seq.Number")){
					requiredColumnNums[3] = columnNumber;
				}else if(obj.getStringCellValue().equals("Sections")){
					requiredColumnNums[4] = columnNumber;
				}else if(obj.getStringCellValue().equals("People Task Pos.")){
					requiredColumnNums[5] = columnNumber;
					list = generateListOfRequiredColumnNums(requiredColumnNums);
				}
				
				continue;
				}
				//System.out.println(list);
				//System.out.println(columnNumber);
				//System.out.println(list.contains(columnNumber));
				//System.out.println(list.indexOf(columnNumber));
				//System.out.println(rowNumber);
				//System.out.println(columnNumber);
				if(list.contains(columnNumber)){ // This column is the column I am interested in .. but I need to know the index
					if(list.indexOf(columnNumber) == 0){
						category = setCategoryId(category, obj);
						subcategory.setCategory(category);
					}else if(list.indexOf(columnNumber) == 1){
						//if(obj.getStringCellValue().equals("NEW"))
							//continue;
						subcategory.setId(Long.valueOf((int)obj.getNumericCellValue()));
					}else if(list.indexOf(columnNumber) == 2){
						subcategory.setName(obj.toString());
					}else if(list.indexOf(columnNumber) == 3){
						subcategory.setSequence(returnCellValue(obj));
					}else if(list.indexOf(columnNumber) == 4){
						subcategory.setContainsSections(obj.equals("Y"));
					}else if(list.indexOf(columnNumber) == 5){
						subcategory.setPeoplePerTask((int) obj.getNumericCellValue());
					}
					
				}
				continue;
			}
			//System.out.println(subcategory);
			if(rowNumber != 1)
				subcategories.add(subcategory);
			columnNumber = 0;
		}
		categories.add(category);
		category.setSubcategories(subcategories);
		}
		

		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(Category.class, new CategorySerializer());
        mapper.registerModule(module);

        //String serialized = mapper.writeValueAsString(categories);
		try {
			mapper.writeValue(new File("data/category.json"), categories);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
}

	private static int returnCellValue(HSSFCell obj) {
		//System.out.println(obj);
		return new Double(obj.toString()).intValue();
	}

	private static Category setCategoryId(Category category, HSSFCell obj) {
		if(obj.getStringCellValue().equals("Supervision")){
			category.setId(1L);
			category.setName("Supervision");
			category.setSequence(1);
		}else if(obj.getStringCellValue().equals("Cleaning")){
			category.setId(2L);
			category.setName("Cleaning");
			category.setSequence(2);
		}else if(obj.getStringCellValue().equals("Collection")){
			category.setId(3L);
			category.setName("Collection");
			category.setSequence(3);
		}else if(obj.getStringCellValue().equals("Recycling")){
			category.setId(4L);
			category.setName("Recycling");
			category.setSequence(4);
		}else if(obj.getStringCellValue().equals("Snow")){
			category.setId(5L);
			category.setName("Snow");
			category.setSequence(5);
		}else if(obj.getStringCellValue().equals("ERD")){
			category.setId(6L);
			category.setName("ERD");
			category.setSequence(6);
		}else if(obj.getStringCellValue().equals("Support")){
			category.setId(7L);
			category.setName("Support");
			category.setSequence(7);
		}else if(obj.getStringCellValue().equals("Surplus")){
			category.setId(8L);
			category.setName("Surplus");
			category.setSequence(8);
		}		
		return category;
	}

	private static ArrayList<Integer> generateListOfRequiredColumnNums(
			int[] requiredColumnNums) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < requiredColumnNums.length; i++) {
			list.add(requiredColumnNums[i]);
		}
		return list;
	}

	private static HSSFWorkbook getWorkbook(FileInputStream file) 
	{
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workbook;
	}

	private static FileInputStream readFile(String fileName) 
	{
		FileInputStream file = null;
		try {
			file = new FileInputStream(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return file;
	}

}
