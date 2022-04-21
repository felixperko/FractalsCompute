package de.felixperko.expressions;

import java.util.ArrayList;
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
				//expr in form of ...*i -> skip, treat as complex expression, aggregate real result in imag slot
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
		if (!imagResult) {
			boolean optimized = false;
			if (subExpr instanceof ConstantExpression) {
				ConstantExpression expr = (ConstantExpression)subExpr;
				if (expr.complexNumber.realDouble() == 2. && expr.complexNumber.imagDouble() == 0. && complexInstructions.get(i-1) == ComputeInstruction.INSTR_MULT_COMPLEX){
					instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_ADD_COMPLEX, tempResultIndexReal, tempResultIndexImag, expr.getResultIndexReal(), expr.getResultIndexImag()));
					optimized = true;
				}
			}
			if (!optimized)
				super.linkSubExpression(instructions, subExpr, i);
		}
		else {
			//expr in form of ...*i -> skip, treat as complex expression, aggregate real result in imag slot
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
	
	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		double sum = 0;
		
		List<Integer> instrList = isComplexExpression() ? complexInstructions : partInstructions;
		boolean[] divLinks = new boolean[instrList.size()];
		for (int i = 0 ; i < divLinks.length ; i++) {
			Integer instr = instrList.get(i);
			divLinks[i] = instr == ComputeInstruction.INSTR_DIV_COMPLEX || instr == ComputeInstruction.INSTR_DIV_PART;
		}
		
		int i = 0;
		for (FractalsExpression subExpr : subExpressions) {
			if (i > 0 && divLinks[i-1])
				sum -= subExpr.getSmoothstepConstant(expressionBuilder);
			else
				sum += subExpr.getSmoothstepConstant(expressionBuilder);
			i++;
		}
		
		return sum;
	}
	
	@Override
	public FractalsExpression getDerivative(String derivativeVariableName) {
		List<FractalsExpression> derivExpressions = new ArrayList<>();
		boolean allStatic = true;
		for (FractalsExpression expr : subExpressions) {
			if (expr instanceof ConstantExpression || expr instanceof StaticSubExpression || ((expr instanceof VariableExpression) && !((VariableExpression)expr).getVariableName().equals(derivativeVariableName))) {
				derivExpressions.add(expr.copy());
			}
			else {
				allStatic = false;
				derivExpressions.add(expr.getDerivative(derivativeVariableName));
			}
		}
		if (allStatic)
			return new ConstantExpression(0, 0);
		return new ChainExpression(derivExpressions, partInstructions, complexInstructions);
	}

}
