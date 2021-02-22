package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.system.calculator.ComputeInstruction;

public class ExpressionSymbol {
	
	private String name;
	
	private boolean outputVar;
	
	private List<Boolean> slotsUsed = new ArrayList<>();
	private List<Boolean> slotReal = new ArrayList<>();
	private List<Boolean> slotImag = new ArrayList<>();
	private int slotsCounter = 0;
	
	private int occurences = 0;
	private int occurencesReal = 0;
	private int occurencesImag = 0;
	
	private int pristineIndex;
	private int[] copyIndices;
	
	private boolean copiesAdded = false;
	
	public ExpressionSymbol(String name, boolean outputVar){
		this.name = name;
		this.outputVar = outputVar;
	}
	
	public void addOccurence(){
		occurences++;
	}
	
	public void addOccurenceReal(){
		occurencesReal++;
	}
	
	public void addOccurenceImag(){
		occurencesImag++;
	}
	
	public boolean removeOccurenceAndIsLast(){
		occurences--;
		return occurences == 0 && occurencesReal == 0 && occurencesImag == 0;
	}
	
	public boolean removeOccurenceRealAndIsLast(){
		occurencesReal--;
		return occurences == 0 && occurencesReal == 0;
	}
	
	public boolean removeOccurenceImagAndIsLast(){
		occurencesImag--;
		return occurences == 0 && occurencesImag == 0;
	}
	
	public int getSlot(boolean real, boolean imag){
		for (int i = occurences > 0 ? 1 : 0 ; i < slotsUsed.size() ; i++){
			if (!slotsUsed.get(i)){
				slotsUsed.set(i, true);
				return i;
			}
		}
		
		int slot = slotsCounter++;
		slotReal.add(real);
		slotImag.add(imag);
		return slot;
	}
	
	public void freeVarSlot(int varSlot){
		this.slotsUsed.set(varSlot, false);
	}
	
	public int assignPristineIndex(int pristineIndex){
		this.pristineIndex = pristineIndex;
		return pristineIndex+2;
	}
	
	public int assignCopyIndices(int copyCounter){
		this.copyIndices = new int[slotsCounter];
		int startCopiesIndex = outputVar ? 1 : 0;
		for (int i = startCopiesIndex ; i < slotsCounter ; i++){
			copyIndices[i] = copyCounter;
			if (slotReal.get(i))
				copyCounter++;
			if (slotImag.get(i))
				copyCounter++;
		}
		return copyCounter;
	}

	public int getIndexReal(int symbolSlot) {
		if (symbolSlot < 0)
			return getPristineIndexReal();
		boolean pristine = outputVar;
		if (pristine){
			for (int i = 0 ; i < symbolSlot ; i++){
				if (slotReal.get(i)){
					pristine = false;
					break;
				}
			}
			if (pristine)
				return getPristineIndexReal();
		}
		return getCopyIndexReal(symbolSlot);
	}

	public int getIndexImag(int symbolSlot) {
		if (symbolSlot < 0)
			return getPristineIndexImag();
		boolean pristine = outputVar;
		if (pristine){
			for (int i = 0 ; i < symbolSlot ; i++){
				if (slotImag.get(i)){
					pristine = false;
					break;
				}
			}
			if (pristine)
				return getPristineIndexImag();
		}
		return getCopyIndexImag(symbolSlot);
	}
	
//	public void initCopiesIfNeeded(List<GPUInstruction> instructions){
//		if (copiesAdded)
//			return;
//		for (int i = 0 ; i < copyIndices.length ; i++){
//			int idxReal = copyIndices[i];
//			if (idxReal != pristineIndex)
//				instructions.add(new GPUInstruction(GPUInstruction.INSTR_COPY_COMPLEX, pristineIndex, pristineIndex+1, idxReal, idxReal+1));
//		}
//		copiesAdded = true;
//	}

	public int getPristineIndexReal() {
		return pristineIndex;
	}

	public int getPristineIndexImag() {
		return pristineIndex+1;
	}

	public int getCopyIndexReal(int symbolSlot) {
		return copyIndices[symbolSlot];
	}

	public int getCopyIndexImag(int symbolSlot) {
		return copyIndices[symbolSlot]+1;
	}

	public String getName() {
		return name;
	}
}
