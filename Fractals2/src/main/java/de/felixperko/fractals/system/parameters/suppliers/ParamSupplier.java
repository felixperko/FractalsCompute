package de.felixperko.fractals.system.parameters.suppliers;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.systems.infra.SystemContext;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = StaticParamSupplier.class, name = "staticSupplier"),
	    @Type(value = CoordinateBasicShiftParamSupplier.class, name = "coordinateBasicShiftSupplier")})
public interface ParamSupplier extends Serializable{
	
	public String getName();

	public Object getGeneral();
	public <C> C getGeneral(Class<C> cls);
	public Object get(SystemContext systemContext, ComplexNumber chunkPos, int pixel, int sample);
	public <C> C get(SystemContext systemContext, Class<C> valueCls, ComplexNumber chunkPos, int pixel, int sample);
	
	public boolean isSystemRelevant();
	public boolean isLayerRelevant();
	
	public AbstractParamSupplier setSystemRelevant(boolean relevant);
	public AbstractParamSupplier setLayerRelevant(boolean relevant);
	
	public boolean isChanged();

	public ParamSupplier copy();

	public void updateChanged(ParamSupplier old);
	public boolean evaluateChanged(ParamSupplier old);

}
