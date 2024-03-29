package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.SystemServerMessage;
import de.felixperko.fractals.system.parameters.ParamConfiguration;

public class SystemConnectedMessage extends SystemServerMessage {

	private static final long serialVersionUID = 1578093945175083508L;
	
	ClientConfiguration clientConfiguration;
	ParamConfiguration parameterConfiguration;
	
	public SystemConnectedMessage(UUID systemId, ClientConfiguration clientConfiguration, ParamConfiguration parameterConfiguration) {
		super(systemId);
		this.clientConfiguration = new ClientConfiguration(clientConfiguration, true);
		this.parameterConfiguration = parameterConfiguration;
	}

	@Override
	protected void process() {
		getClientMessageInterface().createdSystem(systemId, clientConfiguration, parameterConfiguration);
	}

}
