package epmc.constraintsolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;
import static epmc.value.OperatorNames.*;

import com.microsoft.z3.*;

public class ConstraintSolverSMT  {

    HashMap<String, String> cfg = new HashMap<String, String>();
    Context context = null;
    private final ConstraintSolverProblem solverProblem;
    private int numVariables;
    private int numConstraints;
    private boolean solveInquality ;
    private boolean closed;
    private boolean working ;
    private boolean hasResult;
    private ArrayList<Expr> variables = new ArrayList<Expr>();
    private ArrayList<Expr> assumptions = new ArrayList<Expr>();
    private HashMap<String, Expr> varMap = new HashMap<String, Expr>();
    private Status smtResult ;
    private Solver solver ;
    private ContextExpression contextExpr;
    static {
    	try {
    		System.loadLibrary("z3");
    		System.loadLibrary("z3java");
    	}catch(Exception e) {
    		System.err.println("can not load z3 and z3java");
    		e.printStackTrace();
    	}
    }
    /** We do not need to fix the number of constraints and variables, thus
     * no need to initialize number of variables and constraints
     * @param input problem we should solve 
     * */
    public ConstraintSolverSMT(ConstraintSolverProblem solverProblem) {
        assert solverProblem != null;
        this.solverProblem = solverProblem;
        this.numConstraints = 0;
        this.numVariables = 0;
        this.closed = false;
        this.working = false;
        this.solveInquality = false;
        cfg.put("model", "true");
        context = new Context(cfg);
        solver = context.mkSolver();
    }
    public ConstraintSolverSMT(ContextExpression contextExpr) {
        this.solverProblem = null;
        this.numConstraints = 0;
        this.numVariables = 0;
        this.closed = false;
        this.working = false;
        this.solveInquality = false;
        cfg.put("model", "true");
        context = new Context(cfg);
        solver = context.mkSolver();
        this.hasResult = false;
        this.contextExpr = contextExpr;
    }
    
    public void showVars() {
    	for(Entry<String, Expr> var : varMap.entrySet()) {
    		System.out.println("str: " + var.getKey() + " val: " + var.getValue());
    	}
    }
    /** 
     * @param name variable name 
     * @param type only be double, integer, boolean 
     * */
	public int addVariable(String name, Type type) {
		// TODO Auto-generated method stub
        assert !closed;
        assert type != null;
        assert type.isReal() || type.isInteger() || type.isBoolean();
        Expr var = null;
        if(type.isInteger()) {
        	var = context.mkIntConst(name);
        }else if(type.isBoolean()) {
        	var = context.mkBoolConst(name);
        }else {
        	FPSort s = context.mkFPSort(type.getLower(), type.getUpper());
        	var = (FPExpr)context.mkConst(name, s);
        }	
        assert var != null;
        this.variables.add(var);
        varMap.put(name, var);
        assert var == this.variables.get(numVariables);
        assert var == this.varMap.get(name);
        this.numVariables ++;
		return this.numVariables;
	}
	/**
	 * another way to add varibales */
	public int addVariable(Expression variable, Type type) {
        assert !closed;
        assert variable != null;
        return addVariable(variable.getName(), type);
	}
    /**
     * @param row coefficient of a row in matrix
     * @param variables counter of each variable? ?
     * @param constraintType LE, GE, GT or LT
     * @param rightHandSide values of right hand
     * */
	public void addConstraint(Value row, int[] variables,
			ConstraintType constraintType, Value rightHandSide) {
		// TODO Auto-generated method stub
		assert variables.length <= this.variables.size();
		if(!this.solveInquality){
			this.solveInquality = true;
		}
		Expr sum = this.context.mkInt(0);
		Value val = this.contextExpr.getContextValue().getTypeWeight().newValue();
		for(int index = 0; index < variables.length ; index ++) {
			assert variables[index] < this.variables.size();
			row.get(val, index);
			ArithExpr operand = this.context.mkMul((ArithExpr)exprTransform(this.contextExpr.newLiteral(val))
					, (ArithExpr)this.variables.get(variables[index]));
			sum = this.context.mkAdd((ArithExpr)sum, operand);
		}
	    ArithExpr rightHand = (ArithExpr) exprTransform(this.contextExpr.newLiteral(rightHandSide));
		switch(constraintType) {
		case LE:
			sum = this.context.mkLe((ArithExpr)sum, rightHand);
		case GE:
			sum = this.context.mkGe((ArithExpr)sum, rightHand);
		case EQ:
			sum = this.context.mkEq((ArithExpr)sum, rightHand);
		default:
			assert false : "unvalid constraint type";
		}
		
		this.solver.add((BoolExpr)sum);
		this.numConstraints ++;
		this.hasResult = false;
	}
	/*
	 * suppose row = [c1,c2, ..., cn], variables = [b1,c2,...,bn] 
	 * */
 
	public void addConstraint(Value[] row, int[] variables,
			ConstraintType constraintType, Value rightHandSide) {
		// TODO Auto-generated method stub
		if(!this.solveInquality){
			this.solveInquality = true;
		}
        assert !closed;
        assert row != null;
        assert variables != null;
        assert constraintType != null;
        assert rightHandSide != null;
        
        for(int i = 0 ; i < row.length ; i ++) {
        	this.addConstraint(row[i], variables, constraintType, rightHandSide);
        }
        
	}
	
	private Expr exprTransform(Expression expr) {
		assert expr != null;
		assert expr.isOperator() || expr.isIdentifier() || expr.isLiteral();
		
		if(expr.isIdentifier()) {
	        String name = expr.getName();
//	        System.out.println("name: " + name);
	        assert varMap.get(name) != null;
	        return varMap.get(name);
		}else if(expr.isLiteral()){
			Value value = expr.getValue();
			assert value.isBoolean() || value.isDouble() || value.isInteger();
			if(value.isBoolean()) {
				return context.mkBool(value.getBoolean());
			}else if(value.isInteger()){
				return context.mkInt(value.getInt());
			}
			FPSort sort = context.mkFPSort(value.getBoundLower(), value.getBoundUpper());
			return context.mkFP(value.getDouble(), sort);
		}
		ArrayList<Expr> exprArr = new ArrayList<Expr>();
		Operator operator = expr.getOperator();
		Expr result = null, op1, op2, op3;
		switch(operator.getIdentifier()) {
		case     ADD : 
			for (Expression e : expr.getChildren()) {
				exprArr.add(exprTransform(e));
			}
			result = context.mkAdd((ArithExpr[]) exprArr.toArray(new ArithExpr[]{}));
			break;
		case     MULTIPLY : 
			for (Expression e : expr.getChildren()) {
				exprArr.add(exprTransform(e));
			}
			result = context.mkMul((ArithExpr[]) exprArr.toArray(new ArithExpr[]{}));
			break;
		case     SUBTRACT :
			for (Expression e : expr.getChildren()) {
				exprArr.add(exprTransform(e));
			}
			result = context.mkSub((ArithExpr[]) exprArr.toArray(new ArithExpr[]{}));
			break;
		case     AND :
			for (Expression e : expr.getChildren()) {
				exprArr.add(exprTransform(e));
			}
			result = context.mkAnd((BoolExpr[]) exprArr.toArray(new BoolExpr[]{}));
			break;
		case     OR :
			for (Expression e : expr.getChildren()) {
				exprArr.add(exprTransform(e));
			}
			result = context.mkOr((BoolExpr[]) exprArr.toArray(new BoolExpr[]{}));
			break;
		case     NOT:
			op1 =  exprTransform(expr.getOperand1());
			assert op1.isBool();
			result = context.mkNot((BoolExpr)op1);
			break;
		case     IFF :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkIff((BoolExpr)op1, (BoolExpr)op2);
			break;
		case     IMPLIES : 
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkImplies((BoolExpr)op1, (BoolExpr)op2);
			break;
		case     ITE :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			op3 = exprTransform(expr.getOperand3());
			result = context.mkITE((BoolExpr)op1, op2, op3);
			break;
		case     MOD :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkMod((IntExpr)op1, (IntExpr)op2);
			break;
		case     POW :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkPower((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     DIVIDE :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkDiv((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     GT :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkGt((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     GE :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkGe((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     LT :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkLt((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     LE :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkLe((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     EQ :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkEq((ArithExpr)op1, (ArithExpr)op2);
			break;
		case     NE :
			op1 = exprTransform(expr.getOperand1());
			op2 = exprTransform(expr.getOperand2());
			result = context.mkEq((ArithExpr)op1, (ArithExpr)op2);
			result = context.mkNot((BoolExpr) result);
			break;
		case     CEIL :
		case     FLOOR :
		case     LOG :
		case     MAX :
		case     MIN :
			assert false;
		default:
				assert false;
		
		}
		return result;
	}
    /**
     * add constraints according to the expression */
	public void addConstraint(Expression expression) throws EPMCException {
		// TODO Auto-generated method stub
		assert expression != null;
		this.working = true;
		Expr constraint = exprTransform(expression);
		solver.add((BoolExpr)constraint);
		this.numConstraints ++;
		this.hasResult = false;
	}

	public void addConstraint(DD constraint) throws EPMCException {
		// TODO Auto-generated method stub
		assert false;
	}

	public void setObjective(Expression objective) {
		// TODO Auto-generated method stub
		assert objective != null;
		this.working = true;
		Expr assumption = exprTransform(objective);
		assumptions.add((BoolExpr)assumption);
	}

	public void setObjective(Value row, int[] variables) {
		// TODO Auto-generated method stub
		assert false;
	}

	public void setObjective(Value[] row, int[] variables) {
		// TODO Auto-generated method stub
		assert false;
	}

	public void setDirection(Direction direction) {
		// TODO Auto-generated method stub
		assert false;
	}

	public ConstraintSolverResult solve() {
		// TODO Auto-generated method stub
		smtResult = solver.check((Expr[]) assumptions.toArray(new Expr[]{}));
		this.hasResult = true;
		this.working = false;
		if(smtResult == Status.SATISFIABLE) 
			return ConstraintSolverResult.SAT;
		else if(smtResult == Status.UNSATISFIABLE)
			return ConstraintSolverResult.UNSAT;
		return ConstraintSolverResult.UNKNOWN;
	}
    /* get model of variables */
	public Value getOptimalVariablesValue() {
		// TODO Auto-generated method stub
		assert this.hasResult && this.smtResult == Status.SATISFIABLE;
		Type typeWeight = contextExpr.getContextValue().getTypeWeight();
		Value resultValues = typeWeight.getTypeArray().newValue(this.variables.size());
		
		for(int index = 0; index < this.variables.size() ; index ++) {
			resultValues.set(this.getOptimalVariablesValue(index), index);
		}
		return resultValues;
	}
	
	public Value getOptimalVariablesValue(int index) {
		assert index >= 0 && index < this.variables.size();
		assert this.hasResult && this.smtResult == Status.SATISFIABLE;
		Expr value = solver.getModel().eval(this.variables.get(index), false);
		if(value.isTrue()) return contextExpr.getContextValue().getTrue();
		if(value.isFalse()) return contextExpr.getContextValue().getFalse();
		if(value.isReal()) return contextExpr.getContextValue().newReal(value.toString());
		if(value.isInt()) return contextExpr.getContextValue().newInteger(value.toString());
//		System.out.println("expr" + value);
		assert false : "UNKNOWN VLAUE";
		return null;//solver.getModel().eval(this.variables.get(index), false);
	}
    /* get UNSAT CORE */
	public Value getOptimalObjectiveValue() {
		// TODO Auto-generated method stub
		assert ! assumptions.isEmpty();
		assert this.hasResult && this.smtResult == Status.UNSATISFIABLE;
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
        if (closed) {
            return;
        }
        assert !this.working : "solver is still working";
        this.closed = true;
        this.variables.clear();
        this.assumptions.clear();
        this.solver.dispose();
        this.context.dispose();
	}
	
	public void showResult() {
        Model model = null;
        if (smtResult !=  Status.SATISFIABLE)
        	assert false : "UNSAT problem"; 
       model = solver.getModel();
       
       for(Entry<String, Expr> entry : this.varMap.entrySet()) {
           System.out.println(entry.getValue() + " -> " + model.eval(entry.getValue(), false));
       }

	}
	
	public static void testZ3(ContextExpression context) throws EPMCException {
		ContextValue contextValue = context.getContextValue();
		ConstraintSolverSMT smtSolver = new ConstraintSolverSMT(context);
		Expression t11 = context.newIdentifier("t11");
		Expression t12 = context.newIdentifier("t12");
		Expression t21 = context.newIdentifier("t21");
		Expression t22 = context.newIdentifier("t22");
		Expression t31 = context.newIdentifier("t31");
		Expression t32 = context.newIdentifier("t32");
		Type intType = contextValue.getTypeInteger();
		
		/* t11 >= 0 & t12 >= (t11 + 2) & (t12 + 1 <= 8)*/
		Expression t11ge1 = context.newOperator(GE, t11, context.getZero());
		Expression t12ge1 = context.newOperator(GE, t12, context.newOperator(ADD, t11, context.newLiteral(2)));
		Expression t12le8 = context.newOperator(LE, context.newOperator(ADD, t12, context.getOne()), context.newLiteral(8));
		Expression exp1 = context.newOperator(AND, t11ge1, t12ge1, t12le8);
		/* t21 >= 0 & t22 >= (t21 + 3) & (t22 + 1 <= 8)*/
		Expression t21ge1 = context.newOperator(GE, t21, context.getZero());
		Expression t22ge1 = context.newOperator(GE, t22, context.newOperator(ADD, t21, context.newLiteral(3)));
		Expression t22le8 = context.newOperator(LE, context.newOperator(ADD, t22, context.getOne()), context.newLiteral(8));
		Expression exp2 = context.newOperator(AND, t21ge1, t22ge1, t22le8);
		/* t31 >= 0 & t32 >= (t31 + 2) & (t32 + 3 <= 8)*/
		Expression t31ge1 = context.newOperator(GE, t31, context.getZero());
		Expression t32ge1 = context.newOperator(GE, t32, context.newOperator(ADD, t31, context.newLiteral(2)));
		Expression t32le8 = context.newOperator(LE, context.newOperator(ADD, t32, context.newLiteral(3)), context.newLiteral(8));
		Expression exp3 = context.newOperator(AND, t31ge1, t32ge1, t32le8);
		
		/*t11 >= t21 + 3 | t21 >= t11 + 2*/
		Expression exp4 = context.newOperator(OR, context.newOperator(GE, t11, context.newOperator(ADD, t21, context.newLiteral(3)))
				                                , context.newOperator(GE, t21, context.newOperator(ADD, t11, context.newLiteral(2))));
		/*t11 >= t31 + 2 | t31 >= t11 + 2*/
		Expression exp5 = context.newOperator(OR, context.newOperator(GE, t11, context.newOperator(ADD, t31, context.newLiteral(2)))
				                                , context.newOperator(GE, t31, context.newOperator(ADD, t11, context.newLiteral(2))));
		
		/*t21 >= t31 + 2 | t31 >= t21 + 3*/
		Expression exp6 = context.newOperator(OR, context.newOperator(GE, t21, context.newOperator(ADD, t31, context.newLiteral(2)))
				                                , context.newOperator(GE, t31, context.newOperator(ADD, t21, context.newLiteral(3))));
		
		/*t12 >= t22 + 1 | t22 >= t12 + 1*/
		Expression exp7 = context.newOperator(OR, context.newOperator(GE, t12, context.newOperator(ADD, t22, context.newLiteral(1)))
				                                , context.newOperator(GE, t22, context.newOperator(ADD, t12, context.newLiteral(1))));
		
		/*t12 >= t32 + 3 | t32 >= t12 + 1*/
		Expression exp8 = context.newOperator(OR, context.newOperator(GE, t12, context.newOperator(ADD, t32, context.newLiteral(3)))
				                                , context.newOperator(GE, t32, context.newOperator(ADD, t12, context.newLiteral(1))));
		
		/*t22 >= t32 + 3 | t32 >= t22 + 1*/
		Expression exp9 = context.newOperator(OR, context.newOperator(GE, t22, context.newOperator(ADD, t32, context.newLiteral(3)))
				                                , context.newOperator(GE, t32, context.newOperator(ADD, t22, context.newLiteral(1))));
		/* add variables */
		
		smtSolver.addVariable(t11.getName() , intType);
		smtSolver.addVariable(t12.getName() , intType);
		smtSolver.addVariable(t21.getName(), intType);
		smtSolver.addVariable(t22.getName(), intType);
		smtSolver.addVariable(t31.getName(), intType);
		smtSolver.addVariable(t32.getName(), intType);
		
		smtSolver.showVars();
		
		smtSolver.addConstraint(exp1);
		System.out.println("add " + exp1);
		smtSolver.addConstraint(exp2);
		System.out.println("add " + exp2);
		smtSolver.addConstraint(exp3);
		System.out.println("add " + exp3);
		smtSolver.addConstraint(exp4);
		System.out.println("add " + exp4);
		smtSolver.addConstraint(exp5);
		System.out.println("add " + exp5);
		smtSolver.addConstraint(exp6);
		System.out.println("add " + exp6);
		smtSolver.addConstraint(exp7);
		System.out.println("add " + exp7);
		smtSolver.addConstraint(exp8);
		System.out.println("add " + exp8);
		smtSolver.addConstraint(exp9);
		System.out.println("add " + exp9);
		
		smtSolver.solve();
		
		smtSolver.showResult();
		Expression [] vars = {t11, t12, t21, t22, t31, t32};
		for(int i = 0; i < 6; i ++ ) {
			System.out.println("var: " + vars[i] + " val: " + smtSolver.getOptimalVariablesValue(i));
		}
		
		Value value = smtSolver.getOptimalVariablesValue();
		Value result = context.getContextValue().getTypeWeight().newValue();
		for(int i = 0 ; i < value.size() ; i ++) {
			value.get(result, i);
			System.out.println("var: " + vars[i] + " val: " + result);
		}
		
		quantifierExample1();
		
		System.out.println("test fairness");
		Expression l0 = context.newIdentifier("l0");
		Expression l1 = context.newIdentifier("l1");
		Expression l2 = context.newIdentifier("l2");
		Expression l3 = context.newIdentifier("l3");
		Expression l4 = context.newIdentifier("l4");
		
		Expression inner = context.newOperator(AND, context.newOperator(OR, l0, l1)
				, context.newOperator(OR, context.newFinally(l2), context.newGlobally(l3)));
		Expression outter = context.newOperator(AND, context.newFinally(context.newGlobally(inner))
				, context.newFinally(context.newGlobally(l4)));
		System.out.println("EXPR: " + outter);
		
		
	}
	
    static void quantifierExample1()
    {
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("proof", "true");
        cfg.put("auto-config", "false");
        Context ctx = new Context(cfg);
        System.out.println("QuantifierExample");
//        Log.append("QuantifierExample");

        Sort[] types = new Sort[3];
        IntExpr[] xs = new IntExpr[3];
        Symbol[] names = new Symbol[3];
        IntExpr[] vars = new IntExpr[3];

        for (int j = 0; j < 3; j++)
        {
            types[j] = ctx.getIntSort();
            names[j] = ctx.mkSymbol("x_" + Integer.toString(j));
            xs[j] = (IntExpr) ctx.mkConst(names[j], types[j]);
            vars[j] = (IntExpr) ctx.mkBound(2 - j, types[j]); // <-- vars
                                                              // reversed!
        }

        Expr body_vars = ctx.mkAnd(
                ctx.mkEq(ctx.mkAdd(vars[0], ctx.mkInt(1)), ctx.mkInt(2)),
                ctx.mkEq(ctx.mkAdd(vars[1], ctx.mkInt(2)),
                        ctx.mkAdd(vars[2], ctx.mkInt(3))));

        Expr body_const = ctx.mkAnd(
                ctx.mkEq(ctx.mkAdd(xs[0], ctx.mkInt(1)), ctx.mkInt(2)),
                ctx.mkEq(ctx.mkAdd(xs[1], ctx.mkInt(2)),
                        ctx.mkAdd(xs[2], ctx.mkInt(3))));

        Expr x = ctx.mkForall(types, names, body_vars, 1, null, null,
                ctx.mkSymbol("Q1"), ctx.mkSymbol("skid1"));
        System.out.println("Quantifier X: " + x.toString());

        Expr y = ctx.mkForall(xs, body_const, 1, null, null,
                ctx.mkSymbol("Q2"), ctx.mkSymbol("skid2"));
        System.out.println("Quantifier Y: " + y.toString());
    }
	
	public static void test() {
		
		/**/
		
	  	String property = System.getProperty("java.library.path");
    	StringTokenizer parser = new StringTokenizer(property, ";");
    	while (parser.hasMoreTokens()) {
    	    System.err.println(parser.nextToken());
    	}
    	File dir = new File(".");
    	try {
			System.out.println(dir.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("proof", "true");
        cfg.put("auto-config", "false");
        Context ctx = new Context(cfg);
        System.out.println("SimplifierExample");


        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");
        IntExpr z = ctx.mkIntConst("z");
        @SuppressWarnings("unused")
        IntExpr u = ctx.mkIntConst("u");

        Expr t1 = ctx.mkAdd(x, ctx.mkSub(y, ctx.mkAdd(x, z)));
        Expr t2 = t1.simplify();
        System.out.println((t1) + " -> " + (t2));
        
        {
        	System.out.println("FindModelExample1");

            BoolExpr x1 = ctx.mkBoolConst("x");
            BoolExpr y1 = ctx.mkBoolConst("y");
            BoolExpr x_xor_y = ctx.mkXor(x1, y1);

            Model model = null;
            Solver s = ctx.mkSolver();
            s.add(x_xor_y);
            if (s.check() !=  Status.SATISFIABLE)
            	assert false; 
           model = s.getModel();
            System.out.println("x = " + model.evaluate(x1, false) + ", y = "
                    + model.evaluate(y1, false));
        }
	}
	
	public static void main(String []args) throws EPMCException {
		System.loadLibrary("z3java");
//		System.loadLibrary("z3java");
		System.out.println("Hello");
		ConstraintSolverSMT.test();
	}

}
