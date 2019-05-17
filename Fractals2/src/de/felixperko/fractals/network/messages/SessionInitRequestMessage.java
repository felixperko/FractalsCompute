package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.ClientMessage;

/**
 * client -> server
 * initiates the session (client is assigned to a system, layer and view and will receive relevant chunk/state updates)
 */
public class SessionInitRequestMessage extends ClientMessage{
	private static final long serialVersionUID = -6879047655133190298L;
	
	ClientConfiguration configuration;
	
	public SessionInitRequestMessage(ClientConfiguration clientConfiguration) {
		this.configuration = clientConfiguration;
	}
	
	@Override
	protected void process() {
//		if (!(connection instanceof ClientConnection))
//			throw new IllegalStateException("Client has recieved SessionInitRequestMessage?");
//		FractalsServerMain.dataContainer.newClient(new Client((ClientConnection)connection, configuration));
		configuration.setConnection(getConnection());
		getBackConnection().getNetworkManager().updateClientConfiguration(getSender(), configuration);
		answer(new SessionInitResponseMessage());
	}
	
}
