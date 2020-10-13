package de.felixperko.fractals.system.calculator.EscapeTime;

public interface ComputeKernel {

	void iterate(int dataOffset);

	void instr_square(int r1, int i1);

	void instr_sin_complex(int r1, int i1);

	void instr_sin_part(int p1);

	void instr_cos_complex(int r1, int i1);

	void instr_cos_part(int p1);

	void instr_tan_complex(int r1, int i1);

	void instr_tan_part(int p1);

	void instr_sinh_complex(int r1, int i1);

	void instr_sinh_part(int p1);

	void instr_cosh_complex(int r1, int i1);

	void instr_cosh_part(int p1);

	void instr_tanh_complex(int r1, int i1);

	void instr_tanh_part(int p1);

	void instr_abs_complex(int r1, int i1);

	void instr_abs_part(int p1);

	void instr_copy_complex(int r1, int i1, int r2, int i2);

	void instr_copy_part(int p1, int p2);

	void instr_negate_complex(int r1, int i1);

	void instr_negate_part(int p1);

	void instr_add_complex(int r1, int i1, int r2, int i2);

	void instr_add_part(int p1, int p2);

	void instr_sub_complex(int r1, int i1, int r2, int i2);

	void instr_sub_part(int p1, int p2);

	void instr_mult_complex(int r1, int i1, int r2, int i2);

	void instr_mult_part(int p1, int p2);

	void instr_pow_complex_or_square(int r1, int i1, int r2, int i2);

	void instr_pow_complex(int r1, int i1, int r2, int i2);

	void instr_pow_part(int p1, int p2);

}