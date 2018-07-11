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

package epmc.dd.sylvan;

import static epmc.error.UtilError.ensure;

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

public final class LibraryDDSylvan implements LibraryDD {
    public final static String IDENTIFIER = "sylvan";

    private final static class LowLevelPermutationSylvan
    implements PermutationLibraryDD {

        private long permMap;

        LowLevelPermutationSylvan(int [] perm) {
            assert perm != null;
            permMap = Sylvan.Sylvan_map_empty();
            // Walk backwards to avoid walking the complete tree each time
            for(int i = perm.length - 1; i >= 0; i--) {
                permMap = Sylvan.sylvan_map_add(permMap, i, Sylvan.sylvan_ithvar(perm[i]));
            }
            Sylvan.sylvan_ref(permMap);
        }

        long getMap() {
            return permMap;
        }

    }

    /**
     * Interface to call native functions of Sylvan library.
     * The functions that start with a capital letter call
     * the wrapper functions in jna.c.
     */
    private final static class Sylvan {
        static native void lace_init(int workers, int queueSize);
        static native void lace_startup(int workers, Pointer p1, Pointer p2);
        /** initialise Sylvan */
        static native void sylvan_init_package(long initial_tablesize,
                long max_tablesize, long initial_cachesize, long max_cachesize);
        static native void sylvan_init_bdd(int granularity);
        /** free Sylvan resources */
        static native void sylvan_quit();

        /** increase reference counter of Sylvan DD node */
        static native long sylvan_ref(long n);
        /** decrease reference counter of Sylvan DD node */
        static native void sylvan_deref(long n);

        static native long Sylvan_ite(long f, long g, long h);

        static native long Sylvan_and(long f,long g);

        static native long Sylvan_or(long f,long g);

        static native long Sylvan_equiv(long f,long g);

        static native long Sylvan_nor(long f,long g);

        static native long Sylvan_xor(long f,long g);

        static native long Sylvan_imp(long f, long g);

        static native long Sylvan_false();

        static native long Sylvan_true();

        static native long sylvan_low(long f);

        static native long sylvan_high(long f);

        /** obtain variable BDD node (with boolean branches) */
        static native long sylvan_ithvar(long var);
        static native int sylvan_var(long f);

        static native long Sylvan_not(long f);

        static native long Sylvan_exists(long f, long cube);
        static native long Sylvan_forall(long f, long cube);
        static native long Sylvan_and_exists(long f, long g, long cube);

        /** functions used for permutations */
        static native long Sylvan_compose(long f, long map);
        static native long Sylvan_map_empty();
        static native long sylvan_map_add(long map, long key, long value);

        static native void sylvan_printdot(long f);

        private final static boolean loaded =
                JNATools.registerLibrary(Sylvan.class, "sylvan");
    }

    private static final long COMPLEMENT = 0x8000000000000000L;
    private long falseNode;
    private long trueNode;
    private Value valueTrue;
    private Value valueFalse;
    private long nextVariable = 0;

    private ContextDD contextDD;

    @Override
    public void setContextDD(ContextDD contextDD) {
        assert contextDD != null;
        ensure(Sylvan.loaded, ProblemsDD.SYLVAN_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        int workers = Options.get().getInteger(OptionsDDSylvan.DD_SYLVAN_WORKERS);
        long initMem = Options.get().getInteger(OptionsDDSylvan.DD_SYLVAN_INIT_NODES);
        long initCache = Options.get().getInteger(OptionsDDSylvan.DD_SYLVAN_INIT_CACHE_SIZE);
        int cacheGranularity = Options.get().getInteger(OptionsDDSylvan.DD_SYLVAN_CACHE_GRANULARITY);
        Sylvan.lace_init(workers, 1000000);
        Sylvan.lace_startup(0, Pointer.NULL, Pointer.NULL);
        Sylvan.sylvan_init_package(initMem, 1 << 28, initCache, 1 << 27);
        Sylvan.sylvan_init_bdd(cacheGranularity);
        falseNode = Sylvan.Sylvan_false();
        trueNode = Sylvan.Sylvan_true();
        this.valueTrue = UtilValue.newValue(TypeBoolean.get(), true);
        this.valueFalse = UtilValue.newValue(TypeBoolean.get(), false);
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
        long result;
        if (operation.equals(OperatorId.ID)) {
            result = operands[0];        	
        } else if (operation.equals(OperatorNot.NOT)) {
            result = Sylvan.Sylvan_not(operands[0]);        	
        } else if (operation.equals(OperatorAnd.AND)) {
            result = Sylvan.Sylvan_and(operands[0], operands[1]);
        } else if (operation.equals(OperatorEq.EQ)
                || operation.equals(OperatorIff.IFF)) {
            result = Sylvan.Sylvan_equiv(operands[0], operands[1]);
        } else if (operation.equals(OperatorImplies.IMPLIES)) {
            result = Sylvan.Sylvan_imp(operands[0], operands[1]);
        } else if (operation.equals(OperatorNe.NE)) {
            result = Sylvan.Sylvan_xor(operands[0], operands[1]);
        } else if (operation.equals(OperatorOr.OR)) {
            result = Sylvan.Sylvan_or(operands[0], operands[1]);
        } else if (operation.equals(OperatorIte.ITE)) {
            result = Sylvan.Sylvan_ite(operands[0], operands[1], operands[2]);
        } else {
            assert false;
            result = -1;
        }
        Sylvan.sylvan_ref(result);
        return result;
    }

    @Override
    public long newConstant(Value value) {
        assert value != null;
        assert ValueBoolean.is(value) : value.getType() + " " + value;
        long result;
        if (ValueBoolean.as(value).getBoolean()) {
            result = trueNode;
        } else {
            result = falseNode;
        }
        Sylvan.sylvan_ref(result);
        return result;
    }

    @Override
    public long newVariable() {
        long result = Sylvan.sylvan_ithvar(nextVariable);
        nextVariable++;
        Sylvan.sylvan_ref(result);
        return result;
    }

    @Override
    public boolean isLeaf(long dd) {
        return dd == trueNode || dd == falseNode;
    }

    @Override
    public Value value(long dd) {
        assert isLeaf(dd);
        return dd == trueNode ? valueTrue : valueFalse;
    }

    @Override
    public int variable(long dd) {
        return Sylvan.sylvan_var(dd);
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
        assert permutation instanceof LowLevelPermutationSylvan;
        return Sylvan.sylvan_ref(Sylvan.Sylvan_compose(dd, ((LowLevelPermutationSylvan) permutation).getMap()));
    }

    @Override
    public long clone(long uniqueId) {
        Sylvan.sylvan_ref(uniqueId);
        return uniqueId;
    }

    @Override
    public void free(long uniqueId) {
        Sylvan.sylvan_deref(uniqueId);
    }

    @Override
    public long getWalker(long uniqueId) {
        return uniqueId;
    }

    @Override
    public long walkerLow(long uniqueId) {
        return Sylvan.sylvan_low(uniqueId);
    }

    @Override
    public long walkerHigh(long uniqueId) {
        return Sylvan.sylvan_high(uniqueId);
    }

    @Override
    public boolean isComplement(long node) {
        return (node & COMPLEMENT) == COMPLEMENT;
    }

    @Override
    public long walkerComplement(long from) {
        return from ^ COMPLEMENT;
    }

    @Override
    public long walkerRegular(long from) {
        return from & ~COMPLEMENT ;
    }

    @Override
    public long abstractExist(long dd, long cube) {
        long result = Sylvan.Sylvan_exists(dd, cube);
        Sylvan.sylvan_ref(result);
        return result;
    }

    @Override
    public long abstractForall(long dd, long cube) {
        long result = Sylvan.Sylvan_forall(dd, cube);
        Sylvan.sylvan_ref(result);
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
        long result = Sylvan.Sylvan_and_exists(dd1, dd2, cube);
        Sylvan.sylvan_ref(result);
        return result;
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) {
        return new LowLevelPermutationSylvan(permutation);
    }

    @Override
    public boolean equals(long op1, long op2) {
        return op1 == op2;
    }

    @Override
    public void close() {
        Sylvan.sylvan_quit();
    }

    @Override
    public boolean walkerIsLeaf(long dd) {
        return dd == trueNode || dd == falseNode;
    }

    @Override
    public Value walkerValue(long dd) {
        assert walkerIsLeaf(dd);
        return dd == trueNode ? valueTrue : valueFalse;
    }

    @Override
    public int walkerVariable(long dd) {
        return Sylvan.sylvan_var(dd);
    }

    @Override
    public boolean walkerIsComplement(long from) {
        return isComplement(from);
    }

    @Override
    public boolean hasInverterArcs() {
        return true;
    }

    @Override
    public int hashCode(long uniqueId) {
        return (int) uniqueId;
    }

    @Override
    public boolean hasAndExist() {
        return true;
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
