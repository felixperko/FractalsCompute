package de.felixperko.io.deserializers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ExpressionsParam;
import de.felixperko.fractals.system.parameters.ParamValueType;

public class ExpressionsXMLDeserializer extends AbstractParamXMLDeserializer {

	public ExpressionsXMLDeserializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public Object deserializeContent(String content, ParamContainer container) {
		LinkedHashMap<String, String> pairs = new LinkedHashMap<>();
		String[] s = content.split(";");
		for (int i = 0 ; i < s.length ; i++) {
			String[] s2 = s[i].split("=");
			pairs.put(s2[0], s2[1]);
		}
		ExpressionsParam param = new ExpressionsParam(pairs);
		return param;
	}

}
