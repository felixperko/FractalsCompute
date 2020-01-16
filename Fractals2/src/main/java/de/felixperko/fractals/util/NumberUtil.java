package de.felixperko.fractals.util;

public class NumberUtil {

	public static final double MS_TO_S = 0.001;
	public static final double NS_TO_S = 0.000000001;
	public static final double NS_TO_MS = 0.000001;
	public static final long S_TO_NS = (long)(1/NS_TO_S);
	
	public static double getRoundedDouble(double value, int precision) {
		double f = Math.pow(10, precision);
		return Math.round(value*f)/f;
	}
	
	public static double getRoundedPercentage(double value, int precision) {
		double f = Math.pow(10, precision);
		return Math.round(value*100*f)/f;
	}

	public static double getTimeInS(long timeInNs, int precision) {
		return getRoundedDouble(NS_TO_S*timeInNs, precision);
	}

	public static double getElapsedTimeInS(long sinceTimeInNs, int precision) {
		return getTimeInS(System.nanoTime()-sinceTimeInNs, precision);
	}
}
