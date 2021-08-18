package de.felixperko.expressions;

import java.util.List;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

/**
 * Holds another Expression that can be pre-evaluated (per sample) and manages its subexpressions on its own (doesn't propagate addInstructions(),...)
 */
public class StaticSubExpression extends AbstractExpression {

	FractalsExpression containedExpression;
	
	public StaticSubExpression(FractalsExpression containedExpression) {
		this.containedExpression = containedExpression;
	}

	public FractalsExpression getContainedExpression() {
		return containedExpression;
	}

	public void setContainedExpression(FractalsExpression containedExpression) {
		this.containedExpression = containedExpression;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isComplexExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleRealExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleImagExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasTempResult() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getResultIndexReal() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResultIndexImag() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
	}
	
	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return true;
	}

	@Override
	public FractalsExpression getFirstChildlessExpression() {
		// TODO Auto-generated method stub
		return null;
	}

}
