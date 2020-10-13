package de.felixperko.fractals.util.expressions;

import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class VariablePartExpression extends VariableExpression {
	
	boolean returnReal, returnImag;
	int zeroSlot;
	int sourceIndex;
	
	
	public VariablePartExpression(String name, boolean returnReal, boolean returnImag) {
		super(name);
		if (returnReal && returnImag)
			throw new IllegalArgumentException("Tried to create a VariablePartExpression with returnReal and returnImag both true. Use VariableExpression for complex variables.");
		this.returnReal = returnReal;
		this.returnImag = returnImag;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable) {
		ExpressionSymbol varSymbol = expressionBuilder.getReferenceExpressionSymbol(name);
		addVarOccurence(varSymbol);
		if (copyVariable)
			symbolSlot = varSymbol.getSlot(returnReal, returnImag);
	}
	
	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		
		super.addInitInstructions(instructions, expressionBuilder);

		ExpressionSymbol valueSymbol = expressionBuilder.getReferenceExpressionSymbol(name);
		if (returnImag){
			resultIndexReal = resultIndexImag;
		}
//		ExpressionSymbol zeroSymbol = expressionBuilder.getReferenceExpressionSymbol("zero");
//		sourceIndexReal = returnReal ? valueSymbol.getPristineIndexReal() : zeroSymbol.getPristineIndexReal();
//		sourceIndexImag = returnImag ? valueSymbol.getPristineIndexImag() : zeroSymbol.getPristineIndexImag();
//		zeroSymbol.removeOccurenceAndIsLast();
		
//		sourceIndexReal = returnReal ? valueSymbol.getIndexReal(super.symbolSlot) : zeroSymbol.getPristineIndexReal();
//		sourceIndexImag = returnImag ? valueSymbol.getIndexImag(super.symbolSlot) : zeroSymbol.getPristineIndexImag();
		
		sourceIndex = returnReal ? valueSymbol.getIndexReal(super.symbolSlot) : (returnImag ? valueSymbol.getIndexImag(super.symbolSlot) : -1);
	}
	
	@Override
	protected void addVarOccurence(ExpressionSymbol varSymbol) {
		if (returnReal)
			varSymbol.addOccurenceReal();
		if (returnImag)
			varSymbol.addOccurenceImag();
	}
	
	@Override
	protected boolean removeVarOccurenceAndIsLast(ExpressionSymbol varSymbol) {
		if (returnReal)
			return varSymbol.removeOccurenceRealAndIsLast();
		if (returnImag)
			return varSymbol.removeOccurenceImagAndIsLast();
		throw new IllegalArgumentException("Unexpected VariablePartExpression.removeVarOccurenceAndIsLast() branch");
	}

	protected void initCopy(List<ComputeInstruction> instructions, ExpressionSymbol symbol){
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_PART, sourceIndex, resultIndexReal, -1, -1));
	}
	
	@Override
	public boolean isComplexExpression() {
		return false;
	}

	@Override
	public boolean isSingleRealExpression() {
		return true;
	}

	@Override
	public boolean isSingleImagExpression() {
		return false;
	}
	
	@Override
	public int getResultIndexImag() {
		return -1;
	}
}
