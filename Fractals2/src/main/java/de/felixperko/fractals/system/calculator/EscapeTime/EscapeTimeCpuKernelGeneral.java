package de.felixperko.fractals.system.calculator.EscapeTime;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;

/**
 * A general soft coded implementation that doesn't require bytecode manipulation (Android compatible).
 * Might be slower than expression-specific implementations.
 * Implements orbit tracing (if enabled).
 */
public class EscapeTimeCpuKernelGeneral extends EscapeTimeCpuKernelNew {
	
	List<ComputeInstruction> instructions = new ArrayList<>();
	int orbitIndex = 0;
	int iteration = 0;
	
	public EscapeTimeCpuKernelGeneral(ComputeKernelParameters kernelParameters) {
		super(kernelParameters);
	}
	
	@Override
	public float run(int pixel, double[] params, float[] sampleOffsets, int chunkSize, int currentSamples, int maxIterations, double limitSq) {
		instructions = kernelParameters.getMainExpression().getInstructions();
		orbitIndex = 1;
		return super.run(pixel, params, sampleOffsets, chunkSize, currentSamples, maxIterations, limitSq);
	}

	@Override
	public void iterate(int dataOffset) {
		
		for (ComputeInstruction instruction : instructions) {
			switch (instruction.type){
			case (INSTR_ABS_PART):
				instr_abs_part(instruction.fromReal);
				break;
			case (INSTR_SIN_PART):
				instr_sin_part(instruction.fromReal);
				break;
			case (INSTR_COS_PART):
				instr_cos_part(instruction.fromReal);
				break;
			case (INSTR_TAN_PART):
				instr_tan_part(instruction.fromReal);
				break;
			case (INSTR_SINH_PART):
				instr_sinh_part(instruction.fromReal);
				break;
			case (INSTR_COSH_PART):
				instr_cosh_part(instruction.fromReal);
				break;
			case (INSTR_TANH_PART):
				instr_tanh_part(instruction.fromReal);
				break;
			case (INSTR_NEGATE_PART):
				instr_negate_part(instruction.fromReal);
				break;
			case (INSTR_RECIPROCAL_PART):
				instr_reciprocal_part(instruction.fromReal);
				break;
			case (INSTR_LOG_PART):
				instr_log_part(instruction.fromReal);
				break;
			//2 params
			case (INSTR_SQUARE_PART):
				instr_square(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_SQUARE_COMPLEX):
				instr_square(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_ABS_COMPLEX):
				instr_abs_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_SIN_COMPLEX):
				instr_sin_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_COS_COMPLEX):
				instr_cos_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_TAN_COMPLEX):
				instr_tan_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_SINH_COMPLEX):
				instr_sinh_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_COSH_COMPLEX):
				instr_cosh_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_TANH_COMPLEX):
				instr_tanh_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_NEGATE_COMPLEX):
				instr_negate_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_RECIPROCAL_COMPLEX):
				instr_reciprocal_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_LOG_COMPLEX):
				instr_log_complex(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_POW_PART):
				instr_pow_part(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_MULT_PART):
				instr_mult_part(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_DIV_PART):
				instr_div_part(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_ADD_PART):
				instr_add_part(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_SUB_PART):
				instr_sub_part(instruction.fromReal, instruction.fromImag);
				break;
			case (INSTR_COPY_PART):
				instr_copy_part(instruction.fromReal, instruction.fromImag);
				break;
			//4 params
			case (INSTR_POW_COMPLEX):
				//if(r2 == 2 && i2 == 0) instr = "instr_square"
				//else instr = "instr_pow"
				instr_pow_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			case (INSTR_MULT_COMPLEX):
				instr_mult_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			case (INSTR_DIV_COMPLEX):
				instr_div_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			case (INSTR_ADD_COMPLEX):
				instr_add_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			case (INSTR_SUB_COMPLEX):
				instr_sub_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			case (INSTR_COPY_COMPLEX):
				instr_copy_complex(instruction.fromReal, instruction.fromImag, instruction.toReal, instruction.toImag);
				break;
			default:
				String type = CONSTANT_NAMES.get(instruction.type);
				if (type == null)
					type = ""+instruction.type;
				throw new IllegalArgumentException("Unsupported instruction: "+type);
			}
			if (tracesCount > 0 && orbitIndex < tracesCount && (!tracePerInstruction || tracesReal[orbitIndex-1] != data[0] || tracesImag[orbitIndex-1] != data[1])) {
				tracesReal[orbitIndex] = data[0];
				tracesImag[orbitIndex] = data[1];
				tracesIterations[orbitIndex] = iteration;
				orbitIndex += tracePerInstruction ? 1 : 0;
			}
		}
		iteration++;
		orbitIndex += tracePerInstruction ? 0 : 1;
	}
}
