package de.felixperko.fractals.system.task;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "@type")
@JsonSubTypes({
    @Type(value = BreadthFirstLayer.class, name = "bfLayer"),
    @Type(value = BreadthFirstUpsampleLayer.class, name = "bfUpsampleLayer") })
//@JsonTypeInfo(
//	    use = JsonTypeInfo.Id.NAME,
//	    include = JsonTypeInfo.As.WRAPPER_OBJECT)
public interface Layer extends Serializable{
	
	int getId();
	boolean isActive(int pixel);
	double getPriorityMultiplier();
	double getPriorityShift();
	int getSampleCount();
	boolean cullingEnabled();
	boolean renderingEnabled();
	int getUpsample();
	void setId(int id);
	int getMaxIterations();


	class LayerListSerializer extends JsonSerializer<List<Layer>> {
		@Override
		public void serialize(List<Layer> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeStartArray();
			for (Layer layer : value) {
				jgen.writeStartObject();
				jgen.writeObjectField("layer", layer);
				jgen.writeEndObject();
			}
			jgen.writeEndArray();
		}
	}
}