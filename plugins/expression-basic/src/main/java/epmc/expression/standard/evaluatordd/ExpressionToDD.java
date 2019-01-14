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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.value.ValueEnum;
import epmc.value.ValueInteger;
import epmc.value.ValueNumBitsKnown;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorSubtract;
import epmc.options.Options;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;

/**
 * Translates expressions to DD-based symbolic representations.
 * 
 * @author Ernst Moritz Hahn
 */

// TODO documentation
// TODO functionality to support range checks
// TODO still needed?
// TODO BDD vector functionality should be used to DD nodes, such that
// speedup would appear for all cases where applicable, not only
// during expression translation
public final class ExpressionToDD implements Closeable {
    private final class Translated implements Closeable {
        private DD singleDD = null;
        private final List<DD> vector;
        private final List<DD> vectorExternal;
        private final Type type;
        private boolean closed;

        Translated(DD singleDD, List<DD> vector, Type type) {
            assert singleDD != null || vector != null;
            if (vector != null) {
                for (DD dd : vector) {
                    assert dd != null;
                }
            }
            if (vector != null) {
                this.vector = new ArrayList<>(ContextDD.get().clone(vector));
                vectorExternal = Collections.unmodifiableList(this.vector);
            } else {
                this.vector = null;
                vectorExternal = null;
            }
            this.type = type;
            if (singleDD != null) {
                this.singleDD = singleDD.clone();
            }
        }

        Translated(VariableDD variableDD, int copy)
        {
            this(variableDDToSingleDD(variableDD, copy),
                    variableDDToVector(variableDD, copy),
                    variableDDToType(variableDD));
        }

        Translated(DD singleDD) {
            this(singleDD, null, singleDD.getType());
        }

        Translated(Value value) {
            this(valueToSingleDD(value), valueToVector(value), value.getType());
        }

        DD getSingleDD() {
            assert !closed && alive();
            assert alive();
            if (singleDD != null) {
                assert singleDD.alive();
                return singleDD;
            }

            if (type == null || TypeInteger.is(type)) {
                int digVal = 1;
                ValueInteger value = TypeInteger.get()
                        .newValue();
                singleDD = ContextDD.get().newConstant(0);
                for (int pos = 0; pos < vector.size() - 1; pos++) {
                    value.set(digVal);
                    DD valueDD = ContextDD.get().newConstant(value);
                    DD posDD = vector.get(pos).toMT().multiplyWith(valueDD);
                    singleDD = singleDD.addWith(posDD);
                    digVal *= 2;
                }
                DD signConstDD = vector.get(vector.size() - 1).toMT().
                        multiplyWith(ContextDD.get().newConstant(-digVal));
                singleDD = singleDD.addWith(signConstDD);
            } else if (TypeEnum.is(type)) {
                TypeEnum typeEnum = TypeEnum.as(type);
                Enum<?>[] consts = typeEnum.getEnumClass().getEnumConstants();
                int numBits = typeEnum.getNumBits() + 1;
                singleDD = ContextDD.get().newConstant(typeEnum.newValue(consts[0]));
                for (int constNr = 0; constNr < consts.length; constNr++) {
                    List<DD> constNrDD = ContextDD.get().twoCplFromInt(constNr, numBits);
                    DD eq = ContextDD.get().eq(constNrDD, vector);
                    ContextDD.get().dispose(constNrDD);
                    Value value = typeEnum.newValue(consts[constNr]);
                    DD constValDD = ContextDD.get().newConstant(value);
                    singleDD = eq.iteWith(constValDD, singleDD);
                }
                //            } else if (type.isUserEnum()) {
                //              assert false;
                /*
                Type typeEnum = type;
                List<Object> consts = typeEnum.getValues();
                int numBits = typeEnum.getNumBits() + 1;
                singleDD = contextDD.newConstant(typeEnum.newValue(consts.get(0)));
                for (int constNr = 0; constNr < consts.size(); constNr++) {
                    List<DD> constNrDD = contextDD.twoCplFromInt(constNr, numBits);
                    DD eq = contextDD.eq(constNrDD, vector);
                    contextDD.dispose(constNrDD);
                    Value value = typeEnum.newValue(consts.get(constNr));
                    DD constValDD = contextDD.newConstant(value);
                    singleDD = eq.iteWith(constValDD, singleDD);
                }
                 */
            } else {
                assert false;
            }
            assert singleDD.alive();
            return singleDD;
        }

        boolean hasVector() {
            assert !closed && alive();
            return vector != null;
        }

        List<DD> getVector() {
            assert !closed && alive();
            return vectorExternal;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (singleDD != null) {
                singleDD.dispose();
            }
            if (vector != null) {
                ContextDD.get().dispose(vector);
            }
        }

    }

    private final static class AssignEntry {
        private final VariableDD variable;
        private final int copy;
        private final Expression value;

        AssignEntry(VariableDD variable, int copy, Expression value) {
            assert variable != null;
            assert copy >= 0;
            assert copy < variable.getNumCopies();
            assert value != null;
            this.variable = variable;
            this.copy = copy;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof AssignEntry)) {
                return false;
            }
            AssignEntry other = (AssignEntry) obj;
            if (this.variable != other.variable) {
                return false;
            }
            if (this.copy != other.copy) {
                return false;
            }
            if (!this.value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = variable.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = copy + (hash << 6) + (hash << 16) - hash;
            hash = value.hashCode()
                    + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
    }

    private final Map<Expression,DD> constants;
    private final Map<Expression,VariableDD> variables;
    private final Map<Expression,Translated> translationCache;
    private final Map<AssignEntry,DD> assignCache;
    private final boolean useVector;
    private boolean closed;

    private List<DD> valueToVector(Value value) {
        if (!useVector) {
            return null;
        } else if (ValueEnum.is(value)) {
            int numBits = ValueNumBitsKnown.getNumBits(value);
            int number = ValueEnum.as(value).getEnum().ordinal();
            return ContextDD.get().twoCplFromInt(number, numBits);
        } else if (ValueInteger.is(value)) {
            return ContextDD.get().twoCplFromInt(ValueInteger.as(value).getInt());
        } else {
            return null;
        }
    }

    private DD valueToSingleDD(Value value) {
        if (useVector && (ValueInteger.is(value) || ValueEnum.is(value))) {
            return null;
        } else {
            return ContextDD.get().newConstant(value);
        }
    }

    private Type variableDDToType(VariableDD variableDD) {
        Type type;
        if (variableDD.isInteger()) {
            int lower = variableDD.getLower();
            int upper = variableDD.getUpper();
            type = TypeInteger.get(lower, upper);
        } else {
            type = TypeBoolean.get();
        }
        return type;
    }

    private List<DD> variableDDToVector(VariableDD variableDD, int copy)
    {
        assert variableDD != null;
        assert copy >= 0;
        assert copy < variableDD.getNumCopies();
        if (!useVector || !variableDD.isInteger()) {
            return null;
        }
        List<DD> origVec = new ArrayList<>(ContextDD.get().clone(variableDD.getDDVariables(copy)));
        origVec.add(ContextDD.get().newConstant(false));
        List<DD> add = ContextDD.get().twoCplFromInt(variableDD.getLower());

        List<DD> result = ContextDD.get().twoCplAdd(origVec, add);
        ContextDD.get().dispose(add);
        ContextDD.get().dispose(origVec);

        return result;
    }

    private DD variableDDToSingleDD(VariableDD variableDD, int copy)
    {
        assert variableDD != null;
        if (useVector && variableDD.isInteger()) {
            return null;
        }
        return variableDD.getValueEncoding(copy);
    }

    public ExpressionToDD(Map<Expression,VariableDD> variables,
            Map<Expression,DD> constants) {
        assert assertConstructorArgs(variables,
                constants);
        Options options = Options.get();
        this.variables = new HashMap<>();
        this.variables.putAll(variables);
        this.constants = new HashMap<>();
        this.constants.putAll(constants);
        if (options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_CACHE)) {
            this.translationCache = new HashMap<>();
            this.assignCache = new HashMap<>();
        } else {
            this.translationCache = null;
            this.assignCache = null;
        }
        this.useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
    }

    private static boolean assertConstructorArgs(Map<Expression,VariableDD>
    variables, Map<Expression,DD> constants) {
        ContextDD contextDD = ContextDD.get();
        assert variables != null;
        for (Entry<Expression, VariableDD> entry : variables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
            //            assert entry.getKey().isIdentifier() : entry.getKey();
            assert entry.getValue().getContext() == contextDD;
        }
        assert constants != null;
        for (Entry<Expression, DD> entry : constants.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
            assert entry.getValue().getContext() == contextDD;
        }
        return true;
    }

    public ExpressionToDD(Map<Expression,VariableDD> variables) {
        this(variables, Collections.<Expression,DD>emptyMap());
    }

    public void putConstantWith(Expression expression, DD dd) {
        constants.put(expression, dd);
    }

    public void putConstant(Expression expression, DD dd) {
        putConstantWith(expression, dd.clone());
    }

    public DD translate(Expression expression) {
        assert alive();
        assert expression != null;
        Translated trans = transRec(expression);
        DD result = trans.getSingleDD().clone();
        if (translationCache == null) {
            trans.close();
        }
        return result;
    }

    public DD assign(VariableDD variable, int copy, Expression value)
    {
        assert alive();
        assert variable != null;
        assert copy >= 0;
        assert copy < variable.getNumCopies();
        assert value != null;
        if (assignCache != null) {
            AssignEntry entry = new AssignEntry(variable, copy, value);
            if (assignCache.containsKey(entry)) {
                return assignCache.get(entry).clone();
            }
        }
        Translated trans = transRec(value);
        Translated varTrans = new Translated(variable, copy);
        Translated resultTrans = opEq(varTrans, trans);
        DD result = resultTrans.getSingleDD().clone();
        if (translationCache == null) {
            trans.close();
            resultTrans.close();
        }
        if (assignCache != null) {
            AssignEntry entry = new AssignEntry(variable, copy, value);
            assignCache.put(entry, result.clone());
        }
        return result;
    }

    private Translated transRec(Expression expression) {
        assert expression != null;
        if (translationCache != null && translationCache.containsKey(expression)) {
            return translationCache.get(expression);
        }

        Translated result = null;
        if (expression instanceof ExpressionIdentifier) {
            assert variables.containsKey(expression)
            || constants.containsKey(expression) : expression;
            if (variables.containsKey(expression)) {
                result = new Translated(variables.get(expression), 0);
            } else if (constants.containsKey(expression)) {
                return new Translated(constants.get(expression));
            } else {
                assert false : expression;
            }
        } else if (expression instanceof ExpressionLiteral) {
            Value value = UtilEvaluatorExplicit.evaluate(expression);
            result = new Translated(value);
        } else if (expression instanceof ExpressionOperator) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Operator operator = expressionOperator.getOperator();
            List<Translated> inner = new ArrayList<>();

            for (Expression op : expressionOperator.getOperands()) {
                inner.add(transRec(op));
            }
            if (operator.equals(OperatorAdd.ADD)) {
                result = opAdd(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorAddInverse.ADD_INVERSE)) {
                result = opAddInverse(inner.get(0));
            } else if (operator.equals(OperatorEq.EQ)) {
                result = opEq(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorGe.GE)) {
                result = opGe(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorGt.GT)) {
                result = opGt(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorIte.ITE)) {
                result = opIte(inner.get(0), inner.get(1), inner.get(2));
            } else if (operator.equals(OperatorLe.LE)) {
                result = opLe(inner.get(0), inner.get(1));
            }  else if (operator.equals(OperatorLt.LT)) {
                result = opLt(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorMax.MAX)) {
                result = opMax(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorMin.MIN)) {
                result = opMin(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorNe.NE)) {
                result = opNe(inner.get(0), inner.get(1));
            } else if (operator.equals(OperatorSubtract.SUBTRACT)) {
                result = opSubtract(inner.get(0), inner.get(1));
            } else {
                result = generalApply(operator, inner);
            }
            if (translationCache == null) {
                for (Translated op : inner) {
                    op.close();
                }
            }
        } else {
            assert false;
            result = null;
        }
        assert result != null;
        if (translationCache != null) {
            translationCache.put(expression, result);
        }
        return result;
    }

    private Translated opEq(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD eq = ContextDD.get().twoCplEq(op1.getVector(), op2.getVector());
            result = new Translated(eq, null, null);
            eq.dispose();
        } else {
            result = generalApply(OperatorEq.EQ, op1, op2);
        }

        return result;
    }

    private Translated opNe(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD ne = ContextDD.get().twoCplNe(op1.getVector(), op2.getVector());
            result = new Translated(ne, null, null);
            ne.dispose();
        } else {
            result = generalApply(OperatorNe.NE, op1, op2);
        }

        return result;
    }

    private Translated opGe(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD ge = ContextDD.get().twoCplGe(op1.getVector(), op2.getVector());
            result = new Translated(ge, null, null);
            ge.dispose();
        } else {
            result = generalApply(OperatorGe.GE, op1, op2);
        }

        return result;
    }

    private Translated opLe(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD le = ContextDD.get().twoCplLe(op1.getVector(), op2.getVector());
            result = new Translated(le, null, null);
            le.dispose();
        } else {
            result = generalApply(OperatorLe.LE, op1, op2);
        }

        return result;
    }

    private Translated opLt(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD lt = ContextDD.get().twoCplLt(op1.getVector(), op2.getVector());
            result = new Translated(lt, null, null);
            lt.dispose();
        } else {
            result = generalApply(OperatorLt.LT, op1, op2);
        }

        return result;
    }

    private Translated opGt(Translated op1, Translated op2) {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD gt = ContextDD.get().twoCplGt(op1.getVector(), op2.getVector());
            result = new Translated(gt, null, null);
            gt.dispose();
        } else {
            result = generalApply(OperatorGt.GT, op1, op2);
        }

        return result;
    }


    private Translated opAddInverse(Translated op) {
        Translated result;
        if (op.hasVector()) {
            List<DD> list = ContextDD.get().twoCplAddInverse(op.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorAddInverse.ADD_INVERSE, op);
        }
        return result;
    }

    private Translated opAdd(Translated op1, Translated op2)
    {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = ContextDD.get().twoCplAdd(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorAdd.ADD, op1, op2);
        }

        return result;
    }

    /*
    private Translated opMultiply(Translated op1, Translated op2)
            {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = contextDD.twoCplMultiply(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(contextValue.getOperator(MULTIPLY), op1, op2);
        }

        return result;
    }
     */

    private Translated opMax(Translated op1, Translated op2)
    {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = ContextDD.get().twoCplMax(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorMax.MAX, op1, op2);
        }

        return result;
    }

    private Translated opMin(Translated op1, Translated op2)
    {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = ContextDD.get().twoCplMin(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorMin.MIN, op1, op2);
        }

        return result;
    }

    private Translated opSubtract(Translated op1, Translated op2)
    {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = ContextDD.get().twoCplSubtract(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorSubtract.SUBTRACT, op1, op2);
        }

        return result;
    }

    private Translated opIte(Translated ifT, Translated thenT, Translated elseT)
    {
        Translated result;
        if (thenT.hasVector() && elseT.hasVector()) {
            List<DD> list = ContextDD.get().twoCplIte(ifT.getSingleDD(), thenT.getVector(), elseT.getVector());
            result = new Translated(null, list, null);
            ContextDD.get().dispose(list);
        } else {
            result = generalApply(OperatorIte.ITE, ifT, thenT, elseT);
        }

        return result;
    }

    private Translated generalApply(Operator operator, List<Translated> operands)
    {
        assert operator != null;
        assert operands != null;
        for (Translated trans : operands) {
            assert trans != null;
        }
        Translated[] array = operands.toArray(new Translated[0]);
        return generalApply(operator, array);
    }

    private Translated generalApply(Operator operator, Translated... operands)
    {
        assert operator != null;
        assert operands != null;
        for (Translated trans : operands) {
            assert trans != null;
        }
        DD[] innerDD = new DD[operands.length];
        for (int index = 0; index < operands.length; index++) {
            innerDD[index] = operands[index].getSingleDD();
        }
        DD resultDD = ContextDD.get().apply(operator, innerDD);
        Translated result = new Translated(resultDD, null, null);
        resultDD.dispose();

        return result;
    }

    @Override
    public void close() {
        if (!alive()) {
            return;
        }
        closed = true;
        if (translationCache != null) {
            for (Translated trans : translationCache.values()) {
                trans.close();
            }
        }
        if (assignCache != null) {
            for (DD dd : assignCache.values()) {
                dd.dispose();
            }
        }
    }

    private boolean alive() {
        return !closed && ContextDD.get().alive();
    }
}
