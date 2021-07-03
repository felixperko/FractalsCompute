package de.felixperko.fractals.system.calculator;

import java.util.HashMap;
import java.util.Map;

public class ComputeInstruction {
	
	//
	//STATIC
	//
	
	public static final int OFFSET_INSTR_PART = 256;
	
	@SuppressWarnings("serial")
	public static final Map<Integer, String> CONSTANT_NAMES = new HashMap<Integer, String>(){
		{
			int counter = 0;
			put(counter++, "INSTR_ADD_COMPLEX");
			put(counter++, "INSTR_SUB_COMPLEX");
			put(counter++, "INSTR_MULT_COMPLEX");
			put(counter++, "INSTR_DIV_COMPLEX");
			put(counter++, "INSTR_POW_COMPLEX");
			put(counter++, "INSTR_COPY_COMPLEX");
			put(counter++, "INSTR_ABS_COMPLEX");
			put(counter++, "INSTR_SIN_COMPLEX");
			put(counter++, "INSTR_COS_COMPLEX");
			put(counter++, "INSTR_TAN_COMPLEX");
			put(counter++, "INSTR_SINH_COMPLEX");
			put(counter++, "INSTR_COSH_COMPLEX");
			put(counter++, "INSTR_TANH_COMPLEX");
			put(counter++, "INSTR_SQUARE_COMPLEX");
			put(counter++, "INSTR_NEGATE_COMPLEX");
			put(counter++, "INSTR_LOG_COMPLEX");
			
			counter = OFFSET_INSTR_PART;
			put(counter++, "INSTR_ADD_PART");
			put(counter++, "INSTR_SUB_PART");
			put(counter++, "INSTR_MULT_PART");
			put(counter++, "INSTR_DIV_PART");
			put(counter++, "INSTR_POW_PART");
			put(counter++, "INSTR_COPY_PART");
			put(counter++, "INSTR_ABS_PART");
			put(counter++, "INSTR_SIN_PART");
			put(counter++, "INSTR_COS_PART");
			put(counter++, "INSTR_TAN_PART");
			put(counter++, "INSTR_SINH_PART");
			put(counter++, "INSTR_COSH_PART");
			put(counter++, "INSTR_TANH_PART");;
			put(counter++, "INSTR_SQUARE_PART");
			put(counter++, "INSTR_NEGATE_PART");
			put(counter++, "INSTR_LOG_PART");
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
	public static final int INSTR_RECIPROCAL_COMPLEX = 15;
	public static final int INSTR_LOG_COMPLEX = 16;

	public static final int INSTR_ADD_PART = OFFSET_INSTR_PART+0;
	public static final int INSTR_SUB_PART = OFFSET_INSTR_PART+1;
	public static final int INSTR_MULT_PART = OFFSET_INSTR_PART+2;
	public static final int INSTR_DIV_PART = OFFSET_INSTR_PART+3;
	public static final int INSTR_POW_PART = OFFSET_INSTR_PART+4;
	public static final int INSTR_COPY_PART = OFFSET_INSTR_PART+5;
	public static final int INSTR_ABS_PART = OFFSET_INSTR_PART+6;
	public static final int INSTR_SIN_PART = OFFSET_INSTR_PART+7;
	public static final int INSTR_COS_PART = OFFSET_INSTR_PART+8;
	public static final int INSTR_TAN_PART = OFFSET_INSTR_PART+9;
	public static final int INSTR_SINH_PART = OFFSET_INSTR_PART+10;
	public static final int INSTR_COSH_PART = OFFSET_INSTR_PART+11;
	public static final int INSTR_TANH_PART = OFFSET_INSTR_PART+12;
	public static final int INSTR_SQUARE_PART = OFFSET_INSTR_PART+13;
	public static final int INSTR_NEGATE_PART = OFFSET_INSTR_PART+14;
	public static final int INSTR_RECIPROCAL_PART = OFFSET_INSTR_PART+15;
	public static final int INSTR_LOG_PART = OFFSET_INSTR_PART+16;
	
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
