package de.felixperko.fractals.util;

public class Message {
	
	String message;
	CategoryLogger category;
	String prefix;
	
	String logString = null;
	
	public Message(CategoryLogger category, String message){
		this.category = category;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Message setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public CategoryLogger getCategory() {
		return category;
	}
	
	public Message setCategory(CategoryLogger category) {
		this.category = category;
		resetLogString();
		return this;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public Message setPrefix(String prefix) {
		this.prefix = prefix;
		resetLogString();
		return this;
	}
	
	public String getCategoryPrefix() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(category.category);
		if (prefix != null){
			sb.append("/").append(prefix);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String getLogString() {
		return getLogString(false);
	}
	
	public String getLogString(boolean reset) {
		if (reset || logString == null)
			logString = getCategoryPrefix()+" "+message;
		return logString;
	}
	
	public void resetLogString() {
		logString = null;
	}
}
