package epmc.param.value;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParameterSet {
    private final TObjectIntMap<Object> parameterToNumber = new TObjectIntHashMap<>();
	private final List<String> parameters = new ArrayList<>();
	private final List<String> parametersExternal = Collections.unmodifiableList(parameters);
	
    public List<String> getParameters() {
		return parametersExternal;
	}
    
	public void addParameter(String parameter) {
		assert parameter != null;
		assert !this.parameterToNumber.containsKey(parameter) : parameter;
		this.parameters.add(parameter);
		this.parameterToNumber.put(parameter, parameterToNumber.size());
	}

	public int getNumParameters() {
		return this.parameters.size();
	}

	public int getParameterNumber(String parameter) {
		assert parameter != null;
		assert parameterToNumber.containsKey(parameter) : parameter;
		return parameterToNumber.get(parameter);
	}

	public String getParameter(int parameterNumber) {
		assert parameterNumber >= 0;
		assert parameterNumber < parameters.size();
		return parameters.get(parameterNumber);
	}

	public boolean isParameter(String parameter) {
		assert parameter != null;
		return this.parameterToNumber.containsKey(parameter);
	}
}
