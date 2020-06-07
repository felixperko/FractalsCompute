package de.felixperko.fractals.util.expressions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class ConstantExpression extends AbstractExpression {
	
	String real, imag;
	String name;
	ComplexNumber complexNumber;
	Integer resultIndexReal = -1;
	Integer resultIndexImag = -1;
	int symbolSlot = -1;
	
	public ConstantExpression(String real, String imag) {
		this.real = real;
		this.imag = imag;
	}
	
	public ConstantExpression(double real, double imag) {
		this.real = real+"";
		this.imag = imag+"";
	}

//	@Override
//	public void registerVariableUses(Map<String, ExpressionSymbol> variables, String inputVar) {
//	}
//
//	@Override
//	public void registerConstantUses(LinkedHashMap<String, ExpressionSymbol> valueSymbols, NumberFactory nf) {
//		complexNumber = nf.createComplexNumber(real, imag);
//		String valueParamName = "CONST_EXPR_"+complexNumber.toString();
//		constantSet.add(complexNumber);
//	}
//
//	@Override
//	public void addInstructions(List<GPUInstruction> instructions, Map<String, ExpressionSymbol> variables,
//			Map<ComplexNumber, Integer> constantIndices, String inputVar) {
//		
//		this.resultIndexReal = constantIndices.get(complexNumber);
//		if (this.resultIndexReal == null)
//			throw new IllegalStateException("Didn't find index for constant ("+real+","+imag+")");
//		this.resultIndexImag = resultIndexReal == -1 ? -1 : resultIndexReal+1;
//	}

	@Override
	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		
		complexNumber = numberFactory.createComplexNumber(real, imag);
		name = "CONST_EXPR_"+complexNumber.toString();
		
		ExpressionSymbol varSymbol = expressionBuilder.getValueExpressionSymbol(name);
		expressionBuilder.setExplicitValue(name, complexNumber);
		varSymbol.addOccurence();
		if (copyVariable)
			symbolSlot = varSymbol.getSlot();
	}

	@Override
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getValueExpressionSymbol(name);
		symbol.initCopiesIfNeeded(instructions);
		resultIndexReal = symbol.getIndexReal(symbolSlot);
		resultIndexImag = symbol.getIndexImag(symbolSlot);
		symbol.removeOccurenceAndIsLast();
	}

	@Override
	public boolean hasTempResult() {
		return false;
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
