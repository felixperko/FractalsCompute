package de.felixperko.expressions;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import org.junit.jupiter.api.Test;

public class TestTrigExpression extends AbstractExpressionTest{


	@Test
	public void testDerivSin() {
		exprDeriv("z", "sin(z)"); // cos(x)
		checkInstrs(INSTR_COS_COMPLEX);
	}

	@Test
	public void testDerivCos() {
		exprDeriv("z", "cos(z)"); // -sin(x)
		checkInstrs(INSTR_SIN_COMPLEX, INSTR_NEGATE_COMPLEX);
	}

	@Test
	public void testDerivTan() {
		exprDeriv("z", "tan(z)"); // 1/cos(z)^2
		checkInstrs(INSTR_COPY_COMPLEX, INSTR_COS_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_DIV_COMPLEX);
	}

	@Test
	public void testDerivSinh() {
		exprDeriv("z", "sinh(z)"); // cosh(z)
		checkInstrs(INSTR_COSH_COMPLEX);
	}

	@Test
	public void testDerivCosh() {
		exprDeriv("z", "cosh(z)"); // sinh(z)
		checkInstrs(INSTR_SINH_COMPLEX);
	}

	@Test
	public void testDerivTanh() {
		exprDeriv("z", "tanh(z)"); // 1/cosh(z)^2
		checkInstrs(INSTR_COPY_COMPLEX, INSTR_COSH_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_DIV_COMPLEX);
	}

	@Test
	public void testDerivSinSq() {
		exprDeriv("z", "sin(z)^2"); // 2*sin(z)*cos(z)
		checkInstrs(INSTR_COPY_COMPLEX, INSTR_SIN_COMPLEX, INSTR_MULT_COMPLEX, INSTR_COS_COMPLEX, INSTR_MULT_COMPLEX);
	}
}
