package com.xresch.cfw.response;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class AbstractHTMLResponse extends AbstractResponse {

	protected String pageTitle;
	
	protected FileAssembly headjs = new FileAssembly("js_assembly_jshead", "js");
	protected FileAssembly bottomjsCFW = new FileAssembly("js_assembly_cfw", "js");
	protected FileAssembly bottomjsCustom = new FileAssembly("js_assembly_custom", "js");
	protected FileAssembly assemblyCSSCFW = new FileAssembly("css_assembly_cfw", "css");
	protected FileAssembly assemblyCSSCustom = new FileAssembly("css_assembly_custom", "css");
	
	protected ArrayList<FileAssembly> bottomjsAssemblies = new ArrayList<FileAssembly>();
	protected ArrayList<FileAssembly> cssAssemblies = new ArrayList<FileAssembly>();
	
	protected ArrayList<FileDefinition> singleJSBottom = new ArrayList<FileDefinition>();
	
	protected StringBuffer head = new StringBuffer();
	protected StringBuffer menu = new StringBuffer();
	protected StringBuffer footer = new StringBuffer();
	protected StringBuffer supportInfo = new StringBuffer();
	protected StringBuffer javascript = new StringBuffer();
	protected StringBuffer javascriptData = new StringBuffer("JSDATA = {};\n");
	
	public AbstractHTMLResponse(){
		super();
		
		bottomjsAssemblies.add(bottomjsCFW);
		bottomjsAssemblies.add(bottomjsCustom);
		
		cssAssemblies.add(assemblyCSSCFW);
		cssAssemblies.add(assemblyCSSCustom);
		
		String requestID = (String)request.getAttribute(CFW.REQUEST_ATTR_ID);

		this.addJavascriptData("localeIdentifier", CFW.Localization.getLocaleIdentifierForRequest() );
		this.addJavascriptData(CFW.REQUEST_ATTR_ID, requestID );
		this.addJavascriptData("time", CFW.Time.currentTimestamp());
		
	}
	
	//##############################################################################
	// Class Methods
	//##############################################################################
	
	public void addJSFileHeadAssembly(FileDefinition.HandlingType type, String path, String filename){
		headjs.addFile(type, path, filename);
	}
	
	protected void addJSFileBottomCFW(FileDefinition.HandlingType type, String path, String filename){
		bottomjsCFW.addFile(type, path, filename);
	}
	public void addJSFileBottom(FileDefinition.HandlingType type, String path, String filename){
		bottomjsCustom.addFile(type, path, filename);
	}
	
	public void addJSBottomAssembly(FileAssembly assembly){
		bottomjsAssemblies.add(assembly);
	}
	
	/***************************************************************************
	 * Adds a javascript file to the bottom of the page.
	 * @param javascript
	 ***************************************************************************/
	public void addJSFileBottomSingle(FileDefinition fileDef){
		singleJSBottom.add(fileDef);
	}
	
	/***************************************************************************
	 * Adds the javascript code to the bottom of the page.
	 * @param javascript
	 ***************************************************************************/
	public void addJavascriptCode(String javascript) {
		singleJSBottom.add(new FileDefinition(javascript));
	}
	
	public void addJavascriptData(String key, boolean value){
		this.javascriptData.append("JSDATA.").append(key)
				.append(" = ").append(value).append(";\n");

	}
	public void addJavascriptData(String key, int value){
		this.javascriptData.append("JSDATA.").append(key)
				.append(" = ").append(value).append(";\n");

	}
	public void addJavascriptData(String key, String value){
		this.javascriptData.append("JSDATA.").append(key)
				.append(" = '").append(value).append("';\n");

	}
	
	protected void addCSSFileCFW(FileDefinition.HandlingType type, String path, String filename){
		assemblyCSSCFW.addFile(type, path, filename);
	}
	public void addCSSFile(FileDefinition.HandlingType type, String path, String filename){
		assemblyCSSCustom.addFile(type, path, filename);
	}
	
	public void addCSSAssembly(FileAssembly assembly){
		cssAssemblies.add(assembly);
	}
	
	
	protected void appendSectionTitle(StringBuffer buildedPage, String title){ 
		buildedPage.append("<!--\n");
		buildedPage.append("==================================================\n");
		buildedPage.append(title);
		buildedPage.append("\n==================================================\n");
		buildedPage.append("-->\n");
	}
	
	@Override
	public int getEstimatedSizeChars() {
		int size = this.head.length();
		size += this.menu.length();
		size += this.content.length();
		size += this.footer.length();
		size += this.javascript.length();
		size += this.supportInfo.length();
		
		return size;
	}
	
	//##############################################################################
	// Getters
	//##############################################################################
	public String getPageTitle() { return pageTitle; }
	public StringBuffer getHead() { return head; }
	public StringBuffer getMenu() { return menu; }
	public StringBuffer getFooter() {return footer;}
	public StringBuffer getJavascript() {return javascript;}
	public StringBuffer getSupportInfo() {return supportInfo;}

	//##############################################################################
	// Setters
	//##############################################################################
	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
	public void setHead(StringBuffer head) { this.head = head; }
	public void setMenu(StringBuffer menu) { this.menu = menu; }
	public void setFooter(StringBuffer footer) {this.footer = footer;}
	public void setSupportInfo(StringBuffer comments) {this.supportInfo = comments;}
	
	
}
