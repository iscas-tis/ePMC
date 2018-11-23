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
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;

import java.util.Map;
import java.util.Set;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonExporter;
import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonMaps;
import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.kretinsky.options.OptionsKretinsky;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;
import epmc.util.StopWatch;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Value;

public final class AutomatonKretinskyProduct implements AutomatonGeneralisedRabin {
    private final static String RUN_PREFIX = "run-";
    public final static String IDENTIFIER = "kretinsky";    
    private final AutomatonMaps observerMaps = new AutomatonMaps();
    private Options options;
    private ContextExpression contextExpression;
    private ModelChecker modelChecker;
    private AutomatonMojmir master;
    private AutomatonSlave[] slaves;
    private AutomatonNumeredInput[] allAutomata;
    private AutomatonKretinskyProductState initState;
    private Expression[] expressions;
    private Expression[] gSubformulae;
    private int succState;
    private int succLabel;
    private boolean disableUnusedSlaves;
    private int[][] acceptances;
    private int[][] acceptancesToSlaveNumber;
    private ExpressionsUnique expressionsUnique;
    private boolean[][][] pairSlaveToStates;
    private final TLongLongMap cache = new TLongLongHashMap(100, 0.5f, -1, -1);
    private final TLongLongMap cacheNoSlaves = new TLongLongHashMap(100, 0.5f, -1, -1);
    private boolean runSlaves = true;

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
    public AutomatonKretinskyProductState numberToState(int number) {
        return (AutomatonKretinskyProductState) observerMaps.numberToState(number);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public ModelChecker getModelChecker() {
        return this.modelChecker;
    }

    public AutomatonKretinskyProduct() {
    }

    public void setExpression(Expression expression, Expression[] expressions) {
        assert expression != null;
        assert expressions != null;
        this.disableUnusedSlaves = options.get(OptionsKretinsky.KRETINSKY_DISABLE_UNUSED_SLAVES);
        expression = UtilExpression.toNegationNormalForm(expression);
        if (this.expressionsUnique == null) {
            this.expressionsUnique = new ExpressionsUnique(contextExpression, expressions);
        }
        Map<Expression, Expression> replacement = expressionsUnique.getReplacement();
        master = new AutomatonMojmir(expression, expressionsUnique, false, false);
        Set<Expression> gSubformulae = collectGSubformulae(expression);
        this.gSubformulae = new Expression[gSubformulae.size()];
        slaves = new AutomatonSlave[gSubformulae.size()];
        allAutomata = new AutomatonNumeredInput[gSubformulae.size() + 1];
        allAutomata[0] = master;
        int slaveNr = 0;
        for (Expression sub : gSubformulae) {
            this.gSubformulae[slaveNr] = sub.replace(replacement);
            slaves[slaveNr] = new AutomatonSlave(sub.getOperand1(), expressionsUnique, false);
            allAutomata[slaveNr + 1] = slaves[slaveNr];
            slaveNr++;
        }
        AutomatonStateUtil[] init = new AutomatonStateUtil[allAutomata.length];
        this.expressions = expressions.clone();

        preprocessSlaves();

        init[0] = master.numberToState(master.getInitState());
        for (int i = 0; i < slaves.length; i++){
            init[i + 1] = slaves[i].numberToState(slaves[i].getInitState());
        }
        disableUnusedSlaves(init);
        initState = makeUnique(new AutomatonKretinskyProductState(this, init));
        prepareAcceptance();
    };

    @Override
    public void setExpression(Expression expression) {
        expression = UtilExpression.toNegationNormalForm(expression);
        Expression[] expressions = getModelChecker().relevantExpressionsArray(expression);
        setExpression(expression, expressions);
    }

    private void preprocessSlaves() {
        AutomatonStateUtil[] slStates = new AutomatonStateUtil[slaves.length];
        for (int i = 0; i < slaves.length; i++) {
            slStates[i] = slaves[i].numberToState(slaves[i].getInitState());
        }
        String preprocessSlaves = options.get(OptionsKretinsky.KRETINSKY_PREPROCESS_SLAVES);
        if (preprocessSlaves.length() >= 5 && preprocessSlaves.substring(0, 4).equals(RUN_PREFIX)) {
            String numStepString = preprocessSlaves.substring(4);
            int numSteps = Integer.parseInt(numStepString);
            for (int step = 0; step < numSteps; step++) {
                for (int i = 0; i < slaves.length; i++) {
                    slaves[i].queryState(0, slStates[i].getNumber());
                    slStates[i] = slaves[i].numberToState(slaves[i].getSuccessorState());
                }
            }
        }
    }

    AutomatonKretinskyProduct(Expression expression, Expression[] expressions)
    {
        assert assertConstructor(expression, expressions);
        setExpression(expression, expressions);
    }

    private static boolean assertConstructor(Expression expression,
            Expression[] expressions) {
        assert expression != null;
        assert expressions != null;
        for (Expression exp : expressions) {
            assert exp != null;
        }
        return true;
    }

    private void disableUnusedSlaves(AutomatonStateUtil[] state) {
        if (!disableUnusedSlaves) {
            return;
        }
        Expression expression = ((AutomatonMojmirState) state[0]).getExpression();
        for (int slaveNr = 0; slaveNr < state.length - 1; slaveNr++) {
            Expression slaveExpr = gSubformulae[slaveNr];
            Expression expressionF = expression.replace(slaveExpr, contextExpression.getFalse());
            Expression expressionT = expression.replace(slaveExpr, contextExpression.getTrue());
            DD ddF = expressionsUnique.formulaToDD(expressionF);
            DD ddT = expressionsUnique.formulaToDD(expressionT);
            if (ddF.equals(ddT)) {
                AutomatonSlaveState slaveState = (AutomatonSlaveState) state[slaveNr + 1];
                state[slaveNr + 1] = slaveState.disable();
            }
            ddF.dispose();
            ddT.dispose();
        }
    }

    @Override
    public int getInitState() {
        return initState.getNumber();
    }

    public void queryState(int modelState, int automatonState)
    {
        if (runSlaves) {
            long cacheKey = (((long) modelState) << 32) | (automatonState);
            long cacheVal = cache.get(cacheKey);
            if (cacheVal == -1) {
                AutomatonKretinskyProductState current = numberToState(automatonState);
                AutomatonStateUtil[] succState = new AutomatonStateUtil[allAutomata.length];
                AutomatonLabelUtil[] succLabel = new AutomatonLabelUtil[allAutomata.length];
                for (int i = 0; i < allAutomata.length; i++) {
                    allAutomata[i].queryState(modelState, current.getStates()[i].getNumber());
                    succState[i] = (AutomatonStateUtil) allAutomata[i].numberToState(allAutomata[i].getSuccessorState());
                    succLabel[i] = (AutomatonLabelUtil) allAutomata[i].numberToLabel(allAutomata[i].getSuccessorLabel());
                }
                disableUnusedSlaves(succState);
                this.succState = makeUnique(new AutomatonKretinskyProductState(this, succState)).getNumber();
                this.succLabel = makeUnique(new AutomatonKretinskyProductLabel(this, current, succLabel)).getNumber();
                cacheVal = (((long) this.succState) << 32) | (this.succLabel);            
                cache.put(cacheKey, cacheVal);
            } else {
                this.succState = (int) (cacheVal >>> 32);
                this.succLabel = (int) (cacheVal & 0xFFFF);
            }
        } else {
            long cacheKey = (((long) modelState) << 32) | (automatonState);
            long cacheVal = cacheNoSlaves.get(cacheKey);
            if (cacheVal == -1) {
                AutomatonKretinskyProductState current = numberToState(automatonState);
                AutomatonStateUtil[] succState = new AutomatonStateUtil[allAutomata.length];
                AutomatonLabelUtil[] succLabel = new AutomatonLabelUtil[allAutomata.length];
                master.queryState(modelState, current.getStates()[0].getNumber());
                succState[0] = (AutomatonStateUtil) allAutomata[0].numberToState(allAutomata[0].getSuccessorState());
                succLabel[0] = (AutomatonLabelUtil) allAutomata[0].numberToLabel(allAutomata[0].getSuccessorLabel());
                for (int i = 1; i < allAutomata.length; i++) {
                    succState[i] = current.getStates()[i];
                    succLabel[i] = (AutomatonLabelUtil) allAutomata[i].numberToLabel(0);
                }
                this.succState = makeUnique(new AutomatonKretinskyProductState(this, succState)).getNumber();
                this.succLabel = makeUnique(new AutomatonKretinskyProductLabel(this, current, succLabel)).getNumber();
                cacheVal = (((long) this.succState) << 32) | (this.succLabel);            
                cacheNoSlaves.put(cacheKey, cacheVal);
            } else {
                this.succState = (int) (cacheVal >>> 32);
                this.succLabel = (int) (cacheVal & 0xFFFF);
            }
        }
    }

    @Override
    public void queryState(Value[] modelState, int automatonState)
    {
        int modelStateNr = expressionsUnique.valueToNumber(modelState);
        queryState(modelStateNr, automatonState);
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
        for (Automaton automaton : allAutomata) {
            automaton.close();
        }
    }

    private Set<Expression> collectGSubformulae(Expression expression) {
        Set<Expression> result = contextExpression.newSet();
        if (expression.isGlobally()) {
            result.add(expression);
        }
        for (Expression child : expression.getChildren()) {
            result.addAll(collectGSubformulae(child));
        }
        return result;
    }

    @Override
    public String toString() {
        AutomatonExporter exporter = new AutomatonExporterDot();
        exporter.setAutomaton(this);
        return exporter.toString();
    }

    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return observerMaps.numberToLabel(number);
    }

    @Override
    public int getNumPairs() {
        return acceptances.length;
    }

    @Override
    public int getNumAccepting(int pair) {
        int[] acceptance = acceptances[pair];
        int result = 0;
        for (int i = 0; i < acceptance.length; i++) {
            if (acceptance[i] != -1) {
                result++;
            }
        }
        return result;
    }

    private void prepareAcceptance() {
        ContextDD contextDD = ContextDD.get();
        int numAcceptancePairs = 1;
        for (int i = 0; i < slaves.length; i++) {
            int numRanksPlusOne = slaves[i].getMojmir().getNumStates() + 1;
            numAcceptancePairs *= numRanksPlusOne;
        }
        acceptances = new int[numAcceptancePairs][];
        acceptancesToSlaveNumber = new int[numAcceptancePairs][];
        TIntList acceptanceToSlaveNumber = new TIntArrayList();

        for (int acceptanceNumber = 0; acceptanceNumber < numAcceptancePairs; acceptanceNumber++) {
            int usedNumber = acceptanceNumber;
            int[] acceptance = new int[slaves.length];
            acceptanceToSlaveNumber.clear();
            for (int slaveNr = 0; slaveNr < slaves.length; slaveNr++) {
                int numRanksPlusOne = slaves[slaveNr].getMojmir().getNumStates() + 1;
                int number = usedNumber % numRanksPlusOne;
                number--;
                if (number != -1) {
                    acceptanceToSlaveNumber.add(slaveNr);
                }
                acceptance[slaveNr] = number;
                usedNumber /= numRanksPlusOne;
            }
            acceptancesToSlaveNumber[acceptanceNumber] = acceptanceToSlaveNumber.toArray();
            acceptances[acceptanceNumber] = acceptance;
        }

        System.out.println(numAcceptancePairs);

        pairSlaveToStates = new boolean[numAcceptancePairs][][];
        for (int acceptanceNumber = 0; acceptanceNumber < numAcceptancePairs; acceptanceNumber++) {
            boolean[][] slaveToState = new boolean[slaves.length][];
            pairSlaveToStates[acceptanceNumber] = slaveToState;
            DD leftSide = contextDD.newConstant(true);
            int[] acceptance = acceptances[acceptanceNumber];
            for (int formulaNr = 0; formulaNr < gSubformulae.length; formulaNr++) {
                if (acceptance[formulaNr] == -1) {
                    continue;
                }
                Expression subformula = gSubformulae[formulaNr];
                expressionsUnique.getExpressionDD(subformula);
                leftSide = leftSide.andWith(expressionsUnique.getExpressionDD(subformula).clone());
            }

            for (int slaveNr = 0; slaveNr < slaves.length; slaveNr++) {
                AutomatonSlave slave = slaves[slaveNr];
                AutomatonMojmir mojmir = slave.getMojmir();
                boolean[] statesBs = new boolean[mojmir.getNumStates()];
                slaveToState[slaveNr] = statesBs;
                for (int state = 0; state < mojmir.getNumStates(); state++) {
                    Expression expression = mojmir.getStateExpression(state);
                    DD expressionDD = expressionsUnique.getExpressionDD(expression);
                    statesBs[state] = leftSide.implies(expressionDD).isTrueWith();
                }
            }
            leftSide.dispose();
        }
    }

    int[] getAcceptance(int number) {
        return acceptances[number];
    }

    public int acceptanceToSlaveNumber(int pair, int number) {
        return acceptancesToSlaveNumber[pair][number];
    }

    public boolean[] getStateAcc(int pair, int number) {
        return pairSlaveToStates[pair][number];
    }

    public static void main(String[] args) {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsEPMC.PLUGIN, "/Users/emhahn/Documents/workspace/iscasmc/plugins/kretinsky");
        UtilPlugin.preparePlugins(options);
        ContextExpression contextExpression = UtilExpression.newContextExpression(options);
        //        options.set(Options.KRETINSKY_GFFG_OPTIMISATION, false);
        //      options.set(Options.KRETINSKY_OPTIMISE_MOJMIR, KretinskyOptimiseMojmir.LANGUAGE);
        //        Expression formula = contextExpression.parse("b & (X(b)) & (G(a & (X(b U c))))");
        //        Expression formula = contextExpression.parse("(a R b)");
        //          Expression formula = contextExpression.parse("(G(G(b)))");
        //        Expression formula = contextExpression.parse("(F(G(X((G(a)) | (X(G(b)))))))");
        //        Expression formula = contextExpression.parse("(F(G(a))) | (G(F(b)))");
        //      Expression formula = contextExpression.parse("((G(F(a))) | (F(G(b)))) & ((G(F(c))) | (F(G(d | (F(G(X(e))))))))");
        //        Expression formula = contextExpression.parse("(X((G(r)) | (r U (r & (s U p))))) U (G(r) | ((r) U (r & s)))");
        //        Expression formula = contextExpression.parse("(F(G(((a) & (X(X(b))) & (G(F(b)))) U (G((X(X(!c))) | (X(X(a & b))))))))");
        //      Expression formula = contextExpression.parse("(G(F(!c | (a & b))))");
        //        Expression formula = contextExpression.parse("(G(F((X(X(X(a)))) & (X(X(X(X(b)))))))) & (G(F(b | (X(c))))) & (G(F(c & (X(X(a))))))");
        //        Expression formula = contextExpression.parse("(G(F(c & (X(X(a)))) & (F(a & (X(b))))))");
        //        Expression formula = contextExpression.parse("(G(F(a))) | (b & (G(F(c))))");
        //        Expression formula = contextExpression.parse("((a) U (b))");

        //        Expression formula = contextExpression.parse("(G(G(G(a))))");
        // TODO ask Jan Kretinsky about simplification steps for this formula
        //      Expression formula = contextExpression.parse("(F(G(a)))");
        //      Expression formula = contextExpression.parse("((G(a)))");


        //        Expression formula = contextExpression.parse("(G(F(a))) | (b & (G(F(c & (X(c))))))");
        //        Expression formula = contextExpression.parse("(G(F(a))) | (b & (G(F(c))))");
        //        Expression formula = contextExpression.parse("(G(F((X(X(X(a)))) & (X(X(X(X(b)))))))) & (G(F((b) | (X(c))))) & (G(F((c) & (X(X(a))))))");
        //        Expression formula = contextExpression.parse("(G((!(q)) | (((!(p)) | ((!(r)) U ((!(r)) & (s) & (!(z)) & (X(((!(r)) & (!(z))) U (t)))))) U ((r) | (G((!(p)) | ((s) & (!(z)) & (X((!(z)) U (t))))))))))");
        //        Expression formula = contextExpression.parse("(G(((p1) & (X(!(p1)))) | (X((p1) U ((p1) & (!(p2)) & (X((p1) & (p2) & ((p1) U ((p1) & (!(p2)) & (X((p1) & (p2))))))))))))");
        Expression formula = UtilModelChecker.parse(options, "(F(G((G(a)) & (G(b)) )))");
        Set<Expression> identifiers = formula.collectIdentifiers();
        int idNr = 0;
        Expression[] expressions = new Expression[identifiers.size()];
        for (Expression identifier : identifiers) {
            expressions[idNr] = identifier;
            identifier.setType(TypeBoolean.get());
            idNr++;
        }
        StopWatch watch = Util.newStopWatch();
        AutomatonKretinskyProduct product = new AutomatonKretinskyProduct(formula, expressions);
        System.out.println("TIME " + watch.getTimeSeconds());
        String productString = product.toString();
        System.out.println("TIME " + watch.getTimeSeconds());
        System.out.println(productString);
        product.close();
        contextExpression.close();
        System.out.println("TIME " + watch.getTimeSeconds());
    }

    public DD getSubformulaDD(int slaveNr) {
        return expressionsUnique.getExpressionDD(gSubformulae[slaveNr]);
    }

    public DD expressionToDD(Expression expression) {
        return expressionsUnique.getExpressionDD(expression);
    }    

    public void setExpressionUnique(ExpressionsUnique expressionsUnique) {
        this.expressionsUnique = expressionsUnique;
    }

    public void setRunSlaves(boolean runSlaves) {
        this.runSlaves = runSlaves;
    }
}
