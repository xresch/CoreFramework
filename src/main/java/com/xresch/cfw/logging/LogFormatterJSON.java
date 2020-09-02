package com.xresch.cfw.logging;

import java.util.Date;
import java.util.Map.Entry;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class LogFormatterJSON extends Formatter {
	
	@Override
	public String format(LogRecord rec) {
		
		StringBuilder buf = new StringBuilder(1000);
		LogMessage log = (LogMessage)rec.getParameters()[0];
		

		
		buf.append("{");

			//-------------------------
			// Timestamp
			buf.append("\"time\":\"");
			buf.append(CFW.Time.formatDate(new Date(rec.getMillis())));
			buf.append("\"");
						
			//-------------------------
			// Level
			buf.append(", \"level\":\"");
			buf.append(rec.getLevel());
			buf.append("\"");
			
			//-------------------------
			// Check is Plain
			if(log.isMinimal) {
				buf.append(", \"message\":\"");
				buf.append(CFW.JSON.escapeString(rec.getMessage()));
				buf.append("\"");
				buf.append("}\n");
				return rec.getMessage();
			}
			
			//-------------------------
			// Delta
			buf.append(", \"delta\":\"");
			buf.append(log.deltaStartMillis);
			buf.append("\"");
			
			//-------------------------
			// user
			buf.append(", \"user\":\"");
			buf.append(log.userID);
			buf.append("\"");
			
			
			//-------------------------
			// URL
			buf.append(", \"webURL\":\"");
			buf.append(log.webURL);
			buf.append("\"");
			
			//-------------------------
			// URL
			buf.append(", \"webParams\":\"");
			buf.append(CFW.JSON.escapeString(log.queryString));
			buf.append("\"");
			
			//-------------------------
			// Class
			buf.append(", \"class\":\"");
			buf.append(log.sourceClass);
			buf.append("\"");
			
			//-------------------------
			// Method
			buf.append(", \"method\":\"");
			buf.append(log.sourceMethod);
			buf.append("\"");
			
			//-------------------------
			// RequestID
			buf.append(", \"requestID\":\"");
			buf.append(log.requestID);
			buf.append("\"");
			
			//-------------------------
			// SessionID
			buf.append(", \"sessionID\":\"");
			buf.append(log.sessionID);
			buf.append("\"");
		
			//-------------------------
			// Response Size Bytes
			buf.append(", \"sizeChars\":\"");
			buf.append(log.estimatedResponseSizeChars);
			buf.append("\"");

			//-------------------------
			// message
			buf.append(", \"message\":\"");
			buf.append(CFW.JSON.escapeString(rec.getMessage()));
			buf.append("\"");
			
			//-------------------------
			// Duration Millisecond
			if(log.durationMillis != -1){
				buf.append(", \"duration\":\"");
				buf.append(log.durationMillis);
				buf.append("\"");
			}
			
			//-------------------------
			// Duration Millisecond
			
			if(log.customEntries != null && log.customEntries.size() > 0) {
				for(Entry<String, String> entry : log.customEntries.entrySet()) {
						buf.append(", \""+entry.getKey()+"\":\"");
						buf.append(CFW.JSON.escapeString(entry.getValue()));
						buf.append("\"");
				}
			}
						
			//-------------------------
			// Exception
			if(log.exception != null){
				buf.append(", \"exception\":\"");
				buf.append(CFW.JSON.escapeString(log.exception));
				buf.append("\"");
			}
			
			
		buf.append("}\n");
		
		
		return buf.toString();
	}

}
