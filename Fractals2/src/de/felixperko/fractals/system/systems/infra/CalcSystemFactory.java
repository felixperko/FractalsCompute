package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.ThreadManager;

public interface CalcSystemFactory {

	CalcSystem createSystem(ThreadManager threadManager);
}
