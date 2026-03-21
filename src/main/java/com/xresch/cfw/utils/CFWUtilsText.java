package com.xresch.cfw.utils;

import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.xrutils.utils.XRText;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWUtilsText extends XRText{
		
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static CFWField<String> getCheckTypeOptionField(String fieldname, String label, String description) {
		LinkedHashMap<String, String> clone = new LinkedHashMap<>();
		clone.putAll(checkTypeOptions);
		
		return CFWField.newString(CFWField.FormFieldType.SELECT, fieldname)
				.setLabel(label)
				.setDescription(description)
				.setOptions(clone);
	}

}
