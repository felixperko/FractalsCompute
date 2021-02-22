package de.felixperko.expressions;

import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;

public class MultExpression extends ChainExpression {
	
	boolean complexResult;
	boolean imagResult = false;

	public MultExpression(List<FractalsExpression> subExpressions, List<Integer> partLinkInstructions, List<Integer> complexLinkInstructions) {
		super(subExpressions, partLinkInstructions, complexLinkInstructions);
	}
	
	@Override
	protected boolean addSubInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder,
			FractalsExpression subExpr, int i) {
		if (subExpr instanceof ConstantExpression){
			ConstantExpression expr = (ConstantExpression) subExpr;
			if (expr.complexNumber.realDouble() == 0.){
				//expr in form of ...*i -> skip, count MultExpression as complex expression, aggregate real result in imag slot
				if (expr.complexNumber.imagDouble() == 1.){
					imagResult = true;
					complexExpression = false;
					singleRealExpression = false;
					singleImagExpression = true;
					return false;
				}
			}
		}
		subExpr.addInstructions(instructions, expressionBuilder);
		return true;
	}
	
	@Override
	protected void linkSubExpression(List<ComputeInstruction> instructions, FractalsExpression subExpr, int i) {
		if (!imagResult)
			super.linkSubExpression(instructions, subExpr, i);
		else {
			instructions.add(new ComputeInstruction(partInstructions.get(i-1), tempResultIndexReal, subExpr.getResultIndexReal(), -1, -1));
		}
	}
	
	@Override
	public boolean isComplexExpression() {
		// TODO Auto-generated method stub
		return super.isComplexExpression();
	}
	
	@Override
	public boolean isSingleRealExpression() {
		return !complexExpression && !imagResult;
	}
	
	@Override
	public boolean isSingleImagExpression() {
		return imagResult;
	}

}
