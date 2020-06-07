package de.felixperko.fractals.util.expressions;

import java.util.List;
import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public interface FractalsExpression {

	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable);
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder);
	
	public boolean hasTempResult();
	
	public int getResultIndexReal();
	public int getResultIndexImag();
}
