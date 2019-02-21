package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerThreadManager;

public interface CalcSystemFactory {

	CalcSystem createSystem(Managers managers);
}
