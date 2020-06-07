package de.felixperko.fractals.system.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public class GPUExpression {
	
	
	List<GPUInstruction> instructions;
	Map<ParamSupplier, Integer> constants = new HashMap<>(); //idx_real == value; idx_imag == value+1
	Map<ParamSupplier, Integer> variables = new HashMap<>(); //see above
	
	ParamSupplier[] params;
	
	String text;
	
	int requiredVariableSlots;
	
	public GPUExpression(String text, List<GPUInstruction> instructions, Map<ParamSupplier, Integer> parameters, int requiredVariableSlots) {
		this.text = text;
		this.instructions = instructions;
		this.requiredVariableSlots = requiredVariableSlots;
		this.params = new ParamSupplier[parameters.size()];
		for (Entry<ParamSupplier, Integer> e : parameters.entrySet()){
			ParamSupplier supp = e.getKey();
			Integer index = e.getValue();
			params[index/2] = supp;
			if (supp instanceof StaticParamSupplier){
				constants.put(supp, index);
			} else if (supp instanceof MappedParamSupplier){
				variables.put(supp, index);
			} else {
				throw new IllegalArgumentException(GPUExpression.class.getName()+" only supports "+
				StaticParamSupplier.class.getName()+" and "+MappedParamSupplier.class.getName()+" as parameters.\nGot "+supp == null ? "null" : supp.getClass().getName());
			}
		}
	}
	
	public GPUExpression(String text, List<GPUInstruction> instructions, Map<ParamSupplier, Integer> constants, Map<ParamSupplier, Integer> variables) {
		this.text = text;
		this.instructions = instructions;
		this.constants = constants;
		this.variables = variables;
	}
	
	public String getText(){
		return text;
	}

	public List<ParamSupplier> getParameterList() {
		return Arrays.asList(params);
	}

	public List<String> getConstantNames() {
		List<String> list = new ArrayList<>();
		for (ParamSupplier constant : constants.keySet())
			list.add(constant.getName());
		return list;
	}

	public int getRequiredVariableSlots() {
		return requiredVariableSlots;
	}
}
