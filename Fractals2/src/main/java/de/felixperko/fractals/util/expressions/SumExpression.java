package de.felixperko.fractals.util.expressions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.GPUInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

/**
 * e.g.
 * sumExpr0 = expr0 + expr1
 * sumExpr1 = expr0 - expr1
 * sumExpr2 = expr0 + expr1 + expr2
 * sumExpr3 = expr0 + expr1 - expr2
 */
public class SumExpression extends AbstractExpression {
	
	LinkedHashMap<FractalsExpression, Integer> subExpressions;
	List<FractalsExpression> subExpressionList = new ArrayList<>();
	int tempResultIndexReal = 0;
	int tempResultIndexImag = 1;
	int tempResultSlot = -1;
	
	public SumExpression(LinkedHashMap<FractalsExpression, Integer> subExpressions) {
		this.subExpressions = subExpressions;
		for (FractalsExpression expr : subExpressions.keySet()){
			subExpressionList.add(expr);
		}
	}

	@Override
	public void registerSymbolUses(GPUExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		boolean first = true;
		for (FractalsExpression subExpr : subExpressions.keySet()){
			subExpr.registerSymbolUses(expressionBuilder, numberFactory, first);
			if (first)
				first = false;
		}
	}

	@Override
	public void addInstructions(List<GPUInstruction> instructions, GPUExpressionBuilder expressionBuilder) {
		FractalsExpression lastExpr = null;
		boolean first = true;
		for (FractalsExpression subExpr : subExpressions.keySet()){
			subExpr.addInstructions(instructions, expressionBuilder);
			if (first){
				first = false;
				//set result indices
				FractalsExpression reuseResultExpr = subExpr;
				tempResultIndexReal = reuseResultExpr.getResultIndexReal();
				tempResultIndexImag = reuseResultExpr.getResultIndexImag();
			} else {
				instructions.add(new GPUInstruction(subExpressions.get(lastExpr), tempResultIndexReal, tempResultIndexImag, subExpr.getResultIndexReal(), subExpr.getResultIndexImag()));
			}
			lastExpr = subExpr;
		}
	}

	@Override
	public boolean hasTempResult() {
		return true;
	}

	@Override
	public int getResultIndexReal() {
		return tempResultIndexReal;
	}

	@Override
	public int getResultIndexImag() {
		return tempResultIndexImag;
	}
	
}
