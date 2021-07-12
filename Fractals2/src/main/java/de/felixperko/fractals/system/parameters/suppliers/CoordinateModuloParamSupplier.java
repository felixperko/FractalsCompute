package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.systems.infra.SystemContext;

/**
 * for patterns like:
 * 1,1 2,1 1,1 2,1
 * 1,2 2,2 1,2 2,2
 * 1,1 2,1 1,1 2,1
 * 1,2 2,2 1,2 2,2
 */
public class CoordinateModuloParamSupplier extends MappedParamSupplier {

	private static final long serialVersionUID = -7923917428809913571L;
	
	Number modulo;

	public CoordinateModuloParamSupplier(String name) {
		super(name);
	}
	
	public CoordinateModuloParamSupplier(String name, Number modulo) {
		super(name);
		this.modulo = modulo;
	}

	@Override
	public ParamSupplier copy() {
		return new CoordinateDiscreteModuloParamSupplier(name);
	}

	@Override
	public boolean evaluateChanged(ParamSupplier old) {
		return this.equals(old);
	}
	
	@Override
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modulo == null) ? 0 : modulo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoordinateDiscreteModuloParamSupplier other = (CoordinateDiscreteModuloParamSupplier) obj;
		if (modulo == null) {
			if (other.modulo != null)
				return false;
		} else if (!modulo.equals(other.modulo))
			return false;
		return true;
	}

	public Number getModulo() {
		return modulo;
	}

	public void setModulo(Number modulo) {
		this.modulo = modulo;
	}

}
