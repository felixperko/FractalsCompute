package de.felixperko.expressions;

import org.junit.jupiter.api.Test;

import de.felixperko.fractals.system.calculator.ComputeInstruction;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

class TestChainExpression extends AbstractExpressionTest{

	@Test
	void testAdd() {
		
		checkInstrs("z", "z+c", INSTR_ADD_COMPLEX);
	}

	@Test
	void testAdd2() {
		
		checkInstrs("z", "z+c+c", INSTR_ADD_COMPLEX, INSTR_ADD_COMPLEX);
	}

	@Test
	void testSub() {
		
		checkInstrs("z", "z-c", INSTR_SUB_COMPLEX);
	}

	@Test
	void testAddSub() {
		
		checkInstrs("z", "z+c-c", INSTR_ADD_COMPLEX, INSTR_SUB_COMPLEX);
	}
	
	@Test
	void testConstSub() {
		
		expr("z", "1-z");
		checkInstrs(new ComputeInstruction(INSTR_COPY_COMPLEX, 2, 3, 4, 5),
				new ComputeInstruction(INSTR_SUB_COMPLEX, 4, 5, 0, 1),
				new ComputeInstruction(INSTR_COPY_COMPLEX, 4, 5, 0, 1));
	}
	
	@Test
	void testAddConstSub() {
		
		expr("z", "z+(1-z)");
		checkInstrs(new ComputeInstruction(INSTR_COPY_COMPLEX, 2, 3, 4, 5),
				new ComputeInstruction(INSTR_SUB_COMPLEX, 4, 5, 0, 1),
				new ComputeInstruction(INSTR_ADD_COMPLEX, 0, 1, 4, 5));
	}
	
	@Test
	void testMultConstSub() {
		
		expr("z", "z^2*(1-z)");
		checkInstrs(new ComputeInstruction(INSTR_COPY_COMPLEX, 2, 3, 4, 5),
				new ComputeInstruction(INSTR_SUB_COMPLEX, 4, 5, 0, 1),
				new ComputeInstruction(INSTR_MULT_COMPLEX, 0, 1, 4, 5));
	}

}
