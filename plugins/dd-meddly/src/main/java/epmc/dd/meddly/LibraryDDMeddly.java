package epmc.dd.meddly;

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
import epmc.util.JNATools;
import epmc.value.ContextValue;
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

public final class LibraryDDMeddly implements LibraryDD {
    final static String IDENTIFIER = "meddly";
    
    private final static class LowLevelPermutationMeddly
    implements PermutationLibraryDD, Closeable {
        private final Pointer pointer;
        private boolean closed;

        LowLevelPermutationMeddly(Pointer pointer) {
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
                    Meddly.meddlywrapper_free_permutation(pointer);
                }
            }
        }
    }
    
    private static class Meddly {
        static native Pointer meddlywrapper_new_manager(int num_variables);

        static native Pointer meddlywrapper_get_xmanager(Pointer bddManager);

        static native void meddlywrapper_free_manager(Pointer manager);

        static native Pointer meddlywrapper_or(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_and(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_xor(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_nor(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_nand(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_xnor(Pointer op1, Pointer op2);

        static native Pointer meddlywrapper_not(Pointer op);

        static native Pointer meddlywrapper_ite(Pointer manager, Pointer op1, Pointer op2, Pointer op3);

        static native void meddlywrapper_free_bdd(Pointer bdd);

        static native Pointer meddlywrapper_new_one(Pointer manager);

        static native Pointer meddlywrapper_new_zero(Pointer manager);

        static native Pointer meddlywrapper_new_variable(Pointer manager, int variable);
        
        static native boolean meddlywrapper_equals(Pointer op1, Pointer op2);

        static native boolean meddlywrapper_is_leaf(Pointer op);

        static native boolean meddlywrapper_get_value(Pointer op);

        static native int meddlywrapper_node(Pointer op1);

        static native Pointer meddlywrapper_exist(Pointer op, Pointer cube);

        static native Pointer meddlywrapper_universal(Pointer op, Pointer cube);

        static native Pointer meddlywrapper_and_exist(Pointer op1, Pointer op2, Pointer cube);

        static native int meddlywrapper_get_variable(Pointer op);

        static native Pointer meddlywrapper_permute(Pointer op, Pointer permutation);

        static native Pointer meddlywrapper_new_permutation(Pointer perm, int size, int maxNumVariables);

        static native void meddlywrapper_free_permutation(Pointer permutation);

        static native Pointer meddlywrapper_clone(Pointer op);
        
        static native int meddlywrapper_walker_variable(Pointer manager, int walker);

        static native int meddlywrapper_walker_low(Pointer manager, int walker);

        static native int meddlywrapper_walker_high(Pointer manager, int walker);

        private final static boolean loaded =
                JNATools.registerLibrary(Meddly.class, "meddly");
    }

    private final static int COMPLEMENT = 0x80000000;
    
    private ContextDD contextDD;
    private ContextValue contextValue;
    private Pointer xbddmanager;
    private Pointer xmanager;
    private Value valueTrue;
    private Value valueFalse;
    private int maxNumVariables;
    private int nextVariable = 1;
    private final List<LowLevelPermutationMeddly> permutations = new ArrayList<>();

    @Override
    public void setContextDD(ContextDD contextDD) throws EPMCException {
        assert contextDD != null;
        ensure(Meddly.loaded, ProblemsDD.MEDDLY_NATIVE_LOAD_FAILED);
        this.contextDD = contextDD;
        this.contextValue = contextDD.getContextValue();
        this.maxNumVariables = contextDD.getOptions()
                .getInteger(OptionsDDMeddly.DD_MEDDLY_MAX_NUM_VARIABLES);
        this.xbddmanager = Meddly.meddlywrapper_new_manager(maxNumVariables);
        ensure(this.xbddmanager != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        this.xmanager = Meddly.meddlywrapper_get_xmanager(xbddmanager);
        this.valueFalse = TypeBoolean.get(contextValue).getFalse();
        this.valueTrue = TypeBoolean.get(contextValue).getTrue();
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
        Pointer p1 = operands.length >= 1 ? new Pointer(operands[0]) : null;
        Pointer p2 = operands.length >= 2 ? new Pointer(operands[1]) : null;
        Pointer p3 = operands.length >= 3 ? new Pointer(operands[2]) : null;
        switch (operation.getIdentifier()) {
        case OperatorNot.IDENTIFIER:
            result = Meddly.meddlywrapper_not(p1);
        	break;
        case OperatorAnd.IDENTIFIER:
            result = Meddly.meddlywrapper_and(p1, p2);
            break;
        case OperatorEq.IDENTIFIER: case OperatorIff.IDENTIFIER:
            result = Meddly.meddlywrapper_xnor(p1, p2);
            break;
        case OperatorImplies.IDENTIFIER: {
            Pointer np1 = Meddly.meddlywrapper_not(p1);
            ensure(np1 != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            result = Meddly.meddlywrapper_or(np1, p2);
            Meddly.meddlywrapper_free_bdd(np1);
            break;
        }
        case OperatorNe.IDENTIFIER:
            result = Meddly.meddlywrapper_xor(p1, p2);
            break;
        case OperatorOr.IDENTIFIER:
            result = Meddly.meddlywrapper_or(p1, p2);
            break;
        case OperatorIte.IDENTIFIER:
            result = Meddly.meddlywrapper_ite(xbddmanager, p1, p2, p3);
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
            result = Meddly.meddlywrapper_new_one(xbddmanager);
        } else {
            result = Meddly.meddlywrapper_new_zero(xbddmanager);            
        }
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long newVariable() throws EPMCException {
        Pointer result = Meddly.meddlywrapper_new_variable(xbddmanager, nextVariable);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        nextVariable++;
        return Pointer.nativeValue(result);
    }

    @Override
    public boolean isLeaf(long dd) {
        Pointer p = new Pointer(dd);
        return Meddly.meddlywrapper_is_leaf(p);
    }

    @Override
    public Value value(long dd) {
        Pointer p = new Pointer(dd);
        boolean result = Meddly.meddlywrapper_get_value(p);
        return result ? valueTrue : valueFalse;
    }

    @Override
    public int variable(long dd) {
        Pointer p = new Pointer(dd);
        return Meddly.meddlywrapper_get_variable(p) - 1;
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
        assert permutation instanceof LowLevelPermutationMeddly;
        Pointer p1 = new Pointer(dd);
        Pointer p2 = ((LowLevelPermutationMeddly) permutation).getPointer();
        if (p2 == null) {
            Pointer result = Meddly.meddlywrapper_clone(p1);
            ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            return Pointer.nativeValue(result);
        } else {
            Pointer result = Meddly.meddlywrapper_permute(p1, p2);
            ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
            return Pointer.nativeValue(result);
        }
    }

    @Override
    public long clone(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        Pointer result = Meddly.meddlywrapper_clone(p);
        if (result == null) {
            throw new OutOfMemoryError();
        }
        return Pointer.nativeValue(result);
    }

    @Override
    public void free(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        Meddly.meddlywrapper_free_bdd(p);
    }

    @Override
    public long getWalker(long uniqueId) {
        Pointer p = new Pointer(uniqueId);
        int result = Meddly.meddlywrapper_node(p);
        return result;
    }

    @Override
    public long walkerLow(long uniqueId) {
        assert isInt(uniqueId);
        int node = (int) uniqueId;
        return Meddly.meddlywrapper_walker_low(xmanager, node);
    }

    @Override
    public long walkerHigh(long uniqueId) {
        assert isInt(uniqueId);
        int node = (int) uniqueId;
        return Meddly.meddlywrapper_walker_high(xmanager, node);
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
        Pointer result = Meddly.meddlywrapper_exist(p1, p2);
        ensure(result != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        return Pointer.nativeValue(result);
    }

    @Override
    public long abstractForall(long dd, long cube) throws EPMCException {
        Pointer p1 = new Pointer(dd);
        Pointer p2 = new Pointer(cube);
        Pointer result = Meddly.meddlywrapper_universal(p1, p2);
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
        Pointer result = Meddly.meddlywrapper_and_exist(p1, p2, p3);
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
            p = Meddly.meddlywrapper_new_permutation(memory,
                    permutation.length, maxNumVariables);
            ensure(p != null, ProblemsDD.INSUFFICIENT_NATIVE_MEMORY);
        }
        LowLevelPermutationMeddly perm = new LowLevelPermutationMeddly(p);
        permutations.add(perm);
        
        return perm;
    }

    @Override
    public boolean equals(long op1, long op2) {
        Pointer p1 = new Pointer(op1);
        Pointer p2 = new Pointer(op2);
        return Meddly.meddlywrapper_equals(p1, p2);
    }

    @Override
    public void close() {
        for (LowLevelPermutationMeddly permutation : permutations) {
            permutation.close();
        }
        if (xbddmanager != null) {
            Meddly.meddlywrapper_free_manager(xbddmanager);
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
        return Meddly.meddlywrapper_walker_variable(xmanager, node) - 1;
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
        return Meddly.meddlywrapper_node(p);
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
