package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientConnection;
import de.felixperko.fractals.network.Message;

/**
 * client -> server
 * initiates the session (client is assigned to an instance, domain and view and will receive relevant chunk/state updates)
 */
public class SessionInitRequestMessage extends Message{
	private static final long serialVersionUID = -6879047655133190298L;
	
	ClientConfiguration configuration;
	
	public SessionInitRequestMessage(ClientConfiguration clientConfiguration) {
		this.configuration = clientConfiguration;
	}
	
	@Override
	protected void process() {
		if (!(connection instanceof ClientConnection))
			throw new IllegalStateException("Client has recieved SessionInitRequestMessage?");
		FractalsServerMain.dataContainer.newClient(new Client((ClientConnection)connection, configuration));
		answer(new SessionInitResponseMessage());
	}
	
}
