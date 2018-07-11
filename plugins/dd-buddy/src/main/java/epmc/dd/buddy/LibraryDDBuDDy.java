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

package epmc.dd.buddy;

import static epmc.error.UtilError.ensure;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.dd.ProblemsDD;
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

public final class LibraryDDBuDDy implements LibraryDD {
    public final static String IDENTIFIER = "buddy";

    private final static class LowLevelPermutationBuDDy
    implements PermutationLibraryDD, Closeable {
        private final Pointer pointer;
        private boolean closed;

        LowLevelPermutationBuDDy(Pointer pointer) {
            assert pointer != null;
            this.pointer = pointer;
        }

        Pointer getPointer() {
            assert !closed;
            return pointer;
        }

        @Override
        public void close() {
            if (!closed) {
                BuDDy.bdd_freepair(pointer);
            }
        }
    }

    private static final class BuDDy {
        static native int bdd_init(int nodesize, int cachesize);

        static native void bdd_done();

        static native int bdd_true();

        static native int bdd_false();

        static native int bdd_setvarnum(int num);

        static native int bdd_ithvar(int var);

        static native int bdd_low(int r);

        static native int bdd_high(int r);

        static native int bdd_or(int l, int r);

        static native int bdd_and(int l, int r);

        static native int bdd_xor(int l, int r);

        static native int bdd_imp(int l, int r);

        static native int bdd_biimp(int l, int r);

        static native int bdd_not(int r);

        static native int bdd_ite(int f, int g, int h);

        static native int bdd_addref(int r);

        static native int bdd_delref(int r);

        static native int bdd_exist(int op, int cube);

        static native int bdd_forall(int op, int cube);

        static native int epmc_bdd_relprod(int a, int b, int cube);

        static native int bdd_var(int r);

        static native Pointer bdd_newpair();

        static native int bdd_setpair(Pointer pair, int oldvar, int newvar);

        static native void bdd_freepair(Pointer pair);

        static native int bdd_replace(int r, Pointer pair);

        static native void epmc_buddy_silence();

        static native void bdd_disable_reorder();

        static native void bdd_enable_reorder();

        static native int bdd_setcacheratio(int r);

        static native int bdd_setmaxincrease(int size);

        static native int bdd_setmaxnodenum(int size);

        static native int bdd_setminfreenodes(int mf);

        private final static boolean loaded =
                JNATools.registerLibrary(BuDDy.class, "buddy");
    }

    private static int instancesRunning;

    private final static int BDD_MEMORY = -1;
    private final static int BDD_VAR = -2;
    private final static int BDD_RANGE = -3;
    private final static int BDD_DEREF = -4;
    private final static int BDD_RUNNING = -5;
    private final static int BDD_FILE = -6;
    private final static int BDD_FORMAT = -7;
    private final static int BDD_ORDER = -8;
    private final static int BDD_BREAK = -9;
    private final static int BDD_VARNUM = -10;
    private final static int BDD_NODES = -11;
    private final static int BDD_OP = -12;
    private final static int BDD_VARSET = -13;
    private final static int BDD_VARBLK = -14;
    private final static int BDD_DECVNUM = -15;
    private final static int BDD_REPLACE = -16;
    private final static int BDD_NODENUM = -17;
    private final static int BDD_ILLBDD = -18;
    private final static int BDD_SIZE = -19;

    private final static int BVEC_SIZE = -20;
    private final static int BVEC_SHIFT = -21;
    private final static int BVEC_DIVZERO = -22;

    private final static int BDD_ERRNUM = 24;

    private ContextDD contextDD;
    private Value valueTrue;
    private Value valueFalse;
    private int nextVariable = 0;
    private final List<LowLevelPermutationBuDDy> permutations = new ArrayList<>();
    private int falseNode;
    private int trueNode;
    private int initVarNum;

    @Override
    public void setContextDD(ContextDD contextDD) {
        assert contextDD != null;
        ensure(BuDDy.loaded, ProblemsDD.BUDDY_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        Options options = Options.get();
        int initCache = options.getInteger(OptionsDDBuDDy.DD_BUDDY_INIT_CACHE_SIZE);
        int initSlots = options.getInteger(OptionsDDBuDDy.DD_BUDDY_INIT_NODES);

        assert instancesRunning == 0;
        checkBuDDyResult(BuDDy.bdd_init(initSlots, initCache));
        BuDDy.epmc_buddy_silence();
        BuDDy.bdd_disable_reorder();
        int cacheRatio = options.getInteger(OptionsDDBuDDy.DD_BUDDY_CACHE_RATIO);
        if (cacheRatio > 0) {
            BuDDy.bdd_setcacheratio(cacheRatio);
        }
        int maxIncrease = options.getInteger(OptionsDDBuDDy.DD_BUDDY_MAX_INCREASE);
        BuDDy.bdd_setmaxincrease(maxIncrease);
        int maxNodeNum = options.getInteger(OptionsDDBuDDy.DD_BUDDY_MAX_NODE_NUM);
        if (maxNodeNum > 0) {
            BuDDy.bdd_setmaxnodenum(maxNodeNum);
        }
        int minFreeNodes = options.getInteger(OptionsDDBuDDy.DD_BUDDY_MIN_FREE_NODES);
        BuDDy.bdd_setminfreenodes(minFreeNodes);
        this.initVarNum = options.getInteger(OptionsDDBuDDy.DD_BUDDY_INIT_VARNUM);
        if (this.initVarNum > 0) {
            BuDDy.bdd_setvarnum(initVarNum);
        }

        this.trueNode = BuDDy.bdd_true();
        this.falseNode = BuDDy.bdd_false();

        this.valueFalse = UtilValue.newValue(TypeBoolean.get(),  false);
        this.valueTrue = UtilValue.newValue(TypeBoolean.get(), true);

        instancesRunning++;
    }

    @Override
    public ContextDD getContextDD() {
        return contextDD;
    }

    @Override
    public long apply(Operator operation, Type type, long... operands) {
        assert operation != null;
        assert type != null;
        assert TypeBoolean.is(type);
        for (int opNr = 0; opNr < operands.length; opNr++) {
            assert assertNonNegInt(operands[opNr]);        	
        }
        int result;
        if (operation.equals(OperatorId.ID)) {
            result = (int) operands[0];        	
        } else if (operation.equals(OperatorNot.NOT)) {
            result = BuDDy.bdd_not((int) operands[0]);
        } else if (operation.equals(OperatorAnd.AND)) {
            result = BuDDy.bdd_and((int) operands[0], (int) operands[1]);
        } else if (operation.equals(OperatorEq.EQ)
                || operation.equals(OperatorIff.IFF)) {
            result = BuDDy.bdd_biimp((int) operands[0], (int) operands[1]);
        } else if (operation.equals(OperatorImplies.IMPLIES)) {
            result = BuDDy.bdd_imp((int) operands[0], (int) operands[1]);
        } else if (operation.equals(OperatorNe.NE)) {
            result = BuDDy.bdd_xor((int) operands[0], (int) operands[1]);
        } else if (operation.equals(OperatorOr.OR)) {
            result = BuDDy.bdd_or((int) operands[0], (int) operands[1]);
        } else if (operation.equals(OperatorIte.ITE)) {
            result = BuDDy.bdd_ite((int) operands[0], (int) operands[1], (int) operands[2]);
        } else {
            result = -1;
            assert false;
        }
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public long newConstant(Value value) {
        assert value != null;
        assert ValueBoolean.is(value) : value.getType() + " " + value;
        int result;
        if (ValueBoolean.as(value).getBoolean()) {
            result = trueNode;
        } else {
            result = falseNode;
        }
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public long newVariable() {
        if (nextVariable + 1 > initVarNum) {
            checkBuDDyResult(BuDDy.bdd_setvarnum(nextVariable + 1));
        }
        int result = BuDDy.bdd_ithvar(nextVariable);
        checkBuDDyResult(result);
        nextVariable++;
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public boolean isLeaf(long dd) {
        assert assertNonNegInt(dd);
        int node = (int) dd;
        return node == trueNode || node == falseNode;
    }

    @Override
    public Value value(long dd) {
        assert assertNonNegInt(dd);
        assert isLeaf(dd);
        return ((int) dd) == trueNode ? valueTrue : valueFalse;
    }

    @Override
    public int variable(long dd) {
        assert assertNonNegInt(dd);
        int node = (int) dd;
        return BuDDy.bdd_var(node);
    }

    @Override
    public void reorder() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        // TODO Auto-generated method stub
    }

    @Override
    public long permute(long dd, PermutationLibraryDD permutation)
    {
        assert permutation != null;
        assert permutation instanceof LowLevelPermutationBuDDy;
        assert assertNonNegInt(dd);
        int p1 = (int) dd;
        Pointer p2 = ((LowLevelPermutationBuDDy) permutation).getPointer();
        int result = BuDDy.bdd_replace(p1, p2);
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public long clone(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        int node = (int) uniqueId;
        BuDDy.bdd_addref(node);
        return uniqueId;
    }

    @Override
    public void free(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        BuDDy.bdd_delref((int) uniqueId);
    }

    @Override
    public long getWalker(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        return uniqueId;
    }

    @Override
    public long walkerLow(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        int node = (int) uniqueId;
        return BuDDy.bdd_low(node);
    }

    @Override
    public long walkerHigh(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        int node = (int) uniqueId;
        return BuDDy.bdd_high(node);
    }

    @Override
    public boolean isComplement(long node) {
        assert assertNonNegInt(node);
        return false;
    }

    @Override
    public long walkerComplement(long from) {
        assert assertNonNegInt(from);
        return from;
    }

    @Override
    public long walkerRegular(long from) {
        assert assertNonNegInt(from);
        return from;
    }

    @Override
    public long abstractExist(long dd, long cube) {
        assert assertNonNegInt(dd);
        assert assertNonNegInt(cube);
        int p1 = (int) dd;
        int p2 = (int) cube;
        int result = BuDDy.bdd_exist(p1, p2);
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public long abstractForall(long dd, long cube) {
        assert assertNonNegInt(dd);
        assert assertNonNegInt(cube);
        int p1 = (int) dd;
        int p2 = (int) cube;
        int result = BuDDy.bdd_forall(p1, p2);
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) {
        assert false;
        return -1;
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) {
        assert false;
        return -1;
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
        assert assertNonNegInt(dd1);
        assert assertNonNegInt(dd2);
        assert assertNonNegInt(cube);
        int p1 = (int) dd1;
        int p2 = (int) dd2;
        int p3 = (int) cube;
        int result = BuDDy.epmc_bdd_relprod(p1, p2, p3);
        checkBuDDyResult(result);
        BuDDy.bdd_addref(result);
        return result;
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) {
        Pointer p = BuDDy.bdd_newpair();
        ensure(p != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        for (int var = 0; var < permutation.length; var++) {
            checkBuDDyResult(BuDDy.bdd_setpair(p, var, permutation[var]));
        }
        LowLevelPermutationBuDDy perm = new LowLevelPermutationBuDDy(p);
        permutations.add(perm);

        return perm;
    }

    @Override
    public boolean equals(long op1, long op2) {
        assert assertNonNegInt(op1);
        assert assertNonNegInt(op2);
        return op1 == op2;
    }

    @Override
    public void close() {
        for (LowLevelPermutationBuDDy permutation : permutations) {
            permutation.close();
        }
        instancesRunning--;
        if (instancesRunning == 0) {
            BuDDy.bdd_done();
        }
    }

    @Override
    public boolean walkerIsLeaf(long dd) {
        assert assertNonNegInt(dd);
        int node = (int) dd;
        return node == trueNode || node == falseNode;
    }

    @Override
    public Value walkerValue(long dd) {
        assert assertNonNegInt(dd);
        assert walkerIsLeaf(dd);
        int node = (int) dd;
        return node == trueNode ? valueTrue : valueFalse;
    }

    @Override
    public int walkerVariable(long dd) {
        assert assertNonNegInt(dd);
        int node = (int) dd;
        return BuDDy.bdd_var(node);
    }

    @Override
    public boolean walkerIsComplement(long from) {
        assert assertNonNegInt(from);
        return false;
    }

    private static boolean assertNonNegInt(long number) {
        assert number >= 0 : number;
        assert number == (int) number : number;
        return true;
    }

    @Override
    public boolean hasInverterArcs() {
        return false;
    }

    @Override
    public int hashCode(long uniqueId) {
        assert assertNonNegInt(uniqueId);
        return (int) uniqueId;
    }

    @Override
    public boolean hasAndExist() {
        return true;
    }

    private void checkBuDDyResult(int result) {
        ensure(result != BDD_MEMORY, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        if (result >= 0) {
            return;
        }
        assert false;
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
