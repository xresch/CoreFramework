package com.xresch.cfw.response;

import java.util.Collection;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTMenu;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class HTMLResponse extends AbstractHTMLResponse {
	
	private static final Logger logger = CFWLog.getLogger(HTMLResponse.class.getName());
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	public HTMLResponse(String pageTitle){
		
		super();
		
		//------------------------------------------
		// Title
		this.pageTitle = pageTitle;
		
		if(CFW.DB.Config.getConfigAsBoolean(FeatureConfig.CATEGORY_LOOK_AND_FEEL, FeatureConfig.CONFIG_MENU_TITLE_IN_TAB)) {
			String menuTitle = CFW.DB.Config.getConfigAsString(FeatureConfig.CATEGORY_LOOK_AND_FEEL, FeatureConfig.CONFIG_MENU_TITLE);
			if(!Strings.isNullOrEmpty(menuTitle)) {
				this.pageTitle = menuTitle + ": " + this.pageTitle;
			}
		}
		
		//------------------------------------------
		// CSS
		String themeName = CFW.DB.Config.getConfigAsString(FeatureConfig.CATEGORY_LOOK_AND_FEEL, FeatureConfig.CONFIG_THEME);
		if(themeName.equals("custom")) {
			this.addCSSFileTheme(HandlingType.FILE, "./resources/css", "bootstrap-theme-custom.css");
		}else {
			this.addCSSFileTheme(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "bootstrap-theme-"+themeName+".css");
		}
		
		String codeThemeName = CFW.DB.Config.getConfigAsString(FeatureConfig.CATEGORY_LOOK_AND_FEEL, FeatureConfig.CONFIG_CODE_THEME);
		this.addCSSFileTheme(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".css", "highlightjs_"+codeThemeName+".css");
	}
		
	@Override
	public StringBuilder buildResponse() {
		
		StringBuilder buildedPage = new StringBuilder();
		
		buildedPage.append("<!DOCTYPE html>\n");
		buildedPage.append("<html id=\"cfw-html\">\n");
		
			buildedPage.append("<head>\n");
				
				buildedPage.append("<meta charset=\"utf-8\">");
				buildedPage.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
		    	buildedPage.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
		    	// Set Content-Security-Policy header to avoid random errors with page load
		    	// Allow all hosts to be able to load scripts from other locations, e.g. pyscript
		    	buildedPage.append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'self' 'unsafe-inline' 'unsafe-eval' * data: *;\">");
		    	buildedPage.append("<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"/resources/images/favicon.ico\"/>");
				buildedPage.append("<title>").append(this.pageTitle).append("</title>");
				buildedPage.append(head);
				for(FileAssembly cssAssembly : cssAssemblies) {
					if(cssAssembly.hasFiles()) {
						buildedPage.append("<link rel=\"stylesheet\" href=\""+cssAssembly.assembleAndCache().getAssemblyServletPath()+"\" />");
					}
				}

				if(headjs.hasFiles()) {
					buildedPage.append("<script src=\""+headjs.assembleAndCache().getAssemblyServletPath()+"\"></script>");
				}
			buildedPage.append("</head>\n");
			
			buildedPage.append("<body id=\"cfw-body\">\n");
			
				//--------------------------
				// Menubar
				
				this.appendSectionTitle(buildedPage, "Menubar");
				buildedPage.append("");
				CFWSessionData sessionData = CFW.Context.Request.getSessionData();
				
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
						buildedPage.append("<script src=\""+jsAssembly.assembleAndCache().getAssemblyServletPath()+"\"></script>");
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
