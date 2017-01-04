package epmc.rddl.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.rddl.expression.OffsetComputer;

public final class ModelOffsetComputer {
	private final OffsetComputer offsetComputer = new OffsetComputer();
	private final Instance instance;
	private final List<PVariable> variables = new ArrayList<>();
	private final Map<Expression,RDDLObject> parameters = new LinkedHashMap<>();
	
	ModelOffsetComputer(Instance instance) {
		assert instance != null;
		this.instance = instance;
	}
	
	public void addVariable(PVariable variable) {
		assert variable != null;
		List<RDDLObject> parameters = variable.getParameters(this.instance);
		int[] parameterSizes = new int[parameters.size()];
		for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
			parameterSizes[paramNr] = parameters.get(paramNr).numValues();
		}
		this.offsetComputer.addVariable(variable.getName(), parameterSizes);
		this.variables.add(variable);
	}
	
	public void addParameter(ExpressionIdentifierStandard name, RDDLObject type) {
		assert name != null;
		assert type != null;
		assert !this.parameters.containsKey(name);
		this.offsetComputer.addParameter(name.getName(), type.numValues());
		this.parameters.put(name, type);
	}
	
	public void build() {
		this.offsetComputer.build();
	}
	
	public OffsetComputer getOffsetComputer() {
		return this.offsetComputer;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("variables:\n");
		for (PVariable variable : this.variables) {
			builder.append(variable + "\n");
		}
		builder.append("parameters:\n");
		for (Entry<Expression, RDDLObject> entry : this.parameters.entrySet()) {
			builder.append(entry.getKey() + " : " + entry.getValue() + "\n");
		}
		return builder.toString();
	}
	
	public int[] getParameters() {
		return this.offsetComputer.getParameters();
	}
	
	public void setParameters(int[] values) {
		this.offsetComputer.setParameters(values);
	}
	
	public void setParameter(ExpressionIdentifierStandard variable, int value) {
		assert variable != null;
		this.offsetComputer.setParameter(variable.getName(), value);
	}
	
	public void setParameter(int paramNr, int value) {
		this.offsetComputer.setParameter(paramNr, value);
	}
	
	public int getParameter(int paramNr) {
		return this.offsetComputer.getParameterValue(paramNr);
	}
	
	public int getParameter(ExpressionIdentifierStandard parameter) {
		assert parameter != null;
		return this.offsetComputer.getParameterValue(parameter.getName());
	}
}
