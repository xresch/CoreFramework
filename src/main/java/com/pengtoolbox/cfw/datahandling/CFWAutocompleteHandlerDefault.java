package com.pengtoolbox.cfw.datahandling;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw.logging.CFWLog;

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
	public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String inputValue)  {
		
		CFWField parent = this.getParent();
		String fieldname = parent.getName();
		LinkedHashMap<Object, Object> result = null;
		if( !(parent.getValue() instanceof Object[])) {
			try {
				result = clazz.newInstance()
					.select("DISTINCT "+fieldname)
					.whereLike(fieldname, "%"+inputValue+"%")
					.orderby(fieldname)
					.limit(this.getMaxResults())
					.getAsLinkedHashMap(fieldname, fieldname);
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
