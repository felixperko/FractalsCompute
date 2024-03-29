package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.ExpressionsParam;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;

public class ComputeExpressionBuilder {

//	List<FractalsExpression> expressions;

	List<String> inputStrings = new ArrayList<>();
	List<String> varNames = new ArrayList<>();
	String inputVarName;
	Map<String, ParamSupplier> paramsByUID;
	Map<String, String> uidsByName = new HashMap<>();

	LinkedHashMap<String, ExpressionSymbol> referenceSymbols = new LinkedHashMap<>();

	LinkedHashMap<String, ExpressionSymbol> valueSymbols = new LinkedHashMap<>();
	Map<String, ComplexNumber> explicitValues = new HashMap<>();

	private LinkedHashMap<String, ExpressionSymbol> allSymbols = new LinkedHashMap<>();

	List<ComputeInstruction> instructions;

	Map<String, ComplexNumber> fixedValues = new HashMap<>();

	double smoothstepConstant;

	public ComputeExpressionBuilder(String input, String inputVarName, Map<String, ParamSupplier> paramsByUID,
			Map<String, String> uidsByName) {
		this.inputStrings.add(input);
		this.inputVarName = inputVarName;
		this.paramsByUID = paramsByUID;
		if (uidsByName != null)
			this.uidsByName = uidsByName;
	}

	public ComputeExpressionBuilder(ExpressionsParam expressions, Map<String, ParamSupplier> paramsByUID,
			Map<String, String> uidsByName) {
		this.paramsByUID = paramsByUID;
		this.inputVarName = expressions.getMainInputVar();
		if (uidsByName != null)
			this.uidsByName = uidsByName;
		for (java.util.Map.Entry<String, String> e : expressions.getExpressions().entrySet()) {
			addMainExpression(e.getValue(), e.getKey());
		}
	}

	public void addMainExpression(String input, String inputVarName) {
		inputStrings.add(input);
		this.varNames.add(inputVarName);
	}

	public ComputeExpression getComputeExpression() {
		List<ComputeExpression> computeExpressions = getComputeExpressionDomain(false).getMainExpressions();
		return computeExpressions != null && !computeExpressions.isEmpty() ? computeExpressions.get(0) : null;
	}

	public List<FractalsExpression> getFractalsExpressions() {
		List<FractalsExpression> expressions = new ArrayList<>();
		for (String input : inputStrings) {
			FractalsExpression mainExpression = FractalsExpressionParser.parse(input);
			expressions.add(mainExpression);
		}
		return expressions;
	}

	public ComputeExpressionDomain getComputeExpressionDomain(boolean extractStaticExpressions) {
		List<FractalsExpression> exprs = getFractalsExpressions();
		return getComputeExpressionDomain(extractStaticExpressions, exprs);
	}

	public ComputeExpressionDomain getComputeExpressionDomain(boolean extractStaticExpressions,
			List<FractalsExpression> expressions) {
		List<FractalsExpression> processExpressions = new ArrayList<>();
		Map<ParamSupplier, Integer> mappedParams = new HashMap<>();

		List<FractalsExpression> subFractalsExpressions = new ArrayList<>();
		if (extractStaticExpressions) {
			Set<String> varNameSet = new HashSet<>();
			for (int i = 0; i < varNames.size(); i++) {
				String varName = varNames.get(i);
				FractalsExpression expr = expressions.get(i);
				boolean isIdentity = expr instanceof VariableExpression
						&& varName.equals(((VariableExpression) expr).getVariableName());
				if (!isIdentity)
					varNameSet.add(varName);
			}
			for (FractalsExpression expression : expressions)
				expression.extractStaticExpressions(subFractalsExpressions, varNameSet);
		}

		for (FractalsExpression subExpression : subFractalsExpressions) {
			processExpressions.add(subExpression);
		}
		for (FractalsExpression expression : expressions) {
			processExpressions.add(expression);
		}

		NumberFactory nf = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);

		int ind = 0;
		for (FractalsExpression expression : processExpressions) {
			String inputVar = varNames.get(ind);
			ind++;
			FractalsExpression firstExpr = expression.getFirstChildlessExpression();
			boolean startsWithInputVar = firstExpr instanceof VariableExpression
					&& ((VariableExpression) firstExpr).getVariableName().equals(inputVar);
//			//identity doesn't need a copy
			expression.registerSymbolUses(this, nf, !startsWithInputVar);
//			expression.registerSymbolUses(this, nf, false);
		}

		int copyCounter = 0;

		for (ExpressionSymbol symbol : allSymbols.values()) {

			int copyIndex = copyCounter;
			if (copyCounter == 0 && !symbol.isOutputVar()) {
				copyIndex += 2;
			}
			if (symbol.isOutputVar()) {
				copyIndex = 0;
			}

			mapParamToSymbol(mappedParams, nf, copyIndex, symbol);

			if (!symbol.isOutputVar())
				copyCounter = symbol.assignPristineIndex(copyIndex);
			else
				symbol.assignPristineIndex(copyIndex);
		}
		;

		for (ExpressionSymbol var : allSymbols.values()) {
			copyCounter = var.assignCopyIndices(copyCounter);
		}

		instructions = new ArrayList<>();

		List<ComputeExpression> mainExpressions = new ArrayList<>();
		List<ComputeExpression> preExpressions = new ArrayList<>();

		for (int i = processExpressions.size() - 1; i >= 0; i--) {
			processExpressions.get(i).addInitInstructions(instructions, this);
		}
		for (FractalsExpression expression : processExpressions)
			expression.addInstructions(instructions, this);
		for (FractalsExpression expression : processExpressions)
			expression.addEndInstructions(instructions, this);

		// check if copy necessary
		FractalsExpression firstExpr = processExpressions.get(0);
		if (firstExpr.getResultIndexReal() != 0) {
			instructions.add(new ComputeInstruction(ComputeInstruction.INSTR_COPY_COMPLEX,
					firstExpr.getResultIndexReal(), firstExpr.getResultIndexImag(), 0, 1));
		}

		for (int i = 0; i < processExpressions.size(); i++) {
			FractalsExpression expression = processExpressions.get(i);
			String input = inputStrings.get(i);
			smoothstepConstant = Math.log(expression.getSmoothstepConstant(this));

			ComputeExpression computeExpression = new ComputeExpression(input, instructions, mappedParams, copyCounter,
					fixedValues, explicitValues, smoothstepConstant, uidsByName);

			if (expressions.contains(expression))
				mainExpressions.add(computeExpression);
			else if (subFractalsExpressions.contains(expression))
				preExpressions.add(computeExpression);
		}
		ComputeExpressionDomain domain = new ComputeExpressionDomain(mainExpressions);
		domain.addStaticExpressions(preExpressions);
		return domain;
	}

	public void mapParamToSymbol(Map<ParamSupplier, Integer> mappedParams, NumberFactory nf, Integer copyCounter,
			ExpressionSymbol symbol) {
		String symbolName = symbol.getName();
		boolean isReference = referenceSymbols.containsKey(symbolName);
		if (symbolName.equalsIgnoreCase(inputVarName))
			symbolName = symbolName + "_0"; // get start param instead of inputVarName (e.g. "z")
		if (isReference) {
			//also dynamically defined params (uid == name)
			String symbolUID = uidsByName.containsKey(symbolName) ? uidsByName.get(symbolName) : symbolName; 
			ParamSupplier param = paramsByUID.get(symbolUID);
			if (param == null)
				param = new StaticParamSupplier(symbolName, nf.createComplexNumber(1, 0));
//				throw new IllegalStateException("didn't find parameter: "+symbolName+" uid: "+symbolUID); //hen-egg problem
			mappedParams.put(param, copyCounter);
		} else { // is raw value
			ComplexNumber constant = explicitValues.get(symbolName);
			ParamSupplier supp = new StaticParamSupplier(symbolName, constant);
			mappedParams.put(supp, copyCounter);
		}
	}

	public ExpressionSymbol getReferenceExpressionSymbol(String name) {
		return getSymbol(referenceSymbols, name);
	}

	public ExpressionSymbol getValueExpressionSymbol(String name) {
		return getSymbol(valueSymbols, name);
	}

	public ExpressionSymbol getExpressionSymbol(String name) {
		return getSymbol(allSymbols, name);
	}

	private ExpressionSymbol getSymbol(LinkedHashMap<String, ExpressionSymbol> symbolList, String name) {
		ExpressionSymbol symbol = symbolList.get(name);
		if (symbol == null) {
			symbol = new ExpressionSymbol(name, name.equals(inputVarName));
			symbolList.put(name, symbol);
			allSymbols.put(name, symbol);
		}
		return symbol;
	}

	public void setExplicitValue(String name, ComplexNumber complexNumber) {
		explicitValues.put(name, complexNumber);
	}

	public double getSmoothstepConstant() {
		return smoothstepConstant;
	}
}
