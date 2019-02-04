package de.felixperko.fractals.network;

import de.felixperko.fractals.system.systems.infra.CalcSystem;

public interface ClientConnection extends Connection {
	public CalcSystem getCurrentSystem();
	public void setCurrentSystem(CalcSystem system);
}
