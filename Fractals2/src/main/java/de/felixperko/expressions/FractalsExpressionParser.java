package de.felixperko.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static de.felixperko.fractals.system.calculator.ComputeInstruction.*;

public class FractalsExpressionParser {
	
	static LinkedHashMap<String, Integer> complexFunctionExpressions = new LinkedHashMap<String, Integer>(){
		{
			put("", -1);
			put("sin", INSTR_SIN_COMPLEX);
			put("cos", INSTR_COS_COMPLEX);
			put("tan", INSTR_TAN_COMPLEX);
			put("sinh", INSTR_SINH_COMPLEX);
			put("cosh", INSTR_COSH_COMPLEX);
			put("tanh", INSTR_TANH_COMPLEX);
			put("abs", INSTR_ABS_COMPLEX);
			put("negate", INSTR_NEGATE_COMPLEX);
			put("log", INSTR_LOG_COMPLEX);
			//put("conj", INSTR_NEGATE_PART);//TODO which part?!
		}
	};
	static LinkedHashMap<String, Integer> singleFunctionExpressions = new LinkedHashMap<String, Integer>(){
		{
			put("", -1);
			put("sin", INSTR_SIN_PART);
			put("cos", INSTR_COS_PART);
			put("tan", INSTR_TAN_PART);
			put("sinh", INSTR_SINH_PART);
			put("cosh", INSTR_COSH_PART);
			put("tanh", INSTR_TANH_PART);
			put("abs", INSTR_ABS_PART);
			put("negate", INSTR_NEGATE_PART);
			put("log", INSTR_LOG_PART);
		}
	};
	
	public static String getInstructionName(int instr) {
		for (Entry<String, Integer> e : complexFunctionExpressions.entrySet()) {
			if (e.getValue() == instr)
				return e.getKey();
		}
		for (Entry<String, Integer> e : singleFunctionExpressions.entrySet()) {
			if (e.getValue() == instr)
				return e.getKey();
		}
		return null;
	}
	
	public static FractalsExpression parse(String input) throws IllegalArgumentException, IllegalStateException{
		
		input = input.trim();
		
		if (input.length() == 0)
			input = "0";
		
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
						val = Double.parseDouble(input.substring(0, input.length()-1).trim());
					return new ConstantExpression(0, val);
				} catch (NumberFormatException e2){
				}
			}
		}
		
		//try variable
		if (val == null){
			if (input.matches("[a-zA-Z]+_\\(n-1\\)")){
				return new VariablePastIterationExpression(input);
			}
			try {
				//TODO remove risk of stack overflow, not sure under which conditions it occurs, but it has something to do with the '*'...
				if (input.matches("[a-zA-Z]([a-zA-Z0-9]*)(_n)?")){
					if (input.equalsIgnoreCase("pi"))
						return new ConstantExpression(Math.PI, 0, "CON_pi");
					if (input.equalsIgnoreCase("e"))
						return new ConstantExpression(Math.E, 0, "CON_e");
					return new VariableExpression(input);
				}
			} catch (StackOverflowError e) {
				System.err.println("Stackoverflow while parsing expression");
				System.err.println(e.getMessage());
				throw new IllegalStateException("Parse error: Stackoverflow");
			}
		}
		
		if (input.startsWith("-")){
			FractalsExpression subExpr = parse(input.substring(1));
			return new NegateExpression(subExpr);
		}

		List<String> dashParts = new ArrayList<>(); //split at '+', '-'
		List<Integer> dashPartSingleInstructions = new ArrayList<>();
		List<Integer> dashPartComplexInstructions = new ArrayList<>();
		List<String> pointParts = new ArrayList<>(); //split at '*', '/'
		List<Integer> pointPartSingleInstructions = new ArrayList<>();
		List<Integer> pointPartComplexInstructions = new ArrayList<>();
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
					dashParts.add(input.substring(startDashPart, i));
					dashPartSingleInstructions.add(INSTR_ADD_PART);
					dashPartComplexInstructions.add(INSTR_ADD_COMPLEX);
					startDashPart = i+1;
					break;
				case '-': 
					dashParts.add(input.substring(startDashPart, i));
					dashPartSingleInstructions.add(INSTR_SUB_PART);
					dashPartComplexInstructions.add(INSTR_SUB_COMPLEX);
					startDashPart = i+1;
					break;
				case '*':
					pointParts.add(input.substring(startPointPart, i));
					pointPartSingleInstructions.add(INSTR_MULT_PART);
					pointPartComplexInstructions.add(INSTR_MULT_COMPLEX);
					startPointPart = i+1;
					break;
				case '/':
					pointParts.add(input.substring(startPointPart, i));
					pointPartSingleInstructions.add(INSTR_DIV_PART);
					pointPartComplexInstructions.add(INSTR_DIV_COMPLEX);
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
			//add remainder
			dashParts.add(input.substring(startDashPart));
			
			List<FractalsExpression> dashPartExpressions = new ArrayList<>();
			for (String subExprString : dashParts){
				FractalsExpression dashSubExpression = parse(subExprString);
				if (dashSubExpression == null)
					throw new IllegalArgumentException("Couldn't parse '"+subExprString+"'");
				dashPartExpressions.add(dashSubExpression);
			}
			return new ChainExpression(dashPartExpressions, dashPartSingleInstructions, dashPartComplexInstructions);
		}
//		
		else if (pointParts.size() > 0){
			//add remainder
			pointParts.add(input.substring(startPointPart)); 
			
			List<FractalsExpression> pointPartExpressions = new ArrayList<>();
			for (String pointPart : pointParts){
				pointPartExpressions.add(parse(pointPart));
			}
			return new MultExpression(pointPartExpressions, pointPartSingleInstructions, pointPartComplexInstructions);
		}
		
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
				FractalsExpression subExpr = parse(bracketContent);
				if (subExpr == null)
					throw new IllegalArgumentException("Couldn't parse '"+bracketContent+"'");
				
				if (prefix.equalsIgnoreCase("re") || prefix.equalsIgnoreCase("real")){
					if (subExpr == null)
						return null;
					return new VariablePartExpression(bracketContent, true, false);
				}
				if (prefix.equalsIgnoreCase("im") || prefix.equalsIgnoreCase("imag") || prefix.equalsIgnoreCase("imaginary")){
					if (subExpr == null)
						return null;
					return new VariablePartExpression(bracketContent, false, true);
				}
				
				Map<String, Integer> prefixMapping = subExpr.isComplexExpression() ? complexFunctionExpressions : singleFunctionExpressions;
				for (String functionPrefix : prefixMapping.keySet()){
					if (prefix.equalsIgnoreCase(functionPrefix)){
						return new NestedExpression(subExpr, singleFunctionExpressions.get(functionPrefix), complexFunctionExpressions.get(functionPrefix));
					}
				}
				
				boolean iPartCandidate = prefix.endsWith("i") && prefix.length() > 1;
				boolean rPartCandidate = prefix.endsWith("r") && prefix.length() > 1;
				if (iPartCandidate || rPartCandidate){
					prefix = prefix.substring(0, prefix.length()-1); //clear function name of part information
					for (String functionPrefix : prefixMapping.keySet()){
						if (prefix.equalsIgnoreCase(functionPrefix)){
							NestedExpression nestedExpression = new NestedExpression(subExpr, singleFunctionExpressions.get(functionPrefix),
									complexFunctionExpressions.get(functionPrefix));
							nestedExpression.setComplexFilterOutReal(iPartCandidate);
							nestedExpression.setComplexFilterOutImag(rPartCandidate);
							return nestedExpression;
						}
					}
				}
			} else
				throw new IllegalArgumentException("Tried to parse ...(...), but has suffix");
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
		
		throw new IllegalArgumentException("No parsable expression found");
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
}
