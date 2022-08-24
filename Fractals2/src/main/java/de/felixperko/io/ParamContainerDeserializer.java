package de.felixperko.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.io.deserializers.AbstractParamXMLDeserializer;

public class ParamContainerDeserializer {
	
	static Logger LOG = LoggerFactory.getLogger(ParamContainerDeserializer.class);
	
	ClassKeyRegistry typeRegistry;
	ParamXMLDeserializerRegistry deserializerRegistry;
	
	public ParamContainerDeserializer(ClassKeyRegistry typeRegistry,
			ParamXMLDeserializerRegistry deserializerRegistry) {
		this.typeRegistry = typeRegistry;
		this.deserializerRegistry = deserializerRegistry;
	}

	public ParamContainer deserialize(byte[] data, ParamConfiguration config, ParamContainer baseContainer, boolean failOnParamError) {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return deserialize(bais, config, baseContainer, failOnParamError);
	}
	
	public ParamContainer deserialize(InputStream is, ParamConfiguration config, ParamContainer baseContainer, boolean failOnParamError) {
		try {
			DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			
			NodeList root = doc.getElementsByTagName("params");
			
			Node containerNode = root.item(0);
			if (containerNode == null)
				return null;
			
			ParamContainer container;
			if (baseContainer == null)
				container = new ParamContainer(config);
			else
				container = new ParamContainer(baseContainer, true);
			
			NodeList paramNodes = containerNode.getChildNodes();
			for (int i = 0 ; i < paramNodes.getLength() ; i++) {
				Node paramNode = paramNodes.item(i);
				if (!"p".equals(paramNode.getNodeName()))
					continue;
				NamedNodeMap attrs = paramNode.getAttributes();
				Node uidNode = attrs.getNamedItem("id");
				Node typeNode = attrs.getNamedItem("type");
				
				String uid = uidNode.getTextContent();
				String suppType = typeNode.getTextContent();
				
				Class<? extends ParamSupplier> cls = typeRegistry.getClass(suppType);
				if (cls == null) {
					handleError(failOnParamError, "Class for supplier type name not found: "+suppType);
					continue;
				}
				
				ParamDefinition def = config.getParamDefinitionByUID(uid);
				if (def == null) {
					handleError(failOnParamError, "Parameter not defined for uid: "+uid);
					continue;
				}
				
				AbstractParamXMLDeserializer deserializer = deserializerRegistry.getSerializer(def.getValueType().getUID(), def.getValueTypeVersion());
				if (deserializer == null) {
					handleError(failOnParamError, "No deserializer registered for ValueType uid="+def.getValueType()+" requested version="+def.getValueTypeVersion());
					continue;
				}
				
				if (cls.isAssignableFrom(StaticParamSupplier.class)) {
					Object obj = deserializer.deserialize(paramNode, container, config);
					StaticParamSupplier supp = new StaticParamSupplier(uid, obj);
					container.addParam(supp);
				}
				else {
					Constructor<? extends ParamSupplier> constr = cls.getConstructor(String.class);
					ParamSupplier supp = constr.newInstance(uid);
					container.addParam(supp);
				}
			}
			
			//add defaults
			for (ParamDefinition def : config.getParameters()) {
				String uid = def.getUID();
				if (container.getParam(uid) == null) {
					ParamSupplier defaultValue = config.getDefaultValue(null, uid);
					if (defaultValue != null) {
						container.addParam(new StaticParamSupplier(uid, defaultValue));
					}
					else {
						handleError(failOnParamError, "No existing or new value found for parameter "+uid+" ("+def.getName()+")");
					}
				}
			}
			
			return container;
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void handleError(boolean failOnParamError, String error) {
		if (failOnParamError) {
			LOG.error(error);
			throw new IllegalArgumentException(error);
		}
		else {
			LOG.warn(error);
		}
	}
}
