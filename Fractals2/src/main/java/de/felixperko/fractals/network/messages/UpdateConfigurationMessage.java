package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.ClientMessage;

public class UpdateConfigurationMessage extends ClientMessage {

	private static final long serialVersionUID = 1476570289262051108L;
	
	ClientConfiguration configuration;

	public UpdateConfigurationMessage(ClientConfiguration configuration) {
		this.configuration = new ClientConfiguration(configuration, true);
	}
	
	@Override
	protected void process() {
		getBackConnection().getNetworkManager().updateClientConfiguration(getSender(), configuration);
	}

}
