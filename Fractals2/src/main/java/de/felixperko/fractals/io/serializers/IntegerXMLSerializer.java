package de.felixperko.fractals.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class IntegerXMLSerializer extends AbstractParamXMLSerializer{

	public IntegerXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	protected void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) {
		checkStaticType(paramSupp, Integer.class);
		Integer val = paramSupp.getGeneral(Integer.class);
		setText(paramRootNode, String.valueOf(val));
	}
	
}
