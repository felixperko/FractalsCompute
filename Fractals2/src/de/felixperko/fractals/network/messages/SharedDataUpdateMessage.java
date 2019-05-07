package de.felixperko.fractals.network.messages;

import java.util.List;
import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.network.infra.ServerMessage;

public class SharedDataUpdateMessage extends ServerMessage{
	
	private static final long serialVersionUID = -5502455503264224221L;
	
	List<DataContainer> updates;

	public SharedDataUpdateMessage(List<DataContainer> updates) {
		super();
		this.updates = updates;
	}

	@Override
	protected void process() {
		for (DataContainer container : updates)
			getBackConnection().getNetworkManager().getMessageInterface().updateSharedData(container);
	}
}
