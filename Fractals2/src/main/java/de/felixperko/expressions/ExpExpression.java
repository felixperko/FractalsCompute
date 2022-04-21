package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.Arrays;
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
 * x^2 x^(expr) (expr)^(expr)
 * 
 */
public class ExpExpression extends AbstractExpression {

//	static int counter = 0;

	FractalsExpression baseExpr;
	FractalsExpression expExpr;

	int maxOptimizedPow = 32;
	int minOptimizedPow = -32;
	int tempVarSlotOptimization = -1;
	String tempOptimizationSymbolName = "temp_exp";

	// temp results:
	// b^e
	// f f - register new temp result
	// f t - reverse indices
	// t f - normal power
	// t t - free expExpr temp result

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
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		baseExpr.registerSymbolUses(expressionBuilder, numberFactory, copyVariable);
//		FractalsExpression firstExpExpr = expExpr.getFirstChildlessExpression();
//		boolean copyExpVar = !(firstExpExpr instanceof VariableExpression && ((VariableExpression)firstExpExpr).getVariableName().equals(expressionBuilder.inputVarName));
		boolean copyExpVar = expExpr.modifiesFirstVariable();
		expExpr.registerSymbolUses(expressionBuilder, numberFactory, copyExpVar);
		if (needsTempOptimizationVar()) {
			ExpressionSymbol symbol = expressionBuilder.getReferenceExpressionSymbol(tempOptimizationSymbolName);
			symbol.setVisible(false);
			symbol.addOccurence();
			tempVarSlotOptimization = symbol.getSlot(true, isComplexExpression());
		}
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		expExpr.addInitInstructions(instructions, expressionBuilder);
		baseExpr.addInitInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		this.baseExpr.addEndInstructions(instructions, expressionBuilder);
		this.expExpr.addEndInstructions(instructions, expressionBuilder);
	}

	protected boolean needsTempOptimizationVar() {
		FractalsExpression expExpr = this.expExpr;
		while (expExpr instanceof NestedExpression)
			expExpr = ((NestedExpression) expExpr).contentExpression;
		ComplexNumber value;
		if (expExpr instanceof ConstantExpression) {
			value = ((ConstantExpression) expExpr).complexNumber;
		} else {
			return false;
		}
		double r = value.realDouble();
		if (value.imagDouble() != 0.0 || r == 0.0 || r != (int) r || r < minOptimizedPow || r > maxOptimizedPow)
			return false;
		boolean isPower2 = (Math.log(r) / Math.log(2)) % 1.0 == 0.0;
		return !isPower2;
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		baseExpr.addInstructions(instructions, expressionBuilder);
		expExpr.addInstructions(instructions, expressionBuilder);

		complexExpression = baseExpr.isComplexExpression();
		if (expExpr.isComplexExpression() != complexExpression)
			throw new IllegalArgumentException("Can't combine real and complex expressions (yet)");
		if (baseExpr.isSingleRealExpression() != expExpr.isSingleRealExpression())
			throw new IllegalArgumentException("Can't combine real and imag expressions (yet)");
		this.singleRealExpression = baseExpr.isSingleRealExpression();
		this.singleImagExpression = baseExpr.isSingleImagExpression();

		resIndexReal = baseExpr.getResultIndexReal();
		resIndexImag = baseExpr.getResultIndexImag();

		// Optimize?
		ComplexNumber value = null;
		String expParamName = null;

		while (expExpr instanceof NestedExpression)
			expExpr = ((NestedExpression) expExpr).contentExpression;

		if (expExpr instanceof ConstantExpression) {
			value = ((ConstantExpression) expExpr).complexNumber;
			expParamName = ((ConstantExpression) expExpr).name;
		} else if (expExpr instanceof VariableExpression) {
			String variableName = ((VariableExpression) expExpr).name;
			ParamSupplier supp = expressionBuilder.parameters.get(variableName);
			if (supp instanceof StaticParamSupplier) {
				Object obj = ((StaticParamSupplier) supp).getObj();
				if (obj instanceof ComplexNumber) {
					value = (ComplexNumber) obj;
					expParamName = variableName;
				}
			}
		}

		boolean optimized = false;
		if (value != null) { // optimize
			if (value.imagDouble() == 0.) {
				double real = value.realDouble();
				if (real == 0.) {
					// TODO set 1?
				} else {
					if (real == (int) real && real <= maxOptimizedPow && real >= minOptimizedPow && real != 0.0) {
						if (real < 0) {
							addReciprInstruction(instructions);
							real = -real;
						}

						int lastIndex = -1;
						int remainingPow = (int) real;
						int maxStep = (int) Math.floor(Math.log(real) / Math.log(2));
						int currPow = (int) Math.pow(2, maxStep);
						boolean[] copyStep = new boolean[maxStep + 1];
						for (int i = 0; i <= maxStep; i++) {
							if (remainingPow >= currPow) {
								remainingPow -= currPow;
								copyStep[maxStep - i] = true;
								if (lastIndex == -1)
									lastIndex = maxStep - i;
								if (remainingPow == 0)
									break;
							}
							currPow /= 2;
						}

						boolean copyCreated = false;
						ExpressionSymbol symbol = expressionBuilder
								.getReferenceExpressionSymbol(tempOptimizationSymbolName);
						int copyIndex = tempVarSlotOptimization == -1 ? -1
								: symbol.getCopyIndexReal(tempVarSlotOptimization);
						if (symbol.removeOccurenceAndIsLast())
							copyIndex = symbol.getPristineIndexReal();
						for (int i = 0; i < maxStep; i++) {
							if (copyStep[i]) {
								if (lastIndex > i) {
									if (!copyCreated) {
										addCopyInstruction(instructions, resIndexReal, copyIndex);
										copyCreated = true;
									} else {
										addMultInstruction(instructions, copyIndex, resIndexReal);
									}
								}
							}
							addSquareInstruction(instructions);
						}
						if (copyCreated)
							addMultInstruction(instructions, resIndexReal, copyIndex);
						optimized = true;
					}
				}
			}
		}
		if (optimized)
			expressionBuilder.fixedValues.put(expParamName, value);
		else { // default case
			if (complexExpression)
				instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_POW_COMPLEX, resIndexReal,
						resIndexImag, expExpr.getResultIndexReal(), expExpr.getResultIndexImag()));
			else
				instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_POW_PART, resIndexReal,
						expExpr.getResultIndexReal(), -1, -1));
		}
	}

	protected void addReciprInstruction(List<ComputeInstruction> instructions) {
		if (complexExpression)
			instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_RECIPROCAL_COMPLEX, resIndexReal,
					resIndexImag, -1, -1));
		else
			instructions
					.add(new ComputeInstruction(ComputeInstruction.INSTR_RECIPROCAL_PART, resIndexReal, -1, -1, -1));
	}

	private void addSquareInstruction(List<ComputeInstruction> instructions) {
		addInstruction(instructions, ComputeInstruction.INSTR_SQUARE_COMPLEX, ComputeInstruction.INSTR_SQUARE_PART);
	}

	private void addCopyInstruction(List<ComputeInstruction> instructions, int sourceIndexReal, int targetIndexReal) {
		addInstruction(instructions, ComputeInstruction.INSTR_COPY_COMPLEX, ComputeInstruction.INSTR_COPY_PART,
				sourceIndexReal, sourceIndexReal + 1, targetIndexReal, targetIndexReal + 1);
	}

	private void addMultInstruction(List<ComputeInstruction> instructions, int sourceIndexReal, int targetIndexReal) {
		addInstruction(instructions, ComputeInstruction.INSTR_MULT_COMPLEX, ComputeInstruction.INSTR_MULT_PART,
				sourceIndexReal, sourceIndexReal + 1, targetIndexReal, targetIndexReal + 1);
	}

	private void addAddInstruction(List<ComputeInstruction> instructions, int sourceIndexReal, int targetIndexReal) {
		addInstruction(instructions, ComputeInstruction.INSTR_ADD_COMPLEX, ComputeInstruction.INSTR_ADD_PART,
				sourceIndexReal, sourceIndexReal + 1, targetIndexReal, targetIndexReal + 1);
	}

	private void addInstruction(List<ComputeInstruction> instructions, int complexInstruction, int partInstruction) {
		addInstruction(instructions, complexInstruction, partInstruction, resIndexReal, resIndexImag, -1, -1);
	}

	private void addInstruction(List<ComputeInstruction> instructions, int complexInstruction, int partInstruction,
			int fromReal, int fromImag, int toReal, int toImag) {
		if (complexExpression)
			instructions.add(new ComputeInstruction(complexInstruction, fromReal, fromImag, toReal, toImag));
		else
			instructions.add(new ComputeInstruction(partInstruction, fromReal, -1, toReal, -1));
	}

	@Override
	public FractalsExpression getDerivative(String derivativeVariableName) {
		if (baseExpr instanceof VariableExpression
				&& derivativeVariableName.equals(((VariableExpression) baseExpr).getVariableName())) {
			FractalsExpression expExpr = this.expExpr;
			if (expExpr instanceof NestedExpression) {
				NestedExpression ne = ((NestedExpression) expExpr);
				if (ne.instructionComplex < 0 && ne.instructionPart < 0)
					expExpr = ne.contentExpression;
			}
			boolean expConstant = expExpr instanceof ConstantExpression;
			if (expConstant) {
				ConstantExpression ce = (ConstantExpression) expExpr;
				double r = Double.parseDouble(ce.real);
				double i = Double.parseDouble(ce.imag);
				if (i == 0.0) {
					if (r == 1.0) // 1*x^0
						return new ConstantExpression("1", "0");
					else if (r == 2.0) { // 2*x^1
						List<FractalsExpression> subExprParts = new ArrayList<>();
						List<Integer> subExprPartLinks = new ArrayList<>();
						List<Integer> subExprComplexLinks = new ArrayList<>();
						subExprParts.add(baseExpr.copy());
						subExprParts.add(new ConstantExpression(2, 0));
						subExprPartLinks.add(-1);
						subExprComplexLinks.add(ComputeInstruction.INSTR_MULT_COMPLEX);
						return new MultExpression(subExprParts, subExprPartLinks, subExprComplexLinks);
					}
					else if (r == -1.0) // -x^-2
						return new NegateExpression(new ExpExpression(baseExpr.copy(), new ConstantExpression(r - 1, i)));
					else { // x^(pow-1)*pow
						ExpExpression derivExp = new ExpExpression(baseExpr.copy(), new ConstantExpression(r - 1, i));
						return new MultExpression(Arrays.asList(derivExp, new ConstantExpression(r, i)),
								Arrays.asList(ComputeInstruction.INSTR_MULT_PART),
								Arrays.asList(ComputeInstruction.INSTR_MULT_COMPLEX));
					}
				}
			}
			//remaining cases:
			ArrayList<FractalsExpression> subExprs = new ArrayList<>();
			subExprs.add(expExpr.copy());
			subExprs.add(new ConstantExpression(1f, 0f));
			ChainExpression decrExpExpr = new ChainExpression(subExprs, Arrays.asList(-1), Arrays.asList(ComputeInstruction.INSTR_SUB_COMPLEX));
			ExpExpression derivedExpression = new ExpExpression(baseExpr.copy(), decrExpExpr);
			ArrayList<FractalsExpression> subExprs2 = new ArrayList<>();
			subExprs2.add(derivedExpression);
			subExprs2.add(expExpr.copy());
			MultExpression multExpression = new MultExpression(subExprs2, Arrays.asList(-1), Arrays.asList(ComputeInstruction.INSTR_MULT_COMPLEX));
			return multExpression;
		}
		return copy();
	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {

		double baseSmoothstepConstant = baseExpr.getSmoothstepConstant(expressionBuilder);

		ParamSupplier supplier = null;
		if (expExpr instanceof VariableExpression) {
			supplier = expressionBuilder.parameters.get(((VariableExpression) expExpr).name);
		}
		if (expExpr instanceof ConstantExpression) {
			return baseSmoothstepConstant * ((ConstantExpression) expExpr).complexNumber.absDouble();
		}

		if (supplier == null)
			return 0;
		if (supplier instanceof MappedParamSupplier)
			return 2; // TODO smoothstep for mapped power possible?
		if (supplier instanceof StaticParamSupplier) {
			StaticParamSupplier supp = ((StaticParamSupplier) supplier);
			Object obj = supp.getGeneral();
			if (obj instanceof ComplexNumber) {
				double r = ((ComplexNumber) obj).realDouble();
				double i = ((ComplexNumber) obj).imagDouble();
				return baseSmoothstepConstant * Math.sqrt(r * r + i * i);
			}
		}
		return 0;
	}

	@Override
	public FractalsExpression getFirstChildlessExpression() {
		return baseExpr.getFirstChildlessExpression();
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
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions,
			Set<String> iterateVarNames) {
		StaticSubExpression staticBase = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames,
				baseExpr);
		StaticSubExpression staticExp = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames,
				expExpr);
		if (staticBase != null)
			baseExpr = staticBase;
		if (staticExp != null)
			expExpr = staticExp;
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return baseExpr.isStatic(iterateVarNames) && expExpr.isStatic(iterateVarNames);
	}

	@Override
	public boolean modifiesFirstVariable() {
		return true;
	}

	@Override
	public FractalsExpression copy() {
		return new ExpExpression(baseExpr.copy(), expExpr.copy());
	}

}
