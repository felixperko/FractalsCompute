package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.felixperko.fractals.system.parameters.suppliers.JsonAbstractTypedObject;
import de.felixperko.fractals.system.parameters.suppliers.JsonTypedObject;
import de.felixperko.fractals.system.task.Layer;

public class BfLayerList extends JsonAbstractTypedObject{

	public static final String TYPE_NAME = "bfLayerList";
	
	@JsonTypeInfo(
		    use = JsonTypeInfo.Id.NAME,
		    include = JsonTypeInfo.As.PROPERTY,
		    property = "@type")
	@JsonSubTypes({
	    @Type(value = BreadthFirstLayer.class),
	    @Type(value = BreadthFirstUpsampleLayer.class)})
	@JsonDeserialize(using = BfLayerListDeserializer.class)
	List<Layer> layers;

	public BfLayerList() {
		super(TYPE_NAME);
		layers = new ArrayList<>();
	}
	
	public BfLayerList(List<Layer> layers) {
		super(TYPE_NAME);
		this.layers = layers;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}
	
	public void addLayer(Layer layer) {
		this.layers.add(layer);
	}

	@Override
	public String getType() {
		return TYPE_NAME;
	}
}
