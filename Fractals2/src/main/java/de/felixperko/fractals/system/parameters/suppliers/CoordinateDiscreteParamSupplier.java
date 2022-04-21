package de.felixperko.fractals.system.parameters.suppliers;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.systems.infra.SystemContext;

/**
 * For patterns like:
 * 1,1 1,1 2,1 2,1
 * 1,1 1,1 2,1 2,1
 * 1,2 1,2 2,2 2,2
 * 1,2 1,2 2,2 2,2
 */
public class CoordinateDiscreteParamSupplier extends MappedParamSupplier{
	
	private static final long serialVersionUID = -7923917428809913571L;
	
	Number stepSize;
	Number modulo;
	ComplexNumber offset;

	public CoordinateDiscreteParamSupplier(String name) {
		super(name);
	}
	
	public CoordinateDiscreteParamSupplier(String name, Number modulo, Number stepSize, ComplexNumber offset) {
		super(name);
		this.modulo = modulo;
		this.stepSize = stepSize;
		this.offset = offset;
	}

	@Override
	public ParamSupplier copy() {
		return new CoordinateDiscreteParamSupplier(name);
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
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((stepSize == null) ? 0 : stepSize.hashCode());
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
		CoordinateDiscreteParamSupplier other = (CoordinateDiscreteParamSupplier) obj;
		if (modulo == null) {
			if (other.modulo != null)
				return false;
		} else if (!modulo.equals(other.modulo))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (stepSize == null) {
			if (other.stepSize != null)
				return false;
		} else if (!stepSize.equals(other.stepSize))
			return false;
		return true;
	}

	public Number getStepSize() {
		return stepSize;
	}

	public void setStepSize(Number stepSize) {
		this.stepSize = stepSize;
	}

	public Number getModulo() {
		return modulo;
	}

	public void setModulo(Number modulo) {
		this.modulo = modulo;
	}

	public ComplexNumber getOffset() {
		return offset;
	}

	public void setOffset(ComplexNumber offset) {
		this.offset = offset;
	}
	
}
