package de.felixperko.fractalsio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.client.ClientManagers;
import de.felixperko.fractals.manager.common.NetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.messages.SessionInitRequestMessage;
import de.felixperko.fractals.network.messages.UpdateConfigurationMessage;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.LayerConfiguration;

public class FractalsIO {
	
	static ClientManagers managers;
	
	static ClientConfiguration clientConfiguration;
	
	static FractalsIOMessageInterface messageInterface;
	
	static NumberFactory numberFactory;
	static Number zoom;
	
	public static void main(String[] args) {
		
		numberFactory = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		Map<String, ParamSupplier> params = new HashMap<>();
		int samplesDim = 1;
		params.put("width", new StaticParamSupplier("width", (Integer)1024));
		params.put("height", new StaticParamSupplier("height", (Integer)1024));
		Integer chunkSize = 512;
		params.put("chunkSize", new StaticParamSupplier("chunkSize", chunkSize));
//		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(0.251), new DoubleNumber(0.00004849892910689283399687005))));
		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(0.251), new DoubleNumber(0.000055))));
		zoom = numberFactory.createNumber(5./100000.);
		params.put("zoom", new StaticParamSupplier("zoom", zoom));
		params.put("iterations", new StaticParamSupplier("iterations", (Integer)50000));
		params.put("samples", new StaticParamSupplier("samples", (Integer)(samplesDim*samplesDim)));
		
		List<BreadthFirstLayer> layers = new ArrayList<>();
		layers.add(new BreadthFirstUpsampleLayer(16, chunkSize).with_culling(true));
		layers.add(new BreadthFirstUpsampleLayer(4, chunkSize).with_culling(true).with_priority_shift(10));
		layers.add(new BreadthFirstLayer().with_priority_shift(20));
		layers.add(new BreadthFirstLayer().with_priority_shift(30).with_samples(8));
		LayerConfiguration layerConfig = new LayerConfiguration(layers, 0.05, 20, 42);
		params.put("layerConfiguration", new StaticParamSupplier("layerConfiguration", layerConfig));
		params.put("border_generation", new StaticParamSupplier("border_generation", (Double) 0.));
		params.put("border_dispose", new StaticParamSupplier("border_dispose", (Double) 5.));
		params.put("task_buffer", new StaticParamSupplier("task_buffer", (Integer) 5));
		
//		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(.0), new DoubleNumber(0.0))));
//		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(4./50000.)));
//		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(3.)));
		
		params.put("calculator", new StaticParamSupplier("calculator", "MandelbrotCalculator"));
		params.put("systemName", new StaticParamSupplier("systemName", "BreadthFirstSystem"));
		params.put("numberFactory", new StaticParamSupplier("numberFactory", numberFactory));
		params.put("chunkFactory", new StaticParamSupplier("chunkFactory", new ArrayChunkFactory(ReducedNaiveChunk.class, chunkSize)));
		
		params.put("start", new StaticParamSupplier("start", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
		params.put("c", new CoordinateBasicShiftParamSupplier("c"));
		params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(2), new DoubleNumber(0))));
		params.put("limit", new StaticParamSupplier("limit", (Double)100.));
		
//		params.put("c", new StaticParamSupplier("c", new DoubleComplexNumber(new DoubleNumber(0.5), new DoubleNumber(0.3))));
//		params.put("start", new CoordinateParamSupplier("start", numberFactory));
		
		clientConfiguration = new ClientConfiguration();
		clientConfiguration.addRequest(new SystemClientData(params, 0));
		
		
		messageInterface = new FractalsIOMessageInterface();
		managers = new ClientManagers(messageInterface);
		messageInterface.setManagers(managers);
		
//		managers.getClientNetworkManager().connectToServer("192.168.0.9", 3141);
		managers.getClientNetworkManager().connectToServer("localhost", 3141);
		while (managers.getClientNetworkManager().getClientInfo() == null) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		managers.getClientNetworkManager().getServerConnection().writeMessage(new SessionInitRequestMessage(clientConfiguration));
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		zoom.mult(numberFactory.createNumber(0.5));
//		StaticParamSupplier zoomSupplier = new StaticParamSupplier("zoom", zoom);
//		zoomSupplier.setLayerRelevant(true);
//		params.put("zoom", zoomSupplier);
//		managers.getClientNetworkManager().getServerConnection().writeMessage(new UpdateConfigurationMessage(clientConfiguration));
	}
}
