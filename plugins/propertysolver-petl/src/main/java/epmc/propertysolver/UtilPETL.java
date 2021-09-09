package epmc.propertysolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.modelchecker.ModelChecker;
import epmc.petl.model.EquivalenceClasses;
import epmc.petl.model.ExpressionKnowledge;
import epmc.petl.model.KnowledgeType;
import epmc.prism.model.PropertyPRISM;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

public class UtilPETL {
	private static EquivalenceClasses equivalenceClasses;
	
	public static Set<Expression> collectPETLInner(Expression expression) {        
        if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return collectPETLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            return collectPETLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            return collectPETLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPETLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPETLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPETLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPETLInner(expressionTemporal.getOperandRight()));
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }
	
	public static Set<Expression> collectIdentifiers(Expression expression) {
        assert expression != null;
        if (expression instanceof ExpressionIdentifier) {
            return Collections.singleton(expression);
        }
        Set<Expression> result = new HashSet<>();
        for (Expression child : expression.getChildren()) {
            result.addAll(collectIdentifiers(child));
        }
        return result;
    }
	
	public static List<BitSet> getAllClassesOfPlayer(String player, ModelChecker modelChecker)
	{
		if(equivalenceClasses == null || !equivalenceClasses.isInitalized())
		{
			equivalenceClasses = new EquivalenceClasses(modelChecker);
		}
		
		return equivalenceClasses.getClassesOfPlayer(player);
	}
	
	public static BitSet getClassFor(String player, int state)
	{
		assert equivalenceClasses != null;

		return equivalenceClasses.getClassFor(player,state);
	}
	
	public static BitSet getEquivalenceClass(int state, Expression expression, ModelChecker modelChecker)
	{
		assert expression instanceof ExpressionKnowledge;
		
		if(equivalenceClasses == null || !equivalenceClasses.isInitalized())
		{
			equivalenceClasses = new EquivalenceClasses(modelChecker);
		}
		ExpressionKnowledge exp = (ExpressionKnowledge) expression;
		KnowledgeType type = exp.getType();
		if(type == KnowledgeType.K)
		{
			assert exp.getPlayers().size() == 1;
			return equivalenceClasses.getClassFor(exp.getPlayers().get(0), state);
		}
		else
		{
			if(type == KnowledgeType.E)
			{
				return computeEveryoneKnowledge(exp.getPlayers(),state);
			}
			else if(type == KnowledgeType.C)
			{
				int size = 1;
				BitSet res = computeEveryoneKnowledge(exp.getPlayers(),state);
				while(size != res.cardinality())
				{
					size = res.cardinality();
					for(int i=res.nextSetBit(0);i>=0;i=res.nextSetBit(i+1))
					{
						res.or(computeEveryoneKnowledge(exp.getPlayers(),i));
					}
				}

				return res;
			}
			else if(type == KnowledgeType.D)
			{
				BitSet res = null;
				for(String player : exp.getPlayers())
				{
					BitSet tmp = equivalenceClasses.getClassFor(player,state);
					if(res == null)
						res = tmp;
					else
						res.and(tmp);
				}
				return res;
			}
		}
		
		return null;
	}
	
	private static BitSet computeEveryoneKnowledge(List<String> players, int state)
	{
		BitSet res = null;
		for(String player : players)
		{
			BitSet tmp = equivalenceClasses.getClassFor(player,state);
			if(res == null)
				res = tmp;
			else
				res.or(tmp);
		}
		return res;
	}
	
	public static boolean isSubsetOf(BitSet op1, BitSet op2)
	{
		assert op1 != null;
		assert op2 != null;
		
		for(int i= op1.nextSetBit(0);i>=0;i=op1.nextSetBit(i+1))
		{
			if(!op2.get(i))
				return false;
		}
		return true;
	}
	
	public static boolean isPCTLPath(Expression pathProp)
	{
		if (ExpressionTemporalNext.is(pathProp)) {
            ExpressionTemporalNext next = ExpressionTemporalNext.as(pathProp);
            return isPCTLState(next.getOperand());
        } else if (ExpressionTemporalFinally.is(pathProp)) {
            ExpressionTemporalFinally expFinally = ExpressionTemporalFinally.as(pathProp);
            return isPCTLState(expFinally.getOperand());
        } else if (ExpressionTemporalGlobally.is(pathProp)) {
            ExpressionTemporalGlobally expGlobally = ExpressionTemporalGlobally.as(pathProp);
            return isPCTLState(expGlobally.getOperand());
        } else if (ExpressionTemporalRelease.is(pathProp)) {
            ExpressionTemporalRelease expRelease = ExpressionTemporalRelease.as(pathProp);
            return isPCTLState(expRelease.getOperandLeft())
                    && isPCTLState(expRelease.getOperandRight());
        } else if (ExpressionTemporalUntil.is(pathProp)) {
            ExpressionTemporalUntil expRelease = ExpressionTemporalUntil.as(pathProp);
            return isPCTLState(expRelease.getOperandLeft())
                    && isPCTLState(expRelease.getOperandRight());
        } else {
            return false;
        }
	}
	
	public static boolean isPCTLState(Expression stateProp)
	{
		if (!(stateProp instanceof ExpressionPropositional) && !(stateProp instanceof ExpressionKnowledge) && !(stateProp instanceof ExpressionQuantifier)) {
            return false;
        }
		
		if(stateProp instanceof ExpressionPropositional)
		{
			if(!(stateProp instanceof ExpressionOperator))
				return true;
			
			ExpressionOperator asOperator = (ExpressionOperator) stateProp;
			for(Expression operand : asOperator.getOperands())
			{
				if(!isPCTLState(operand))
					return false;
			}
		}
		if(stateProp instanceof ExpressionKnowledge)
		{
			ExpressionKnowledge asKnowledge = (ExpressionKnowledge) stateProp;
			if(!isPCTLState(asKnowledge.getQuantifier()))
				return false;
		}
		if(stateProp instanceof ExpressionQuantifier)
		{
			ExpressionQuantifier asQuantifier = (ExpressionQuantifier) stateProp;
			if(!isPCTLPath(asQuantifier.getQuantified()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static Set<Expression> collectPCTLInner(Expression expression) {
		if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPCTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPCTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPCTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPCTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (expression instanceof ExpressionKnowledge) {
        	ExpressionKnowledge expressionKnowledge = (ExpressionKnowledge) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionKnowledge.getChildren()) {
                result.addAll(collectPCTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }
	
	public static boolean isPCTLPathUntil(Expression pathProp) {
        if (!isPCTLPath(pathProp)) {
            return false;
        }
        if (ExpressionTemporalFinally.is(pathProp)) {
            return true;
        }
        if (ExpressionTemporalGlobally.is(pathProp)) {
            return true;
        }
        if (ExpressionTemporalRelease.is(pathProp)) {
            return true;
        }
        if (ExpressionTemporalUntil.is(pathProp)) {
            return true;
        }
        return false;
    }
	
	static BitSet getUnKnownStates(BitSet oneSet, BitSet zeroSet, GraphExplicit graph)
	{
		BitSet unKnown = UtilBitSet.newBitSetUnbounded();
		int nodeNum = graph.getNumNodes();
		
		NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        for(int i=0;i<nodeNum;i++)
        {
        	if (!stateProp.getBoolean(i) || oneSet.get(i) || zeroSet.get(i)) {
                continue;
            }
        	unKnown.set(i);
        }
        
        //unKnown states should be able to reach some oneState, otherwise they are zeroState
        BitSet canReach = UtilBitSet.newBitSetUnbounded();
        for(int i=unKnown.nextSetBit(0);i>=0 && i<nodeNum;i=unKnown.nextSetBit(i+1))
        {
        	Stack<Integer> stack = new Stack<Integer>();
        	stack.push(i);
        	BitSet visited = UtilBitSet.newBitSetUnbounded();
        	visited.set(i);
        	boolean canReachOneState =false;
        	while(!stack.isEmpty())
        	{
        		int state = stack.pop();
        		int numSucc = graph.getNumSuccessors(state);
        		boolean found_outer = false;
        		for (int succNr = 0; succNr < numSucc; succNr++)
        		{
        			int succ = graph.getSuccessorNode(state, succNr);
        			assert !stateProp.getBoolean(succ);
                    
                    int num_Succ = graph.getNumSuccessors(succ);
                    boolean found_inner = false;
                    for(int nr=0;nr<num_Succ;nr++)
                    {
                    	int succState = graph.getSuccessorNode(succ, nr);
                    	if(oneSet.get(succState))
                    	{
                    		canReach.set(i);
                    		found_inner = true;
                    		break;
                    	}
                    	else if(canReach.get(succState))
                    	{
                    		found_inner = true;
                    		break;
                    	}
                    	else
                    	{
                    		if(!visited.get(succState))
                    		{
                    			visited.set(succState);
                    			if(!zeroSet.get(succState))
                    				stack.push(succState);
                    		}
                    	}
                    }
                    if(found_inner)
                    {
                    	found_outer = true;
                    	break;
                    }
        		}
        		if(found_outer)
        		{
        			canReachOneState = true;
        			break;
        		}
        	}
        	if(!canReachOneState)
        	{
        		for(int k= visited.nextSetBit(0);k>=0 && k<nodeNum;k=visited.nextSetBit(k+1))
            	{
            		zeroSet.set(k);
            		unKnown.clear(k);
            	}
        	}
        }
        return unKnown;
	}
	
	public static Expression parseExpression(String exp)
	{
		//System.out.println(exp.length());
		InputStream stream = new ByteArrayInputStream(exp.getBytes());
        return new PropertyPRISM().parseExpression(exp.toString(), stream);
	}
}
