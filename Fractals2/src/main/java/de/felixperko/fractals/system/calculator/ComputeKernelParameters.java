package de.felixperko.fractals.system.calculator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.expressions.ComputeExpressionDomain;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public class ComputeKernelParameters {
	
	ComputeExpressionDomain expressionDomain;;
	int precision;
	int pixelCount;
	List<Layer> layers;
	
	public ComputeKernelParameters(ComputeExpressionDomain expressionDomain, List<Layer> layers, int pixelCount, int precision) {
		this.expressionDomain = expressionDomain;
		this.layers = layers;
		this.pixelCount = pixelCount;
		this.precision = precision;
	}
	
	public void setComputeExpressionDomain(ComputeExpressionDomain expressionDomain) {
		this.expressionDomain = expressionDomain;
	}
	
	public boolean isCompartible(ComputeKernelParameters other, ParamContainer params){
		
		ComputeExpression expression = expressionDomain.getMainExpressions().get(0);
		
		//check basic values and array sizes
		if (this.precision != other.precision || this.pixelCount != other.pixelCount || expression.instructions.size() != expression.instructions.size())
			return false;
		if ((this.layers == null) != (other.layers == null) || (this.layers != null && (this.layers.size() != other.layers.size())))
			return false;
		
		//check expression instructions
		for (int i = 0 ; i < expression.instructions.size() ; i++){
			if (!expression.instructions.get(i).equals(expression.instructions.get(i)))
				return false;
		}

		//check fixed values against the given parameters
		Map<String, ComplexNumber> fixedValues = getFixedValues();
		if (fixedValues != null){
			for (Entry<String, ComplexNumber> e : fixedValues.entrySet()){
				String name = e.getKey();
				ParamSupplier supp = params.getClientParameter(name);
				if (supp == null){
					if (expression.getConstants().containsKey(name))
						continue;
					else
						return false;
				}
				if (!(supp instanceof StaticParamSupplier))
					return false;
				ComplexNumber currentValue = supp.getGeneral(ComplexNumber.class);
				if (!e.getValue().equals(currentValue))
					return false;
			}
			//check if others fixed values are fixed here as well
			Map<String, ComplexNumber> otherFixedValues = other.getFixedValues();
			if (otherFixedValues == null)
				return false;
			for (String fixedParamName : otherFixedValues.keySet()){
				if (!fixedValues.containsKey(fixedParamName))
					return false;
			}
		}
		
		//TODO check static subexpressions
		
		//check individual layers
		for (int i = 0 ; i < this.layers.size() ; i++){
			int layerSampleCount = this.layers.get(i).getSampleCount();
			if (layerSampleCount != other.layers.get(i).getSampleCount())
				return false;
			//Technically they could still have different shifts, but a mismatch is unlikely/probably wouldn't matter
		}
		return true;
	}

	public ComputeExpression getMainExpression() {
		return expressionDomain.getMainExpressions().get(0);
	}

	public int getPrecision() {
		return precision;
	}

	public int getPixelCount() {
		return pixelCount;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public Map<String, ComplexNumber> getFixedValues() {
		return getMainExpression().getFixedValues();
	}	
}
