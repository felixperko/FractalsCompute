package de.felixperko.expressions;

import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;

public class VariablePastIterationExpression extends VariableExpression implements FractalsExpression {

	public VariablePastIterationExpression(String name) {
		super(name);
	}
	
	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		super.addInitInstructions(instructions, expressionBuilder);
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
//		resultIndexReal = symbol.getPristineIndexReal();
//		resultIndexImag = symbol.getPristineIndexImag();
		
		String originalVariableName = name.split("_")[0];
		ExpressionSymbol originalSymbol = expressionBuilder.getReferenceExpressionSymbol(originalVariableName);
		int originalReal = originalSymbol.getPristineIndexReal();
		int originalImag = originalSymbol.getPristineIndexImag();
		int real = symbol.getPristineIndexReal();
		int imag = symbol.getPristineIndexImag();
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX, originalReal, originalImag, real, imag));
	}

}
