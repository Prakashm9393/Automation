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
	public static String MailIdLocate_TC1 = null;
	public static String NextBut_TC1 = null;
	public static String MsgeBox_TC1 = null;
	public static String MailIdLocate_TC2 = null;
	public static String NextBut_TC2 = null;
	public static String MsgeBox2_TC2_1 = null;
	public static String MsgeBox2_TC2_2 = null;
	public static String MsgeBox = null;
	public static String MsgeBox1 = null;
	public static String MsgeBox_TC2_1 = null;
	public static String MsgeBox_TC2_1_1 = null;
	public static String MsgeBox_TC2_2 = null;
	public static String MsgeBox_TC3 = null;
	public static String MsgeBox_TC4 = null;
	public static String MsgeBox_TC5 = null;
	
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
			MailIdLocate_TC1 = prop2.getProperty("TC001.mailid");
			NextBut_TC1 = prop2.getProperty("TC001.next");
			MsgeBox_TC1 = prop2.getProperty("TC001.msge");
			MailIdLocate_TC2 = prop2.getProperty("TC002.mailid");
			NextBut_TC2 = prop2.getProperty("TC002.next");
			MsgeBox2_TC2_1 = prop2.getProperty("TC002.msge2_1");
			MsgeBox2_TC2_2 = prop2.getProperty("TC002.msge2_2");
			MsgeBox = prop2.getProperty("TC002.msgBox");
			MsgeBox1 = prop2.getProperty("TC002.msgBox1");
			MsgeBox_TC2_1 = prop2.getProperty("TC002.msge1");
			MsgeBox_TC2_1_1 = prop2.getProperty("TC002.msge1_1");
			MsgeBox_TC2_2 = prop2.getProperty("TC002.msge2");
			MsgeBox_TC3 = prop2.getProperty("TC002.msge3");
			MsgeBox_TC4 = prop2.getProperty("TC002.msge4");
			MsgeBox_TC5 = prop2.getProperty("TC002.msge5");
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
}
