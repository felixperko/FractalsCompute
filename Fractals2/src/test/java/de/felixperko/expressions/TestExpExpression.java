package de.felixperko.expressions;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;
import org.junit.jupiter.api.Test;

public class TestExpExpression extends AbstractExpressionTest {
	
	//General
	@Test
	public void testExpFractional() {
		checkInstrs("z", "z^3.141", INSTR_POW_COMPLEX);
	}
	
	//Derivatives
	@Test
	public void testDerivPow3() {
		exprDeriv("z", "z^3"); //3*z^2
		checkInstrs(INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
		checkConst(nf.ccn("3"));
		checkConst(nf.ccn("2"));
	}

	@Test
	public void testDerivPow2() {
		exprDeriv("z", "z^2"); //2*z^1 = z+z
		checkInstrs(INSTR_ADD_COMPLEX);
	}
	
	@Test
	public void testDerivPow1() {
		exprDeriv("z", "z"); //z^0 == 1
		checkInstrs(INSTR_COPY_COMPLEX); //set 1
		checkConst(nf.ccn("1"));
	}
	
	@Test
	public void testDerivPowNegative1() {
		exprDeriv("z", "z^(-1)"); //-z^(-2)
		checkInstrs(INSTR_RECIPROCAL_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_NEGATE_COMPLEX);
	}
}
