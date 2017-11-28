package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.testng.annotations.Test;

public class SendMail extends AutomaticSendMail {
	
	@Test
	public static void sendSuccessMail(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./mail.properties")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String time = dateFormat.format(date);
		System.out.println("Sending....");
		sendSuccessReportByGmail(prop.getProperty("Automation.Mail.To"), prop.getProperty("Automation.Mail.Subject"),time);
		System.out.println("Sent.");
	}
	
	@Test
	public static void sendFailureMail(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./mail.properties")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String time = dateFormat.format(date);
		System.out.println("Sending....");
		sendFailureReportByGmail(prop.getProperty("Automation.Mail.To"), prop.getProperty("Automation.Mail.Subject"),time);
		System.out.println("Sent.");
	}
	
	@Test
	public static void sendAttachmentMail(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./mail.properties")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String time = dateFormat.format(date);
		System.out.println("Sending....");
		sendAttachmentReport(prop.getProperty("Automation.Mail.To"), prop.getProperty("Automation.Mail.Subject"), prop.getProperty("Automation.Mail.Body.Text"), prop.getProperty("Automation.Mail.FileName"), time);
		System.out.println("Sent.");
	}

}
