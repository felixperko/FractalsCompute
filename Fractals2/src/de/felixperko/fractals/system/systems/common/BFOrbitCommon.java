package de.felixperko.fractals.system.systems.common;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;
import de.felixperko.fractals.system.parameters.ParameterDefinition;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.Selection;

public class BFOrbitCommon {
	
	public static ParamValueType integerType = new ParamValueType("integer");
	public static ParamValueType doubleType = new ParamValueType("double");
	public static ParamValueType booleanType = new ParamValueType("boolean");
	public static ParamValueType classType = new ParamValueType("class");
	public static ParamValueType listType = new ParamValueType("list");
	
	public static ParamValueType numberType = new ParamValueType("number");
	public static ParamValueType complexnumberType = new ParamValueType("complexnumber");
	
	public static ParamValueType numberfactoryType = new ParamValueType("numberfactory",
			new ParamValueField("numberClass", classType, DoubleNumber.class),
			new ParamValueField("complexNumberClass", classType, DoubleComplexNumber.class));
	
	public static ParamValueType arraychunkfactoryType = new ParamValueType("arraychunkfactory",
			new ParamValueField("chunkClass", classType, ReducedNaiveChunk.class),
			new ParamValueField("chunkSize", integerType, 200));
	
	public static ParamValueType selectionType = new ParamValueType("selection");
	
	
	public static ParameterConfiguration getCommonParameterConfiguration() {
		
		ParameterConfiguration parameterConfiguration = new ParameterConfiguration();
		
		ParamValueType[] types = new ParamValueType[] {
				integerType, doubleType, booleanType, classType, listType, numberType, complexnumberType,
				numberfactoryType, arraychunkfactoryType, selectionType
		};
		parameterConfiguration.addValueTypes(types);
		
		List<Class<? extends ParamSupplier>> varList = new ArrayList<>();
		varList.add(StaticParamSupplier.class);
		varList.add(CoordinateBasicShiftParamSupplier.class);

		List<ParameterDefinition> defs = new ArrayList<>();
		defs.add(new ParameterDefinition("iterations", "Basic", StaticParamSupplier.class, integerType)
				.withDescription("The maximum number of iterations until a sample is marked as unsuccessful.\n"
						+ "Increase to allow more black regions to be filled at the cost of slower computation."));
		defs.add(new ParameterDefinition("calculator", "Basic", StaticParamSupplier.class, selectionType)
				.withDescription("Choose the calculator to render different fractals."));
		
		defs.add(new ParameterDefinition("midpoint", "Position", StaticParamSupplier.class, complexnumberType)
				.withDescription("The current default coordinate center."));
		defs.add(new ParameterDefinition("numberFactory", "Position", StaticParamSupplier.class, numberfactoryType)
				.withDescription("Manages the used number and complex number class."));
		
		defs.add(new ParameterDefinition("chunkFactory", "Advanced", StaticParamSupplier.class, arraychunkfactoryType)
				.withDescription("Details about the chunks, e.g. the size of used chunks."));
		defs.add(new ParameterDefinition("systemName", "Advanced", StaticParamSupplier.class, selectionType)
				.withDescription("The internal calculation system to use for task management."));
		defs.add(new ParameterDefinition("view", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The current view to calculate for."));
		
		parameterConfiguration.addParameterDefinitions(defs);

		List<ParameterDefinition> mandelbrot_calculator_defs = new ArrayList<>();
		mandelbrot_calculator_defs.add(new ParameterDefinition("pow", "Calculator", varList, complexnumberType)
				.withDescription("The exponent parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: fields", "ui-element:plane soft-min=-2 soft-max=2"));
		mandelbrot_calculator_defs.add(new ParameterDefinition("c", "Calculator", varList, complexnumberType)
				.withDescription("The shift parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: plane soft-min=-2 soft-max=2", "ui-element:fields"));
		mandelbrot_calculator_defs.add(new ParameterDefinition("start", "Calculator", varList, complexnumberType)
				.withDescription("The input number parameter that is used for the first calculation step.")
				.withHints("ui-element[default]: plane soft-min=-2 soft-max=2", "ui-element:fields"));
		
		parameterConfiguration.addCalculatorParameters("MandelbrotCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("BurningShipCalculator", mandelbrot_calculator_defs);
		parameterConfiguration.addCalculatorParameters("TricornCalculator", mandelbrot_calculator_defs);
		
		List<ParameterDefinition> newton_calculator_defs = new ArrayList<>();
		newton_calculator_defs.add(new ParameterDefinition("start", "Calculator", CoordinateBasicShiftParamSupplier.class, complexnumberType)
				.withDescription("The start position for Newton's method."));
		
		parameterConfiguration.addCalculatorParameters("NewtonThridPowerMinusOneCalculator", newton_calculator_defs);									//TODO test -> newton_calculator_defs!
		parameterConfiguration.addCalculatorParameters("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", newton_calculator_defs);	//
		
		Selection<String> calculatorSelection = new Selection<>("calculator");
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator", "The famous Mandelbrot set.\n"
				+ "f(z_n+1) = f(z_n)^pow + c; z_0 = start");
		calculatorSelection.addOption("BurningShip", "BurningShipCalculator", "A variation of the Mandelbrot set.\n"
				+ "z = |a| + i*|b| before every step.");
		calculatorSelection.addOption("Tricorn", "TricornCalculator", "A variation of the Mandelbrot set.\n"
				+ "z = a - i*b before every step.");
		calculatorSelection.addOption("Newton x^3 - 1", "NewtonThridPowerMinusOneCalculator", "Newton's method for f(x) = x^3 - 1");
		calculatorSelection.addOption("Newton x^8 + 15x^4 - 16", "NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", "Newton's method for f(x) = x^8 + 15x^4 - 16");
		
		parameterConfiguration.addSelection(calculatorSelection);
		
		Selection<String> systemNameSelection = new Selection<>("systemName");
		systemNameSelection.addOption("BreadthFirstSystem", "BreadthFirstSystem", "The current default real time calculation system.");
		parameterConfiguration.addSelection(systemNameSelection);
		
		Selection<Class<? extends Number<?>>> numberClassSelection = new Selection<>("numberClass");
		numberClassSelection.addOption("double", DoubleNumber.class, "A wrapper for the Java double primitive.");
		parameterConfiguration.addSelection(numberClassSelection);
		
		Selection<Class<? extends ComplexNumber<?, ?>>> complexNumberClassSelection = new Selection<>("complexNumberClass");
		complexNumberClassSelection.addOption("double-complex", DoubleComplexNumber.class, "A complex number wrapping two Java double primitives.");
		parameterConfiguration.addSelection(complexNumberClassSelection);
		return parameterConfiguration;
	}
}
