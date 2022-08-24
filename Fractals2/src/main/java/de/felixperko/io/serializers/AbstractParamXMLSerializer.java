package de.felixperko.io.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;

public abstract class AbstractParamXMLSerializer {
	
	String paramTypeUid;
	String paramTypeName;
	double version;
	
	public AbstractParamXMLSerializer(ParamValueType valueType, double version) {
		this.paramTypeUid = valueType.getUID();
		this.paramTypeName = valueType.getName();
		this.version = version;
	}
	
	public boolean isApplicableByUID(String paramTypeUid) {
		return this.paramTypeUid.equals(paramTypeUid);
	}
	
	public boolean isApplicableByName(String paramTypeName) {
		return this.paramTypeName != null && this.paramTypeName.equals(paramTypeName);
	}
	
	public void serialize(Document document, Node parentNode, ParamSupplier paramSupp, double version) throws IllegalArgumentException{
//		Element paramRoot = document.createElement("p");
//		paramRoot.setAttribute("id", paramSupp.getUID());
		serializeContent(document, parentNode, paramSupp, version);
	}
	
	protected abstract void serializeContent(Document document, Node paramRootNode, ParamSupplier paramSupp, double version);

	public double getVersion() {
		return version;
	}

	public String getTypeId() {
		return paramTypeUid;
	}

	protected void checkStaticType(ParamSupplier paramSupp, Class<?> checkObjCls) {
		if (!(paramSupp instanceof StaticParamSupplier))
			throw new IllegalArgumentException("Serializer for type "+checkObjCls.getName()+" expects StaticParamSupplier as container");
		if (!checkObjCls.isInstance(paramSupp.getGeneral()))
			throw new IllegalArgumentException("Expressions can only be serialized from class "+checkObjCls.getName());
	}

	protected void setText(Node paramRootNode, String text) {
		paramRootNode.setTextContent(text);
	}
}
