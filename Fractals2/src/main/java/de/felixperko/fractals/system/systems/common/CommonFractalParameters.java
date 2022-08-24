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
import de.felixperko.fractals.system.parameters.suppliers.CoordinateDiscreteParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.CoordinateModuloParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.Selection;

public class CommonFractalParameters {
	
	public static final int DEFAULT_CHUNK_SIZE = 256;

	public static String PARAM_ZSTART = "z_0";
	public static String PARAMNAME_ZSTART = "z_0";
	
	public static String PARAM_EXPRESSIONS = "WNT14e";
	public static String PARAMNAME_EXPRESSIONS = "z_(n+1) = ";

	public static String PARAM_ITERATIONS = "_sZulJ";
	
	public static String PARAM_CALCULATOR = "_TqHam";
	
	public static String PARAM_MIDPOINT = "ZSJUao";

	public static String PARAM_NUMBERFACTORY = "juul7S";
	
	public static String PARAM_CHUNKFACTORY = "1xzYUd";
	public static String PARAM_SYSTEMNAME = "7A-_59";
	public static String PARAM_VIEW = "zqzAfz";
	public static String PARAM_POW = "BRXzAz";
	public static String PARAM_C = "qNXcrF";
	public static String PARAM_PRECISION = "FI8ZdL";
	

	public static String TYPEID_INTEGER = "iLF_Ww";
	public static String TYPEID_DOUBLE = "471nMe";
	public static String TYPEID_BOOLEAN = "U3B8Al";
	public static String TYPEID_STRING = "QBZKRJ";
	public static String TYPEID_CLASS = "4VzaX5";
	public static String TYPEID_LIST = "tOV6uh";
	public static String TYPEID_NUMBER = "09dY2p";
	public static String TYPEID_COMPLEXNUMBER = "QKC4rN";
	public static String TYPEID_NUMBERFACTORY = "owNzA_";
	public static String TYPEID_EXPRESSIONS = "F65uzh";
	public static String TYPEID_ARRAYCHUNKFACTORY = "6CH2hs";
	public static String TYPEID_SELECTION = "g1SGW0";
	
	public static ParamValueType integerType = new ParamValueType(TYPEID_INTEGER, "integer");
	public static ParamValueType doubleType = new ParamValueType(TYPEID_DOUBLE, "double");
	public static ParamValueType booleanType = new ParamValueType(TYPEID_BOOLEAN, "boolean");
	public static ParamValueType stringType = new ParamValueType(TYPEID_STRING, "string");
	public static ParamValueType classType = new ParamValueType(TYPEID_CLASS, "class");
	public static ParamValueType listType = new ParamValueType(TYPEID_LIST, "list");
	
	public static ParamValueType numberType = new ParamValueType(TYPEID_NUMBER, "number");
	public static ParamValueType complexnumberType = new ParamValueType(TYPEID_COMPLEXNUMBER, "complexnumber");
	
	public static ParamValueType numberfactoryType = new ParamValueType(TYPEID_NUMBERFACTORY, "numberfactory",
			new ParamValueField("numberClass", classType, DoubleNumber.class),
			new ParamValueField("complexNumberClass", classType, DoubleComplexNumber.class));
	
	public static ParamValueType expressionsType = new ParamValueType(TYPEID_EXPRESSIONS, "expressions");
	
	public static ParamValueType arraychunkfactoryType = new ParamValueType(TYPEID_ARRAYCHUNKFACTORY, "arraychunkfactory",
			new ParamValueField("chunkClass", classType, ReducedNaiveChunk.class),
			new ParamValueField("chunkSize", integerType, 200));
	
	public static ParamValueType selectionType = new ParamValueType(TYPEID_SELECTION, "selection");
	
	
	public static ParamConfiguration getCommonParameterConfiguration() {
		
		ParamConfiguration parameterConfiguration = new ParamConfiguration("F_yNf2", 1.0);
		NumberFactory nf = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		
		ParamValueType[] types = new ParamValueType[] {
				integerType, doubleType, booleanType, stringType, classType, listType,
				numberType, complexnumberType, numberfactoryType, arraychunkfactoryType, selectionType
		};
		parameterConfiguration.addValueTypes(types);
		
		List<Class<? extends ParamSupplier>> suppClasses = new ArrayList<>();
		suppClasses.add(StaticParamSupplier.class);
		suppClasses.add(CoordinateBasicShiftParamSupplier.class);
		suppClasses.add(CoordinateDiscreteParamSupplier.class);
		suppClasses.add(CoordinateModuloParamSupplier.class);

		List<ParamDefinition> defs = new ArrayList<>();
		List<ParamSupplier> defaultValues = new ArrayList<>();
		defs.add(new ParamDefinition(PARAM_ITERATIONS, "iterations", "Calculator", StaticParamSupplier.class, integerType, 1.0)
				.withDescription("The maximum number of iterations until a sample is marked as unsuccessful.\n"
						+ "Increase to allow more black regions to be filled at the cost of slower computation."));
		defs.add(new ParamDefinition(PARAM_CALCULATOR, "calculator", "Calculator", StaticParamSupplier.class, selectionType, 1.0)
				.withDescription("Choose the calculator to render different fractals."));
		defaultValues.add(new StaticParamSupplier(PARAM_CALCULATOR, "CustomCalculator"));
		
		defs.add(new ParamDefinition(PARAM_MIDPOINT, "midpoint", "Mapping", StaticParamSupplier.class, complexnumberType, 1.0)
				.withDescription("The current default coordinate center."));
		defs.add(new ParamDefinition(PARAM_NUMBERFACTORY, "numberFactory", "Mapping", StaticParamSupplier.class, numberfactoryType, 1.0)
				.withDescription("Manages the used number and complex number class."));
		defaultValues.add(new StaticParamSupplier(PARAM_NUMBERFACTORY, nf));
		
		defs.add(new ParamDefinition(PARAM_CHUNKFACTORY, "chunkFactory", "Advanced", StaticParamSupplier.class, arraychunkfactoryType, 1.0)
				.withDescription("Details about the chunks, e.g. the size of used chunks."));
		defaultValues.add(new StaticParamSupplier(PARAM_CHUNKFACTORY, new ArrayChunkFactory(ReducedNaiveChunk.class, DEFAULT_CHUNK_SIZE)));
		defs.add(new ParamDefinition(PARAM_SYSTEMNAME, "systemName", "Automatic", StaticParamSupplier.class, selectionType, 1.0)
				.withDescription("The internal calculation system to use for task management."));
		defaultValues.add(new StaticParamSupplier(PARAM_SYSTEMNAME, "BreadthFirstSystem"));
		defs.add(new ParamDefinition(PARAM_VIEW, "view", "Automatic", StaticParamSupplier.class, integerType, 1.0)
				.withDescription("The current view to calculate for."));
		defaultValues.add(new StaticParamSupplier(CommonFractalParameters.PARAM_VIEW, 1));
		
		parameterConfiguration.addParameterDefinitions(defs);
		parameterConfiguration.addDefaultValues(defaultValues);

		List<ParamDefinition> mandelbrot_calculator_defs = new ArrayList<>();
		mandelbrot_calculator_defs.add(new ParamDefinition(PARAM_POW, "pow", "Calculator", suppClasses, complexnumberType, 1.0)
				.withDescription("The exponent parameter that is applied at every calculation step.")
				.withHints("ui-element[default]: fields", "ui-element:plane soft-min=-2 soft-max=2"));
		mandelbrot_calculator_defs.add(new ParamDefinition(PARAM_C, "c", "Calculator", suppClasses, complexnumberType, 1.0)
				.withDescription("The shift parameter that is added at every calculation step.")
				.withHints("ui-element[default]:plane soft-min=-2 soft-max=2", "ui-element:fields"));
		mandelbrot_calculator_defs.add(new ParamDefinition(PARAM_ZSTART, PARAMNAME_ZSTART, "Calculator", suppClasses, complexnumberType, 1.0)
				.withDescription("The input number parameter that is used for the first calculation step.")
				.withHints("ui-element[default]:slider soft-min=-2 soft-max=2", "ui-element:fields"));

		List<ParamDefinition> custom_calculator_defs = new ArrayList<>();
		List<ParamSupplier> custom_calculator_defaults = new ArrayList<>();
		custom_calculator_defs.add(new ParamDefinition(PARAM_EXPRESSIONS, PARAMNAME_EXPRESSIONS, "Calculator", StaticParamSupplier.class, expressionsType, 1.0)
				.withDescription("The formula of the fractal"));
		custom_calculator_defs.add(new ParamDefinition(PARAM_PRECISION, "precision", "Calculator", StaticParamSupplier.class, selectionType, 1.0)
				.withDescription("Bits of precision. Increase if image becomes pixelated."));
		custom_calculator_defs.addAll(mandelbrot_calculator_defs);
		
		custom_calculator_defaults.add(new StaticParamSupplier(PARAM_PRECISION, "32"));
		custom_calculator_defaults.add(new StaticParamSupplier(PARAM_POW, nf.createComplexNumber(2.0, 0.0)));
		
		parameterConfiguration.addCalculatorParameters("MandelbrotCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("BurningShipCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("TricornCalculator", mandelbrot_calculator_defs, null);
		parameterConfiguration.addCalculatorParameters("CustomCalculator", custom_calculator_defs, custom_calculator_defaults);
		parameterConfiguration.addCalculatorParameters("FibonacciPowCalculator", mandelbrot_calculator_defs, null);
		
		List<ParamDefinition> newton_calculator_defs = new ArrayList<>();
		newton_calculator_defs.add(new ParamDefinition(PARAM_ZSTART, PARAMNAME_ZSTART, "Calculator", CoordinateBasicShiftParamSupplier.class, complexnumberType, 1.0)
				.withDescription("The start position for Newton's method."));
		
		parameterConfiguration.addCalculatorParameters("NewtonThridPowerMinusOneCalculator", newton_calculator_defs, null);									//TODO test -> newton_calculator_defs!
		parameterConfiguration.addCalculatorParameters("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", newton_calculator_defs, null);	//
		
		Selection<String> calculatorSelection = new Selection<>(PARAM_CALCULATOR);
		calculatorSelection.addOption("Mandelbrot", "MandelbrotCalculator",
				"The Mandelbrot set.\n"
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
		
		Selection<String> precisionSelection = new Selection<>(PARAM_PRECISION);
		precisionSelection.addOption("Auto", "Auto", "Uses default precision or a value based on the zoom level.");
		precisionSelection.addOption("32 bit", "32", "");
		precisionSelection.addOption("64 bit", "64", "");
		parameterConfiguration.addSelection(precisionSelection);
		return parameterConfiguration;
	}
}
