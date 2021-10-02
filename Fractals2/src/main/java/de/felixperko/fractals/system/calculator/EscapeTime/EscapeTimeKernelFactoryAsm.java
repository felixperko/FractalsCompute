package de.felixperko.fractals.system.calculator.EscapeTime;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import com.aparapi.device.Device;
import com.aparapi.internal.model.CacheEnabler;

import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.calculator.ComputeKernelParameters;
import de.felixperko.fractals.system.calculator.IGpuKernelFactory;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.codegen.DynamicClassLoader;
import de.felixperko.fractals.util.codegen.LineNumberTextifier;

public class EscapeTimeKernelFactoryAsm implements IGpuKernelFactory{

	static String packageNameSlash = "de/felixperko/fractals/system/calculator/";
	static String packageNameDot = packageNameSlash.replace("/", ".");
	static String packageNameDoubleBackspace = packageNameSlash.replace("/", "\\");
	
	static int counter = 0;
	
	boolean printClass = false;
	boolean printJasm = false;

	@Override
	public EscapeTimeGpuKernelAbstract createKernel(Device device, ComputeKernelParameters kernelParameters) {
		
		ComputeExpression expression = kernelParameters.getMainExpression();
		
		Class<?> superclass = null;
		if (kernelParameters.getPrecision() == 32)
			superclass = EscapeTimeGpuKernel32.class;
		else if (kernelParameters.getPrecision() == 64)
			superclass = EscapeTimeGpuKernel64.class;
		else
			throw new IllegalArgumentException("precision not supported: "+kernelParameters.getPrecision()+" supported values: 32, 64");

		Class<?> clazz = createClass(expression, superclass, DeviceType.GPU);
		
		EscapeTimeGpuKernelAbstract kernel = null;
		try {
			kernel = (EscapeTimeGpuKernelAbstract) clazz.getDeclaredConstructor(Device.class, ComputeKernelParameters.class).newInstance(device, kernelParameters);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return kernel;
	}

	public EscapeTimeCpuKernel createKernelCpu(ComputeKernelParameters kernelParameters) {
		Class<?> clazz = createClass(kernelParameters.getMainExpression(), EscapeTimeCpuKernel.class, DeviceType.CPU);
		
		EscapeTimeCpuKernel kernel = null;
		try {
			kernel = (EscapeTimeCpuKernel) clazz.getDeclaredConstructor(ComputeKernelParameters.class).newInstance(kernelParameters);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return kernel;
	}

	public ComputeKernel createKernelCpu(Class<? extends ComputeKernel> kernelClass, ComputeKernelParameters kernelParameters) {
		
		boolean createClass = kernelClass != EscapeTimeCpuKernelGeneralized.class;
		Class<?> clazz = !createClass ? kernelClass : createClass(kernelParameters.getMainExpression(), kernelClass, DeviceType.CPU);
		
		ComputeKernel kernel = null;
		try {
			kernel = (ComputeKernel) clazz.getDeclaredConstructor(ComputeKernelParameters.class).newInstance(kernelParameters);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return kernel;
	}

	protected Class<?> createClass(ComputeExpression expression, Class<?> superclass, DeviceType deviceType) {
		String simpleClassName = "GpuAsmExpressionKernel_"+counter++;
		String binPathStr = packageNameSlash+simpleClassName+".class";
		
		ClassWriter actualCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		String simpleSuperClassName = superclass.getSimpleName(); //TODO choose Kernel class based on precision
		
		String className = packageNameSlash+simpleClassName;
		String superClassName = superclass.getPackage().getName().replace(".", "/")+"/"+simpleSuperClassName;
		
		String srcName = simpleClassName+".jasm";
		String dir = System.getProperty("user.dir");
		
		if (printClass || printJasm){
			File srcFile = new File(dir + "\\src\\main\\java\\de\\felixperko\\fractals\\system\\calculator");
			Path srcPath = srcFile.toPath().resolve(srcName);
			File binFile = new File(dir + "\\bin\\main\\java\\de\\felixperko\\fractals\\system\\calculator");
			Path binPath = binFile.toPath().resolve(simpleClassName+".class");
			try {
				
				if (printJasm){
					try (PrintWriter sourceWriter = new PrintWriter(Files.newBufferedWriter(srcPath))){
						LineNumberTextifier lno = new LineNumberTextifier();
						TraceClassVisitor cw_trace = new TraceClassVisitor(actualCw, lno, sourceWriter);
						writeBytecode(expression, className, superClassName, srcName, cw_trace, deviceType);
					}
				} else {
					writeBytecode(expression, className, superClassName, srcName, actualCw, deviceType);
				}
				
				if (printClass)
					Files.write(binPath, actualCw.toByteArray());
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			writeBytecode(expression, className, superClassName, srcName, actualCw, deviceType);
		}

		DynamicClassLoader loader = new DynamicClassLoader();
		Class<?> clazz = loader.defineClass(packageNameDot+simpleClassName, binPathStr, actualCw.toByteArray());
		return clazz;
	}

	private void writeBytecode(ComputeExpression expression, String className,
			String superClassName, String srcName, ClassVisitor cw, DeviceType deviceType) {
		
		cw.visit(V1_8, ACC_PUBLIC, className, null, superClassName, null);
		
		if (deviceType == DeviceType.GPU)
			writeConstructorGpu(superClassName, cw);
		else if (deviceType == DeviceType.CPU)
			writeConstructorCpu(superClassName, cw);
		
		writeIterateMethod(expression, superClassName, cw);

		cw.visitSource(srcName, null);
		cw.visitEnd();
	}

	protected void writeConstructorGpu(String superClassName, ClassVisitor cw) {
		String constructor_descriptor = "(Lcom/aparapi/device/Device;"
				+ "L"+ComputeKernelParameters.class.getName().replace(".", "/")+";)"
				+ "V";
		MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", constructor_descriptor, null, null);
		
		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);//this
		constructor.visitVarInsn(ALOAD, 1);//device
		constructor.visitVarInsn(ALOAD, 2);//kernelParameters
		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructor_descriptor, false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(3, 3);
		constructor.visitEnd();
	}

	protected void writeConstructorCpu(String superClassName, ClassVisitor cw) {
		String constructor_descriptor = "(L"+ComputeKernelParameters.class.getName().replace(".", "/")
				+";)"+ "V";
		MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", constructor_descriptor, null, null);
		
		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);//this
		constructor.visitVarInsn(ALOAD, 1);//kernelParameters
		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructor_descriptor, false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(2, 2);
		constructor.visitEnd();
	}

	protected void writeIterateMethod(ComputeExpression expression, String superClassName, ClassVisitor cw) {
		MethodVisitor iterate = cw.visitMethod(ACC_PROTECTED, "iterate", "(I)V", null, null);
		
		iterate.visitParameter("dataOffset", ACC_FINAL);
		
		for (ComputeInstruction instruction : expression.getInstructions()){
				
			iterate.visitVarInsn(ALOAD, 0);
			iterate.visitLdcInsn(instruction.fromReal);
			iterate.visitVarInsn(ILOAD, 1);
			iterate.visitInsn(IADD);
			
			//1 param
			switch (instruction.type){
			case (INSTR_SQUARE_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_square_part", "(I)V", false);
				break;
			case (INSTR_ABS_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_abs_part", "(I)V", false);
				break;
			case (INSTR_SIN_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sin_part", "(I)V", false);
				break;
			case (INSTR_COS_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cos_part", "(I)V", false);
				break;
			case (INSTR_TAN_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tan_part", "(I)V", false);
				break;
			case (INSTR_SINH_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sinh_part", "(I)V", false);
				break;
			case (INSTR_COSH_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cosh_part", "(I)V", false);
				break;
			case (INSTR_TANH_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tanh_part", "(I)V", false);
				break;
			case (INSTR_NEGATE_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_negate_part", "(I)V", false);
				break;
			case (INSTR_RECIPROCAL_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_reciprocal_part", "(I)V", false);
				break;
			case (INSTR_LOG_PART):
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_log_part", "(I)V", false);
				break;
			default:
				iterate.visitLdcInsn(instruction.fromImag);
				iterate.visitVarInsn(ILOAD, 1);
				iterate.visitInsn(IADD);
				//2 params
				switch (instruction.type){
				case (INSTR_SQUARE_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_square", "(II)V", false);
					break;
				case (INSTR_ABS_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_abs_complex", "(II)V", false);
					break;
				case (INSTR_SIN_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sin_complex", "(II)V", false);
					break;
				case (INSTR_COS_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cos_complex", "(II)V", false);
					break;
				case (INSTR_TAN_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tan_complex", "(II)V", false);
					break;
				case (INSTR_SINH_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sinh_complex", "(II)V", false);
					break;
				case (INSTR_COSH_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cosh_complex", "(II)V", false);
					break;
				case (INSTR_TANH_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tanh_complex", "(II)V", false);
					break;
				case (INSTR_NEGATE_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_negate_complex", "(II)V", false);
					break;
				case (INSTR_RECIPROCAL_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_reciprocal_complex", "(II)V", false);
					break;
				case (INSTR_LOG_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_log_complex", "(II)V", false);
					break;
				case (INSTR_POW_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_pow_part", "(II)V", false);
					break;
				case (INSTR_MULT_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_mult_part", "(II)V", false);
					break;
				case (INSTR_DIV_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_div_part", "(II)V", false);
					break;
				case (INSTR_ADD_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_add_part", "(II)V", false);
					break;
				case (INSTR_SUB_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sub_part", "(II)V", false);
					break;
				case (INSTR_COPY_PART):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_copy_part", "(II)V", false);
					break;
				default:
					iterate.visitLdcInsn(instruction.toReal);
					iterate.visitVarInsn(ILOAD, 1);
					iterate.visitInsn(IADD);
					iterate.visitLdcInsn(instruction.toImag);
					iterate.visitVarInsn(ILOAD, 1);
					iterate.visitInsn(IADD);
					//4 params
					switch (instruction.type){
					case (INSTR_POW_COMPLEX):
						//if(r2 == 2 && i2 == 0) instr = "instr_square"
						//else instr = "instr_pow"
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_pow_complex", "(IIII)V", false);
						break;
					case (INSTR_MULT_COMPLEX):
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_mult_complex", "(IIII)V", false);
						break;
					case (INSTR_DIV_COMPLEX):
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_div_complex", "(IIII)V", false);
						break;
					case (INSTR_ADD_COMPLEX):
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_add_complex", "(IIII)V", false);
						break;
					case (INSTR_SUB_COMPLEX):
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sub_complex", "(IIII)V", false);
						break;
					case (INSTR_COPY_COMPLEX):
						iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_copy_complex", "(IIII)V", false);
						break;
					default:
						String type = CONSTANT_NAMES.get(instruction.type);
						if (type == null)
							type = ""+instruction.type;
						throw new IllegalArgumentException("Unsupported instruction: "+type);
					}
				}
			}
		}
		
		iterate.visitInsn(RETURN);
		iterate.visitMaxs(6, 1);
		iterate.visitEnd();
	}
}
