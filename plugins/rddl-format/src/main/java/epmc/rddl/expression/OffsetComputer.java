package epmc.rddl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.rddl.value.TypeRDDLObject;
import epmc.value.Operator;
import epmc.value.Value;

public final class OffsetComputer {
	private boolean built;
	private Map<String,Integer> variableNameToIndex = new LinkedHashMap<>();
	private List<int[]> variableSizes = new ArrayList<>();
	private List<String> indexToParameterName = new ArrayList<>();
	private Map<String,Integer> parameterNameToIndex = new LinkedHashMap<>();
	private List<Integer> parameterNumValues = new ArrayList<>();
	private List<Expression> identifiers = new ArrayList<>();
	private List<ExpressionRDDLQuantifier> quantifiers = new ArrayList<>();
	private int[] variablesParameterFromTo;
	private int[] variablesIndicesFromTo;
	private int[] variablesParameterSizes;
	private int[] expressionVariable;
	private int[] expressionParametersFromTo;
	private int[] expressionParameters;
	private Value[] quantifierInitialValues;
	private int[] quantifierParameterFromTo;
	private int[] quantifierParameters;
	private int[] quantifierParameterSizes;
	private int[] quantifierNumAssignments;
	private Operator[] quantifierOperators;

	private int[] parameterValues;
	private final List<String> indexToVariableName = new ArrayList<>();
	
	public void addVariable(String name, int[] parameterSizes) {
		assert !this.built;
		assert name != null;
		assert parameterSizes != null;
		for (int paramNr = 0; paramNr < parameterSizes.length; paramNr++) {
			assert parameterSizes[paramNr] >= 1;
		}
		assert !this.variableNameToIndex.containsKey(name) : name;
		this.indexToVariableName.add(name);
		this.variableNameToIndex.put(name, variableNameToIndex.size());
		this.variableSizes.add(Arrays.copyOf(parameterSizes, parameterSizes.length));
	}
	
	public void addParameter(String name, int numValues) {
		assert name != null;
		assert numValues >= 0;
		assert !this.parameterNameToIndex.containsKey(name);
		this.indexToParameterName.add(name);
		this.parameterNameToIndex.put(name, parameterNameToIndex.size());
		this.parameterNumValues.add(numValues);
	}
	
	public int registerIdentifier(Expression expression) {
		assert expression != null;
		// TODO fix
		String name = null;
//		String name = expression.getName();
		assert this.variableNameToIndex.containsKey(name) : name + " " + variableNameToIndex;
		if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
			ExpressionRDDLQuantifiedIdentifier identifierQuantified = (ExpressionRDDLQuantifiedIdentifier) expression;
			for (Expression parameter : identifierQuantified.getParameters()) {
//				assert this.parameterNameToIndex.containsKey(parameter.getName()) : parameter + " " + parameterNameToIndex.keySet();
			}
			Expression baseIdentifier = identifierQuantified.getIdentifier();
			int baseIdentifierNumber = this.variableNameToIndex.get(((ExpressionRDDLQuantifiedIdentifier) baseIdentifier).getName());
			for (Expression parameter : identifierQuantified.getParameters()) {
				int parameterNumber = this.parameterNameToIndex.get(((ExpressionIdentifierStandard) parameter).getName());
			}
		} else {
			assert expression instanceof ExpressionIdentifierStandard;
		}
		this.identifiers.add(expression);
		return this.identifiers.size() - 1;
	}
	
	public void build() {
		this.built = true;
		int totalSize = 0;
		for (int[] varSizes : this.variableSizes) {
			totalSize += varSizes.length;
		}
		int varParamBegin = 0;
		int varIndexBegin = 0;
		this.variablesParameterFromTo = new int[this.variableSizes.size() + 1];
		this.variablesIndicesFromTo = new int[this.variableSizes.size() + 1];
		this.variablesParameterSizes = new int[totalSize];
		int number = 0;
		for (int variableNr = 0; variableNr < this.variableSizes.size(); variableNr++) {
			this.variablesIndicesFromTo[variableNr] = varIndexBegin;
			this.variablesParameterFromTo[variableNr] = varParamBegin;
			int[] varSize = this.variableSizes.get(variableNr);
			varParamBegin += varSize.length;
			int size = 1;
			for (int param : varSize) {
				size *= param;
				this.variablesParameterSizes[number] = param;
				number++;
			}
			varIndexBegin += size;
		}
		assert number == totalSize : number + " " + totalSize;
		this.variablesParameterFromTo[this.variableSizes.size()] = varParamBegin;
		this.variablesIndicesFromTo[this.variableSizes.size()] = varIndexBegin;
		this.parameterValues = new int[this.parameterNameToIndex.size()];
	}

	public void buildExpressions() {
		this.expressionVariable = new int[this.identifiers.size()];
		this.expressionParametersFromTo = new int[this.identifiers.size() + 1];
		int totalSize = 0;
		for (int exprNr = 0; exprNr < this.identifiers.size(); exprNr++) {
			Expression expression = this.identifiers.get(exprNr);
			if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
				ExpressionRDDLQuantifiedIdentifier identifierQuantified = (ExpressionRDDLQuantifiedIdentifier) expression;
				totalSize += identifierQuantified.getParameters().size();
			}
		}
		this.expressionParameters = new int[totalSize];
		int number = 0;
		for (int exprNr = 0; exprNr < this.identifiers.size(); exprNr++) {
			this.expressionParametersFromTo[exprNr] = number;
			Expression expression = this.identifiers.get(exprNr);
			if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
				ExpressionRDDLQuantifiedIdentifier identifierRDDL = (ExpressionRDDLQuantifiedIdentifier) expression;
				ExpressionRDDLQuantifiedIdentifier identifierQuantified = (ExpressionRDDLQuantifiedIdentifier) expression;
				ExpressionIdentifierStandard identifier = (ExpressionIdentifierStandard) identifierQuantified.getIdentifier();
				int variableNr = variableNameToIndex.get(identifierRDDL.getName());
				this.expressionVariable[exprNr] = variableNr;
				for (Expression parameter : identifierQuantified.getParameters()) {
					ExpressionIdentifierStandard parameterI = (ExpressionIdentifierStandard) parameter;
					int paramNr = this.parameterNameToIndex.get(parameterI.getName());
					this.expressionParameters[number] = paramNr;
					number++;
				}
			} else {
				assert false;
				ExpressionIdentifierStandard exId = (ExpressionIdentifierStandard) expression;
				int variableNr = variableNameToIndex.get(exId.getName());
				this.expressionVariable[exprNr] = variableNr;
			}
		}
		this.expressionParametersFromTo[this.identifiers.size()] = number;
	}

	public void buildQuantifier() {
		this.quantifierInitialValues = new Value[this.quantifiers.size()];
		this.quantifierParameterFromTo = new int[this.quantifiers.size() + 1];
		this.quantifierNumAssignments = new int[this.quantifiers.size()];
		this.quantifierOperators = new Operator[this.quantifiers.size()];
		int quantBegin = 0;
		int totalSize = 0;
		for (ExpressionRDDLQuantifier quantifier : this.quantifiers) {
			Map<Expression, TypeRDDLObject> parameters = quantifier.getParameterMap();
			int numAssignments = 1;
			for (Expression type : parameters.keySet()) {
				numAssignments *= getParameterSize((ExpressionIdentifierStandard) type);
			}
			totalSize += numAssignments;
		}
		this.quantifierParameters = new int[totalSize];
		this.quantifierParameterSizes = new int[totalSize];
		int absParamNr = 0;
		for (int quantNr = 0; quantNr < this.quantifiers.size(); quantNr++) {
			ExpressionRDDLQuantifier quantifier = this.quantifiers.get(quantNr);
			this.quantifierInitialValues[quantNr] = quantifier.getInitialValue();
			this.quantifierParameterFromTo[quantNr] = quantBegin;
			this.quantifierOperators[quantNr] = quantifier.getOperator();
			Map<Expression, TypeRDDLObject> parameters = quantifier.getParameterMap();
			int numAssignments = 1;
			for (Expression parameter : parameters.keySet()) {
				int paramNr = getParamNumber((ExpressionIdentifierStandard) parameter);
				int paramSize = getParameterSize((ExpressionIdentifierStandard) parameter);
				numAssignments *= getParameterSize((ExpressionIdentifierStandard) parameter);
				this.quantifierParameters[absParamNr] = paramNr;
				this.quantifierParameterSizes[absParamNr] = paramSize;
				absParamNr++;
			}
			this.quantifierNumAssignments[quantNr] = numAssignments;
			quantBegin += parameters.size();
		}
		this.quantifierParameterFromTo[quantifiers.size()] = quantBegin;
//		private int[] quantifierParameterSizes;

	}
	
	public void setParameter(int number, int value) {
		assert this.built;
		assert number >= 0;
		assert number < this.parameterValues.length;
		assert value >= 0;
		assert value < this.parameterNumValues.get(number);
		this.parameterValues[number] = value;
	}
	
	public int computeIdentifierIndex(int expressionNr) {
		int variable = this.expressionVariable[expressionNr];
		int variableParamBegin = this.variablesParameterFromTo[variable];
		int numParameters = this.expressionParametersFromTo[expressionNr + 1] - 
				this.expressionParametersFromTo[expressionNr];
		int expressionParamBegin = this.expressionParametersFromTo[expressionNr];
		int index = this.variablesIndicesFromTo[variable];
		int mult = 1;
		for (int paramNr = 0; paramNr < numParameters; paramNr++) {
			int exprParamNr = paramNr + expressionParamBegin;
			int param = expressionParameters[exprParamNr];
			int paramValue = this.parameterValues[param];
			int varParamNr = paramNr + variableParamBegin;
			int varParamSize = this.variablesParameterSizes[varParamNr];
			assert paramValue < varParamSize : + paramValue + " " + varParamSize + " " + this.indexToParameterName.get(param);
			index += mult * paramValue;
			mult *= varParamSize;
		}
		return index;
	}
	
	public int computeExpressionIndex(Expression expression) {
		return -1;
	}

	public int[] getParameters() {
		assert this.built;
		return this.parameterValues;
	}

	public void setParameters(int[] values) {
		assert this.built;
		assert values != null;
		System.arraycopy(values, 0, this.parameterValues, 0, values.length);
	}

	public int getParameterValue(int paramNr) {
		assert this.built;
		assert paramNr >= 0;
		assert paramNr < this.parameterValues.length;
		return this.parameterValues[paramNr];
	}

	public void setParameter(String name, int value) {
		assert this.built;
		assert name != null;
		assert this.parameterNameToIndex.containsKey(name);
		assert value >= 0;
		int paramNr = this.parameterNameToIndex.get(name);
		setParameter(paramNr, value);
	}

	public int getParameterValue(String name) {
		assert this.built;
		assert name != null;
		assert this.parameterNameToIndex.containsKey(name);
		int paramNr = this.parameterNameToIndex.get(name);
		return getParameterValue(paramNr);
	}
	
	public int getParameterIndex(String name) {
		assert name != null;
		assert this.parameterNameToIndex.containsKey(name) : name + " " + parameterNameToIndex.keySet();
		return this.parameterNameToIndex.get(name);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("variables:\n");
		for (Entry<String, Integer> entry : variableNameToIndex.entrySet()) {
			int[] sizes = this.variableSizes.get(entry.getValue());
			builder.append(entry.getKey() + " " + Arrays.toString(sizes) + "\n");
		}
		builder.append("parameters:\n");
		for (Entry<String, Integer> entry : parameterNameToIndex.entrySet()) {
			int size = this.parameterNumValues.get(entry.getValue());
			builder.append(entry.getKey() + " " + size + "\n");
		}
		builder.append("expressions:\n");
		for (Expression entry : this.identifiers) {
			builder.append(entry + "\n");
		}
		builder.append("variablesIndicesFromTo: " + Arrays.toString(this.variablesIndicesFromTo) + "\n");
		builder.append("variableParametersFromTo: " + Arrays.toString(this.variablesParameterFromTo) + "\n");
		builder.append("variablesParameterSizes: " + Arrays.toString(this.variablesParameterSizes) + "\n");
		return builder.toString();
	}

	public int registerQuantifier(ExpressionRDDLQuantifier expression) {
		this.quantifiers.add(expression);
		return this.quantifiers.size() - 1;
	}

	public Value getQuantifierInit(int quantifierIndex) {
		assert quantifierIndex >= 0;
		assert quantifierIndex < this.quantifiers.size();
		return this.quantifierInitialValues[quantifierIndex];
	}

	public int getQuantifierNumAssignments(int quantifierIndex) {
		assert quantifierIndex >= 0;
		assert quantifierIndex < this.quantifierNumAssignments.length;
		return this.quantifierNumAssignments[quantifierIndex];
	}

	public void setQuantifierAssignment(int quantifier, int assignment) {
		assert quantifier >= 0;
		assert quantifier < this.quantifiers.size();
		assert assignment >= 0;
		assert assignment < this.quantifierNumAssignments[quantifier];
		for (int paramNr = this.quantifierParameterFromTo[quantifier];
				paramNr < this.quantifierParameterFromTo[quantifier + 1];
				paramNr++) {
			int param = this.quantifierParameters[paramNr];
			int numValues = this.quantifierParameterSizes[paramNr];
			int paramValue = assignment % numValues;
			assignment /= numValues;
			this.parameterValues[param] = paramValue;
		}
		assert assignment == 0;
	}
	
	private int getParamNumber(ExpressionIdentifierStandard parameter) {
		assert parameter != null;
		assert this.parameterNameToIndex.containsKey(parameter.getName())
		: parameter + " " + this.parameterNameToIndex.keySet();
		return this.parameterNameToIndex.get(parameter.getName());
	}
	
	private int getParameterSize(ExpressionIdentifierStandard parameter) {
		assert parameter != null;
		assert this.parameterNameToIndex.containsKey(parameter.getName())
		: parameter + " " + this.parameterNameToIndex.keySet();
		int paramNr = this.parameterNameToIndex.get(parameter.getName());
		return this.parameterNumValues.get(paramNr);
	}

	public Operator getQuantifierOperator(int quantifier) {
		assert quantifier >= 0;
		assert quantifier < this.quantifierOperators.length;
		return this.quantifierOperators[quantifier];
	}
	
	public String getVariable(int variableNr) {
		assert variableNr >= 0;
		assert variableNr < this.indexToVariableName.size();
		return this.indexToVariableName.get(variableNr);
	}
}
