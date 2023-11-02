package com.xresch.cfw.features.dashboard.widgets;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;

public class ManualPageWidget extends ManualPage {

	public ManualPageWidget(ManualPage parent, WidgetDefinition widget) {
		this(widget, true);
		
		parent.addChild(this);
	}
	
	public ManualPageWidget(WidgetDefinition widget) {
		this(widget, true);
	}
	
	public ManualPageWidget(WidgetDefinition widget, boolean registerLocales) {
		
		//-------------------------------------
		// Set title to widget name
		super(widget.widgetName());
		
		StringBuilder builder = new StringBuilder();
		
		//-------------------------------------
		// Localization
		if(registerLocales) {
			HashMap<Locale, FileDefinition> localeMap = widget.getLocalizationFiles();
			
			if(localeMap != null) {
				for(Entry<Locale, FileDefinition> entry : localeMap.entrySet()) {
					CFW.Localization.registerLocaleFile(entry.getKey(), FeatureManual.URI_MANUAL, entry.getValue());
				}
			}
		}
		
		//-------------------------------------
		// Settings
		builder.append("<h2 class=\"toc-hidden\">Settings</h2>");
		CFWObject settings = widget.getSettings();
		
		if(settings.getFields().size() == 0) {
			builder.append("<p>No widget specific settings.</p>");
		}else {
			builder.append("<ul>");
			for(CFWField<?> field : settings.getFields().values()) {
				if(field.getDescription() == null) { continue; }
				builder.append("<li><b>"+field.getLabel()+":&nbsp;</b>"+field.getDescription()+"</li>");
			}
			builder.append("</ul>");
		}
		//-------------------------------------
		// Set description 
		builder.append("<h2 class=\"toc-hidden\">Usage</h2>");
		builder.append(widget.descriptionHTML());
		
		this.content(builder.toString());
		
	}

}
