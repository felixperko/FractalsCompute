package de.felixperko.fractals.io.deserializers;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamValueType;

public class BooleanXMLDeserializer extends AbstractParamXMLDeserializer {

	public BooleanXMLDeserializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public Object deserializeContent(String content, ParamContainer container) {
		try {
			return Integer.parseInt(content) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
