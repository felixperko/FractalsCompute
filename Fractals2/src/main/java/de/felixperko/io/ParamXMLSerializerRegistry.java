package de.felixperko.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.io.serializers.AbstractParamXMLSerializer;

public class ParamXMLSerializerRegistry {
	
	Map<String, List<AbstractParamXMLSerializer>> serializers = new HashMap<>();
	
	public void registerSerializers(AbstractParamXMLSerializer... serializers) {
		for (AbstractParamXMLSerializer serializer : serializers)
			registerSerializer(serializer);
	}
	
	public void registerSerializer(AbstractParamXMLSerializer serializer) {
		List<AbstractParamXMLSerializer> list = serializers.get(serializer.getTypeId());
		if (list == null) {
			list = new ArrayList<>();
			serializers.put(serializer.getTypeId(), list);
		}
		for (AbstractParamXMLSerializer existing : list) {
			//Do nothing if exact matching Serializer is already registered
			if (existing.getVersion() == serializer.getVersion())
				return;
		}
		list.add(serializer);
	}
	
	public AbstractParamXMLSerializer getSerializer(String typeUid, double requestVersion) {
		List<AbstractParamXMLSerializer> list = serializers.get(typeUid);
		if (list == null)
			return null;
		AbstractParamXMLSerializer closestSerializer = null;
		double closestVersion = Double.NEGATIVE_INFINITY;
		double closestDist = Double.MAX_VALUE;
		for (AbstractParamXMLSerializer serializer : list) {
			double sVersion = serializer.getVersion();
			double dist = Math.abs(closestVersion-sVersion);
			//exact match
			if (dist == 0.0)
				return serializer;
			//closest match yet?
			if (dist < closestDist || closestVersion < requestVersion) {
				closestSerializer = serializer;
				closestVersion = sVersion;
				closestDist = dist;
			}
		}
		return closestSerializer;
	}
}
