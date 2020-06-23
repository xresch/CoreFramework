package com.pengtoolbox.cfw.tests.assets.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.datahandling.CFWFormHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.tests.assets.mockups.CFWObjectMockup;

public class FormTestServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(FormTestServlet.class.getName());

	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		
		CFWLog log = new CFWLog(logger).method("doGet");
		
		HTMLResponse html = new HTMLResponse("Test Page");
		StringBuffer content = html.getContent();
		  
		//------------------------------
		// Test Form
		//------------------------------
        CFWForm form = new CFWForm("directForm", "Save");
        form.addChild(CFWField.newString(FormFieldType.TEXT, "Firstname"));
        form.addChild(CFWField.newString(FormFieldType.TEXT, "Lastname"));
        
        content.append("<h2>Direct use of BTForm</h2>");
        content.append(form.getHTML());
        
		//------------------------------
		// Test Form
		//------------------------------
//        content.append("<h2>Form Created Through CFWObject</h2>");
//        content.append(new CFWObjectMockup().toForm("myForm", "Submit!!!").getHTML());
//        
//		//------------------------------
//		// Form with Handler
//		//------------------------------
//        
//        content.append("<h2>Form with BTFormHandler</h2>");
//        BTForm handledForm = new CFWObjectMockup().toForm("handlerForm", "Handle!!!");
//        
//        handledForm.setFormHandler(new BTFormHandler() {
//			@Override
//			public void handleForm(HttpServletRequest request, HttpServletResponse response, BTForm form, CFWObject object) {
//				// TODO Auto-generated method stub
//				String formID = request.getParameter(BTForm.FORM_ID);
//				
//				JSONResponse json = new JSONResponse();
//		    	json.addAlert(MessageType.SUCCESS, "BTFormHandler: Post recieved from "+formID+"!!!");
//			}
//		});
//        content.append(handledForm.getHTML());
        
		//------------------------------
		// Form with Handler
		//------------------------------
        content.append("<h2>Map Requests and Validate</h2>");
        CFWForm handledForm2 = new CFWObjectMockup().toForm("handlerForm2", "Handle Again!!!");
        
        handledForm2.setFormHandler(new CFWFormHandler() {
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
				// TODO Auto-generated method stub
				String formID = request.getParameter(CFWForm.FORM_ID);
				origin.mapRequestParameters(request);
				JSONResponse json = new JSONResponse();
		    	json.addAlert(MessageType.SUCCESS, "BTFormHandler: Post recieved from "+formID+"!!!");
		    	json.addAlert(MessageType.SUCCESS, origin.dumpFieldsAsKeyValueHTML());
		    	form.mapRequestParameters(request);
			}
		});
        content.append(handledForm2.getHTML());
    }
	
	
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
    	    	
    	String formID = request.getParameter(CFWForm.FORM_ID);
    	
    	JSONResponse json = new JSONResponse();
    	json.addAlert(MessageType.SUCCESS, "Post recieved from "+formID+"!!!");
    	
    }
	
}