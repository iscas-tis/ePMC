package epmc.propertysolver;

import static epmc.value.UtilValue.newValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateSetExplicit;
import epmc.jani.model.Action;
import epmc.modelchecker.ModelChecker;
import epmc.constraintsolver.smtlib.petl.ConstraintSolverSMTLib;
import epmc.petl.model.ModelMAS;
import epmc.prism.model.Module;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueObject;

public class UtilConstraints {
    private static boolean pIsReal = false;
    private static int variableCounter = 0;
	private static Map<String, String> variables = new HashMap<String, String>();
	
	private static String bitSet2String(BitSet set, GraphExplicit graph)
	{
		assert(set != null && set.cardinality() != 0);
		
		int nodeNum = graph.getNumNodes();
		StringBuilder builder = new StringBuilder();
		for(int i=set.nextSetBit(0);i>=0 && i<nodeNum;i=set.nextSetBit(i+1))
		{
			builder.append("_" + i);
		}
		builder.append("_");
		
		String res = variables.get(builder.toString());
		if(res!=null)
			return res;
		res = variableCounter + "";
		variableCounter++;
		variables.put(builder.toString(), res);
		return res;
		//return builder.toString();
	}
	private static String getPVariableString(int playerIndex, BitSet stateSet, String localAction, GraphExplicit graph)
	{
		return "p_" + playerIndex + "_"+ bitSet2String(stateSet,graph) + "_" + localAction;
	}
    
	private static void getAllPossibleActions(List<List<String>> res, List<String> temp, List<List<String>> playerActions)
	{
		int size = temp.size();
		if(size == playerActions.size())
		{
			res.add(temp);
			return;
		}
		List<String> next = playerActions.get(size);
		for(String action : next)
		{
			List<String> newTemp = new ArrayList<String>(temp);
			newTemp.add(action);
			getAllPossibleActions(res,newTemp,playerActions);
		}
	}
	
	static ConstraintSolver setBasicConstraints(BitSet unKnown, BitSet oneSet, BitSet zeroSet, GraphExplicit graph, ModelChecker modelChecker)
	{
		NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
		
		ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration();
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        TypeInteger typeInteger = TypeInteger.get();
        TypeReal typeReal = TypeReal.get();
        List<Module> modules = ((ModelMAS) modelChecker.getModel()).getModelPrism().getModules();
        List<String> players = new ArrayList<String>();
        for(Module m : modules)
        {
        	players.add(m.getName());
        }
        
        int nodeNum = graph.getNumNodes();
        for(int i=0;i<nodeNum;i++)
        {
        	if(stateProp.getBoolean(i))
        	{
        		solver.addVariable("x"+i, typeReal, newValue(typeReal, 0),newValue(typeReal, 1));
        	}
        }

        for(int state = oneSet.nextSetBit(0);state>=0 && state<nodeNum;state=oneSet.nextSetBit(state+1))
        {
        	solver.addConstraint(UtilPETL.parseExpression("x" + state + " = 1"));
        }
        for(int state = zeroSet.nextSetBit(0);state>=0 && state<nodeNum;state=zeroSet.nextSetBit(state+1))
        {
        	solver.addConstraint(UtilPETL.parseExpression("x" + state + " = 0"));
        }
        
        int playerNum = players.size();
        Map<String,Map<BitSet, List<String>>> playerSetToActions = new HashMap<String,Map<BitSet, List<String>>>();
        for(int playerIndex=0;playerIndex<playerNum;playerIndex++)
        {
        	String player = players.get(playerIndex);
        	List<BitSet> equivClasses = UtilPETL.getAllClassesOfPlayer(player, modelChecker);
        	Map<BitSet,List<String>> setToActions = new HashMap<BitSet,List<String>>();
        	int equivClassesNum = equivClasses.size();
        	for(int i=0;i<equivClassesNum;i++)
        	{
        		BitSet stateSet = equivClasses.get(i);
        		List<String> actions = new ArrayList<String>();
        		//there is at least one state, and all the states have the same actions set.
        		int state = stateSet.nextSetBit(0);

        		int numSucc = graph.getNumSuccessors(state);
        		StringBuilder builder = new StringBuilder();
        		for (int succNr = 0; succNr < numSucc; succNr++) {
                    EdgeProperty label = graph.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
                    Value value = label.get(state, succNr);
                    Action ac = (Action) ((ValueObject)value).getObject();
                    String globalAction = ac.getName();
                    String localAction = globalAction.split(",")[playerIndex];
                    if(!actions.contains(localAction))
                    {
                    	if(succNr > 0)
	                    	builder.append("+");
                    	actions.add(localAction);
                    	if(!pIsReal)
                    		solver.addVariable(getPVariableString(playerIndex, stateSet, localAction, graph), typeInteger, newValue(typeInteger, 0), newValue(typeInteger, 1));
                    	else
                    	{
                    		solver.addVariable(getPVariableString(playerIndex, stateSet, localAction, graph), typeReal, newValue(typeReal, 0), newValue(typeReal, 1));
                    		solver.addConstraint(UtilPETL.parseExpression(getPVariableString(playerIndex, stateSet, localAction, graph) + "< 1 =>" + getPVariableString(playerIndex, stateSet, localAction, graph) + "=0"));
                    	}
	                    builder.append(getPVariableString(playerIndex, stateSet, localAction, graph));
                    }
                }
        		builder.append("=1");
        		//System.out.println(builder.toString() + "     " + state);
        		solver.addConstraint(UtilPETL.parseExpression(builder.toString()));
        		setToActions.put(stateSet, actions);
        	}
        	playerSetToActions.put(player, setToActions);
        }
  
        for(int i=unKnown.nextSetBit(0);i>=0 && i<nodeNum;i=unKnown.nextSetBit(i+1))
        {
        	StringBuilder builder = new StringBuilder();
        	builder.append("x" + i + "=");
        	
        	List<List<String>> playerActions = new ArrayList<List<String>>();
        	Map<String, BitSet> playerToSet = new HashMap<String, BitSet>();
        	for(int j=0;j<playerNum;j++)
        	{
        		String player = players.get(j);
        		
        		BitSet bitSet = UtilPETL.getClassFor(player, i);
        		playerToSet.put(player, bitSet);
        		List<String> actions = playerSetToActions.get(player).get(bitSet);
        		playerActions.add(actions);
        	}
        	
        	List<List<String>> allPossibleActions = new ArrayList<List<String>>();
        	List<String> temp = new ArrayList<String>();
        	getAllPossibleActions(allPossibleActions,temp,playerActions);
        	int allActionNum = allPossibleActions.size();
        	for(int j=0;j<allActionNum;j++)
        	{
        		List<String> globalAction = allPossibleActions.get(j);
        		StringBuilder globalActionBuilder = new StringBuilder();
        		for(int k=0;k<playerNum;k++)
        		{
        			String localAction = globalAction.get(k);
        			String player = players.get(k);
        			globalActionBuilder.append(localAction);
        			if(k<playerNum-1)
        				globalActionBuilder.append(",");
        			builder.append(getPVariableString(k, playerToSet.get(player), localAction, graph) + "*");	
        		}
        		
        		String globalActionString = globalActionBuilder.toString();
        		builder.append("(");
        		int numSucc = graph.getNumSuccessors(i);
        		for (int succNr = 0; succNr < numSucc; succNr++) {
                    EdgeProperty label = graph.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
                    Value value = label.get(i, succNr);
                    Action ac = (Action) ((ValueObject)value).getObject();
                    if(!ac.getName().equals(globalActionString))
                    	continue;
                    
                    int succ = graph.getSuccessorNode(i, succNr);
                    assert !stateProp.getBoolean(succ);
                    
                    int num_Succ = graph.getNumSuccessors(succ);
                    for(int nr=0;nr<num_Succ;nr++)
                    {
                    	EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
                        Value pro = weight.get(succ, nr);
                        int succState = graph.getSuccessorNode(succ, nr);
                        //builder.append(((ValueReal) pro).getDouble() + "*x" + succState);
                        builder.append(pro + "*x" + succState);
                        if(nr<num_Succ-1)
                        	builder.append("+");
                    }
                    //there is exactly one globalAction action
                    break;
                }
        		builder.append(")");
        		if(j<allPossibleActions.size()-1)
        			builder.append("+");
        	}
        	//System.out.println(builder.toString());
        	solver.addConstraint(UtilPETL.parseExpression(builder.toString()));
        }
        
        return solver;
	}
	
	private static int computeNumberOfEdges(GraphExplicit graph)
    {
    	int result = 0;
    	int nodeNum = graph.getNumNodes();
    	NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
    	for(int node=0;node<nodeNum;node++)
    	{
    		if(!stateProp.getBoolean(node))
    			continue;
    		
    		int numSucc = graph.getNumSuccessors(node);
    		result += numSucc;
//    		for (int succNr = 0; succNr < numSucc; succNr++)
//    		{
//    			int succ = graph.getSuccessorNode(node, succNr);
//    			int num_Succ = graph.getNumSuccessors(succ);
//    			result += num_Succ;
//    		}
    	}
    	return result;
    }
	
	private static void addConstraintsOfFVariables(ConstraintSolver solver, BitSet unKnown, BitSet oneSet, BitSet zeroSet, GraphExplicit graph, ModelChecker modelChecker)
	{
		NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
		EdgeProperty label = graph.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
		EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
		
		int nodeNum = graph.getNumNodes();
		int upper = graph.computeNumStates() - zeroSet.cardinality();
		TypeInteger typeInteger = TypeInteger.get();
		TypeReal typeReal = TypeReal.get();
        for(int i=0;i<nodeNum;i++)
        {
        	if(stateProp.getBoolean(i))
        	{
        		solver.addVariable("f"+i, typeInteger, newValue(typeInteger, 0),newValue(typeInteger, upper));
        	}
        }
        
        List<String> players = new ArrayList<String>();
        List<Module> modules = ((ModelMAS) modelChecker.getModel()).getModelPrism().getModules();
        for(Module m : modules)
        {
        	players.add(m.getName());
        }
        
        for(int state=oneSet.nextSetBit(0);state>=0 && state<nodeNum;state=oneSet.nextSetBit(state+1))
        {
        	solver.addConstraint(UtilPETL.parseExpression("f" + state + "=1"));
        }
        for(int state=zeroSet.nextSetBit(0);state>=0 && state<nodeNum;state=zeroSet.nextSetBit(state+1))
        {
        	solver.addConstraint(UtilPETL.parseExpression("f" + state + "=0"));
        }
        for(int state=unKnown.nextSetBit(0);state>=0 && state<nodeNum;state=unKnown.nextSetBit(state+1))
        {
        	solver.addConstraint(UtilPETL.parseExpression("!(f" + state + "=1)"));
        	solver.addConstraint(UtilPETL.parseExpression("f" + state + "=0 => x" + state + "=0"));
        	
        	StringBuilder builder = new StringBuilder();
        	StringBuilder builderForSec = new StringBuilder();
        	builder.append("f" + state + ">1 =>");
        	builderForSec.append("f" + state + "=0 <=>");
        	
        	int numSucc = graph.getNumSuccessors(state);
        	Map<Integer, List<String>> stateToActions = new HashMap<Integer, List<String>>();
        	Map<Integer, List<String>> stateToProba = new HashMap<Integer, List<String>>();
        	for(int succIter = 0;succIter<numSucc;succIter++)
        	{
                Value value = label.get(state, succIter);
                Action ac = (Action) ((ValueObject)value).getObject();
                String globalAction = ac.getName();
                
        		int proNode = graph.getSuccessorNode(state, succIter);
        		int realNumSucc = graph.getNumSuccessors(proNode);
        		for(int realSuccIter=0;realSuccIter<realNumSucc;realSuccIter++)
        		{
        			String pro = weight.get(proNode, realSuccIter).toString();
        			int realSucc = graph.getSuccessorNode(proNode, realSuccIter);
        			
        			List<String> actions = stateToActions.get(realSucc);
        			List<String> probas = stateToProba.get(realSucc);
        			if(actions==null)
        			{
        				actions= new ArrayList<String>();
        				probas = new ArrayList<String>();
        			}
        			
    				actions.add(globalAction);
    				probas.add(pro);
    				stateToActions.put(realSucc, actions);
    				stateToProba.put(realSucc, probas);
        		}
        	}
        	int help = 0;
        	for(int succState : stateToActions.keySet())
        	{
        		if(help > 0)
        		{
        			builder.append("|");
        			builderForSec.append("&");
        		}
        		help++;
        		
        		builder.append("(");
        		builderForSec.append("(");
        		List<String> actions = stateToActions.get(succState);
        		List<String> probas = stateToProba.get(succState);
        		builder.append("f" + state + "=f" + succState + "+1 & (");
        		builderForSec.append("f" + succState + "=0 | ");
        		
        		StringBuilder builderHelp = new StringBuilder();
        		for(int iter = 0;iter<actions.size();iter++)
        		{
        			if(iter>0)
        			{
        				builder.append("|");
        				builderHelp.append("+");
        			}
        			String[] action = actions.get(iter).split(",");
        			String proba = probas.get(iter);
        			for(int playerIndex=0;playerIndex<players.size();playerIndex++)
        			{
        				String player = players.get(playerIndex);
        				BitSet bitSet = UtilPETL.getClassFor(player, state);
        				
            			builder.append(getPVariableString(playerIndex, bitSet, action[playerIndex], graph) + "*");
            			builderHelp.append(getPVariableString(playerIndex, bitSet, action[playerIndex], graph) + "*");
        			}
        			builder.append(proba + ">0");
        			builderHelp.append(proba);
        		}
        		builderForSec.append("(" + builderHelp.toString() + ")=0 )");
        		builder.append("))");
        	}
        	solver.addConstraint(UtilPETL.parseExpression(builder.toString()));
        	solver.addConstraint(UtilPETL.parseExpression(builderForSec.toString()));
        }   
	}
	
	private static double compuetMinProbability(ConstraintSolver solver, int state)
	{
		solver.setDirection(Direction.MIN);
    	solver.setObjective(UtilPETL.parseExpression("x" + state));
    	
    	StopWatch watch = new StopWatch(true);
    	System.out.println("Call z3 to compute the minimal probability ...");
    	ConstraintSolverResult result = solver.solve();
    	System.out.println("Time required by z3: " + watch.getTimeSeconds() + " seconds");
    	
    	solver.setObjective(null);
    	
    	if(result.equals(ConstraintSolverResult.UNSAT) || result.equals(ConstraintSolverResult.UNKNOWN))
    	{
    		System.out.println("z3 cannot compute the minimal probability for this problem");
    		return 2;
    	}
		return ((ValueDouble)solver.getResultVariablesValues()[state]).getDouble();
	}
	
	private static double compuetMaxProbability(ConstraintSolver solver, int state, BitSet unKnown)
	{
		solver.setDirection(Direction.MAX);
    	solver.setObjective(UtilPETL.parseExpression("x" + state));
    	
    	StopWatch watch = new StopWatch(true);
    	System.out.println("Call z3 to compute the maximal probability ...");
    	ConstraintSolverResult result = solver.solve();
    	System.out.println("Time required by z3: " + watch.getTimeSeconds() + " seconds");
    	
    	solver.setObjective(null);
    	
    	if(result.equals(ConstraintSolverResult.UNSAT) || result.equals(ConstraintSolverResult.UNKNOWN))
    	{
    		System.out.println("z3 cannot compute the maximal probability for this problem");
    		return 2;
    	}
		return ((ValueDouble)solver.getResultVariablesValues()[state]).getDouble();
	}
	
	static double[] computeProbabilities(ConstraintSolver solver, BitSet oneSet, BitSet zeroSet, boolean min, boolean negate, BitSet unKnown, StateSetExplicit computeForStates, GraphExplicit graph, ModelChecker modelChecker)
	{
		int size = computeForStates.size();
		double[] resultValue = new double[size];
		System.out.println("Number of transitions:" + computeNumberOfEdges(graph));
		
		if(!min)
		{
			addConstraintsOfFVariables(solver, unKnown, oneSet, zeroSet, graph, modelChecker);
		}
		System.out.println("Number of variables:" + ((ConstraintSolverSMTLib)solver).getNumberOfVariables());
		System.out.println("Number of constraints:" + ((ConstraintSolverSMTLib)solver).getNumberOfConstraints());
		
		for(int i=0;i<size;i++)
		{
			int state = computeForStates.getExplicitIthState(i);
			if(oneSet.get(state))
			{
				if(negate)
					resultValue[i] = 0.0;
				else
					resultValue[i] = 1.0;
			}
			else if(zeroSet.get(state))
			{
				if(negate)
					resultValue[i] = 1.0;
				else
					resultValue[i] = 0.0;
			}
			else
			{
				if(min)
				{
					double val = compuetMinProbability(solver,state);
					if(val <= 1.0)
					{
						if(negate)
							resultValue[i] = 1 - val;
						else
							resultValue[i] = val;
					}
					else
					{
						System.out.println("Well, z3 can not solve this and the program will terminate ...");
						System.exit(0);
					}
				}
				else
				{
					double val = compuetMaxProbability(solver,state, unKnown);
					if(val <= 1.0)
					{
						if(negate)
							resultValue[i] = 1 - val;
						else
							resultValue[i] = val;
					}
					else
					{

						System.out.println("Well, z3 can not solve this and the program will terminate ...");
						System.exit(0);
					}
				}
			}
		}
		return resultValue;
	}
}
