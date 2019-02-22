package de.felixperko.fractalsio;

import java.util.UUID;

import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ClientMessageInterface;
import de.felixperko.fractals.network.ClientSystemInterface;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class FractalsIOMessageInterface extends ClientMessageInterface {

	@Override
	protected ClientSystemInterface createSystemInterface(ClientConfiguration clientConfiguration) {
		FractalsIOSystemInterface systemInterface = new FractalsIOSystemInterface();
		return systemInterface;
	}
	
	@Override
	public void createdSystem(UUID systemId, ClientConfiguration clientConfiguration) {
		FractalsIOSystemInterface systemInterface = new FractalsIOSystemInterface();
		
		int chunkSize = clientConfiguration.getParameterGeneralValue(systemId, "chunkSize", Integer.class);
		int width = clientConfiguration.getParameterGeneralValue(systemId, "width", Integer.class);
		int height = clientConfiguration.getParameterGeneralValue(systemId, "height", Integer.class);
		if (width % chunkSize > 0)
			width += chunkSize; //'normalized' ++ because of /= chunkSize
		if (height % chunkSize > 0)
			height += chunkSize;
		width /= chunkSize;
		height /= chunkSize;
		systemInterface.addChunkCount((width)*(height));
		systemInterface.setParameters(clientConfiguration.getSystemClientData(systemId).getClientParameters());
		addSystemInterface(systemId, systemInterface);
		
		FractalsIO.clientConfiguration = clientConfiguration;
	}
	
	public static boolean TEST_FINISH = false;

	@Override
	public void serverStateUpdated(ServerStateInfo serverStateInfo) {
		for (SystemStateInfo ssi : serverStateInfo.getSystemStates()) {
			if (ssi.getTaskListForState(TaskState.ASSIGNED).size() == 0 && ssi.getTaskListForState(TaskState.OPEN).size() == 0)
				TEST_FINISH = true;
			for (TaskState state : TaskState.values())
				System.out.println(state.name()+": "+ssi.getTaskListForState(state).size());
		}
	}

}
