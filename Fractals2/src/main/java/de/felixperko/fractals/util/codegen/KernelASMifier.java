package de.felixperko.fractals.util.codegen;

import java.io.IOException;

import org.objectweb.asm.util.ASMifier;

public class KernelASMifier {
	
	public static void main(String[] args) {
		try {
			ASMifier.main(new String[]{KernelASMifierCode.class.getCanonicalName()});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//dummy fields/methods
	
	final float[] data = new float[256];

	protected void instr_square(int r1, int i1) {
		float temp1;
		temp1 = data[r1]*data[r1] - data[i1]*data[i1];
		data[i1] = data[r1]*data[i1]*2;
		data[r1] = temp1;
	}
	
	protected void instr_pow_complex(int r1, int i1, int r2, int i2){
		float temp1;
		float temp2;
		float temp3;
		temp3 = (float)Math.sqrt(data[r1]*data[r1]+data[i1]*data[i1]);
		if (temp3 != 0){
			temp3 =(float) Math.log(temp3);
			temp1 =(float) Math.atan2(data[i1], data[r1]);
			//mult
			temp2 = (temp3*data[r2] - temp1*data[i2]);
			temp1 = (temp3*data[i2] + temp1*data[r2]);
			temp3 = temp2;
			//exp
			temp2 =(float) Math.exp(temp3);
			data[r1] =(float) (temp2 * Math.cos(temp1));
			data[i1] =(float) (temp2 * Math.sin(temp1));
		} else {
			data[r1] = data[r1]; //Workaround for "child list broken" in nested if-clause without else
		}
	}
}
