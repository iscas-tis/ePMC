package epmc.dd;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongByteMap;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import epmc.error.EPMCException;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.RecursiveStopWatch;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAnd;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorOr;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.TypeUnknown;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;

// TODO add support for JINC as soon as/if ever notified by mail
// TODO documentation
// TODO remove entries from Value table if no longer used

public final class ContextDD implements Closeable {
	private final static Map<ContextValue,ContextDD> MAP = new LinkedHashMap<>();
	
	public static ContextDD get(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		ContextDD result = MAP.get(contextValue);
		if (result == null) {
			result = new ContextDD(contextValue);
			MAP.put(contextValue, result);
		}
		return result;
	}

	public static void close(ContextValue contextValue) {
		assert contextValue != null;
		ContextDD result = MAP.get(contextValue);
		if (result == null) {
			return;
		}
		result.close();
		MAP.remove(contextValue);
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
    /** value context we are using */
    private final ContextValue contextValue;
    private final ArrayList<String> variableNames = new ArrayList<>();
    private final Map<LibraryDD,Set<DD>> nodes
    = new TCustomHashMap<>(new IdentityHashingStrategy<>());
    private final Set<LibraryDD> lowLevels
    = new TCustomHashSet<>(new IdentityHashingStrategy<>());
    private final Set<LibraryDD> lowLevelsExt = Collections.unmodifiableSet(lowLevels);
            
    /** whether this context has not yet been shut down */
    private boolean alive = true;

    private final LibraryDD lowLevelBinary;
    private final LibraryDD lowLevelMulti;

    private final Map<LibraryDD,List<DD>> llVariables =
            new TCustomHashMap<>(new IdentityHashingStrategy<>());
    private final boolean debugDD;
    private final static boolean PRINT_INVERTER_ARCS = false;
    private final boolean useAndExist;
    private final Map<Walker,Object> walkers = new WeakHashMap<>();
    private boolean allowReorder;
    private final GenericOperations genericApply;
    private final RecursiveStopWatch totalTime = new RecursiveStopWatch();
    private final RecursiveStopWatch convertTime = new RecursiveStopWatch();
    private final Log log;

    public ContextDD(ContextValue valueContext) throws EPMCException {
        totalTime.start();
        assert valueContext != null;
        this.contextValue = valueContext;
        this.alive = true;
        Options options = valueContext.getOptions();
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = options.get(OptionsDD.DD_LIBRARY_CLASS);
        assert ddLibraryClasses != null;
        Map<String,Class<? extends LibraryDD>> ddMtLibraryClasses = options.get(OptionsDD.DD_MT_LIBRARY_CLASS);
        assert ddMtLibraryClasses != null;
        this.lowLevelMulti = UtilOptions.getInstance(options, OptionsDD.DD_MULTI_ENGINE);
        this.lowLevelMulti.setContextDD(this);
        this.log = options.get(OptionsMessages.LOG);
        
        this.lowLevelBinary = UtilOptions.getInstance(options, OptionsDD.DD_BINARY_ENGINE);
        assert this.lowLevelBinary != null;
        this.lowLevelBinary.setContextDD(this);
        this.lowLevels.add(lowLevelMulti);
        this.lowLevels.add(lowLevelBinary);
        this.llVariables.put(lowLevelBinary, new ArrayList<DD>());
        this.llVariables.put(lowLevelMulti, new ArrayList<DD>());
        this.nodes.put(lowLevelMulti, new TCustomHashSet<DD>(new IdentityHashingStrategy<>()));
        this.nodes.put(lowLevelBinary, new TCustomHashSet<DD>(new IdentityHashingStrategy<>()));
        this.useAndExist = options.getBoolean(OptionsDD.DD_AND_EXIST);
        this.debugDD = options.getBoolean(OptionsDD.DD_DEBUG);
        this.genericApply = new GenericOperations(this);
        totalTime.stop();
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
            throws EPMCException {
        assert name != null;
        assert type != null;
        assert type.getContext() == contextValue;
        assert copies > 0;
        totalTime.start();
        VariableDD variable = new VariableDDImpl(this, copies, type, name);
        variables.add(variable);
        totalTime.stop();
        return variable;
    }

    public VariableDD newVariable(String name, Type type,
            List<List<DD>> ddActionBits) throws EPMCException {
        int copies = ddActionBits.size();
        return newVariable(name, type, copies, ddActionBits);
    }
    
    public VariableDD newVariable(String name, Type type, int copies, List<List<DD>> ddVariables)
            throws EPMCException {
        assert name != null;
        assert type != null;
        assert type.getContext() == contextValue;
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
    
    public VariableDD newBoolean(String name, int copies)
            throws EPMCException {
        assert name != null;
        assert copies > 0;
        totalTime.start();
        VariableDD result = newVariable(name, TypeBoolean.get(contextValue), copies);
        totalTime.stop();
        return result;
    }

    
    public VariableDD newInteger(String name, int copies, int lower, int upper) throws EPMCException {
        assert name != null;
        assert copies > 0;
        assert lower <= upper;
        totalTime.start();
        Type type = TypeInteger.get(contextValue, lower, upper);
        VariableDD result = newVariable(name, type, copies);
        totalTime.stop();
        return result;
    }
    
    public ContextValue getContextValue() {
        assert alive;
        return contextValue;
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

    public DD applyWith(String operator, DD... operands) throws EPMCException {
        assert operator != null;
        assert assertValidDDArray(operands);
        DD result = apply(operator, operands);
        for (DD op : operands) {
            op.dispose();
        }
        return result;
    }

    public DD apply(String operator, DD... ops) throws EPMCException {
        return apply(contextValue.getOperator(operator), ops);
    }
    
    public DD apply(Operator operator, DD... ops) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert operator != null;
        assert assertValidDDArray(ops);
        assert ops.length > 0;
        assert assertOperatorCompatible(operator, ops);
        totalTime.start();
        LibraryDD lowLevel = ops[0].getLowLevel();
        boolean importToMtbdd = mustImportToMtbdd(operator, ops);
        Type type = computeType(operator, ops);
        if (importToMtbdd) {
            lowLevel = lowLevelMulti;
            ops[0] = importDD(ops[0], lowLevelMulti);
        }
        int opAssNr = 0;
        for (DD op : ops) {
            assert op.getLowLevel() == lowLevel : "invalid argument no " + opAssNr;
            opAssNr++;
        }
        
        // TODO the low level interfaces should rather have some method to ask
        // whether they can apply a certain operation, say
        // canApply(Operator operator, long... ops)
        // Also, having three different apply functions in the low level was
        // also no great design decision
        long result;
        long[] opsLong = new long[ops.length];
        for (int opNr = 0; opNr < ops.length; opNr++) {
            opsLong[opNr] = ops[opNr].uniqueId();
        }
        if (ops.length == 0) {
            Type resultType = operator.resultType(new Type[0]);
            Value resultValue = resultType.newValue();
            operator.apply(resultValue, new Value[0]);
            result = lowLevel.newConstant(resultValue);
        } else if (lowLevel.canApply(operator, type, opsLong)) {
            result = lowLevel.apply(operator, type, opsLong);
        } else {
            long[] operands = new long[ops.length];
            for (int index = 0; index < ops.length; index++) {
                operands[index] = ops[index].uniqueId();
            }
            result = genericApply(operator, type, lowLevel, operands);
        }
        DD resultDD = toDD(result, lowLevel);

        if (importToBinary(operator, resultDD, ops)) {
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

    private Type computeType(Operator operator, DD... ops) {
        assert operator != null;
        assert ops != null;
        Type[] types = new Type[ops.length];
        for (int index = 0; index < ops.length; index++) {
            types[index] = ops[index].getType();
        }
        Type type = operator.resultType(types);
        assert !TypeUnknown.isUnknown(type) : operator + SPACE + Arrays.toString(types);
        return type;
    }

    
    public DD apply(Operator operator, List<DD> ops) throws EPMCException {
        totalTime.start();
        DD result = apply(operator, ops.toArray(new DD[0]));
        totalTime.stop();
        return result;
    }
    
    private long genericApply(Operator operator, Type type, LibraryDD lowLevel, long... ops)
            throws EPMCException {
        return this.genericApply.apply(operator, type, lowLevel, ops);
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

    private boolean assertOperatorCompatible(Operator operator, Type... types)
            throws EPMCException {
        Type resultType = operator.resultType(types);
        return true;
    }

    private boolean assertOperatorCompatible(Operator operator, DD... ops)
            throws EPMCException {
        assert assertValidDDArray(ops);
        Type[] types = new Type[ops.length];
        for (int index = 0; index < ops.length; index++) {
            types[index] = ops[index].getType();
        }
        Type resultType = operator.resultType(types);
        assert resultType != null : operator + SPACE + Arrays.toString(types);
        return true;
    }

    private boolean importToBinary(Operator operator, DD resultDD, DD... ops) {
        assert operator != null;
        assert assertValidDD(resultDD);
        assert assertValidDDArray(ops);
        if (lowLevelMulti == lowLevelBinary) {
            return false;
        }
        switch (operator.getIdentifier()) {
        case OperatorGe.IDENTIFIER: case OperatorGt.IDENTIFIER: case OperatorLe.IDENTIFIER: case OperatorLt.IDENTIFIER:
            return true;
        case OperatorEq.IDENTIFIER: case OperatorNe.IDENTIFIER:
            return ops[0].getLowLevel() == lowLevelMulti;
        default:
            return false;
        }
    }

    private boolean mustImportToMtbdd(Operator operator, DD... ops) {
        assert operator != null;
        assert assertValidDDArray(ops);
        return operator == contextValue.getOperator(OperatorIte.IDENTIFIER)
                && ops[0].getLowLevel() == lowLevelBinary
                && ops[1].getLowLevel() == lowLevelMulti;
    }

    private DD newConstant(Value value, LibraryDD lowLevel) throws EPMCException {
        assert alive();
        assert value != null;
        assert invalidateWalkersIfReorder();
        return toDD(lowLevel.newConstant(value), lowLevel);
    }
    
    
    public DD newConstant(Value value) throws EPMCException {
        assert alive();
        assert value != null;
        assert value.getType().getContext() == contextValue;
        totalTime.start();
        DD result;
        if (ValueBoolean.isBoolean(value)) {
            result = newConstant(value, lowLevelBinary);
        } else {
            result = newConstant(value, lowLevelMulti);
        }
        totalTime.stop();
        return result;
    }

    
    public DD newConstant(int value) throws EPMCException {
        assert alive();
        totalTime.start();
        DD result = newConstant(UtilValue.<ValueInteger,TypeInteger>newValue(TypeInteger.get(contextValue), value));
        totalTime.stop();
        return result;
    }

    
    public final DD newConstant(boolean value) throws EPMCException {
        assert alive();
        totalTime.start();
        DD result = newConstant(TypeBoolean.get(contextValue).newValue(value));
        totalTime.stop();
        return result;
    }

    public DD newConstant(Enum<?> constant) throws EPMCException {
        assert alive();
        assert constant != null;
        totalTime.start();
        TypeEnum typeEnum = TypeEnum.get(contextValue, constant.getDeclaringClass());
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

    public DD newVariable() throws EPMCException {
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
        if (contextValue.getOptions().getBoolean(OptionsDD.DD_LEAK_CHECK)) {
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
        }
        List<DD> remaining = new ArrayList<>();
        for (Set<DD> set : nodes.values()) {
            remaining.addAll(set);
        }
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

    
    public TIntSet support(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        TIntSet set = new TIntHashSet();
        TLongSet seen = new TLongHashSet();
        Walker walker = dd.walker();
        support(walker, set, seen);
        totalTime.stop();
        return set;
    }

    
    public Set<VariableDD> highLevelSupport(DD dd) {
        assert alive();
        assert assertValidDD(dd);
        Set<VariableDD> result = new THashSet<>();
        totalTime.start();
        TIntSet llSupport = support(dd);
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
    
    private void support(Walker walker, TIntSet set, TLongSet seen) {
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

    private void toStringEnumNodes(Walker walker, TLongLongMap numeration) {
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
    
    private void toStringNodesRecurse(Walker walker, TLongSet seen, StringBuilder builder, TLongLongMap nodeMap) {
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
        TLongSet seen = new TLongHashSet();
        boolean complement = dd.walker(!PRINT_INVERTER_ARCS).isComplement();
        Walker walker = dd.walker(false);
        if (complement) {
            builder.append("  nodeinit [style=invisible]\n");
        }
        TLongLongMap nodeMap = new TLongLongHashMap();
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
        TIntObjectMap<TLongSet> map = new TIntObjectHashMap<>(0, 0.5F, -2);
        seen.clear();
        toStringRanks(walker, seen, map, builder);
        TIntIterator mapIter = map.keySet().iterator();
        while (mapIter.hasNext()) {
            int number = mapIter.next();
            if (number >= 0) {
                builder.append("  var" + number + " [label=\"" );
                builder.append(variableNames.get(number) + "\"");
                builder.append(" shape=box color=white fillcolor=white");
                builder.append("];\n");
            }
        }
        builder.append("\n");
        int[] sameRank = map.keySet().toArray();
        Arrays.sort(sameRank);
        for (int number : sameRank) {
            TLongSet varNodesSet = map.get(number);
            builder.append("  {rank=same; ");
            TLongIterator varIter = varNodesSet.iterator();
            long[] nodes = new long[varNodesSet.size()];

            int index = 0;
            while (varIter.hasNext()) {
                nodes[index] = nodeMap.get(varIter.next());
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

    private void toStringRanks(Walker walker, TLongSet seen,
            TIntObjectMap<TLongSet> map,
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
                map.put(-1, new TLongHashSet(1, 0.5F, -1L));
            }
            TLongSet set = map.get(-1);
            set.add(walker.uniqueId());
        } else {
            if (!map.containsKey(walker.variable())) {
                map.put(walker.variable(), new TLongHashSet(1, 0.5F, -1));
            }
            TLongSet set = map.get(walker.variable());
            set.add(walker.uniqueId());            
            walker.low();
            toStringRanks(walker, seen, map, builder);
            walker.back();
            walker.high();
            toStringRanks(walker, seen, map, builder);
            walker.back();
        }
    }

    private void toStringEdgesRecurse(Walker dd, TLongSet seen, StringBuilder builder, TLongLongMap nodeMap) {
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
            throws EPMCException {
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
    
    public TLongObjectMap<BigInteger> countSatMap(DD dd, DD cube) {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert cube.assertCube();
        totalTime.start();
        Walker ddWalker = dd.walker();
        Walker cubeWalker = cube.walker();
        TLongObjectHashMap<BigInteger> cache = new TLongObjectHashMap<>();
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
        TLongObjectHashMap<BigInteger> cache = new TLongObjectHashMap<>();
        BigInteger varValue = computeInitialVarValue(variablesCube);
        BigInteger result = countSat(ddWalker, cubeWalker, cache, varValue);
        totalTime.stop();
        return result;
    }

    private boolean assertSupport(DD dd, DD support) {
        assert assertValidDD(dd);
        assert assertValidDD(support);
        assert assertCube(support);
        TIntSet ddSupport = dd.support();
        TIntSet supportSupport = support.support();
        TIntIterator iter = ddSupport.iterator();
        while (iter.hasNext()) {
            int var = iter.next();
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
            TLongObjectMap<BigInteger> cache, BigInteger varValue) {
        BigInteger result;
        if (cube.isLeaf()) {
            assert dd.isLeaf();
            if (ValueBoolean.asBoolean(dd.value()).getBoolean()) {
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
            throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        Walker walker = dd.walker();
        TLongObjectMap<DD> nodeMap = new TLongObjectHashMap<>();
        DD result = importDD(walker, variablesMap, nodeMap, lowLevel);
        for (DD entry : nodeMap.valueCollection()) {
            if (entry != result) {
                entry.dispose();
            }
        }
        
        return result;
    }

    private DD importDD(Walker walker, int[] variablesMap,
            TLongObjectMap<DD> nodeMap, LibraryDD lowLevel)
                    throws EPMCException {
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

    private DD importDD(DD dd, LibraryDD lowLevel) throws EPMCException {
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
        TLongSet seen = new TLongHashSet();
        Walker walker = dd.walker();
        BigInteger result = countNodes(walker, seen);
        totalTime.stop();
        return result;
    }

    private BigInteger countNodes(Walker walker, TLongSet seen) {
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
        nodes.get(lowLevel).add(result);
        return result;
    }
    
    public DD findSat(DD dd, DD cube) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.isBoolean(dd.getType());
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
            TLongByteMap map = new TLongByteHashMap();
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

    private void findSat(Walker walker, TLongByteMap map) {
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
            }
            walker.back();
        }
    }
    
    public DD abstractExist(DD dd, DD cube) throws EPMCException {
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
    
    public DD abstractForall(DD dd, DD cube) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.isBoolean(dd.getType());
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
    
    public DD abstractSum(DD dd, DD cube) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(contextValue.getOperator(OperatorAdd.IDENTIFIER), dd, dd);
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
    
    public DD abstractProduct(DD dd, DD cube) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(contextValue.getOperator(OperatorMultiply.IDENTIFIER), dd, dd);
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

    public DD abstractMax(DD dd, DD cube) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(contextValue.getOperator(OperatorMax.IDENTIFIER), dd, dd);
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
    
    public DD abstractMin(DD dd, DD cube) throws EPMCException {
        assert checkDD();
        assert alive();
        assert invalidateWalkersIfReorder();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert assertOperatorCompatible(contextValue.getOperator(OperatorMax.IDENTIFIER), dd, dd);
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
    
    public DD toMT(DD dd, Value forTrue, Value forFalse) throws EPMCException {
        assert checkDD();
        assert alive();
        assert assertValidDD(dd);
        assert TypeBoolean.isBoolean(dd.getType());
        assert forTrue != null;
        assert forFalse != null;
        assert contextValue == forTrue.getType().getContext();
        assert contextValue == forFalse.getType().getContext();        
        totalTime.start();
        DD one = newConstant(forTrue);
        DD zero = newConstant(forFalse);
        DD result = apply(contextValue.getOperator(OperatorIte.IDENTIFIER), dd, one, zero);
        zero.dispose();
        one.dispose();
        totalTime.stop();
        assert checkDD();
        return result;
    }
    
    public DD toMT(DD dd, int forTrue, int forFalse) throws EPMCException {
        assert checkDD();
        Value forTrueValue = UtilValue.newValue(TypeInteger.get(contextValue), forTrue);
        Value forFalseValue = UtilValue.newValue(TypeInteger.get(contextValue), forFalse);
        assert checkDD();
        return toMT(dd, forTrueValue, forFalseValue);
    }
    
    public DD toInt(DD dd) throws EPMCException {
        assert checkDD();
        return toMT(dd, 1, 0);
    }
    
    public Permutation newPermutation(int[] first, int[] second) throws EPMCException {
        assert alive();
        assert first != null;
        assert second != null;
        assert first.length == second.length;
        totalTime.start();
        TIntHashSet seen = new TIntHashSet();
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
    
    public Permutation newPermutationListInteger(List<Integer> first, List<Integer> second) throws EPMCException {
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
    
    public Permutation newPermutationListDD(List<DD> first, List<DD> second) throws EPMCException {
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
    
    public Permutation newPermutationCube(DD cube1, DD cube2) throws EPMCException {
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

    public DD permute(DD dd, int[] permutationIntArr) throws EPMCException {
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
    
    public DD listToCube(Iterable<DD> vars) throws EPMCException {
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

    public List<DD> cubeToList(DD cube) throws EPMCException {
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

    public List<DD> cubeToListClone(DD cube) throws EPMCException {
        assert alive();
        assert assertCube(cube);
        totalTime.start();
        List<DD> result = clone(cubeToList(cube));
        totalTime.stop();
        return result;
    }

    public void printSupport(DD dd) throws EPMCException {
        System.out.println(supportString(dd));
    }
    
    public String supportString(DD dd) throws EPMCException {
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
    
    public DD abstractExist(DD dd, Iterable<DD> variables) throws EPMCException {
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
    
    public TIntSet findSatSet(DD dd, DD cube) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(cube);
        assert TypeBoolean.isBoolean(dd.getType());
        assert assertCube(cube);
        totalTime.start();
        TIntSet result = new TIntHashSet();
        if (dd.isLeaf()) {
            if (ValueAlgebra.asAlgebra(dd.value()).isZero()) {
                totalTime.stop();
                assert false;
                return null;
            } else {
                totalTime.stop();
                return result;
            }
        } else {
            Walker ddWalker = dd.walker();
            TLongByteMap map = new TLongByteHashMap();
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
    
    public DD intSetToDD(TIntSet set, DD cube) throws EPMCException {
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

    private boolean assertValidIntSet(TIntSet set, DD cube) {
        assert alive();
        assert set != null;
        assert assertValidDD(cube);
        assert cube.assertCube();
        assert assertValidSupport(set);
        TIntIterator supportIterator = set.iterator();
        TIntSet support = cube.support();
        while (supportIterator.hasNext()) {
            int var = supportIterator.next();
            assert var >= 0;
            assert var < llVariables.get(lowLevelBinary).size() :
                var + " " + llVariables.get(lowLevelBinary).size();
            assert support.contains(var);
        }
        
        return true;
    }
    
    public DD divide(DD dd, int intValue) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        assert assertOperatorCompatible(contextValue.getOperator(OperatorDivide.IDENTIFIER), dd.getType(),
                TypeInteger.get(contextValue));
        totalTime.start();
        DD divBy = newConstant(intValue);
        DD result = apply(contextValue.getOperator(OperatorDivide.IDENTIFIER), dd, divBy);
        divBy.dispose();
        totalTime.stop();
        return result;
    }
    
    public Value applyOverSat(Operator operator, DD dd, DD support, DD sat)
            throws EPMCException {
        assert alive();
        assert operator != null;
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert assertOperatorCompatible(operator, dd, dd);
        assert applyOverSatSupportOK(dd, support, sat);
        totalTime.start();
        Type ddType = dd.getType();
        Type type = operator.resultType(ddType, ddType);
        Map<LongTriple,Value> known = new THashMap<>();
        Value value = applyOverSat(operator, dd.walker(), support.walker(), known, type, sat.walker());
        totalTime.stop();
        return value;
    }

    private boolean applyOverSatSupportOK(DD dd, DD support, DD sat)
            throws EPMCException {
        TIntSet commonSupport = new TIntHashSet();
        commonSupport.addAll(dd.support());
        commonSupport.addAll(sat.support());
        TIntSet supportSet = support.support();
        TIntIterator it = commonSupport.iterator();
        while (it.hasNext()) {
            int var = it.next();
            assert supportSet.contains(var) : var;
        }
        return true;
    }
    
    public Value applyOverSat(Operator operator, DD dd, DD sat)
            throws EPMCException {
        assert operator != null;
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert assertOperatorCompatible(operator, dd, dd);
        totalTime.start();
        DD support = dd.supportDD().andWith(sat.supportDD());
        Value result = applyOverSat(operator, dd, support, sat);
        support.dispose();
        totalTime.stop();
        return result;
    }

    private Value applyOverSat(Operator operator, Walker dd, Walker support,
            Map<LongTriple,Value> known,
            Type type, Walker sat) throws EPMCException {
        LongTriple triple = new LongTriple(dd.uniqueId(), sat.uniqueId(), support.uniqueId());
        if (known.containsKey(triple)) {
            return known.get(triple);
        }
        if (sat.isFalse()) {
            known.put(triple, null);
            return null;
        } else if (support.isLeaf()) {
            Value result = type.newValue();
            result.set(dd.value());
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
            Value left = applyOverSat(operator, dd, support, known, type, sat);
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
            Value right = applyOverSat(operator, dd, support, known, type, sat);
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
                operator.apply(result, left, right);
            }
            known.put(triple, result);
            return result;
        }
    }
    
    public Value maxOverSat(DD dd, DD sat) throws EPMCException  {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(contextValue.getOperator(OperatorMax.IDENTIFIER), dd, sat);
        totalTime.stop();
        return result;
    }
    
    public Value minOverSat(DD dd, DD sat) throws EPMCException  {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(contextValue.getOperator(OperatorMin.IDENTIFIER), dd, sat);
        totalTime.stop();
        return result;
    }
    
    public Value andOverSat(DD dd, DD sat) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(contextValue.getOperator(OperatorAnd.IDENTIFIER), dd, sat);
        totalTime.stop();
        return result;
    }
    
    public Value orOverSat(DD dd, DD sat) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        totalTime.start();
        Value result = applyOverSat(contextValue.getOperator(OperatorOr.IDENTIFIER), dd, sat);
        totalTime.stop();
        return result;
    }
    
    public void collectValues(Set<Value> values, DD dd, DD sat) throws EPMCException {
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
            Walker sat) throws EPMCException {
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
    
    public DD supportDD(DD dd) throws EPMCException {
        assert alive();
        assert assertValidDD(dd);
        totalTime.start();
        DD result = intSetToCube(support(dd));
        totalTime.stop();
        return result;
    }
    
    public DD intSetToCube(TIntSet support) throws EPMCException {
        assert alive();
        assert assertValidSupport(support);
        totalTime.start();
        DD result = newConstant(true);
        TIntIterator supportIterator = support.iterator();
        while (supportIterator.hasNext()) {
            DD resultOld = result;
            result = result.and(variable(supportIterator.next()));
            resultOld.dispose();
        }
        totalTime.stop();
        return result;
    }

    private boolean assertValidSupport(TIntSet support) {
        assert support != null;
        TIntIterator supportIterator = support.iterator();
        while (supportIterator.hasNext()) {
            int var = supportIterator.next();
            assert var >= 0;
            assert var < llVariables.get(lowLevelMulti).size();
        }
        return true;
    }
    
    public Permutation newPermutation(int[] array) throws EPMCException {
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
        TIntSet contained = new TIntHashSet(array);
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
    
    public DD abstractAndExist(DD dd, DD other, DD cube)
            throws EPMCException {
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
        assert dd.getContext() == this;
        assert !dd.internalAlive();
        assert invalidateWalkersIfReorder();
        totalTime.start();
        LibraryDD lowLevel = dd.getLowLevel();
        lowLevel.free(dd.uniqueId());
        nodes.get(lowLevel).remove(dd);
        assert checkDD();
        totalTime.stop();
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
        DD result = toDD(lowLevel.clone(dd.uniqueId()), lowLevel);
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
    
    public DD eq(List<DD> set1, List<DD> set2) throws EPMCException {
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
    
    public List<DD> twoCplIte(DD ifDD, List<DD> op1, List<DD> op2)
            throws EPMCException {
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
    
    public List<DD> twoCplMultiply(List<DD> op1p, List<DD> op2p)
            throws EPMCException {
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
    
    public List<DD> twoCplShiftLeft(List<DD> op, int numShift)
            throws EPMCException {
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
    
    public List<DD> twoCplAdd(List<DD> op1, List<DD> op2)
            throws EPMCException {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        List<DD> result = twoCplAdd(op1, op2, Math.max(op1.size(), op2.size()) + 1);
        totalTime.stop();
        return result;
    }    
    
    private List<DD> twoCplAdd(List<DD> op1w, List<DD> op2w, int size)
            throws EPMCException {
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
    
    public List<DD> twoCplAddInverse(List<DD> op) throws EPMCException {
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
    
    public List<DD> twoCplSubtract(List<DD> op1, List<DD> op2)
            throws EPMCException {
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
    
    public DD twoCplEq(List<DD> op1, List<DD> op2) throws EPMCException {
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
    
    public DD twoCplLt(List<DD> op1, List<DD> op2) throws EPMCException {
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
    
    public DD twoCplLe(List<DD> op1, List<DD> op2) throws EPMCException {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLt(op1, op2).orWith(twoCplEq(op1, op2));
        totalTime.stop();
        return result;
    }
    
    public DD twoCplGt(List<DD> op1, List<DD> op2) throws EPMCException {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLt(op2, op1);
        totalTime.stop();
        return result;
    }
    
    public DD twoCplGe(List<DD> op1, List<DD> op2) throws EPMCException {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplLe(op2, op1);
        totalTime.stop();
        return result;
    }
    
    public List<DD> twoCplMin(List<DD> op1, List<DD> op2) throws EPMCException {
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
    
    public List<DD> twoCplMax(List<DD> op1, List<DD> op2) throws EPMCException {
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
    
    public DD twoCplNe(List<DD> op1, List<DD> op2) throws EPMCException {
        assert alive();
        assert twoCplOK(op1);
        assert twoCplOK(op2);
        totalTime.start();
        DD result = twoCplEq(op1, op2).notWith();
        totalTime.stop();
        return result;
    }
    
    public List<DD> twoCplFromInt(int number) throws EPMCException {
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
    
    public List<DD> twoCplFromInt(int number, int numBits)
            throws EPMCException {
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
        boolean result = TypeBoolean.isBoolean(getType(dd));
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
        assert dd.getContext() == this;
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
    
    public Value getSomeLeafValue(DD dd, DD sat) throws EPMCException {
        assert assertValidDD(dd);
        assert assertValidDD(sat);
        assert sat.isBoolean();
        assert !sat.isFalse();
        totalTime.start();
        DD support = dd.supportDD().andWith(sat.supportDD());
        Set<LongTriple> seen = new THashSet<>();
        Value result = getSomeLeafValue(dd.walker(), sat.walker(), support.walker(), seen);
        assert result != null;
        totalTime.stop();
        return result;
    }

    private Value getSomeLeafValue(Walker dd, Walker sat,
            Walker support,
            Set<LongTriple> seen) throws EPMCException {
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
    
    public DD or(DD[] dds) throws EPMCException {
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
    
    public DD abstractImpliesForall(DD dd, DD other, DD cube) throws EPMCException {
        DD notDD = dd.not();
        DD result = abstractAndExist(notDD, other, cube).notWith();
        notDD.dispose();
        return result;
    }
    
    public Options getOptions() {
        return getContextValue().getOptions();
    }
}