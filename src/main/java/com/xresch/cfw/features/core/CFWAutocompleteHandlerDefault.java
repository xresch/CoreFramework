package com.xresch.cfw.features.core;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;

public class CFWAutocompleteHandlerDefault extends CFWAutocompleteHandler {

	private static Logger logger = CFWLog.getLogger(CFWAutocompleteHandlerDefault.class.getName());
	private Class<? extends CFWObject> clazz;
	
	public CFWAutocompleteHandlerDefault(Class<? extends CFWObject> clazz) {
		this.setMaxResults(10);
		this.clazz = clazz;
	}
	
	public CFWAutocompleteHandlerDefault(Class<? extends CFWObject> clazz, int maxResults) {
		super(maxResults);
		this.clazz = clazz;
	}
	@Override
	public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue)  {
		
		CFWField parent = this.getParent();
		String fieldname = parent.getName();
		AutocompleteResult result = null;
		if( !(parent.getValue() instanceof Object[])) {
			try {
				result = clazz.newInstance()
					.select("DISTINCT "+fieldname)
					.whereLike(fieldname, "%"+inputValue+"%")
					.orderby(fieldname)
					.limit(this.getMaxResults())
					.getAsAutocompleteResult(fieldname, fieldname);
				
			} catch (Exception e) {
				new CFWLog(logger)
				.method("getAutocompleteData")
				.severe("Exception occured while trying to instanciate a CFWObject.", e);
			} 
		}else {
			
		}

		return result;
	}

}
