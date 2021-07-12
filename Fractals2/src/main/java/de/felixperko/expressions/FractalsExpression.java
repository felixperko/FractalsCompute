package de.felixperko.expressions;

import java.util.List;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public interface FractalsExpression {

	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable);
	
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder);
	
	public boolean isComplexExpression();
	public boolean isSingleRealExpression();
	public boolean isSingleImagExpression();
	
	public boolean hasTempResult();
	
	public int getResultIndexReal();
	public int getResultIndexImag();
	
//	FractalsExpression getDerivative(String derivativeVariableName);
//	FractalsExpression simplify();
}
