package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wrappers.GenericWrapper;

public class ReadExcellData extends GenericWrapper{
	
      public static String[][] getSheet(String dataSheetName){
		
		XSSFWorkbook wbook;
		String[][] obj = null;
		try {
			FileInputStream fis = new FileInputStream(new File("./data/"+dataSheetName+".xlsx"));
			wbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = wbook.getSheetAt(0);
			int rowCount = sheet.getLastRowNum();
			int colCount = sheet.getRow(0).getLastCellNum();
			System.out.println("Total numbers of records in the "+dataSheetName+" is:"+rowCount);
			obj = new String[rowCount][colCount];
			for (int i = 1; i < rowCount+1; i++) {
				
				XSSFRow row = sheet.getRow(i);
				
				for (int j = 0; j < colCount; j++) {
					
					XSSFCell cell = row.getCell(j);
					String cellValue = cell.getStringCellValue();
					obj[i-1][j]=cellValue;
				}
				
			}
			wbook.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return obj;
	}

}
