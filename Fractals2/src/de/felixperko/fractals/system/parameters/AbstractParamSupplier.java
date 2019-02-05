package de.felixperko.fractals.system.parameters;

public abstract class AbstractParamSupplier implements ParamSupplier {
	
	private static final long serialVersionUID = -7127742325514423406L;
	
	String name;
	boolean resetCalculation = false;
	
	public AbstractParamSupplier(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isResetCalculation() {
		boolean reset = resetCalculation;
		resetCalculation = false;
		return reset;
	}

	@Override
	public void setResetCalculation() {
		// TODO Auto-generated method stub

	}

}
