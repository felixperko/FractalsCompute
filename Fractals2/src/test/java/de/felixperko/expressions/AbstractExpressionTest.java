package de.felixperko.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.calculator.ComputeExpression;
import de.felixperko.fractals.system.calculator.ComputeInstruction;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.ExpressionsParam;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public class AbstractExpressionTest {
	
	NumberFactory nf = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
	
	Map<String, ComplexNumber> constants = new HashMap<>();
	
	ComputeExpression expr;
	ComputeExpressionDomain exprDomain;
	
	public void setConstant(String name, ComplexNumber value) {
		constants.put(name, value);
	}

	public AbstractExpressionTest constant(String name, String value) {
		setConstant(name, nf.ccn(value));
		return this;
	}
	
	public void checkInstrs(ComputeExpression expr, int... instrTypes) {
		this.expr = expr;
		checkInstrs(instrTypes);
	}
	
	public void checkInstrs(String outputVarName, String expression, int... instrTypes) {
		expr(outputVarName, expression);
		checkInstrs(instrTypes);
	}
	
	protected void checkConst(ComplexNumber ccn) {
		for (String constName : expr.getConstantNames()) {
			ComplexNumber val = expr.getExplicitValues().get(constName);
			if (val != null && val.equals(ccn))
				return;
		}
		fail("Explicit value not found: "+ccn.toString());
	}

	protected AbstractExpressionTest expr(String outputVarName, String expression) {
		ComputeExpressionBuilder builder = getComputeExpressionBuilder(outputVarName, expression);
		exprDomain = builder.getComputeExpressionDomain(false);
		expr = exprDomain.mainExpressions.get(0);
		return this;
	}

	protected void exprDeriv(String outputVarName, String expression) {
		ComputeExpressionBuilder ceb = getComputeExpressionBuilder(outputVarName, expression);
		List<FractalsExpression> exprs = ceb.getFractalsExpressions();
		List<FractalsExpression> derivs = new ArrayList<>();
		for (FractalsExpression expr : exprs) {
			derivs.add(expr.getDerivative(outputVarName));
		}
		exprDomain = ceb.getComputeExpressionDomain(false, derivs);
		expr = exprDomain.getMainExpressions().get(0);
	}

	protected ComputeExpressionBuilder getComputeExpressionBuilder(String outputVarName, String expression) {
		Map<String, ParamSupplier> params = new HashMap<>();
		for (Entry<String, ComplexNumber> e : constants.entrySet()) {
			params.put(e.getKey(), new StaticParamSupplier(e.getKey(), e.getValue()));
		}
		
		ExpressionsParam exprParam = new ExpressionsParam(expression, outputVarName);
		ComputeExpressionBuilder builder = new ComputeExpressionBuilder(exprParam, params);
		return builder;
	}
	
	protected void checkInstrs(int... checkInstrTypes) {
		List<ComputeInstruction> exprInstrs = expr.getInstructions();
		int instrStrParts = Math.max(checkInstrTypes.length, exprInstrs.size());
		StringBuilder debugStrBuilder = new StringBuilder();
		for (int i = 0 ; i < instrStrParts ; i++) {
			ComputeInstruction exprInstr = exprInstrs.size() <= i ? null : exprInstrs.get(i);
			String exprInstrStr = exprInstrs.size() <= i ? "NULL" : exprInstr.toString()+"";
			int checkInstrType = checkInstrTypes.length <= i ? -1 : checkInstrTypes[i];
			String checkInstrTypeStr = checkInstrTypes.length <= i ? "NULL" : ComputeInstruction.CONSTANT_NAMES.get(checkInstrType);
			boolean matches = (checkInstrType == -1 && exprInstrStr == null) || (exprInstr != null && checkInstrType == exprInstr.type);
			String importanceStr = matches ? "  " : "! ";
			debugStrBuilder.append(importanceStr+i+" is: "+exprInstrStr+(matches ? "" : " expected: "+checkInstrTypeStr)).append("\n");
		}
		int i = 0;
		String debugStr = debugStrBuilder.toString();
		for (ComputeInstruction instr : exprInstrs) {
			String actual = ComputeInstruction.CONSTANT_NAMES.get(instr.type);
			String expected = ComputeInstruction.CONSTANT_NAMES.get(checkInstrTypes[i]);
			assertEquals(instr.type, checkInstrTypes[i], "Instruction "+i+ ((actual==null) ?
					(" NULL") :
					(" not "+expected+" but "+actual+"\n"+
					debugStr)));
			i++;
		}
	}
	
	protected void checkInstrs(ComputeInstruction... instrs) {
		List<ComputeInstruction> exprInstrs = expr.getInstructions();
		StringBuilder debugStrBuilder = new StringBuilder();
		int instrStrParts = Math.max(instrs.length, exprInstrs.size());
		for (int i = 0 ; i < instrStrParts ; i++) {
			ComputeInstruction exprInstr = exprInstrs.size() <= i ? null : exprInstrs.get(i);
			String exprInstrStr = exprInstr == null ? "NULL" : exprInstr.toString();
			ComputeInstruction checkInstr = instrs.length <= i ? null : instrs[i];
			boolean matches = (exprInstr != null && exprInstr.equals(checkInstr));
			String importanceStr = matches ? "  " : "! ";
			debugStrBuilder.append(importanceStr+i+" is: "+exprInstrStr+(matches ? "" : " expected: "+checkInstr.toString())).append("\n");
		}
		int i = 0;
		String debugStr = debugStrBuilder.toString();
		for (ComputeInstruction instr : expr.getInstructions()) {
			assertEquals(instrs[i], instr, "\n"+debugStr);
			i++;
		}
	}

}
