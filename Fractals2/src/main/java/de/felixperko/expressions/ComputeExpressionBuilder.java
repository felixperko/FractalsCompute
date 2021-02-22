package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public class ComputeExpressionBuilder {
	
	FractalsExpression expression;
	
	String input;
	String inputVarName;
	Map<String, ParamSupplier> parameters;
	
	LinkedHashMap<String, ExpressionSymbol> referenceSymbols = new LinkedHashMap<>();
	
	LinkedHashMap<String, ExpressionSymbol> valueSymbols = new LinkedHashMap<>();
	Map<String, ComplexNumber> explicitValues = new HashMap<>();
	
	private LinkedHashMap<String, ExpressionSymbol> allSymbols = new LinkedHashMap<>();
	
	List<ComputeInstruction> instructions;
	
	Map<String, ComplexNumber> fixedValues = new HashMap<>();
	
	double smoothstepConstant;
	
	public ComputeExpressionBuilder(String input, String inputVarName, Map<String, ParamSupplier> parameters){
		this.input = input;
		this.inputVarName = inputVarName;
		this.parameters = parameters;
	}
	
	public ComputeExpression getComputeExpression(){
		
		Map<ParamSupplier, Integer> mappedParams = new HashMap<>();
		
		expression = FractalsExpressionParser.parse(input);

		NumberFactory nf = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		
		expression.registerSymbolUses(this, nf, true);

		int copyCounter = 0;
		
		for (ExpressionSymbol symbol : allSymbols.values()){
			String symbolName = symbol.getName();
			boolean isReference = referenceSymbols.containsKey(symbolName);
			if (isReference){
				if (symbolName.equalsIgnoreCase(inputVarName))
					symbolName = "start"; //get "start" instead of inputVarName (e.g. "z")
				ParamSupplier param = parameters.get(symbolName);
				if (param == null)
					param = new StaticParamSupplier(symbolName, nf.createComplexNumber(0, 0));
//					throw new IllegalStateException("didnt find parameter "+symbolName); 
				mappedParams.put(param, (Integer)copyCounter);
			} else { //is raw value
				ComplexNumber constant = explicitValues.get(symbolName);
				ParamSupplier supp = new StaticParamSupplier(symbolName, constant);
				mappedParams.put(supp, (Integer)copyCounter);
			}
			copyCounter = symbol.assignPristineIndex(copyCounter);
		};
		
		for (ExpressionSymbol var : allSymbols.values()){
			copyCounter = var.assignCopyIndices(copyCounter);
		}
		
		instructions = new ArrayList<>();
		expression.addInitInstructions(instructions, this);
		expression.addInstructions(instructions, this);
		smoothstepConstant = Math.log(expression.getSmoothstepConstant(this));
		return new ComputeExpression(input, instructions, mappedParams, copyCounter, fixedValues, explicitValues, smoothstepConstant);
	}
	
	public ExpressionSymbol getReferenceExpressionSymbol(String name){
		return getSymbol(referenceSymbols, name);
	}
	
	public ExpressionSymbol getValueExpressionSymbol(String name){
		return getSymbol(valueSymbols, name);
	}
	
	private ExpressionSymbol getSymbol(LinkedHashMap<String, ExpressionSymbol> symbolList, String name){
		ExpressionSymbol symbol = symbolList.get(name);
		if (symbol == null){
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
