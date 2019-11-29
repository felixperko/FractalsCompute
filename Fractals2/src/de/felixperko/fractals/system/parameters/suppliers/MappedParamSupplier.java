package de.felixperko.fractals.system.parameters.suppliers;

public abstract class MappedParamSupplier extends AbstractParamSupplier{
	
	private static final long serialVersionUID = 6838402763464871572L;

	public MappedParamSupplier(String name) {
		super(name);
	}

	//List<String> inputParamNames = new ArrayList<>();
	
	//transient ParamSupplier[] parameters;
	
//	public void bindParameters(SystemContext systemContext, Map<String, ParamSupplier> localParameters) {
//		
//		//get param fields
//		Map<String, Field> fields = new HashMap<>();
//		for (Field f : getClass().getDeclaredFields()) {
//			if (f.getName().startsWith("p_")) {
//				fields.put(f.getName().substring(2), f);
//			}
//		}
//		
//		//loop over parameters
//		for (Entry<String, Field> e : fields.entrySet()) {
//			String name = e.getKey();
//			
//			//set param field if found
//			Field f = e.getValue();
//			ParamSupplier ps = localParameters.get(name);
//			if (ps == null){
//				ps = systemContext.getParameters().get(name);
//			}
//			
//			if (f != null && ps != null) {
//				try {
//					f.set(this, ps);
//				} catch (IllegalArgumentException | IllegalAccessException ex) {
//					ex.printStackTrace();
//				}
//			}
//			
//			//set parameter
//			//ParamSupplier paramSupplier = parameters.get(name);
//			//if (paramSupplier == null) {
//			//	LOG.error("Couldn't resolve parameter: '"+name+"'");
//			//} else {
//			//	this.parameters[i] = paramSupplier;
//			//}
//		}
//	}
}
