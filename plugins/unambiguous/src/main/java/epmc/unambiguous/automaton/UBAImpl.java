package epmc.unambiguous.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import epmc.automaton.Buechi;
import epmc.automaton.BuechiTransition;
import epmc.automaton.UtilAutomaton;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.options.Options;
import epmc.unambiguous.util.PowerSetIter;
import epmc.unambiguous.util.UtilUBA;
import epmc.util.BitSet;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class UBAImpl implements Buechi {

    private final static String IDENTIFIER = "tguba";

    private final ContextExpression context;
    private final GraphExplicit automaton;
    private int numLabels;
    private final int trueState;
    private boolean deterministic;
    private final EvaluatorExplicit[] evaluators;
    private final Options options;
    private final Expression[] expressions;
    private final Type[] expressionTypes;
    private int stateNum ;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    public UBAImpl(Expression expression, Expression[] expressions)
            throws EPMCException {
        assert expression != null;
        this.options = expression.getOptions();
        this.context = expression.getContext();
        Set<Expression> expressionsSeen = context.newSet();
        automaton = createUBA(expression, expressionsSeen);
        if (expressions == null) {
            expressions = new Expression[expressionsSeen.size()];
            int index = 0;
            for (Expression expr : expressionsSeen) {
                expressions[index] = expr;
                index++;
            }
        }
        if (this.numLabels == 0) {
            fixNoLabels();
        }
        trueState = findTrueState();
        this.expressions = expressions.clone();
        expressionTypes = new Type[expressions.length];
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            Expression expr = expressions[exprNr];
            expressionTypes[exprNr] = expr.getType();
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
                BuechiTransition trans = labels.getObject(succNr);
                Expression guard = trans.getExpression();
                evaluators[totalSize] = UtilEvaluatorExplicit.newEvaluator(guard, expressions);
                totalSize++;
            }
        }
        totalSize = 0;
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            automaton.queryNode(node);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                UBATransition trans = labels.getObject(succNr);
                trans.setResult(evaluators[totalSize].getResultValue());
                totalSize++;
            }
        }
    }

    @Override
    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public void query(Value[] get) throws EPMCException {
    	for (int i = 0; i < evaluators.length; i++) {
    		evaluators[i].evaluate(get);
    	}
    }
    
    private int findTrueState() throws EPMCException {
        int trueState = -1;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            automaton.queryNode(node);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                BuechiTransition trans = labels.getObject(succNr);
                Expression expr = trans.getExpression();
                boolean isTrue = expr.isTrue();
                if (isTrue && trans.getLabeling().cardinality() == numLabels) {
                    trueState = node;
                    break;
                }
            }
        }
        return trueState;
    }

    private GraphExplicit createUBA(Expression expression,
            Set<Expression> expressionsSeen) throws EPMCException {
        assert expression != null;
        assert expressionsSeen != null;      

        /** remove all the bounded until formulas */
//        expression = UtilAutomaton.bounded2next(expression);
//        expression = UtilUBA.ltlfiltSimplify(expression);
//        System.out.println("after simplification : " + expression);
        /** transform it to elementary form
         * , may have other atomic propositions like !a, !b*/
        expression = UtilUBA.elementaryForm(expression);
//        System.out.println("elementary form : " + expression);
        Set<Expression> aps = context.newSet();
        UtilUBA.getVars(expression, aps);
        /** we need get all sub until formulas 
         * in the formula to label accepting edges */
        Set<Expression> labelFormulas = new HashSet<Expression>();
        UtilUBA.getUntilFormulas(expression, labelFormulas);
        /** set label number to the until formula number */
        this.numLabels = labelFormulas.size();
        Expression[] untilFormulas = (Expression[]) labelFormulas.toArray(new Expression[]{});
        
        Set<Expression> elemSet = UtilUBA.getElementarySet(expression, aps);
        
        ArrayList<Set<Expression>> num2State;
        ArrayList<Set<Expression>> num2Letter;  
        
        num2State = new ArrayList<Set<Expression>>(2^(elemSet.size()) + 1);
        Set<Expression> initState = new HashSet<Expression>();
        initState.add(expression);
        
        /* init state will be state that satisfy expression */
        this.stateNum = 0;
        num2State.add(this.stateNum, initState);
        /* add other states */
        PowerSetIter<Expression> pset = new PowerSetIter<Expression>(elemSet);
        
        for(Set<Expression> set : pset) {
        	this.stateNum ++;
        	num2State.add(this.stateNum, set);
        }
//        System.out.println("the size of num2state is " + num2State.size());
//        System.out.println("the num2state is " + num2State);
        /* now we initialize the alphabet symbols */
        boolean hasTrue = aps.contains(context.getTrue());
        aps.remove(context.getTrue());
        aps.remove(context.getFalse());
        expressionsSeen.addAll(aps);
        pset.reset(aps);
        num2Letter = new ArrayList<Set<Expression>>(2^(aps.size()));
        int letterIdx = -1;
        for(Set<Expression> set : pset) {
        	letterIdx ++;
        	if(hasTrue) set.add(context.getTrue());
        	num2Letter.add(letterIdx, set);
        }
        
        if(hasTrue) aps.add(context.getTrue());
        /* now it is time to build an automaton */
        GraphExplicitWrapper graph = new GraphExplicitWrapper(context);

        ContextValue contextValue = context.getContextValue();
        Type typeInteger = contextValue.getTypeInteger();
        Type typeLabel = contextValue.getTypeObject(BuechiTransition.class);
        Value numLabels = typeInteger.newValue();
        graph.addSettableGraphProperty(CommonProperties.NUM_LABELS, typeInteger);
        graph.addSettableEdgeProperty(CommonProperties.AUTOMATON_LABEL, typeLabel);
        
        /* now we start to construct transition relation */
        /*1 first add relations with initial state \phi 
         * T(\phi, a) = \{ V\subseteq el(\phi) \| (V, a) ||- \phi \}
         * */
        ArrayList<StateTR> successors = new ArrayList<StateTR>();
        /** should rename all state number from 0 .... */

        BitSet stateVisited = contextValue.newBitSetBounded(num2State.size());
        int stateCounter = 0;
        // needs to reorder states
        Map<Integer, Integer> state2Index = new HashMap<>();
        stateVisited.set(0);
        state2Index.put(0, stateCounter);  /* state -> index */
        Queue<Integer> currentLevel = new LinkedList<Integer>();  /* need to be BitSet ?*/
        ++ stateCounter;
        for(int stateIndex = 1; stateIndex < num2State.size() ; stateIndex ++) {
        	
            Set<Expression> succCandidate = num2State.get(stateIndex);
        	for(int letterIndex = 0 ; letterIndex < num2Letter.size() ; letterIndex++) {
        		Set<Expression> letter = num2Letter.get(letterIndex);
    			Expression edgeLabel = UtilUBA.letter2expr(context, letter, aps);
    			assert ! edgeLabel.isFalse();
        		// successor test 
        		if(UtilUBA.isSat(succCandidate
        				, letter, expression, aps)) {
            		stateCounter = addSuccessor(num2State, state2Index, stateIndex,
            				letter, edgeLabel, aps, contextValue, successors
            				, untilFormulas, stateCounter);
        			if(! stateVisited.get(stateIndex)){
        				currentLevel.add(stateIndex);
        				stateVisited.set(stateIndex);
        			}
        		}
        	}
        }
        
        Value transitionValue = graph.getEdgePropertyType(CommonProperties.AUTOMATON_LABEL).newValue();
        EdgeProperty labelProp = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        /* add transitions from initial state */

        addTransitions(graph, 0, successors, transitionValue, labelProp);
	    successors.clear();
        /* 2 this is the situation when V is subset of elementary set of formula 
         * T(V, a) = \{ U \subseteq el(\phi) \| \forall \X\psi \in V \iff (U, a) ||- \psi  \}
         * */
//        System.out.println("step 2: ");
        while (!currentLevel.isEmpty()) {
        	
        	int stateV = currentLevel.poll();
        	
            for(int stateU = 1; stateU < num2State.size() ; stateU ++) { /* first fix U */
            	boolean isSuccessor = false;
            	for(int letterIndex = 0 ; letterIndex < num2Letter.size() ; letterIndex++) {
            		Set<Expression> letter = num2Letter.get(letterIndex);
            		Expression edgeLabel = UtilUBA.letter2expr(context, letter, aps);
            		assert ! edgeLabel.isFalse();
            		/* here we can add implement BuechiTransition class */
            		boolean flag = true;
            		/* 1 (U, a) ||- \psi */
            		for(Expression xpsi : elemSet) {
            			flag = num2State.get(stateV).contains(xpsi) ==
            					UtilUBA.isSat(num2State.get(stateU)
            							, letter, xpsi.getOperand1(), aps);
            			if(! flag) break;
            		}
            		
            		if(! flag) continue; /* change for another letter */
            		/* good , one match found */
            		isSuccessor = true;
            		stateCounter = addSuccessor(num2State, state2Index, stateU,
            				letter, edgeLabel, aps, contextValue, successors
            				, untilFormulas, stateCounter);
            	}
    			if( isSuccessor && ! stateVisited.get(stateU)) {
        			currentLevel.add(stateU);
        			stateVisited.set(stateU);
    			}
            }
            /* add transitions */
            addTransitions(graph, state2Index.get(stateV), successors, transitionValue, labelProp);
            successors.clear();
        }
        
        /* 3 still need to identify accepting transitions, better to do with transition construction simultanously 
         * transition edge (U, a , V) belongs to eccepting set ACC(\psi) if (V, a) ||- \phi_2 \lor (V, a) ||- \not \psi 
         * where \psi = \phi_1 \U \phi_2 , and \psi is a subformula of \phi 
         * */
        
        numLabels.set(untilFormulas.length);
        BitSet init = graph.getInitialNodes();
        init.set(0);
        graph.setGraphProperty(CommonProperties.NUM_LABELS, numLabels);
//        System.out.println("The number of states in UBA is " + stateCounter);
//    	System.out.println("graph node: \n " + graph.getQueriedNodes() + " " + this.stateNumber);
//    	System.out.println("graph: " + graph.toString());
        return graph;
    }
    
    private int addSuccessor(List<Set<Expression>> num2State
    		, Map<Integer, Integer> state2Index, int succ
    		, Set<Expression> letter, Expression edgeLabel, Set<Expression> aps,
    		ContextValue contextValue, List<StateTR> successors,
    		Expression[] untilFormulas, int counter) {
		BitSet accs = contextValue.newBitSetBounded(untilFormulas.length);
		/* successfull transition, now check whether it is accepting by some set */
		for(int untilIndex = 0; untilIndex < untilFormulas.length ; untilIndex ++) {
			if(UtilUBA.isSat(num2State.get(succ), letter
					, untilFormulas[untilIndex].getOperand2(), aps)
					|| UtilUBA.isSat(num2State.get(succ), letter
							, untilFormulas[untilIndex].not(), aps)) {
    			accs.set(untilIndex);
			}
		}
		UBATransition transition = new UBATransitionImpl(edgeLabel, accs);
		Integer succState = state2Index.get(succ);
		if(succState == null) {
			state2Index.put(succ, counter);  /* state -> index */
			successors.add(new StateTR(counter, transition));
			counter ++ ;
		}else {
			successors.add(new StateTR(succState, transition));
		}
		return counter ;
    }
    
    private void addTransitions(GraphExplicitWrapper graph, int currentState, ArrayList<StateTR> successors
    		, Value transitionValue, EdgeProperty labelProp) throws EPMCException {
    
        int numSuccessors = successors.size();
        graph.queryNode(currentState);
	    graph.prepareNode(numSuccessors);
	    for(int succIndex = 0; succIndex < numSuccessors; succIndex ++) {
	    	int toState = successors.get(succIndex).successor;
	    	graph.setSuccessorNode(succIndex, toState);
		    transitionValue.set(successors.get(succIndex).transition);
		    labelProp.set(transitionValue, succIndex);
	    }
    }
    /** used to store transition relation from a state,
     *  tuple for <succ, transition> */
    final class StateTR {
    	int successor;
    	UBATransition transition;
    	StateTR(int successor, UBATransition transition) {
    		this.successor = successor;
    		this.transition = transition;
    	}
    }

    private void fixNoLabels() throws EPMCException {
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            automaton.queryNode(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                BuechiTransition trans = labels.getObject(succNr);
                trans.getLabeling().set(0);
            }
        }
        numLabels = 1;
    }
    
    @Override
    public boolean isDeterministic() {
        return deterministic;
    }
    
    @Override
    public int getNumLabels() {
        return numLabels;
    }
    
    @Override
    public GraphExplicit getGraph() {
        return automaton;
    }
    
    @Override
    public int getTrueState() {
        return trueState;
    }
    
}
