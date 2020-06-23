package com.pengtoolbox.cfw.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.logging.CFWLog;

public class CFWMail {
	
	private static Logger logger = CFWLog.getLogger(CFWMail.class.getName());
	
	/***************************************************************
	 * Creates a session for sending mails.
	 * @return session or null if mailing is disabled
	 ***************************************************************/
	public static Session initializeSession() {

		if(CFW.Properties.MAIL_ENABLED) {
			final String authMethod = CFW.Properties.MAIL_SMTP_AUTHENTICATION.trim().toUpperCase();
			final String loginEmail = CFW.Properties.MAIL_SMTP_LOGIN_MAIL;
			final String password = CFW.Properties.MAIL_SMTP_LOGIN_PASSWORD; 
			
			//------------------------------
			//General Properties
			Properties props = new Properties();
			props.put("mail.smtp.host", CFW.Properties.MAIL_SMTP_HOST); 
			
			//------------------------------
			// TLS Authentication
			if(authMethod.equals("TLS")) {
				props.put("mail.smtp.port", CFW.Properties.MAIL_SMTP_PORT); //TLS Port
				props.put("mail.smtp.auth", "true"); //enable authentication
				props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
				
				Authenticator auth = new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(loginEmail, password);
					}
				};
				
				return Session.getInstance(props, auth);
			}
			
			//------------------------------
			// SSL Authentication
			if(authMethod.equals("SSL")){
				System.out.println("SSLEmail Start");
				
				props.put("mail.smtp.socketFactory.port", CFW.Properties.MAIL_SMTP_PORT);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
				props.put("mail.smtp.auth", "true"); 
				props.put("mail.smtp.port", CFW.Properties.MAIL_SMTP_PORT); 
				
				Authenticator auth = new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(loginEmail, password);
					}
				};
				
				return Session.getInstance(props, auth);
			}
			
			//------------------------------
			// No Authentication
			return Session.getInstance(props, null);
			}
		return null;
	}
	
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReply(String toEmail, String subject, String body){
		Session session = initializeSession();
		sendFromNoReply(session, toEmail, subject, body);
	}
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReply(Session session, String toEmail, String subject, String body){
		
		if(CFW.Properties.MAIL_ENABLED) {
			try
		    {
		     
				//---------------------------
				// Create Message
				MimeMessage msg = new MimeMessage(session);
				
				msg.addHeader("Content-type", "text/html; charset=UTF-8");
				msg.addHeader("Content-Transfer-Encoding", "8bit");
				
				msg.setFrom(new InternetAddress(CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY, CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY));
				msg.setReplyTo(InternetAddress.parse(CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY, false));
				
				msg.setSubject(subject, "UTF-8");
				msg.setContent(body, "text/html");
				
				msg.setSentDate(new Date());
				
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
				
				//---------------------------
				// Send Message
				Transport.send(msg);  
				

		    }
		    catch (Exception e) {
		    	new CFWLog(logger)
		    		.method("sendEmailFromNoReply")
		    		.severe("Exception occured while sending mail.", e);
		    }
		}
	}
	
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(String toEmail, String subject, String body, File attachment){
		Session session = initializeSession();
		sendFromNoReplyWithAttachement(session, toEmail, subject, body, attachment);
	}
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(Session session, String toEmail, String subject, String body, File attachment){
		try{
	         MimeMessage msg = new MimeMessage(session);
	         msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		     msg.addHeader("format", "flowed");
		     msg.addHeader("Content-Transfer-Encoding", "8bit");
		      
		     msg.setFrom(new InternetAddress(CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY, CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY));

		     msg.setReplyTo(InternetAddress.parse(CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY, false));

		     msg.setSubject(subject, "UTF-8");

		     msg.setSentDate(new Date());

		     msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
		      
	         // Create the message body part
	         BodyPart messageBodyPart = new MimeBodyPart();

	         // Fill the message
	         messageBodyPart.setText(body);
	         
	         // Create a multipart message for attachment
	         Multipart multipart = new MimeMultipart();

	         // Set text message part
	         multipart.addBodyPart(messageBodyPart);

	         // Second part is attachment
	         messageBodyPart = new MimeBodyPart();
	         DataSource source = new FileDataSource(attachment);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(attachment.getName());
	         multipart.addBodyPart(messageBodyPart);

	         // Send the complete message parts
	         msg.setContent(multipart);

	         // Send message
	         Transport.send(msg);

	      }catch (MessagingException e) {
	    	  new CFWLog(logger)
	    		.method("sendEmailFromNoReply")
	    		.severe("Exception occured while sending mail.", e);
	      } catch (UnsupportedEncodingException e) {
	    	  new CFWLog(logger)
	    		.method("sendEmailFromNoReply")
	    		.severe("Exception occured while sending mail.", e);
		}
	}
	
	//To Be Done: Email with image example
	
//	public static void sendImageEmail(Session session, String toEmail, String subject, String body){
//		try{
//	         MimeMessage msg = new MimeMessage(session);
//	         msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
//		     msg.addHeader("format", "flowed");
//		     msg.addHeader("Content-Transfer-Encoding", "8bit");
//		      
//		     msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));
//
//		     msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
//
//		     msg.setSubject(subject, "UTF-8");
//
//		     msg.setSentDate(new Date());
//
//		     msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
//		      
//	         // Create the message body part
//	         BodyPart messageBodyPart = new MimeBodyPart();
//
//	         messageBodyPart.setText(body);
//	         
//	         // Create a multipart message for attachment
//	         Multipart multipart = new MimeMultipart();
//
//	         // Set text message part
//	         multipart.addBodyPart(messageBodyPart);
//
//	         // Second part is image attachment
//	         messageBodyPart = new MimeBodyPart();
//	         String filename = "image.png";
//	         DataSource source = new FileDataSource(filename);
//	         messageBodyPart.setDataHandler(new DataHandler(source));
//	         messageBodyPart.setFileName(filename);
//	         //Trick is to add the content-id header here
//	         messageBodyPart.setHeader("Content-ID", "image_id");
//	         multipart.addBodyPart(messageBodyPart);
//
//	         //third part for displaying image in the email body
//	         messageBodyPart = new MimeBodyPart();
//	         messageBodyPart.setContent("<h1>Attached Image</h1>" +
//	        		     "<img src='cid:image_id'>", "text/html");
//	         multipart.addBodyPart(messageBodyPart);
//	         
//	         //Set the multipart message to the email message
//	         msg.setContent(multipart);
//
//	         // Send message
//	         Transport.send(msg);
//	         System.out.println("EMail Sent Successfully with image!!");
//	      }catch (MessagingException e) {
//	         e.printStackTrace();
//	      } catch (UnsupportedEncodingException e) {
//			 e.printStackTrace();
//		}
//	}
}
