package epmc.rddl.expression;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAnd;
import epmc.value.OperatorOr;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;

public final class EvaluatorRDDL {
    private final ContextValue contextValue;
    private final TObjectIntMap<Expression> exprNumbers;
    private final BitSet constantValues;
    private Value[] stack = new Value[1];
    private Value[] evalArr = new Value[1024];
    private Expression[] variables;
    private final BitSet variableIsParameter;
    private final Set<Expression> variableSet;
    private final TObjectIntMap<Expression> varToNumber;
    private int[] variableToExprNumber;
    private final ArrayList<Expression> queryExprs = new ArrayList<>();
    private Operator[] operators;
    private int[] identifierIndices;
    private int[] quantifierIndices;
    private int[] parameterIndices;
    private int[] childrenNr = new int[1];
    private int[] childrenFromTo = new int[1];
    private BitSet[] dependent;
    private int[] allExprNumbers;
	private Expression[] expressions;
	private OffsetComputer offsetComputer;
	private final Operator and;
	private final Operator or;

    public EvaluatorRDDL(ContextValue context, Expression[] variables,
    		OffsetComputer offsetComputer)
    		throws EPMCException {
        assert context != null;
        assert assertVariables(context, variables);
        assert offsetComputer != null;
        constantValues = UtilBitSet.newBitSetUnbounded();
        variableIsParameter = UtilBitSet.newBitSetUnbounded();
        this.exprNumbers = new TObjectIntHashMap<>();
        this.varToNumber = new TObjectIntHashMap<>();
        this.contextValue = context;
        this.variables = variables.clone();
        this.variableSet = new HashSet<>();
        this.offsetComputer = offsetComputer;
        this.or = contextValue.getOperator(OperatorOr.IDENTIFIER);
        this.and = contextValue.getOperator(OperatorAnd.IDENTIFIER);
        for (Expression variable : variables) {
            this.variableSet.add(variable);
        }
        for (int varNr = 0; varNr < variables.length; varNr++) {
        	this.varToNumber.put(variables[varNr], varNr);
        }
        for (Expression variable : variables) {
            if (!this.exprNumbers.containsKey(variable)) {
                this.exprNumbers.put(variable, exprNumbers.size());
            }
        }
        for (int varNr = 0; varNr < variables.length; varNr++) {
        	ExpressionIdentifierStandard variable = (ExpressionIdentifierStandard) variables[varNr];
        	this.variableIsParameter.set(varNr, variable.getName().startsWith("?"));
        }
        this.variableToExprNumber = new int[variables.length];
        for (int variableNr = 0; variableNr < variables.length; variableNr++) {
            int index = exprNumbers.get(variables[variableNr]);
            this.variableToExprNumber[variableNr] = index;
        }
        for (Expression variable : variables) {
            prepare(variable);
        }
    }
    
    private static boolean assertVariables(ContextValue context,
            Expression[] variables) {
        assert variables != null;
        Set<Expression> testSet = new HashSet<>();
        for (Expression variable : variables) {
            assert variable != null;
            assert !testSet.contains(variable);
            testSet.add(variable);
        }
        return true;
    }

    public int addExpression(Expression expression) throws EPMCException {
        assert assertExpression(expression);
        queryExprs.add(expression);
        int number;
        if (exprNumbers.containsKey(expression)) {
            number = exprNumbers.get(expression);
        } else {
            number = exprNumbers.size();
            exprNumbers.put(expression, number);
        }
    	enumerateExpressions(expression);
        prepare(expression);
        return number;
    }

    private boolean assertExpression(Expression expression) {
        assert expression != null;
        assert assertExpressionRec(expression);
        return true;
    }

    private boolean assertExpressionRec(Expression expression) {
        if (variableSet.contains(expression)) {
            return true;
        }
        if (expression instanceof ExpressionOperator) {
        	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            for (Expression operand : expressionOperator.getOperands()) {
                assertExpressionRec(operand);
            }
        } else if (expression instanceof ExpressionLiteral) {
        } else if (expression instanceof ExpressionRDDLQuantifier) {
        	ExpressionRDDLQuantifier quant = (ExpressionRDDLQuantifier) expression;
        	assertExpressionRec(quant.getOver());
        } else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
        } else if (expression instanceof ExpressionIdentifierStandard
        		&& ((ExpressionIdentifierStandard) expression).getName().startsWith("?")) {
        } else {
            assert false : UtilExpression.toString(expression) + " " + expression.getClass() + " " + variableSet;
        }
        return true;
    }

    public void prepare() throws EPMCException {
        this.childrenFromTo = new int[exprNumbers.size() + 1];
        this.expressions = new Expression[exprNumbers.size()];
        TObjectIntIterator<Expression> it = exprNumbers.iterator();
        int numChildren = 0;
        this.operators = new Operator[exprNumbers.size()];
        this.identifierIndices = new int[exprNumbers.size()];
        this.quantifierIndices = new int[exprNumbers.size()];
        this.parameterIndices = new int[exprNumbers.size()];
        Arrays.fill(this.identifierIndices, -1);
        Arrays.fill(this.quantifierIndices, -1);
        Arrays.fill(this.parameterIndices, -1);
        while (it.hasNext()) {
            it.advance();
            Expression expression = it.key();
            expressions[it.value()] = expression;
            if (expression instanceof ExpressionOperator) {
            	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
                this.operators[it.value()] = expressionOperator.getOperator();
                numChildren += expression.getChildren().size();
            } else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
            	this.identifierIndices[it.value()] = this.offsetComputer.registerIdentifier(expression);
            } else if (expression instanceof ExpressionRDDLQuantifier) {
            	ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
            	this.quantifierIndices[it.value()] = this.offsetComputer.registerQuantifier(quantifier);
            	numChildren += 1;
            } else if (expression instanceof ExpressionIdentifierStandard
            		&& ((ExpressionIdentifierStandard) expression).getName().startsWith("?")) {
            	this.parameterIndices[it.value()] = this.offsetComputer.getParameterIndex(((ExpressionIdentifierStandard) expression).getName());
            }
        }
        childrenNr = new int[numChildren];
        int nextChild = 0;
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            Expression expression = expressions[exprNr];
            if (expression instanceof ExpressionOperator) {
            	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
                for (Expression child : expressionOperator.getOperands()) {
                    childrenNr[nextChild] = exprNumbers.get(child);
                    nextChild++;
                }
            } else if (expression instanceof ExpressionRDDLQuantifier) {
            	ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
                childrenNr[nextChild] = exprNumbers.get(quantifier.getOver());
                nextChild++;
            }
            this.childrenFromTo[exprNr + 1] = nextChild;
        }
        computeDependent();
        allExprNumbers = new int[queryExprs.size()];
        for (int variableNr = 0; variableNr < queryExprs.size(); variableNr++) {
            allExprNumbers[variableNr] = exprNumbers.get(queryExprs.get(variableNr));
        }
        this.offsetComputer.buildExpressions();
        this.offsetComputer.buildQuantifier();
    }
    
    private void computeDependent() {
        dependent = new BitSet[variables.length];
        for (int entryNr = 0; entryNr < dependent.length; entryNr++) {
            dependent[entryNr] = UtilBitSet.newBitSetUnbounded();
        }
        for (Expression expression : queryExprs) {
            computeDependent(expression);
        }
        
        for (Expression expression : variables) {
            assert exprNumbers.containsKey(expression);
            int varExprNr = exprNumbers.get(expression);
            for (int varNr = 0; varNr < variables.length; varNr++) {
                dependent[varNr].set(varExprNr, false);
            }
        }
    }

    private void computeDependent(Expression expression) {
        int exprNr = exprNumbers.get(expression);
        for (Expression child : expression.getChildren()) {
            computeDependent(child);
            assert exprNumbers.containsKey(child);
            int childNr = exprNumbers.get(child);
            for (int varNr = 0; varNr < variables.length; varNr++) {
                if (dependent[varNr].get(childNr)) {
                    dependent[varNr].set(exprNr);
                }
            }
        }
        if (varToNumber.containsKey(expression)) {
            int varNr = varToNumber.get(expression);
            dependent[varNr].set(exprNr);
        }
    }

    private void enumerateExpressions(Expression expression) {
        for (Expression child : expression.getChildren()) {
            enumerateExpressions(child);
        }
        if (!exprNumbers.containsKey(expression)) {
            exprNumbers.put(expression, exprNumbers.size());
        }
    }

    private void prepare(Expression expression) throws EPMCException {
        Type type;
        assert expression != null;
        assert exprNumbers.containsKey(expression);
        int exprNumber = exprNumbers.get(expression);
        // TODO
        type = expression.getType(null);
        assert type != null : expression + "    " + expression.getClass();
        if (variableSet.contains(expression)) {
        	constantValues.set(exprNumber);
        } else if (expression instanceof ExpressionLiteral) {
        	constantValues.set(exprNumber);
        } else if (expression instanceof ExpressionOperator) {
        	for (Expression child : expression.getChildren()) {
        		prepare(child);
        	}
        } else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
        	//ExpressionRDDLQuantifiedIdentifier quantified = (ExpressionRDDLQuantifiedIdentifier) expression;
        } else if (expression instanceof ExpressionRDDLQuantifier) {
        	ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
        	prepare(quantifier.getOver());
        } else if (expression instanceof ExpressionIdentifierStandard
        		&& ((ExpressionIdentifierStandard) expression).getName().startsWith("?")) {
        } else {
        	assert false : "cannot handle expression " + expression
        	+ " of " + expression.getClass();
        type = null;
        }
        if (!TypeUnknown.isUnknown(type)) {
        	ensureStackSize(exprNumber + 1);
        	stack[exprNumber] = type.newValue();
        }
        if (expression instanceof ExpressionLiteral) {
        	ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        	stack[exprNumber].set(expressionLiteral.getValue());
        }
        assert stack != null;
    }

    public void setVariableValues(Value[] values) throws EPMCException {
        assert assertVariableValues(values);
        for (int nr = 0; nr < variables.length; nr++) {
            int variableNr = variableToExprNumber[nr];
            Value there = stack[variableNr];
            assert there != null : variableNr + " " + variableToExprNumber[nr];
            if (!there.isEq(values[nr])) {
                there.set(values[nr]);
            }
        }
    }

    private boolean assertVariableValues(Value[] values) {
        assert values != null;
        assert values.length == variables.length : values.length + " " + variables.length;
        for (int index = 0; index < variables.length; index++) {
            assert values[index] != null : index;
            assert values[index].getType().getContext() == contextValue;
            try {
            	// TODO
                assert variables[index].getType(null).canImport(values[index].getType())
                // TODO
                : variables[index].getType(null) + " " + values[index] + " " + values[index] + " " + index;
            } catch (EPMCException e) {
                e.printStackTrace();
                assert false;
            }
        }
        return true;
    }

    public void evaluate(int expression) throws EPMCException {
        assert expression >= 0;
        assert expression < exprNumbers.size();
        evaluateInternal(expression);
    }
    
    public void evaluate(Expression expression) throws EPMCException {
        assert expression != null;
        /*
        if (!exprNumbers.containsKey(expression)) {
            addExpression(expression);
            prepare();
        } 
        */       
        assert exprNumbers.containsKey(expression) : expression;
        int exprNr = exprNumbers.get(expression);
        evaluateInternal(exprNr);
    }
    
    private void evaluateInternal(int exprNr) throws EPMCException {
        if (constantValues.get(exprNr)) {
        	return;
        }
        Value value = stack[exprNr];
        int exprBegin = this.childrenFromTo[exprNr];
        Operator operator = this.operators[exprNr];
        int identifierIndex = this.identifierIndices[exprNr];
        int quantifierIndex = this.quantifierIndices[exprNr];
        int parameterIndex = this.parameterIndices[exprNr];
        if (operator != null) {
            int exprEnd = childrenFromTo[exprNr + 1];
            for (int nr = exprBegin; nr < exprEnd; nr++) {
                int childNr = childrenNr[nr];
                evaluateInternal(childNr);
            }
            for (int nr = exprBegin; nr < exprEnd; nr++) {
                int childNr = childrenNr[nr];
                evalArr[nr - exprBegin] = stack[childNr];
            }
            operator.apply(value, evalArr);
        } else if (identifierIndex >= 0) {
        	int variable = this.offsetComputer.computeIdentifierIndex(identifierIndex);
        	ExpressionIdentifierStandard variableId = (ExpressionIdentifierStandard) this.variables[variable];
        	ExpressionIdentifierStandard expressionId = (ExpressionIdentifierStandard) expressions[exprNr];
        	assert variableId.getName().startsWith(expressionId.getName())
        	: variableId.getName() + " " + expressionId.getName()
        	+ " " + variable + " " + identifierIndex;
        	int actual = variableToExprNumber[variable];
        	value.set(stack[actual]);
        } else if (quantifierIndex >= 0) {
        	value.set(this.offsetComputer.getQuantifierInit(quantifierIndex));
        	int numValues = this.offsetComputer.getQuantifierNumAssignments(quantifierIndex);
    		operator = this.offsetComputer.getQuantifierOperator(quantifierIndex);
    		int innerExpression = childrenNr[exprBegin + 0];
        	for (int assignment = 0; assignment < numValues; assignment++) {
        		this.offsetComputer.setQuantifierAssignment(quantifierIndex, assignment);
        		evaluateInternal(innerExpression);
        		operator.apply(value, value, stack[innerExpression]);
        		if (operator == this.or && ValueBoolean.isTrue(value)) {
        			break;
        		}
        		if (operator == this.and && ValueBoolean.isFalse(value)) {
        			break;
        		}
        	}
        } else if (parameterIndex >= 0) {
        	ValueAlgebra.asAlgebra(value).set(this.offsetComputer.getParameterValue(parameterIndex));
        } else {
        	assert false;
        }
    }

    public Value getResultValue(int expression) {
        assert expression >= 0;
        assert expression < exprNumbers.size();
        return stack[expression];
    }
    
    public boolean getResultBoolean(int expression) {
        assert expression >= 0;
        assert expression < exprNumbers.size();
        assert ValueBoolean.isBoolean(stack[expression]);
        return ValueBoolean.asBoolean(stack[expression]).getBoolean();
    }

    public boolean evaluateBoolean(int expression) throws EPMCException {
        assert expression >= 0;
        assert expression < exprNumbers.size();
        assert ValueBoolean.isBoolean(stack[expression]);
        evaluate(expression);
        return getResultBoolean(expression);
    }

    public boolean evaluateBoolean(Expression expression)
            throws EPMCException {
        assert expression != null;
        assert exprNumbers.containsKey(expression);
        assert ValueBoolean.isBoolean(stack[exprNumbers.get(expression)]);
        int exprNr = exprNumbers.get(expression);
        return evaluateBoolean(exprNr);
    }

    public boolean getResultBoolean(Expression expression) {
        assert expression != null;
        assert exprNumbers.containsKey(expression);
        int exprNr = exprNumbers.get(expression);
        return ValueBoolean.asBoolean(stack[exprNr]).getBoolean();
    }

    public Value getResultValue(Expression expression) {
        assert expression != null;
        assert exprNumbers.containsKey(expression);
        int exprNr = exprNumbers.get(expression);
        return stack[exprNr];
    }

    public int getExpressionNumber(Expression expression) {
        assert expression != null;
        assert exprNumbers.containsKey(expression);
        return exprNumbers.get(expression);
    }

    public void evaluateAll() throws EPMCException {
        for (int exprNr : allExprNumbers) {
            evaluate(exprNr);
        }
    }
    
    public Expression[] getVariables() {
        return variables.clone();
    }
    
    private void ensureStackSize(int size) {
        if (size < stack.length) {
            return;
        }
        int roundedSize = 1;
        while (roundedSize < size) {
            roundedSize *= 2;
        }
        stack = Arrays.copyOf(stack, size);
    }
    
}
