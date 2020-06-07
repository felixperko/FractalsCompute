package de.felixperko.fractals.util.expressions;

import java.util.List;
import java.util.Map;
import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class VariableExpression extends AbstractExpression {
	
	String name;
	int resultIndexReal = -1;
	int resultIndexImag = -1;
	int symbolSlot = -1;
	
	public VariableExpression(String name){
		this.name = name;
	}

	@Override
	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable) {
		ExpressionSymbol varSymbol = expressionBuilder.getReferenceExpressionSymbol(name);
		varSymbol.addOccurence();
		if (copyVariable)
			symbolSlot = varSymbol.getSlot();
	}

	@Override
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
		resultIndexReal = symbol.getIndexReal(symbolSlot);
		resultIndexImag = symbol.getIndexImag(symbolSlot);
		symbol.removeOccurenceAndIsLast();
	}

	@Override
	public boolean hasTempResult() {
		return true;
	}

	@Override
	public int getResultIndexReal() {
		return resultIndexReal;
	}

	@Override
	public int getResultIndexImag() {
		return resultIndexImag;
	}

}
