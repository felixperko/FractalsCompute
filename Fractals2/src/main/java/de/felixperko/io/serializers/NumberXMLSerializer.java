package de.felixperko.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class NumberXMLSerializer extends AbstractParamXMLSerializer {

	public NumberXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	protected void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) {
		checkStaticType(paramSupp, Number.class);
		Number num = paramSupp.getGeneral(Number.class);
		setText(paramRootNode, num.toString());
	}

}
