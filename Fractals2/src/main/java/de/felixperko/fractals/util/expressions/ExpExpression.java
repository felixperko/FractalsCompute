package de.felixperko.fractals.util.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

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
	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		baseExpr.registerSymbolUses(expressionBuilder, numberFactory, true);
		expExpr.registerSymbolUses(expressionBuilder, numberFactory, false);
	}

	@Override
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder) {
		baseExpr.addInstructions(instructions, expressionBuilder);
		expExpr.addInstructions(instructions, expressionBuilder);
		
		resIndexReal = baseExpr.getResultIndexReal();
		resIndexImag = baseExpr.getResultIndexImag();
		
		instructions.add(new GPUInstruction(GPUInstruction.INSTR_POW, resIndexReal, resIndexImag, expExpr.getResultIndexReal(), expExpr.getResultIndexImag()));
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

}
