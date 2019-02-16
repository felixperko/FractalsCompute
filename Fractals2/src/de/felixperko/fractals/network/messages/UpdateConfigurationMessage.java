package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.ClientMessage;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.SystemClientMessage;

public class UpdateConfigurationMessage extends ClientMessage {

	private static final long serialVersionUID = 1476570289262051108L;
	
	ClientConfiguration configuration;

	public UpdateConfigurationMessage(ClientConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	protected void process() {
		//log.log("updating configuration: view:"+configuration.isUpdate_view()+" domain:"+configuration.isUpdate_domain()+" instance:"+configuration.isUpdate_instance());
		getConnection().getNetworkManager().updateClientConfiguration(getSender(), configuration);
		//FractalsServerMain.dataContainer.getClient(connection.getSenderInfo().getClientId()).configurationUpdated(configuration);
	}

}
