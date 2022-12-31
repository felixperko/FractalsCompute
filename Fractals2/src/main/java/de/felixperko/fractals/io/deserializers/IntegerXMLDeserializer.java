package de.felixperko.fractals.io.deserializers;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamValueType;

public class IntegerXMLDeserializer extends AbstractParamXMLDeserializer {

	public IntegerXMLDeserializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public Object deserializeContent(String content, ParamContainer container) {
		return Integer.valueOf(content);
	}

}
