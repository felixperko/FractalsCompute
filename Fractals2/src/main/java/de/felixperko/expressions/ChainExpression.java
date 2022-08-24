package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	
	boolean complexExpression = true;
	boolean singleRealExpression = false;
	boolean singleImagExpression = false;
	
	public ChainExpression(List<FractalsExpression> subExpressions, List<Integer> partLinkInstructions, List<Integer> complexLinkInstructions) {
		int counter = 0;
		for (FractalsExpression expr : subExpressions) {
			if (expr == null)
				throw new IllegalArgumentException("expression "+counter+" in chain expression is null");
			counter++;
		}
		this.subExpressions = subExpressions;
		this.partInstructions = partLinkInstructions;
		this.complexInstructions = complexLinkInstructions;
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		boolean first = true;
		for (FractalsExpression subExpr : subExpressions){
			boolean isModifyingPart = !(subExpr instanceof VariableExpression || subExpr instanceof ConstantExpression);
			boolean isOutputVar = subExpr instanceof VariableExpression && ((VariableExpression)subExpr).name.equals(expressionBuilder.inputVarName);
			boolean copySubVar = (first && copyVariable) || (!first && (isOutputVar || isModifyingPart));
			subExpr.registerSymbolUses(expressionBuilder, numberFactory, copySubVar);
			first = false;
		}
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		for (int i = subExpressions.size()-1 ; i >= 0 ; i--) {
			subExpressions.get(i).addInitInstructions(instructions, expressionBuilder);
		}
//		subExpressions.forEach(expr -> expr.addInitInstructions(instructions, expressionBuilder));
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.subExpressions.forEach(expr -> expr.addEndInstructions(instructions, expressionBuilder));
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		int counter = 0;
		boolean[] addedSubInstructions = new boolean[subExpressions.size()];
		for (FractalsExpression subExpr : subExpressions){
			boolean added = addSubInstructions(instructions, expressionBuilder, subExpr, counter);
			addedSubInstructions[counter] = added;
			if (added){
				registerSubIndices(subExpr, counter);
			}
			counter++;
		}
		counter = 0;
		for (FractalsExpression subExpr : subExpressions){
			if (counter > 0 && addedSubInstructions[counter]){
				linkSubExpression(instructions, subExpr, counter);
			}
			counter++;
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
					complexExpression = false;
				} else {
					tempResultIndexImag = subExpr.getResultIndexReal();//still uses "real" slot since single value
					singleImagExpression = true;
					complexExpression = false;
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
	
	@Override
	public FractalsExpression getFirstChildlessExpression() {
		return subExpressions.get(0).getFirstChildlessExpression();
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

	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
		for (int i = 0 ; i < subExpressions.size() ; i++) {
			FractalsExpression expr = subExpressions.get(i);
			StaticSubExpression staticSubExpression = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames, expr);
			if (staticSubExpression != null)
				subExpressions.set(i, staticSubExpression);
		}
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		for (FractalsExpression expr : subExpressions) {
			if (!expr.isStatic(iterateVarNames))
				return false;
		}
		return true;
	}
	
	@Override
	public FractalsExpression getDerivative(String derivativeVariableName) {
		List<FractalsExpression> derivExpressions = new ArrayList<>();
		for (FractalsExpression expr : subExpressions)
			derivExpressions.add(expr.getDerivative(derivativeVariableName));
		return new ChainExpression(derivExpressions, partInstructions, complexInstructions);
	}

	@Override
	public boolean modifiesFirstVariable() {
		return subExpressions.size() > 1;
	}

	@Override
	public FractalsExpression copy() {
		List<FractalsExpression> subExpressions = new ArrayList<>();
		for (FractalsExpression expr : this.subExpressions)
			subExpressions.add(expr.copy());
		List<Integer> partInstructions = new ArrayList<>(this.partInstructions);
		List<Integer> complexInstructions = new ArrayList<>(this.complexInstructions);
		return new ChainExpression(subExpressions, partInstructions, complexInstructions);
	}
	
	@Override
	public void serialize(StringBuilder sb, boolean pretty) {
		boolean closeBracket = false;
		for (int i = 0 ; i < subExpressions.size() ; i++) {
			
			FractalsExpression subExpr = subExpressions.get(i);
			
			subExpr.serialize(sb, pretty);
			
			if (closeBracket) {
				sb.append(")");
				closeBracket = false;
			}
			
			if (i < complexInstructions.size()) {
				Integer instr = complexInstructions.get(i);
				if (instr == ComputeInstruction.INSTR_ADD_COMPLEX) {
					sb.append(pretty ? " + " : "+");
				}
				else if (instr == ComputeInstruction.INSTR_SUB_COMPLEX) {
					sb.append(pretty ? " - " : "-");
				}
				else if (instr == ComputeInstruction.INSTR_MULT_COMPLEX) {
					sb.append("*");
				}
				else if (instr == ComputeInstruction.INSTR_DIV_COMPLEX) {
					sb.append("/(");
					closeBracket = true;
				}
			}
		}
	}
}
