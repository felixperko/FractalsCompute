package de.felixperko.fractals.system.systems.infra;

import java.util.UUID;

import de.felixperko.fractals.manager.common.Managers;

public interface CalcSystemFactory {

	CalcSystem createSystem(UUID systemId, Managers managers);
}
