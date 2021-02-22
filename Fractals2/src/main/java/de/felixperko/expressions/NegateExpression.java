package de.felixperko.expressions;

import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class NegateExpression implements FractalsExpression {
	
	FractalsExpression subExpr;
	
	int resultIndexReal, resultIndexImag;

	public NegateExpression(FractalsExpression subExpr) {
		this.subExpr = subExpr;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		this.subExpr.registerSymbolUses(expressionBuilder, numberFactory, true);
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.subExpr.addInitInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.subExpr.addInstructions(instructions, expressionBuilder);
		if (subExpr.isComplexExpression()){
			instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_NEGATE_COMPLEX, subExpr.getResultIndexReal(), subExpr.getResultIndexImag(), -1, -1));
		} else {
			instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_NEGATE_PART, subExpr.getResultIndexReal(), -1, -1, -1));
		}
	}

	@Override
	public boolean isComplexExpression() {
		return subExpr == null || subExpr.isComplexExpression();
	}

	@Override
	public boolean isSingleRealExpression() {
		return subExpr != null && !subExpr.isSingleRealExpression();
	}

	@Override
	public boolean isSingleImagExpression() {
		return subExpr != null && !subExpr.isSingleImagExpression();
	}

	@Override
	public boolean hasTempResult() {
		return subExpr != null && subExpr.hasTempResult();
	}

	@Override
	public int getResultIndexReal() {
		return resultIndexReal;
	}

	@Override
	public int getResultIndexImag() {
		return resultIndexImag;
	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		return subExpr.getSmoothstepConstant(expressionBuilder);
	}

}
