package de.felixperko.fractals.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xerial.snappy.Snappy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.felixperko.fractals.system.PadovanLayerConfiguration;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BfLayerList;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;

public class ParamContainer implements Serializable{
	
	final static String UTF8 = "UTF-8";
	
//	public static void main(String[] args) {
//		ParamContainer container = new ParamContainer();
//		container.addClientParameter(new StaticParamSupplier("midpoint", new DoubleComplexNumber(0.123, 0.246)));
//		container.addClientParameter(new StaticParamSupplier("c", new DoubleComplexNumber(0.369, 0.482)));
//		try {
//			String serialized = container.serializeBase64();
//			System.out.println(serialized);
//			ParamContainer container2 = deserializeBase64(serialized);
//			System.out.println(container2.getClientParameter("midpoint").getGeneral(DoubleComplexNumber.class).toString());
//		} catch (IOException | ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void main(String[] args) {
		ParamContainer container = new ParamContainer();
		List<Layer> layers = new ArrayList<>();
		BreadthFirstLayer layer = new BreadthFirstLayer().with_culling(true).with_max_iterations(1000).with_priority_multiplier(3).with_priority_shift(20).with_samples(42);
		BreadthFirstLayer layer2 = new BreadthFirstUpsampleLayer(4, 256).with_culling(true).with_rendering(true).with_priority_shift(10);
		layers.add(layer);
		layers.add(layer2);
		PadovanLayerConfiguration layerConfig = new PadovanLayerConfiguration(layers);
		container.addClientParameter(new StaticParamSupplier("midpoint", new DoubleComplexNumber(1,2)));
		container.addClientParameter(new StaticParamSupplier("layerConfiguration", layerConfig));
		
		try {
			System.out.println(container.serializeJson(false));
			System.out.println(container.serializeJson(true));
			System.out.println(container.serializeJsonCompressedBase64());
			System.out.println(deserializeJsonCompressedBase64(container.serializeJsonCompressedBase64()).serializeJson(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ParamContainer deserializeObjectBase64(String base64) throws IOException, ClassNotFoundException{
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
		ObjectInputStream ois = new ObjectInputStream(in);
		Object object = ois.readObject();
		if (!(object instanceof ParamContainer))
			throw new IllegalArgumentException("Serialized object isn't ParamContainer but "+object.getClass().getName());
		return (ParamContainer)object;
	}
	
	public static ParamContainer deserializeJsonCompressedBase64(String jsonCompressedBase64) throws JsonParseException, JsonMappingException, IOException {
		return deserializeJsonCompressed(Base64.getDecoder().decode(jsonCompressedBase64));
	}
	
	public static ParamContainer deserializeJsonCompressed(byte[] compressedJson) throws JsonParseException, JsonMappingException, IOException {
		String uncompressed = Snappy.uncompressString(compressedJson, Charset.forName(UTF8));
		return deserializeJson(uncompressed);
	}
	
	public static ParamContainer deserializeJson(String json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json.getBytes(), ParamContainer.class);
	}

	private static final long serialVersionUID = 2325163791938639608L;
	
	private Map<String, ParamSupplier> clientParameters;

	public ParamContainer() {
		this.clientParameters = new HashMap<>();
	}

	public ParamContainer(Map<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}

	public ParamContainer(ParamContainer parent, boolean newInstances) {
		if (!newInstances)
			this.clientParameters = parent.getClientParameters();
		else {
			this.clientParameters = new HashMap<String, ParamSupplier>();
			for (Entry<String, ParamSupplier> e : parent.getClientParameters().entrySet()){
				this.clientParameters.put(e.getKey(), e.getValue().copy());
			}
		}
	}
	
	public void applyParams(ParamContainer copyContainer, boolean onlyOverrideExisting) {
		for (Entry<String, ParamSupplier> e : copyContainer.getClientParameters().entrySet()) {
			String name = e.getKey();
			if (!onlyOverrideExisting || this.clientParameters.containsKey(name))
				this.clientParameters.put(name, e.getValue().copy());
		}
	}
	
	public void applyParams(ParamContainer copyContainer, HashSet<String> overrideParamNames) {
		for (Entry<String, ParamSupplier> e : copyContainer.getClientParameters().entrySet()) {
			String name = e.getKey();
			if (overrideParamNames.contains(name))
				this.clientParameters.put(name, e.getValue().copy());
		}
	}
	
	public ParamContainer getSubContainer(HashSet<String> includeParamNames) {
		HashMap<String, ParamSupplier> map = new HashMap<>();
		for (String name : includeParamNames) {
			if (clientParameters.containsKey(name))
				map.put(name, clientParameters.get(name).copy());
		}
		return new ParamContainer(map);
	}

	public boolean needsReset(Map<String, ParamSupplier> oldParams){
		boolean reset = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : clientParameters.values()) {
				supplier.updateChanged(oldParams.get(supplier.getName()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
		return reset;
	}
	
	public void applyParams(ParamContainer paramContainer) {
		Map<String, ParamSupplier> old = getClientParameters();
		this.clientParameters = new HashMap<>(paramContainer.getClientParameters());
		for (String key : old.keySet())
			if (!this.clientParameters.containsKey(key))
				this.clientParameters.put(key, old.get(key).copy());
	}
	
	public boolean applyParamsAndNeedsReset(ParamContainer paramContainer) {
		Map<String, ParamSupplier> old = getClientParameters();
		applyParams(paramContainer);
		return needsReset(old);
	}

	public void addClientParameter(ParamSupplier paramSupplier) {
		clientParameters.put(paramSupplier.getName(), paramSupplier);
	}

	public boolean hasClientParameters() {
		return clientParameters.size() > 0;
	}

	public ParamSupplier getClientParameter(String name) {
		return clientParameters.get(name);
	}
	
	@JsonIgnore
	public Map<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}

	@JsonIgnore
	public void setClientParameters(Map<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}
	
	public Collection<ParamSupplier> getParameters(){
		return clientParameters.values();
	}
	
	public void setParameters(Collection<ParamSupplier> parameters) {
		for (ParamSupplier supplier : parameters)
			clientParameters.put(supplier.getName(), supplier);
	}
	
	public String serializeObjectBase64() throws IOException{
		ByteArrayOutputStream out = null;
        ObjectOutputStream oos = null;
        out = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(out);
        oos.writeObject(this);
        oos.close();
		return new String(Base64.getEncoder().encode(out.toByteArray()), UTF8);
	}
	
	public String serializeJson(boolean pretty) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		if (pretty)
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		return mapper.writeValueAsString(this);
	}
	
	public byte[] serializeJsonCompressed() throws IOException {
		return Snappy.compress(serializeJson(false), Charset.forName(UTF8));
	}
	
	public String serializeJsonCompressedBase64() throws IOException {
		return new String(Base64.getEncoder().encode(serializeJsonCompressed()));
	}

}