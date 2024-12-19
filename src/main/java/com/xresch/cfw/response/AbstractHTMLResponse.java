package com.xresch.cfw.response;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileAssembly;
import com.xresch.cfw.caching.FileDefinition;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class AbstractHTMLResponse extends AbstractResponse {

	protected String pageTitle;
	
	protected FileAssembly headjs = new FileAssembly("js_assembly_jshead", "js");

	protected FileAssembly bottomjsCustom = new FileAssembly("js_assembly_custom", "js");
	protected FileAssembly assemblyCSSTheme = new FileAssembly("css_assembly_theme", "css");
	protected FileAssembly assemblyCSSCustom = new FileAssembly("css_assembly_custom", "css");
	
	protected ArrayList<FileAssembly> bottomjsAssemblies = new ArrayList<FileAssembly>();
	protected ArrayList<FileAssembly> cssAssemblies = new ArrayList<FileAssembly>();
	
	protected ArrayList<FileDefinition> singleJSBottom = new ArrayList<FileDefinition>();
	
	protected StringBuilder head = new StringBuilder();
	protected StringBuilder menu = new StringBuilder();
	protected StringBuilder footer = new StringBuilder();
	protected StringBuilder supportInfo = new StringBuilder();
	protected StringBuilder javascript = new StringBuilder();
	protected StringBuilder javascriptData = new StringBuilder("JSDATA = {};\n");
	
	public AbstractHTMLResponse(){
		super();
		
		bottomjsAssemblies.add(CFW.Registry.Components.getGlobalJavascripts());
		bottomjsAssemblies.add(bottomjsCustom);
		
		cssAssemblies.add(assemblyCSSTheme);
		cssAssemblies.add(CFW.Registry.Components.getGlobalCSS());
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
	
	public void addJavascriptData(String key, JsonElement value){
		this.javascriptData
				.append("JSDATA.")
				.append(key)
				.append(" = ")
				.append( CFW.JSON.toJSON(value) )
				.append("';\n");
		
	}
	
	public void addCSSFileTheme(FileDefinition.HandlingType type, String path, String filename){
		assemblyCSSTheme.addFile(type, path, filename);
	}
	
	public void addCSSFile(FileDefinition.HandlingType type, String path, String filename){
		assemblyCSSCustom.addFile(type, path, filename);
	}
	
	public void addCSSAssembly(FileAssembly assembly){
		cssAssemblies.add(assembly);
	}
	
	
	protected void appendSectionTitle(StringBuilder buildedPage, String title){ 
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
	public StringBuilder getHead() { return head; }
	public StringBuilder getMenu() { return menu; }
	public StringBuilder getFooter() {return footer;}
	public StringBuilder getJavascript() {return javascript;}
	public StringBuilder getSupportInfo() {return supportInfo;}

	//##############################################################################
	// Setters
	//##############################################################################
	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
	public void setHeader(StringBuilder head) { this.head = head; }
	public void setMenu(StringBuilder menu) { this.menu = menu; }
	public void setFooter(StringBuilder footer) {this.footer = footer;}
	public void setSupportInfo(StringBuilder comments) {this.supportInfo = comments;}
	
	
}
