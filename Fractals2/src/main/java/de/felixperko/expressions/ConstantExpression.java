package de.felixperko.expressions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
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
	
	public ConstantExpression(double real, double imag, String name) {
		this.real = real+"";
		this.imag = imag+"";
		this.name = name;
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
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		
		complexNumber = numberFactory.createComplexNumber(real, imag);
		if (name == null)
			name = "CON_"+complexNumber.toString();
		
		ExpressionSymbol varSymbol = expressionBuilder.getValueExpressionSymbol(name);
		expressionBuilder.setExplicitValue(name, complexNumber);
		if (copyVariable) {
			varSymbol.addOccurence();
			symbolSlot = varSymbol.getSlot(true, true);
		}
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getValueExpressionSymbol(name);
		resultIndexReal = symbol.getIndexReal(symbolSlot);
		resultIndexImag = symbol.getIndexImag(symbolSlot);
		//not using pristine slot -> init copy
		if (symbol.getPristineIndexReal() != resultIndexReal || symbol.getPristineIndexImag() != resultIndexImag){
			initCopy(instructions, symbol);
		}
	}
	
	@Override
	public FractalsExpression getFirstChildlessExpression() {
		return this;
	}
	
	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		
	}

	protected void initCopy(List<ComputeInstruction> instructions, ExpressionSymbol symbol){
		instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX, symbol.getPristineIndexReal(), symbol.getPristineIndexImag(), resultIndexReal, resultIndexImag));
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		ExpressionSymbol symbol = expressionBuilder.getValueExpressionSymbol(name);
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
//		double r = Double.parseDouble(real);
//		double i = Double.parseDouble(imag);
//		double mod = Math.sqrt(r*r+i*i);
//		return mod;
//		return mod == 0 ? 0 : Math.log(mod);
		return 0;
	}

	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return true;
	}

	@Override
	public FractalsExpression getDerivative(String derivativeVariableName) {
		return new ConstantExpression(0, 0);
	}

	@Override
	public boolean modifiesFirstVariable() {
		return false;
	}

	@Override
	public FractalsExpression copy() {
		return new ConstantExpression(real, imag);
	}

	@Override
	public void serialize(StringBuilder sb, boolean pretty) {
		boolean complex = imag != null && Double.parseDouble(imag) != 0.0;
		if (complex) {
			sb.append("(");
		}
		
		double r = Double.parseDouble(real);
		if (r == (double)((int)r))
			sb.append((int)r);
		else
			sb.append(real);
		
		if (complex){
			sb.append(imag).append("*i").append(")");
		}
	}

}
