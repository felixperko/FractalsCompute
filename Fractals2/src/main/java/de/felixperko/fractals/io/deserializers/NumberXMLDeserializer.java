package de.felixperko.fractals.io.deserializers;

import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;

public class NumberXMLDeserializer extends AbstractParamXMLDeserializer {

	public NumberXMLDeserializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public Object deserializeContent(String content, ParamContainer container) {
		NumberFactory nf = container.getParam(CommonFractalParameters.PARAM_NUMBERFACTORY).getGeneral(NumberFactory.class);
		Number n = nf.cn(content);
		return n;
	}

}
