package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Map;

import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;

public class BreadthFirstSystem extends AbstractCalcSystem {

	public BreadthFirstSystem(ServerManagers managers) {
		super(managers);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addClient(ClientConfiguration newConfiguration, SystemClientData systemClientData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changedClient(ClientConfiguration newConfiguration, ClientConfiguration oldConfiguration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeClient(ClientConfiguration oldConfiguration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeClientMaxThreadCount(int newGranted, int oldGranted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isApplicable(ClientConnection connection, Map<String, ParamSupplier> map) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onInit(Map<String, ParamSupplier> params) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onStart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onStop() {
		// TODO Auto-generated method stub
		return false;
	}

}
