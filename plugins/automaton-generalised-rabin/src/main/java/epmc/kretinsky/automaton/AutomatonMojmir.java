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

package epmc.kretinsky.automaton;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import epmc.automaton.AutomatonExporter;
import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.kretinsky.options.KretinskyOptimiseMojmir;
import epmc.kretinsky.options.OptionsKretinsky;
import epmc.kretinsky.util.UtilKretinsky;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;
import epmc.util.StopWatch;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class AutomatonMojmir implements AutomatonNumeredInput {
    private final ContextExpression contextExpression;
    private final Evaluator formulaEvaluator;
    private final boolean implicit;
    private final AutomatonMojmirState initState;
    private int succState;
    private int succLabel;
    private final boolean simpleG;
    private final TIntList sinkMask = new TIntArrayList();
    private final AutomatonMaps observerMaps = new AutomatonMaps();
    private boolean closed;
    private final ExpressionsUnique expressionsUnique;
    private Expression[] stateExpressions;
    private DD[] stateExpressionsDD;
    private Value[][] consistentValues;
    private Expression[] consistentExpressions;
    private int[] successorStates;
    private int[] successorLabels;
    private boolean useGFFGOptimisation;
    private KretinskyOptimiseMojmir optimisation;
    private Map<Expression,Expression> languageMap;

    public AutomatonMojmir(Expression formula, ExpressionsUnique expressionsUnique,
            boolean implicit, boolean simpleG) {
        formula = UtilExpression.toNegationNormalForm(formula);
        this.languageMap = contextExpression.newMap();
        this.expressionsUnique = expressionsUnique;
        this.implicit = implicit;
        this.simpleG = simpleG;

        Options options = Options.get();
        this.useGFFGOptimisation = options.getBoolean(OptionsKretinsky.KRETINSKY_GFFG_OPTIMISATION);
        this.optimisation = options.get(OptionsKretinsky.KRETINSKY_OPTIMISE_MOJMIR);
        formula = formula.replace(expressionsUnique.getReplacement());

        this.formulaEvaluator = contextExpression.newEvaluator(expressionsUnique.getReplaced());
        Map<Expression, Type> propositionalTypes = collectPropositionalTypes(formula);
        for (Expression propositional : propositionalTypes.keySet()) {
            formulaEvaluator.addExpression(propositional);
        }
        formulaEvaluator.addExpression(contextExpression.getFalse());
        formulaEvaluator.addExpression(contextExpression.getTrue()); 

        formula = expressionsUnique.makeUnique(formula);
        this.initState = makeUnique(new AutomatonMojmirState(this, formula));
        this.consistentValues = expressionsUnique.getConsistentValues();
        this.consistentExpressions = expressionsUnique.getConsistentExpressions();
        if (!implicit) {
            explore();
        }
    }

    @Override
    public int getNumStates() {
        return observerMaps.getNumStates();
    }

    protected <T extends AutomatonStateUtil> T makeUnique(T state) {
        return observerMaps.makeUnique(state);
    }

    protected <T extends AutomatonLabelUtil> T makeUnique(T label) {
        return observerMaps.makeUnique(label);
    }

    @Override
    public AutomatonMojmirState numberToState(int number) {
        return (AutomatonMojmirState) observerMaps.numberToState(number);
    }

    private void explore() {
        Set<AutomatonMojmirState> seen = new HashSet<>();
        Queue<AutomatonMojmirState> todo = new LinkedList<>();
        todo.add(initState);
        seen.add(initState);
        AutomatonMojmirState[] succStates = new AutomatonMojmirState[consistentValues.length];
        Expression[] succExpressions = new Expression[consistentValues.length];
        List<Expression> stateExpressions = new ArrayList<>();
        List<DD> stateExpressionsDD = new ArrayList<>();
        TIntList successorStates = new TIntArrayList();
        TIntList successorLabels = new TIntArrayList();
        // TODO optionally subsume expressions with same language
        while (!todo.isEmpty()) {
            AutomatonMojmirState current = todo.poll();
            for (int succNr = 0; succNr < consistentValues.length; succNr++) {
                Value[] succ = consistentValues[succNr];
                AutomatonMojmirState succState = computeSuccessor(current, succ);
                Expression entry = consistentExpressions[succNr];
                succStates[succNr] = succState;
                succExpressions[succNr] = entry;
                successorStates.add(succState.getNumber());
                successorLabels.add(makeUnique(new AutomatonMojmirLabel(consistentExpressions[succNr])).getNumber());
                if (!seen.contains(succState)) {
                    seen.add(succState);
                    todo.add(succState);
                }
            }
            stateExpressions.add(current.getExpression());
            stateExpressionsDD.add(expressionsUnique.getExpressionDD(current.getExpression()));
        }
        this.stateExpressions = stateExpressions.toArray(new Expression[seen.size()]);
        this.stateExpressionsDD = stateExpressionsDD.toArray(new DD[seen.size()]);
        this.successorStates = successorStates.toArray(new int[successorStates.size()]);
        this.successorLabels = successorLabels.toArray(new int[successorLabels.size()]);
    }

    private AutomatonMojmirState computeSuccessor(
            AutomatonMojmirState current, Value[] succ) {
        Expression formula = current.getExpression();
        Expression succExpr = af(formula, succ, simpleG);
        succExpr = expressionsUnique.makeUnique(succExpr);
        if (optimisation == KretinskyOptimiseMojmir.LANGUAGE) {
            Expression equiv = languageMap.get(succExpr);
            if (equiv != null) {
                succExpr = equiv;
            } else {
                boolean foundEquiv = false;
                Collection<Expression> allExpressions = languageMap.keySet();
                for (Expression compare : allExpressions) {
                    if (UtilKretinsky.spotLTLEquivalent(compare, succExpr)) {
                        languageMap.put(succExpr, compare);
                        succExpr = compare;
                        foundEquiv = true;
                        break;
                    }
                }
                if (!foundEquiv) {
                    languageMap.put(succExpr, succExpr);
                }
            }
        }
        AutomatonMojmirState succState = makeUnique(new AutomatonMojmirState(this, succExpr));
        return succState;
    }

    private Map<Expression, Type> collectPropositionalTypes(Expression formula)
    {
        assert formula != null;
        if (formula.isPropositional() && !formula.isTrue() && !formula.isFalse()) {
            return Collections.singletonMap(formula, formula.getType());
        } else {
            Map<Expression, Type> result = new HashMap<>();
            for (Expression child : formula.getChildren()) {
                result.putAll(collectPropositionalTypes(child));
            }
            return result;
        }
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] input, int automatonState)
    {
        assert assertModelState(input);
        int inputNr = expressionsUnique.valueToNumber(input);
        queryState(inputNr, automatonState);
    }

    @Override
    public void queryState(int inputNr, int automatonState) {
        if (implicit) {
            Value[] modelState = consistentValues[inputNr];
            AutomatonMojmirState kretinskyState = numberToState(automatonState);
            this.succState = computeSuccessor(kretinskyState, modelState).getNumber();
            this.succLabel = makeUnique(new AutomatonMojmirLabel(consistentExpressions[inputNr])).getNumber();
        } else {
            this.succLabel = successorLabels[automatonState * consistentValues.length + inputNr];
            this.succState = successorStates[automatonState * consistentValues.length + inputNr];
        }
    }

    private Expression af(Expression formula, Value[] modelState, boolean simpleG)
    {
        formula = expressionsUnique.makeUnique(formula);
        if (formula.isPropositional()) {
            formulaEvaluator.setVariableValues(modelState);
            formulaEvaluator.evaluate(formula);
            boolean value = formulaEvaluator.getResultBoolean(formula);
            return value ? contextExpression.getTrue() : contextExpression.getFalse();
        } else if (formula.isAnd()) {
            Expression left = af(formula.getOperand1(), modelState, simpleG);
            Expression right = af(formula.getOperand2(), modelState, simpleG);
            return and(left, right);
        } else if (formula.isOr()) {
            Expression left = af(formula.getOperand1(), modelState, simpleG);
            Expression right = af(formula.getOperand2(), modelState, simpleG);
            return or(left, right);
        } else if (formula.isNext()) {
            return formula.getOperand1();
        } else if (useGFFGOptimisation && isGFFG(formula)) {
            return formula;
        } else if (formula.isGlobally()) {
            if (simpleG) {
                return formula;
            } else {
                Expression inner = formula.getOperand1();
                Expression afInner = af(inner, modelState, simpleG);
                return and(afInner, formula);
            }
        } else if (formula.isFinally()) {
            Expression inner = formula.getOperand1();
            Expression afInner = af(inner, modelState, simpleG);
            return or(afInner, formula);
        } else if (formula.isUntil()) {
            Expression left = formula.getOperand1();
            Expression right = formula.getOperand2();
            Expression afLpart = af(right, modelState, simpleG);
            Expression afPrt2 = af(left, modelState, simpleG);
            return or(afLpart, and(afPrt2, formula));
        } else if (formula.isRelease()) {
            Expression left = formula.getOperand1();
            Expression right = formula.getOperand2();
            Expression afLpart = af(right, modelState, simpleG);
            Expression afP2 = af(left, modelState, simpleG);
            return and(afLpart, or(afP2, formula));
        } else {
            assert false : formula;
        return null;
        }
    }

    private boolean isGFFG(Expression formula) {
        boolean fSeen = formula.isFinally();
        boolean gSeen = formula.isGlobally();
        while ((formula.isFinally() || formula.isGlobally())
                && !(fSeen && gSeen)) {
            formula = formula.getOperand1();
            fSeen |= formula.isFinally();
            gSeen |= formula.isGlobally();
        }
        return fSeen && gSeen;
    }

    private Expression or(Expression left, Expression right) {
        if (left.isTrue()) {
            return left;
        } else if (left.isFalse()) {
            return right;
        } else if (right.isTrue()) {
            return right;
        } else if (right.isFalse()) {
            return left;
        } else {
            return left.or(right);
        }
    }

    private Expression and(Expression left, Expression right) {
        if (left.isTrue()) {
            return right;
        } else if (left.isFalse()) {
            return left;
        } else if (right.isTrue()) {
            return left;
        } else if (right.isFalse()) {
            return right;
        } else {
            return left.and(right);
        }
    }

    private boolean assertModelState(Value[] modelState) {
        assert modelState != null;
        assert modelState.length == expressionsUnique.getReplaced().length
                : Arrays.toString(modelState) + " " + Arrays.toString(expressionsUnique.getReplaced());
        for (int i = 0; i < modelState.length; i++) {
//            assert expressionsUnique.getReplaced()[i].getType().canImport(modelState[i]);
        }
        return true;
    }

    @Override
    public int getSuccessorState() {
        return succState;
    }

    @Override
    public int getSuccessorLabel() {
        return succLabel;
    }

    @Override
    public ContextExpression getContextExpression() {
        return contextExpression;
    }

    @Override
    public Expression[] getExpressions() {
        return expressionsUnique.getExpressions();
    }

    // TODO remove
    Value[][] getConsistentValues() {
        return consistentValues;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    boolean isImplicit() {
        return implicit;
    }

    boolean isSimpleG() {
        return simpleG;
    }

    @Override
    public String toString() {
        if (implicit) {
            return "implicit";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("digraph {\n");
        for (int node = 0; node < observerMaps.getNumStates(); node++) {
            buffer.append("  ");
            buffer.append(node);
            buffer.append(" [label=\"");
            buffer.append(stateExpressions[node]);
            buffer.append("\"];\n");
        }
        buffer.append("\n");
        for (int node = 0; node < observerMaps.getNumStates(); node++) {
            for (int succ = 0; succ < consistentValues.length; succ++) {
                int succNode = successorStates[node * consistentValues.length + succ];
                buffer.append("  ");
                buffer.append(node);
                buffer.append(" -> ");
                buffer.append(succNode);
                buffer.append(" [label=\"");
                Expression guard = consistentExpressions[succ];
                buffer.append(guard.toString(false, false));
                buffer.append("\"];\n");                    
            }
        }
        buffer.append("}\n");        
        return buffer.toString();
    }

    boolean isSink(int state) {
        while (sinkMask.size() <= state) {
            sinkMask.add(-1);
        }
        if (sinkMask.get(state) == -1) {
            boolean leaving = false;
            for (int value = 0; value < consistentValues.length; value++) {
                queryState(value, state);
                int succState = getSuccessorState();
                if (succState != state) {
                    leaving = true;
                    break;
                }
            }
            sinkMask.set(state, leaving ? 0 : Integer.MAX_VALUE);
        }
        return sinkMask.get(state) == Integer.MAX_VALUE;
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return observerMaps.numberToLabel(number);
    }

    DD getStateExpressionDD(int number) {
        return stateExpressionsDD[number];
    }

    Expression getStateExpression(int number) {
        return stateExpressions[number];
    }

    public static void main(String[] args) {
        StopWatch watch = Util.newStopWatch(true);
        Options options = UtilOptionsEPMC.newOptions();
        UtilPlugin.preparePlugins(options);
        ContextExpression contextExpression = UtilExpression.newContextExpression(options);
        options.set(OptionsValue.CONTEXT_VALUE, contextValue);
        //        Expression formula = contextExpression.parse("b & (X(b)) & (G(a & (X(b U c))))");
        //        Expression formula = contextExpression.parse("(G(F((X(X(X(a)))) & (X(X(X(X(b)))))))) & (G(F(b | (X(c))))) & (G(F(c & (X(X(a))))))");
        //        Expression formula = contextExpression.parse("(G(F(c & (X(X(a)))) & (F(a & (X(b))))))");
        //        Expression formula = contextExpression.parse("(G(((p1) & (X(!(p1)))) | (X((p1) U ((p1) & (!(p2)) & (X((p1) & (p2) & ((p1) U ((p1) & (!(p2)) & (X((p1) & (p2))))))))))))");
        //        Expression formula = contextExpression.parse("(X((G(a)) | (X(G(b)))))");
        Expression formula = UtilModelChecker.parse(options, "a & (G(a))");
        Set<Expression> identifiers = formula.collectIdentifiers();
        int idNr = 0;
        Expression[] expressions = new Expression[identifiers.size()];
        for (Expression identifier : identifiers) {
            expressions[idNr] = identifier;
            identifier.setType(contextValue.getTypeBoolean());
            idNr++;
        }
        ExpressionsUnique expressionsUnique = new ExpressionsUnique(contextExpression, expressions);
        AutomatonMojmir master = new AutomatonMojmir(formula, expressionsUnique, true, true);
        AutomatonExporter exporter = new AutomatonExporterDot();
        exporter.setAutomaton(master);
        exporter.setOutput(System.out);
        exporter.export();

        System.out.println(master);
        int state = master.getInitState();
        System.out.println(state);
        Value[] modelState = new Value[1];
        modelState[0] = contextValue.getFalse();
        master.queryState(modelState, state);
        state = master.getSuccessorState();
        System.out.println(state);
        master.queryState(modelState, state);
        state = master.getSuccessorState();
        System.out.println(state);

        master.close();
        contextExpression.close();
        System.out.println(watch.getTimeSeconds());
    }

    public ExpressionsUnique getExpressionsUnique() {
        return expressionsUnique;
    }
}
