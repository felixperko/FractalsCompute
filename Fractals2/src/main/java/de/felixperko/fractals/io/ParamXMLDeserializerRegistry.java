package de.felixperko.fractals.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.io.deserializers.AbstractParamXMLDeserializer;
import de.felixperko.fractals.io.serializers.AbstractParamXMLSerializer;

public class ParamXMLDeserializerRegistry {
	
	Map<String, List<AbstractParamXMLDeserializer>> deserializers = new HashMap<>();
	
	public void registerDeserializers(AbstractParamXMLDeserializer... deserializers) {
		for (AbstractParamXMLDeserializer deserializer : deserializers)
			registerDeserializer(deserializer);
	}
	
	public void registerDeserializer(AbstractParamXMLDeserializer deserializer) {
		List<AbstractParamXMLDeserializer> list = deserializers.get(deserializer.getParamTypeUid());
		if (list == null) {
			list = new ArrayList<>();
			deserializers.put(deserializer.getParamTypeUid(), list);
		}
		for (AbstractParamXMLDeserializer existing : list) {
			//Do nothing if exact matching Serializer is already registered
			if (existing.getVersion() == deserializer.getVersion())
				return;
		}
		list.add(deserializer);
	}
	
	public AbstractParamXMLDeserializer getSerializer(String typeUid, double requestVersion) {
		List<AbstractParamXMLDeserializer> list = deserializers.get(typeUid);
		if (list == null)
			return null;
		AbstractParamXMLDeserializer closestDeserializer = null;
		double closestVersion = Double.NEGATIVE_INFINITY;
		double closestDist = Double.MAX_VALUE;
		for (AbstractParamXMLDeserializer deserializer : list) {
			double dVersion = deserializer.getVersion();
			double dist = Math.abs(closestVersion-dVersion);
			//exact match
			if (dist == 0.0)
				return deserializer;
			//closest match yet?
			if (dist < closestDist || closestVersion < requestVersion) {
				closestDeserializer = deserializer;
				closestVersion = dVersion;
				closestDist = dist;
			}
		}
		return closestDeserializer;
	}
}
