package de.felixperko.fractals.system.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateDiscreteParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateModuloParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public class ComputeExpression {
	
	
	List<ComputeInstruction> instructions;
	Map<ParamSupplier, Integer> constants = new HashMap<>(); //idx_real == value; idx_imag == value+1
	Map<ParamSupplier, Integer> variables = new HashMap<>(); //see above
	List<Integer> copySlots = new ArrayList<>();

	String text;

	ParamSupplier[] params;
	
	Map<String, ComplexNumber> fixedValues;
	Map<String, ComplexNumber> explicitValues;
	
	int requiredVariableSlots;
	
	double smoothstepConstant;
	
	public ComputeExpression(String text, List<ComputeInstruction> instructions, Map<ParamSupplier, Integer> parameters, int requiredVariableSlots,
			Map<String, ComplexNumber> fixedValues, Map<String, ComplexNumber> explicitValues, double smoothstepConstant) {
		this.text = text;
		this.instructions = instructions;
		this.requiredVariableSlots = requiredVariableSlots;
		this.params = new ParamSupplier[parameters.size()];
		this.fixedValues = fixedValues;
		this.explicitValues = explicitValues;
		this.smoothstepConstant = smoothstepConstant;
		for (Entry<ParamSupplier, Integer> e : parameters.entrySet()){
			ParamSupplier supp = e.getKey();
			Integer index = e.getValue();
			if (index/2 >= params.length) {
				throw new IllegalArgumentException("Slot "+index+" invalid for parameter "+supp.getName());
			}
			params[index/2] = supp;
			if (supp instanceof StaticParamSupplier || supp instanceof CoordinateDiscreteParamSupplier){
				constants.put(supp, index);
			} else if (supp instanceof MappedParamSupplier || supp instanceof CoordinateModuloParamSupplier){
				variables.put(supp, index);
			} else {
				throw new IllegalArgumentException(ComputeExpression.class.getName()+" doesn't support "+supp == null ? "null" : supp.getClass().getName());
			}
		}
		for (int i = parameters.size()*2 ; i < requiredVariableSlots ; i+=2){
			this.copySlots.add(i);
		}
	}
	
	public String getText(){
		return text;
	}

	public List<ParamSupplier> getParameterList() {
		return Arrays.asList(params);
	}
	
	public List<ParamSupplier> getMissingParams(ParamConfiguration paramConfiguration){
		HashSet<String> definedSet = new HashSet<>();
		for (ParamDefinition definition : paramConfiguration.getParameters()){
			definedSet.add(definition.getName());
		}
		List<ParamSupplier> list = new ArrayList<>();
        for (ParamSupplier exprParam : params){
            if (!definedSet.contains(exprParam.getName()))
            	list.add(exprParam);
        }
        return list;
	}

	public List<String> getConstantNames() {
		List<String> list = new ArrayList<>();
		for (ParamSupplier constant : constants.keySet())
			list.add(constant.getName());
		return list;
	}
	
	public Map<String, ParamSupplier> getConstants(){
		Map<String, ParamSupplier> res = new HashMap<>();
		for (ParamSupplier constant : constants.keySet()){
			res.put(constant.getName(), constant);
		}
		return res;
	}

	public int getRequiredVariableSlots() {
		return requiredVariableSlots;
	}

	public List<ComputeInstruction> getInstructions() {
		return instructions;
	}

	public Map<ParamSupplier, Integer> getVariables() {
		return variables;
	}

	public ParamSupplier[] getParams() {
		return params;
	}

	public Map<String, ComplexNumber> getFixedValues() {
		return fixedValues;
	}

	public Map<String, ComplexNumber> getExplicitValues() {
		return explicitValues;
	}

	public double getSmoothstepConstant() {
		return smoothstepConstant;
	}

	public void setSmoothstepConstant(double smoothstepConstant) {
		this.smoothstepConstant = smoothstepConstant;
	}
	
	public List<Integer> getCopySlots() {
		return copySlots;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fixedValues == null) ? 0 : fixedValues.hashCode());
		result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
		result = prime * result + requiredVariableSlots;
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
		ComputeExpression other = (ComputeExpression) obj;
		if (fixedValues == null) {
			if (other.fixedValues != null)
				return false;
		} else if (!fixedValues.equals(other.fixedValues))
			return false;
		if (instructions == null) {
			if (other.instructions != null)
				return false;
		} else if (!instructions.equals(other.instructions))
			return false;
		if (requiredVariableSlots != other.requiredVariableSlots)
			return false;
		return true;
	}

}
