package de.felixperko.fractals.util.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

/**
 * eg.
 * (expr)
 * sin(expr)
 * abs(expr)
 * conj(expr)
 */
public class NestedExpression extends AbstractExpression {

	protected FractalsExpression contentExpression;
	int instruction;

	public NestedExpression(FractalsExpression contentExpression, int instruction) {
		this.contentExpression = contentExpression;
		this.instruction = instruction;
	}

	@Override
	public boolean hasTempResult() {
		return contentExpression.hasTempResult();
	}

	@Override
	public int getResultIndexReal() {
		return contentExpression.getResultIndexReal();
	}

	@Override
	public int getResultIndexImag() {
		return contentExpression.getResultIndexImag();
	}

	@Override
	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		contentExpression.registerSymbolUses(expressionBuilder, numberFactory, copyVariable);
	}

	@Override
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder) {
		contentExpression.addInstructions(instructions, expressionBuilder);
		 if (instruction >= 0)
			 instructions.add(new GPUInstruction(instruction, contentExpression.getResultIndexReal(), contentExpression.getResultIndexImag(), contentExpression.getResultIndexReal(), contentExpression.getResultIndexImag()));
	}

}