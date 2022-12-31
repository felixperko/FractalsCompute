package de.felixperko.fractals.io.serializers;

import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.felixperko.expressions.FractalsExpression;
import de.felixperko.fractals.system.parameters.ExpressionsParam;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public class ExpressionParamXMLSerializer extends AbstractParamXMLSerializer{

	public ExpressionParamXMLSerializer(ParamValueType valueType, double version) {
		super(valueType, version);
	}

	@Override
	public void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version) throws IllegalArgumentException {
		
		checkStaticType(paramSupp, ExpressionsParam.class);
		
		ExpressionsParam exprParam = (ExpressionsParam)paramSupp.getGeneral();

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry<String, String> e : exprParam.getExpressions().entrySet()) {
			if (!first)
				sb.append(";");
			else
				first = false;
			sb.append(e.getKey()+"="+e.getValue());
		}
//		expr.serialize(sb, true);
		
		setText(paramRootNode, sb.toString());
	}

}
