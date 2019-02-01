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

package epmc.automaton;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.automaton.hoa.HoaParser;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.explicit.GraphExplicit;
import epmc.messages.Message;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.Util;
import epmc.value.TypeBoolean;
import epmc.value.UtilValue;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;

public final class UtilAutomaton {
    public static String expr2string(Expression expression, Map<Expression, String> expr2str,
            int[] numAPs) {
        Options options = Options.get();
        return expr2string(expression, expr2str, numAPs, options.getBoolean(OptionsAutomaton.AUTOMATON_SUBSUME_APS));
    }

    public static Buechi newBuechi
    (Expression property, Expression[] expressions, boolean isNondet,
            ValueBoolean negate) {
        assert property != null;
        assert negate != null;
        Options options = Options.get();
        Buechi buechi = null;
        Log log = options.get(OptionsMessages.LOG);
        if (isNondet) {
            Expression usedProperty = negate.getBoolean() ? not(property) : property;
            if (negate.getBoolean()) {
                log.send(MessagesAutomaton.COMPUTING_NEG_BUECHI);
            } else {
                log.send(MessagesAutomaton.COMPUTING_ORIG_BUECHI);
            }
            buechi = new BuechiImpl(usedProperty, expressions);
            Message buechiDone = buechi.isDeterministic() ?
                    MessagesAutomaton.COMPUTING_BUECHI_DONE_DET
                    : MessagesAutomaton.COMPUTING_BUECHI_DONE_NONDET;
            log.send(buechiDone, buechi.getNumLabels(), buechi.getNumStates());
        } else {
            Buechi origBuechi = null;
            Buechi negBuechi = null;
            OptionsAutomaton.Ltl2BaDetNeg detNeg = options.getEnum(OptionsAutomaton.AUTOMATON_DET_NEG);
            if (detNeg == OptionsAutomaton.Ltl2BaDetNeg.NEVER
                    || detNeg == OptionsAutomaton.Ltl2BaDetNeg.BETTER) {
                log.send(MessagesAutomaton.COMPUTING_ORIG_BUECHI);
                origBuechi = new BuechiImpl(property, expressions);
                Message buechiDone = origBuechi.isDeterministic() ?
                        MessagesAutomaton.COMPUTING_BUECHI_DONE_DET
                        : MessagesAutomaton.COMPUTING_BUECHI_DONE_NONDET;
                log.send(buechiDone,
                        origBuechi.getNumLabels(), origBuechi.getNumStates());
            }
            if (detNeg == OptionsAutomaton.Ltl2BaDetNeg.BETTER || detNeg == OptionsAutomaton.Ltl2BaDetNeg.ALWAYS) {
                log.send(MessagesAutomaton.COMPUTING_NEG_BUECHI);
                negBuechi = new BuechiImpl(not(property), expressions);
                Message buechiDone = negBuechi.isDeterministic() ?
                        MessagesAutomaton.COMPUTING_BUECHI_DONE_DET
                        : MessagesAutomaton.COMPUTING_BUECHI_DONE_NONDET;
                log.send(buechiDone,
                        negBuechi.getNumLabels(), negBuechi.getNumStates());
            }
            if (negBuechi == null) {
                buechi = origBuechi;
            } else if (origBuechi == null) {
                buechi = negBuechi;
            } else if (origBuechi.isDeterministic() && !negBuechi.isDeterministic()) {
                buechi = origBuechi;
            } else if (!origBuechi.isDeterministic() && negBuechi.isDeterministic()) {
                buechi = negBuechi;
            } else if (origBuechi.getNumLabels() < negBuechi.getNumLabels()) {
                buechi = origBuechi;
            } else if (origBuechi.getNumLabels() > negBuechi.getNumLabels()) {
                buechi = negBuechi;
            } else if (origBuechi.getNumStates() < negBuechi.getNumStates()) {
                buechi = origBuechi;
            } else if (negBuechi.getNumStates() > negBuechi.getNumStates()) {
                buechi = negBuechi;
            } else {
                buechi = origBuechi;
            }
            if (buechi == negBuechi) {
                negate.set(true);
                log.send(MessagesAutomaton.USING_NEG_BUECHI);
            } else {
                negate.set(false);
                log.send(MessagesAutomaton.USING_ORIG_BUECHI);
            }
        }

        return buechi;
    }

    public static GraphExplicit newBuechi(String property, List<String> spotParameters) {
        assert property != null;
        return BuechiImpl.createSpotAutomaton(property, spotParameters, null);
    }
    
    public static Buechi computeBuechi(Expression property, Expression[] expressions) {
        assert property != null;
        assert expressions != null;
        for (Expression expression : expressions) {
            assert expression != null;
        }
        ValueBoolean negate = UtilValue.newValue(TypeBoolean.get(), false);
        return newBuechi(property, expressions, true, negate);
    }

    public static Expression bounded2next(Expression expression) {
        assert expression != null;
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(bounded2next(child));
        }
        expression = expression.replaceChildren(newChildren);

        TimeBound timeBound = null;
        Expression leftExpr = null;
        Expression rightExpr = null;
        if (isUntil(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            timeBound = expressionTemporal.getTimeBound();
            leftExpr = expressionTemporal.getOperandLeft();
            rightExpr = expressionTemporal.getOperandRight();
        } else if (isRelease(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            timeBound = expressionTemporal.getTimeBound();
            leftExpr = expressionTemporal.getOperandLeft();
            rightExpr = expressionTemporal.getOperandRight();
        }
        if (timeBound != null && (timeBound.isLeftBounded() || timeBound.isRightBounded())) {
            int boundLeft = ValueInteger.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getInt();
            int bound;
            Expression result;
            if (timeBound.isRightBounded()) {
                result = rightExpr;
                bound = ValueInteger.as(UtilEvaluatorExplicit.evaluate(timeBound.getRight())).getInt();;
            } else {
                result = expression;
                bound = boundLeft;
            }
            bound--;
            while (bound >= 0) {
                Expression nextResult = newNext(result, expression.getPositional());
                if (isUntil(expression)) {
                    result = and(leftExpr, nextResult, expression.getPositional());
                } else if (isRelease(expression)) {
                    result = or(leftExpr, nextResult, expression.getPositional());
                }
                if (bound - boundLeft >= 0) {
                    if (isUntil(expression)) {
                        result = or(result, rightExpr, expression.getPositional());
                    } else if (isRelease(expression)) {
                        result = and(result, rightExpr, expression.getPositional());
                    }
                }
                bound--;
            }
            return result;
        } else {
            return expression;
        }
    }

    public static Set<Expression> collectLTLInner(Expression expression) {
        if (ExpressionPropositional.is(expression)) {
            return Collections.singleton(expression);
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionOperator.is(expression)) {
            ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);           
        }
    }

    public static AutomatonRabin newAutomatonRabin(
            Expression expression,
            Expression[] expressions) {
        assert expression != null;
        assert expressions != null;
        for (Expression entry : expressions) {
            assert entry != null;
        }
        Map<String,Class<Automaton.Builder>> automata = Options.get().get(OptionsAutomaton.AUTOMATON_CLASS);
        AutomatonRabin.Builder result = (AutomatonRabin.Builder)
                Util.getInstanceByClass(automata,
                        a -> AutomatonRabin.Builder.class.isAssignableFrom(a));
        assert result != null;
        result.setExpression(expression, expressions);
        return result.build();
    }

    public static AutomatonExporter getExporter(Automaton automaton,
            AutomatonExporterFormat format) {
        Map<String,Class<? extends AutomatonExporter>> automata = Options.get().get(OptionsAutomaton.AUTOMATON_EXPORTER_CLASS);
        return Util.getInstance(automata, exporter -> 
            exporter.setAutomaton(automaton).setFormat(format).canHandle());
    }
    
    public static AutomatonRabin newAutomatonRabinSafra(Buechi buechi, BitSet automatonState) {
        Map<String,Class<Automaton.Builder>> automata = Options.get().get(OptionsAutomaton.AUTOMATON_CLASS);
        AutomatonSafra.Builder result = (AutomatonSafra.Builder)
                Util.getInstanceByClass(automata,
                        a -> AutomatonRabin.Builder.class.isAssignableFrom(a)
                        && AutomatonSafra.Builder.class.isAssignableFrom(a));
        assert result != null;
        result.setBuechi(buechi);
        result.setInit(automatonState);
        return (AutomatonRabin) result.build();
    }

    public static AutomatonParity newAutomatonParity(
            Expression expression,
            Expression[] expressions) {
        assert expression != null;
        assert expressions != null;
        for (Expression entry : expressions) {
            assert entry != null;
        }
        Map<String,Class<Automaton.Builder>> automata = Options.get().get(OptionsAutomaton.AUTOMATON_CLASS);
        AutomatonParity.Builder result = (AutomatonParity.Builder)
                Util.getInstanceByClass(automata,
                        a -> AutomatonParity.Builder.class.isAssignableFrom(a));
        assert result != null;
        result.setExpression(expression, expressions);
        return result.build();
    }

    private static Expression and(Expression a, Expression b, Positional positional) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAnd.AND)
                .setPositional(positional)
                .setOperands(a, b)
                .build();
    }

    private static Expression or(Expression a, Expression b, Positional positional) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorOr.OR)
                .setPositional(positional)
                .setOperands(a, b)
                .build();
    }

    private static Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setPositional(expression.getPositional())
                .setOperands(expression)
                .build();
    }

    private static boolean isRelease(Expression expression) {
        return ExpressionTemporalRelease.is(expression);
    }

    private static boolean isUntil(Expression expression) {
        return ExpressionTemporalUntil.is(expression);
    }

    private static ExpressionTemporalNext newNext(Expression operand, Positional positional) {
        return new ExpressionTemporalNext.Builder()
                .setOperand(operand)
                .setTimeBound(new TimeBound.Builder().build())
                .setPositional(positional)
                .build();
    }

    private static String expr2string(Expression expression, Map<Expression, String> expr2str,
            int[] numAPs, boolean subsumeAPs) {
        String result = expr2str.get(expression);
        if (result != null) {
            return result;
        }
        if (ExpressionLiteral.is(expression)) {
            assert isTrue(expression) || isFalse(expression);
            ExpressionLiteral literal = ExpressionLiteral.as(expression);
            result = literal.getValue().toString();
        } else if (expression instanceof ExpressionOperator) {
            ExpressionOperator op = (ExpressionOperator) expression;
            if (isAnd(op) || isOr(op) || isIff(op) || isImplies(op)
                    || isIte(op)) {
                String left = expr2string(op.getOperand1(), expr2str, numAPs, subsumeAPs);
                String right = expr2string(op.getOperand2(), expr2str, numAPs, subsumeAPs);
                if (left != null && right != null && subsumeAPs) {
                    result = "ap" + numAPs[0];
                    numAPs[0]++;            
                }
            } else if (isNot(op)) {
                String left = expr2string(op.getOperand1(), expr2str, numAPs, subsumeAPs);
                if (left != null && subsumeAPs) {
                    result = "ap" + numAPs[0];
                    numAPs[0]++;            
                }
            } else {
                result = "ap" + numAPs[0];
                numAPs[0]++;
            }
        } else if (ExpressionIdentifier.is(expression)) {
            result = "ap" + numAPs[0];
            numAPs[0]++;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionNext = ExpressionTemporalNext.as(expression);
            expr2string(expressionNext.getOperand(), expr2str, numAPs, subsumeAPs);
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionNext = ExpressionTemporalFinally.as(expression);
            expr2string(expressionNext.getOperand(), expr2str, numAPs, subsumeAPs);
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionNext = ExpressionTemporalGlobally.as(expression);
            expr2string(expressionNext.getOperand(), expr2str, numAPs, subsumeAPs);
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionNext = ExpressionTemporalRelease.as(expression);
            expr2string(expressionNext.getOperandLeft(), expr2str, numAPs, subsumeAPs);
            expr2string(expressionNext.getOperandRight(), expr2str, numAPs, subsumeAPs);
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionNext = ExpressionTemporalUntil.as(expression);
            expr2string(expressionNext.getOperandLeft(), expr2str, numAPs, subsumeAPs);
            expr2string(expressionNext.getOperandRight(), expr2str, numAPs, subsumeAPs);
        } else if (expression instanceof ExpressionQuantifier) {
            result = "ap" + numAPs[0];
            numAPs[0]++;
        }
        if (result != null) {
            expr2str.put(expression,  result);
        }
        return result;
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return !Boolean.valueOf(getValue(expressionLiteral));
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return Boolean.valueOf(getValue(expressionLiteral));
    }

    private static String getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.is(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return expressionLiteral.getValue();
    }

    private static boolean isAnd(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorAnd.AND);
    }

    private static boolean isOr(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorOr.OR);
    }

    private static boolean isIte(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorIte.ITE);
    }

    private static boolean isIff(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorIff.IFF);
    }

    private static boolean isImplies(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorImplies.IMPLIES);
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    public static GraphExplicit parseHOAAutomaton(Reader reader, Map<String, Expression> ap2expr) {
        HoaParser spotParser = new HoaParser(reader);
        return spotParser.parseAutomaton(ap2expr);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilAutomaton() {
    }
}
