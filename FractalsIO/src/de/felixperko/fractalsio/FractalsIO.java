package de.felixperko.fractalsio;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.manager.ClientManagers;
import de.felixperko.fractals.manager.ClientNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientMessageInterface;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.messages.SessionInitRequestMessage;
import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.parameters.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;

public class FractalsIO {
	
	static ClientManagers managers;
	
	static ClientConfiguration clientConfiguration;
	
	static FractalsIOMessageInterface messageInterface;
	
	public static void main(String[] args) {
		
		NumberFactory numberFactory = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		Map<String, ParamSupplier> params = new HashMap<>();
		int samplesDim = 2;
		params.put("width", new StaticParamSupplier("width", (Integer)1000));
		params.put("height", new StaticParamSupplier("height", (Integer)1000));
		params.put("chunkSize", new StaticParamSupplier("chunkSize", (Integer)200));
//		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(0.251), new DoubleNumber(0.00004849892910689283399687005))));
		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
//		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(4./50000.)));
		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(3.)));
		params.put("iterations", new StaticParamSupplier("iterations", (Integer)1000));
		params.put("samples", new StaticParamSupplier("samples", (Integer)(samplesDim*samplesDim)));
		
		params.put("calculator", new StaticParamSupplier("calculator", "MandelbrotCalculator"));
		params.put("systemName", new StaticParamSupplier("systemName", "BasicSystem"));
		
		params.put("start", new StaticParamSupplier("start", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
//		params.put("c", new StaticParamSupplier("c", new DoubleComplexNumber(new DoubleNumber(0.5), new DoubleNumber(0.3))));
//		params.put("start", new CoordinateParamSupplier("start", numberFactory));
		params.put("c", new CoordinateBasicShiftParamSupplier("c", numberFactory, samplesDim));
		params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(3), new DoubleNumber(0))));
		params.put("limit", new StaticParamSupplier("limit", (Double)100.));
		
		clientConfiguration = new ClientConfiguration();
		clientConfiguration.addRequest(new SystemClientData(params, 0));
		
		
		messageInterface = new FractalsIOMessageInterface();
		managers = new ClientManagers(messageInterface);
//		clientNetworkManager = new ClientNetworkManager(managers, messageInterface);
		
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
	}
}
