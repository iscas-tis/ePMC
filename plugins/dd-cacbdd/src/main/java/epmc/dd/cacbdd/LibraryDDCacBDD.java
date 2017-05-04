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

package epmc.dd.cacbdd;

import static epmc.error.UtilError.ensure;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.dd.ProblemsDD;
import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.value.Operator;
import epmc.value.OperatorAnd;
import epmc.value.OperatorEq;
import epmc.value.OperatorId;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class LibraryDDCacBDD implements LibraryDD {
    public final static String IDENTIFIER = "cacbdd";
    
    private final static class LowLevelPermutationCacBDD
    implements PermutationLibraryDD, Closeable {
        private final Pointer pointer;
        private boolean closed;

        LowLevelPermutationCacBDD(Pointer pointer) {
            this.pointer = pointer;
        }
        
        Pointer getPointer() {
            assert !closed;
            return pointer;
        }

        @Override
        public void close() {
            if (!closed) {
                if (pointer != null) {
                    CacBDD.cacwrapper_free_permutation(pointer);
                }
            }
        }
    }
    
    private static class CacBDD {
        static native Pointer cacwrapper_new_manager(int num_variables);

        static native Pointer cacwrapper_get_xmanager(Pointer bddManager);

        static native void cacwrapper_free_manager(Pointer manager);

        static native Pointer cacwrapper_or(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_and(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_xor(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_nor(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_nand(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_xnor(Pointer op1, Pointer op2);

        static native Pointer cacwrapper_not(Pointer op);

        static native Pointer cacwrapper_ite(Pointer manager, Pointer op1, Pointer op2, Pointer op3);

        static native void cacwrapper_free_bdd(Pointer bdd);

        static native Pointer cacwrapper_new_one(Pointer manager);

        static native Pointer cacwrapper_new_zero(Pointer manager);

        static native Pointer cacwrapper_new_variable(Pointer manager, int variable);
        
        static native boolean cacwrapper_equals(Pointer op1, Pointer op2);

        static native boolean cacwrapper_is_leaf(Pointer op);

        static native boolean cacwrapper_get_value(Pointer op);

        static native int cacwrapper_node(Pointer op1);

        static native Pointer cacwrapper_exist(Pointer op, Pointer cube);

        static native Pointer cacwrapper_universal(Pointer op, Pointer cube);

        static native Pointer cacwrapper_and_exist(Pointer op1, Pointer op2, Pointer cube);

        static native int cacwrapper_get_variable(Pointer op);

        static native Pointer cacwrapper_permute(Pointer op, Pointer permutation);

        static native Pointer cacwrapper_new_permutation(Pointer perm, int size, int maxNumVariables);

        static native void cacwrapper_free_permutation(Pointer permutation);

        static native Pointer cacwrapper_clone(Pointer op);
        
        static native int cacwrapper_walker_variable(Pointer manager, int walker);

        static native int cacwrapper_walker_low(Pointer manager, int walker);

        static native int cacwrapper_walker_high(Pointer manager, int walker);

        static native void cacwrapper_set_max_cache_size(Pointer bddManager, int size);

        private final static boolean loaded =
                JNATools.registerLibrary(CacBDD.class, "cacbdd");
    }

    private final static int COMPLEMENT = 0x80000000;
    
    private ContextDD contextDD;
    private Pointer xbddmanager;
    private Pointer xmanager;
    private Value valueTrue;
    private Value valueFalse;
    private int maxNumVariables;
    private int nextVariable = 1;
    private final List<LowLevelPermutationCacBDD> permutations = new ArrayList<>();

    @Override
    public void setContextDD(ContextDD contextDD) throws EPMCException {
        assert contextDD != null;
        ensure(CacBDD.loaded, ProblemsDD.CACBDD_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        Options options = Options.get();
        this.maxNumVariables = options.getInteger(OptionsDDCacBDD.DD_CACBDD_MAX_NUM_VARIABLES);
        this.xbddmanager = CacBDD.cacwrapper_new_manager(maxNumVariables);
        ensure(this.xbddmanager != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        int maxCacheSize = options.getInteger(OptionsDDCacBDD.DD_CACBDD_MAX_CACHE_SIZE);
        if (maxCacheSize > 0) {
            CacBDD.cacwrapper_set_max_cache_size(this.xbddmanager, maxCacheSize);
        }
        this.xmanager = CacBDD.cacwrapper_get_xmanager(xbddmanager);
        this.valueFalse = TypeBoolean.get().getFalse();
        this.valueTrue = TypeBoolean.get().getTrue();
    }
    
    @Override
    public ContextDD getContextDD() {
        return contextDD;
    }

    @Override
    public long apply(Operator operation, Type type, long... operands) throws EPMCException {
        assert operation != null;
        assert type != null;
        assert TypeBoolean.isBoolean(type);
        Pointer result;
        switch (operation.getIdentifier()) {
        case OperatorNot.IDENTIFIER:
        	result = CacBDD.cacwrapper_not(new Pointer(operands[0]));
        	break;
        case OperatorAnd.IDENTIFIER:
            result = CacBDD.cacwrapper_and(new Pointer(operands[0]), new Pointer(operands[1]));
            break;
        case OperatorEq.IDENTIFIER: case OperatorIff.IDENTIFIER:
            result = CacBDD.cacwrapper_xnor(new Pointer(operands[0]), new Pointer(operands[1]));
            break;
        case OperatorImplies.IDENTIFIER: {
            Pointer np1 = CacBDD.cacwrapper_not(new Pointer(operands[0]));
            ensure(np1 != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            result = CacBDD.cacwrapper_or(np1, new Pointer(operands[1]));
            CacBDD.cacwrapper_free_bdd(np1);
            break;
        }
        case OperatorNe.IDENTIFIER:
            result = CacBDD.cacwrapper_xor(new Pointer(operands[0]), new Pointer(operands[1]));
            break;
        case OperatorOr.IDENTIFIER:
            result = CacBDD.cacwrapper_or(new Pointer(operands[0]), new Pointer(operands[1]));
            break;
        case OperatorIte.IDENTIFIER:
        	result = CacBDD.cacwrapper_ite(xbddmanager, new Pointer(operands[0]), new Pointer(operands[1]), new Pointer(operands[2]));
        	break;
        default:
            assert false;
            result = null;
            break;
        }
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long newConstant(Value value) throws EPMCException {
        assert value != null;
        assert ValueBoolean.isBoolean(value);
        Pointer result;
        if (ValueBoolean.asBoolean(value).getBoolean()) {
            result = CacBDD.cacwrapper_new_one(xbddmanager);
        } else {
            result = CacBDD.cacwrapper_new_zero(xbddmanager);            
        }
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long newVariable() throws EPMCException {
        Pointer result = CacBDD.cacwrapper_new_variable(xbddmanager, nextVariable);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        nextVariable++;
        return Pointer.nativeValue(result);
    }

    @Override
    public boolean isLeaf(long dd) {
        Pointer p = new Pointer(dd);
        return CacBDD.cacwrapper_is_leaf(p);
    }

    @Override
    public Value value(long dd) {
        Pointer p = new Pointer(dd);
        boolean result = CacBDD.cacwrapper_get_value(p);
        return result ? valueTrue : valueFalse;
    }

    @Override
    public int variable(long dd) {
        Pointer p = new Pointer(dd);
        return CacBDD.cacwrapper_get_variable(p) - 1;
    }

    @Override
    public void reorder() throws EPMCException {
        // TODO Auto-generated method stub
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        // TODO Auto-generated method stub
    }

    @Override
    public long permute(long dd, PermutationLibraryDD permutation)
            throws EPMCException {
        assert permutation != null;
        assert permutation instanceof LowLevelPermutationCacBDD;
        Pointer p1 = new Pointer(dd);
        Pointer p2 = ((LowLevelPermutationCacBDD) permutation).getPointer();
        if (p2 == null) {
            Pointer result = CacBDD.cacwrapper_clone(p1);
            ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            return Pointer.nativeValue(result);
        } else {
            Pointer result = CacBDD.cacwrapper_permute(p1, p2);
            ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            return Pointer.nativeValue(result);
        }
    }

    @Override
    public long clone(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        Pointer result = CacBDD.cacwrapper_clone(p);
        if (result == null) {
            throw new OutOfMemoryError();
        }
        return Pointer.nativeValue(result);
    }

    @Override
    public void free(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        CacBDD.cacwrapper_free_bdd(p);
    }

    @Override
    public long getWalker(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        int result = CacBDD.cacwrapper_node(p);
        return result;
    }

    @Override
    public long walkerLow(long uniqueId) {
        assert isInt(uniqueId);
        int node = (int) uniqueId;
        return CacBDD.cacwrapper_walker_low(xmanager, node);
    }

    @Override
    public long walkerHigh(long uniqueId) {
        assert isInt(uniqueId);
        int node = (int) uniqueId;
        return CacBDD.cacwrapper_walker_high(xmanager, node);
    }

    @Override
    public boolean isComplement(long node) {
        int walker = (int) getWalker(node);
        return walkerIsComplement(walker);
    }

    @Override
    public long walkerComplement(long from) {
        assert isInt(from);
        int node = (int) from;
        return node ^ COMPLEMENT;
    }

    @Override
    public long walkerRegular(long from) {
        assert isInt(from);
        int node = (int) from;
        return node & ~COMPLEMENT;
    }

    @Override
    public long abstractExist(long dd, long cube) throws EPMCException {
        Pointer p1 = new Pointer(dd);
        Pointer p2 = new Pointer(cube);
        Pointer result = CacBDD.cacwrapper_exist(p1, p2);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractForall(long dd, long cube) throws EPMCException {
        Pointer p1 = new Pointer(dd);
        Pointer p2 = new Pointer(cube);
        Pointer result = CacBDD.cacwrapper_universal(p1, p2);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractMax(Type type, long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractMin(Type type, long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractAndExist(long dd1, long dd2, long cube)
            throws EPMCException {
        Pointer p1 = new Pointer(dd1);
        Pointer p2 = new Pointer(dd2);
        Pointer p3 = new Pointer(cube);
        Pointer result = CacBDD.cacwrapper_and_exist(p1, p2, p3);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) throws EPMCException {
        assert permutation != null;
        Pointer p;
        if (permutation.length == 0) {
            p = null;
        } else {
            Memory memory = new Memory(permutation.length * 4);
            memory.write(0, permutation, 0, permutation.length);
            p = CacBDD.cacwrapper_new_permutation(memory,
                    permutation.length, maxNumVariables);
            ensure(p != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        LowLevelPermutationCacBDD perm = new LowLevelPermutationCacBDD(p);
        permutations.add(perm);
        
        return perm;
    }

    @Override
    public boolean equals(long op1, long op2) {
        Pointer p1 = new Pointer(op1);
        Pointer p2 = new Pointer(op2);
        return CacBDD.cacwrapper_equals(p1, p2);
    }

    @Override
    public void close() {
        for (LowLevelPermutationCacBDD permutation : permutations) {
            permutation.close();
        }
        if (xbddmanager != null) {
            CacBDD.cacwrapper_free_manager(xbddmanager);
        }
    }

    @Override
    public boolean walkerIsLeaf(long dd) {
        assert isInt(dd);
        int node = (int) dd;
        return node == 0 || node == (0 | COMPLEMENT);
    }

    @Override
    public Value walkerValue(long dd) {
        assert isInt(dd);
        assert walkerIsLeaf(dd);
        int node = (int) dd;
        return node == 0 ? valueTrue : valueFalse;
    }

    @Override
    public int walkerVariable(long dd) {
        assert isInt(dd);
        int node = (int) dd;
        assert node != 0 && node != (0 | COMPLEMENT);
        return CacBDD.cacwrapper_walker_variable(xmanager, node) - 1;
    }

    @Override
    public boolean walkerIsComplement(long from) {
        assert isInt(from);
        int node = (int) from;
        return (node & COMPLEMENT) != 0;
    }
    
    private static boolean isInt(long number) {
        return number == (int) number;
    }

    @Override
    public boolean hasInverterArcs() {
        return true;
    }

    @Override
    public int hashCode(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        return CacBDD.cacwrapper_node(p);
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
		if (!TypeBoolean.isBoolean(resultType)) {
			return false;
		}
		switch (operation.getIdentifier()) {
		case OperatorId.IDENTIFIER:
		case OperatorNot.IDENTIFIER:
		case OperatorAnd.IDENTIFIER:
		case OperatorEq.IDENTIFIER:
		case OperatorIff.IDENTIFIER:
		case OperatorImplies.IDENTIFIER:
		case OperatorNe.IDENTIFIER:
		case OperatorOr.IDENTIFIER:
		case OperatorIte.IDENTIFIER:
			break;
		default:
			return false;
		}
		return true;
	}
}
