package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerBigDecimal implements JsonSerializer<BigDecimal> {

	public SerializerBigDecimal() {

	}
	
	@Override
	public JsonElement serialize(BigDecimal object, Type type, JsonSerializationContext context) {
			
		return new JsonPrimitive(object.stripTrailingZeros().toPlainString());
	}

}
