package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.SenderInfo;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.network.infra.connection.ClientConnection;

public class ConnectedMessage extends ServerMessage {

	private static final long serialVersionUID = -1809347006971064792L;
	
	SenderInfo clientInfo;
	
	public ConnectedMessage(ClientConnection clientConnection) {
		this.clientInfo = clientConnection.getSenderInfo();
	}

	@Override
	protected void process() {
		getBackConnection().getNetworkManager().setClientInfo(clientInfo);
//		getBackConnection().setSenderInfo(clientInfo);
		log.log("Got client info!");
		answer(new ConnectedAckMessage());
	}

}
