package epmc.jani.explorer;

import static epmc.error.UtilError.ensure;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.UtilExpressionSimplify;
import epmc.jani.model.Assignment;
import epmc.jani.model.Destination;
import epmc.jani.model.Variable;
import epmc.jani.value.TypeLocation;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

/**
 * Evaluator for edge destinations.
 * 
 * @author Ernst Moritz Hahn
 */
public final class DestinationEvaluator {
	public final static class Builder {
		private Destination destination;
		private Expression[] variables;
		private Map<Variable, Integer> variableMap;
		private int locationVariable;
		private Map<Expression, Expression> autVarToLocal;
		private TypeLocation typeLocation;
		private ExpressionToType expressionToType;
		
		public Builder setDestination(Destination destination) {
			this.destination = destination;
			return this;
		}
		
		private Destination getDestination() {
			return destination;
		}
		
		public Builder setVariables(Expression[] variables) {
			this.variables = variables;
			return this;
		}
		
		private Expression[] getVariables() {
			return variables;
		}
		
		public Builder setVariableMap(Map<Variable, Integer> variableMap) {
			this.variableMap = variableMap;
			return this;
		}
		
		private Map<Variable, Integer> getVariableMap() {
			return variableMap;
		}
		
		public Builder setLocationVariable(int locationVariable) {
			this.locationVariable = locationVariable;
			return this;
		}
		
		private int getLocationVariable() {
			return locationVariable;
		}
		
		public Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal) {
			this.autVarToLocal = autVarToLocal;
			return this;
		}
		
		private Map<Expression, Expression> getAutVarToLocal() {
			return autVarToLocal;
		}
		
		public Builder setTypeLocation(TypeLocation typeLocation) {
			this.typeLocation = typeLocation;
			return this;
		}
		
		private TypeLocation getTypeLocation() {
			return typeLocation;
		}
		
		public Builder setExpressionToType(ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
		
		public ExpressionToType getExpressionToType() {
			return expressionToType;
		}
		
		public DestinationEvaluator build() throws EPMCException {
			return new DestinationEvaluator(this);
		}
	}
	
	/** Probability (reference in {@link #evaluator}). */
	private final EvaluatorExplicit probability;
	/** Location to which the destination moves. */
	private final int location;
	/** Assignments performed by this evaluator. */
	private final AssignmentEvaluator[] assignments;
	/** Zero real. */
	private final Value zeroReal;
	/** Number of location variable in the automaton evaluator belongs to. */
	private final int locationVariable;
	
	private DestinationEvaluator(Builder builder) throws EPMCException {
		assert builder != null;
		assert builder.getDestination() != null;
		assert builder.getExpressionToType() != null;
		this.locationVariable = builder.getLocationVariable();
		Destination destination = builder.getDestination();
		Map<Expression, Expression> autVarToLocal = builder.getAutVarToLocal();
		Map<Variable, Integer> variableMap = builder.getVariableMap();
		Expression[] variables = builder.getVariables();
		Expression probExpr = destination.getProbabilityExpressionOrOne();
		probExpr = destination.getModel().replaceConstants(probExpr);
		probExpr = UtilExpressionStandard.replace(probExpr, autVarToLocal);
		ContextValue contextValue = builder.getExpressionToType().getContextValue();
		Type typeWeight = TypeWeight.get(contextValue);
		probExpr = UtilExpressionSimplify.simplify(builder.getExpressionToType(), probExpr, typeWeight);
		probability = UtilEvaluatorExplicit.newEvaluator(probExpr, builder.getExpressionToType(), variables);
		TypeLocation typeLocation = builder.getTypeLocation();

		location = typeLocation.getNumber(destination.getLocation());
//		AT: there are no transient/observable assignment in the JANI specification
		int numObservables = 0;
//		for (Assignment entry : destination.getObservableAssignmentsOrEmpty()) {
//			AssignmentEvaluator evaluator = newAssignmentEvaluator(autVarToLocal, variableMap, variables, entry);;
//			if (evaluator == null) {
//				continue;
//			}
//			numObservables++;
//		}
		assignments = new AssignmentEvaluator[destination.getAssignmentsOrEmpty().size() + numObservables];
		int varNr = 0;
		for (Assignment entry : destination.getAssignmentsOrEmpty()) {
			assignments[varNr] = newAssignmentEvaluator(autVarToLocal, variableMap, variables, entry, builder.getExpressionToType());
			varNr++;
		}
//		for (Assignment entry : destination.getObservableAssignmentsOrEmpty()) {
//			AssignmentEvaluator evaluator = newAssignmentEvaluator(autVarToLocal, variableMap, variables, entry);;
//			if (evaluator == null) {
//				continue;
//			}
//			assignments[varNr] = evaluator;
//			varNr++;
//		}
		zeroReal = UtilValue.newValue(TypeReal.get(destination.getModel().getContextValue()), 0);
	}

	Value evaluateProbability(NodeJANI node) throws EPMCException {
		ValueAlgebra result = ValueAlgebra.asAlgebra(probability.evaluate(node.getValues()));
		ensure(result.isGe(zeroReal), ProblemsJANIExplorer.JANI_EXPLORER_NEGATIVE_WEIGHT);
		return result;
	}

	/**
	 * Assign to an explorer node the effect of the destination.
	 * The parameter may not be {@code null}.
	 * 
	 * @param toNode node to assign effect to
	 * @throws EPMCException thrown in case of problems
	 */
	void assignTo(NodeJANI fromNode, NodeJANI toNode) throws EPMCException {
		assert toNode != null;
		for (int i = 0; i < assignments.length; i++) {
			assignments[i].apply(fromNode, toNode);
		}
		if (locationVariable != -1) {
			toNode.setVariable(locationVariable, location);
		}
	}
	
	private AssignmentEvaluator newAssignmentEvaluator(Map<Expression, Expression> autVarToLocal, Map<Variable, Integer> variableMap, Expression[] variables, Assignment assignment, ExpressionToType expressionToType) throws EPMCException {
		assert assignment != null;
		Options options = assignment.getModel().getContextValue().getOptions();
        Map<String,Class<? extends AssignmentEvaluator.Builder>> assignmentEvaluators = options.get(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS);
        for (Class<? extends AssignmentEvaluator.Builder> entry : assignmentEvaluators.values()) {
        	AssignmentEvaluator.Builder builder = Util.getInstance(entry);
			builder.setAssignment(assignment)
				.setVariables(variables)
				.setVariableMap(variableMap)
				.setAutVarToLocal(autVarToLocal)
				.setExpressionToType(expressionToType);
			if (builder.canHandle()) {
				return builder.build();
			}
        }
		return null;
	}

}
