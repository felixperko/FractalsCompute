package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

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
//		if (!copyVariable)
//			copyVariable = instructionPart != -1 || instructionComplex != -1;
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
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexImag(), -1, -1, -1));
				}
				else if (complexIgnoreImag){ //apply to real part of complex input, ignore other (pass)
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexReal(), -1, -1, -1));
				}
				else { //nothing filtered, apply complex instruction
					instructions.add(new ComputeInstruction(instructionComplex, contentExpression.getResultIndexReal(), contentExpression.getResultIndexImag(),
							-1, -1));
				}
			}
		} else { //add a part expression
			if (instructionPart >= 0){
					instructions.add(new ComputeInstruction(instructionPart, contentExpression.getResultIndexReal(), contentExpression.getResultIndexReal(), -1, -1));
			}
		}
	}
	
	@Override
	public FractalsExpression getFirstChildlessExpression() {
		return contentExpression.getFirstChildlessExpression();
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

	@Override
	public void extractStaticExpressions(List<FractalsExpression> staticSubFractalsExpressions, Set<String> iterateVarNames) {
		StaticSubExpression staticContent = extractSubExpressionOrPropagate(staticSubFractalsExpressions, iterateVarNames, contentExpression);
		if (staticContent != null)
			contentExpression = staticContent;
	}

	@Override
	public boolean isStatic(Set<String> iterateVarNames) {
		return contentExpression.isStatic(iterateVarNames);
	}

	@Override
	public FractalsExpression getDerivative(String derivativeVariableName) {
		if (instructionComplex == -1 && instructionPart == -1)
			return contentExpression.getDerivative(derivativeVariableName);
		else if (instructionComplex == INSTR_SIN_COMPLEX)
			return new NestedExpression(contentExpression, INSTR_COS_PART, INSTR_COS_COMPLEX);
		else if (instructionComplex == INSTR_COS_COMPLEX)
			return new NegateExpression(new NestedExpression(contentExpression, INSTR_SIN_PART, INSTR_SIN_COMPLEX));
		else if (instructionComplex == INSTR_TAN_COMPLEX) {
			FractalsExpression higher = new ConstantExpression("1", "0");
			FractalsExpression lower = new ExpExpression(new NestedExpression(contentExpression, INSTR_COS_PART, INSTR_COS_COMPLEX), new ConstantExpression("2", "0"));
			return new MultExpression(Arrays.asList(new FractalsExpression[] {higher, lower}), Arrays.asList(INSTR_DIV_PART), Arrays.asList(INSTR_DIV_COMPLEX));
		}
		else if (instructionComplex == INSTR_SINH_COMPLEX)
			return new NestedExpression(contentExpression, INSTR_COSH_PART, INSTR_COSH_COMPLEX);
		else if (instructionComplex == INSTR_COSH_COMPLEX)
			return new NestedExpression(contentExpression, INSTR_SINH_PART, INSTR_SINH_COMPLEX);
		else if (instructionComplex == INSTR_TANH_COMPLEX) {
			FractalsExpression higher = new ConstantExpression("1", "0");
			FractalsExpression lower = new ExpExpression(new NestedExpression(contentExpression, INSTR_COSH_PART, INSTR_COSH_COMPLEX), new ConstantExpression("2", "0"));
			return new MultExpression(Arrays.asList(new FractalsExpression[] {higher, lower}), Arrays.asList(INSTR_DIV_PART), Arrays.asList(INSTR_DIV_COMPLEX));
		}
		return null;
	}

	@Override
	public boolean modifiesFirstVariable() {
		return instructionPart != -1 || instructionComplex != -1 || contentExpression.modifiesFirstVariable();
	}

	@Override
	public FractalsExpression copy() {
		return new NestedExpression(contentExpression.copy(), instructionPart, instructionComplex);
	}

	@Override
	public void serialize(StringBuilder sb, boolean pretty) {
		if (instructionComplex != -1) {
			sb.append(FractalsExpressionParser.getInstructionName(instructionComplex));
		}
		sb.append("(");
		contentExpression.serialize(sb, pretty);
		sb.append(")");
	}

}