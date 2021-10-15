package com.xresch.cfw.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
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

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

public class CFWMailBuilder {
	
	private static Logger logger = CFWLog.getLogger(CFWMailBuilder.class.getName());
	
	private MimeMessage message;
	
	//wrapper of all parts
	private Multipart multipart;
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailBuilder(String subject) {

		try {
			message = new MimeMessage(initializeSession());
			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
			message.addHeader("format", "flowed");
			message.addHeader("Content-Transfer-Encoding", "8bit");
			
			message.setSubject(subject, "UTF-8");
			
	         multipart = new MimeMultipart();

	         // Send the complete message parts
	         message.setContent(multipart);
			 
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}

	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailBuilder(String subject, String messageBodyHTML) {
		this(subject);
	    this.addMessage(messageBodyHTML, true);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailBuilder fromNoReply() {
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
	public CFWMailBuilder from(String eMail, String displayName) {
		try {
			message.setFrom(new InternetAddress(eMail, displayName));
		} catch (MessagingException | UnsupportedEncodingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	/***************************************************************
	 * 
	 ***************************************************************/
	public CFWMailBuilder replyTo(String eMail, String displayName) {
		try {
			message.setReplyTo(new Address[] {new InternetAddress(eMail, displayName)} );
		} catch (MessagingException | UnsupportedEncodingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	/***************************************************************
	 * 
	 * @param enhanceWhitespaces flag to define if newlines and tabs should be replaced with the respective html entities
	 ***************************************************************/
	public CFWMailBuilder addMessage(String html, boolean enhanceWhitespaces) {
		
		if(enhanceWhitespaces) {
			html = html.replaceAll("\r\n", "<br>")
					   .replaceAll("\n"  , "<br>")
					   .replaceAll("\t"  , "&emsp;");
		}
		
		try {
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(html, "text/html");
			multipart.addBodyPart(messageBodyPart);
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	private LinkedHashMap<String,String> convertUserListToRecipientList(HashMap<Integer, User> userlist) {
		LinkedHashMap<String,String> emailDisplayNameMap = new LinkedHashMap<>();
		for(User user : userlist.values()) {
			
			if( !Strings.isNullOrEmpty(user.email()) ) {
				emailDisplayNameMap.put(user.email(), user.createUserLabel());
			}
		}
		
		return emailDisplayNameMap;
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param userlist with userID and User
	 ***************************************************************/
	public CFWMailBuilder recipientsTo(HashMap<Integer, User> userlist) {
		return recipients(Message.RecipientType.TO, convertUserListToRecipientList(userlist));
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailBuilder recipientsTo(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.TO, recipients);
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param userlist with userID and User
	 ***************************************************************/
	public CFWMailBuilder recipientsCC(HashMap<Integer, User> userlist) {
		return recipients(Message.RecipientType.CC, convertUserListToRecipientList(userlist));
	}
	/***************************************************************
	 * Set the CC recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailBuilder recipientsCC(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.CC, recipients);
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param userlist with userID and User
	 ***************************************************************/
	public CFWMailBuilder recipientsBCC(HashMap<Integer, User> userlist) {
		return recipients(Message.RecipientType.BCC, convertUserListToRecipientList(userlist));
	}
	
	/***************************************************************
	 * Set the BCC recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	public CFWMailBuilder recipientsBCC(LinkedHashMap<String,String> recipients) {
		return recipients(Message.RecipientType.BCC, recipients);
	}
	
	/***************************************************************
	 * Set the recipients of the eMail.
	 * @param recipients map with email as key and displayName as value
	 ***************************************************************/
	private CFWMailBuilder recipients(Message.RecipientType type, LinkedHashMap<String,String> recipients) {
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
	 * Add text as an attachment with the specified attachment name.
	 * You might want to include ".txt" in the filename.
	 ***************************************************************/
	public CFWMailBuilder addAttachment(String attachmentName, String textContent) {

		try {

			DataSource attachmentSource = new ByteArrayDataSource(textContent, "text/plain");
			addAttachment(attachmentName, attachmentSource);
		} catch (IOException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		
		return this;
	}
		
	/***************************************************************
	 * Add text as an attachment with the specified attachment name.
	 * You might want to include ".txt" in the filename.
	 ***************************************************************/
	public CFWMailBuilder addAttachment(String attachmentName, InputStream inputStream, String mimeType) {

		try {
			DataSource attachmentSource = new ByteArrayDataSource(inputStream, mimeType);
			addAttachment(attachmentName, attachmentSource);
		} catch (IOException e) {
			new CFWLog(logger).severe("Error creating eMail: "+e.getMessage(), e);
		}
		
		return this;
	}
	
	/***************************************************************
	 * Add text as an attachment with the specified attachment name.
	 * You might want to include ".txt" in the filename.
	 ***************************************************************/
	public CFWMailBuilder addAttachment(String attachmentName, byte[] bytes, String mimeType) {
		
		DataSource attachmentSource = new ByteArrayDataSource(bytes, mimeType);
		addAttachment(attachmentName, attachmentSource);

		return this;
	}
	
	/***************************************************************
	 * Add a File as an attachment.
	 ***************************************************************/
	public CFWMailBuilder addAttachment(File file) {

		DataSource attachmentSource = new FileDataSource(file);
		addAttachment(file.getName(), attachmentSource);
		
		return this;
	}
	
	/***************************************************************
	 * Send the eMail.
	 ***************************************************************/
	public CFWMailBuilder addAttachment(String attachmentName, DataSource attachment) {
		try {

			 // Second part is attachment
	         BodyPart attachmentBodyPart = new MimeBodyPart();
	         attachmentBodyPart.setDataHandler(new DataHandler(attachment));
	         attachmentBodyPart.setFileName(attachmentName);
	         multipart.addBodyPart(attachmentBodyPart);
	         
		} catch (MessagingException e) {
			new CFWLog(logger).severe("Error sending eMail: "+e.getMessage(), e);
		}
		return this;
	}
	
	/***************************************************************
	 * Send the eMail.
	 ***************************************************************/
	public CFWMailBuilder send() {
		
		if(!CFW.Properties.MAIL_ENABLED) { return this ; }
		
		try {
			message.setSentDate(new Date());
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
}
