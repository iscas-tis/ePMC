package epmc.rddl.model;

import java.util.ArrayList;
import java.util.List;

import epmc.value.Type;

interface PVariable {
    String getName();
        
    Type getType();
    
    List<String> getParameterStrings();    
    
    default List<RDDLObject> getParameters(Instance instance) {
    	assert instance != null;
    	List<RDDLObject> result = new ArrayList<>();
    	for (String parameterName : getParameterStrings()) {
    		result.add(instance.getObject(parameterName));
    	}
    	return result;
    }
}
