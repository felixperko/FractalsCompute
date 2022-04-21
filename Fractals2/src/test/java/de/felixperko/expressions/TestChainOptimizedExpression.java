package de.felixperko.expressions;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import org.junit.jupiter.api.Test;

public class TestChainOptimizedExpression extends AbstractExpressionTest{

	@Test
	public void testOptimizedPow2() {
		checkInstrs("z", "z^2", INSTR_SQUARE_COMPLEX);
	}

	@Test
	public void testOptimizedPow3() {
		checkInstrs("z", "z^3", INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	public void testOptimizedPow4() {
		checkInstrs("z", "z^4", INSTR_SQUARE_COMPLEX, INSTR_SQUARE_COMPLEX);
	}

	@Test
	public void testOptimizedPow5() {
		checkInstrs("z", "z^5", INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	public void testOptimizedPow6() {
		checkInstrs("z", "z^6", INSTR_SQUARE_COMPLEX, INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	public void testOptimizedPow7() {
		checkInstrs("z", "z^7", INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	public void testOptimizedPow8() {
		checkInstrs("z", "z^8", INSTR_SQUARE_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_SQUARE_COMPLEX);
	}

	@Test
	public void testOptimizedPowNegative1() {
		checkInstrs("z", "z^(-1)", INSTR_RECIPROCAL_COMPLEX);
	}

	@Test
	public void testOptimizedPowNegative2() {
		checkInstrs("z", "z^(-2)", INSTR_RECIPROCAL_COMPLEX, INSTR_SQUARE_COMPLEX);
	}

	@Test
	public void testOptimizedPowNegative3() {
		checkInstrs("z", "z^(-3)", INSTR_RECIPROCAL_COMPLEX, INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	public void testOptimizedPow3PlusPow2() {
		checkInstrs("z", "z^3+z^2", INSTR_COPY_COMPLEX, INSTR_SQUARE_COMPLEX, INSTR_MULT_COMPLEX, INSTR_ADD_COMPLEX);
	}

}
