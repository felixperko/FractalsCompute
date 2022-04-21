package de.felixperko.fractals.util.serialization.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.system.LayerConfiguration;
import de.felixperko.fractals.system.PadovanLayerConfiguration;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BfLayerList;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;

public class JsonObjectDeserializer extends JsonDeserializer<Object> {
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonObjectDeserializer.class);
	static Map<String, Class<? extends JsonTypedObject>> availableClasses = new HashMap<>();
	
	static {
		addClass(BreadthFirstLayer.TYPE_NAME, BreadthFirstLayer.class);
		addClass(BreadthFirstUpsampleLayer.TYPE_NAME, BreadthFirstUpsampleLayer.class);
		addClass(LayerConfiguration.TYPE_NAME, LayerConfiguration.class);
		addClass(PadovanLayerConfiguration.TYPE_NAME, PadovanLayerConfiguration.class);
		addClass(ArrayChunkFactory.TYPE_NAME, ArrayChunkFactory.class);
		addClass(NumberFactory.TYPE_NAME, NumberFactory.class);
	}
	
	public static void addClass(String name, Class<? extends JsonTypedObject> cls) {
		availableClasses.put(name, cls);
	}
	
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		ObjectCodec codec = jp.getCodec();
		
		JsonNode node = codec.readTree(jp);
		
		if (node instanceof ArrayNode) {
			ArrayNode arrNode = (ArrayNode)node;
			List<Object> objects = new ArrayList<>();
			for (JsonNode n : node) {
				objects.add(deserializeNode(n, codec, jp, ctxt));
			}
			return objects;
		}
		
		return deserializeNode(node, codec, jp, ctxt);
		
	}
	
	private Object deserializeNode(JsonNode node, ObjectCodec codec, JsonParser jp, DeserializationContext ctxt) throws IOException {

		String type;
		try {
			type = (String)node.get("type").asText();
		} catch (NullPointerException e) {
			return decode(jp, codec, node, null);
		}
		
		if (type.equals(JsonValueWrapper.TYPE_NAME)) {
			//Class<?> clsName = (Class<?>) ctxt.readValue(jp, Class.class);
			Class<?> cls = (Class<?>) decode(jp, codec, node.get("cls"), Class.class);
			return decode(jp, codec, node.get("val"), cls);
		}
		
		Class<? extends JsonTypedObject> cls = availableClasses.get(type);
		return decode(jp, codec, node, cls);
	}
	
	private Object decode(JsonParser jp, ObjectCodec codec, JsonNode node, Class<?> cls) throws JsonProcessingException {
		if (cls == null || cls == Object.class) {
			LOG.warn("couldn't deserialize object at "+jp.getCurrentValue().getClass().getName());
			return codec.treeToValue(node, Object.class);
		}
		return codec.treeToValue(node, cls);
	}

}
