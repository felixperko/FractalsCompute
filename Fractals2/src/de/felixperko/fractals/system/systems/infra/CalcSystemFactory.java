package de.felixperko.fractals.system.systems.infra;

import de.felixperko.fractals.manager.common.Managers;

public interface CalcSystemFactory {

	CalcSystem createSystem(Managers managers);
}
