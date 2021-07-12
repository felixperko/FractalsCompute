package de.felixperko.expressions;

import java.util.List;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.NumberFactory;

/**
 * e.g.
 * sumExpr0 = expr0 + expr1
 * sumExpr1 = expr0 - expr1
 * sumExpr2 = expr0 + expr1 + expr2
 * sumExpr3 = expr0 + expr1 - expr2
 */
public class ChainExpression extends AbstractExpression {
	
	//<expr, <partInstruction, complexInstruction>
	List<FractalsExpression> subExpressions;
	List<Integer> partInstructions;
	List<Integer> complexInstructions;
	int tempResultIndexReal = -1;
	int tempResultIndexImag = -1;
	int tempResultSlot = -1;
	
	boolean complexExpression = false;
	boolean singleRealExpression = false;
	boolean singleImagExpression = false;
	
	public ChainExpression(List<FractalsExpression> subExpressions, List<Integer> partLinkInstructions, List<Integer> complexLinkInstructions) {
		this.subExpressions = subExpressions;
		this.partInstructions = partLinkInstructions;
		this.complexInstructions = complexLinkInstructions;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		boolean first = true;
		for (FractalsExpression subExpr : subExpressions){
			subExpr.registerSymbolUses(expressionBuilder, numberFactory, !first);
			if (first)
				first = false;
		}
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		subExpressions.forEach(expr -> expr.addInitInstructions(instructions, expressionBuilder));
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.subExpressions.forEach(expr -> expr.addEndInstructions(instructions, expressionBuilder));
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		int i = 0;
		boolean[] addedSubInstructions = new boolean[subExpressions.size()];
		for (FractalsExpression subExpr : subExpressions){
			boolean added = addSubInstructions(instructions, expressionBuilder, subExpr, i);
			addedSubInstructions[i] = added;
			if (added){
				registerSubIndices(subExpr, i);
			}
			i++;
		}
		i = 0;
		for (FractalsExpression subExpr : subExpressions){
			if (i > 0 && addedSubInstructions[i]){
				linkSubExpression(instructions, subExpr, i);
			}
			i++;
		}
		
		if (singleImagExpression){ //still uses "real" slot since single value
//			tempResultIndexReal = tempResultIndexImag;
			tempResultIndexImag = -1;
		}
	}

	protected boolean addSubInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder,
			FractalsExpression subExpr, int i) {
		subExpr.addInstructions(instructions, expressionBuilder);
		return true;
	}

	private void registerSubIndices(FractalsExpression subExpr, int i) {
		if (i == 0){
			//set result indices
			if (subExpr.hasTempResult()){
				if (subExpr.isComplexExpression()){
					complexExpression = true;
					tempResultIndexReal = subExpr.getResultIndexReal();
					tempResultIndexImag = subExpr.getResultIndexImag();
				} else if (subExpr.isSingleRealExpression()){
					tempResultIndexReal = subExpr.getResultIndexReal();
					singleRealExpression = true;
				} else {
					tempResultIndexImag = subExpr.getResultIndexReal();//still uses "real" slot since single value
					singleImagExpression = true;
				}
			}
		} else {
			if (!complexExpression) {
				if (subExpr.hasTempResult()){
					if (tempResultIndexReal == -1 && subExpr.isSingleRealExpression()){
						tempResultIndexReal = subExpr.getResultIndexReal();
						if (singleImagExpression){
							singleImagExpression = false;
							complexExpression = true;
						}
					} else if (tempResultIndexImag == -1 && subExpr.isSingleImagExpression()) {
						tempResultIndexImag = subExpr.getResultIndexReal();
						if (singleRealExpression){
							singleRealExpression = false;
							complexExpression = true;
						}
					}
				}
			}
		}
	}

	protected void linkSubExpression(List<ComputeInstruction> instructions, FractalsExpression subExpr, int i) {
		if (i > 0){
			if (complexExpression){
				if (subExpr.isComplexExpression())
					instructions.add(new ComputeInstruction(complexInstructions.get(i-1), tempResultIndexReal, tempResultIndexImag, subExpr.getResultIndexReal(), subExpr.getResultIndexImag()));
				else if (subExpr.isSingleRealExpression() && subExpr.getResultIndexReal() != tempResultIndexReal)
					instructions.add(new ComputeInstruction(partInstructions.get(i-1), tempResultIndexReal, subExpr.getResultIndexReal(), -1, -1));
				else if (subExpr.isSingleImagExpression() && subExpr.getResultIndexReal() != tempResultIndexImag)
					instructions.add(new ComputeInstruction(partInstructions.get(i-1), tempResultIndexImag, subExpr.getResultIndexReal(), -1, -1));
			} else {
				instructions.add(new ComputeInstruction(partInstructions.get(i-1), tempResultIndexReal, subExpr.getResultIndexReal(), -1, -1));
			}
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
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		double smoothstepConstant = 0;
		for (FractalsExpression expr : subExpressions) {
			double exprSmoothstepConstant = expr.getSmoothstepConstant(expressionBuilder);
			if (exprSmoothstepConstant > smoothstepConstant)
				smoothstepConstant = exprSmoothstepConstant; //TODO overwrite for div!
		}
		return smoothstepConstant;
	}
	
}
