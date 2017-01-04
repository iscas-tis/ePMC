package epmc.propertysolverltlfg.hoa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.propertysolverltlfg.automaton.RabinStateUtil;
import epmc.propertysolverltlfg.automaton.RabinTransitionUtil;
import epmc.util.BitSet;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

/**
 * Take Hanoi format as input and return Rabin automaton 
 * @author Yong Li
 * */

public class HOA2DFA implements HOAUser, AutomatonRabin {
	
	private final static String IDENTIFIER = "hoa2dfa";
	private GraphExplicitWrapper automaton; 
    private ContextValue contextValue ;
	private Map<String, Expression> ap2expr;
	private Map<Expression, Expression> replaceM;
	private AutomatonType type ;

	private int numLabels = 0;
    private Expression[] expressions = null;
    private Type[] expressionTypes = null;
    List<AcceptanceCondition> acceptances = null;
    List<Expression> formulaAccs = null;
    private List<StateTransition> currentSuccs = new LinkedList<>();
    private BitSet currentSignature;
    private int currentState ;
    private boolean isStateBased = false;
	private EvaluatorExplicit[] evaluators;
    
    private class StateTransition {
    	int id;
    	Expression expression;
    	BitSet signature;
    	StateTransition(int succ, Expression expr, BitSet signature) {
    		this.id = succ;
    		this.expression = expr;
    		this.signature = signature;
    	}
    	
    	@Override
		public String toString() {
    		return id + "," + expression + "," + signature.toString();
    	}
    }
    
	public HOA2DFA(ContextValue context, Map<String, Expression> ap2expr
			, AutomatonType type) throws EPMCException {
		this.contextValue = context;
		this.ap2expr = ap2expr;
		this.replaceM = new HashMap<>();
		this.type = type;
		this.automaton = new GraphExplicitWrapper(context);
		this.isStateBased = type == AutomatonType.GRA || type == AutomatonType.RA
				|| type == AutomatonType.STGRA || type == AutomatonType.FDA;
		prepareGraph();
	}
	
	private void prepareGraph() throws EPMCException {
		// only those dfa need state labels
		if(isStateBased) {
			Type typeNodeLabel = new TypeObject.Builder()
	                .setContext(contextValue)
	                .setClazz(RabinStateUtil.class)
	                .build();
			automaton.addSettableNodeProperty(CommonProperties.AUTOMATON_LABEL, typeNodeLabel);
		}
    	Type typeEdgeLabel = new TypeObject.Builder()
                .setContext(contextValue)
                .setClazz(RabinTransitionUtil.class)
                .build();
    	automaton.addSettableEdgeProperty(CommonProperties.AUTOMATON_LABEL, typeEdgeLabel);
	    Type typeInteger = TypeInteger.get(contextValue);
	    automaton.addSettableGraphProperty(CommonProperties.NUM_LABELS, typeInteger);
	}
	
    // conduct evalutation on edges
	@Override
	public void prepare() throws EPMCException {
		Set<Entry<String,Expression>> expressionsSeen = ap2expr.entrySet();
        if (expressions == null) {
            expressions = new Expression[expressionsSeen.size()];
            int index = 0;
            for (Entry<String, Expression> entry : expressionsSeen) {
                expressions[index] = entry.getValue();
                index++;
            }
        }
        this.expressions = expressions.clone();
        expressionTypes = new Type[expressions.length];
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            Expression expr = expressions[exprNr];
            // TODO
            expressionTypes[exprNr] = expr.getType(null);
        }
        
        int totalSize = 0;
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            automaton.queryNode(node);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                totalSize++;
            }
        }
        this.evaluators = new EvaluatorExplicit[totalSize];
        totalSize = 0;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            automaton.queryNode(node);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                RabinTransitionUtil trans = labels.getObject(succNr);
                Expression guard = trans.getExpression();
                evaluators[totalSize] = UtilEvaluatorExplicit.newEvaluator(guard, expressions);
                totalSize++;
            }
        }
        totalSize = 0;
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            automaton.queryNode(node);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
            	RabinTransitionUtil trans = labels.getObject(succNr);
                ((RabinTransitionImpl) trans).setResult(evaluators[totalSize].getResultValue());
                totalSize++;
            }
        }
	}


	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	@Override
	public boolean isDeterministic() {
		return true;
	}
	@Override
	public Expression[] getExpressions() {
		return this.expressions;
	}
	@Override
	public int getNumLabels() {
		return this.numLabels;
	}
	@Override
	public int getTrueState() {
		return -1;
	}
	@Override
	public GraphExplicit getGraph() {
		return this.automaton;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Acceptance: \n" );
		boolean first = true;
		for(AcceptanceCondition acc: this.acceptances) {
			if(!first) builder.append(" | ");
			first = false;
			builder.append(acc.toString());
		}
		builder.append("\n" + automaton.toString());
		return builder.toString();
	}
	
	@Override
	public void query(Value[] get) throws EPMCException {
	    for (EvaluatorExplicit evaluator : evaluators) {
	        evaluator.evaluate(get);
	    }
	}
	@Override
	public AutomatonType getAutomatonType() {
		return this.type;
	}
	@Override
	public AutomatonRabin getAutomaton() {
		return this;
	}
	
	@Override
	public List<AcceptanceCondition> getAcceptances() {
		return this.acceptances;
	}

	@Override
	public void setNumOfStates(int numOfStates) throws EPMCException {
		for(int stateId = 0; stateId < numOfStates; stateId ++) {
			this.automaton.queryNode(stateId);
		}
	}

	@Override
	public void setStartStates(List<Integer> conjStates) {
		assert conjStates.size() == 1;
		BitSet init = automaton.getInitialNodes();
		init.set(conjStates.get(0), true);
	}

	@Override
	public void setAcceptances(int numOfSets, List<AcceptanceCondition> accs) {
		ValueInteger numLabels = ValueInteger.asInteger(automaton.getGraphProperty(CommonProperties.NUM_LABELS));
		numLabels.set(numOfSets);
		this.numLabels = numOfSets;
		this.acceptances = accs;
	}

	@Override
	public void setAps(int numOfAps, List<String> aps) {
	    // needs to deal with dummy? NO need to do that
		for (String ap : aps) {
			replaceM.put(new ExpressionIdentifierStandard.Builder()
					.setContext(context)
					.setName(ap)
					.build(), ap2expr.get(ap));
		}
	}

	@Override
	public void setCurrState(int id, Expression label, String comment,
			BitSet signature) {
		currentState = id;
		currentSignature = signature;
	}

	@Override
	public void addEdge(int succ, Expression label, BitSet signature) {
		Expression expr = UtilExpressionStandard.replace(label, replaceM);
		currentSuccs.add(new StateTransition(succ, expr, signature));
	}

	@Override
	public void endOfState() throws EPMCException {
		automaton.queryNode(currentState);
		automaton.prepareNode(currentSuccs.size());
		if (isStateBased) {
			RabinStateUtil stateLabel = new RabinStateImpl(currentSignature);
		    Value nodeValue = automaton.getNodePropertyType(CommonProperties.AUTOMATON_LABEL).newValue();
		    NodeProperty labelProp = automaton.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
		    ValueObject.asObject(nodeValue).set(stateLabel);
		    labelProp.set(nodeValue);
		}
	    Value edgeValue = automaton.getEdgePropertyType(CommonProperties.AUTOMATON_LABEL).newValue();
	    EdgeProperty labelProp = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
		int succId = 0;
		for(StateTransition succ : currentSuccs) {
			RabinTransitionUtil edgeLabel = new RabinTransitionImpl(succ.expression, succ.signature);
			automaton.setSuccessorNode(succId, succ.id);
			ValueObject.asObject(edgeValue).set(edgeLabel);
			labelProp.set(edgeValue, succId);
			++ succId; 
		}
		currentSuccs.clear();
	}
	@Override
	public void parseEnd() {
		if(formulaAccs == null) return ;
		for(int accNr = 0; accNr < formulaAccs.size() ; accNr ++) {
			Expression label = formulaAccs.get(accNr);
			formulaAccs.set(accNr, UtilExpressionStandard.replace(label, replaceM));
		}
	}

	@Override
	public void setAccExpressions(int numOfSets, List<Expression> accs) {
		this.numLabels = numOfSets;
		this.formulaAccs = accs;
	}

	@Override
	public List<Expression> getAccExpressions() {
		return this.formulaAccs;
	}
	
	
	private class RabinStateImpl implements RabinStateUtil {
		private final BitSet labeling ;
		
		RabinStateImpl(BitSet labeling) {
			this.labeling = labeling;
		}
		@Override
		public BitSet getLabeling() {
			return this.labeling;
		}
	    @Override
	    public String toString() {
	        return  labeling + "";
	    }
		
	}
	
	private class RabinTransitionImpl implements RabinTransitionUtil {
        private final Expression expression;
        private final BitSet labeling;
        private Value result;
        RabinTransitionImpl(Expression expression, BitSet labeling) {
        	this.expression = expression;
        	this.labeling = labeling;
        }
		@Override
		public Expression getExpression() {
			return this.expression;
		}
	    public void setResult(Value result) {
	        this.result = result;
	    }
	    @Override
	    public String toString() {
	        return expression + "\n" + labeling;
	    }
		@Override
		public BitSet getLabeling() {
			return this.labeling;
		}
		@Override
		public boolean guardFulfilled() throws EPMCException {
			return ValueBoolean.asBoolean(result).getBoolean();
		}
	}

}
