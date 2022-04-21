package de.felixperko.othertests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * https://hal.archives-ouvertes.fr/hal-00021443/document
 * https://hal.inria.fr/inria-00070314/document
 *
 */
public class FloatFloatTest {
	
	private static final double TESTVAL = 1.000000000000001;
	private static final double TESTVAL2 = 0.000000000000001;
	private static final double TESTVAL3 = 1.000000000000000;
	private static final float TESTVAL4 = 1.0000001f;
	private static final float TESTVAL5 = 10000000.0000001f;
	private static final double TESTVAL6 = ((double)TESTVAL5)*((double)TESTVAL4);

	@Test
	public void testRoundingError() {
		double valD = TESTVAL;
		double valF = (float)valD;
		System.out.println(valD);
		System.out.println(valF);
		assertFalse(valD == (double)valF);
	}
	
	@Test
	public void testSplitD() {
		float[] vals = splitD(TESTVAL);
		double val2 = ((double)vals[0])+((double)vals[1]);
		System.out.println(TESTVAL+" = "+vals[0]+" + "+vals[1]);
		assertEquals(TESTVAL, val2);
	}
	
	@Test
	public void testAdd() {
		float[] vals1 = splitD(TESTVAL2);
		float[] vals2 = splitD(TESTVAL3);
		float[] sum = add22Flattened(vals1[0], vals1[1], vals2[0], vals2[1]);
		double res = ((double)sum[0])+((double)sum[1]);
		assertEquals(TESTVAL, res);
	}
	
	@Test
	public void testFailMult() {
		float wrongResult = ((float)TESTVAL4)*((float)TESTVAL5);
		double error = Math.abs(TESTVAL6-wrongResult);
		System.out.println("test mult fail err = "+error);
		assertNotEquals(TESTVAL6, wrongResult);
	}
	
	@Test
	public void testMult() {
		float[] vals1 = splitF((float)TESTVAL4);
		float[] vals2 = splitF((float)TESTVAL5);
		float[] sum = mult22flattened(vals1[0], vals1[1], vals2[0], vals2[1]);
		double res = ((double)sum[0])+((double)sum[1]);
		double error = Math.abs(TESTVAL6-res);
		System.out.println("test mult err = "+error);
		assertTrue(error <= 2E-44);
	}
	
	protected float[] splitD(double val) {
		int s = 32;
		double c = (Math.pow(2.0, s)+1.0)*val;
		double big = c-val;
		float hi = (float)(c-big);
		float lo = (float)(val-hi);
		return new float[] {hi, lo};
	}
	
	protected float[] add12(float a, float b) {
		float s = a+b;
		float v = s-a;
		float r = (a-(s-v))+(b-v);
		return new float[] {s, r};
	}
	
	protected float[] mult12(float a, float b) {
		float x = a*b;
		float[] av = splitF(a);
		float[] bv = splitF(b);
		float err1 = x - (av[0]*bv[0]);
		float err2 = err1 - (av[1]*bv[0]);
		float err3 = err2 - (av[0]*bv[1]);
		float y = av[1]*bv[1] - err3;
		return new float[] {x, y};
	}
	
	protected float[] add22(float ah, float al, float bh, float bl) {
		float r = ah + bh;
		float s = 0f;
		if (Math.abs(ah) >= Math.abs(bh)) {
			s = (((ah-r)+bh)+bl)+al;
		}
		else {
			s = (((bh-r)+ah)+al)+bl;
		}
		return add12(r, s);
	}
	
	protected float[] add22Flattened(float ah, float al, float bh, float bl) {
		float r = ah + bh;
		float s = 0f;
		if (Math.abs(ah) >= Math.abs(bh)) {
			s = (((ah-r)+bh)+bl)+al;
		} else {
			s = (((bh-r)+ah)+al)+bl;
		}
		float s2 = r+s;
		float v = s2-r;
		float r2 = (r-(s2-v))+(s-v);
		return new float[] {s2, r2};
	}
	
	protected float[] mult22(float ah, float al, float bh, float bl) {
		float[] t = mult12(ah, bh);
		float t2 = ((ah*bl)+(al*bh)) + t[1];
		return add12(t[0], t2);
	}

	
	protected float[] mult22flattened(float ah, float al, float bh, float bl) {
		
		float x = ah*bh;
		float[] av = splitF(ah);
		float[] bv = splitF(bh);
		float err1 = x - (av[0]*bv[0]);
		float err2 = err1 - (av[1]*bv[0]);
		float err3 = err2 - (av[0]*bv[1]);
		float y = av[1]*bv[1] - err3;
		

		float t2 = ((ah*bl)+(al*bh)) + y;
		
		float s = x+t2;
		float v = s-x;
		float r = (x-(s-v))+(t2-v);
		return new float[] {s, r};
	}
	

	protected float[] splitF(float val) {
		int s = 16;
		float c = (2^s+1)*val;
		float big = c-val;
		float hi = c-big;
		float lo = val-hi;
		return new float[] {hi, lo};
	}
	
	int splitF = (2^16+1);
	
	protected float[] splitFsimple(float val) {
		float temp = splitF*val;
		float temp2 = temp-val;
		float hi = temp-temp2;
		float lo = val-hi;
		return new float[] {hi, lo};
	}
	
	protected float[] add22expanded(float ah, float al, float bh, float bl) {
		
		float temp = ah + bh;
		float temp2 = 0.0f;
		if (Math.abs(ah) >= Math.abs(bh)) {
			temp2 = (((ah-temp)+bh)+bl)+al;
		}
		else {
			temp2 = (((bh-temp)+ah)+al)+bl;
		}

		ah = temp+temp2;
		float temp3 = ah-temp;
		al = (temp-(ah-temp3))+(temp2-temp3);
		
		return new float[] {ah, al};
	}

	
	protected float[] mult22expanded(float ah, float al, float bh, float bl) {
		
		float temp = ah*bh;
		
		float temp2 = splitF*ah;
		float temp3 = temp2-ah;
		float temp4 = temp2-temp3;
		float temp5 = ah-temp4;

		temp2 = splitF*bh;
		temp3 = temp2-bh;
		float temp6 = temp2-temp3;
		float temp7 = bh-temp6;
		
		temp2 = temp - temp4*temp6;
		temp3 = temp2 - temp5*temp6;
		temp2 = temp3 - temp4*temp7;
		
		temp3 = ah*bl + al*bh + temp5*temp7 - temp2;
		ah = temp+temp3;
		temp2 = ah-temp;
		al = (temp-(ah-temp2))+(temp3-temp2);
		
		return new float[] {ah, al};
	}
}
