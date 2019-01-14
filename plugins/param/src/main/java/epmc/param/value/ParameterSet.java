package epmc.param.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class ParameterSet {
    private final Object2IntOpenHashMap<Object> parameterToNumber = new Object2IntOpenHashMap<>();
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
		return parameterToNumber.getInt(parameter);
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
