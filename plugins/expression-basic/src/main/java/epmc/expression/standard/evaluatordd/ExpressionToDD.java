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

import gnu.trove.map.hash.THashMap;

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
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorNe;
import epmc.value.OperatorSubtract;

/**
 * Translates expressions to DD-based symbolic representations.
 * 
 * @author Ernst Moritz Hahn
 */

// TODO documentation
// TODO functionality to support range checks
// TODO still needed?

public final class ExpressionToDD implements Closeable {
    private final class Translated implements Closeable {
        private DD singleDD = null;
        private final List<DD> vector;
        private final List<DD> vectorExternal;
        private final Type type;
        private boolean closed;
        
        Translated(DD singleDD, List<DD> vector, Type type) {
            assert singleDD != null || vector != null;
            assert singleDD == null || singleDD.getContext() == contextDD;
            if (vector != null) {
                for (DD dd : vector) {
                    assert dd != null;
                    assert dd.getContext() == contextDD;
                }
            }
            if (vector != null) {
                this.vector = new ArrayList<>(contextDD.clone(vector));
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
                throws EPMCException {
            this(variableDDToSingleDD(variableDD, copy),
                    variableDDToVector(variableDD, copy),
                    variableDDToType(variableDD));
        }

        Translated(DD singleDD) {
            this(singleDD, null, singleDD.getType());
        }
        
        Translated(Value value) throws EPMCException {
            this(valueToSingleDD(value), valueToVector(value), value.getType());
        }

        DD getSingleDD() throws EPMCException {
            assert !closed && alive();
            assert alive();
            if (singleDD != null) {
                assert singleDD.alive();
                return singleDD;
            }
            
            if (type == null || TypeInteger.isInteger(type)) {
                int digVal = 1;
                ValueInteger value = TypeInteger.get()
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
                contextDD.dispose(vector);
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
    
    private final ContextValue contextValue;
    private final ContextDD contextDD;
    private final Map<Expression,DD> constants;
    private final Map<Expression,VariableDD> variables;
    private final Map<Expression,Translated> translationCache;
    private final Map<AssignEntry,DD> assignCache;
    private final boolean useVector;
    private boolean closed;

    private List<DD> valueToVector(Value value) throws EPMCException {
        if (!useVector) {
            return null;
        } else if (ValueEnum.isEnum(value)) {
            int numBits = ValueNumBitsKnown.getNumBits(value);
            int number = ValueEnum.asEnum(value).getEnum().ordinal();
            return contextDD.twoCplFromInt(number, numBits);
        } else if (ValueInteger.isInteger(value)) {
            return contextDD.twoCplFromInt(ValueInteger.asInteger(value).getInt());
        } else {
            return null;
        }
    }

    private DD valueToSingleDD(Value value) throws EPMCException {
        if (useVector && (ValueInteger.isInteger(value) || ValueEnum.isEnum(value))) {
            return null;
        } else {
            return contextDD.newConstant(value);
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
            throws EPMCException {
        assert variableDD != null;
        assert copy >= 0;
        assert copy < variableDD.getNumCopies();
        if (!useVector || !variableDD.isInteger()) {
            return null;
        }
        List<DD> origVec = new ArrayList<>(contextDD.clone(variableDD.getDDVariables(copy)));
        origVec.add(contextDD.newConstant(false));
        List<DD> add = contextDD.twoCplFromInt(variableDD.getLower());
        
        List<DD> result = contextDD.twoCplAdd(origVec, add);
        contextDD.dispose(add);
        contextDD.dispose(origVec);
        
        return result;
    }
    
    private DD variableDDToSingleDD(VariableDD variableDD, int copy)
            throws EPMCException {
        assert variableDD != null;
        if (useVector && variableDD.isInteger()) {
            return null;
        }
        return variableDD.getValueEncoding(copy);
    }

    public ExpressionToDD(ContextValue contextValue,
            Map<Expression,VariableDD> variables,
            Map<Expression,DD> constants) throws EPMCException {
        assert assertConstructorArgs(contextValue,
            variables,
            constants);
        ContextDD contextDD = getContextDD(contextValue);
        Options options = contextDD.getOptions();
        this.contextValue = contextValue;
        this.contextDD = contextDD;
        this.variables = new HashMap<>();
        this.variables.putAll(variables);
        this.constants = new HashMap<>();
        this.constants.putAll(constants);
        if (options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_CACHE)) {
            this.translationCache = new HashMap<>();
            this.assignCache = new THashMap<>();
        } else {
            this.translationCache = null;
            this.assignCache = null;
        }
        this.useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
    }
    
    private static ContextDD getContextDD(ContextValue contextValue) throws EPMCException {
        return ContextDD.get(contextValue);
    }

    private static boolean assertConstructorArgs(ContextValue
            contextValue, Map<Expression,VariableDD>
    variables, Map<Expression,DD> constants) throws EPMCException {
        assert contextValue != null;
        ContextDD contextDD = getContextDD(contextValue);
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

    public ExpressionToDD(ContextValue contextExpression,
            Map<Expression,VariableDD> variables) throws EPMCException {
        this(contextExpression, variables,
                Collections.<Expression,DD>emptyMap());
    }
    
    public void putConstantWith(Expression expression, DD dd) {
        constants.put(expression, dd);
    }

    public void putConstant(Expression expression, DD dd) {
        putConstantWith(expression, dd.clone());
    }

    public DD translate(Expression expression) throws EPMCException {
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
            throws EPMCException {
        assert alive();
        assert variable != null;
        assert copy >= 0;
        assert copy < variable.getNumCopies();
        assert value != null;
        assert variable.getContext() == contextDD;
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

    private Translated transRec(Expression expression) throws EPMCException {
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
            result = new Translated(getValue(expression));
        } else if (expression instanceof ExpressionOperator) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Operator operator = expressionOperator.getOperator();
            List<Translated> inner = new ArrayList<>();
            
            for (Expression op : expressionOperator.getOperands()) {
                inner.add(transRec(op));
            }
            switch (operator.getIdentifier()) {
            case OperatorAdd.IDENTIFIER:
                result = opAdd(inner.get(0), inner.get(1));
                break;
            case OperatorAddInverse.IDENTIFIER:
                result = opAddInverse(inner.get(0));
                break;
            case OperatorEq.IDENTIFIER:
                result = opEq(inner.get(0), inner.get(1));
                break;
            case OperatorGe.IDENTIFIER:
                result = opGe(inner.get(0), inner.get(1));
                break;
            case OperatorGt.IDENTIFIER:
                result = opGt(inner.get(0), inner.get(1));
                break;
            case OperatorIte.IDENTIFIER:
                result = opIte(inner.get(0), inner.get(1), inner.get(2));
                break;
            case OperatorLe.IDENTIFIER:
                result = opLe(inner.get(0), inner.get(1));
                break;
            case OperatorLt.IDENTIFIER:
                result = opLt(inner.get(0), inner.get(1));
                break;
            case OperatorMax.IDENTIFIER:
                result = opMax(inner.get(0), inner.get(1));
                break;
            case OperatorMin.IDENTIFIER:
                result = opMin(inner.get(0), inner.get(1));
                break;
            case OperatorNe.IDENTIFIER:
                result = opNe(inner.get(0), inner.get(1));
                break;
            case OperatorSubtract.IDENTIFIER:
                result = opSubtract(inner.get(0), inner.get(1));
                break;
                // TODO multiply needs work
//            case MULTIPLY:
  //              result = opMultiply(inner.get(0), inner.get(1));
    //            break;
//            case MOD:
//            case MULTIPLY_INVERSE:
//            case DIVIDE
//                break;
            default:
                result = generalApply(operator, inner);
                break;
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

    private Translated opEq(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD eq = contextDD.twoCplEq(op1.getVector(), op2.getVector());
            result = new Translated(eq, null, null);
            eq.dispose();
        } else {
            result = generalApply(OperatorEq.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opNe(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD ne = contextDD.twoCplNe(op1.getVector(), op2.getVector());
            result = new Translated(ne, null, null);
            ne.dispose();
        } else {
            result = generalApply(OperatorNe.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opGe(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD ge = contextDD.twoCplGe(op1.getVector(), op2.getVector());
            result = new Translated(ge, null, null);
            ge.dispose();
        } else {
            result = generalApply(OperatorGe.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opLe(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD le = contextDD.twoCplLe(op1.getVector(), op2.getVector());
            result = new Translated(le, null, null);
            le.dispose();
        } else {
            result = generalApply(OperatorLe.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opLt(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD lt = contextDD.twoCplLt(op1.getVector(), op2.getVector());
            result = new Translated(lt, null, null);
            lt.dispose();
        } else {
            result = generalApply(OperatorLt.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opGt(Translated op1, Translated op2) throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            DD gt = contextDD.twoCplGt(op1.getVector(), op2.getVector());
            result = new Translated(gt, null, null);
            gt.dispose();
        } else {
            result = generalApply(OperatorGt.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    
    private Translated opAddInverse(Translated op) throws EPMCException {
        Translated result;
        if (op.hasVector()) {
            List<DD> list = contextDD.twoCplAddInverse(op.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorAddInverse.IDENTIFIER, op);
        }
        return result;
    }

    private Translated opAdd(Translated op1, Translated op2)
            throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = contextDD.twoCplAdd(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorAdd.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    /*
    private Translated opMultiply(Translated op1, Translated op2)
            throws EPMCException {
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
            throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = contextDD.twoCplMax(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorMax.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opMin(Translated op1, Translated op2)
            throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = contextDD.twoCplMin(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorMin.IDENTIFIER, op1, op2);
        }
        
        return result;
    }
    
    private Translated opSubtract(Translated op1, Translated op2)
            throws EPMCException {
        Translated result;
        if (op1.hasVector() && op2.hasVector()) {
            List<DD> list = contextDD.twoCplSubtract(op1.getVector(), op2.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorSubtract.IDENTIFIER, op1, op2);
        }
        
        return result;
    }

    private Translated opIte(Translated ifT, Translated thenT, Translated elseT)
            throws EPMCException {
        Translated result;
        if (thenT.hasVector() && elseT.hasVector()) {
            List<DD> list = contextDD.twoCplIte(ifT.getSingleDD(), thenT.getVector(), elseT.getVector());
            result = new Translated(null, list, null);
            contextDD.dispose(list);
        } else {
            result = generalApply(OperatorIte.IDENTIFIER, ifT, thenT, elseT);
        }
        
        return result;
    }
    
    private Translated generalApply(Operator operator, List<Translated> operands)
            throws EPMCException {
        assert operator != null;
        assert operands != null;
        for (Translated trans : operands) {
            assert trans != null;
        }
        Translated[] array = operands.toArray(new Translated[0]);
        return generalApply(operator, array);
    }
    
    private Translated generalApply(Operator operator, Translated... operands)
            throws EPMCException {
        assert operator != null;
        assert operands != null;
        for (Translated trans : operands) {
            assert trans != null;
        }
        DD[] innerDD = new DD[operands.length];
        for (int index = 0; index < operands.length; index++) {
            innerDD[index] = operands[index].getSingleDD();
        }
        DD resultDD = contextDD.apply(operator, innerDD);
        Translated result = new Translated(resultDD, null, null);
        resultDD.dispose();

        return result;
    }

    private Translated generalApply(String operatorString, Translated... operands)
            throws EPMCException {
        assert operatorString != null;
        assert operands != null;
        for (Translated operand : operands) {
            assert operand != null;
        }
        Operator operator = contextValue.getOperator(operatorString);
        assert operator != null;
        return generalApply(operator, operands);
    }

    
    public ContextDD getContextDD() {
        assert alive();
        return contextDD;
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

    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    private boolean alive() {
        return !closed && contextDD.alive();
    }
}
