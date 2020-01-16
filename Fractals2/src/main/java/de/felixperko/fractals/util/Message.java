package de.felixperko.fractals.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
	
	String message;
	String prefix;
	
	String logString = null;
	
	public Message(String message){
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Message setMessage(String message) {
		this.message = message;
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
	
//	public String getCategoryPrefix() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("[").append(category.category);
//		if (prefix != null){
//			sb.append("/").append(prefix);
//		}
//		sb.append("]");
//		return sb.toString();
//	}
	
	public String getLogString() {
		return logString;
	}
	
	public void resetLogString() {
		logString = null;
	}
}
