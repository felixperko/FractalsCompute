package de.felixperko.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

/**
 * e.g.
 * 
 * x^2
 * x^(expr)
 * (expr)^(expr) 
 * 
 */
public class ExpExpression extends AbstractExpression {

	static int counter = 0;
	
	FractalsExpression baseExpr;
	FractalsExpression expExpr;
	
	//temp results:
	//b^e
	//f f - register new temp result
	//f t - reverse indices
	//t f - normal power
	//t t - free expExpr temp result
	
//	boolean baseTempResult;
//	boolean expTempResult;
//	boolean reverseIndices;
	int tempSlot;
	int resIndexReal;
	int resIndexImag;
	
	boolean complexExpression = true;
	boolean singleRealExpression = false;
	boolean singleImagExpression = false;
	
	public ExpExpression(FractalsExpression base, FractalsExpression exponent) {
		this.baseExpr = base;
		this.expExpr = exponent;
		
//		this.baseTempResult = baseExpr.hasTempResult();
//		this.expTempResult = expExpr.hasTempResult();
//		this.reverseIndices = expTempResult && !baseTempResult;
	}

//	@Override
//	public void registerVariableUses(Map<String, ExpressionSymbol> variables, boolean copyVariable) {
//		baseExpr.registerVariableUses(variables, true);
//		expExpr.registerVariableUses(variables, false);
//		
////		if (!baseTempResult && !expTempResult){
////			//register new temp result
//////			tempVarName = tempVarPrefix+counter++;
////			ExpressionSymbol zVar = variables.get(inputVar);
////			zVar.addOccurence();
////			tempSlot = zVar.getSlot();
////		}
//	}
//
//	@Override
//	public void registerConstantUses(Set<ComplexNumber> constantSet, NumberFactory nf) {
//		baseExpr.registerConstantUses(constantSet, nf);
//		expExpr.registerConstantUses(constantSet, nf);
//	}
//
//	@Override
//	public void addInstructions(List<GPUInstruction> instructions, Map<String, ExpressionSymbol> variables,
//			Map<ComplexNumber, Integer> constantIndices, String inputVar) {
//		
//		baseExpr.addInstructions(instructions, variables, constantIndices, inputVar);
//		expExpr.addInstructions(instructions, variables, constantIndices, inputVar);
//		
////		if (!reverseIndices){
////			if (baseExpr.hasTempResult()){
//				resIndexReal = baseExpr.getTempResultIndexReal();
//				resIndexImag = baseExpr.getTempResultIndexImag();
////			} else {
////				ExpressionSymbol tempVar = variables.get(inputVar);
////				boolean last = tempVar.removeOccurenceAndIsLast();
////				if (last){ //use var directly if pristine value isn't used anymore
////					resIndexReal = tempVar.getPristineIndexReal();
////					resIndexImag = tempVar.getPristineIndexImag();
////				} else {
////					resIndexReal = tempVar.getCopyIndexReal(tempSlot);
////					resIndexImag = tempVar.getCopyIndexImag(tempSlot);
////				}
////			}
////		} else { //take temp result from exp
////			resIndexReal = expExpr.getTempResultIndexReal();
////			resIndexImag = expExpr.getTempResultIndexImag();
////			//TODO not complete yet!!!
////		}
//		
//		instructions.add(new GPUInstruction(GPUInstruction.INSTR_POW, resIndexReal, resIndexImag, expExpr.getResultIndexReal(), expExpr.getResultIndexImag()));
//	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		baseExpr.registerSymbolUses(expressionBuilder, numberFactory, true);
		expExpr.registerSymbolUses(expressionBuilder, numberFactory, false);
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		baseExpr.addInitInstructions(instructions, expressionBuilder);
		expExpr.addInitInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.baseExpr.addEndInstructions(instructions, expressionBuilder);
		this.expExpr.addEndInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		baseExpr.addInstructions(instructions, expressionBuilder);
		expExpr.addInstructions(instructions, expressionBuilder);
		
		complexExpression = baseExpr.isComplexExpression();
		if (expExpr.isComplexExpression() != complexExpression)
			throw new IllegalArgumentException("Can't combine part and complex expressions (yet)");
		if (baseExpr.isSingleRealExpression() != expExpr.isSingleRealExpression())
			throw new IllegalArgumentException("Can't combine part and complex expressions (yet)");
		this.singleRealExpression = baseExpr.isSingleRealExpression();
		this.singleImagExpression = baseExpr.isSingleImagExpression();
		
		resIndexReal = baseExpr.getResultIndexReal();
		resIndexImag = baseExpr.getResultIndexImag();
		
		//Optimize?
		ComplexNumber value = null;
		String expParamName = null;
		
		while (expExpr instanceof NestedExpression)
			expExpr = ((NestedExpression)expExpr).contentExpression;
		
		if (expExpr instanceof ConstantExpression){
			value = ((ConstantExpression)expExpr).complexNumber;
			expParamName = ((ConstantExpression)expExpr).name;
		}
		else if (expExpr instanceof VariableExpression){
			String variableName = ((VariableExpression)expExpr).name;
			ParamSupplier supp = expressionBuilder.parameters.get(variableName);
			if (supp instanceof StaticParamSupplier){
				Object obj = ((StaticParamSupplier) supp).getObj();
				if (obj instanceof ComplexNumber){
					value = (ComplexNumber) obj;
					expParamName = variableName;
				}
			}
		}
		
		boolean optimized = false;
		if (value != null){ //optimize
			if (value.imagDouble() == 0.){
				double real = value.realDouble();
				if (real == 2.){
					addSquareInstruction(instructions);
					optimized = true;
				}
				else if (real == 4.){
					addSquareInstruction(instructions);
					addSquareInstruction(instructions);
					optimized = true;
				}
				else if (real == -2.){
					// z^(-2) = (1/z)^2
					// 1/z = a - bi / (a^2 * b^2)
					// z = a - bi
					if (complexExpression)
						instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_RECIPROCAL_COMPLEX, resIndexReal, resIndexImag, -1, -1));
					else
						instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_RECIPROCAL_PART, resIndexReal, -1, -1, -1));
					addSquareInstruction(instructions);
					optimized = true;
				}
		}
		}
		if (optimized)
			expressionBuilder.fixedValues.put(expParamName, value);
		else { //default case
			if (complexExpression)
				instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_POW_COMPLEX, resIndexReal, resIndexImag, expExpr.getResultIndexReal(), expExpr.getResultIndexImag()));
			else
				instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_POW_PART, resIndexReal, expExpr.getResultIndexReal(), -1, -1));
		}
	}

	private void addSquareInstruction(List<ComputeInstruction> instructions) {
		addInstruction(instructions, ComputeInstruction.INSTR_SQUARE_COMPLEX, ComputeInstruction.INSTR_SQUARE_PART);
	}
	
	private void addInstruction(List<ComputeInstruction> instructions, int complexInstruction, int partInstruction) {
		if (complexExpression)
			instructions.add(new ComputeInstruction(complexInstruction, resIndexReal, resIndexImag, -1, -1));
		else
			instructions.add(new ComputeInstruction(partInstruction, resIndexReal, -1, -1, -1));
	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		ParamSupplier supplier = null;
		if (expExpr instanceof VariableExpression){
			supplier = expressionBuilder.parameters.get(((VariableExpression)expExpr).name);
		}
		if (expExpr instanceof ConstantExpression){
			return ((ConstantExpression)expExpr).complexNumber.absDouble();
		}
		
		if (supplier == null)
			return 0;
		if (supplier instanceof MappedParamSupplier)
			return 2; //TODO smoothstep for mapped power possible?
		if (supplier instanceof StaticParamSupplier){
			StaticParamSupplier supp = ((StaticParamSupplier)supplier);
			Object obj = supp.getGeneral();
			if (obj instanceof ComplexNumber){
				double r = ((ComplexNumber)obj).realDouble();
				double i = ((ComplexNumber)obj).imagDouble();
				return Math.sqrt(r*r + i*i);
			}
		}
		return 0;
	}

	@Override
	public boolean hasTempResult() {
		return true;
	}

	@Override
	public int getResultIndexReal() {
		return resIndexReal;
	}

	@Override
	public int getResultIndexImag() {
		return resIndexImag;
	}

	@Override
	public boolean isComplexExpression() {
		return complexExpression;
	}

	@Override
	public boolean isSingleRealExpression() {
		return singleRealExpression;
	}

	@Override
	public boolean isSingleImagExpression() {
		return singleImagExpression;
	}

	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
		StaticSubExpression staticBase = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames, baseExpr);
		StaticSubExpression staticExp = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames, expExpr);
		if (staticBase != null)
			baseExpr = staticBase;
		if (staticExp != null)
			expExpr = staticExp;
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return baseExpr.isStatic(iterateVarNames) && expExpr.isStatic(iterateVarNames);
	}
	

}
