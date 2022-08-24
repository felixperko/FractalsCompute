package de.felixperko.fractals.system.parameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.attributes.ParamAttributeContainer;
import de.felixperko.fractals.system.parameters.attributes.ParamAttributeHolder;

public class ExpressionsParam implements ParamAttributeHolder{

	Map<String, String> expressions = new LinkedHashMap<>();
	ParamAttributeContainer paramAttributes = new ParamAttributeContainer();
	String mainInputVar;
	
	public ExpressionsParam(String mainExpression, String mainInputVar) {
		this.expressions.put(mainInputVar, mainExpression);
		this.mainInputVar = mainInputVar;
	}
	
	public ExpressionsParam(LinkedHashMap<String, String> expressionsForVars) {
		for (String var : expressionsForVars.keySet()) {
			if (this.mainInputVar == null)
				this.mainInputVar = var;
			this.expressions.put(var, expressionsForVars.get(var));
		}
	}

	public void putExpression(String inputVar, String expr) {
		this.expressions.put(inputVar, expr);
	}
	
	public void putExpressions(Map<String, String> exprs) {
		for (Entry<String, String> e : exprs.entrySet()) {
			putExpression(e.getKey(), e.getValue());
		}
	}
	
	@Override
	public ParamAttributeContainer getParamAttributeContainer() {
		return this.paramAttributes;
	}
	
	public String getMainExpression() {
		return expressions.get(mainInputVar);
	}
	
	public String getMainInputVar() {
		return mainInputVar;
	}
	
	public Map<String, String> getExpressions() {
		return expressions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expressions == null) ? 0 : expressions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionsParam other = (ExpressionsParam) obj;
		if (expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!expressions.equals(other.expressions))
			return false;
		return true;
	}
	
}
