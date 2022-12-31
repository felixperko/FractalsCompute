package de.felixperko.fractals.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class StringXMLSerializer extends AbstractParamXMLSerializer{

	public StringXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	protected void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) {
		checkStaticType(paramSupp, String.class);
		String val = paramSupp.getGeneral(String.class);
		setText(paramRootNode, val);
	}

}
