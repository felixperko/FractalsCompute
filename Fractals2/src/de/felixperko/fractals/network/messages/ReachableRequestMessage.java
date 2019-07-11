package de.felixperko.fractals.network.messages;

import java.util.List;

import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;

public class ReachableRequestMessage extends ServerMessage {

	private static final long serialVersionUID = 7296165361042593042L;
	
	List<DataContainer> sharedDataStateUpdates;
	
	public ReachableRequestMessage(ClientConnection connection, ServerStateInfo serverStateInfo) {
		addSentCallback(new Runnable() {
			@Override
			public void run() {
				sharedDataStateUpdates = serverStateInfo.getSharedDataUpdates(connection);
			}
		});
	}

	@Override
	protected void process() {
		answer(new ReachableResponseMessage());
		if (sharedDataStateUpdates != null && !sharedDataStateUpdates.isEmpty())
			getClientMessageInterface().sharedDataUpdated(sharedDataStateUpdates);
	}

}
