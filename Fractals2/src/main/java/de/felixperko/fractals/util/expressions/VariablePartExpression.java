package de.felixperko.fractals.util.expressions;

public class VariablePartExpression extends VariableExpression {
	
	boolean returnReal, returnImag;
	int resultSlot;
	
	public VariablePartExpression(String name, boolean returnReal, boolean returnImag) {
		super(name);
		this.returnReal = returnReal;
		this.returnImag = returnImag;
	}
	
	//TODO
}
