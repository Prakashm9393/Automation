package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class AutomaticSendMail {
	
	static String htmlText;
	
	public static void sendSuccessReportByGmail(String to,String subject,String time){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./htmlcontent.properties")));
		    htmlText = prop.getProperty("Automation.Mail.Body.HtmlText");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String host = "smtp.gmail.com";
		String from = "seleniumautomationmail.ameex@gmail.com";
		String pass = "ameexusa";
		prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.user", from);
        prop.put("mail.smtp.password", pass);
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
		
        Session session = Session.getDefaultInstance(prop);
        MimeMessage message = new MimeMessage(session);        
		try {			
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));				
			message.setSubject(subject+" : "+time);
			message.setContent(htmlText, "text/html");
			
		    Transport transport = session.getTransport("smtp");
			transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();		

		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendFailureReportByGmail(String to,String subject,String time){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("./htmlcontent.properties")));
		    htmlText = prop.getProperty("Automation.Mail.Body.HtmlFailureText");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String host = "smtp.gmail.com";
		String from = "seleniumautomationmail.ameex@gmail.com";
		String pass = "ameexusa";
		prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.user", from);
        prop.put("mail.smtp.password", pass);
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
		
        Session session = Session.getDefaultInstance(prop);
        MimeMessage message = new MimeMessage(session);        
		try {			
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));						
			message.setSubject(subject+" : "+time);
			message.setContent(htmlText, "text/html");
			
		    Transport transport = session.getTransport("smtp");
			transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();		

		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAttachmentReport(String to,String cc,String subject,String body,String filename,String time){
		Properties prop = new Properties();
		String host = "smtp.gmail.com";
		String from = "seleniumautomationmail.ameex@gmail.com";
		String pass = "ameexusa";
		prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.user", from);
        prop.put("mail.smtp.password", pass);
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
		
        Session session = Session.getDefaultInstance(prop);
        MimeMessage message = new MimeMessage(session);
        try {
        	//Set from address
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
           //Set subject
            message.setSubject(subject+" : "+time);
            message.setText("On "+time+" "+body);
          
            BodyPart objMessageBodyPart = new MimeBodyPart();
            
            objMessageBodyPart.setText("On "+time+" "+body);
            
            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(objMessageBodyPart);

            objMessageBodyPart = new MimeBodyPart();

            //Set path to the report file
            String filePath = "./data/"+filename+".xlsx";
            
            //Create data source to attach the file in mail
            DataSource source = new FileDataSource(filePath);
            
            objMessageBodyPart.setDataHandler(new DataHandler(source));

            objMessageBodyPart.setFileName(filename+".xlsx");

            multipart.addBodyPart(objMessageBodyPart);

            message.setContent(multipart);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
	}

}
