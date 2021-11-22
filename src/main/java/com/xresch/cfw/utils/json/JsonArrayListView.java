package com.xresch.cfw.utils.json;

import java.util.AbstractList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

final class JsonArrayListView extends AbstractList<JsonElement> {

	private final JsonArray jsonArray;

	private JsonArrayListView(final JsonArray jsonArray) {
		this.jsonArray = jsonArray;
	}

	static List<JsonElement> of(final JsonArray jsonArray) {
		return new JsonArrayListView(jsonArray);
	}

	// This method is required when implementing AbstractList
	@Override
	public JsonElement get(final int index) {
		return jsonArray.get(index);
	}

	// This method is required when implementing AbstractList as well
	@Override
	public int size() {
		return jsonArray.size();
	}

	// And this one is required to make the list implementation modifiable
	@Override
	public JsonElement set(final int index, final JsonElement element) {
		return jsonArray.set(index, element);
	}

}
