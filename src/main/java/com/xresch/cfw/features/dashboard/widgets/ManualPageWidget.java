package com.xresch.cfw.features.dashboard.widgets;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.manual.ManualPage;

public class ManualPageWidget extends ManualPage {

	public ManualPageWidget(ManualPage parent, WidgetDefinition widget) {
		this(widget);
		
		parent.addChild(this);
	}
	
	public ManualPageWidget(WidgetDefinition widget) {
		//Set title to command name
		super(widget.widgetName());
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(widget.descriptionHTML());
		
		this.content(builder.toString());
		
	}

}
