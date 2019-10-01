package de.felixperko.fractals.system.systems.stateinfo;

import java.util.List;

import de.felixperko.fractals.system.numbers.ComplexNumber;

public class ComplexNumberListUpdate extends AbstractSharedDataUpdate {
	
	private static final long serialVersionUID = 4539082372221169789L;
	
	List<ComplexNumber> content;
	
	public ComplexNumberListUpdate(List<ComplexNumber> content) {
		this.content = content;
	}

	public List<ComplexNumber> getList() {
		return content;
	}
}
