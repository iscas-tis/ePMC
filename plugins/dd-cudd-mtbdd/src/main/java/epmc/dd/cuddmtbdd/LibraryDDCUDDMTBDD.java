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

package epmc.dd.cuddmtbdd;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.dd.ProblemsDD;
import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.operator.OperatorAdd;
import epmc.value.operator.OperatorAnd;
import epmc.value.operator.OperatorDivide;
import epmc.value.operator.OperatorEq;
import epmc.value.operator.OperatorId;
import epmc.value.operator.OperatorIff;
import epmc.value.operator.OperatorImplies;
import epmc.value.operator.OperatorMax;
import epmc.value.operator.OperatorMin;
import epmc.value.operator.OperatorMultiply;
import epmc.value.operator.OperatorNe;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorOr;
import epmc.value.operator.OperatorSubtract;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

public final class LibraryDDCUDDMTBDD implements LibraryDD {
    public final static String IDENTIFIER = "cudd-mtbdd";

    private final static class OperatorKey {
        private Operator operator;
        private Type[] types;
        
        @Override
        public boolean equals(Object obj) {
            OperatorKey other = (OperatorKey) obj;
            if (operator != other.operator) {
                return false;
            }
            if (!Arrays.equals(types, other.types)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = operator.hashCode() + (hash << 6) + (hash << 16) - hash;            
            hash = Arrays.hashCode(types) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
    }
    
    private final OperatorKey testKey = new OperatorKey();
    private final Map<OperatorKey,OperatorEvaluator> evaluators = new HashMap<>(); 
    
    private OperatorEvaluator getEvaluator(Operator operator, Type[] types) {
        testKey.operator = operator;
        testKey.types = types;
        OperatorEvaluator result = evaluators.get(testKey);
        if (result != null) {
            return result;
        }
        result = ContextValue.get().getEvaluator(operator, types);
        OperatorKey newKey = new OperatorKey();
        newKey.operator = operator;
        newKey.types = types.clone();
        evaluators.put(newKey, result);
        return result;
    }
    
    private final static class LowLevelPermutationCUDD
    implements PermutationLibraryDD {
        private Memory memory;

        LowLevelPermutationCUDD(int numVariables, Memory memory) {
            assert (numVariables == 0) == (memory == null);
            this.memory = memory;
        }

        Memory getMemory() {
            return memory;
        }

    }

    private static interface DD_VOP1 extends Callback {
        long invoke(int op, long f);
    }

    private static interface DD_VOP2 extends Callback {
        long invoke(int op, long f, long g);
    }

    private static interface DD_VOP3 extends Callback {
        long invoke(int op, long f, long g, long h);
    }

    private class DD_VOP1Impl implements DD_VOP1 {
        @Override
        public long invoke(int op, long f) {
            Value opValue = numberToValue(f);
            try {
                Value result = resultType.newValue();
                Operator operator = operators[op];
                assert operator != null;
                Type[] types = new Type[1];
                types[0] = opValue.getType();
                OperatorEvaluator evaluator = getEvaluator(operator, types);
                evaluator.apply(result, opValue);
                return valueToNumber(result);
            } catch (EPMCException e) {
                valueProblem = e;
                return valueToNumber(TypeInteger.get().getZero());
            }
        }
    }

    private class DD_VOP2Impl implements DD_VOP2 {
        @Override
        public long invoke(int op, long f, long g) {
            Value op1Value = numberToValue(f);
            Value op2Value = numberToValue(g);
            Operator operator = operators[op];
            try {
                Value result = resultType.newValue();
                Type[] types = new Type[2];
                types[0] = op1Value.getType();
                types[1] = op2Value.getType();
                OperatorEvaluator evaluator = getEvaluator(operator, types);
                evaluator.apply(result, op1Value, op2Value);
                return valueToNumber(result);
            } catch (EPMCException e) {
                valueProblem = e;
                return valueToNumber(TypeInteger.get().getZero());
            }
        }
    }

    private class DD_VOP3Impl implements DD_VOP3 {
        @Override
        public long invoke(int op, long f, long g, long h) {
            Operator operator = operators[op];
            Value op1Value = numberToValue(f);
            Value op2Value = numberToValue(g);
            Value op3Value = numberToValue(h);
            try {
                Value result = resultType.newValue();
                Type[] types = new Type[3];
                types[0] = op1Value.getType();
                types[1] = op2Value.getType();
                types[2] = op3Value.getType();
                OperatorEvaluator evaluator = getEvaluator(operator, types);
                evaluator.apply(result, op1Value, op2Value, op3Value);
                return valueToNumber(result);
            } catch (EPMCException e) {
                valueProblem = e;
                return valueToNumber(TypeInteger.get().getZero());
            }
        }
    }

    private interface AssertFail extends Callback  {
        void invoke(String file, int line);
    }

    private interface GetOperatorNumber extends Callback  {
        int invoke(String name);
    }

    private class AssertFailImpl implements AssertFail {
        @Override
        public void invoke(String file, int line) {
            try {
                System.err.print("assertion failure in native file: ");
                System.err.println(file + ", " + line);
                assert false : "native file: " + file + ", " + line;
            } catch (RuntimeException e) {
                badProblem = e;
            }
        }
    }

    private class GetOperatorNumberImpl implements GetOperatorNumber {

        @Override
        public int invoke(String cuddName) {
            assert cuddName != null;
            assert OPERATOR_TO_MTBDD.containsKey(cuddName) : cuddName;
            Operator name = OPERATOR_TO_MTBDD.get(cuddName);
            int number = operatorToNumber.get(name);
            //            assert operators[number].getIdentifier().equals(name) : 
            //                operators[number].getIdentifier() + " " + name;
            return number;
        }
    }

    private interface GetNumberOfOperators extends Callback  {
        int invoke();
    }

    private class GetNumberOfOperatorsImpl implements GetNumberOfOperators {

        @Override
        public int invoke() {
            return operators.length;
        }
    }

    /**
     * Stores special values needed to initialise the CUDD manager correctly.
     * 
     * @author Ernst Moritz Hahn
     */
    public final class DdValueTable extends Structure {
        public long zero = valueToNumber(TypeInteger.get().getZero());
        public long one = valueToNumber(TypeInteger.get().getOne());
        public long posInf = valueToNumber(TypeReal.get().getPosInf());
        public long negInf = valueToNumber(TypeReal.get().getNegInf());
        public long two = valueToNumber(UtilValue.newValue(TypeInteger.get(), 2));
        public long falseValue = valueToNumber(TypeBoolean.get().getFalse());
        public long trueValue = valueToNumber(TypeBoolean.get().getTrue());

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {
                    "zero",
                    "one",
                    "posInf",
                    "negInf",
                    "two",
                    "falseValue",
                    "trueValue"
            });
        }
    }

    private long valueToNumberTime;
    private long valueToNumberCalled;
    private long valueToNumberEqq;
    private long valueToNumberEquals;

    /**
     * Interface to call native functions of CUDD library.
     * 
     * @author <a href="mailto:hahn@ios.ac.cn">Ernst Moritz Hahn</a>
     */
    private static class CUDD {
        /** initialise CUDD manager */
        static native Pointer Cudd_MTBDD_Init(int numVars, int numVarsZ, int numSlots,
                int cacheSize, NativeLong maxMemory,
                DdValueTable valueTable,
                DD_VOP1 vop1, DD_VOP2 vop2, DD_VOP3 vop3,
                AssertFail assertFail,
                GetOperatorNumber getOperatorNumber,
                GetNumberOfOperators getNumberOfOperators);
        /** free CUDD manager */
        static native void Cudd_MTBDD_Quit(Pointer unique);

        /** increase reference counter of CUDD DD node */
        static native void Cudd_MTBDD_Ref(Pointer n);
        /** recursively decrease reference counter of CUDD DD node */
        static native void Cudd_MTBDD_RecursiveDeref(Pointer table, Pointer n);

        /** obtain constant MTBDD node */
        static native Pointer Cudd_MTBDD_addConst(Pointer dd, long c);
        /** obtain variable MTBDD node (with boolean branches) */
        static native Pointer Cudd_MTBDD_addIthVar(Pointer dd, int i);
        /** apply monadic function on an MTBDD node */
        static native Pointer Cudd_MTBDD_addMonadicApplyOpNumber(Pointer dd, int op, Pointer f);
        /** apply binary function on MTBDD nodes */
        static native Pointer Cudd_MTBDD_addApplyOpNumber(Pointer dd, int op, Pointer f, Pointer g);
        /** apply ternary function on MTBDD nodes */
        static native Pointer Cudd_MTBDD_addApplyTernaryOpNumber(Pointer dd, int op,
                Pointer f, Pointer g, Pointer h);
        /** sum abstraction for MTBDDs */
        static native Pointer Cudd_MTBDD_addExistAbstract(Pointer dd, Pointer f, Pointer g);
        /** product abstraction for MTBDDs */
        static native Pointer Cudd_MTBDD_addUnivAbstract(Pointer dd, Pointer f, Pointer g);
        /** maximum abstraction for MTBDDs */
        static native Pointer Cudd_MTBDD_addMaxAbstract(Pointer dd, Pointer f, Pointer g);
        /** minimum abstraction for MTBDDs */
        static native Pointer Cudd_MTBDD_addMinAbstract(Pointer dd, Pointer f, Pointer g);

        /** variable permutations for MTBDDs */
        static native Pointer Cudd_MTBDD_addPermute(Pointer manager, Pointer node, Memory permut);

        static native int Cudd_MTBDD_CheckZeroRef(Pointer manager);

        static native int Cudd_DebugCheck(Pointer table);

        static native void Cudd_SetMinHit(Pointer dd, int hr);

        static native void Cudd_EnableGarbageCollection(Pointer dd);

        static native void Cudd_DisableGarbageCollection (Pointer dd);

        static native void Cudd_SetLooseUpTo(Pointer dd, int  lut);

        static native void Cudd_SetMaxCacheHard(Pointer dd, int mc);

        private final static boolean loaded =
                JNATools.registerLibrary(CUDD.class, "cudd-modified");
    }

    /** maximal number of variables possible */
    static final long CUDD_MAXINDEX;
    static {
        if (Pointer.SIZE == 8) {
            CUDD_MAXINDEX = ((~0) >>> 1);
        } else {
            CUDD_MAXINDEX = ((short) ~0);
        }
    };

    /** constant to mark that a constant is not a variable but a constant */
    static final long CUDD_CONST_INDEX = CUDD_MAXINDEX;

    final static int CUDD_CACHE_SLOTS = 262144;
    //    final static int CUDD_UNIQUE_SLOTS = 256;
    final static int MTR_DEFAULT = 0x00000000;
    final static int MTR_TERMINAL = 0x00000001;
    final static int MTR_SOFT = 0x00000002;
    final static int MTR_FIXED = 0x00000004;
    final static int MTR_NEWNODE = 0x00000008;

    private static final Map<String,Operator> OPERATOR_TO_MTBDD;
    static {
        Map<String,Operator> mtbddToOperatorName = new LinkedHashMap<>();
        mtbddToOperatorName.put("add", OperatorAdd.ADD);
        mtbddToOperatorName.put("subtract", OperatorSubtract.SUBTRACT);
        mtbddToOperatorName.put("multiply", OperatorMultiply.MULTIPLY);
        mtbddToOperatorName.put("divide", OperatorDivide.DIVIDE);
        mtbddToOperatorName.put("max", OperatorMax.MAX);
        mtbddToOperatorName.put("min", OperatorMin.MIN);
        mtbddToOperatorName.put("and", OperatorAnd.AND);
        mtbddToOperatorName.put("or", OperatorOr.OR);
        mtbddToOperatorName.put("not", OperatorNot.NOT);
        mtbddToOperatorName.put("iff", OperatorIff.IFF);
        mtbddToOperatorName.put("implies", OperatorImplies.IMPLIES);
        mtbddToOperatorName.put("eq", OperatorEq.EQ);
        mtbddToOperatorName.put("ne", OperatorNe.NE);
        OPERATOR_TO_MTBDD = Collections.unmodifiableMap(mtbddToOperatorName);
    }

    /** pointer to native CUDD manager we are using */
    private Pointer cuddManager;
    /** DD nodes alive just after creation of the manager */
    private int initNodesAlive;    
    /** number to use for next new Value object in table */
    private long nextNumber = 0;
    /** maps long to Value object it represents */
    private TLongObjectHashMap<Value> numberToValue;
    /** maps Value object to its number */
    private TObjectLongHashMap<Value> valueToNumber;    
    /** special values (e.g. 0, 1, true, false) to initialise CUDD manager */
    private DdValueTable valueTable;
    /** current number of variables */
    private int numVariables;
    /** reorder method */
    private RuntimeException badProblem;
    private EPMCException valueProblem;
    private DD_VOP1 vop1;
    private DD_VOP2 vop2;
    private DD_VOP3 vop3;

    /**
     * Obtain number of existing {@code Value} object in table or create new.
     * 
     * @param value {@code Value} object to obtain value of
     * @return number representing {@code Value} object
     */
    private long valueToNumber(Value value) {
        valueToNumberTime -= System.nanoTime();
        valueToNumberCalled++;
        if (valueToNumber.containsKey(value)) {
            valueToNumberTime += System.nanoTime();
            return valueToNumber.get(value);
        }
        /*
        for (TLongObjectIterator<Value> it = numberToValue.iterator(); it.hasNext();) {
            it.advance();
            if (it.value().getType() == value.getType()
                    && it.value().isEq(value)) {
                valueToNumberEquals++;
                valueToNumberTime += System.nanoTime();
                return it.key();
            }
        }
         */
        long currentNumber = nextNumber;
        //            currentNumber |= (long) value.getTypeNumber() << 32;
        Value clone = UtilValue.clone(value);
        numberToValue.put(currentNumber, clone);
        valueToNumber.put(clone, currentNumber);
        nextNumber++;
        valueToNumberTime += System.nanoTime();
        return currentNumber;
    }

    /**
     * Obtain {@code Value} object with given number.
     * 
     * @param number number of {@code Value} object
     * @return {@code Value} object with given number
     */
    private Value numberToValue(long number) {
        return numberToValue.get(number);
    }

    private ContextDD contextDD;
    private boolean alive = true;
    private AssertFailImpl assertFail;
    private GetOperatorNumberImpl getOperatorNumber;
    private GetNumberOfOperators getNumberOfOperators;
    private Type resultType;
    private Operator[] operators;
    private TObjectIntCustomHashMap<Operator> operatorToNumber = new TObjectIntCustomHashMap<>(new IdentityHashingStrategy<>());
    private Operator opId;
    private int opIdNr;

    private Operator[] collectOperators() {
        Set<Operator> operators = new LinkedHashSet<>();
        Collection<OperatorEvaluator> identifiers = ContextValue.get().getEvaluators();
        for (OperatorEvaluator evaluator : identifiers) {
            operators.add(evaluator.getOperator());
        }
        return operators.toArray(new Operator[0]);
    }

    @Override
    public void setContextDD(ContextDD contextDD) {
        assert contextDD != null;
        ensure(CUDD.loaded, ProblemsDD.CUDD_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        this.operators = collectOperators();
        int index = 0;
        for (Operator operator : operators) {
            this.operatorToNumber.put(operator, index);
            index++;
        }
        opId = OperatorId.ID;
        opIdNr = operatorToNumber.get(opId);

        this.numberToValue = new TLongObjectHashMap<>();
        this.valueToNumber = new TObjectLongHashMap<>();
        this.valueTable = new DdValueTable();
        this.vop1 = new DD_VOP1Impl();
        this.vop2 = new DD_VOP2Impl();
        this.vop3 = new DD_VOP3Impl();
        this.assertFail = new AssertFailImpl();
        this.getOperatorNumber = new GetOperatorNumberImpl();
        this.getNumberOfOperators = new GetNumberOfOperatorsImpl();
        Options options = Options.get();
        int initCache = options.getInteger(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_INIT_CACHE_SIZE);
        long maxMemory = options.getLong(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_MEMORY);
        int uniqueSlots = options.getInteger(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_UNIQUE_SLOTS);

        cuddManager = CUDD.Cudd_MTBDD_Init(0, 0, uniqueSlots,
                initCache, new NativeLong(maxMemory),
                valueTable, vop1, vop2, vop3,
                assertFail, getOperatorNumber, getNumberOfOperators);
        if (cuddManager == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        int hardCacheLimit = options.getInteger(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_CACHE_HARD);
        CUDD.Cudd_SetMaxCacheHard(cuddManager, hardCacheLimit);
        int minHit = options.getInteger(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MIN_HIT);
        CUDD.Cudd_SetMinHit(cuddManager, minHit);
        boolean garbageCollect = options.getBoolean(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_GARBAGE_COLLECT);
        if (garbageCollect) {
            CUDD.Cudd_EnableGarbageCollection(cuddManager);
        } else {
            CUDD.Cudd_DisableGarbageCollection(cuddManager);
        }
        int looseUpTo = options.getInteger(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_LOOSE_UP_TO);
        CUDD.Cudd_SetLooseUpTo(cuddManager, looseUpTo);
        initNodesAlive = CUDD.Cudd_MTBDD_CheckZeroRef(cuddManager);
    }

    @Override
    public long apply(Operator operation, Type type, long... operands)
    {
        assert operation != null;
        assert type != null;
        this.resultType = type;
        int opNr = operatorToNumber.get(operation);
        Pointer op1Ptr = operands.length >= 1 ? new Pointer(operands[0]) : null;
        Pointer op2Ptr = operands.length >= 2 ? new Pointer(operands[1]) : null;
        Pointer op3Ptr = operands.length >= 3 ? new Pointer(operands[2]) : null;
        Pointer resPtr;
        if (operands.length == 1) {
            resPtr = CUDD.Cudd_MTBDD_addMonadicApplyOpNumber(cuddManager, opNr, op1Ptr);
        } else if (operands.length == 2) {
            resPtr = CUDD.Cudd_MTBDD_addApplyOpNumber(cuddManager, opNr, op1Ptr, op2Ptr);
        } else if (operands.length == 3) {
            boolean doFree = false;
            Pointer iPtr2;
            Pointer iPtr3;
            if (TypeBoolean.is(type)) {
                iPtr2 = op2Ptr;
                iPtr3 = op3Ptr;
            } else {
                doFree = true;
                iPtr2 = CUDD.Cudd_MTBDD_addMonadicApplyOpNumber(cuddManager, opIdNr, op2Ptr);
                CUDD.Cudd_MTBDD_Ref(iPtr2);
                iPtr3 = CUDD.Cudd_MTBDD_addMonadicApplyOpNumber(cuddManager, opIdNr, op3Ptr);
                CUDD.Cudd_MTBDD_Ref(iPtr3);
            }
            resPtr = CUDD.Cudd_MTBDD_addApplyTernaryOpNumber
                    (cuddManager, opNr, op1Ptr, iPtr2, iPtr3);
            if (doFree) {
                CUDD.Cudd_MTBDD_RecursiveDeref(cuddManager, iPtr2);
                CUDD.Cudd_MTBDD_RecursiveDeref(cuddManager, iPtr3);
            }
        } else {
            assert false;
            resPtr = null;
        }
        if (resPtr == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (valueProblem != null) {
            free(Pointer.nativeValue(resPtr));
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
        CUDD.Cudd_MTBDD_Ref(resPtr);
        return Pointer.nativeValue(resPtr);
    }


    @Override
    public long newConstant(Value value) {
        long valueNumber = valueToNumber(value);
        Pointer res = CUDD.Cudd_MTBDD_addConst(cuddManager, valueNumber);
        if (res == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        CUDD.Cudd_MTBDD_Ref(res);
        return Pointer.nativeValue(res);
    }

    @Override
    public Value value(long dd) {
        Pointer node = new Pointer(dd);
        long value = node.getLong(Pointer.SIZE * 2 - cmplBit(dd));
        return numberToValue(value);
    }

    @Override
    public void close() {
        if (!alive) {
            return;
        }
        alive = false;
        if (contextDD.isDebugDD()) {
            System.out.println("BDD debugging info:");
            System.out.println("valueToNumberTime: " + valueToNumberTime * 1E-9);
            System.out.println("valueToNumberCalled: " + valueToNumberCalled);
            System.out.println("valueToNumberEqq: " + valueToNumberEqq);
            System.out.println("valueToNumberEquals: " + valueToNumberEquals);
            System.out.println("alive: " + CUDD.Cudd_MTBDD_CheckZeroRef(cuddManager));
        }
        int checkZeroRef = CUDD.Cudd_MTBDD_CheckZeroRef(cuddManager);
        assert initNodesAlive == CUDD.Cudd_MTBDD_CheckZeroRef(cuddManager)
                : "init: " + initNodesAlive + "; checkZeroRef: " + checkZeroRef;
        if (cuddManager != null) {
            CUDD.Cudd_MTBDD_Quit(cuddManager);
        }
    }

    @Override
    public long newVariable() {
        Pointer var;
        var = CUDD.Cudd_MTBDD_addIthVar(cuddManager, numVariables);
        if (var == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        numVariables++;
        CUDD.Cudd_MTBDD_Ref(var);
        return Pointer.nativeValue(var);
    }

    @Override
    public void free(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        CUDD.Cudd_MTBDD_RecursiveDeref(cuddManager, node);
    }

    boolean isLeaf(Pointer node) {
        return variable(node) == CUDD_CONST_INDEX;
    }

    @Override
    public boolean isLeaf(long dd) {
        return isLeaf(new Pointer(dd));
    }

    private int variable(Pointer node) {
        int index;
        if (Pointer.SIZE == 8) {
            index = node.getInt(-cmplBit(node));
        } else {
            index = node.getShort(-cmplBit(node));            
        }
        return index;
    }

    @Override
    public int variable(long dd) {
        return variable(new Pointer(dd));
    }

    long uniqueId(Pointer node) {
        return Pointer.nativeValue(node);
    }

    private long cmplBit(long pointer) {
        return 0;
    }

    /**
     * Obtain bit to denote whether pointers are complemented.
     * If the pointer is complemented will return 1 otherwise 0.
     */
    private long cmplBit(Pointer pointer) {
        return 0;
    }

    @Override
    public long walkerLow(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        assert !isLeaf(node);
        return node.getLong(3*Pointer.SIZE - cmplBit(uniqueId));
    }

    @Override
    public long walkerHigh(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        assert !isLeaf(node);
        return node.getLong(2*Pointer.SIZE - cmplBit(uniqueId));
    }

    @Override
    public void reorder() {
        assert alive;
    }

    @Override
    public long permute(long node, PermutationLibraryDD permutation)
    {
        assert permutation != null;
        assert permutation instanceof LowLevelPermutationCUDD;
        LowLevelPermutationCUDD permutationCUDD = (LowLevelPermutationCUDD) permutation;
        Memory permutationMemory = permutationCUDD.getMemory();
        Pointer ptr = new Pointer(node);
        if (permutationMemory == null) {
            CUDD.Cudd_MTBDD_Ref(ptr);
            return node;
        } else {
            Pointer result;
            result = CUDD.Cudd_MTBDD_addPermute(cuddManager, ptr, permutationMemory);
            if (result == null) {
                if (badProblem != null) {
                    throw badProblem;
                }
                fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            }
            CUDD.Cudd_MTBDD_Ref(result);
            return Pointer.nativeValue(result);
        }
    }

    @Override
    public long walkerRegular(long from) {
        return from;
    }

    @Override
    public boolean isComplement(long node) {
        return false;
    }

    void complement(Pointer from, Pointer to) {
    }

    @Override
    public long walkerComplement(long from) {
        return from;
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
    }

    @Override
    public long abstractExist(long dd, long cube) {
        assert false;
        return -1;
    }

    @Override
    public long abstractForall(long dd, long cube) {
        assert false;
        return -1;
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) {
        assert type != null;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        this.resultType = type;
        Pointer result = CUDD.Cudd_MTBDD_addExistAbstract(cuddManager, f, g);
        if (result == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (valueProblem != null) {
            free(Pointer.nativeValue(result));
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
        CUDD.Cudd_MTBDD_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) {
        assert type != null;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        this.resultType = type;
        Pointer result = CUDD.Cudd_MTBDD_addUnivAbstract(cuddManager, f, g);
        if (result == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (valueProblem != null) {
            free(Pointer.nativeValue(result));
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
        CUDD.Cudd_MTBDD_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractAndExist(long dd1, long dd2, long cube)
    {
        assert false;
        return -1;
    }

    @Override
    public long abstractMax(Type type, long dd, long cube) {
        assert type != null;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        this.resultType = type;
        Pointer result = CUDD.Cudd_MTBDD_addMaxAbstract(cuddManager, f, g);
        if (result == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (valueProblem != null) {
            free(Pointer.nativeValue(result));
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
        CUDD.Cudd_MTBDD_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractMin(Type type, long dd, long cube) {
        assert type != null;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        this.resultType = type;
        Pointer result = CUDD.Cudd_MTBDD_addMinAbstract(cuddManager, f, g);
        if (result == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (valueProblem != null) {
            free(Pointer.nativeValue(result));
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
        CUDD.Cudd_MTBDD_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) {
        assert permutation != null;
        assert permutation.length == numVariables
                : "permutation.length == " + permutation.length
                + " != numVariables = " + numVariables;
        Memory memory;
        if (permutation.length == 0) {
            memory = null;
        } else {
            memory = new Memory(permutation.length * 4);
            memory.write(0, permutation, 0, permutation.length);
        }
        return new LowLevelPermutationCUDD(numVariables, memory);
    }

    @Override
    public ContextDD getContextDD() {
        return contextDD;
    }

    @Override
    public long clone(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        CUDD.Cudd_MTBDD_Ref(node);
        return uniqueId;
    }

    @Override
    public boolean equals(long op1, long op2) {
        return op1 == op2;
    }

    @Override
    public long getWalker(long uniqueId) {
        return uniqueId;
    }

    @Override
    public boolean walkerIsLeaf(long dd) {
        return isLeaf(dd);
    }

    @Override
    public Value walkerValue(long dd) {
        return value(dd);
    }

    @Override
    public int walkerVariable(long dd) {
        return variable(dd);
    }

    @Override
    public boolean walkerIsComplement(long node) {
        return isComplement(node);
    }

    @Override
    public boolean hasInverterArcs() {
        return false;
    }

    @Override
    public int hashCode(long uniqueId) {
        return (int)(uniqueId^(uniqueId>>>32));
    }

    @Override
    public boolean hasAndExist() {
        return false;
    }

    @Override
    public boolean checkConsistent() {
        return CUDD.Cudd_DebugCheck(cuddManager) == 0;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean canApply(Operator operation, Type resultType, long... operands) {
        if (operands.length > 3) {
            return false;
        }
        return true;
    }
}
