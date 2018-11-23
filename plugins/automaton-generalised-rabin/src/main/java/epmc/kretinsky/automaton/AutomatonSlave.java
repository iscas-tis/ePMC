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

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongLongHashMap;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;

import epmc.automaton.AutomatonExporter;
import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;
import epmc.value.ContextValue;
import epmc.value.Value;

public final class AutomatonSlave implements AutomatonNumeredInput {
    private final ContextExpression contextExpression;
    private final Expression[] expressions;
    private final AutomatonSlaveState initState;
    private final Value[][] consistentValues;
    private final AutomatonMojmir observerMojmir;
    private boolean closed;
    private final boolean closeInner;
    private int succState;
    private int succLabel;
    private final AutomatonMaps observerMaps = new AutomatonMaps();
    private final BitSet usedPriorities = new BitSet();
    private final TLongLongMap cache = new TLongLongHashMap(100, 0.5f, -1, -1);
    private ExpressionsUnique expressionsUnique;

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
    public AutomatonStateUtil numberToState(int number) {
        return observerMaps.numberToState(number);
    }

    AutomatonSlave(AutomatonMojmir observerExpression, boolean closeInner) {
        assert observerExpression != null;
        assert observerExpression.isSimpleG();
        this.closeInner = closeInner;
        contextExpression = observerExpression.getContextExpression();
        this.observerMojmir = observerExpression;
        this.expressions = observerExpression.getExpressions();
        this.consistentValues = observerExpression.getConsistentValues();
        int[] initContent = new int[1];
        initContent[0] = 0;
        this.initState = makeUnique(new AutomatonSlaveState(this, initContent));
        this.expressionsUnique = observerExpression.getExpressionsUnique();
    }

    AutomatonSlave(Expression expression, ExpressionsUnique expressionsUnique, boolean implicit) {
        this(new AutomatonMojmir(expression, expressionsUnique, false, true), true);
    }

    private void computeSuccessor(AutomatonSlaveState current, int succNr)
    {
        if (current.getContent().length == 0) {
            AutomatonSlaveState succState = makeUnique(new AutomatonSlaveState(this, current.getContent()));
            this.succState = succState.getNumber();
            this.succLabel = makeUnique(new AutomatonSlaveLabel(succState, succNr)).getNumber();
            return;
        }

        int[] currentContent = current.getContent();
        int maxSuccState = 0;
        for (int i = 0; i < currentContent.length; i++) {
            int succStateNr = getIthExpressionStateSuccessor(i, succNr);
            maxSuccState = Math.max(maxSuccState, succStateNr);
        }

        int[] succContent = new int[maxSuccState + 1];
        Arrays.fill(succContent, Integer.MAX_VALUE);
        for (int i = 0; i < currentContent.length; i++) {
            int iPriority = currentContent[i];
            int succStateNr = getIthExpressionStateSuccessor(i, succNr);
            succContent[succStateNr] = Math.min(succContent[succStateNr], iPriority);
        }
        for (int i = 0; i < succContent.length; i++) {
            if (observerMojmir.isSink(i)) {
                succContent[i] = Integer.MAX_VALUE;
            }
        }

        usedPriorities.clear();
        for (int i = 0; i < succContent.length; i++) {
            if (succContent[i] != Integer.MAX_VALUE) {
                usedPriorities.set(succContent[i]);
            }
        }
        int newPriority = 0;
        TIntIntMap oldToNewPriority = new TIntIntHashMap();
        for (int oldPriority = usedPriorities.nextSetBit(0); oldPriority >= 0;
                oldPriority = usedPriorities.nextSetBit(oldPriority+1)) {
            oldToNewPriority.put(oldPriority, newPriority);
            newPriority++;
        }
        oldToNewPriority.put(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < succContent.length; i++) {
            succContent[i] = oldToNewPriority.get(succContent[i]);
        }
        succContent[0] = Math.min(succContent[0], newPriority);

        AutomatonSlaveState succState = makeUnique(new AutomatonSlaveState(this, succContent));
        this.succState = succState.getNumber();
        this.succLabel = makeUnique(new AutomatonSlaveLabel(succState, succNr)).getNumber();
    }

    private int getIthExpressionStateSuccessor(int i, int value) {
        observerMojmir.queryState(value, i);
        return observerMojmir.getSuccessorState();
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
    {
        int succNr = expressionsUnique.valueToNumber(modelState);
        queryState(succNr, automatonState);
    }

    @Override
    public void queryState(int modelState, int observerState)
    {
        long cacheKey = (((long) modelState) << 32) | (observerState);
        long cacheVal = cache.get(cacheKey);
        if (cacheVal == -1) {
            computeSuccessor((AutomatonSlaveState) numberToState(observerState), modelState);
            cacheVal = (((long) this.succState) << 32) | (this.succLabel);            
            cache.put(cacheKey, cacheVal);
        } else {
            this.succState = (int) (cacheVal >>> 32);
            this.succLabel = (int) (cacheVal & 0xFFFF);
        }
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
        return expressions;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (closeInner) {
            observerMojmir.close();
        }
    }

    Value[][] getConsistentValues() {
        return consistentValues;
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return observerMaps.numberToLabel(number);
    }

    public AutomatonMojmir getMojmir() {
        return observerMojmir;
    }

    @Override
    public String toString() {
        AutomatonExporter exporter = new AutomatonExporterDot();
        exporter.setAutomaton(this);
        return exporter.toString();
    }

    public static void main(String[] args) {
        Options options = UtilOptionsEPMC.newOptions();
        UtilPlugin.preparePlugins(options);
        ContextExpression contextExpression = UtilExpression.newContextExpression(options);
        options.set(OptionsValue.CONTEXT_VALUE, contextValue);
        Expression formula = UtilModelChecker.parse(options, "(a | (b U c))");
        Set<Expression> identifiers = formula.collectIdentifiers();
        int idNr = 0;
        Expression[] expressions = new Expression[identifiers.size()];
        for (Expression identifier : identifiers) {
            expressions[idNr] = identifier;
            identifier.setType(contextValue.getTypeBoolean());
            idNr++;
        }
        ExpressionsUnique expressionsUnique = new ExpressionsUnique(contextExpression, expressions);
        AutomatonSlave observer = new AutomatonSlave(formula, expressionsUnique, false);
        AutomatonExporter exporter = new AutomatonExporterDot();
        exporter.setAutomaton(observer);
        contextExpression.close();
        System.out.println(exporter);
    }
}
