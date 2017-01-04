package epmc.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.constraintsolver.SMTSolver;
import epmc.constraintsolver.Z3SMT;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.prism.model.PredModel;

/**
 * PrismModel is a symbolic representation of PRISM model 
 * it contains initial states I(x)
 * transition relation        R(x,x')
 * probably we can use SMT solver to represent those expressions
 * */
public class PrismModel implements TSModel{
	
	private PredModel predModel;
	
    private final List<Expression> invariants;
    private Expression invariantExpr;
    /* disjunction */
    private final List<Expression> transRelation;
    private Expression transRelationExpr;
    private Expression initialStates;
    
    /* current state variable and primed state variable */
    private final Map<Expression, Expression> varsMap ;
    private Expression [] varsArr;
    private Expression [] primedVarsArr;
    /* action variable, integer variable */
    private Expression actionVar ;
    /* probability variable*/
    private Expression probVar   ;
    private boolean containProb = false;
    private Expression property;
	
	public PrismModel(PredModel predModel, Expression expression) throws EPMCException  {
		assert predModel != null;
		this.predModel = predModel;
		this.invariants = predModel.getInvariants();
		this.transRelation = predModel.getTransitions();
		this.varsMap = predModel.getVarMap();
		this.initialStates = predModel.getInitialNodes();
		this.actionVar = predModel.getActionVar();
		this.property = expression;
		prepareModel();
		prepareSMTContext();
	}

	private void prepareModel() {
		// invariants
		for(Expression inv : this.invariants) {
			if(this.invariantExpr == null) this.invariantExpr = inv;
			else this.invariantExpr = this.invariantExpr.and(inv);
		}
		// transition
		for(Expression assign : this.transRelation) {
			if(this.transRelationExpr == null) this.transRelationExpr = assign;
			else this.transRelationExpr = this.transRelationExpr.or(assign);
		}
		// variables 
		this.varsArr = new Expression[this.varsMap.size()];
		this.primedVarsArr = new Expression[this.varsMap.size()];
		int index = 0;
		for(Map.Entry<Expression, Expression> entry : this.varsMap.entrySet()) {
			this.varsArr[index] = entry.getKey();
			this.primedVarsArr[index] = entry.getValue();
			index ++;
		}
		
	}
	
	Z3SMT z3SmtContext = null;
	
	private void prepareSMTContext() throws EPMCException {
		
		z3SmtContext = new Z3SMT(getContext());
		
		for(Entry<Expression, Expression> varEntry : varsMap.entrySet()) {
			z3SmtContext.addVariable(varEntry.getKey(), varEntry.getKey().getType());
			z3SmtContext.addVariable(varEntry.getValue(), varEntry.getValue().getType());
		}
		
		z3SmtContext.addVariable(actionVar
				, getContext().getContextValue().getTypeInteger());
		
	}

	@Override
	public Expression getInitialStates() {
		return this.initialStates;
	}

	@Override
	public Expression getInvariants() {
		return this.invariantExpr;
	}

	@Override
	public Expression getTransitions() {
		return this.transRelationExpr;
	}

	@Override
	public Expression getErrorProperty() {
		return this.property;
	}

	@Override
	public Expression getPrimed(Expression expression) {
		return expression.replace(varsMap);
	}

	@Override
	public SMTSolver newSolver() {
		return z3SmtContext.newSolver();
	}

	@Override
	public Expression[] getVariables(boolean isPrimed) {
		if(isPrimed) return  this.primedVarsArr;
		return this.varsArr;
	}

	@Override
	public Expression getActionVariables() {
		return this.actionVar;
	}

	@Override
	public List<Expression> getTransitionsDNF() {
		return Collections.unmodifiableList(this.transRelation);
	}

	@Override
	public ContextExpression getContext() {
		return predModel.getContext();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		z3SmtContext.close();
	}
	

}
