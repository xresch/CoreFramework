package com.xresch.cfw.features.parameter;

import com.xresch.cfw.features.manual.ManualPage;

public class CFWQueryManualPageParameter extends ManualPage {

	public CFWQueryManualPageParameter(ManualPage parent, ParameterDefinition paramDef) {

		super(paramDef.getParamUniqueName());
		
		if(parent != null) {
			parent.addChild(this);
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("<div>"+paramDef.descriptionHTML()+"</div>");
		
		this.content(builder.toString());
		
	}

}
