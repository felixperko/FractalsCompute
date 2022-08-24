package de.felixperko.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ComplexNumberXMLSerializer extends AbstractParamXMLSerializer {

	public ComplexNumberXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	protected void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) {
		checkStaticType(paramSupp, ComplexNumber.class);
		ComplexNumber num = paramSupp.getGeneral(ComplexNumber.class);
		setText(paramRootNode, num.toString());
	}

}
