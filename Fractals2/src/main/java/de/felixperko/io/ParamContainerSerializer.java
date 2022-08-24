package de.felixperko.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.ParamDefinition;
import de.felixperko.fractals.system.parameters.ParamValueType;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.io.serializers.AbstractParamXMLSerializer;

public class ParamContainerSerializer {
	
	static Logger LOG = LoggerFactory.getLogger(ParamContainerSerializer.class);
	
	ParamXMLSerializerRegistry xmlRegistry;
	ClassKeyRegistry typeRegistry;
	
	public ParamContainerSerializer(ParamXMLSerializerRegistry paramXmlSerializerRegistry, ClassKeyRegistry paramSupplierTypeRegistry) {
		this.xmlRegistry = paramXmlSerializerRegistry;
		this.typeRegistry = paramSupplierTypeRegistry;
	}
	
	public byte[] serialize(ParamContainer container, ParamConfiguration config, boolean exi, boolean encodeBase64, boolean skipDefaults, boolean failOnParamError) {
		Document document = XMLIO.createDocument();
		
    	Element configEl = document.createElement("params");
    	configEl.setAttribute("def", config.getUid());
    	configEl.setAttribute("v", ""+config.getVersion());
    	document.appendChild(configEl);
    	
    	for (ParamDefinition def : config.getParameters()) {

    		String uid = def.getUID();
    		
    		Element paramEl = document.createElement("p");
    		paramEl.setAttribute("id", uid);
    		paramEl.setAttribute("name", def.getName());
    		
    		ParamSupplier supp = container.getParam(uid);
			ParamSupplier defaultSupp = config.getDefaultValue(null, uid);
			Object defaultValue = defaultSupp == null ? null : defaultSupp.getGeneral();
			if (supp == null) {
				if (skipDefaults)
					continue;
				else
					supp = new StaticParamSupplier(uid, defaultValue);
			}
			String supplierTypeName = typeRegistry.getClassKey(supp.getClass());
			if (supplierTypeName == null){
				String error = "Supplier class not registered: "+supp.getClass();
				handleError(failOnParamError, error);
				continue;
			}
    		paramEl.setAttribute("type", supplierTypeName);
			if (supp instanceof StaticParamSupplier) {
				Object obj = ((StaticParamSupplier) supp).getObj();
				if (obj == null)
					continue;
				if (skipDefaults && obj.equals(defaultValue))
					continue;
				ParamValueType valueType = def.getValueType();
				AbstractParamXMLSerializer serializer = xmlRegistry.getSerializer(valueType.getUID(), def.getValueTypeVersion());
				if (serializer == null) {
					String error = "No serializer found for valueType = "+valueType.getUID()+" ("+valueType.getName()+") requested version = "+def.getValueTypeVersion();
					handleError(failOnParamError, error);
					continue;
				}
				serializer.serialize(document, paramEl, supp, def.getValueTypeVersion());
			}
			configEl.appendChild(paramEl);
    	}
		
		byte[] data = XMLIO.encodeToByteArray(document, exi, encodeBase64);
		return data;
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
