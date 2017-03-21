/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.expression.standard.evaluatordd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.ValueInteger;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class UtilEvaluatorDD {
    private final static String SPACE = " ";
    
    private final static class ExpressionToTypeDD implements ExpressionToType {
    	private final ContextValue context;
		private final Map<Expression, VariableDD> variables;

		private ExpressionToTypeDD(ContextValue context, Map<Expression, VariableDD> variables) {
    		assert context != null;
    		this.context = context;
    		this.variables = variables;
    	}

		@Override
		public ContextValue getContextValue() {
			return context;
		}

		@Override
		public Type getType(Expression expression) throws EPMCException {
			assert expression != null;
			if (variables == null) {
				return null;
			}
			VariableDD variable = variables.get(expression);
			if (variable == null) {
				return null;
			}
			return variable.getType();
		}
    }
    
    public static EvaluatorDD newEvaluator(ContextValue context, Expression expression, Map<Expression,VariableDD> variables) throws EPMCException {
    	assert context != null;
        assert expression != null;
        assert variables != null;
        for (Entry<Expression, VariableDD> entry : variables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        Options options = context.getOptions();
        Map<String,Class<? extends EvaluatorDD>> evaluators = options.get(OptionsExpressionBasic.EXPRESSION_EVALUTOR_DD_CLASS);
        for (Class<? extends EvaluatorDD> clazz : evaluators.values()) {
            EvaluatorDD evaluator = Util.getInstance(clazz);
            evaluator.setExpression(expression);
            evaluator.setVariables(variables);
            evaluator.setContextValue(context);
            if (evaluator.canHandle()) {
                evaluator.build();
                return evaluator;
            }
        }
        assert false : expression + SPACE + expression.getClass();
        return null;
    }
    
    public static DD translate(ContextValue context, Expression expression, Map<Expression,VariableDD> variables) throws EPMCException {
        EvaluatorDD evaluator = newEvaluator(context, expression, variables);
        DD result = evaluator.getDD();
        evaluator.close();
        return result;
    }
    
    public static DD assign(VariableDD variable, int copy, Expression value, Map<Expression,VariableDD> variables)
            throws EPMCException {
        assert variable != null;
        assert copy >= 0;
        assert copy < variable.getNumCopies();
        assert value != null;
        EvaluatorDD evaluator = newEvaluator(variable.getContext().getContextValue(), value, variables);
        ContextDD contextDD = variable.getContext();
        DD result;
        if (evaluator.getVector() != null) {
            List<DD> varVec = variable.getDDVariables(copy);
            result = contextDD.twoCplEq(varVec, evaluator.getVector());
        } else {
            result = variable.getValueEncoding(copy).eq(evaluator.getDD());
        }
        evaluator.close();
        return result;
    }

    
    public static DD getDD(DD singleDD, List<DD> vector, Expression expression) throws EPMCException {
        if (singleDD != null) {
            assert singleDD.alive();
            return singleDD;
        }
        assert vector != null;
        ContextDD contextDD = vector.get(0).getContext();
        Type type = TypeInteger.get(contextDD.getContextValue());
//        Type type = expression.getType(new ExpressionToTypeDD(contextDD.getContextValue(), null));
        if (type == null || TypeInteger.isInteger(type)) {
            int digVal = 1;
            ValueInteger value = TypeInteger.get(contextDD.getContextValue())
                    .newValue();
            singleDD = contextDD.newConstant(0);
            for (int pos = 0; pos < vector.size() - 1; pos++) {
                value.set(digVal);
                DD valueDD = contextDD.newConstant(value);
                DD posDD = vector.get(pos).toMT().multiplyWith(valueDD);
                singleDD = singleDD.addWith(posDD);
                digVal *= 2;
            }
            DD signConstDD = vector.get(vector.size() - 1).toMT().
                    multiplyWith(contextDD.newConstant(-digVal));
            singleDD = singleDD.addWith(signConstDD);
        } else if (TypeEnum.isEnum(type)) {
            TypeEnum typeEnum = TypeEnum.asEnum(type);
            Enum<?>[] consts = typeEnum.getEnumClass().getEnumConstants();
            int numBits = typeEnum.getNumBits() + 1;
            singleDD = contextDD.newConstant(typeEnum.newValue(consts[0]));
            for (int constNr = 0; constNr < consts.length; constNr++) {
                List<DD> constNrDD = contextDD.twoCplFromInt(constNr, numBits);
                DD eq = contextDD.eq(constNrDD, vector);
                contextDD.dispose(constNrDD);
                Value value = typeEnum.newValue(consts[constNr]);
                DD constValDD = contextDD.newConstant(value);
                singleDD = eq.iteWith(constValDD, singleDD);
            }
        } else {
            assert false;
        }
        assert singleDD.alive();
        return singleDD;
    }
    
    public static boolean canVectorOperator(ContextValue context, Expression expression, String operator, Map<Expression, VariableDD> variables) throws EPMCException {
        assert expression != null;
        assert operator != null;
        assert variables != null;
        if (variables.containsKey(expression)) {
            return false;
        }
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (!expressionOperator.getOperator().getIdentifier().equals(operator)) {
            return false;
        }
        Options options = context.getOptions();
        if (!options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR)) {
            return false;
        }
        return true;
    }

    public static boolean canIntegerVectorOperator(ContextValue context, Expression expression, String operator, Map<Expression, VariableDD> variables) throws EPMCException {
        assert expression != null;
        assert operator != null;
        assert variables != null;
        if (!canVectorOperator(context, expression, operator, variables)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        for (Expression operand : expressionOperator.getOperands()) {
            if (!TypeInteger.isInteger(operand.getType(new ExpressionToTypeDD(context, variables)))) {
                return false;
            }
        }
        return true;
    }

    public static List<DD> applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperatorOneArg operator) throws EPMCException {
        return applyVector(context, expression, variables, (@SuppressWarnings("unchecked") List<DD>... operands) -> operator.apply(operands[0]));
    }
    
    public static List<DD> applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperatorTwoArgs operator) throws EPMCException {
        return applyVector(context, expression, variables, (@SuppressWarnings("unchecked") List<DD>... operands) -> operator.apply(operands[0], operands[1]));
    }
    
    public static List<DD> applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperatorThreeArgs operator) throws EPMCException {
        return applyVector(context, expression, variables, (@SuppressWarnings("unchecked") List<DD>... operands) -> operator.apply(operands[0], operands[1], operands[2]));
    }

    public static List<DD> applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperator operator) throws EPMCException {
        assert expression != null;
        List<EvaluatorDD> inner = new ArrayList<>();
        boolean allHaveVectors = true;
        boolean allInteger = true;
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        for (Expression op : expressionOperator.getOperands()) {
            EvaluatorDD evaluator = newEvaluator(context, op, variables);
            inner.add(evaluator);
            allInteger &= TypeInteger.isInteger(op.getType(new ExpressionToTypeDD(context, variables)));
            allHaveVectors &= evaluator.getVector() != null;
        }
        @SuppressWarnings("unchecked")
        List<DD>[] dds = new List[expressionOperator.getOperands().size()];
        List<DD> result = null;
        for (int i = 0; i < inner.size(); i++) {
            dds[i] = inner.get(i).getVector();
        }
        if (allHaveVectors && allInteger) {
            result = operator.apply(dds);
        }
        for (EvaluatorDD evaluator : inner) {
            evaluator.close();
        }
        return result;
    }

    public static DD applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperatorTwoArgsSingleDDResult operator) throws EPMCException {
        return applyVector(context, expression, variables, (@SuppressWarnings("unchecked") List<DD>... operands) -> operator.apply(operands[0], operands[1]));
    }
    
    public static DD applyVector(ContextValue context, Expression expression, Map<Expression,VariableDD> variables, VectorOperatorSingleDDResult operator) throws EPMCException {
        assert expression != null;
        List<EvaluatorDD> inner = new ArrayList<>();
        boolean allHaveVectors = true;
        boolean allInteger = true;
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        for (Expression op : expressionOperator.getOperands()) {
            EvaluatorDD evaluator = newEvaluator(context, op, variables);
            inner.add(evaluator);
            allInteger &= TypeInteger.isInteger(op.getType(new ExpressionToTypeDD(context, variables)));
            allHaveVectors &= evaluator.getVector() != null;
        }
        @SuppressWarnings("unchecked")
        List<DD>[] dds = new List[expressionOperator.getOperands().size()];
        for (int i = 0; i < inner.size(); i++) {
            dds[i] = inner.get(i).getVector();
        }
        DD result = null;
        if (allHaveVectors && allInteger) {
            result = operator.apply(dds);
        } else {
            EvaluatorDDOperatorGeneral general = new EvaluatorDDOperatorGeneral();
            general.setExpression(expression);
            general.setVariables(variables);
            general.setContextValue(context);
            general.build();
            result = general.getDD().clone();
            general.close();
        }
        for (EvaluatorDD op : inner) {
            op.close();
        }
        return result;
    }

    public static boolean close(boolean closed, DD dd, List<DD> vector) {
        if (closed) {
            return true;
        }
        if (dd != null) {
            dd.dispose();
        }
        if (vector != null) {
            for (DD ddV : vector) {
                ddV.dispose();
            }
        }
        return true;
    }
}
