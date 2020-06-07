package de.felixperko.fractals.util.codegen;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;


public class AsmTest {
	
	static String packageName = "de/felixperko/fractals/util/codegen/";
	
	public static void main(String[] args) {
		
		ClassWriter actualCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		String dir = System.getProperty("user.dir");
		System.out.println(dir);
		
		String className = "AsmImpl";
		String srcName = className+".jasm";
		File srcFile = new File(dir + "\\src\\main\\java\\de\\felixperko\\fractals\\util\\codegen");
		Path srcPath = srcFile.toPath().resolve(srcName);
		File binFile = new File(dir + "\\bin\\main\\java\\de\\felixperko\\fractals\\util\\codegen");
		Path binPath = binFile.toPath().resolve(className+".class");
		try {
			try (PrintWriter sourceWriter = new PrintWriter(Files.newBufferedWriter(srcPath))){
				LineNumberTextifier lno = new LineNumberTextifier();
				TraceClassVisitor cw = new TraceClassVisitor(actualCw, lno, sourceWriter);
			    
				
				String[] interfaces = new String[]{IAsm.class.getName().replace('.', '/')};
				
				//public class packageName.AsmImpl{
				cw.visit(V1_8, ACC_PUBLIC, packageName+"AsmImpl", null, "java/lang/Object", interfaces);
				
				////<init>
				//new AsmImpl();
				MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
				
				constructor.visitCode();
				//this -> 0
				constructor.visitVarInsn(ALOAD, 0); 
				
				//call constructor
				constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
				
				//return constructor
				constructor.visitInsn(RETURN);
				//1 stack variable, 1 local variable
				constructor.visitMaxs(1, 1);
				
				//public int add(int arg0, int arg1){
				MethodVisitor add_method = cw.visitMethod(ACC_PUBLIC, "add", "(II)I", null, null);
				
				add_method.visitCode();
				Label start = new Label(), end1 = new Label(), end2 = new Label();
				add_method.visitLabel(start);
//				lno.updateLineInfo(add_method);
//				add_method.visitLocalVariable("this", "L"+packageName+"AsmImpl;", null, start, end1, 1);
//				lno.updateLineInfo(add_method);
//				add_method.visitLocalVariable("arg0", "I", null, start, end1, 1);
//				lno.updateLineInfo(add_method);
//				add_method.visitLocalVariable("arg1", "I", null, start, end1, 2);
				//arg0 -> stack #1
				lno.updateLineInfo(add_method);
				add_method.visitVarInsn(ILOAD, 1);
				//arg1 -> stack #2
				lno.updateLineInfo(add_method);
				add_method.visitVarInsn(ILOAD, 2);
				//add #1, #2 -> #1, pop #2
				lno.updateLineInfo(add_method);
				add_method.visitInsn(IADD);
				//return #1
//				lno.updateLineInfo(add_method);
				add_method.visitLabel(end1);
				lno.updateLineInfo(add_method);
//				add_method.visitLocalVariable("arg0", "I", null, end1, end2, 1);
				add_method.visitLabel(end2);
//				lno.updateLineInfo(add_method);
				add_method.visitInsn(IRETURN);
				add_method.visitMaxs(2, 3);
				add_method.visitEnd();
				
				cw.visitSource(srcName, null);
				cw.visitEnd();
			}
			
			Files.write(binPath, actualCw.toByteArray());
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DynamicClassLoader loader = new DynamicClassLoader();
		Class<?> clazz = loader.defineClass("de.felixperko.fractals.util.codegen.AsmImpl", binPath.toString(), actualCw.toByteArray());
		System.out.println(clazz.getName());
		IAsm test;
		try {
			test = (IAsm)clazz.newInstance();
			System.out.println("40+2 = "+test.add(40, 2));
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
//	int currentLineNumber() {
//		return count(super.text)+1;
//	}
//	
//	public static void updateLineInfo(MethodVisitor target){
//		Label l1 = new Label();
//		target.visitLabel(l1);
//		target.visitLineNumber(currentLineNumber(), 1));
//	}
}
