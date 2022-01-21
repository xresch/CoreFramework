package com.xresch.cfw.features.query.sources;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.NumberRangeValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceRandom extends CFWQuerySource {

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceRandom(CFWQuery parent) {
		super(parent);
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "random";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns random person data for testing.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return "<p>To be done</p>";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					CFWField.newInteger(FormFieldType.NUMBER, "records")
						.setDescription("Creates random person data useful for training, demostration and testing.")
						.addValidator(new NumberRangeValidator(0, 100000))
						.setValue(1000)
				)
			;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue) throws Exception {
		
		int records = (int)parameters.getField("records").getValue();

		long earliest = this.getParent().getContext().getEarliest();
		long latest = this.getParent().getContext().getLatest();
		long diff = latest - earliest;
		long diffStep = diff / records;
		
		for(int i = 0; i < records; i++) {
			
			EnhancedJsonObject person = new EnhancedJsonObject( CFWRandom.randomJSONObjectMightyPerson() );
			person.addProperty("TIME", earliest +(i * diffStep));
			outQueue.add(person);
		}
		

	}

}
