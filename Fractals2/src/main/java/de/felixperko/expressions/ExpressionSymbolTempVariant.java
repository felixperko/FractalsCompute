package de.felixperko.expressions;

public class ExpressionSymbolTempVariant {
	
	String text;
	boolean modified;
	int useCounter;
	
	public ExpressionSymbolTempVariant(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	public void addUse() {
		useCounter++;
	}
	
	public void removeUse() {
		useCounter--;
	}
	
	public int getPendingUsages() {
		return useCounter;
	}
}
