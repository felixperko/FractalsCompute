package de.felixperko.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

public class VariableExpression extends AbstractExpression {
	
	String name;
	int resultIndexReal = -1;
	int resultIndexImag = -1;
	int symbolSlot = -1;
	
	public VariableExpression(String name){
		if (name.endsWith("_n"))
			name = name.substring(0, name.length()-2);
		else if (name.endsWith("_(n)"))
			name = name.substring(0, name.length()-4);
		this.name = name;
	}
	
	public String getVariableName() {
		return name;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory, boolean copyVariable) {
		ExpressionSymbol varSymbol = expressionBuilder.getReferenceExpressionSymbol(name);
		addVarOccurence(varSymbol);
		if (copyVariable)
			symbolSlot = varSymbol.getSlot(true, true);
	}
	
	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
		resultIndexReal = symbol.getIndexReal(symbolSlot);
		resultIndexImag = symbol.getIndexImag(symbolSlot);
		boolean last = removeVarOccurenceAndIsLast(symbol);
		if (last) {
			resultIndexReal = symbol.getPristineIndexReal();
			resultIndexImag = symbol.getPristineIndexImag();
		}
		//not using pristine slot -> init copy
		if ((symbol.getPristineIndexReal() != resultIndexReal && resultIndexReal >= 0) || (symbol.getPristineIndexImag() != resultIndexImag && resultIndexImag >= 0)){
			initCopy(instructions, symbol);
		}
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
	}

	protected void initCopy(List<ComputeInstruction> instructions, ExpressionSymbol symbol){
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX, symbol.getPristineIndexReal(), symbol.getPristineIndexImag(), resultIndexReal, resultIndexImag));
	}

	protected void addVarOccurence(ExpressionSymbol varSymbol) {
		varSymbol.addOccurence();
	}

	protected boolean removeVarOccurenceAndIsLast(ExpressionSymbol symbol) {
		return symbol.removeOccurenceAndIsLast();
	}
	
	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(name);
		removeVarOccurenceAndIsLast(symbol);
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
	
	@Override
	public boolean isComplexExpression() {
		return true;
	}

	@Override
	public boolean isSingleRealExpression() {
		return false;
	}

	@Override
	public boolean isSingleImagExpression() {
		return false;
	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		return 1;
	}

	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return !iterateVarNames.contains(name);
	}

}
