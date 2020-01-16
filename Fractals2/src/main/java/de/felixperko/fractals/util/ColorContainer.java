package de.felixperko.fractals.util;

/**
 * Contains color data to be converted by the client application to it's specific Color class.
 * java.awt.Color creation causes a ClassNotFound (?) exception for android.
 */
public class ColorContainer {
	
	public static final ColorContainer MAGENTA = new ColorContainer(1f, 0f, 1f);
	public static final ColorContainer YELLOW = new ColorContainer(1f, 1f, 0f);
	byte r,g,b;

	public ColorContainer(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public ColorContainer(int r, int g, int b){
		this.r = (byte) r;
		this.g = (byte) g;
		this.b = (byte) b;
	}
	
	public ColorContainer(float r, float g, float b){
		this.r = (byte)(r*256f);
		this.g = (byte)(g*256f);
		this.b = (byte)(b*256f);
	}
	
	public void set(byte r, byte g, byte b){
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public void set(float r, float g, float b){
		this.r = (byte)(r*256f);
		this.g = (byte)(g*256f);
		this.b = (byte)(b*256f);
	}

	public byte getR() {
		return r;
	}
	
	public float getRf(){
		return r/256f;
	}

	public void setR(byte r) {
		this.r = r;
	}

	public void setRf(float r) {
		this.r = (byte)(r/256f);
	}

	public byte getG() {
		return g;
	}
	
	public float getGf(){
		return g/256f;
	}

	public void setG(byte g) {
		this.g = g;
	}

	public void setGf(float g) {
		this.g = (byte)(g/256f);
	}

	public byte getB() {
		return b;
	}
	
	public float getBf(){
		return b/256f;
	}

	public void setB(byte b) {
		this.b = b;
	}

	public void setBf(float b) {
		this.b = (byte)(b/256f);
	}
}
