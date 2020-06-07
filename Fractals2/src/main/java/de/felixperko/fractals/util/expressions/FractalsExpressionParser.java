package de.felixperko.fractals.util.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static de.felixperko.fractals.system.calculator.GPUInstruction.*;

public class FractalsExpressionParser {
	
	final static Map<String, Integer> singleFunctionExpressions = new HashMap<String, Integer>(){
		{
			put("", -1);
			put("sin", INSTR_SIN);
			put("cos", INSTR_COS);
			put("tan", INSTR_TAN);
			put("sinh", INSTR_SINH);
			put("cosh", INSTR_COSH);
			put("tanh", INSTR_TANH);
			put("abs", INSTR_ABS);
			put("conj", INSTR_CONJ);
		}
	};
	
	public static FractalsExpression parse(String input){
		
		input = input.trim();
		
		if (input == "")
			input = "0";
		
		LinkedHashMap<String, Integer> dashParts = new LinkedHashMap<>(); //split at '+', '-'
		LinkedHashMap<String, Integer> pointParts = new LinkedHashMap<>(); //split at '*', '/'
		List<String> expParts = new ArrayList<>(); //split at '^'
		int bracketLayer = 0;
		int startDashPart = 0;
		int startPointPart = 0;
		int startExpPart = 0;
		for (int i = 0 ; i < input.length() ; i++){
			char c = input.charAt(i);
			if (c == '('){
				bracketLayer++;
				continue;
			}
			if (bracketLayer > 0){ //ignore characters inside a bracket
				if (c == ')'){
					bracketLayer--;
				}
			} else {
				switch (c){
				case '+': 
					dashParts.put(input.substring(startDashPart, i), INSTR_ADD);
					startDashPart = i+1;
					break;
				case '-': 
					dashParts.put(input.substring(startDashPart, i), INSTR_SUB);
					startDashPart = i+1;
					break;
				case '*':
					pointParts.put(input.substring(startPointPart, i), INSTR_ADD);
					startPointPart = i+1;
					break;
				case '/':
					pointParts.put(input.substring(startPointPart, i), INSTR_ADD);
					startPointPart = i+1;
					break;
				case '^':
					if (expParts.size() == 0){
						expParts.add(input.substring(startExpPart, i));
						startExpPart = i+1;
					}
					break;
				}
			}
		}
		
		if (dashParts.size() > 0){
			dashParts.put(input.substring(startDashPart), null); //add remainder
			LinkedHashMap<FractalsExpression, Integer> dashPartExpressions = new LinkedHashMap<>(); //subexpression, operator
			for (Entry<String, Integer> e : dashParts.entrySet()){
				FractalsExpression dashSubExpression = parse(e.getKey());
				if (dashSubExpression == null)
					throw new IllegalArgumentException("Couldn't parse '"+e.getKey()+"'");
				dashPartExpressions.put(dashSubExpression, e.getValue());
			}
			return new SumExpression(dashPartExpressions);
		}
//		
//		else if (pointParts.size() > 0){
//			pointParts.put(input.substring(startPointPart), null); //add remainder
//			LinkedHashMap<FractalsExpression, Integer> pointPartExpressions = new LinkedHashMap<>(); //subexpression, operator
//			for (Entry<String, Integer> e : pointParts.entrySet()){
//				FractalsExpression dashSubExpression = parse(e.getKey());
//				pointPartExpressions.put(dashSubExpression, e.getValue());
//			}
//			return new FactorExpression(pointPartExpressions);
//		}
		
//		else
		if (expParts.size() > 0){
			if (expParts.size() == 1){
				FractalsExpression baseExpr = parse(expParts.get(0));
				FractalsExpression expExpr = parse(input.substring(startExpPart));
				if (baseExpr == null)
					throw new IllegalArgumentException("Couldn't parse '"+expParts.get(0)+"'");
				if (expExpr == null)
					throw new IllegalArgumentException("Couldn't parse '"+input.substring(startExpPart)+"'");
				return new ExpExpression(baseExpr, expExpr);
			} else {
				throw new IllegalArgumentException("Parsed multiple ^ unexpectedly.");
			}
		}
		
		//split at layer 0 brackets (...)
		List<String> absParts = splitBrackets(input, '(', ')');
		if (absParts != null && absParts.size() > 0){
			if (absParts.size() == 2){
				String prefix = absParts.get(0).trim();
				String bracketContent = absParts.get(1);
				for (String singleFunctionPrefix : singleFunctionExpressions.keySet()){
					if (prefix.equalsIgnoreCase(singleFunctionPrefix)){
						FractalsExpression subExpr = parse(bracketContent);
						if (subExpr == null)
							throw new IllegalArgumentException("Couldn't parse '"+bracketContent+"'");
						return new NestedExpression(subExpr, singleFunctionExpressions.get(singleFunctionPrefix));
					}
				}
//				if (prefix.equalsIgnoreCase("re") || prefix.equalsIgnoreCase("real")){
//					FractalsExpression subExpr = parse(bracketContent);
//					if (subExpr == null)
//						return null;
//					return new VariablePartExpression(subExpr, true, false);
//				}
//				if (prefix.equalsIgnoreCase("im") || prefix.equalsIgnoreCase("imag") || prefix.equalsIgnoreCase("imaginary")){
//					FractalsExpression subExpr = parse(bracketContent);
//					if (subExpr == null)
//						return null;
//					return new VariablePartExpression(subExpr, false, true);
//				}
			} else
				throw new IllegalArgumentException("Tried to parse ...(...), but has suffix");
		}
		
		//try double constant
		Double val = null;
		try {
			val = Double.parseDouble(input);
			return new ConstantExpression(val, 0);
		} catch (NumberFormatException e){
			boolean imag = input.endsWith("i");
			if (imag) {
				try {
					if (input.length() == 1) //input == 'i'
						val = 1.;
					else
						val = Double.parseDouble(input.substring(0, input.length()-2).trim());
					return new ConstantExpression(0, val);
				} catch (NumberFormatException e2){
				}
			}
		}
		
		//try variable
		if (val == null){
//			if (input.matches("[a-zA-Z]+_n(-[1-9][0-9]*)")){
//				return new VariablePastIterationExpression(input);
//			}
			if (input.matches("[a-zA-Z]+")){
				return new VariableExpression(input);
			}
		}

//		int index = 0;
//		if (input.charAt(index) == '|'){
//			for (index = 1 ; index < input.length() ; index++){
//				char c = input.charAt(index);
//				if (c == '|')
//					leftPart = new AbsExpression(parse(input.substring(1, index-1)));
//			}
//		}
//		
//		List<String> bracketParts = splitBrackets(input, '(', ')');
		
		
		return null;
	}
	
	/**
	 * returns split results for given bracket characters.
	 * 0: before bracket #1
	 * 1: bracket #1 content
	 * 2: before bracket #2
	 * 3: bracket #2 content
	 * 4: before bracket #3
	 * ...
	 * 
	 * @param input
	 * @param leftBracket
	 * @param rightBracket
	 * @return
	 */
	private static List<String> splitBrackets(String input, char leftBracket, char rightBracket){
		List<String> bracketTerms = new ArrayList<>();
		int bracketLayer = 0;
		int currIndex = 0;
		
		int startBracketContentIndex = 0;
		for (int i = 0 ; i < input.length() ; i++){
			char c = input.charAt(i);
			if (bracketLayer > 0){
				if (c == leftBracket)
					bracketLayer++;
				else if (c == rightBracket){
					bracketLayer--;
					if (bracketLayer == 0) //bracket term finished
						bracketTerms.add(input.substring(startBracketContentIndex, currIndex));
				}
				currIndex++;
				continue;
			}
			if (c == leftBracket){
				bracketLayer++;
				//add before bracket term
				bracketTerms.add(input.substring(startBracketContentIndex, currIndex));
				startBracketContentIndex = currIndex+1;
			}
			if (c == rightBracket) //invalid closing brackets
				return null;
			currIndex++;
		}
		return bracketTerms;
	}
	
//	private static FractalsExpression tryPolynom(String input){
//		//factor*x^b + c*x^d
//		String[] summands = input.split("\\+");
//		Map<String, Boolean> dashParts = new HashMap<>(); //dashPart, negative
//		for (int i = 0 ; i < summands.length ; i++){
//			String summand = summands[i];
//			String[] substractions = summand.split("-");
//			if (substractions.length == 1)
//				dashParts.put(summand, false);
//			for (String substraction : substractions){
//				dashParts.put(substraction, true);
//			}
//		}
//		
//	}
}
