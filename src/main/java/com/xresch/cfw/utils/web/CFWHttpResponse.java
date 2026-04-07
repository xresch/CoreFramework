package com.xresch.cfw.utils.web;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.io.CloseMode;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

/******************************************************************************************************
 * Inner Class for HTTP Response
 ******************************************************************************************************/
public class CFWHttpResponse {
	
	private Logger responseLogger = CFWLog.getLogger(CFWHttpResponse.class.getName());
	
	private URL url;
	private String body;
	private int status = 500;
	private long duration = -1;
	private Header[] headers;
	
	private boolean errorOccured = false;
	private String errorMessage = null;
	
	CloseableHttpClient httpClient = null;
	private HttpServletResponse response = null;
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public CFWHttpResponse(CloseableHttpClient httpClient, HttpUriRequestBase requestBase, boolean autoCloseClient) {
		
		this.httpClient = httpClient;
		
		//----------------------------------
		// Get URL
		try {
			url = requestBase.getUri().toURL();
		} catch (Exception e) {
			errorOccured = true;
			new CFWLog(responseLogger).severe("URL is malformed:"+e.getMessage(), e);
			return ;
		}
		
		//----------------------------------
		// Send Request and Read Response
		long startMillis = System.currentTimeMillis();
		try {
			
			Boolean success = httpClient.execute(requestBase, new HttpClientResponseHandler<Boolean>() {
				@Override
				public Boolean handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
					if(response != null) {
						status = response.getCode();
						headers = response.getHeaders();
						
						HttpEntity entity = response.getEntity();
						if(entity != null) {
							body = EntityUtils.toString(response.getEntity());
						}

					}
					return true;
				}
			});
			
		} catch (IOException e) {
			errorOccured = true;
			errorMessage = e.getMessage();
			new CFWLog(responseLogger).warn("Exception occured during HTTP request:"+e.getMessage(), e);
			
		}finally {
			long endMillis = System.currentTimeMillis();
			duration = endMillis - startMillis;
			
			if(autoCloseClient) {
				close();
			}
		}
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public void close() {
		if(httpClient != null) {
			httpClient.close(CloseMode.IMMEDIATE);
		}
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public boolean errorOccured() {
		return errorOccured;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public String errorMessage() {
		return errorMessage;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public URL getURL() {
		return url;
	}
	
	/******************************************************************************************************
	 * Get the body content of the response.
	 * @return String or null on error
	 ******************************************************************************************************/
	public String getResponseBody() {
		return body;
	}
	
	/******************************************************************************************************
	 * Get the body content of the response as a JsonObject.
	 * @param url used for the request.
	 * @return JsonObject or null in case of issues
	 ******************************************************************************************************/
	public JsonElement getResponseBodyAsJsonElement(){
		
		//----------------------------------
		// Check Body
		if(Strings.isNullOrEmpty(body)) {
			new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to a JsonElement.", new Exception());
			return null;
		}
		
		if(!body.trim().startsWith("{")
		&& !body.trim().startsWith("[")
		) {
			String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
			new CFWLog(responseLogger).severe("Http Response was not a JsonElement: "+messagePart, new Exception());
			return null;
		}
		
		//----------------------------------
		// Create Object
		JsonElement jsonElement = CFW.JSON.fromJson(body);

		if(jsonElement == null) {
			new CFWLog(responseLogger).severe("Error occured while converting http response body to JSON Element.", new Exception());
		}
		
		return jsonElement;

	}
	
	/******************************************************************************************************
	 * Get the body content of the response as a JsonObject.
	 * @param url used for the request.
	 * @return JsonObject or null in case of issues
	 ******************************************************************************************************/
	public JsonObject getResponseBodyAsJsonObject(){
		
		//----------------------------------
		// Check Body
		if(Strings.isNullOrEmpty(body)) {
			new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to a JsonElement.", new Exception());
			return null;
		}
		
		if(!body.trim().startsWith("{")) {
			String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
			new CFWLog(responseLogger).severe("Http Response was not a JsonObject: "+messagePart, new Exception());
			return null;
		}
		
		//----------------------------------
		// Create Object
		JsonElement jsonElement = CFW.JSON.fromJson(body);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		
		if(jsonObject == null) {
			new CFWLog(responseLogger).severe("Error occured while converting http response body to JSON Object.", new Exception());
		}
		
		return jsonObject;

	}
	
	/******************************************************************************************************
	 * Get the body content of the response as a JsonArray.
	 * @param url used for the request.
	 * @return JsonArray never null, empty array on error
	 ******************************************************************************************************/
	public JsonArray getResponseBodyAsJsonArray(){
		
		//----------------------------------
		// Check Body
		if(Strings.isNullOrEmpty(body)) {
			new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to JSON.", new Exception());
			return null;
		}
		
		if(!body.trim().startsWith("{")
		&& !body.trim().startsWith("[")
		) {
			String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
			new CFWLog(responseLogger).severe("Http Response was not JSON: "+messagePart, new Exception());
			return null;
		}
		
		//----------------------------------
		// CreateArray
		JsonArray jsonArray = new JsonArray();
		
		JsonElement jsonElement = CFW.JSON.fromJson(body);
		if(jsonElement == null || jsonElement.isJsonNull()) {
			return jsonArray;
		}
		
		if(jsonElement.isJsonArray()) {
			jsonArray = jsonElement.getAsJsonArray();
		}else if(jsonElement.isJsonObject()) {
			JsonObject object = jsonElement.getAsJsonObject();
			if(object.get("error") != null) {
				new CFWLog(responseLogger).severe("Error occured while reading http response: "+object.get("error").toString());
				CFW.Messages.addErrorMessage("Error: ");
				return jsonArray;
			}else {
				new CFWLog(responseLogger).severe("Error occured while reading http response:"+CFW.JSON.toString(jsonElement));
			}
		}
		
		return jsonArray;
	}

	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public int getStatus() {
		return status;
	}
	
	/******************************************************************************************************
	 * Returns a state for this response
	 * @param checkErrorResponse toggle if HTTP Status should be checked to be below 400.
	 ******************************************************************************************************/
	public CFWStateOption getState(boolean checkErrorResponse) {
		//------------------------------
		// Check Error Occured
		if( ! this.errorOccured() ) {
			
			//------------------------------
			// Check Status Code
			if(checkErrorResponse) {
				if( this.getStatus() < 400 ) {
					return CFWStateOption.GREEN;
				}else {
					return CFWStateOption.RED;
				}
			}
			
			return CFWStateOption.GREEN;
		}else {
			return CFWStateOption.RED;
		}

	}

	/******************************************************************************************************
	 * Returns the approximate duration that was needed for executing and reading the request.
	 ******************************************************************************************************/
	public long getDuration() {
		return duration;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public Header[] getHeaders() {
		return headers;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public JsonObject getHeadersAsJson() {
		
		JsonObject object = new JsonObject();
		for(Header entry : headers) {
			
			if(entry.getName() != null) {
				object.addProperty(entry.getName(), entry.getValue());
			}
		}
		
		return object;
	}
	
}