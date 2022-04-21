package de.felixperko.expressions;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import org.junit.jupiter.api.Test;

public class TestMultExpression extends AbstractExpressionTest{
	@Test
	void testMult() {
		
		checkInstrs("z", "z*c", INSTR_MULT_COMPLEX);
	}

	@Test
	void testMult2() {
		
		checkInstrs("z", "z*c*c", INSTR_MULT_COMPLEX, INSTR_MULT_COMPLEX);
	}

	@Test
	void testDiv() {
		
		checkInstrs("z", "z/c", INSTR_DIV_COMPLEX);
	}

	@Test
	void testMultDiv() {
		
		checkInstrs("z", "z*c/c", INSTR_MULT_COMPLEX, INSTR_DIV_COMPLEX);
	}

}
