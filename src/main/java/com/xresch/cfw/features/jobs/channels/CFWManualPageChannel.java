package com.xresch.cfw.features.jobs.channels;

import com.xresch.cfw.features.manual.ManualPage;

public class CFWManualPageChannel extends ManualPage {

	public CFWManualPageChannel(ManualPage parent, CFWJobsChannel channel) {

		super(channel.manualPageTitle());
		
		if(parent != null) {
			parent.addChild(this);
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("<div>"+channel.manualPageContent()+"</div>");
		
		this.content(builder.toString());
		
	}

}
