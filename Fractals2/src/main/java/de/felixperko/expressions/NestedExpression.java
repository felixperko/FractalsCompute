package de.felixperko.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

/**
 * eg.
 * (expr)
 * sin(expr)
 * abs(expr)
 * conj(expr)
 */
public class NestedExpression extends AbstractExpression {

	protected FractalsExpression contentExpression;
	int instructionPart, instructionComplex;
	
	//apply to part of complex number, e.g. sinr(z) == (sin(re(z))+im(z)*i)
	boolean complexIgnoreReal = false;
	boolean complexIgnoreImag = false;

	public NestedExpression(FractalsExpression contentExpression, int instructionPart, int instructionComplex) {
		this.contentExpression = contentExpression;
		this.instructionPart = instructionPart;
		this.instructionComplex = instructionComplex;
	}

	@Override
	public boolean hasTempResult() {
		return contentExpression.hasTempResult();
	}

	@Override
	public int getResultIndexReal() {
		return contentExpression.getResultIndexReal();
	}

	@Override
	public int getResultIndexImag() {
		return contentExpression.getResultIndexImag();
	}

	@Override
	public void registerSymbolUses(ComputeExpressionBuilder expressionBuilder, NumberFactory numberFactory,
			boolean copyVariable) {
		if (!copyVariable)
			copyVariable = instructionPart != -1 || instructionComplex != -1;
		contentExpression.registerSymbolUses(expressionBuilder, numberFactory, copyVariable);
	}

	@Override
	public void addInitInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		contentExpression.addInitInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addEndInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		contentExpression.addEndInstructions(instructions, expressionBuilder);
	}

	@Override
	public void addInstructions(List<ComputeInstruction> instructions, ComputeExpressionBuilder expressionBuilder) {
		contentExpression.addInstructions(instructions, expressionBuilder);
		boolean complex = contentExpression.isComplexExpression();
		if (complex){ //add a complex expression
			if (instructionComplex >= 0 && !(complexIgnoreReal && complexIgnoreImag)){ //not empty/disabled
				if (complexIgnoreReal){ //apply to imag part of complex input, ignore other (pass)
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexImag(), contentExpression.getResultIndexImag(), -1, -1));
				}
				else if (complexIgnoreImag){ //apply to real part of complex input, ignore other (pass)
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexReal(), contentExpression.getResultIndexReal(), -1, -1));
				}
				else { //nothing filtered, apply complex instruction
					instructions.add(new ComputeInstruction(instructionComplex, contentExpression.getResultIndexReal(), contentExpression.getResultIndexImag(),
							contentExpression.getResultIndexReal(), contentExpression.getResultIndexImag()));
				}
			}
		} else { //add a part expression
			if (instructionPart >= 0){
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexReal(), contentExpression.getResultIndexReal(), -1, -1));
			}
		}
	}

	@Override
	public boolean isComplexExpression() {
		return contentExpression.isComplexExpression();
	}

	@Override
	public boolean isSingleRealExpression() {
		return contentExpression.isSingleRealExpression();
	}

	@Override
	public boolean isSingleImagExpression() {
		return contentExpression.isSingleImagExpression();
	}

	@Override
	public double getSmoothstepConstant(ComputeExpressionBuilder expressionBuilder) {
		return contentExpression.getSmoothstepConstant(expressionBuilder);
	}
	
	public void setComplexFilterOutReal(boolean filterOut){
		this.complexIgnoreReal = filterOut;
	}
	
	public void setComplexFilterOutImag(boolean filterOut){
		this.complexIgnoreImag = filterOut;
	}
	
	public boolean isComplexFilterReal(){
		return complexIgnoreReal;
	}
	
	public boolean isComplexFilterImag(){
		return complexIgnoreImag;
	}

}