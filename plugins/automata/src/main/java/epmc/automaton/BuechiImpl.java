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

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.automaton.hoa.HanoiHeader;
import epmc.automaton.hoa.HoaParser;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.options.Options;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public class BuechiImpl implements Buechi {
    private final static String SPOT_PARAM_FORMULA = "-f";
    private final static String SPOT_PARAM_LOW_OPTIMISATIONS = "--low";
    private final static String SPOT_PARAM_FORCE_TRANSITION_BASED_ACCEPTANCE = "-Ht";
    private final static String DETERMINISTIC = "deterministic";

    private final static String IDENTIFIER = "buechi-spot";
    private final GraphExplicit automaton;
    private int numLabels;
    private final int trueState;
    private boolean deterministic;
    private final EvaluatorExplicit[] evaluators;
    private final Expression[] expressions;
    private final Type[] expressionTypes;
    private final String name;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    public BuechiImpl(Expression expression, Expression[] expressions) {
        assert expression != null;
        this.name = UtilExpressionStandard.niceForm(expression);
        // TODO does not work if used there
        //        if (options.getBoolean(OptionsAutomaton.AUTOMATA_REPLACE_NE)) {
        //          expression = replaceNeOperator(expression);
        //    }
        OptionsAutomaton.Ltl2BaAutomatonBuilder builder = Options.get().getEnum(OptionsAutomaton.AUTOMATON_BUILDER);
        Set<Expression> expressionsSeen = new HashSet<>();
        if (builder == OptionsAutomaton.Ltl2BaAutomatonBuilder.SPOT) {
            automaton = createSpotAutomaton(expression, expressionsSeen);
            deterministic = false;
            HanoiHeader header = automaton.getGraphPropertyObject(HanoiHeader.class);
            for (String automatonPropert : header.getProperties()) {
                if (automatonPropert.equals(DETERMINISTIC)) {
                    deterministic = true;
                    break;
                }
            }
        } else {
            automaton = null;
        }
        if (expressions == null) {
            expressions = new Expression[expressionsSeen.size()];
            int index = 0;
            for (Expression expr : expressionsSeen) {
                expressions[index] = expr;
                index++;
            }
        }
        if (this.numLabels == 0) {
            fixNoLabels();
        }
        trueState = findTrueState();
        this.expressions = expressions.clone();
        expressionTypes = new Type[expressions.length];
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            expressionTypes[exprNr] = TypeBoolean.get();
        }

        int totalSize = 0;
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            for (int succNr = 0; succNr < automaton.getNumSuccessors(node); succNr++) {
                totalSize++;
            }
        }
        this.evaluators = new EvaluatorExplicit[totalSize];
        totalSize = 0;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            for (int succNr = 0; succNr < automaton.getNumSuccessors(node); succNr++) {
                BuechiTransition trans = labels.getObject(node, succNr);
                Expression guard = trans.getExpression();
                evaluators[totalSize] = UtilEvaluatorExplicit.newEvaluator(guard,
                        new ExpressionToTypeBoolean(expressions), expressions);
                totalSize++;
            }
        }
        totalSize = 0;
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            for (int succNr = 0; succNr < automaton.getNumSuccessors(node); succNr++) {
                BuechiTransition trans = labels.getObject(node, succNr);
                ((BuechiTransitionImpl) trans).setResult(ValueBoolean.as(evaluators[totalSize].getResultValue()));
                totalSize++;
            }
        }
    }
    
    @Override
    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public void query(Value[] get) {
        for (int i = 0; i < evaluators.length; i++) {
            evaluators[i].setValues(get);
            evaluators[i].evaluate();
        }
    }

    private int findTrueState() {
        int trueState = -1;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            for (int succNr = 0; succNr < automaton.getNumSuccessors(node); succNr++) {
                BuechiTransition trans = labels.getObject(node, succNr);
                Expression expr = trans.getExpression();
                boolean isTrue = isTrue(expr);
                if (isTrue && trans.getLabeling().cardinality() == numLabels) {
                    trueState = node;
                    break;
                }
            }
        }
        return trueState;
    }

    private static GraphExplicit createSpotAutomaton(Expression expression, Set<Expression> expressionsSeen) {
        assert expression != null;
        assert expressionsSeen != null;
        Map<Expression,String> expr2str = new HashMap<>();
        expression = UtilAutomaton.bounded2next(expression);
        int[] numAPs = new int[1];
        UtilAutomaton.expr2string(expression, expr2str, numAPs);
        expressionsSeen.addAll(expr2str.keySet());
        String spotFn = expr2spot(expression, expr2str);
        assert spotFn != null;
        
        Map<String,Expression> ap2expr = new LinkedHashMap<>();
        for (Entry<Expression,String> entry : expr2str.entrySet()) {
            ap2expr.put(entry.getValue(), entry.getKey());
        }
        return createSpotAutomaton(spotFn, null, ap2expr);
    }

    public static GraphExplicit createSpotAutomaton(String spotFn, List<String> additionalSpotParamerters, Map<String, Expression> ap2expr) {
        String ltl2tgba = Options.get().getString(OptionsAutomaton.AUTOMATON_SPOT_LTL2TGBA_CMD);
        try {
            ArrayList<String> autExecArgsList = new ArrayList<>();
            autExecArgsList.addAll(Arrays.asList(new String[]{ltl2tgba,
                    SPOT_PARAM_FORMULA, spotFn,
                    SPOT_PARAM_LOW_OPTIMISATIONS,
                    SPOT_PARAM_FORCE_TRANSITION_BASED_ACCEPTANCE}));
            if (additionalSpotParamerters != null) {
                autExecArgsList.addAll(additionalSpotParamerters);
            }
            final String[] autExecArgs = autExecArgsList.toArray(new String[0]);
            final Process autProcess = Runtime.getRuntime().exec(autExecArgs);
            final BufferedReader autIn = new BufferedReader
                    (new InputStreamReader(autProcess.getInputStream()));
            GraphExplicit automaton;
            HoaParser spotParser = new HoaParser(autIn);
            automaton = spotParser.parseAutomaton(ap2expr);
            try {
                ensure(autProcess.waitFor() == 0, ProblemsAutomaton.LTL2BA_SPOT_PROBLEM_EXIT_CODE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return automaton;
        } catch (IOException e) {
            fail(ProblemsAutomaton.LTL2BA_SPOT_PROBLEM_IO, e, ltl2tgba);
            return null;
        }
    }

    private void fixNoLabels() {
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                trans.getLabeling().set(0);
            }
        }
        numLabels = 1;
    }

    @Override
    public boolean isDeterministic() {
        return deterministic;
    }

    @Override
    public int getNumLabels() {
        return numLabels;
    }

    @Override
    public GraphExplicit getGraph() {
        return automaton;
    }

    @Override
    public int getTrueState() {
        return trueState;
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return Boolean.valueOf(getValue(expressionLiteral));
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return !Boolean.valueOf(getValue(expressionLiteral));
    }

    private static String getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.is(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return expressionLiteral.getValue();
    }

    public static String expr2spot(Expression expression,
            Map<Expression, String> expr2str)  {
        assert expression != null;
        assert expr2str != null;
        for (Entry<Expression, String> entry : expr2str.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null : entry.getKey();
        }
        String result = expr2str.get(expression);
        if (result != null) {
            return result;
        }
        if (ExpressionLiteral.is(expression)) {
            // must be true or false
            result = expression.toString();
        } else if (expression instanceof ExpressionOperator) {
            ExpressionOperator op = (ExpressionOperator) expression;
            if (isAnd(op)) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " & " + right + ")";
            } else if (isOr(op)) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " | " + right + ")";
            } else if (isNot(op)) {
                String left = expr2spot(op.getOperand1(), expr2str);
                result =  "(!" + left + ")";
            } else if (isIff(op)) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " <=> " + right + ")";
            } else if (isImplies(op)) {
                String left = expr2spot(op.getOperand1(), expr2str);
                String right = expr2spot(op.getOperand2(), expr2str);
                result = "(" + left + " => " + right + ")";                
            } else if (isIte(op)) {
                String ifStr = expr2spot(op.getOperand1(), expr2str);
                String thenStr = expr2spot(op.getOperand2(), expr2str);
                String elseStr = expr2spot(op.getOperand3(), expr2str);
                result = "(" + ifStr + " & " + thenStr + " | !" + ifStr +
                        " & " + elseStr + ")";
            } else {
                assert false : expression;
            }
        } else if (ExpressionIdentifier.is(expression)) {
            assert false;
        } else if (isUntil(expression)) {
            ExpressionTemporalUntil temp = ExpressionTemporalUntil.as(expression);
            String left = expr2spot(temp.getOperandLeft(), expr2str);
            String right = expr2spot(temp.getOperandRight(), expr2str);
            if (isTrue(temp.getOperandLeft())) {
                result = "(F " + right + ")";
            } else {
                result = "(" + left + " U " + right + ")";
            }
        } else if (isRelease(expression)) {
            ExpressionTemporalRelease temp = ExpressionTemporalRelease.as(expression);
            String left = expr2spot(temp.getOperandLeft(), expr2str);
            String right = expr2spot(temp.getOperandRight(), expr2str);
            if (isFalse(temp.getOperandLeft())) {
                result = "(G " + right + ")";
            } else {
                result = "(" + left + " R " + right + ")";
            }
        } else if (isNext(expression)) {
            ExpressionTemporalNext temp = ExpressionTemporalNext.as(expression);
            String left = expr2spot(temp.getOperand(), expr2str);
            result = "(X " + left + ")";
        } else if (isFinally(expression)) {
            ExpressionTemporalFinally temp = ExpressionTemporalFinally.as(expression);
            String inner = expr2spot(temp.getOperand(), expr2str);
            result = "(F " + inner + ")";
        } else if (isGlobally(expression)) {
            ExpressionTemporalGlobally temp = ExpressionTemporalGlobally.as(expression);
            String inner = expr2spot(temp.getOperand(), expr2str);
            result = "(G " + inner + ")";
        } else if (ExpressionQuantifier.is(expression)) {
            assert false;
        } else {
            assert false : expression.getClass();
        }
        expr2str.put(expression,  result);
        return result;
    }

    private static boolean isAnd(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorAnd.AND);
    }

    private static boolean isNext(Expression expression) {
        return ExpressionTemporalNext.is(expression);
    }

    private static boolean isFinally(Expression expression) {
        return ExpressionTemporalFinally.is(expression);
    }

    private static boolean isGlobally(Expression expression) {
        return ExpressionTemporalGlobally.is(expression);
    }

    private static boolean isRelease(Expression expression) {
        return ExpressionTemporalRelease.is(expression);
    }

    private static boolean isUntil(Expression expression) {
        return ExpressionTemporalUntil.is(expression);
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isIff(Expression expression) {
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorIff.IFF);
    }

    private static boolean isOr(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorOr.OR);
    }

    private static boolean isImplies(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorImplies.IMPLIES);
    }

    private static boolean isIte(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorIte.ITE);
    }

    public static Expression replaceNeOperator(Expression expression)
    {
        assert expression != null;
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : expression.getChildren()) {
            newChildren.add(replaceNeOperator(child));
        }
        if (!isNe(expression)) {
            return expression.replaceChildren(newChildren);
        } else {
            return not(new ExpressionOperator.Builder()
                    .setOperator(OperatorEq.EQ)
                    .setOperands(newChildren)
                    .build());
        }
    }

    private static boolean isNe(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNe.NE);
    }

    private static Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setOperands(expression)
                .build();
    }

    @Override
    public String getName() {
        return name;
    }
}
