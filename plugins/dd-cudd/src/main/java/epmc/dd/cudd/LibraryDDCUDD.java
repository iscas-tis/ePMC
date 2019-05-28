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

package epmc.dd.cudd;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.dd.ProblemsDD;
import epmc.dd.cudd.OptionsTypesCUDD.CUDDSubengine;
import epmc.error.EPMCException;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorId;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;

/**
 * CUDD BDD library implementation.
 * The purpose of this class is to provide BDD functionality using the CUDD BDD
 * package by Fabio Somenzi. This class can either use the BDD or the MTBDD
 * functionality of the library. In either case, the library will be used to
 * handle BDDs, not MTBDDs, that is, MTBDDs are only used as 0/1 MTBDDs. For
 * MTBDD support, a separate class implementing {@link LibraryDD} must be used.
 * The reason is, that in EPMC we need MTBDDs which allow values of arbitrary
 * types as leaf nodes, while the unmodified version used by this class only
 * supports double values. We use this version, in order not to influence the
 * performance of the MTBDD part, which might be influenced by the major
 * modification needed to make CUDD usable as an MTBDD library.
 * 
 * @author Ernst Moritz Hahn
 */
public final class LibraryDDCUDD implements LibraryDD {
    private final static int POINTER_SIZE = System.getProperty("sun.arch.data.model")
            .equals("32") ? 4 : 8;

    /** Identifier of the CUDD DD library. */
    public final static String IDENTIFIER = "cudd";
    /** Prefix used for loading the native CUDD dynamic library. */
    private final static String CUDD_LIBRARY_PREFIX = "cudd";

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

    /**
     * Interface to call native functions of CUDD library.
     * For a more thorough description of the functionality of these functions,
     * please refere to the
     * <a href="http://vlsi.colorado.edu/~fabio/CUDD/">CUDD manual</a>.
     * 
     * @author <a href="mailto:hahn@ios.ac.cn">Ernst Moritz Hahn</a>
     */
    private final static class CUDD {
        /** initialise CUDD manager */
        static native Pointer Cudd_Init(int numVars, int numVarsZ, int numSlots,
                int cacheSize, NativeLong maxMemory);
        /** free CUDD manager */
        static native void Cudd_Quit(Pointer unique);

        /** increase reference counter of CUDD DD node */
        static native void Cudd_Ref(Pointer n);
        /** recursively decrease reference counter of CUDD DD node */
        static native void Cudd_RecursiveDeref(Pointer table, Pointer n);

        /** variable reordering */
        static native int Cudd_ReduceHeap(Pointer table, int heuristic, int  minsize);
        /** fixing how not to reorder */
        static native Pointer Cudd_MakeTreeNode(Pointer dd, int  low, int size, int type);

        static native Pointer Cudd_addIte(Pointer dd, Pointer f, Pointer g, Pointer h);

        static native Pointer Cudd_addApplyAnd(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_addApplyOr(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_addApplyEq(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_addApplyNe(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_bddAnd(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_bddOr(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_bddXnor(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_bddXor(Pointer dd, Pointer f,Pointer g);

        static native Pointer Cudd_dd_zero(Pointer dd);

        static native Pointer Cudd_dd_one(Pointer dd);

        /** obtain variable MTBDD node (with boolean branches) */
        static native Pointer Cudd_addIthVar(Pointer dd, int i);
        /** apply monadic function on an MTBDD node */
        static native Pointer Cudd_addApplyNot(Pointer dd, Pointer f);

        /** exist abstraction for MTBDDs */
        static native Pointer Cudd_addOrAbstract(Pointer dd, Pointer f, Pointer g);

        /** product abstraction for MTBDDs */
        static native Pointer Cudd_addUnivAbstract(Pointer dd, Pointer f, Pointer g);

        /** variable permutations for MTBDDs */
        static native Pointer Cudd_addPermute(Pointer manager, Pointer node, Memory permut);

        /** obtain variable BDD node (with boolean branches) */
        static native Pointer Cudd_bddIthVar(Pointer dd, int i);

        static native Pointer Cudd_bddIte(Pointer dd, Pointer f, Pointer g, Pointer h);


        /** variable permutations for BDDs */
        static native Pointer Cudd_bddPermute(Pointer manager, Pointer node, Memory permut);
        /** exist abstraction for BDDs */
        static native Pointer Cudd_bddExistAbstract(Pointer dd, Pointer f, Pointer g);
        /** forall abstraction for BDDs */
        static native Pointer Cudd_bddUnivAbstract(Pointer dd, Pointer f, Pointer g);
        /** simultaneous 'and' and existential quantification for BDDs */
        static native Pointer Cudd_bddAndAbstract(Pointer manager, Pointer f, Pointer g, Pointer h);

        /** read BDD one (true) constant */
        static native Pointer Cudd_ReadOne(Pointer dd);

        /** Obtain number of nodes which are referenced. */
        static native int Cudd_CheckZeroRef(Pointer manager);

        /** Consistency check. */
        static native int Cudd_DebugCheck(Pointer table);

        /** Control table resizing. */
        static native void Cudd_SetMinHit(Pointer dd, int hr);

        /** Enable garbage collection. */
        static native void Cudd_EnableGarbageCollection(Pointer dd);

        /** Disable garbage collection. */
        static native void Cudd_DisableGarbageCollection (Pointer dd);

        /** Set fast growth threshold. */
        static native void Cudd_SetLooseUpTo(Pointer dd, int  lut);

        /** Set hard limit of cache size. */
        static native void Cudd_SetMaxCacheHard(Pointer dd, int mc);

        /** Try to load CUDD library and check whether successful. */
        private final static boolean loaded =
                JNATools.registerLibrary(CUDD.class, CUDD_LIBRARY_PREFIX);
    }

    /** Maximal number of variables possible. */
    static final long CUDD_MAXINDEX;
    static {
        /* The value depends on whether we run EPMC on a 32 bit or 64 bit
         * system. */
        if (POINTER_SIZE == 8) {
            CUDD_MAXINDEX = ((~0) >>> 1);
        } else {
            CUDD_MAXINDEX = ((short) ~0);
        }
    };

    /** constant to mark that a constant is not a variable but a constant */
    static final long CUDD_CONST_INDEX = CUDD_MAXINDEX;

    /* reorder methods */
    final static int CUDD_REORDER_SAME = 0;
    final static int CUDD_REORDER_NONE = 1;
    final static int CUDD_REORDER_RANDOM = 2;
    final static int CUDD_REORDER_RANDOM_PIVOT = 3;
    final static int CUDD_REORDER_SIFT = 4;
    final static int CUDD_REORDER_SIFT_CONVERGE = 5;
    final static int CUDD_REORDER_SYMM_SIFT = 6;
    final static int CUDD_REORDER_SYMM_SIFT_CONV = 7;
    final static int CUDD_REORDER_WINDOW2 = 8;
    final static int CUDD_REORDER_WINDOW3 = 9;
    final static int CUDD_REORDER_WINDOW4 = 10;
    final static int CUDD_REORDER_WINDOW2_CONV = 11;
    final static int CUDD_REORDER_WINDOW3_CONV = 12;
    final static int CUDD_REORDER_WINDOW4_CONV = 13;
    final static int CUDD_REORDER_GROUP_SIFT = 14;
    final static int CUDD_REORDER_GROUP_SIFT_CONV = 15;
    final static int CUDD_REORDER_ANNEALING = 16;
    final static int CUDD_REORDER_GENETIC = 17;
    final static int CUDD_REORDER_LINEAR = 18;
    final static int CUDD_REORDER_LINEAR_CONVERGE = 19;
    final static int CUDD_REORDER_LAZY_SIFT = 20;
    final static int CUDD_REORDER_EXACT = 21;

    /** Default value for the number of cache slots. */
    final static String CUDD_CACHE_SLOTS = "262144";
    /** Default value for the number of unique slots. */
    final static String CUDD_UNIQUE_SLOTS = "256";

    final static int MTR_DEFAULT = 0x00000000;
    final static int MTR_TERMINAL = 0x00000001;
    final static int MTR_SOFT = 0x00000002;
    final static int MTR_FIXED = 0x00000004;
    final static int MTR_NEWNODE = 0x00000008;

    /** pointer to native CUDD manager we are using */
    private Pointer cuddManager;
    /** DD nodes alive just after creation of the manager */
    private int initNodesAlive;

    /** current number of variables */
    private int numVariables;
    /** reorder method */
    private int reorderMethod = CUDD_REORDER_WINDOW4;
    /** one (true) variable for BDD case */
    private Pointer bddOne;
    /** zero (false) variable for BDD case */
    private Pointer bddZero;
    /** zero (false) variable for MTBDD case */
    private Pointer addZero;
    /** one (true) variable for MTBDD case */
    private Pointer addOne;
    private Value valueTrue;
    private Value valueFalse;
    private RuntimeException badProblem;
    private EPMCException valueProblem;

    private boolean mtbdd;
    private ContextDD contextDD;
    private boolean alive = true;

    @Override
    public void setContextDD(ContextDD contextDD) {
        assert contextDD != null;
        ensure(CUDD.loaded, ProblemsDD.CUDD_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        Options options = Options.get();
        OptionsTypesCUDD.CUDDSubengine subengine = options.get(OptionsDDCUDD.DD_CUDD_SUBENGINE);
        this.mtbdd = subengine == CUDDSubengine.MTBDD;
        int initCache = options.getInteger(OptionsDDCUDD.DD_CUDD_INIT_CACHE_SIZE);
        long maxMemory = options.getLong(OptionsDDCUDD.DD_CUDD_MAX_MEMORY);
        int uniqueSlots = options.getInteger(OptionsDDCUDD.DD_CUDD_UNIQUE_SLOTS);
        cuddManager = CUDD.Cudd_Init(0, 0, uniqueSlots,
                initCache, new NativeLong(maxMemory));
        if (cuddManager == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        if (!options.getString(OptionsDDCUDD.DD_CUDD_MAX_CACHE_HARD).equals(Options.DEFAULT)) {
            int hardCacheLimit = options.getInteger(OptionsDDCUDD.DD_CUDD_MAX_CACHE_HARD);
            CUDD.Cudd_SetMaxCacheHard(cuddManager, hardCacheLimit);
        }
        if (!options.getString(OptionsDDCUDD.DD_CUDD_MIN_HIT).equals(Options.DEFAULT)) {
            int minHit = options.getInteger(OptionsDDCUDD.DD_CUDD_MIN_HIT);
            CUDD.Cudd_SetMinHit(cuddManager, minHit);
        }
        boolean garbageCollect = options.getBoolean(OptionsDDCUDD.DD_CUDD_GARBAGE_COLLECT);
        if (garbageCollect) {
            CUDD.Cudd_EnableGarbageCollection(cuddManager);
        } else {
            CUDD.Cudd_DisableGarbageCollection(cuddManager);
        }
        if (!options.getString(OptionsDDCUDD.DD_CUDD_LOOSE_UP_TO).equals(Options.DEFAULT)) {	
            int looseUpTo = options.getInteger(OptionsDDCUDD.DD_CUDD_LOOSE_UP_TO);
            CUDD.Cudd_SetLooseUpTo(cuddManager, looseUpTo);
        }

        initNodesAlive = CUDD.Cudd_CheckZeroRef(cuddManager);
        this.bddOne = mtbdd ? null : CUDD.Cudd_ReadOne(cuddManager);
        this.bddZero = mtbdd ? null : new Pointer(0);
        if (!mtbdd) {
            complement(this.bddOne, this.bddZero);
        }
        this.valueTrue = UtilValue.newValue(TypeBoolean.get(), true);
        this.valueFalse = UtilValue.newValue(TypeBoolean.get(), false);
        this.addZero = mtbdd ? CUDD.Cudd_dd_zero(cuddManager) : null;
        this.addOne = mtbdd ? CUDD.Cudd_dd_one(cuddManager) : null;
    }

    @Override
    public long apply(Operator operation, Type type, long... operands) {
        assert operation != null;
        assert type != null;
        Pointer op1Ptr = operands.length >= 1 ? new Pointer(operands[0]) : null;
        Pointer op2Ptr = operands.length >= 2 ? new Pointer(operands[1]) : null;
        Pointer op3Ptr = operands.length >= 3 ? new Pointer(operands[2]) : null;
        Pointer resPtr;
        Pointer notOp1Ptr = null;
        if (mtbdd) {
            if (operation.equals(OperatorId.ID)) {
                resPtr = op1Ptr;
            } else if (operation.equals(OperatorNot.NOT)) {
                resPtr = CUDD.Cudd_addApplyNot(cuddManager, op1Ptr);
            } else if (operation.equals(OperatorAnd.AND)) {
                resPtr = CUDD.Cudd_addApplyAnd(cuddManager, op1Ptr, op2Ptr);        		
            } else if (operation.equals(OperatorEq.EQ)) {
                resPtr = CUDD.Cudd_addApplyEq(cuddManager, op1Ptr, op2Ptr);        		
            } else if (operation.equals(OperatorIff.IFF)) {
                resPtr = CUDD.Cudd_addApplyEq(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorImplies.IMPLIES)) {
                notOp1Ptr = CUDD.Cudd_addApplyNot(cuddManager, op1Ptr);
                CUDD.Cudd_Ref(notOp1Ptr);
                resPtr = CUDD.Cudd_addApplyOr(cuddManager, notOp1Ptr, op2Ptr);
            } else if (operation.equals(OperatorNe.NE)) {
                resPtr = CUDD.Cudd_addApplyNe(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorOr.OR)) {
                resPtr = CUDD.Cudd_addApplyOr(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorIte.ITE)) {
                resPtr = CUDD.Cudd_addIte(cuddManager, op1Ptr, op2Ptr, op3Ptr);
            } else {
                assert false : operation; 
            resPtr = null;
            }
        } else {
            if (operation.equals(OperatorId.ID)) {
                resPtr = op1Ptr;        		
            } else if (operation.equals(OperatorNot.NOT)) {
                resPtr = new Pointer(0);
                complement(op1Ptr, resPtr);
            } else if (operation.equals(OperatorAnd.AND)) {
                resPtr = CUDD.Cudd_bddAnd(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorEq.EQ)) {
                resPtr = CUDD.Cudd_bddXnor(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorIff.IFF)) {
                resPtr = CUDD.Cudd_bddXnor(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorImplies.IMPLIES)) {
                notOp1Ptr = new Pointer(0);
                complement(op1Ptr, notOp1Ptr);
                CUDD.Cudd_Ref(notOp1Ptr);
                resPtr = CUDD.Cudd_bddOr(cuddManager, notOp1Ptr, op2Ptr);
                CUDD.Cudd_RecursiveDeref(cuddManager, notOp1Ptr);
            } else if (operation.equals(OperatorNe.NE)) {
                resPtr = CUDD.Cudd_bddXor(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorOr.OR)) {
                resPtr = CUDD.Cudd_bddOr(cuddManager, op1Ptr, op2Ptr);
            } else if (operation.equals(OperatorIte.ITE)) {
                resPtr = CUDD.Cudd_bddIte(cuddManager, op1Ptr, op2Ptr, op3Ptr);
            } else {
                assert false;
                resPtr = null;
            }
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
        CUDD.Cudd_Ref(resPtr);
        if (operation.equals(OperatorImplies.IMPLIES)) {
            CUDD.Cudd_RecursiveDeref(cuddManager, notOp1Ptr);
        }
        return Pointer.nativeValue(resPtr);
    }


    @Override
    public long newConstant(Value value) {
        assert value != null;
        assert ValueBoolean.is(value) : value.getType() + " " + value;
        if (mtbdd) {
            Pointer result = ValueBoolean.as(value).getBoolean() ? addOne : addZero;
            CUDD.Cudd_Ref(result);
            return Pointer.nativeValue(result);
        } else {
            long one = Pointer.nativeValue(bddOne);
            long zero = Pointer.nativeValue(bddZero);
            long result = ValueBoolean.as(value).getBoolean() ? one : zero;
            CUDD.Cudd_Ref(new Pointer(result));
            return result;
        }
    }

    @Override
    public Value value(long dd) {
        assert isLeaf(dd);
        Pointer node = new Pointer(dd);
        if (mtbdd) {
            return node.equals(addOne) ? valueTrue : valueFalse;
        } else {
            if (node.equals(bddOne)) {
                return valueTrue;
            } else {
                return valueFalse;
            }
        }
    }

    @Override
    public void close() {
        if (!alive) {
            return;
        }
        alive = false;
        int checkZeroRef = CUDD.Cudd_CheckZeroRef(cuddManager);
        assert initNodesAlive == checkZeroRef
                : "init: " + initNodesAlive + "; checkZeroRef: " + checkZeroRef;
        if (cuddManager != null) {
            CUDD.Cudd_Quit(cuddManager);
        }
    }

    @Override
    public long newVariable() {
        Pointer var;
        if (mtbdd) {
            var = CUDD.Cudd_addIthVar(cuddManager, numVariables);
        } else {
            var = CUDD.Cudd_bddIthVar(cuddManager, numVariables);
        }
        if (var == null) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        numVariables++;
        CUDD.Cudd_Ref(var);
        return Pointer.nativeValue(var);
    }

    @Override
    public void free(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        CUDD.Cudd_RecursiveDeref(cuddManager, node);
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
        if (POINTER_SIZE == 8) {
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
        if (mtbdd) {
            return 0;
        } else {
            return pointer & 1;
        }
    }

    /**
     * Obtain bit to denote whether pointers are complemented.
     * If the pointer is complemented will return 1 otherwise 0.
     */
    private long cmplBit(Pointer pointer) {
        if (mtbdd) {
            return 0;
        } else {
            return Pointer.nativeValue(pointer) & 1;
        }
    }

    @Override
    public long walkerLow(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        assert !isLeaf(node);
        return node.getLong(3*POINTER_SIZE - cmplBit(uniqueId));
    }

    @Override
    public long walkerHigh(long uniqueId) {
        Pointer node = new Pointer(uniqueId);
        assert !isLeaf(node);
        return node.getLong(2*POINTER_SIZE - cmplBit(uniqueId));
    }

    void setReorderMethod(int reorderMethod) {
        this.reorderMethod = reorderMethod;
    }

    @Override
    public void reorder() {
        assert alive;
        if (CUDD.Cudd_ReduceHeap(cuddManager, reorderMethod, 1) == 0) {
            if (badProblem != null) {
                throw badProblem;
            }
            fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
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
            CUDD.Cudd_Ref(ptr);
            return node;
        } else {
            Pointer result;
            if (mtbdd) {
                result = CUDD.Cudd_addPermute(cuddManager, ptr, permutationMemory);
            } else {
                result = CUDD.Cudd_bddPermute(cuddManager, ptr, permutationMemory);
            }
            if (result == null) {
                if (badProblem != null) {
                    throw badProblem;
                }
                fail(ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            }
            CUDD.Cudd_Ref(result);
            return Pointer.nativeValue(result);
        }
    }

    @Override
    public long walkerRegular(long from) {
        if (mtbdd) {
            return from;
        } else {
            return from & (~0L << 1);
        }
    }

    @Override
    public boolean isComplement(long node) {
        if (mtbdd) {
            return false;
        } else {
            return (node & 1) != 0;
        }
    }

    void complement(Pointer from, Pointer to) {
        if (!mtbdd) {
            Pointer.nativeValue(to, Pointer.nativeValue(from) ^ 1);
        }
    }

    @Override
    public long walkerComplement(long from) {
        if (mtbdd) {
            return from;
        } else {
            return from ^ 1;
        }
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        int type = fixedOrder ? MTR_FIXED : MTR_DEFAULT;
        CUDD.Cudd_MakeTreeNode(cuddManager, startVariable, numVariables, type);
    }

    @Override
    public long abstractExist(long dd, long cube) {
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        Pointer result;
        if (mtbdd) {
            result = CUDD.Cudd_addOrAbstract(cuddManager, f, g);
        } else {
            result = CUDD.Cudd_bddExistAbstract(cuddManager, f, g);
        }
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
        CUDD.Cudd_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractForall(long dd, long cube) {
        assert mtbdd;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        Pointer result;
        if (mtbdd) {
            result = CUDD.Cudd_addUnivAbstract(cuddManager, f, g);
        } else {
            result = CUDD.Cudd_bddUnivAbstract(cuddManager, f, g);
        }
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
        CUDD.Cudd_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) {
        assert false;
        return -1L;
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) {
        assert type != null;
        Pointer f = new Pointer(dd);
        Pointer g = new Pointer(cube);
        Pointer result = CUDD.Cudd_addUnivAbstract(cuddManager, f, g);
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
        CUDD.Cudd_Ref(result);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractMax(Type type, long dd, long cube) {
        assert false;
        return -1;
    }

    @Override
    public long abstractMin(Type type, long dd, long cube) {
        assert false;
        return -1;
    }

    @Override
    public long abstractAndExist(long dd1, long dd2, long cube)
    {
        Pointer f = new Pointer(dd1);
        Pointer g = new Pointer(dd2);
        Pointer h = new Pointer(cube);
        Pointer result;
        if (mtbdd) {
            result = null;
            assert false;
        } else {
            result = CUDD.Cudd_bddAndAbstract(cuddManager, f, g, h);
        }
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
        CUDD.Cudd_Ref(result);
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
        CUDD.Cudd_Ref(node);
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
        return !mtbdd;
    }

    @Override
    public int hashCode(long uniqueId) {
        return (int)(uniqueId^(uniqueId>>>32));
    }

    @Override
    public boolean hasAndExist() {
        return !mtbdd;
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
        if (!TypeBoolean.is(resultType)) {
            return false;
        }
        return operation.equals(OperatorId.ID)
                || operation.equals(OperatorNot.NOT)
                || operation.equals(OperatorAnd.AND)
                || operation.equals(OperatorEq.EQ)
                || operation.equals(OperatorIff.IFF)
                || operation.equals(OperatorImplies.IMPLIES)
                || operation.equals(OperatorNe.NE)
                || operation.equals(OperatorOr.OR)
                || operation.equals(OperatorIte.ITE);
    }
}
