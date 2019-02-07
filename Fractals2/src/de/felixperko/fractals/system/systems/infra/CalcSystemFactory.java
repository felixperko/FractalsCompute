package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ThreadManager;

public interface CalcSystemFactory {

	CalcSystem createSystem(Managers managers);
}
