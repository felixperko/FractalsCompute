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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xerial.snappy.Snappy;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		BfLayerList layerList = new BfLayerList(layers);
		container.addClientParameter(new StaticParamSupplier("layers", layerList));
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, container);
			
			
			String str = new String(outputStream.toByteArray());
			StringReader umLayer2Reader = new StringReader(str);
			
			ParamContainer c2 = mapper.readValue(str.getBytes(), ParamContainer.class);
			
			System.out.println(str);
			
			byte[] arr = Snappy.compress(str, Charset.forName("UTF-8"));
			System.out.println(str.length());
			System.out.println(arr.length);
			
			System.out.println(new String(Base64.getEncoder().encode(arr)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static ParamContainer deserializeBase64(String base64) throws IOException, ClassNotFoundException{
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
		ObjectInputStream ois = new ObjectInputStream(in);
		Object object = ois.readObject();
		if (!(object instanceof ParamContainer))
			throw new IllegalArgumentException("Serialized object isn't ParamContainer but "+object.getClass().getName());
		return (ParamContainer)object;
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

	public Map<String, ParamSupplier> getClientParameters() {
		return clientParameters;
	}

	public void setClientParameters(Map<String, ParamSupplier> clientParameters) {
		this.clientParameters = clientParameters;
	}
	
	public String serializeBase64() throws IOException{
		ByteArrayOutputStream out = null;
        ObjectOutputStream oos = null;
        out = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(out);
        oos.writeObject(this);
        oos.close();
		return new String(Base64.getEncoder().encode(out.toByteArray()), UTF8);
	}

}