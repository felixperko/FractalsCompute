package de.felixperko.fractals.system.systems.stateinfo;

import de.felixperko.fractals.data.shareddata.SharedStateUpdate;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;

public class ComplexNumberUpdate extends AbstractSharedDataUpdate implements SharedStateUpdate {
	
	ComplexNumber number;
	
	private static final long serialVersionUID = -5754981790681757272L;
	
	public ComplexNumberUpdate(ComplexNumber number) {
		super();
		this.number = number;
	}
	
	public void refresh(ComplexNumber number) {
		this.number = number;
	}
}
