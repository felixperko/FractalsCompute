package de.felixperko.fractals.network.interfaces;

import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.network.infra.connection.ServerConnection;

public class NetworkInterfaceFactory {
	
	Class<? extends ClientMessageInterface> messageInterfaceClass;
	Class<? extends ClientSystemInterface> systemInterfaceClass;
	
	public NetworkInterfaceFactory(Class<? extends ClientMessageInterface> messageInterfaceClass, Class<? extends ClientSystemInterface> systemInterfaceClass) {
		this.messageInterfaceClass = messageInterfaceClass;
		this.systemInterfaceClass = systemInterfaceClass;
	}
	
	public ClientMessageInterface createMessageInterface(ServerConnection serverConnection) {
		try {
			return messageInterfaceClass.getConstructor(ServerConnection.class).newInstance(serverConnection);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ClientSystemInterface createSystemInterface() {
		try {
			return systemInterfaceClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
