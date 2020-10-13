package de.felixperko.fractals.util.codegen;

import java.io.IOException;
import java.util.List;

import org.objectweb.asm.util.ASMifier;

import com.aparapi.device.Device;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;

public class KernelASMifierCode extends KernelASMifier{
	
	public void iterate(int dataOffset){
		if (data[13+dataOffset] == 0f && data[12+dataOffset] == 2f){
			super.instr_square(10+dataOffset, 11+dataOffset);
		} else {
			super.instr_pow_complex(10+dataOffset, 11+dataOffset, 12+dataOffset, 13+dataOffset);
		}
	}
}
