package com.xresch.cfw.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
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
import javax.mail.util.ByteArrayDataSource;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public class CFWMailer {
	
	private static Logger logger = CFWLog.getLogger(CFWMailer.class.getName());
	
	private MimeMessage message;
	
	//main message part
	private BodyPart messageBodyPart;
	
	//wrapper of all parts
	private Multipart multipart;
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailer(String subject) {

		try {
			message = new MimeMessage(initializeSession());
			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
			message.addHeader("format", "flowed");
			message.addHeader("Content-Transfer-Encoding", "8bit");
			
			message.setSubject(subject, "UTF-8");
			
	         multipart = new MimeMultipart();

			 messageBodyPart = new MimeBodyPart();

	         // Set text message part
	         multipart.addBodyPart(messageBodyPart);

	         // Send the complete message parts
	         message.setContent(multipart);
			 
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}

	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailer(String subject, String messageBodyHTML) {
		this(subject);
		try {
	         // Set the message
	         messageBodyPart.setContent(messageBodyHTML, "text/html");
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailer fromNoReply() {
		try {
			message.setFrom(new InternetAddress(CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY, CFW.Properties.MAIL_SMTP_FROMMAIL_NOREPLY));
		} catch (MessagingException | UnsupportedEncodingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailer from(String eMail, String displayName) {
		try {
			message.setFrom(new InternetAddress(eMail, displayName));
		} catch (MessagingException | UnsupportedEncodingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailer recipientsTo(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.TO, recipients);
	}
	
	/***************************************************************
	 * Set the CC recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailer recipientsCC(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.CC, recipients);
	}
	
	/***************************************************************
	 * Set the BCC recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailer recipientsBCC(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.BCC, recipients);
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	private CFWMailer recipients(Message.RecipientType type, LinkedHashMap<String,String> recipients) {
		try {
			
			InternetAddress[] addresses = new InternetAddress[recipients.size()];
			int index = 0;
			for(Entry<String, String> entry : recipients.entrySet()) {
				String eMail = entry.getKey();
				String displayName = entry.getValue();
				addresses[index] = new InternetAddress(eMail, displayName);
				index++;
			}
			message.setRecipients(type, addresses);
		      
		} catch (MessagingException | UnsupportedEncodingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	
	/***************************************************************
	 * Send the eMail.
	 ***************************************************************/
	public CFWMailer send() {
		try {
	         Transport.send(message); 
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error sending eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
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
				props.put("mail.smtp.ssl.checkserveridentity", "true"); 
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
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
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
	
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(String toEmail, String subject, String body, File attachment){
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
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
	public static void sendFromNoReplyWithAttachement(
			Session session
			, String toEmail
			, String subject
			, String body
			, InputStream attachmentOctetStream
			, String attachmentFilename
		){
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
		DataSource source;
		try {
			source = new ByteArrayDataSource(attachmentOctetStream, "application/octet-stream");

			sendFromNoReplyWithAttachement(session, toEmail, subject, body, source, attachmentFilename);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			new CFWLog(logger).severe("Error occured while sending a mail with attachment: "+e.getMessage(), e);
		}

	}
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(Session session, String toEmail, String subject, String body, String attachmentText, String attachmentFilename){
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
		DataSource source;
		try {
			source = new ByteArrayDataSource(attachmentText, "text/plain");
			
			sendFromNoReplyWithAttachement(session, toEmail, subject, body, source, attachmentFilename);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			new CFWLog(logger).severe("Error occured while sending a mail with attachment: "+e.getMessage(), e);
		}

	}
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(Session session, String toEmail, String subject, String body, File attachment){
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
		DataSource source = new FileDataSource(attachment);

		sendFromNoReplyWithAttachement(session, toEmail, subject, body, source, attachment.getName());
	}
	
	/***************************************************************
	 * Utility method to send simple HTML email
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 ***************************************************************/
	public static void sendFromNoReplyWithAttachement(Session session, String toEmail, String subject, String body, DataSource attachment, String attachmentName){
		
		if(!CFW.Properties.MAIL_ENABLED) { return ; }
		
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
	         messageBodyPart.setDataHandler(new DataHandler(attachment));
	         messageBodyPart.setFileName(attachmentName);
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
