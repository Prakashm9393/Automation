package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadPropertiesFiles {
	
	public static String Aut = null;
	public static String FileName = null;
	public static String SheetName = null;
	public static String MailIdLocate = null;
	public static String NextBut = null;
	public static String MsgeBox = null;
	
	public static void loadConfingFile(){
		Properties prop = new Properties();
		Properties prop2 = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);			
			Aut = prop.getProperty("Url");
			FileName = prop.getProperty("ExcelFilename");
			SheetName = prop.getProperty("ExcelSheetName");
			
			input = new FileInputStream("objects.properties");
			prop2.load(input);
			MailIdLocate = prop2.getProperty("mailid");
			NextBut = prop2.getProperty("next");
			MsgeBox = prop2.getProperty("msge");
			
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
}
