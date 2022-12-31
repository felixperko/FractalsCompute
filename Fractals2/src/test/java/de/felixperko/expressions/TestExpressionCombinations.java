package de.felixperko.expressions;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

import org.junit.jupiter.api.Test;

public class TestExpressionCombinations extends AbstractExpressionTest{
	
	@Test
	public void testSquareAdd() {
		checkInstrs("z", "z^2+c", INSTR_SQUARE_COMPLEX, INSTR_ADD_COMPLEX);
	}
}
