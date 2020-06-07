package de.felixperko.fractals.system.calculator;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import com.aparapi.device.Device;
import com.aparapi.internal.model.CacheEnabler;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.codegen.DynamicClassLoader;
import de.felixperko.fractals.util.codegen.LineNumberTextifier;

public class GpuAsmExpressionKernelFactory implements IGpuExpressionKernelFactory{

	static String packageName = "de/felixperko/fractals/system/calculator/";
	
	static int counter = 0;

	@Override
	public GPUExpressionKernel createKernel(Device device, GPUExpression expression, List<ParamSupplier> paramSuppliers,
			List<String> constantNames, List<Layer> layers, int pixelCount) {
		
		boolean printClass = false;
		boolean printJasm = false;
		
		ClassWriter actualCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		String simpleClassName = "GpuAsmExpressionKernel_"+counter++;
		String simpleSuperClassName = GPUExpressionKernel.class.getSimpleName();
		
		String className = packageName+simpleClassName;
		String superClassName = packageName+simpleSuperClassName;
		
		String srcName = simpleClassName+".jasm";
		String dir = System.getProperty("user.dir");
		
		String binPathStr = "de/felixperko/fractals/system/calculator/"+simpleClassName+".class";
		
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
						writeBytecode(expression, simpleSuperClassName, className, superClassName, srcName, cw_trace);
					}
				} else {
					writeBytecode(expression, simpleSuperClassName, className, superClassName, srcName, actualCw);
				}
				
				if (printClass)
					Files.write(binPath, actualCw.toByteArray());
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			writeBytecode(expression, simpleSuperClassName, className, superClassName, srcName, actualCw);
		}

		DynamicClassLoader loader = new DynamicClassLoader();
		Class<?> clazz = loader.defineClass("de.felixperko.fractals.system.calculator."+simpleClassName, binPathStr, actualCw.toByteArray());
		System.out.println(clazz.getClassLoader());
		GPUExpressionKernel kernel = null;
		try {
			kernel = (GPUExpressionKernel) clazz.getDeclaredConstructor(Device.class, GPUExpression.class, List.class, List.class, List.class, int.class).newInstance(device, expression, paramSuppliers, constantNames, layers, pixelCount);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return kernel;
	}

	private void writeBytecode(GPUExpression expression, String simpleSuperClassName, String className,
			String superClassName, String srcName, ClassVisitor cw) {
		cw.visit(V1_8, ACC_PUBLIC, className, null, packageName+simpleSuperClassName, null);

		String constructor_descriptor = "(Lcom/aparapi/device/Device;"
				+ "Lde/felixperko/fractals/system/calculator/GPUExpression;"
				+ "Ljava/util/List;"
				+ "Ljava/util/List;"
				+ "Ljava/util/List;"
				+ "I)"
				+ "V";
		MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", constructor_descriptor, null, null);
		
		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitVarInsn(ALOAD, 1);
		constructor.visitVarInsn(ALOAD, 2);
		constructor.visitVarInsn(ALOAD, 3);
		constructor.visitVarInsn(ALOAD, 4);
		constructor.visitVarInsn(ALOAD, 5);
		constructor.visitVarInsn(ILOAD, 6);
		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", constructor_descriptor, false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(7, 7);
		constructor.visitEnd();
		
		MethodVisitor iterate = cw.visitMethod(ACC_PROTECTED, "iterate", "(I)V", null, null);
		
		iterate.visitParameter("dataOffset", ACC_FINAL);
		
		for (GPUInstruction instruction : expression.instructions){
			iterate.visitVarInsn(ALOAD, 0);
			iterate.visitLdcInsn(instruction.fromReal);
			iterate.visitVarInsn(ILOAD, 1);
			iterate.visitInsn(IADD);
			iterate.visitLdcInsn(instruction.fromImag);
			iterate.visitVarInsn(ILOAD, 1);
			iterate.visitInsn(IADD);
			if (instruction.type == GPUInstruction.INSTR_SQUARE)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_square", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_ABS)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_abs", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_SIN)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sin", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_COS)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cos", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_TAN)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tan", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_SINH)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sinh", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_COSH)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_cosh", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_TANH)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_tanh", "(II)V", false);
			else if (instruction.type == GPUInstruction.INSTR_CONJ)
				iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_conj", "(II)V", false);
			else {
				iterate.visitLdcInsn(instruction.toReal);
				iterate.visitVarInsn(ILOAD, 1);
				iterate.visitInsn(IADD);
				iterate.visitLdcInsn(instruction.toImag);
				iterate.visitVarInsn(ILOAD, 1);
				iterate.visitInsn(IADD);
				switch (instruction.type){
				case (GPUInstruction.INSTR_POW):
					//if(r2 == 2 && i2 == 0) instr = "instr_square"
					//else instr = "instr_pow"
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_pow", "(IIII)V", false);
					break;
				case (GPUInstruction.INSTR_MULT):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_mult", "(IIII)V", false);
					break;
				case (GPUInstruction.INSTR_ADD):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_add", "(IIII)V", false);
					break;
				case (GPUInstruction.INSTR_SUB):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_sub", "(IIII)V", false);
					break;
				case (GPUInstruction.INSTR_WRITE_COMPLEX):
					iterate.visitMethodInsn(INVOKEVIRTUAL, superClassName, "instr_writecomplex", "(IIII)V", false);
					break;
				default:
					String type = GPUInstruction.CONSTANT_NAMES.get(instruction.type);
					if (type == null)
						type = ""+instruction.type;
					throw new IllegalArgumentException("Unsupported instruction: "+type);
				}
			}
		}
		
		iterate.visitInsn(RETURN);
		iterate.visitMaxs(6, 1);
		iterate.visitEnd();

		cw.visitSource(srcName, null);
		cw.visitEnd();
	}
}
