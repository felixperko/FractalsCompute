package de.felixperko.expressions;

import java.util.List;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public interface FractalsExpression {

	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable);
	
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder);
	
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames);
	public boolean isStatic(Set<String> iterateVarNames);
	
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder);
	
	public boolean isComplexExpression();
	public boolean isSingleRealExpression();
	public boolean isSingleImagExpression();
	
	public boolean hasTempResult();
	
	public int getResultIndexReal();
	public int getResultIndexImag();

	public FractalsExpression getFirstChildlessExpression();

	
//	FractalsExpression getDerivative(String derivativeVariableName);
//	FractalsExpression simplify();
}
