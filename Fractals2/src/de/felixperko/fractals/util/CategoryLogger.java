package de.felixperko.fractals.util;

public class CategoryLogger {
	
	public static CategoryLogger INFO = new CategoryLogger("info", new ColorContainer(0f, 0.5f, 0f));
	public static CategoryLogger WARNING = new CategoryLogger("warning", new ColorContainer(1f, 0.5f, 0f));
	public static CategoryLogger WARNING_SERIOUS = new CategoryLogger("warning/serious", new ColorContainer(1f, 0.25f, 0f));
	public static CategoryLogger ERROR = new CategoryLogger("error", new ColorContainer(1f, 0f, 0f));
	
	String category;
	ColorContainer color;
	
	public CategoryLogger(String category, ColorContainer color) {
		this.category = category;
		this.color = color;
	}
	
	public void log(String msg) {
		Logger.log(new Message(this, msg));
	}
	
	public void log(String prefix, String msg) {
		Logger.log(new Message(this, msg).setPrefix(prefix));
	}

	public ColorContainer getColor() {
		return color;
	}

	public String getName() {
		return category;
	}
	
	public CategoryLogger createSubLogger(String subCategory) {
		return new CategoryLogger(category+"/"+subCategory, color);
	}
	
	public CategoryLogger createSubLogger(String subCategory, ColorContainer color) {
		return new CategoryLogger(category+"/"+subCategory, color);
	}
}
