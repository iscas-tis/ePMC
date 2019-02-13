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

package epmc.dd;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import epmc.error.EPMCException;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.BitSet;
import epmc.util.RecursiveStopWatch;
import epmc.util.Util;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

// TODO add support for JINC as soon as/if ever notified by mail
// TODO documentation
// TODO remove entries from Value table if no longer used

public final class ContextDD implements Closeable {
    private final IdentityHash IDENTITY_HASH = new IdentityHash();

    private final static class IdentityHash implements Hash.Strategy<Object> {
        @Override
        public boolean equals(Object arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int hashCode(Object arg0) {
            return System.identityHashCode(arg0);
        }
        
    }
    
    private static ContextDD contextDD;

    public static ContextDD get() {
        if (contextDD == null) {
            contextDD = new ContextDD();
        }
        return contextDD;
    }

    /** String contain a single space. */
    private final static String SPACE = " ";
    private final class LongTriple {
        private final long long1;
        private final long long2;
        private final long long3;

        LongTriple(long long1, long long2, long long3) {
            this.long1 = long1;
            this.long2 = long2;
            this.long3 = long3;
        }


        @Override
        public int hashCode() {
            int hash = 0;
            hash = (int)(long1^(long1>>>32)) + (hash << 6) + (hash << 16) - hash;
            hash = (int)(long2^(long2>>>32)) + (hash << 6) + (hash << 16) - hash;
            hash = (int)(long3^(long3>>>32)) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }


        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof LongTriple)) {
                return false;
            }
            LongTriple other = (LongTriple) obj;
            if (long1 != other.long1) {
                return false;
            }
            if (long2 != other.long2) {
                return false;
            }
            if (long3 != other.long3) {
                return false;
            }
            return true;
        }
    }

    static final String WRONG_USAGE_NO_DEBUG = "wrong usage of DD package."
            + "Please rerun with \"--" + OptionsDD.DD_DEBUG + " true\" enabled"
            + " to get more information (preferrably on a small"
            + " instance of your model).";
    private final List<VariableDD> variables = new ArrayList<>();

    /** BigInteger with value &quot;2&quot; to avoid repeated rebuilding */
    private final static BigInteger BIG_INTEGER_TWO = new BigInteger("2");
    private final ArrayList<String> variableNames = new ArrayList<>();
    private final Map<LibraryDD,Set<DD>> nodes
    = new Object2ObjectOpenCustomHashMap<LibraryDD,Set<DD>>(IDENTITY_HASH);
    private final Set<LibraryDD> lowLevels
    = new ObjectOpenCustomHashSet<LibraryDD>(IDENTITY_HASH);
    private final Set<LibraryDD> lowLevelsExt = Collections.unmodifiableSet(lowLevels);

    /** whether this context has not yet been shut down */
    private boolean alive = true;

    private final LibraryDD lowLevelBinary;
    private final LibraryDD lowLevelMulti;

    private final Map<LibraryDD,List<DD>> llVariables =
            new Object2ObjectOpenCustomHashMap<>(IDENTITY_HASH);
    private final boolean debugDD;
    private final static boolean PRINT_INVERTER_ARCS = false;
    private final boolean useAndExist;
    private final Map<Walker,Object> walkers = new WeakHashMap<>();
    private boolean allowReorder;
    private final GenericOperations genericApply;
    private final RecursiveStopWatch totalTime = new RecursiveStopWatch();
    private final RecursiveStopWatch convertTime = new RecursiveStopWatch();
    private final Log log;
    private final TypeBoolean typeBoolean = TypeBoolean.get();

    public ContextDD() {
        totalTime.start();
        this.alive = true;
        Options options = Options.get();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        Map<String,Class<? extends LibraryDD>> ddMtLibraryClasses = options.get(OptionsDD.DD_MT_LIBRARY_CLASS);
        assert ddMtLibraryClasses != null;
        this.lowLevelMulti = UtilOptions.getInstance(OptionsDD.DD_MULTI_ENGINE);
        this.lowLevelMulti.setContextDD(this);
        this.log = options.get(OptionsMessages.LOG);

        this.lowLevelBinary = UtilOptions.getInstance(OptionsDD.DD_BINARY_ENGINE);
        assert this.lowLevelBinary != null;
        this.lowLevelBinary.setContextDD(this);
        this.lowLevels.add(lowLevelMulti);
        this.lowLevels.add(lowLevelBinary);
        this.llVariables.put(lowLevelBinary, new ArrayList<DD>());
        this.llVariables.put(lowLevelMulti, new ArrayList<DD>());
        assert assertPutNodes();
        this.useAndExist = options.getBoolean(OptionsDD.DD_AND_EXIST);
        this.debugDD = options.getBoolean(OptionsDD.DD_DEBUG);
        this.genericApply = new GenericOperations(this);
        totalTime.stop();
    }

    private boolean assertPutNodes() {
        this.nodes.put(lowLevelMulti, new ObjectOpenCustomHashSet<DD>(IDENTITY_HASH));
        this.nodes.put(lowLevelBinary, new ObjectOpenCustomHashSet<DD>(IDENTITY_HASH));
        return true;
    }

    public void setAllowReorder(boolean allowReorder) {
        totalTime.start();
        this.allowReorder = allowReorder;
        totalTime.stop();
    }

    public void reorder() {
        assert invalidateWalkers();
        totalTime.start();
        totalTime.stop();
    }

    public int numVariables() {
        totalTime.start();
        int numVariables = llVariables.get(lowLevelBinary).size();
        totalTime.stop();
        return numVariables;
    }

    public VariableDD newVariable(String name, Type type, int copies)
    {
        assert name != null;
        assert type != null;
        assert copies > 0;
        totalTime.start();
        VariableDD variable = new VariableDDImpl(this, copies, type, name);
        variables.add(variable);
        totalTime.stop();
        return variable;
    }

    public VariableDD newVariable(String name, Type type,
            List<List<DD>> ddActionBits) {
        int copies = ddActionBits.size();
        return newVariable(name, type, copies, ddActionBits);
    }

    public VariableDD newVariable(String name, Type type, int copies, List<List<DD>> ddVariables)
    {
        assert name != null;
        assert type != null;
        assert copies > 0;
        assert ddVariables != null;
        assert ddVariables.size() == copies;
        int ddVarLengths = ddVariables.get(0).size();
        for (List<DD> vars : ddVariables) {
            assert vars.size() == ddVarLengths;
        }
        totalTime.start();
        VariableDD variable = new VariableDDImpl(this, copies, type, name, ddVariables);
        variables.add(variable);
        totalTime.stop();
        return variable;
    }

    public VariableDD newBoolean(String name, int copies) {
        assert name != null;
        assert copies > 0;
        totalTime.start();
        VariableDD result = newVariable(name, typeBoolean, copies);
        totalTime.stop();
        return result;
    }


    public VariableDD newInteger(String name, int copies, int lower, int upper) {
        assert name != null;
        assert copies > 0;
        assert lower <= upper;
        totalTime.start();
        Type type = TypeInteger.get(lower, upper);
        VariableDD result = newVariable(name, type, copies);
        totalTime.stop();
        return result;
    }

    public void setVariableName(int number, String name) {
        assert alive;
        assert number >= 0;
        totalTime.start();
        variableNames.ensureCapacity(number);
        while (variableNames.size() <= number) {
            variableNames.add(null);
        }
        variableNames.set(number, name);
        totalTime.stop();
    }

    public DD applyWith(Operator operator, DD... operands) {
        assert operator != null;
        assert assertValidDDArray(operands);
        DD result = apply(operator, operands);
        for (DD op : operands) {
            op.dispose();
        }
        return result;
    }

    public DD applyBooleanWith(Operator operator, DD... operands) {
        assert operator != null;
        assert assertValidDDArray(operands);
        DD result = applyBoolean(operator, operands);
        for (DD op : operands) {
            op.dispose();
        }
        return result;
    }

    public DD apply(Operator identifier, DD... ops) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert identifier != null;
        assert assertValidDDArray(ops);
        assert ops.length > 0;
        assert assertOperatorCompatible(identifier, ops);
        totalTime.start();
        LibraryDD lowLevel = ops[0].getLowLevel();
        boolean importToMtbdd = mustImportToMtbdd(identifier, ops);
        Type type = computeType(identifier, ops);
        if (importToMtbdd) {
            lowLevel = lowLevelMulti;
            ops[0] = importDD(ops[0], lowLevelMulti);
        }
        int opAssNr = 0;
        for (DD op : ops) {
            assert op.getLowLevel() == lowLevel : "invalid argument no " + opAssNr;
            opAssNr++;
        }

        long result;
        long[] opsLong = new long[ops.length];
        for (int opNr = 0; opNr < ops.length; opNr++) {
            opsLong[opNr] = ops[opNr].uniqueId();
        }
        Type types[] = new Type[ops.length];
        for (int i = 0; i < ops.length; i++) {
            types[i] = ops[i].getType();
        }
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, types);
        if (ops.length == 0) {
            Type resultType = evaluator.resultType();
            Value resultValue = resultType.newValue();
            evaluator.apply(resultValue, new Value[0]);
            result = lowLevel.newConstant(resultValue);
        } else if (lowLevel.canApply(identifier, type, opsLong)) {
            result = lowLevel.apply(identifier, type, opsLong);
        } else {
            long[] operands = new long[ops.length];
            for (int index = 0; index < ops.length; index++) {
                operands[index] = ops[index].uniqueId();
            }
            result = genericApply(evaluator, identifier, type, lowLevel, operands);
        }
        DD resultDD = toDD(result, lowLevel);

        if (importToBinary(identifier, resultDD, ops)) {
            DD importedResult = importDD(resultDD, lowLevelBinary);
            resultDD.dispose();
            if (importToMtbdd) {
                ops[0].dispose();
            }
            totalTime.stop();
            assert checkDD();
            return importedResult;
        } else {
            if (importToMtbdd) {
                ops[0].dispose();
            }
            totalTime.stop();
            assert checkDD();
            return resultDD;
        }
    }

    public DD applyBoolean(Operator identifier, DD... ops) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert identifier != null;
        assert assertValidDDArray(ops);
        assert ops.length > 0;
        assert assertOperatorCompatible(identifier, ops);
        totalTime.start();
        long result;
        long[] opsLong = new long[ops.length];
        for (int opNr = 0; opNr < ops.length; opNr++) {
            opsLong[opNr] = ops[opNr].uniqueId();
        }
        result = lowLevelBinary.apply(identifier, typeBoolean, opsLong);
        DD resultDD = toDD(result, lowLevelBinary);
        totalTime.stop();
        assert checkDD();
        return resultDD;
    }

    private boolean checkDD() {
        if (!debugDD) {
            return true;
        }
        for (LibraryDD ll : lowLevels) {
            if (!ll.checkConsistent()) {
                for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                    System.out.println(elem);
                }
                System.exit(1);
            }
        }
        return true;
    }

    private Type computeType(Operator identifier, DD... ops) {
        assert identifier != null;
        assert ops != null;
        Type[] types = new Type[ops.length];
        for (int index = 0; index < ops.length; index++) {
            types[index] = ops[index].getType();
        }
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, types);
        Type type = evaluator.resultType();
        return type;
    }


    public DD apply(Operator identifier, List<DD> ops) {
        totalTime.start();
        DD result = apply(identifier, ops.toArray(new DD[0]));
        totalTime.stop();
        return result;
    }

    private long genericApply(OperatorEvaluator operator, Operator identifier, Type type, LibraryDD lowLevel, long... ops)
    {
        return this.genericApply.apply(operator, identifier, type, lowLevel, ops);
    }

    private boolean assertValidDDArray(DD... dds) {
        assert dds != null;
        int index = 0;
        for (DD op : dds) {
            assert assertValidDD(op) : index;
            index++;
        }
        return true;
    }

    private boolean assertValidDDIterable(Iterable<DD> dds) {
        assert dds != null;
        int index = 0;
        for (DD op : dds) {
            assert assertValidDD(op) : index;
            index++;
        }
        return true;
    }

    private boolean assertOperatorCompatible(Operator identifier, Type... types)
    {
        return true;
    }

    private boolean assertOperatorCompatible(Operator identifier, DD... ops)
    {
        assert assertValidDDArray(ops);
        Type[] types = new Type[ops.length];
        for (int index = 0; index < ops.length; index++) {
            types[index] = ops[index].getType();
        }
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, types);
        assert evaluator != null : identifier + " " + Arrays.toString(types);
        Type resultType = evaluator.resultType();
        assert resultType != null : identifier + SPACE + Arrays.toString(types);
        return true;
    }

    private boolean importToBinary(Operator operator, DD resultDD, DD... ops) {
        assert operator != null;
        assert assertValidDD(resultDD);
        assert assertValidDDArray(ops);
        if (lowLevelMulti == lowLevelBinary) {
            return false;
        }
        if (operator.equals(OperatorGe.GE)
                || operator.equals(OperatorGt.GT)
                || operator.equals(OperatorLe.LE)
                || operator.equals(OperatorLt.LT)) {
            return true;        	
        } else if (operator.equals(OperatorEq.EQ)
                || operator.equals(OperatorNe.NE)) {
            return ops[0].getLowLevel() == lowLevelMulti;
        } else {
            return false;
        }
    }

    private boolean mustImportToMtbdd(Operator operator, DD... ops) {
        assert operator != null;
        assert assertValidDDArray(ops);
        return operator.equals(OperatorIte.ITE)
                && ops[0].getLowLevel() == lowLevelBinary
                && ops[1].getLowLevel() == lowLevelMulti;
    }

    private DD newConstant(Value value, LibraryDD lowLevel) {
        assert alive();
        assert value != null;
        assert invalidateWalkersIfReorder();
        return toDD(lowLevel.newConstant(value), lowLevel);
    }


    public DD newConstant(Value value) {
        assert alive();
        assert value != null;
        totalTime.start();
        DD result;
        if (ValueBoolean.is(value)) {
            result = newConstant(value, lowLevelBinary);
        } else {
            result = newConstant(value, lowLevelMulti);
        }
        totalTime.stop();
        return result;
    }


    public DD newConstant(int value) {
        assert alive();
        totalTime.start();
        DD result = newConstant(UtilValue.<ValueInteger,TypeInteger>newValue(TypeInteger.get(), value));
        totalTime.stop();
        return result;
    }


    public final DD newConstant(boolean value) {
        assert alive();
        totalTime.start();
        DD result = newConstant(typeBoolean.newValue(value));
        totalTime.stop();
        return result;
    }

    public DD newConstant(Enum<?> constant) {
        assert alive();
        assert constant != null;
        totalTime.start();
        TypeEnum typeEnum = TypeEnum.get(constant.getDeclaringClass());
        DD result = newConstant(typeEnum.newValue(constant));
        totalTime.stop();
        return result;
    }

    public DD variable(int variableNr) {
        assert alive();
        totalTime.start();
        DD result = variable(variableNr, lowLevelBinary);
        totalTime.stop();
        return result;
    }

    public DD variable(int variableNr, LibraryDD lowLevel) {
        assert alive();
        assert variableNr >= 0 : "variable = " + variableNr;
        assert variableNr < numVariables()
        : "variableNr = " + variableNr + " !< numVariables() = "
        + numVariables();
        totalTime.start();
        DD result = llVariables.get(lowLevel).get(variableNr);
        totalTime.stop();
        return result;
    }

    public DD newVariable() {
        assert alive();
        totalTime.start();
        long varIdMulti = lowLevelMulti.newVariable();
        assert lowLevelMulti.variable(varIdMulti) == llVariables.get(lowLevelMulti).size();
        DD variableMulti = toDD(varIdMulti, lowLevelMulti);
        llVariables.get(lowLevelMulti).add(variableMulti);
        long varIdBinary = lowLevelBinary.newVariable();
        assert lowLevelBinary.variable(varIdBinary) == llVariables.get(lowLevelBinary).size();
        DD variableBinary = toDD(varIdBinary, lowLevelBinary);
        llVariables.get(lowLevelBinary).add(variableBinary);
        totalTime.stop();
        return variableBinary;
    }

    @Override
    public void close() {
        if (!alive) {
            return;
        }
        totalTime.start();
        for (VariableDD variable : variables) {
            variable.close();
        }
        for (DD var : llVariables.get(lowLevelMulti)) {
            var.dispose();
        }
        if (Options.get().getBoolean(OptionsDD.DD_LEAK_CHECK)) {
            assert assertNodesLeakCheck();
        }
        List<DD> remaining = new ArrayList<>();
        assert assertCleanupNodes(remaining);
        for (DD dd : remaining) {
            if (dd.internalAlive()) {
                dd.dispose();
            }
        }

        lowLevelMulti.close();
        lowLevelBinary.close();
        alive = false;
        totalTime.stop();
        assert totalTime.getRunning() == 0 : totalTime.getRunning();
        assert convertTime.getRunning() == 0 : convertTime.getRunning();
        log.send(MessagesDD.DD_CONVERSION_TIME, convertTime.getTimeSeconds());
        log.send(MessagesDD.DD_TOTAL_TIME, totalTime.getTimeSeconds());
    }


    private boolean assertCleanupNodes(List<DD> remaining) {
        for (Set<DD> set : nodes.values()) {
            remaining.addAll(set);
        }
        return true;
    }

    private boolean assertNodesLeakCheck() {
        if (debugDD) {
            for (Set<DD> set : nodes.values()) {
                for (DD dd : set) {
                    assert !dd.internalAlive() : "DD not freed. Created at\n"
                            + dd.buildCreateTraceString();
                }
            }
        } else {
            for (Set<DD> set : nodes.values()) {
                assert set.isEmpty() : WRONG_USAGE_NO_DEBUG;
            }
        }
        return true;
    }

    public boolean isLeaf(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        boolean result = dd.getLowLevel().isLeaf(dd.uniqueId());
        totalTime.stop();
        return result;
    }


    public Value getValue(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        assert isLeaf(dd);
        totalTime.start();
        Value result = dd.getLowLevel().value(dd.uniqueId());
        totalTime.stop();
        return result;
    }


    public int variable(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        int result = dd.getLowLevel().variable(dd.uniqueId());
        totalTime.stop();
        return result;
    }


    public IntOpenHashSet support(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        IntOpenHashSet set = new IntOpenHashSet();
        LongOpenHashSet seen = new LongOpenHashSet();
        Walker walker = dd.walker();
        support(walker, set, seen);
        totalTime.stop();
        return set;
    }


    public Set<VariableDD> highLevelSupport(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        Set<VariableDD> result = new ObjectOpenHashSet<>();
        totalTime.start();
        IntOpenHashSet llSupport = support(dd);
        for (VariableDD variableDD : variables) {
            for (DD ddVar : variableDD.getDDVariables()) {
                if (llSupport.contains(ddVar.variable())) {
                    result.add(variableDD);
                    break;
                }
            }
        }
        totalTime.stop();
        return result;
    }

    private void support(Walker walker, IntOpenHashSet set, LongOpenHashSet seen) {
        assert walker != null;
        assert set != null;
        assert seen != null;
        if (seen.contains(walker.uniqueId()) || walker.isLeaf()) {
            return;
        }
        seen.add(walker.uniqueId());
        set.add(walker.variable());
        walker.low();
        support(walker, set, seen);
        walker.back();
        walker.high();
        support(walker, set, seen);
        walker.back();
    }

    private void toStringEnumNodes(Walker walker, Long2LongOpenHashMap numeration) {
        if (PRINT_INVERTER_ARCS) {
            walker.regular();
        }
        if (numeration.containsKey(walker.uniqueId())) {
            return;
        }
        numeration.put(walker.uniqueId(), numeration.size());
        if (walker.isLeaf()) {
            return;
        } else {
            walker.low();
            toStringEnumNodes(walker, numeration);
            walker.back();
            walker.high();
            toStringEnumNodes(walker, numeration);
            walker.back();
        }
    }

    private void toStringNodesRecurse(Walker walker, LongOpenHashSet seen, StringBuilder builder, Long2LongOpenHashMap nodeMap) {
        if (PRINT_INVERTER_ARCS) {
            walker.regular();
        }
        if (seen.contains(walker.uniqueId())) {
            return;
        }
        seen.add(walker.uniqueId());
        if (walker.isLeaf()) {
            builder.append("  node" + nodeMap.get(walker.uniqueId()));
            builder.append(" [label=\"" + walker.value() + ": " + walker.value().getType() + "\",");
            builder.append(" shape=box");
            builder.append("];\n");
            return;
        } else {
            builder.append("  node" + nodeMap.get(walker.uniqueId()));
            builder.append(" [label=\"" + walker.variable() + "\",");
            builder.append(" shape=circle");
            builder.append("];\n");
            walker.low();
            toStringNodesRecurse(walker, seen, builder, nodeMap);
            walker.back();
            walker.high();
            toStringNodesRecurse(walker, seen, builder, nodeMap);
            walker.back();
        }
    }


    public String toString(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        LongOpenHashSet seen = new LongOpenHashSet();
        boolean complement = dd.walker(!PRINT_INVERTER_ARCS).isComplement();
        Walker walker = dd.walker(false);
        if (complement) {
            builder.append("  nodeinit [style=invisible]\n");
        }
        Long2LongOpenHashMap nodeMap = new Long2LongOpenHashMap();
        toStringEnumNodes(walker, nodeMap);
        toStringNodesRecurse(walker, seen, builder, nodeMap);
        builder.append("\n");
        seen.clear();

        walker = dd.walker(!PRINT_INVERTER_ARCS);
        walker.regular();
        if (complement) {
            builder.append("  nodeinit -> node" + nodeMap.get(walker.uniqueId()));
            builder.append("  [style=invisible,arrowhead=dot];\n");
        }
        toStringEdgesRecurse(walker, seen, builder, nodeMap);
        builder.append("\n");

        walker = dd.walker();
        walker.regular();
        Int2ObjectOpenHashMap<LongOpenHashSet> map = new Int2ObjectOpenHashMap<>();
        seen.clear();
        toStringRanks(walker, seen, map, builder);
        IntIterator mapIter = map.keySet().iterator();
        while (mapIter.hasNext()) {
            int number = mapIter.nextInt();
            if (number >= 0) {
                builder.append("  var" + number + " [label=\"" );
                builder.append(variableNames.get(number) + "\"");
                builder.append(" shape=box color=white fillcolor=white");
                builder.append("];\n");
            }
        }
        builder.append("\n");
        int[] sameRank = map.keySet().toIntArray();
        Arrays.sort(sameRank);
        for (int number : sameRank) {
            LongOpenHashSet varNodesSet = map.get(number);
            builder.append("  {rank=same; ");
            LongIterator varIter = varNodesSet.iterator();
            long[] nodes = new long[varNodesSet.size()];

            int index = 0;
            while (varIter.hasNext()) {
                nodes[index] = nodeMap.get(varIter.nextLong());
                index++;
            }
            Arrays.sort(nodes);

            for (long varNode : nodes) {
                builder.append("node" + varNode + " ");                
            }
            if (number >= 0) {
                builder.append("var" + number);
            }
            builder.append("}\n");
        }

        builder.append("}\n");
        String result = builder.toString();
        totalTime.stop();
        return result;
    }

    private void toStringRanks(Walker walker, LongOpenHashSet seen,
            Int2ObjectOpenHashMap<LongOpenHashSet> map,
            StringBuilder builder) {
        if (PRINT_INVERTER_ARCS) {
            walker.regular();
        }
        if (seen.contains(walker.uniqueId())) {
            return;
        }
        seen.add(walker.uniqueId());
        if (walker.isLeaf()) {
            if (!map.containsKey(-1)) {
                map.put(-1, new LongOpenHashSet());
            }
            LongOpenHashSet set = map.get(-1);
            set.add(walker.uniqueId());
        } else {
            if (!map.containsKey(walker.variable())) {
                map.put(walker.variable(), new LongOpenHashSet());
            }
            LongOpenHashSet set = map.get(walker.variable());
            set.add(walker.uniqueId());            
            walker.low();
            toStringRanks(walker, seen, map, builder);
            walker.back();
            walker.high();
            toStringRanks(walker, seen, map, builder);
            walker.back();
        }
    }

    private void toStringEdgesRecurse(Walker dd, LongOpenHashSet seen, StringBuilder builder, Long2LongOpenHashMap nodeMap) {
        if (seen.contains(dd.uniqueId())) {
            return;
        }
        if (dd.isLeaf()) {
            return;
        }
        seen.add(dd.uniqueId());
        dd.low();
        dd.back();

        long uid = dd.uniqueId();
        dd.low();
        String arrow = dd.isComplement() ? "dot" : "none";
        dd.regular();
        toStringEdgesRecurse(dd, seen, builder, nodeMap);
        long luid = dd.uniqueId();
        dd.back();
        builder.append("  node" + nodeMap.get(uid) + " -> node" + nodeMap.get(luid)
        + " [style=dashed,arrowhead=" + arrow + "];\n");
        dd.high();
        arrow = dd.isComplement() ? "dot" : "none";
        dd.regular();
        long huid = dd.uniqueId();
        toStringEdgesRecurse(dd, seen, builder, nodeMap);
        dd.back();
        builder.append("  node" + nodeMap.get(uid) + " -> node" + nodeMap.get(huid)
        + " [style=solid,arrowhead=" + arrow + "];\n");
    }


    public DD permute(DD dd, Permutation permutation)
    {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidPermutation(permutation);
        assert invalidateWalkersIfReorder();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        DD result = toDD(lowLevel.permute(dd.uniqueId(),
                permutation.getLowLevel(dd.getLowLevel())),
                lowLevel);
        totalTime.stop();
        return result;
    }

    private boolean assertValidPermutation(Permutation permutation) {
        assert permutation != null;
        assert permutation.getContextDD() == this;
        return true;
    }


    public boolean alive() {
        return alive;
    }

    public Long2ObjectOpenHashMap<BigInteger> countSatMap(DD dd, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert cube.assertCube();
        totalTime.start();
        Walker ddWalker = dd.walker();
        Walker cubeWalker = cube.walker();
        Long2ObjectOpenHashMap<BigInteger> cache = new Long2ObjectOpenHashMap<>();
        BigInteger varValue = computeInitialVarValue(cube);
        countSat(ddWalker, cubeWalker, cache, varValue);
        totalTime.stop();
        return cache;
    }

    public BigInteger countSat(DD dd, DD variablesCube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(variablesCube);
        assert assertCube(variablesCube);
        assert assertSupport(dd, variablesCube);
        totalTime.start();
        Walker ddWalker = dd.walker();
        Walker cubeWalker = variablesCube.walker();
        Long2ObjectOpenHashMap<BigInteger> cache = new Long2ObjectOpenHashMap<>();
        BigInteger varValue = computeInitialVarValue(variablesCube);
        BigInteger result = countSat(ddWalker, cubeWalker, cache, varValue);
        totalTime.stop();
        return result;
    }

    private boolean assertSupport(DD dd, DD support) {
        assert assertValidDD(dd);
        assert assertValidDD(support);
        assert assertCube(support);
        IntOpenHashSet ddSupport = dd.support();
        IntOpenHashSet supportSupport = support.support();
        IntIterator iter = ddSupport.iterator();
        while (iter.hasNext()) {
            int var = iter.nextInt();
            assert var >= 0;
            assert var < variableNames.size();
            assert supportSupport.contains(var) : variableNames.get(var);
        }
        return true;
    }

    private BigInteger computeInitialVarValue(DD variablesCube) {
        assert alive();
        assert assertValidDD(variablesCube);
        Walker walker = variablesCube.walker();
        BigInteger result = BigInteger.ONE;
        while (!walker.isLeaf()) {
            result = result.multiply(BIG_INTEGER_TWO);
            walker.high();
        }

        return result;
    }

    private int cubeSize(Walker cube) {
        assert cube != null;
        int numVars = 0;
        assert !cube.isLeaf() || ValueBoolean.isTrue(cube.value());
        while (!cube.isLeaf()) {
            numVars++;
            cube.low();
            assert cube.isLeaf();
            assert ValueBoolean.isFalse(cube.value());
            cube.back();
            cube.high();
        }
        assert cube.isLeaf();
        assert ValueBoolean.isTrue(cube.value());
        for (int var = 0; var < numVars; var++) {
            cube.back();
        }
        return numVars;
    }

    private boolean assertCube(Walker cube) {
        assert cube != null;
        cubeSize(cube);
        return true;
    }


    public boolean assertCube(DD dd) {
        return assertCube(dd.walker());
    }

    private BigInteger countSat(Walker dd, Walker cube,
            Long2ObjectOpenHashMap<BigInteger> cache, BigInteger varValue) {
        BigInteger result;
        if (cube.isLeaf()) {
            assert dd.isLeaf();
            if (ValueBoolean.as(dd.value()).getBoolean()) {
                result = BigInteger.ONE;
            } else {
                result = BigInteger.ZERO;
            }
        } else if (dd.isLeaf() || cube.variable() != dd.variable()) {
            cube.high();
            BigInteger inner = countSat(dd, cube, cache, varValue.divide(BIG_INTEGER_TWO));
            cube.back();
            result = inner.multiply(BIG_INTEGER_TWO);
        } else if (cache.containsKey(dd.uniqueId())) {
            result = cache.get(dd.uniqueId());
        } else {
            dd.low();
            cube.high();
            BigInteger low = countSat(dd, cube, cache, varValue.divide(BIG_INTEGER_TWO));
            dd.back();
            dd.high();
            BigInteger high = countSat(dd, cube, cache, varValue.divide(BIG_INTEGER_TWO));
            dd.back();
            cube.back();
            result = low.add(high);
            cache.put(dd.uniqueId(), result);
        }
        return result;
    }

    private DD importDD(DD dd, int[] variablesMap, LibraryDD lowLevel)
    {
        assert alive();
        assert assertValidDD(dd);
        Walker walker = dd.walker();
        Long2ObjectOpenHashMap<DD> nodeMap = new Long2ObjectOpenHashMap<>();
        DD result = importDD(walker, variablesMap, nodeMap, lowLevel);
        for (DD entry : nodeMap.values()) {
            if (entry != result) {
                entry.dispose();
            }
        }

        return result;
    }

    private DD importDD(Walker walker, int[] variablesMap,
            Long2ObjectOpenHashMap<DD> nodeMap, LibraryDD lowLevel)
    {
        DD result;
        if (nodeMap.containsKey(walker.uniqueId())) {
            result = nodeMap.get(walker.uniqueId());
        } else if (walker.isLeaf()) {
            result = newConstant(walker.value(), lowLevel);
            nodeMap.put(walker.uniqueId(), result);
        } else {
            int newVariable;
            if (variablesMap == null) {
                newVariable = walker.variable();
            } else {
                newVariable = variablesMap[walker.variable()];
            }
            DD variable = variable(newVariable, lowLevel);
            walker.high();
            DD high = importDD(walker, variablesMap, nodeMap, lowLevel);
            walker.back();
            walker.low();
            DD low = importDD(walker, variablesMap, nodeMap, lowLevel);
            walker.back();
            result = variable.ite(high, low);
            nodeMap.put(walker.uniqueId(), result);
        }
        return result;
    }

    private DD importDD(DD dd, LibraryDD lowLevel) {
        convertTime.start();
        boolean reorderStatus = allowReorder;
        setAllowReorder(false);
        DD result = importDD(dd, null, lowLevel);
        setAllowReorder(reorderStatus);
        convertTime.stop();
        return result;
    }

    public BigInteger countNodes(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        LongOpenHashSet seen = new LongOpenHashSet();
        Walker walker = dd.walker();
        BigInteger result = countNodes(walker, seen);
        totalTime.stop();
        return result;
    }

    private BigInteger countNodes(Walker walker, LongOpenHashSet seen) {
        if (seen.contains(walker.uniqueId())) {
            return BigInteger.ZERO;
        }
        seen.add(walker.uniqueId());

        if (walker.isLeaf()) {
            return BigInteger.ONE;
        } else {
            walker.high();
            BigInteger result = BigInteger.ONE;
            result = result.add(countNodes(walker, seen));
            walker.back();
            walker.low();
            result = result.add(countNodes(walker, seen));
            walker.back();
            return result;
        }
    }

    private DD toDD(long uniqueId, LibraryDD lowLevel) {
        DD result = new DD(lowLevel, uniqueId);
        assert assertAddDDToNodes(lowLevel, result);
        return result;
    }

    private boolean assertAddDDToNodes(LibraryDD lowLevel, DD result) {
        nodes.get(lowLevel).add(result);
        return true;
    }
    
    public DD findSat(DD dd, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.is(dd.getType());
        assert cube.assertCube();
        totalTime.start();
        DD result;
        if (dd.isLeaf()) {
            if (ValueBoolean.isFalse(dd.value())) {
                result = newConstant(false);
            } else {
                result = cube.clone();
            }
        } else {
            Walker ddWalker = dd.walker();
            Long2ByteOpenHashMap map = new Long2ByteOpenHashMap();
            findSat(ddWalker, map);
            Walker cubeWalker = cube.walker();
            result = newConstant(true);
            while (!cubeWalker.isLeaf()) {
                int variableNr = cubeWalker.variable();
                if (map.containsKey(variableNr)) {
                    DD variableDD = variable(variableNr);
                    if (map.get(variableNr) == 0) {
                        variableDD = variableDD.not();
                    } else {
                        variableDD = variableDD.clone();
                    }
                    result = result.andWith(variableDD);
                } else {
                    DD resultOld = result;
                    DD variableNot = variable(variableNr).not();
                    result = result.and(variableNot);
                    variableNot.dispose();
                    resultOld.dispose();
                }
                cubeWalker.high();
            }
        }
        totalTime.stop();
        return result;
    }

    private void findSat(Walker walker, Long2ByteOpenHashMap map) {
        long variable = walker.variable();
        walker.low();
        boolean found;
        if (walker.isLeaf()) {
            if (ValueBoolean.isTrue(walker.value())) {
                map.put(variable, (byte) 0);
                found = true;
            } else {
                found = false;
            }
        } else {
            map.put(variable, (byte) 0);
            found = true;
            findSat(walker, map);
        }
        walker.back();
        if (!found) {
            map.put(variable, (byte) 1);
            walker.high();
            if (!walker.isLeaf()) {
                findSat(walker, map);                
            } else {
                assert walker.isTrue();
            }
            walker.back();
        }
    }

    public DD abstractExist(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert cube.assertCube();
        totalTime.start();
        // TODO should check for being binary, but need to change one use case first
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        LibraryDD lowLevel = dd.getLowLevel();
        DD result = toDD(lowLevel.abstractExist(dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD abstractForall(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.is(dd.getType());
        assert cube.assertCube();
        totalTime.start();
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        LibraryDD lowLevel = dd.getLowLevel();
        DD result = toDD(lowLevel.abstractForall(dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD abstractSum(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(OperatorAdd.ADD, dd, dd);
        assert cube.assertCube();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        DD result = toDD(lowLevel.abstractSum(dd.getType(), dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD abstractProduct(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(OperatorMultiply.MULTIPLY, dd, dd);
        assert cube.assertCube();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        DD result = toDD(lowLevel.abstractProduct(dd.getType(), dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD abstractMax(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(OperatorMax.MAX, dd, dd);
        assert cube.assertCube();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        DD result = toDD(lowLevel.abstractMax(dd.getType(), dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD abstractMin(DD dd, DD cube) {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(OperatorMax.MAX, dd, dd);
        assert cube.assertCube();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        DD result = toDD(lowLevel.abstractMin(dd.getType(), dd.uniqueId(), convertCube.uniqueId()), lowLevel);
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD toMT(DD dd, Value forTrue, Value forFalse) {
        assert checkDD();
        assert alive();
        assert assertValidDD(dd);
        assert TypeBoolean.is(dd.getType());
        assert forTrue != null;
        assert forFalse != null;
        totalTime.start();
        DD one = newConstant(forTrue);
        DD zero = newConstant(forFalse);
        DD result = apply(OperatorIte.ITE, dd, one, zero);
        zero.dispose();
        one.dispose();
        totalTime.stop();
        assert checkDD();
        return result;
    }

    public DD toMT(DD dd, int forTrue, int forFalse) {
        assert checkDD();
        Value forTrueValue = UtilValue.newValue(TypeInteger.get(), forTrue);
        Value forFalseValue = UtilValue.newValue(TypeInteger.get(), forFalse);
        assert checkDD();
        return toMT(dd, forTrueValue, forFalseValue);
    }

    public DD toInt(DD dd) {
        assert checkDD();
        return toMT(dd, 1, 0);
    }

    public Permutation newPermutation(int[] first, int[] second) {
        assert alive();
        assert first != null;
        assert second != null;
        assert first.length == second.length;
        totalTime.start();
        IntOpenHashSet seen = new IntOpenHashSet();
        for (int entryNr = 0; entryNr < first.length; entryNr++) {
            assert !seen.contains(first[entryNr]);
            seen.add(first[entryNr]);
        }
        for (int entryNr = 0; entryNr < second.length; entryNr++) {
            assert !seen.contains(second[entryNr]);
            seen.add(second[entryNr]);
        }
        int[] permuteArray = new int[numVariables()];
        for (int entryNr = 0; entryNr < permuteArray.length; entryNr++) {
            permuteArray[entryNr] = entryNr;
        }
        for (int entryNr = 0; entryNr < first.length; entryNr++) {
            permuteArray[first[entryNr]] = second[entryNr];
            permuteArray[second[entryNr]] = first[entryNr];
        }
        totalTime.stop();
        return newPermutation(permuteArray);
    }

    public Permutation newPermutationListInteger(List<Integer> first, List<Integer> second) {
        assert alive();
        assert first != null;
        assert second != null;
        assert first.size() == second.size();
        totalTime.start();
        int[] firstInteger = new int[first.size()];
        int[] secondInteger = new int[second.size()];
        int entryNr = 0;
        Iterator<Integer> firstIterator = first.iterator();
        Iterator<Integer> secondIterator = second.iterator();
        while (firstIterator.hasNext()) {
            firstInteger[entryNr] = firstIterator.next();
            secondInteger[entryNr] = secondIterator.next();
            entryNr++;
        }

        Permutation result = newPermutation(firstInteger, secondInteger);
        totalTime.stop();
        return result;
    }

    public Permutation newPermutationListDD(List<DD> first, List<DD> second) {
        assert alive();
        assert first != null;
        assert second != null;
        assert first.size() == second.size();
        totalTime.start();
        int[] firstInteger = new int[first.size()];
        int[] secondInteger = new int[second.size()];
        int entryNr = 0;
        Iterator<DD> firstIterator = first.iterator();
        Iterator<DD> secondIterator = second.iterator();
        while (firstIterator.hasNext()) {
            DD firstVar = firstIterator.next();
            DD secondVar = secondIterator.next();
            assert !firstVar.isLeaf();
            assert !secondVar.isLeaf();
            firstInteger[entryNr] = firstVar.variable();
            secondInteger[entryNr] = secondVar.variable();
            entryNr++;
        }
        Permutation result = newPermutation(firstInteger, secondInteger);
        totalTime.stop();
        return result;
    }

    public Permutation newPermutationCube(DD cube1, DD cube2) {
        assert alive();
        assert cube1 != null;
        assert cube2 != null;
        totalTime.start();
        Walker w1 = cube1.walker();
        Walker w2 = cube2.walker();
        int cubeSize1 = cubeSize(w1);
        int cubeSize2 = cubeSize(w2);
        assert cubeSize1 == cubeSize2;
        int[] firstInteger = new int[cubeSize1];
        int[] secondInteger = new int[cubeSize2];

        int entryNr = 0;
        while(!w1.isLeaf()) {
            firstInteger[entryNr] = w1.variable();
            secondInteger[entryNr] = w2.variable();
            w1.high();
            w2.high();
            entryNr++;
        }

        Permutation result = newPermutation(firstInteger, secondInteger);
        totalTime.stop();
        return result;
    }

    public DD permute(DD dd, int[] permutationIntArr) {
        assert alive();
        totalTime.start();
        Permutation permutation = newPermutation(permutationIntArr);
        DD result = permute(dd, permutation);
        totalTime.stop();
        return result;
    }

    public boolean isComplement(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        boolean result = lowLevelMulti.isComplement(dd.uniqueId());
        totalTime.stop();
        return result;
    }

    public DD listToCube(Iterable<DD> vars) {
        assert alive();
        assert assertValidDDIterable(vars);
        totalTime.start();
        DD cube = newConstant(true);
        for (DD var : vars) {
            DD cubeOld = cube;
            cube = cube.and(var);
            cubeOld.dispose();
        }
        totalTime.stop();
        return cube;
    }

    public List<DD> cubeToList(DD cube) {
        assert alive();
        assert assertCube(cube);
        totalTime.start();
        List<DD> result = new ArrayList<>();
        Walker walker = cube.walker();
        while (!walker.isTrue()) {
            result.add(variable(walker.variable()));
            walker.high();
        }
        totalTime.stop();
        return result;
    }

    public List<DD> cubeToListClone(DD cube) {
        assert alive();
        assert assertCube(cube);
        totalTime.start();
        List<DD> result = clone(cubeToList(cube));
        totalTime.stop();
        return result;
    }

    public void printSupport(DD dd) {
        System.out.println(supportString(dd));
    }

    public String supportString(DD dd) {
        StringBuilder result = new StringBuilder();
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        DD support = supportDD(dd);
        Walker walker = support.walker();
        while (!walker.isLeaf()) {
            result.append(variableNames.get(walker.variable()) + "\n");
            walker.high();
        }
        support.dispose();
        totalTime.stop();
        return result.toString();
    }

    public DD abstractExist(DD dd, Iterable<DD> variables) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDDIterable(variables);
        totalTime.start();
        DD cube = newConstant(true);
        for (DD variable : variables) {
            DD oldCube = cube;
            cube = cube.and(variable);
            oldCube.dispose();
        }
        DD result = abstractExist(dd, cube);
        cube.dispose();
        totalTime.stop();
        return result;
    }

    public IntOpenHashSet findSatSet(DD dd, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.is(dd.getType());
        assert assertCube(cube);
        totalTime.start();
        IntOpenHashSet result = new IntOpenHashSet();
        ValueBoolean cmp = typeBoolean.newValue();
        OperatorEvaluator isZero = ContextValue.get().getEvaluatorOrNull(OperatorIsZero.IS_ZERO, dd.getType());
        if (dd.isLeaf()) {
            if (isZero != null) {
                isZero.apply(cmp, dd.value());
            }
            if (ValueAlgebra.is(dd.value()) && cmp.getBoolean()) {
                totalTime.stop();
                assert false;
                return null;
            } else {
                totalTime.stop();
                return result;
            }
        } else {
            Walker ddWalker = dd.walker();
            Long2ByteOpenHashMap map = new Long2ByteOpenHashMap();
            findSat(ddWalker, map);
            Walker cubeWalker = cube.walker();
            while (!cubeWalker.isLeaf()) {
                int variableNr = cubeWalker.variable();
                if (map.containsKey(variableNr)) {
                    if (map.get(variableNr) != 0) {
                        result.add(variableNr);
                    }
                }
                cubeWalker.high();
            }
            totalTime.stop();
            return result;
        }
    }

    public BitSet findSatBitSet(DD dd, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.is(dd.getType());
        assert assertCube(cube);
        totalTime.start();
//        BitSet result = UtilBitSet.newBitSetBounded(cubeSize(cubeWalker));
        BitSet result = UtilBitSet.newBitSetUnbounded();
        if (dd.isLeaf()) {
            totalTime.stop();
            return result;
        } else {
            Walker cubeWalker = cube.walker();
            Walker ddWalker = dd.walker();
            Long2ByteOpenHashMap map = new Long2ByteOpenHashMap();
            findSat(ddWalker, map);
            while (!cubeWalker.isLeaf()) {
                int variableNr = cubeWalker.variable();
                if (map.containsKey(variableNr)) {
                    if (map.get(variableNr) != 0) {
                        result.set(variableNr);
                    }
                }
                cubeWalker.high();
            }
            totalTime.stop();
            return result;
        }
    }

    public DD intSetToDD(IntOpenHashSet set, DD cube) {
        assert alive();
        assert assertValidIntSet(set, cube);
        totalTime.start();
        DD result = newConstant(true);
        Walker walker = cube.walker();
        while (!walker.isLeaf()) {
            int variableNr = walker.variable();
            DD literal = variable(variableNr);
            if (!set.contains(variableNr)) {
                literal = literal.not();
            } else {
                literal = literal.clone();
            }
            result = result.andWith(literal);
            walker.high();
        }
        totalTime.stop();
        return result;
    }

    private boolean assertValidIntSet(IntOpenHashSet set, DD cube) {
        assert alive();
        assert set != null;
        assert assertValidDD(cube);
        assert cube.assertCube();
        assert assertValidSupport(set);
        IntOpenHashSet support = cube.support();
        set.forEach((int var) -> {
            assert var >= 0;
            assert var < llVariables.get(lowLevelBinary).size() :
                var + " " + llVariables.get(lowLevelBinary).size();
            assert support.contains(var);
        });
        return true;
    }

    public DD divide(DD dd, int intValue) {
        assert alive();
        assert assertValidDD(dd);
        assert assertOperatorCompatible(OperatorDivide.DIVIDE, dd.getType(),
                TypeInteger.get());
        totalTime.start();
        DD divBy = newConstant(intValue);
        DD result = apply(OperatorDivide.DIVIDE, dd, divBy);
        divBy.dispose();
        totalTime.stop();
        return result;
    }

    public Value applyOverSat(Operator identifier, DD dd, DD support, DD sat) {
        assert alive();
        assert identifier != null;
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert assertOperatorCompatible(identifier, dd, dd);
        assert applyOverSatSupportOK(dd, support, sat);
        totalTime.start();
        Type ddType = dd.getType();
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, ddType, ddType);
        Type type = evaluator.resultType();
        Map<LongTriple,Value> known = new HashMap<>();
        Value value = applyOverSat(identifier, dd.walker(), support.walker(), known, type, sat.walker());
        totalTime.stop();
        return value;
    }

    private boolean applyOverSatSupportOK(DD dd, DD support, DD sat) {
        IntOpenHashSet commonSupport = new IntOpenHashSet();
        commonSupport.addAll(dd.support());
        commonSupport.addAll(sat.support());
        IntOpenHashSet supportSet = support.support();
        supportSet.forEach((int var) -> {
            assert supportSet.contains(var) : var;
        });
        return true;
    }

    public Value applyOverSat(Operator identifier, DD dd, DD sat) {
        assert identifier != null;
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert assertOperatorCompatible(identifier, dd, dd);
        totalTime.start();
        DD support = dd.supportDD().andWith(sat.supportDD());
        Value result = applyOverSat(identifier, dd, support, sat);
        support.dispose();
        totalTime.stop();
        return result;
    }

    private Value applyOverSat(Operator identifier, Walker dd, Walker support,
            Map<LongTriple,Value> known,
            Type type, Walker sat) {
        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(identifier, type, type);
        LongTriple triple = new LongTriple(dd.uniqueId(), sat.uniqueId(), support.uniqueId());
        if (known.containsKey(triple)) {
            return known.get(triple);
        }
        if (sat.isFalse()) {
            known.put(triple, null);
            return null;
        } else if (support.isLeaf()) {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, dd.value().getType(), type);
            Value result = type.newValue();
            set.apply(result, dd.value());
            known.put(triple, result);
            return result;
        } else {
            int ddVar = dd.isLeaf() ? Integer.MAX_VALUE : dd.variable();
            int satVar = sat.isLeaf() ? Integer.MAX_VALUE : sat.variable();
            int supportVar = support.variable();
            if (ddVar <= supportVar) {
                dd.low();
            }
            if (satVar <= supportVar) {
                sat.low();
            }
            support.high();
            Value left = applyOverSat(identifier, dd, support, known, type, sat);
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            if (ddVar <= supportVar) {
                dd.high();
            }
            if (satVar <= supportVar) {
                sat.high();
            }
            Value right = applyOverSat(identifier, dd, support, known, type, sat);
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            support.back();
            Value result;
            if (left == null) {
                result = UtilValue.clone(right);
            } else if (right == null) {
                result = UtilValue.clone(left);
            } else {
                result = type.newValue();
                evaluator.apply(result, left, right);
            }
            known.put(triple, result);
            return result;
        }
    }

    public Value maxOverSat(DD dd, DD sat)  {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(OperatorMax.MAX, dd, sat);
        totalTime.stop();
        return result;
    }

    public Value minOverSat(DD dd, DD sat)  {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(OperatorMin.MIN, dd, sat);
        totalTime.stop();
        return result;
    }

    public Value andOverSat(DD dd, DD sat) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(OperatorAnd.AND, dd, sat);
        totalTime.stop();
        return result;
    }

    public Value orOverSat(DD dd, DD sat) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(OperatorOr.OR, dd, sat);
        totalTime.stop();
        return result;
    }

    public void collectValues(Set<Value> values, DD dd, DD sat) {
        assert values != null;
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Map<LongTriple,Value> known = new LinkedHashMap<>();
        DD support = dd.supportDD().andWith(sat.supportDD());
        collectValues(dd.walker(), support.walker(), known, sat.walker());
        values.addAll(known.values());
        support.dispose();
        totalTime.stop();
    }

    private void collectValues(Walker dd, Walker support,
            Map<LongTriple,Value> known,
            Walker sat) {
        LongTriple pair = new LongTriple(dd.uniqueId(), sat.uniqueId(), support.uniqueId());
        if (known.containsKey(pair)) {
            return;
        }
        if (sat.isFalse()) {
            return;
        } else if (support.isLeaf()) {
            known.put(pair, dd.value());
            return;
        } else {
            int ddVar = dd.isLeaf() ? Integer.MAX_VALUE : dd.variable();
            int satVar = sat.isLeaf() ? Integer.MAX_VALUE : sat.variable();
            int supportVar = support.variable();
            if (ddVar <= supportVar) {
                dd.low();
            }
            if (satVar <= supportVar) {
                sat.low();
            }
            support.high();
            collectValues(dd, support, known, sat);
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            if (ddVar <= supportVar) {
                dd.high();
            }
            if (satVar <= supportVar) {
                sat.high();
            }
            collectValues(dd, support, known, sat);
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            support.back();
            known.put(pair, null);
        }
    }    

    public DD supportDD(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        DD result = intSetToCube(support(dd));
        totalTime.stop();
        return result;
    }

    public DD intSetToCube(IntOpenHashSet support) {
        assert alive();
        assert assertValidSupport(support);
        totalTime.start();
        DD[] result = new DD[1];
        result[0] = newConstant(true);
        support.forEach((int var) -> {
            DD resultOld = result[0];
            result[0] = result[0].and(variable(var));
            resultOld.dispose();
        });
        totalTime.stop();
        return result[0];
    }

    private boolean assertValidSupport(IntOpenHashSet support) {
        assert support != null;
        support.forEach((int var) -> {
            assert var >= 0;
            assert var < llVariables.get(lowLevelMulti).size();
        });
        return true;
    }

    public Permutation newPermutation(int[] array) {
        assert alive();
        assert checkPermutation(array);
        totalTime.start();
        Permutation result = new Permutation(this, array);
        totalTime.stop();
        return result;
    }

    private boolean checkPermutation(int[] array) {
        assert alive();
        assert array != null;
        assert array.length <= llVariables.get(lowLevelMulti).size()
                : array.length + " " + llVariables.get(lowLevelMulti).size();
        for (int variable : array) {
            assert variable >= 0 : variable;
            assert variable < llVariables.get(lowLevelMulti).size() : variable;
        }
        IntOpenHashSet contained = new IntOpenHashSet(array);
        assert contained.size() == llVariables.get(lowLevelMulti).size();
        return true;
    }

    public Walker walker(DD dd, boolean autoComplement) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        Walker walker = new Walker(dd, autoComplement);
        totalTime.stop();
        assert registerWalker(walker);
        return walker;
    }

    public SupportWalker supportWalker(DD dd, DD support, boolean stopAtFalse, boolean stopAtZero) {
        assert assertValidDD(dd);
        assert assertValidDD(support);
        assert support.assertCube();
        totalTime.start();
        SupportWalker supportWalker = new SupportWalker(dd, support, stopAtFalse, stopAtZero);
        totalTime.stop();
        return supportWalker;
    }

    public SupportWalker supportWalker(DD dd, DD support) {
        assert assertValidDD(dd);
        assert assertValidDD(support);
        assert support.assertCube();
        totalTime.start();
        SupportWalker supportWalker = new SupportWalker(dd, support);
        totalTime.stop();
        return supportWalker;
    }

    public SupportWalker supportWalker(DD dd, DD support,
            Collection<Value> stopWhere) {
        assert assertValidDD(dd);
        assert assertValidDD(support);
        assert support.assertCube();
        totalTime.start();
        SupportWalker supportWalker = new SupportWalker(dd, support, stopWhere);
        totalTime.stop();
        return supportWalker;
    }

    private boolean registerWalker(Walker walker) {
        assert alive();
        assert walker != null;
        walkers.put(walker, null);
        return true;
    }

    public DD abstractAndExist(DD dd, DD other, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(other);
        assert assertValidDD(cube);
        assert dd.isBoolean();
        assert other.isBoolean();
        assert cube.assertCube();
        totalTime.start();
        DD result;
        DD convertCube;
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube = importDD(cube, dd.getLowLevel());
        } else {
            convertCube = cube;
        }
        LibraryDD lowLevel = dd.getLowLevel();
        if (useAndExist && dd.getLowLevel().hasAndExist()) {
            long resultId = dd.getLowLevel().abstractAndExist(
                    dd.uniqueId(), other.uniqueId(), convertCube.uniqueId());
            result = toDD(resultId, lowLevel);
        } else {
            DD and = dd.and(other);
            result = and.abstractExist(convertCube);
            and.dispose();
        }
        if (dd.getLowLevel() != cube.getLowLevel()) {
            convertCube.dispose();
        }
        totalTime.stop();
        return result;
    }

    void removeDD(DD dd) {
        assert checkDD();
        assert dd != null;
        assert !dd.internalAlive();
        assert invalidateWalkersIfReorder();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        lowLevel.free(dd.uniqueId());
        assert assertRemoveDDFromNodes(lowLevel, dd);
        assert checkDD();
        totalTime.stop();
    }
    
    boolean assertRemoveDDFromNodes(LibraryDD lowLevel, DD dd) {
        nodes.get(lowLevel).remove(dd);
        return true;
    }

    public boolean isDebugDD() {
        return debugDD;
    }

    public List<DD> clone(Iterable<DD> iterable) {
        assert alive();
        assert assertValidDDIterable(iterable);
        totalTime.start();
        List<DD> result = new ArrayList<>();
        for (DD dd : iterable) {
            result.add(dd.clone());
        }
        totalTime.stop();
        return result;
    }

    public List<DD> clone(DD... iterable) {
        assert alive();
        assert assertValidDDArray(iterable);
        totalTime.start();
        List<DD> result = new ArrayList<>();
        for (DD dd : iterable) {
            result.add(dd.clone());
        }
        totalTime.stop();
        return result;
    }

    public void dispose(Iterable<DD> iterable) {
        assert alive();
        assert assertValidDDIterable(iterable);
        totalTime.start();
        for (DD dd : iterable) {
            dd.dispose();
        }
        totalTime.stop();
    }

    public void dispose(DD[] dds) {
        assert alive();
        assert assertValidDDArray(dds);
        totalTime.start();
        for (DD dd : dds) {
            dd.dispose();
        }
        totalTime.stop();
    }

    public DD cloneDD(DD dd) {
        assert checkDD();
        assert alive();
        assert assertValidDD(dd);
        assert invalidateWalkersIfReorder();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        DD result;
        if (assertionsEnabled) {
            result = toDD(lowLevel.clone(dd.uniqueId()), lowLevel);            
        } else {
            lowLevel.clone(dd.uniqueId());
            result = dd;
        }
        totalTime.stop();
        assert checkDD();
        return result;
    }

    // TODO check usage of this function
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        assert alive();
        assert startVariable >= 0;
        assert numVariables > 0;
        totalTime.start();
        lowLevelMulti.addGroup(startVariable, numVariables, fixedOrder);
        lowLevelBinary.addGroup(startVariable, numVariables, fixedOrder);
        totalTime.stop();
    }

    public DD eq(List<DD> set1, List<DD> set2) {
        assert alive();
        assert set1 != null;
        assert set2 != null;
        for (DD dd : set1) {
            assert assertValidDD(dd);
        }
        for (DD dd : set2) {
            assert assertValidDD(dd);
        }
        assert set1.size() == set2.size();
        totalTime.start();
        DD result = newConstant(true);
        Iterator<DD> set1Iter = set1.iterator();
        Iterator<DD> set2Iter = set2.iterator();
        while (set1Iter.hasNext()) {
            DD state1 = set1Iter.next();
            DD state2 = set2Iter.next();
            result = result.andWith(state1.iff(state2));
        }
        totalTime.stop();
        return result;
    }

    Set<LibraryDD> getLowLevels() {
        assert alive();
        return lowLevelsExt;
    }

    public List<DD> twoCplIte(DD ifDD, List<DD> op1, List<DD> op2) {
        assert alive();
        assert assertValidDD(ifDD);
        assert ifDD.isBoolean();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        op1 = new ArrayList<>(clone(op1));
        op2 = new ArrayList<>(clone(op2));
        while (op1.size() < op2.size()) {
            op1.add(op1.get(op1.size() - 1).clone());
        }
        while (op1.size() > op2.size()) {
            op2.add(op2.get(op2.size() - 1).clone());
        }
        List<DD> result = new ArrayList<>();
        for (int index = 0; index < op1.size(); index++) {
            result.add(ifDD.ite(op1.get(index), op2.get(index)));
        }
        dispose(op1);
        dispose(op2);
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplMultiply(List<DD> op1p, List<DD> op2p) {
        assert alive();
        assert twoCplOK(op1p);
        assert twoCplOK(op2p);
        totalTime.start();
        int alignLength = Math.max(op1p.size(), op2p.size());
        List<DD> op1a = twoCplSignExtend(op1p, alignLength);
        List<DD> op2a = twoCplSignExtend(op2p, alignLength);
        int resultLength = 2 * alignLength;
        List<DD> op1Shifted = twoCplShiftLeft(op1a, resultLength);
        List<DD> result = twoCplFromInt(0, resultLength);
        List<DD> zero = twoCplFromInt(0, resultLength);
        for (int place = 0; place < op2a.size(); place++) {
            List<DD> resultOld = result;
            List<DD> subOp1 = op1Shifted.subList
                    (resultLength - place, resultLength + alignLength - place);
            List<DD> subExt = new ArrayList<>();
            subExt.addAll(clone(subOp1));
            while (subExt.size() < zero.size()) {
                subExt.add(newConstant(false));
            }
            List<DD> ite = twoCplIte(op2a.get(place), subExt, zero);
            dispose(subExt);
            result = twoCplAdd(result, ite, resultLength);
            dispose(ite);
            dispose(resultOld);
        }
        dispose(zero);
        dispose(op1a);
        dispose(op2a);
        dispose(op1Shifted);
        totalTime.stop();        
        return result;
    }

    private List<DD> twoCplSignExtend(List<DD> op, int length) {
        assert alive();
        assert twoCplOK(op);
        assert length >= op.size() : length;
        totalTime.start();
        List<DD> result = clone(op);
        while (result.size() < length) {
            result.add(result.get(result.size() - 1).clone());
        }
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplShiftLeft(List<DD> op, int numShift) {
        assert alive();
        assert twoCplOK(op);
        assert numShift >= 0;
        totalTime.start();
        List<DD> result = new ArrayList<>();
        for (int index = 0; index < numShift; index++) {
            result.add(newConstant(false));
        }
        result.addAll(clone(op));
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplAdd(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        List<DD> result = twoCplAdd(op1, op2, Math.max(op1.size(), op2.size()) + 1);
        totalTime.stop();
        return result;
    }    

    private List<DD> twoCplAdd(List<DD> op1w, List<DD> op2w, int size) {
        assert alive();
        assert twoCplOK(op1w);
        assert twoCplOK(op2w);
        List<DD> op1 = new ArrayList<>();
        List<DD> op2 = new ArrayList<>();
        while (op1.size() < Math.min(op1w.size(), size)) {
            op1.add(op1w.get(op1.size()).clone());
        }
        while (op2.size() < Math.min(op2w.size(), size)) {
            op2.add(op2w.get(op2.size()).clone());
        }
        while (op1.size() < size) {
            op1.add(op1.get(op1.size() - 1).clone());
        }
        while (op2.size() < size) {
            op2.add(op2.get(op2.size() - 1).clone());
        }        
        DD overflow = newConstant(false);
        List<DD> result = new ArrayList<>();
        for (int index = 0; index < op1.size(); index++) {
            DD op1p = op1.get(index);
            DD op2p = op2.get(index);
            DD p = op1p.xor(op2p, overflow);
            DD overflowOld = overflow;
            overflow = op1p.and(op2p)
                    .orWith(op1p.and(overflow))
                    .orWith(op2p.and(overflow));
            overflowOld.dispose();
            result.add(p);
        }
        overflow.dispose();
        dispose(op1);
        dispose(op2);
        return result;
    }

    public List<DD> twoCplAddInverse(List<DD> op) {
        assert alive();
        assert twoCplOK(op);
        totalTime.start();
        List<DD> oneComplOp = new ArrayList<>();
        for (DD dd : op) {
            oneComplOp.add(dd.not());
        }
        List<DD> oneVec = twoCplFromInt(1);
        List<DD> twoComplOp = twoCplAdd(oneComplOp, oneVec);
        dispose(oneVec);
        totalTime.stop();
        return twoComplOp;
    }

    public List<DD> twoCplSubtract(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        List<DD> twoComplOp2 = twoCplAddInverse(op2);
        List<DD> result = twoCplAdd(op1, twoComplOp2);
        dispose(twoComplOp2);
        totalTime.stop();
        return result;
    }

    public DD twoCplEq(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        op1 = new ArrayList<>(clone(op1));
        op2 = new ArrayList<>(clone(op2));
        while (op1.size() < op2.size()) {
            op1.add(op1.get(op1.size() - 1).clone());
        }
        while (op1.size() > op2.size()) {
            op2.add(op2.get(op2.size() - 1).clone());
        }
        DD result = eq(op1, op2);
        dispose(op1);
        dispose(op2);
        totalTime.stop();
        return result;
    }

    public DD twoCplLt(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        assert op1 != null;
        assert op2 != null;
        totalTime.start();
        List<DD> subtr = twoCplSubtract(op1, op2);
        DD result = subtr.get(subtr.size() - 1).clone();
        dispose(subtr);
        totalTime.stop();
        return result;
    }

    public DD twoCplLe(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLt(op1, op2).orWith(twoCplEq(op1, op2));
        totalTime.stop();
        return result;
    }

    public DD twoCplGt(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLt(op2, op1);
        totalTime.stop();
        return result;
    }

    public DD twoCplGe(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLe(op2, op1);
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplMin(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD op1Smaller = twoCplLt(op1, op2);
        List<DD> result = twoCplIte(op1Smaller, op1, op2);
        op1Smaller.dispose();
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplMax(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD op1Larger = twoCplGt(op1, op2);
        List<DD> result = twoCplIte(op1Larger, op1, op2);
        op1Larger.dispose();
        totalTime.stop();
        return result;
    }

    public DD twoCplNe(List<DD> op1, List<DD> op2) {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplEq(op1, op2).notWith();
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplFromInt(int number) {
        assert alive();
        assert number > Integer.MIN_VALUE;
        assert number < Integer.MAX_VALUE;
        totalTime.start();
        int numValues = number < 0 ? -number : number + 1;
        int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1) + 1;
        List<DD> result = twoCplFromInt(number, numBits);
        totalTime.stop();
        return result;
    }

    public List<DD> twoCplFromInt(int number, int numBits) {
        assert alive();
        assert number > Integer.MIN_VALUE;
        assert number < Integer.MAX_VALUE;
        assert numBits >= 0;
        totalTime.start();
        int bit = 1;
        List<DD> result = new ArrayList<>();
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            DD literal;
            if ((bit & number) != 0) {
                literal = newConstant(true);
            } else {
                literal = newConstant(false);
            }
            result.add(literal);
            bit <<= 1;
        }
        totalTime.stop();
        return result;
    }

    public boolean isBoolean(DD dd) {
        assert assertValidDD(dd);
        totalTime.start();
        boolean result = TypeBoolean.is(getType(dd));
        totalTime.stop();
        return result;
    }

    Type getType(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        Walker walker = dd.walker();
        while (!walker.isLeaf()) {
            walker.high();
        }
        Type result = walker.value().getType();
        totalTime.stop();
        return result;
    }

    private boolean twoCplOK(List<DD> list) {
        assert list != null;
        for (DD dd : list) {
            assert assertValidDD(dd);
            assert dd.isBoolean();
        }
        return true;
    }

    private boolean invalidateWalkersIfReorder() {
        if (allowReorder) {
            invalidateWalkers();
        }
        return true;
    }

    private boolean invalidateWalkers() {
        for (Walker walker : walkers.keySet()) {
            walker.invalidate();
        }
        walkers.clear();
        return true;
    }

    private boolean assertValidDD(DD dd) {
        assert dd != null;
        assert dd.alive();
        return true;
    }

    Map<LibraryDD, List<DD>> getLowLevelVariables() {
        return llVariables;
    }


    public Value getSomeLeafValue(DD dd) {
        assert assertValidDD(dd);
        totalTime.start();
        Walker walker = dd.walker();
        while (!walker.isLeaf()) {
            walker.high();
        }
        Value result = walker.value();
        totalTime.stop();
        return result;
    }

    public Value getSomeLeafValue(DD dd, DD sat) {
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert !sat.isFalse();
        totalTime.start();
        DD support = dd.supportDD().andWith(sat.supportDD());
        Set<LongTriple> seen = new ObjectOpenHashSet<>();
        Value result = getSomeLeafValue(dd.walker(), sat.walker(), support.walker(), seen);
        assert result != null;
        totalTime.stop();
        return result;
    }

    private Value getSomeLeafValue(Walker dd, Walker sat,
            Walker support,
            Set<LongTriple> seen) {
        LongTriple triple = new LongTriple(dd.uniqueId(), sat.uniqueId(), support.uniqueId());
        if (seen.contains(triple)) {
            return null;
        }
        if (sat.isFalse()) {
            seen.add(triple);
            return null;
        } else if (support.isLeaf()) {
            return dd.value();
        } else {
            int ddVar = dd.isLeaf() ? Integer.MAX_VALUE : dd.variable();
            int satVar = sat.isLeaf() ? Integer.MAX_VALUE : sat.variable();
            int supportVar = support.variable();
            if (ddVar <= supportVar) {
                dd.low();
            }
            if (satVar <= supportVar) {
                sat.low();
            }
            support.high();
            Value left = getSomeLeafValue(dd, sat, support, seen);
            if (left != null) {
                return left;
            }
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            if (ddVar <= supportVar) {
                dd.high();
            }
            if (satVar <= supportVar) {
                sat.high();
            }
            Value right = getSomeLeafValue(dd, sat, support, seen);
            if (right != null) {
                return right;
            }
            if (ddVar <= supportVar) {
                dd.back();
            }
            if (satVar <= supportVar) {
                sat.back();
            }
            support.back();
            assert false;
            return null;
        }
    }

    public DD or(DD[] dds) {
        if (dds.length == 0) {
            return newConstant(false);
        } else {
            return dds[0].or(dds);
        }
    }

    public boolean assertSupport(DD dd, DD[] support) {
        assert alive();
        assert support != null;
        for (DD supp : support) {
            assert supp != null;
            supp.assertCube();
        }
        try {
            assert dd.supportDD().abstractExist(support).isTrueWith()
            : dd.supportDD().abstractExist(support).supportString();
        } catch (EPMCException e) {
            assert false : Util.stackTraceToString(e.getStackTrace());
        }
        return true;
    }

    public DD abstractImpliesForall(DD dd, DD other, DD cube) {
        DD notDD = dd.not();
        DD result = abstractAndExist(notDD, other, cube).notWith();
        notDD.dispose();
        return result;
    }
}
