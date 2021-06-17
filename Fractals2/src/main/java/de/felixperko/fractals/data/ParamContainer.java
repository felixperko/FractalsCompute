package de.felixperko.fractals.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.felixperko.fractals.system.PadovanLayerConfiguration;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.serialization.Compression;

public class ParamContainer implements Serializable{
	
	//
	// STATIC
	//

	private static final long serialVersionUID = 2325163791938639608L;
	private static final Logger LOG = LoggerFactory.getLogger(ParamContainer.class);
	private final static String UTF8 = "UTF-8";
	
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
		String uncompressed = Compression.uncompressString(compressedJson, Charset.forName(UTF8));
		return deserializeJson(uncompressed);
	}
	
	public static ParamContainer deserializeJson(String json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json.getBytes(), ParamContainer.class);
	}
	
	//
	// CLASS
	//
	
	private Map<String, ParamSupplier> clientParameters;
	private ParamConfiguration paramConfiguration;

	public ParamContainer() {
		this.clientParameters = new LinkedHashMap<>();
	}
	
	public ParamContainer(List<ParamSupplier> params){
		this.clientParameters = new LinkedHashMap<>();
		for (ParamSupplier supp : params){
			if (supp.getName() == null)
				throw new IllegalStateException("ParamSupplier doesn't has a name");
			this.clientParameters.put(supp.getName(), supp);
		}
	}

	public ParamContainer(LinkedHashMap<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}

	public ParamContainer(ParamContainer parent, boolean newInstances) {
		if (!newInstances)
			this.clientParameters = parent.getClientParameters();
		else {
			this.clientParameters = new LinkedHashMap<String, ParamSupplier>();
			for (Entry<String, ParamSupplier> e : parent.getClientParameters().entrySet()){
				this.clientParameters.put(e.getKey(), e.getValue().copy());
			}
		}
	}
	
	public boolean updateChangedFlag(Map<String, ParamSupplier> oldParams){
		boolean changed = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : clientParameters.values()) {
				ParamSupplier oldSupp = oldParams.get(supplier.getName());
				supplier.updateChanged(oldSupp);
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						changed = true;
				}
			}
		}
		return changed;
	}
	
	public void setParamConfiguration(ParamConfiguration paramConfiguration){
		this.paramConfiguration = paramConfiguration;
	}

	/**
	 * Applies the given parameters to this container. old values are overwritten and the changed property is updated.
	 * @param paramContainer - the container to take the new values from
	 * @param onlyOverrideExisting
	 */
	public void applyParams(ParamContainer copyContainer, boolean onlyOverrideExisting) {
		for (Entry<String, ParamSupplier> e : copyContainer.getClientParameters().entrySet()) {
			String name = e.getKey();
			ParamSupplier oldSupplier = this.clientParameters.get(name);
			ParamSupplier newSupplier = e.getValue().copy();
			if (oldSupplier != null) {
				newSupplier.updateChanged(oldSupplier);
			}
			if (oldSupplier != null || !onlyOverrideExisting) {
				this.clientParameters.put(name, newSupplier);
			}
		}
	}
	
	public void applyParams(ParamContainer copyContainer, Collection<String> overrideParamNames) {
		for (Entry<String, ParamSupplier> e : copyContainer.getClientParameters().entrySet()) {
			String name = e.getKey();
			if (overrideParamNames.contains(name))
				this.clientParameters.put(name, e.getValue().copy());
		}
	}

	public boolean needsReset(Map<String, ParamSupplier> oldParams){
		if (oldParams == null)
			return true;
		boolean reset = false;
		for (ParamSupplier supplier : clientParameters.values()) {
			supplier.updateChanged(oldParams.get(supplier.getName()));
			if (supplier.isChanged()) {
				if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
					reset = true;
			}
		}
		return reset;
	}
	
	/**
	 * Creates a ParamContainer, applying values of includeParams; copies if newInstances is true
	 * @param newInstances true -> copy ParamSuppliers; false -> applies the same ParamSupplier objects
	 * @param includeParams The parameters to apply to the new ParamContainer
	 * @return
	 */
	public ParamContainer createSubContainer(boolean newInstances, String... includeParams) {
		ParamContainer container = new ParamContainer();
		for (String paramName : includeParams) {
			ParamSupplier paramSupplier = this.clientParameters.get(paramName);
			if (paramSupplier != null) {
				container.addClientParameter(newInstances ? paramSupplier.copy() : paramSupplier);
			} else {
				LOG.warn("Tried to include sub ParamCountainer with missing parameter "+paramName);
			}
		}
		return container;
	}
	
	public void applyParams(ParamContainer paramContainer) {
		
		Map<String, ParamSupplier> old = getClientParameters();
		this.clientParameters = new HashMap<>(paramContainer.getClientParameters());
		
		for (String key : old.keySet()) {
			ParamSupplier oldSupplier = old.get(key);
			ParamSupplier newSupplier = this.getClientParameter(key);
			if (newSupplier != null)
				newSupplier.updateChanged(oldSupplier);
			else //readd old values
				this.clientParameters.put(key, oldSupplier.copy());				
		}
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
		ParamSupplier supp = clientParameters.get(name);
		if (supp == null && paramConfiguration != null){
			ParamSupplier calcNameSupp = clientParameters.get("calculator");
			String calcName = calcNameSupp == null ? null : calcNameSupp.getGeneral(String.class);
			supp = paramConfiguration.getDefaultValue(calcName, name);
		}
		return supp;
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
	
	public void setParameters(Collection<ParamSupplier> parameters, boolean clearExisting) {
		if (clearExisting)
			clientParameters.clear();
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
		return Compression.compress(serializeJson(false), Charset.forName(UTF8));
	}
	
	public String serializeJsonCompressedBase64() throws IOException {
		return new String(Base64.getEncoder().encode(serializeJsonCompressed()));
	}

}