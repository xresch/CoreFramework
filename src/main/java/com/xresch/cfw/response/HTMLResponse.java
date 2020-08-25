package com.xresch.cfw.response;

import java.util.Collection;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.SessionData;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTMenu;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class HTMLResponse extends AbstractHTMLResponse {
	
	private static final Logger logger = CFWLog.getLogger(HTMLResponse.class.getName());
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	public HTMLResponse(String pageTitle){
		
		super();
		
		this.pageTitle = pageTitle;
		
		//------------------------------------------
		// CSS
		String theme = CFW.DB.Config.getConfigAsString(FeatureConfiguration.CONFIG_THEME);
		if(theme.equals("custom")) {
			this.addCSSFileCFW(FileDefinition.HandlingType.FILE, "./resources/css", "bootstrap-theme-custom.css");
		}else {
			this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "bootstrap-theme-"+CFW.DB.Config.getConfigAsString(FeatureConfiguration.CONFIG_THEME)+".css");
		}
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "bootstrap-tagsinput.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "summernote-bs4.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "jquery-ui.min.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "font-awesome.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "highlightjs_"+CFW.DB.Config.getConfigAsString(FeatureConfiguration.CONFIG_CODE_THEME)+".css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "chartjs.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "cfw.css");
		this.addCSSFileCFW(FileDefinition.HandlingType.FILE, "./resources/css", "custom.css");
		
		//------------------------------------------
		// Javascript
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "jquery-3.4.1.min.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "jquery-ui-1.12.3.min.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "bootstrap.bundle.min.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "bootstrap-tagsinput.js");
		
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "summernote-bs4.min.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "highlight.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "lodash-full-4.17.15.min.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "moment-2.27.0.js"); // required by ChartJS
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "chartjs-2.93.min.js"); 
		
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "cfw_components.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "cfw.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "cfw_renderer.js");
		this.addJSFileBottomCFW(FileDefinition.HandlingType.FILE, "./resources/js", "custom.js");
		      
	}
		
	@Override
	public StringBuffer buildResponse() {
		
		StringBuffer buildedPage = new StringBuffer();
		
		buildedPage.append("<!DOCTYPE html>\n");
		buildedPage.append("<html id=\"cfw-html\">\n");
		
			buildedPage.append("<head>\n");
				
				buildedPage.append("<meta charset=\"utf-8\">");
				buildedPage.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
		    	buildedPage.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
		    	buildedPage.append("<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"/resources/images/favicon.ico\"/>");
				buildedPage.append("<title>").append(this.pageTitle).append("</title>");
				buildedPage.append(head);
				for(FileAssembly cssAssembly : cssAssemblies) {
					if(cssAssembly.hasFiles()) {
						buildedPage.append("<link rel=\"stylesheet\" href=\""+cssAssembly.assemble().cache().getAssemblyServletPath()+"\" />");
					}
				}

				if(headjs.hasFiles()) {
					buildedPage.append("<script src=\""+headjs.assemble().cache().getAssemblyServletPath()+"\"></script>");
				}
			buildedPage.append("</head>\n");
			
			buildedPage.append("<body id=\"cfw-body\">\n");
			
				//--------------------------
				// Menubar
				
				this.appendSectionTitle(buildedPage, "Menubar");
				buildedPage.append("");
				SessionData sessionData = CFW.Context.Request.getSessionData();
				BTMenu menu = sessionData.getMenu();
				if(menu != null) {
					buildedPage.append(menu.getHTML());
				}
				
				//--------------------------
				// Messages
				this.appendSectionTitle(buildedPage, "Messages");
				buildedPage.append("<div id=\"cfw-messages\">");
					
				Collection<AlertMessage> messageArray = CFW.Context.Request.getAlertMessages();
					if(messageArray != null) {
						for(AlertMessage message : messageArray) {
							buildedPage.append(message.createHTML());
						}
					}
				
				buildedPage.append("</div>");
				
				//--------------------------
				// Content
				this.appendSectionTitle(buildedPage, "Content");
				buildedPage.append("<div id=\"cfw-content\"> ");
					buildedPage.append("<div id=\"cfw-container\" class=\"container\">");
						buildedPage.append(this.content);
					buildedPage.append("</div>");
				buildedPage.append("</div>");
				
				//--------------------------
				// Footer
				this.appendSectionTitle(buildedPage, "Footer");

				BTFooter footer = sessionData.getFooter();
				if(footer != null) {
					buildedPage.append(footer.getHTML());
				}
				
//				String footerTemplate = CFWFiles.getFileContent(request,CFW.PATH_TEMPLATE_FOOTER);
//				if(footerTemplate != null){
//					String customFooterInserted = footerTemplate.replace("{!customFooter!}", this.footer);	
//					buildedPage.append(customFooterInserted);
//				}else{
//					buildedPage.append("<!-- FAILED TO LOAD FOOTER! -->");
//				}

				//--------------------------
				// JavascriptData
				this.appendSectionTitle(buildedPage, "Javascript Data");
				buildedPage.append("<script id=\"javaScriptData\" style=\"display: none;\">");
				buildedPage.append(this.javascriptData);
				buildedPage.append("</script>");
				
				//--------------------------
				// Javascript
				this.appendSectionTitle(buildedPage, "Javascript");
				buildedPage.append("<div id=\"javascripts\">");
				for(FileAssembly jsAssembly : bottomjsAssemblies) {	
					if(jsAssembly.hasFiles()) {
						buildedPage.append("<script src=\""+jsAssembly.assemble().cache().getAssemblyServletPath()+"\"></script>");
					}
				}
				
				for(FileDefinition fileDef : singleJSBottom) {
					buildedPage.append("\n").append(fileDef.getJavascriptTag()).append("\n");
				}
				
				buildedPage.append(this.javascript);
				buildedPage.append("</div>");
				
				//--------------------------
				// Support Info
//				this.appendSectionTitle(buildedPage, "Support Info");
//				
//				String supportInfoTemplate = CFWFiles.getFileContent(request,CFW.PATH_TEMPLATE_SUPPORTINFO);
//				
//				if(supportInfoTemplate != null){
//					String supportInfoInserted = supportInfoTemplate.replace("{!supportInfo!}", this.supportInfo);	
//					buildedPage.append(supportInfoInserted);
//				}else{
//					buildedPage.append("<!-- FAILED TO LOAD SUPPORT INFO! -->");
//				}
					
				
			buildedPage.append("</body>\n");
			
		buildedPage.append("</html>");
		
		return buildedPage;
	}

}
