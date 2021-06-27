package de.felixperko.expressions;

import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class VariablePastIterationExpression extends VariableExpression implements FractalsExpression {

	public VariablePastIterationExpression(String name) {
		super(name);
	}
	
	int copySlot = -1;
	
	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		super.registerSymbolUses(expressionBuilder, numberFactory, copyVariable);
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
		copySlot = symbol.getSlot(true, true);
	}
	
	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		super.addInitInstructions(instructions, expressionBuilder);
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
//		ExpressionSymbol tempSymbol = expressionBuilder.getReferenceExpressionSymbol(name+"_temp");
//		resultIndexReal = symbol.getPristineIndexReal();
//		resultIndexImag = symbol.getPristineIndexImag();
		
		String originalVariableName = name.split("_")[0];
		ExpressionSymbol originalSymbol = expressionBuilder.getReferenceExpressionSymbol(originalVariableName);
		int originalReal = originalSymbol.getPristineIndexReal();
		int originalImag = originalSymbol.getPristineIndexImag();
//		int real = tempSymbol.getPristineIndexReal();
//		int imag = tempSymbol.getPristineIndexImag();
		int real = symbol.getIndexReal(copySlot);
		int imag = symbol.getIndexImag(copySlot);
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX, originalReal, originalImag, real, imag));
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
//		ExpressionSymbol tempSymbol = expressionBuilder.getReferenceExpressionSymbol(name+"_temp");
		int tempReal = symbol.getIndexReal(copySlot);
		int tempImag = symbol.getIndexImag(copySlot);
//		int tempReal = tempSymbol.getPristineIndexReal();
//		int tempImag = tempSymbol.getPristineIndexImag();
		int real = symbol.getPristineIndexReal();
		int imag = symbol.getPristineIndexImag();
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX, tempReal, tempImag, real, imag));
	}

}
