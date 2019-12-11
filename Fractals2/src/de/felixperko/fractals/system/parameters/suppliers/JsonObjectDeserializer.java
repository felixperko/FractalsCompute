package de.felixperko.fractals.system.parameters.suppliers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BfLayerList;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;

public class JsonObjectDeserializer extends JsonDeserializer<Object> {
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonObjectDeserializer.class);
	static Map<String, Class<? extends JsonTypedObject>> availableClasses = new HashMap<>();
	
	static {
		addClass(BreadthFirstLayer.TYPE_NAME, BreadthFirstLayer.class);
		addClass(BreadthFirstUpsampleLayer.TYPE_NAME, BreadthFirstUpsampleLayer.class);
		addClass(BfLayerList.TYPE_NAME, BfLayerList.class);
	}
	
	public static void addClass(String name, Class<? extends JsonTypedObject> cls) {
		availableClasses.put(name, cls);
	}
	
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		ObjectCodec codec = jp.getCodec();
		ObjectNode node = codec.readTree(jp);			
		
		String type;
		try {
			//type = (String)jp.getValueAsString("type");
			type = (String)node.get("type").asText();
			//node.get("layers").get("type").asText();
		} catch (NullPointerException e) {
			LOG.warn("couldn't deserialize object at "+jp.getCurrentValue().getClass().getName());
			return codec.treeToValue(node, Object.class);
		}
		
		Class<? extends JsonTypedObject> cls = availableClasses.get(type);
		if (cls != null) {
			return codec.treeToValue(node, cls);
		}
		LOG.warn("couldn't deserialize object at "+jp.getCurrentValue().getClass().getName());
		return codec.treeToValue(node, Object.class);
	}

}
