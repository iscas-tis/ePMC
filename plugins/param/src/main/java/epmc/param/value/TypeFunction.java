package epmc.param.value;

import java.util.List;

import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

public interface TypeFunction extends TypeWeightTransition, TypeWeight, TypeReal {    
    @Override
    ValueFunction newValue();

	ContextValuePARAM getContextPARAM();

	boolean isSupportOperator(String identifier);
	
	default void addParameter(Object parameter) {
		getContextPARAM().addParameter(parameter);
	}

    default int getNumParameters() {
    	return getContextPARAM().getNumParameters();
    }
    
    default int getParameterNumber(Object parameter) {
    	return getContextPARAM().getParameterNumber(parameter);
    }
    
    default Object getParameter(int parameterNumber) {
    	assert parameterNumber >= 0;
    	return getContextPARAM().getParameter(parameterNumber);
    }
	
    default List<Object> getParameters() {
    	return getContextPARAM().getParameters();
    }
    
    default boolean isParameter(Object parameter) {
    	assert parameter != null;
    	return getContextPARAM().isParameter(parameter);
    }
    
	@Override
	default ValueFunction getPosInf() {
		return null;
		// TODO Auto-generated method stub
	}

	@Override
	default ValueFunction getNegInf() {
		return null;
		// TODO Auto-generated method stub
	}
}
