package epmc.unambiguous.automaton;

import static epmc.value.OperatorNames.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.automaton.AutomatonDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.ExpressionToDD;
import epmc.expression.TemporalType;
import epmc.unambiguous.util.UtilUBA;

// According to Another Look at CTL model checking
public class AutomatonDDUBA implements AutomatonDD {

	public final static String IDENTIFIER = "uba-dd";
	
    private final ContextDD contextDD;
    private final ArrayList<DD> labels;
    private final ArrayList<DD> presVars;
    private final ArrayList<DD> nextVars;
    private final ArrayList<VariableDD> stateVariables = new ArrayList<>();
    private Map<Expression, Integer> elementaryForms = new HashMap<Expression, Integer>(); 
    private final ArrayList<Expression> elementaryArray = new ArrayList<>();
    private final DD presCube;
    private final DD nextCube;
    private final DD labelsCube;
    private final DD init;
    private final DD trans;
    private final ExpressionToDD expressionToDD;
    private Expression expression;
    private ArrayList<Expression> untils = new ArrayList<Expression>();
//    private GraphDD graph;
    private final ArrayList<DD> accLabels = new ArrayList<>();
    private DD initDD;
    private final Permutation bddPerm;
    /* constructors, states are initial states of model graph */
    public AutomatonDDUBA(ExpressionToDD expressionToDD
    		, Expression expression
    		, DD states, Permutation bddPerm) 
    		throws EPMCException {
        assert expressionToDD != null;
        assert expression != null;
        assert states != null;
        this.bddPerm = bddPerm;
//        this.graph = modelGraph;
        this.expression = expression;
        this.expressionToDD = expressionToDD;
        this.contextDD = expressionToDD.getContextDD(); /* context DD */
        this.labels = new ArrayList<>();                /* change numbers to DD form */           
        this.presVars = new ArrayList<>();              /* current vars */
        this.nextVars = new ArrayList<>();              /* next vars */
        /* get variables ready */
        createTableauVar();
        this.presCube = contextDD.listToCube(presVars);
        this.nextCube = contextDD.listToCube(nextVars);
        this.labelsCube = contextDD.listToCube(labels);
        
        /* get transition relation ready */
        DD tr = computeTableauTransition();
        this.trans = tr.andWith(states.clone());
         
        /* get fairness constraints ready */
        tableauFairness();
        
        /* get initial states ready */
        DD initDD = tableauInit().clone();
        this.init = initDD.andWith(states.clone());
    }


    @Override
    public DD getInitial() {
        return init;
    }

    @Override
    public DD getTransitions() {
        return trans;
    }

    @Override
    public List<DD> getPresVars() {
        return presVars;
    }
    
    @Override
    public List<DD> getNextVars() {
        return nextVars;
    }

    @Override
    public List<DD> getLabelVars() {
        return labels;
    }
    
    public List<DD> getLabels() {
		return accLabels;
    }
    
    public List<VariableDD> getStateVariables() {
        return stateVariables;
    }

    @Override
    public void close() {
        contextDD.dispose(labels);
        contextDD.dispose(presVars);
        contextDD.dispose(nextVars);
        contextDD.dispose(accLabels);
        presCube.dispose();
        nextCube.dispose();
        labelsCube.dispose();
        init.dispose();
        initDD.dispose();
        trans.dispose();
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public DD getLabelCube() {
        return labelsCube;
    }
    
    private void createTableauVar() throws EPMCException {
    	/* 1. transform it to elementary form */
    	expression = UtilUBA.ltlfiltSimplify(expression);
    	System.out.println("after simplification: " + expression);
    	expression = UtilUBA.elementaryForm(expression);
        Set<Expression> ap = new HashSet<>();
        /* 2. get atomic proposition from expression */
        UtilUBA.getAps(expression, ap);
        /* 2. calculate elementary formula which has form X p in expression */
        Set<Expression> elementaryFormulas = UtilUBA.getElementarySet(expression, ap);
        
        int stateVar = 0;
        /** every X p formula will have a new state variables
         * since atomic propositions are already defined in MC,
         * here we do not declare it again  */
        for(Expression el : elementaryFormulas) {
        	String varName = "%autoStateVar" + stateVar;
            VariableDD variable = contextDD.newBoolean(varName, 2);
            stateVariables.add(variable);
            assert stateVariables.get(stateVar) == variable;
            elementaryForms.put(el, stateVar);
            elementaryArray.add(el);
            assert elementaryArray.get(stateVar) == el;
            for (DD var : variable.getDDVariables(0)) {
                presVars.add(var.clone());
            }
            for (DD var : variable.getDDVariables(1)) {
                nextVars.add(var.clone());
            }
            stateVar ++;
        }

        /** labels in state , may be used in future  */
        Set<Expression> labs = new HashSet<>();
        UtilUBA.getUntilFormulas(expression, labs);
        int labelNr = 0;
        for(Expression lab : labs) {
        	/* here we do not need label */
            labels.add(contextDD.newConstant(true));
            untils.add(lab);
            assert untils.get(labelNr) == lab;
            labelNr ++;
        }
    	
    }
    /** characteristic function S_h of sat(h)
     * flag indicates primed or non-primed */
    private void nusmvSat(Expression expression, Map<Expression, DD> ddTable, boolean flag) 
    		throws EPMCException {
       	/** expression:  X p, new variables */
    	List<DD> vars = null;
    	/** determine version, true is current, false is next */
    	if(flag) vars = this.presVars;
    	else vars = this.nextVars;
    	
    	/* only X p */
    	if(elementaryForms.containsKey(expression)) {
    		DD result = vars.get(elementaryForms.get(expression));
    		ddTable.put(expression, result.clone());
    		return ;
    	}/* next version */
    	/* true */
    	if(expression.isTrue()) {
    		ddTable.put(expression, contextDD.newConstant(true));
    		return ;
    	}
    	/* propositional , but not trivial */
    	if(expression.isPropositional()) {
    		DD result = expressionToDD.translate(expression);
    		/* if next version, then permute */
    		if(! flag) result = result.permuteWith(bddPerm);
    		ddTable.put(expression, result);
    		return ;
    	}
    	/* already computed */
    	if(ddTable.containsKey(expression)) {
    		return ;
    	}
    	ContextExpression context = expression.getContext();
    	Expression op1, op2;
    	if(expression.isOperator()) {
    		op1 = expression.getOperand1();
    		switch(expression.getOperatorIdentifier()) {
			case     NOT:
				if(op1.isOperator() && (op1.getOperatorIdentifier() == NOT)) {
					nusmvSat(op1.getOperand1(), ddTable, flag);
					ddTable.put(expression, ddTable.get(op1.getOperand1()).clone());
				}
				else  {
					nusmvSat(op1, ddTable, flag);
					ddTable.put(expression, ddTable.get(op1).not());
				}
				break;
			case     AND :
				op2 = expression.getOperand2();
				nusmvSat(op1, ddTable, flag);
				nusmvSat(op2, ddTable, flag);
				DD result = ddTable.get(op1).clone();
				result = result.andWith(ddTable.get(op2).clone());
				ddTable.put(expression, result);
				break;
			case     OR :
				op2 = expression.getOperand2();
				nusmvSat(op1, ddTable, flag);
				nusmvSat(op2, ddTable, flag);
				DD dd = ddTable.get(op1).clone();
				dd = dd.orWith(ddTable.get(op2).clone());
				ddTable.put(expression, dd);
				break;
			default:
				System.err.println("unvalid operator " + expression.getOperatorIdentifier());
    		    System.exit(-1);
    		}
    		return ;
    	}
    	if(expression.isTemporal()) {
			switch (expression.getTemporalType()) {
			case NEXT:
				assert false : "impossible NEXT modality";
			    break;
			case UNTIL:
				op1 = expression.getOperand1();
				op2 = expression.getOperand2();
				nusmvSat(op1, ddTable, flag);
				nusmvSat(op2, ddTable, flag);
				DD result = ddTable.get(op1).clone();
				Expression xop = context.newNext(expression);
				nusmvSat(xop, ddTable, flag);
				result = result.andWith(ddTable.get(xop).clone());
				result = result.orWith(ddTable.get(op2).clone());
				ddTable.put(expression, result);
				break;
			default:
				assert false : "unvalid temporal modality";
				break;
    	   }
    	}
    }
    
    /** here we use X p <-> p, need to focus on */
    private DD computeTableauTransition() throws EPMCException {
    	
    	DD result = contextDD.newConstant(true);
    	HashMap<Expression, DD> computedTable = new HashMap<>();
    	/* assume that only Xp and f in presVars */
        for(int preVar = 0; preVar < this.presVars.size(); preVar ++) {
            assert elementaryArray.get(preVar).getTemporalType() == TemporalType.NEXT;
        	/** X p <-> p */
            Expression op = elementaryArray.get(preVar).getOperand1();
        	nusmvSat(op, computedTable, false);
        	DD nextImg = computedTable.get(op).clone();
        	nextImg = nextImg.iffWith(presVars.get(preVar).clone());
        	result = result.andWith(nextImg);
        }
        /** clear some DD */
        for(DD ddVal : computedTable.values()) {
        	ddVal.dispose();
        }
        
		return result;
    }
    
    private void tableauFairness() throws EPMCException {
        HashMap<Expression, DD> computedTable = new HashMap<>();
        /** we have labels now, do not know whether use label  */
        for(int labelNr = 0 ; labelNr < labels.size() ; labelNr ++ ) {
        	Expression formula = untils.get(labelNr);        /* until formula g U h */
        	Expression constraint = formula.not().or(formula.getOperand2()); /* not(g U h) or h */
        	nusmvSat(constraint, computedTable, true);
        	DD nextOn = computedTable.get(constraint).clone();          /* this is next version */
        	accLabels.add(nextOn);
        	assert accLabels.get(labelNr) == nextOn;
        }
        
        /* use this to calculate initial state */
        nusmvSat(expression, computedTable, true);
        initDD = computedTable.get(expression).clone();
        
        for(DD ddVal : computedTable.values()) {
        	ddVal.dispose();
        }
    }
    
    /* initial states  */
    private DD tableauInit() throws EPMCException   {
    	return initDD;
    }

}
