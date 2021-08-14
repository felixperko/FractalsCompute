package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ComputeExpressionDomain {
	
	List<ComputeExpression> mainExpressions = new ArrayList<>();
	List<ComputeExpression> staticExpressions = new ArrayList<>();
	
	public ComputeExpressionDomain(List<ComputeExpression> mainExpressions) {
		if (mainExpressions != null)
			this.mainExpressions = mainExpressions;
	}
	
	public ComputeExpressionDomain(ComputeExpression firstExpression) {
		this.mainExpressions.add(firstExpression);
	}
	
	public void addMainExpression(ComputeExpression expression) {
		this.mainExpressions.add(expression);
	}
	
	public List<ComputeExpression> getMainExpressions(){
		return mainExpressions;
	}

	public void addStaticExpressions(List<ComputeExpression> preExpressions) {
		this.staticExpressions.addAll(preExpressions);
	}
	
	public List<ComputeExpression> getStaticExpressions(){
		return staticExpressions;
	}
	
	public List<ParamSupplier> getParameterList(){
		List<ParamSupplier> supps = new ArrayList<>();
		for (ComputeExpression expr : mainExpressions) {
			for (ParamSupplier supp : expr.getParameterList()) {
				if (!supps.contains(supp))
					supps.add(supp);
			}
		}
		return supps;
	}
	
	public Map<String, ComplexNumber> getExplicitValues(){
		Map<String, ComplexNumber> map = new HashMap<String, ComplexNumber>();
		for (ComputeExpression expr : mainExpressions) {
			map.putAll(expr.getExplicitValues());
		}
		return map;
	}
}
