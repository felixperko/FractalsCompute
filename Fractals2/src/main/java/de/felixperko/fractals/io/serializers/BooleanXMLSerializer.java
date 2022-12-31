package de.felixperko.fractals.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class BooleanXMLSerializer extends AbstractParamXMLSerializer{

	public BooleanXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	protected void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) {
		checkStaticType(paramSupp, Boolean.class);
		Boolean val = paramSupp.getGeneral(Boolean.class);
		setText(paramRootNode, val ? "1" : "0");
	}

}
