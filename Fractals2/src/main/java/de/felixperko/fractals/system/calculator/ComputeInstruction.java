package de.felixperko.fractals.system.calculator;

import java.util.HashMap;
import java.util.Map;

public class ComputeInstruction {
	
	//
	//STATIC
	//
	
	@SuppressWarnings("serial")
	public static final Map<Integer, String> CONSTANT_NAMES = new HashMap<Integer, String>(){
		{
			put(0, "INSTR_ADD_COMPLEX");
			put(1, "INSTR_SUB_COMPLEX");
			put(2, "INSTR_MULT_COMPLEX");
			put(3, "INSTR_DIV_COMPLEX");
			put(4, "INSTR_POW_COMPLEX");
			put(5, "INSTR_COPY_COMPLEX");
			put(6, "INSTR_ABS_COMPLEX");
			put(7, "INSTR_SIN_COMPLEX");
			put(8, "INSTR_COS_COMPLEX");
			put(9, "INSTR_TAN_COMPLEX");
			put(10, "INSTR_SINH_COMPLEX");
			put(11, "INSTR_COSH_COMPLEX");
			put(12, "INSTR_TANH_COMPLEX");
			put(13, "INSTR_SQUARE_COMPLEX");
			put(14, "INSTR_NEGATE_COMPLEX");

			put(15, "INSTR_ADD_PART");
			put(16, "INSTR_SUB_PART");
			put(17, "INSTR_MULT_PART");
			put(18, "INSTR_DIV_PART");
			put(19, "INSTR_POW_PART");
			put(20, "INSTR_COPY_PART");
			put(21, "INSTR_ABS_PART");
			put(22, "INSTR_SIN_PART");
			put(23, "INSTR_COS_PART");
			put(24, "INSTR_TAN_PART");
			put(25, "INSTR_SINH_PART");
			put(26, "INSTR_COSH_PART");
			put(27, "INSTR_TANH_PART");;
			put(28, "INSTR_SQUARE_PART");
			put(29, "INSTR_NEGATE_PART");
		}
	};

	public static final int INSTR_ADD_COMPLEX = 0;
	public static final int INSTR_SUB_COMPLEX = 1;
	public static final int INSTR_MULT_COMPLEX = 2;
	public static final int INSTR_DIV_COMPLEX = 3;
	public static final int INSTR_POW_COMPLEX = 4;
	public static final int INSTR_COPY_COMPLEX = 5;
	public static final int INSTR_ABS_COMPLEX = 6;
	public static final int INSTR_SIN_COMPLEX = 7;
	public static final int INSTR_COS_COMPLEX = 8;
	public static final int INSTR_TAN_COMPLEX = 9;
	public static final int INSTR_SINH_COMPLEX = 10;
	public static final int INSTR_COSH_COMPLEX = 11;
	public static final int INSTR_TANH_COMPLEX = 12;
	public static final int INSTR_SQUARE_COMPLEX = 13;
	public static final int INSTR_NEGATE_COMPLEX = 14;

	public static final int INSTR_ADD_PART = 15;
	public static final int INSTR_SUB_PART = 16;
	public static final int INSTR_MULT_PART = 17;
	public static final int INSTR_DIV_PART = 18;
	public static final int INSTR_POW_PART = 19;
	public static final int INSTR_COPY_PART = 20;
	public static final int INSTR_ABS_PART = 21;
	public static final int INSTR_SIN_PART = 22;
	public static final int INSTR_COS_PART = 23;
	public static final int INSTR_TAN_PART = 24;
	public static final int INSTR_SINH_PART = 25;
	public static final int INSTR_COSH_PART = 26;
	public static final int INSTR_TANH_PART = 27;
	public static final int INSTR_SQUARE_PART = 28;
	public static final int INSTR_NEGATE_PART = 29;
	
	//
	//CLASS
	//
	
	public int type;

	public int fromReal;
	public int fromImag;
	public int toReal;
	public int toImag;
	
	public ComputeInstruction(int type, int fromReal, int fromImag, int toReal, int toImag){
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromImag;
		result = prime * result + fromReal;
		result = prime * result + toImag;
		result = prime * result + toReal;
		result = prime * result + type;
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
		ComputeInstruction other = (ComputeInstruction) obj;
		if (fromImag != other.fromImag)
			return false;
		if (fromReal != other.fromReal)
			return false;
		if (toImag != other.toImag)
			return false;
		if (toReal != other.toReal)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
