package de.felixperko.io.deserializers;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;

public class ComplexNumberXMLDeserializer extends AbstractParamXMLDeserializer {

	public ComplexNumberXMLDeserializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public Object deserializeContent(String content, ParamContainer container) {
		NumberFactory nf = container.getParam(CommonFractalParameters.PARAM_NUMBERFACTORY).getGeneral(NumberFactory.class);
		String[] parts = content.split(",");
		return nf.ccn(parts[0], parts[1]);
	}

}
