package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.ClientConnection;
import de.felixperko.fractals.network.Message;
import de.felixperko.fractals.network.SenderInfo;

public class ConnectedMessage extends Message {

	private static final long serialVersionUID = -1809347006971064792L;
	
	SenderInfo clientInfo;

	public ConnectedMessage(ClientConnection clientConnection) {
		this.clientInfo = clientConnection.getSenderInfo();
	}

	@Override
	protected void process() {
		FractalsMain.clientStateHolder.stateClientInfo.setValue(clientInfo);
		log.log("Got client info!");
		answer(new ConnectedAckMessage());
	}

}
