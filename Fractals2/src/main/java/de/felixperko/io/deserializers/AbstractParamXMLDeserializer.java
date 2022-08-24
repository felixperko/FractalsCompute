package de.felixperko.io.deserializers;

import org.w3c.dom.Node;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.io.serializers.AbstractParamXMLSerializer;

public abstract class AbstractParamXMLDeserializer {
	
	String paramTypeUid;
	String paramTypeName;
	double version;
	
	public AbstractParamXMLDeserializer(ParamValueType valueType, double version) {
		this.paramTypeUid = valueType.getUID();
		this.paramTypeName = valueType.getName();
		this.version = version;
	}
	
	public Object deserialize(Node paramNode, ParamContainer container, ParamConfiguration config) {
		String text = paramNode.getTextContent();
		return deserializeContent(text, container);
	}
	
	public abstract Object deserializeContent(String content, ParamContainer container);

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public String getParamTypeUid() {
		return paramTypeUid;
	}

	public String getParamTypeName() {
		return paramTypeName;
	}
}
