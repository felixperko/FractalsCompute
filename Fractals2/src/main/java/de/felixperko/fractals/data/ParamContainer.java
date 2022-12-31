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
import java.util.Iterator;
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
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstSystem;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.serialization.Compression;

public class ParamContainer implements Serializable{
	
	//
	// STATIC
	//

	private static final long serialVersionUID = 2325163791938639608L;
	private static final Logger LOG = LoggerFactory.getLogger(ParamContainer.class);
	private final static String UTF8 = "UTF-8";
	
	public static void main(String[] args) {
		ParamConfiguration config = new ParamConfiguration("qkuTtk", 1.0);
		ParamContainer container = new ParamContainer(config);
		List<Layer> layers = new ArrayList<>();
		BreadthFirstLayer layer = new BreadthFirstLayer().with_culling(true).with_max_iterations(1000).with_priority_multiplier(3).with_priority_shift(20).with_samples(42);
		BreadthFirstLayer layer2 = new BreadthFirstUpsampleLayer(4, 256).with_culling(true).with_rendering(true).with_priority_shift(10);
		layers.add(layer);
		layers.add(layer2);
		PadovanLayerConfiguration layerConfig = new PadovanLayerConfiguration(layers);
		container.addParam(new StaticParamSupplier(CommonFractalParameters.PARAM_MIDPOINT, new DoubleComplexNumber(1,2)));
		container.addParam(new StaticParamSupplier(BreadthFirstSystem.PARAM_LAYER_CONFIG, layerConfig));
		
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
	
	private Map<String, ParamSupplier> paramMap;
	private Map<String, ParamSupplier> paramsByName;
	private ParamConfiguration paramConfiguration;

	public ParamContainer(ParamConfiguration paramConfig) {
		this.paramMap = new LinkedHashMap<>();
		this.paramsByName = new LinkedHashMap<>();
		this.paramConfiguration = paramConfig;
	}

	public ParamContainer(ParamContainer parent, boolean newInstances) {
		this.paramConfiguration = parent.paramConfiguration;
		if (!newInstances) {
			this.paramMap = parent.getParamMap();
			this.paramsByName = parent.paramsByName;
		}
		else {
			this.paramMap = new LinkedHashMap<String, ParamSupplier>();
			this.paramsByName = new LinkedHashMap<String, ParamSupplier>();
			for (Entry<String, ParamSupplier> e : parent.getParamMap().entrySet()){
				this.paramMap.put(e.getKey(), e.getValue().copy());
			}
			for (Entry<String, ParamSupplier> e : parent.paramsByName.entrySet()){
				this.paramsByName.put(e.getKey(), this.paramMap.get(e.getValue().getUID()));
			}
		}
	}
	
	public boolean updateChangedFlag(Map<String, ParamSupplier> oldParams){
		boolean changed = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : paramMap.values()) {
				ParamSupplier oldSupp = oldParams.get(supplier.getUID());
				supplier.updateChanged(oldSupp);
				if (supplier.isChanged()) {
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
	 * @param copyContainer - the container to take the new values from
	 * @param onlyOverrideExisting
	 */
	public void applyParams(ParamContainer copyContainer, boolean onlyOverrideExisting) {
		for (Entry<String, ParamSupplier> e : copyContainer.getParamMap().entrySet()) {
			String uid = e.getKey();
			ParamSupplier oldSupplier = getParam(uid);
			ParamSupplier newSupplier = e.getValue().copy();
			if (oldSupplier != null) {
				newSupplier.updateChanged(oldSupplier);
			}
			if (oldSupplier != null || !onlyOverrideExisting) {
				addParam(newSupplier);
			}
		}
	}
	
	public void applyParams(ParamContainer copyContainer, Collection<String> overrideParamNames) {
		for (Entry<String, ParamSupplier> e : copyContainer.getParamMap().entrySet()) {
			String name = e.getKey();
			if (overrideParamNames.contains(name))
				addParam(e.getValue().copy());
		}
	}

	public boolean needsReset(Map<String, ParamSupplier> oldParams){
		if (oldParams == null)
			return true;
		boolean reset = false;
		for (ParamSupplier supplier : paramMap.values()) {
			supplier.updateChanged(oldParams.get(supplier.getUID()));
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
		ParamContainer container = new ParamContainer(paramConfiguration);
		for (String uid : includeParams) {
			ParamSupplier supp = getParam(uid);
			if (supp != null) {
				container.addParam(newInstances ? supp.copy() : supp);
			} else {
				LOG.warn("Tried to include sub ParamCountainer with missing parameter "+uid);
			}
		}
		return container;
	}
	
	public void applyParams(ParamContainer paramContainer) {
		
		Map<String, ParamSupplier> old = getParamMap();
		this.paramMap = new HashMap<>(paramContainer.getParamMap());
		this.paramsByName = new HashMap<>(paramContainer.paramsByName);
		
		for (String key : old.keySet()) {
			ParamSupplier oldSupplier = old.get(key);
			ParamSupplier newSupplier = this.getParam(key);
			if (newSupplier != null)
				newSupplier.updateChanged(oldSupplier);
			else //readd old values
				addParam(oldSupplier.copy());				
		}
	}
	
	public boolean applyParamsAndNeedsReset(ParamContainer paramContainer) {
		Map<String, ParamSupplier> old = getParamMap();
		applyParams(paramContainer);
		return needsReset(old);
	}

	public void addParam(ParamSupplier paramSupplier) {
		if (paramConfiguration == null)
			throw new IllegalStateException("ParamConfiguration not set");
		paramMap.put(paramSupplier.getUID(), paramSupplier);
		ParamDefinition def = paramConfiguration.getParamDefinition(paramSupplier);
		String name = def != null ? def.getName() : paramSupplier.getUID();
		paramsByName.put(name, paramSupplier);
	}
	
	public boolean removeParam(String uid) {
		ParamSupplier supp = paramMap.remove(uid);
		if (supp == null)
			return false;
		String name = null;
		for (Entry<String, ParamSupplier> e : paramsByName.entrySet()) {
			if (e.getValue().getUID().equals(uid)) {
				name = e.getKey();
				break;
			}
		}
		if (name != null)
			paramsByName.remove(name);
		return true;
	}

	public boolean hasClientParameters() {
		return paramMap.size() > 0;
	}

	public ParamSupplier getParam(String uid) {
		ParamSupplier supp = paramMap.get(uid);
		//TODO generalize sub params
		if (supp == null && paramConfiguration != null){
			ParamSupplier calcNameSupp = paramMap.get(CommonFractalParameters.PARAM_CALCULATOR);
			String calcName = calcNameSupp == null ? null : calcNameSupp.getGeneral(String.class);
			supp = paramConfiguration.getDefaultValue(calcName, uid);
		}
		return supp;
	}
	
	@JsonIgnore
	public Map<String, ParamSupplier> getParamMap() {
		return paramMap;
	}

	@JsonIgnore
	public void setParams(Map<String, ParamSupplier> clientParameters, Map<String, String> namesByUID) {
		this.paramMap = clientParameters;
		this.paramsByName = new LinkedHashMap<>();
		for (String uid : namesByUID.keySet()) {
			ParamSupplier supp = paramMap.get(uid);
			if (supp != null)
				this.paramsByName.put(namesByUID.get(uid), supp);
		}
	}
	
	public Collection<ParamSupplier> getParameters(){
		return paramMap.values();
	}
	
	public void setParameters(Collection<ParamSupplier> parameters, boolean clearExisting) {
		if (clearExisting) {
			paramMap.clear();
			paramsByName.clear();
		}
		for (ParamSupplier supplier : parameters)
			paramMap.put(supplier.getUID(), supplier);
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
	
	public String serializeCustom(ParamConfiguration paramConfig) {
		if (paramConfig == null && this.paramConfiguration != null)
			paramConfig = this.paramConfiguration;
		StringBuilder sb = new StringBuilder();
		
		
		return sb.toString();
	}

}