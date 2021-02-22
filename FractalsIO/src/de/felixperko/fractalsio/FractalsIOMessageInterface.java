package de.felixperko.fractalsio;

import java.util.UUID;

import de.felixperko.fractals.manager.client.ClientManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.interfaces.ClientMessageInterface;
import de.felixperko.fractals.network.interfaces.ClientSystemInterface;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class FractalsIOMessageInterface extends ClientMessageInterface {
	
	ClientManagers managers;
	
	public FractalsIOMessageInterface() {
	}
	
	public void setManagers(ClientManagers managers){
		this.managers = managers;
	}

	@Override
	protected ClientSystemInterface createSystemInterface(ClientConfiguration clientConfiguration) {
		if (managers == null)
			throw new IllegalStateException();
		FractalsIOSystemInterface systemInterface = new FractalsIOSystemInterface(managers);
		return systemInterface;
	}
	
	@Override
	public void createdSystem(UUID systemId, ClientConfiguration clientConfiguration, ParamConfiguration parameterConfiguration) {
		if (managers == null)
			throw new IllegalStateException();
		FractalsIOSystemInterface systemInterface = new FractalsIOSystemInterface(managers);
		
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
		systemInterface.setParameters(clientConfiguration.getParamContainer(systemId).getClientParameters());
		addSystemInterface(systemId, systemInterface);
		
		FractalsIO.clientConfiguration = clientConfiguration;
		recievedParameterConfiguration(parameterConfiguration);
	}
	
	public static boolean TEST_FINISH = false;

	@Override
	public void serverStateUpdated(ServerStateInfo serverStateInfo) {
		for (UUID systemId : getRegisteredSystems()) {
			SystemStateInfo ssi = serverStateInfo.getSystemState(systemId);
			if (ssi.getTaskListForState(TaskState.OPEN).size() == 0 && ssi.getTaskListForState(TaskState.ASSIGNED).size() == 0 && ssi.getTaskListForState(TaskState.STARTED).size() == 0 && ssi.getTaskListForState(TaskState.PLANNED).size() == 0 && ssi.getTaskListForState(TaskState.DONE).size() > 0)
				TEST_FINISH = true;
			for (TaskState state : TaskState.values())
				System.out.println(state.name()+": "+ssi.getTaskListForState(state).size());
		}
	}

	
	@Override
	public void recievedParameterConfiguration(ParamConfiguration parameterConfiguration) {
		// TODO Auto-generated method stub
		
	}

}
