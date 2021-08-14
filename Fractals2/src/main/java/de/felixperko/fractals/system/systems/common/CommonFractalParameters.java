package de.felixperko.fractals.system.systems.common;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.ParamValueField;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateBasicShiftParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.Selection;

public class CommonFractalParameters {
	
	public static final int DEFAULT_CHUNK_SIZE = 256;
	
	public static String PARAM_ZSTART = "z_0";
	public static String PARAM_EXPRESSIONS = "z_(n+1) = ";
	
	public static ParamValueType integerType = new ParamValueType("integer");
	public static ParamValueType doubleType = new ParamValueType("double");
	public static ParamValueType booleanType = new ParamValueType("boolean");
	public static ParamValueType stringType = new ParamValueType("string");
	public static ParamValueType classType = new ParamValueType("class");
	public static ParamValueType listType = new ParamValueType("list");
	
	public static ParamValueType numberType = new ParamValueType("number");
	public static ParamValueType complexnumberType = new ParamValueType("complexnumber");
	
	public static ParamValueType numberfactoryType = new ParamValueType("numberfactory",
			new ParamValueField("numberClass", classType, DoubleNumber.class),
			new ParamValueField("complexNumberClass", classType, DoubleComplexNumber.class));
	
	public static ParamValueType expressionsType = new ParamValueType("expressions");
	
	public static ParamValueType arraychunkfactoryType = new ParamValueType("arraychunkfactory",
			new ParamValueField("chunkClass", classType, ReducedNaiveChunk.class),
			new ParamValueField("chunkSize", integerType, 200));
	
	public static ParamValueType selectionType = new ParamValueType("selection");
	
	
	public static ParamConfiguration getCommonParameterConfiguration() {
		
		ParamConfiguration parameterConfiguration = new ParamConfiguration();
		NumberFactory nf = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		
		ParamValueType[] types = new ParamValueType[] {
				integerType, doubleType, booleanType, stringType, classType, listType,
				numberType, complexnumberType, numberfactoryType, arraychunkfactoryType, selectionType
		};
		parameterConfiguration.addValueTypes(types);
		
		List<Class<? extends ParamSupplier>> varList = new ArrayList<>();
		varList.add(StaticParamSupplier.class);
		varList.add(CoordinateBasicShiftParamSupplier.class);

		List<ParamDefinition> defs = new ArrayList<>();
		List<ParamSupplier> defaultValues = new ArrayList<>();
		defs.add(new ParamDefinition("iterations", "Calculator", StaticParamSupplier.class, integerType)
				.withDescription("The maximum number of iterations until a sample is marked as unsuccessful.\n"
						+ "Increase to allow more black regions to be filled at the cost of slower computation."));
		defs.add(new ParamDefinition("calculator", "Calculator", StaticParamSupplier.class, selectionType)
				.withDescription("Choose the calculator to render different fractals."));
		defaultValues.add(new StaticParamSupplier("calculator", "CustomCalculator"));
		
		defs.add(new ParamDefinition("midpoint", "Position", StaticParamSupplier.class, complexnumberType)
				.withDescription("The current default coordinate center."));
		defs.add(new ParamDefinition("numberFactory", "Position", StaticParamSupplier.class, numberfactoryType)
				.withDescription("Manages the used number and complex number class."));
		defaultValues.add(new StaticParamSupplier("numberFactory", nf));
		
		defs.add(new ParamDefinition("chunkFactory", "Advanced", StaticParamSupplier.class, arraychunkfactoryType)
				.withDescription("Details about the chunks, e.g. the size of used chunks."));
		defaultValues.add(new StaticParamSupplier("chunkFactory", new ArrayChunkFactory(ReducedNaiveChunk.class, DEFAULT_CHUNK_SIZE)));
		defs.add(new ParamDefinition("systemName", "Automatic", StaticParamSupplier.class, selectionType)
				.withDescription("The internal calculation system to use for task management."));
		defaultValues.add(new StaticParamSupplier("systemName", "BreadthFirstSystem"));
		defs.add(new ParamDefinition("view", "Automatic", StaticParamSupplier.class, integerType)
				.withDescription("The current view to calculate for."));
		defaultValues.add(new StaticParamSupplier("view", 1));
		
		parameterConfiguration.addParameterDefinitions(defs);
		parameterConfiguration.addDefaultValues(defaultValues);

		List<ParamDefinition> mandelbrot_calculator_defs = new ArrayList<>();
		mandelbrot_calculator_defs.add(new ParamDefinition("pow", "Calculator", varList, complexnumberType)
				.withDescription("The exponent parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: fields", "ui-element:plane soft-min=-2 soft-max=2"));
		mandelbrot_calculator_defs.add(new ParamDefinition("c", "Calculator", varList, complexnumberType)
				.withDescription("The shift parameter that is applied at every calculation step.")
				.withHints("ui-element[default]:plane soft-min=-2 soft-max=2", "ui-element:fields"));
		mandelbrot_calculator_defs.add(new ParamDefinition(CommonFractalParameters.PARAM_ZSTART, "Calculator", varList, complexnumberType)
				.withDescription("The input number parameter that is used for the first calculation step.")
				.withHints("ui-element[default]:plane soft-min=-2 soft-max=2", "ui-element:fields"));

		List<ParamDefinition> custom_calculator_defs = new ArrayList<>();
		List<ParamSupplier> custom_calculator_defaults = new ArrayList<>();
		custom_calculator_defs.add(new ParamDefinition(CommonFractalParameters.PARAM_EXPRESSIONS, "Calculator", StaticParamSupplier.class, expressionsType)
				.withDescription("The formula of the fractal"));
		custom_calculator_defs.add(new ParamDefinition("precision", "Calculator", StaticParamSupplier.class, selectionType)
				.withDescription("Bits of precision. Increase if image becomes pixelated."));
		custom_calculator_defs.addAll(mandelbrot_calculator_defs);
		
		custom_calculator_defaults.add(new StaticParamSupplier("precision", "32"));
		custom_calculator_defaults.add(new StaticParamSupplier("pow", nf.createComplexNumber(2.0, 0.0)));
		
		parameterConfiguration.addCalculatorParameters("MandelbrotCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("BurningShipCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("TricornCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("CustomCalculator", custom_calculator_defs, custom_calculator_defaults);
		parameterConfiguration.addCalculatorParameters("FibonacciPowCalculator", mandelbrot_calculator_defs, null);
		
		List<ParamDefinition> newton_calculator_defs = new ArrayList<>();
		newton_calculator_defs.add(new ParamDefinition(CommonFractalParameters.PARAM_ZSTART, "Calculator", CoordinateBasicShiftParamSupplier.class, complexnumberType)
				.withDescription("The start position for Newton's method."));
		
		parameterConfiguration.addCalculatorParameters("NewtonThridPowerMinusOneCalculator", newton_calculator_defs, null);									//TODO test -> newton_calculator_defs!
		parameterConfiguration.addCalculatorParameters("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", newton_calculator_defs, null);	//
		
		Selection<String> calculatorSelection = new Selection<>("calculator");
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator",
				"The famous Mandelbrot set.\n"
				+ "f(z_n+1) = f(z_n)^pow + c; z_0 = start");
		calculatorSelection.addOption("BurningShip", "BurningShipCalculator",
				"A variation of the Mandelbrot set.\n"
				+ "z = |a| + i*|b| before every step.");
		calculatorSelection.addOption("Tricorn", "TricornCalculator",
				"A variation of the Mandelbrot set.\n"
				+ "z = a - i*b before every step.");
		calculatorSelection.addOption("Custom", "CustomCalculator",
				"Enter a custom formula.");
		calculatorSelection.addOption("Fibonacci", "FibonacciPowCalculator",
				"The fibonacci number of the iteration is used as the exponent.\n"
				+ "f(z_n+1) = f(z_n) + (1+1/z)^fib + c");
		calculatorSelection.addOption("Newton x^3 - 1", "NewtonThridPowerMinusOneCalculator",
				"Newton's method for f(x) = x^3 - 1");
		calculatorSelection.addOption("Newton x^8 + 15x^4 - 16", "NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator",
				"Newton's method for f(x) = x^8 + 15x^4 - 16");
		
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
		
		Selection<String> precisionSelection = new Selection<>("precision");
		precisionSelection.addOption("Auto", "Auto", "Uses default precision or a value based on the zoom level.");
		precisionSelection.addOption("32 bit", "32", "");
		precisionSelection.addOption("64 bit", "64", "");
		parameterConfiguration.addSelection(precisionSelection);
		return parameterConfiguration;
	}
}
