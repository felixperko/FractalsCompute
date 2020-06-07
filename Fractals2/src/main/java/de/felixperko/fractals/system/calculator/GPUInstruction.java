package de.felixperko.fractals.system.calculator;

import java.util.HashMap;
import java.util.Map;

public class GPUInstruction {
	
	//
	//STATIC
	//
	
	@SuppressWarnings("serial")
	public static final Map<Integer, String> CONSTANT_NAMES = new HashMap<Integer, String>(){
		{
			put(0, "power");
			put(1, "square");
			put(2, "add");
			put(3, "sub");
			put(4, "mult");
			put(5, "div");
			put(6, "write");
			put(7, "abs");
			put(8, "conj");
			put(9, "sin");
			put(10, "cos");
			put(11, "tan");
			put(12, "sinh");
			put(13, "cosh");
			put(14, "tanh");
		}
	};

	public static final int INSTR_POW = 0;
	public static final int INSTR_SQUARE = 1;
	public static final int INSTR_ADD = 2;
	public static final int INSTR_SUB = 3;
	public static final int INSTR_MULT = 4;
	public static final int INSTR_DIV = 5;
	public static final int INSTR_WRITE_COMPLEX = 6;
	public static final int INSTR_ABS = 7;
	public static final int INSTR_CONJ = 8;
	public static final int INSTR_SIN = 9;
	public static final int INSTR_COS = 10;
	public static final int INSTR_TAN = 11;
	public static final int INSTR_SINH = 12;
	public static final int INSTR_COSH = 13;
	public static final int INSTR_TANH = 14;
	
	//
	//CLASS
	//
	
	int type, fromReal, fromImag, toReal, toImag;
	
	public GPUInstruction(int type, int fromReal, int fromImag, int toReal, int toImag){
		this.type = type;
		this.fromReal = fromReal;
		this.fromImag = fromImag;
		this.toReal = toReal;
		this.toImag = toImag;
	}
	
	@Override
	public String toString() {
		return CONSTANT_NAMES.get(type)+" ("+fromReal+", "+fromImag+") -> ("+toReal+", "+toImag+")";
	}
}
