package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import de.felixperko.fractals.system.task.Layer;

class BfLayerListDeserializer extends JsonDeserializer<List<Layer>> {
	@Override
	public List<Layer> deserialize(JsonParser p, DeserializationContext ctxt, List<Layer> intoValue)
			throws IOException {
    	intoValue = p.readValueAs(new TypeReference<List<Layer>>() {});
    	return intoValue;
	}

	@Override
	public List<Layer> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
    	List<Layer> intoValue = p.readValueAs(new TypeReference<List<Layer>>() {});
    	return intoValue;
	}
}