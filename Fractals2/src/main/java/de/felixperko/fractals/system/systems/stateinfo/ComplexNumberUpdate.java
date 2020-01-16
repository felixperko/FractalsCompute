package de.felixperko.fractals.system.systems.stateinfo;

import java.util.UUID;

import de.felixperko.fractals.system.numbers.ComplexNumber;

public class ComplexNumberUpdate extends AbstractSharedDataUpdate {
	
	ComplexNumber<?, ?> number;
	UUID systemId;
	
	private static final long serialVersionUID = -5754981790681757272L;
	
	public ComplexNumberUpdate() {
		super();
	}
	
	public ComplexNumberUpdate(UUID systemId, ComplexNumber<?, ?> number) {
		super();
		this.systemId = systemId;
		this.number = number;
	}
	
	public void refresh(ComplexNumber<?, ?> number) {
		this.number = number;
	}

	public ComplexNumber<?, ?> getNumber() {
		return number;
	}

	public void setNumber(ComplexNumber<?, ?> number) {
		this.number = number;
	}

	public UUID getSystemId() {
		return systemId;
	}
}
