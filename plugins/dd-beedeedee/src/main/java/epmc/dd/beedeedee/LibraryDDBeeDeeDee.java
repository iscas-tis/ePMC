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

package epmc.dd.beedeedee;

import java.util.HashMap;
import java.util.Map;

import com.juliasoft.beedeedee.bdd.BDD;
import com.juliasoft.beedeedee.factories.Factory;
import com.juliasoft.beedeedee.factories.ResizingAndGarbageCollectedFactory;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.error.EPMCException;
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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public final class LibraryDDBeeDeeDee implements LibraryDD {
    public final static String IDENTIFIER = "beedeedee";
    
    private final static class LowLevelPermutationBeeDeeDee
    implements PermutationLibraryDD{
        private final Map<Integer, Integer> bddPerm;
        
        LowLevelPermutationBeeDeeDee(Map<Integer, Integer> bddPerm) {
            this.bddPerm = bddPerm;
        }
        
        Map<Integer, Integer> getPermutation() {
            return bddPerm;
        }
    }
    
    private ResizingAndGarbageCollectedFactory factory;
    private ContextDD contextDD;
    private Value oneValue;
    private Value zeroValue;
    private BDD zeroNode;
    private BDD oneNode;
    private boolean alive;
    private final TIntList variables = new TIntArrayList();
    private int nextVariable;
    private TLongObjectMap<BDD> uniqueIdTable = new TLongObjectHashMap<>();
    
    private TLongIntMap refs = new TLongIntHashMap();

    @Override
    public void setContextDD(ContextDD contextDD) {
        assert contextDD != null;
        this.contextDD = contextDD;
        int initCache = contextDD.getOptions().getInteger(OptionsDDBeeDeeDee.DD_BEEDEEDEE_INIT_CACHE_SIZE);
        int initSlots = contextDD.getOptions().getInteger(OptionsDDBeeDeeDee.DD_BEEDEEDEE_INIT_NODES);
        this.factory = Factory.mkResizingAndGarbageCollected(initSlots, initCache);
        this.zeroValue = TypeBoolean.get().getFalse();
        this.oneValue = TypeBoolean.get().getTrue();
        this.zeroNode = factory.makeZero();
        this.oneNode = factory.makeOne();
        uniqueIdTable.put(zeroNode.hashCodeAux(), zeroNode);
        uniqueIdTable.put(oneNode.hashCodeAux(), oneNode);
        this.alive = true;
    }
    
    @Override
    public long apply(Operator operation, Type type, long... operands) throws EPMCException {
        assert alive;
        assert operation != null;
        assert type != null;
        assert TypeBoolean.isBoolean(type);
        assert operands != null;
        for (int opNr = 0; opNr < operands.length; opNr++) {
        	assert operands[opNr] >= 0 : opNr + " " + operands[opNr];
        }
        BDD result;
        switch (operation.getIdentifier()) {
        case OperatorId.IDENTIFIER:
        	result = uniqueIdTable.get(operands[0]);
        	break;
        case OperatorNot.IDENTIFIER:
        	result = uniqueIdTable.get(operands[0]).not();
        	break;
        case OperatorAnd.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).and(uniqueIdTable.get(operands[1]));
            break;
        case OperatorEq.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).biimp(uniqueIdTable.get(operands[1]));
            break;
        case OperatorIff.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).biimp(uniqueIdTable.get(operands[1]));
            break;
        case OperatorImplies.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).imp(uniqueIdTable.get(operands[1])); 
            break;
        case OperatorNe.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).xor(uniqueIdTable.get(operands[1]));
            break;
        case OperatorOr.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).or(uniqueIdTable.get(operands[1]));
            break;
        case OperatorIte.IDENTIFIER:
            result = uniqueIdTable.get(operands[0]).ite(uniqueIdTable.get(operands[1]), uniqueIdTable.get(operands[2]));
            break;
        default:
            assert false;
            return -1;
        }
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public long newConstant(Value value) throws EPMCException {
        assert alive;
        assert value != null;
        assert ValueBoolean.isBoolean(value);
        BDD result = ValueBoolean.asBoolean(value).getBoolean() ? factory.makeOne() : factory.makeZero();
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public long newVariable() throws EPMCException {
        assert alive;
        BDD result = factory.makeVar(nextVariable);
        variables.add(nextVariable);
        nextVariable++;
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public boolean isLeaf(long dd) {
        assert alive;
        assert dd >= 0;
        return dd == zeroNode.hashCodeAux() || dd == oneNode.hashCodeAux();
    }

    @Override
    public Value value(long dd) {
        assert alive;
        assert dd == oneNode.hashCodeAux() || dd == zeroNode.hashCodeAux();
        return dd == oneNode.hashCodeAux() ? oneValue : zeroValue;
    }

    @Override
    public int variable(long dd) {
        assert alive;
        assert dd >= 0;
        assert !isLeaf(dd);
        return uniqueIdTable.get(dd).var();
    }

    @Override
    public void reorder() {
        assert alive;
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        assert alive;
    }

    @Override
    public long permute(long dd, PermutationLibraryDD permutation)
            throws EPMCException {
        assert alive;
        assert dd >= 0;
        assert permutation != null;
        assert permutation instanceof LowLevelPermutationBeeDeeDee;
        LowLevelPermutationBeeDeeDee permBeeDeeDee = (LowLevelPermutationBeeDeeDee) permutation;
        Map<Integer, Integer> bddPerm = permBeeDeeDee.getPermutation();
        BDD result = uniqueIdTable.get(dd).replace(bddPerm);
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public long clone(long uniqueId) {
        ref(uniqueIdTable.get(uniqueId));
        return uniqueId;
    }

    private void ref(BDD dd) {
        uniqueIdTable.put(dd.hashCodeAux(), dd);
        refs.adjustOrPutValue(dd.hashCodeAux(), 1, 1);
    }
    
    @Override
    public void free(long dd) {
        assert alive;
        assert dd >= 0;
        assert refs.containsKey(dd);
        int refsRemaining = refs.adjustOrPutValue(dd, -1, 0);
        if(refsRemaining <= 0) {
            uniqueIdTable.get(dd).free();
            uniqueIdTable.remove(dd);
            refs.remove(dd);
        }
    }

    @Override
    public long walkerLow(long dd) {
        assert alive;
        assert dd >= 0;
        BDD low = uniqueIdTable.get(dd).low();
        uniqueIdTable.put(low.hashCodeAux(), low);
        return low.hashCodeAux();
    }

    @Override
    public long walkerHigh(long dd) {
        assert alive;
        assert dd >= 0;
        BDD high = uniqueIdTable.get(dd).high();
        uniqueIdTable.put(high.hashCodeAux(), high);
        return high.hashCodeAux();
    }

    @Override
    public boolean isComplement(long node) {
        assert alive;
        return false;
    }

    @Override
    public long walkerComplement(long from) {
        assert alive;
        assert false;
        return -1;
    }

    @Override
    public long walkerRegular(long from) {
        assert alive;
        assert from >= 0;
        return from;
    }

    @Override
    public long abstractExist(long dd, long cube) {
        assert alive;
        assert dd >= 0;
        assert cube >= 0;
        BDD result = uniqueIdTable.get(dd).exist(uniqueIdTable.get(cube));
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public long abstractForall(long dd, long cube) {
        assert alive;
        assert dd >= 0;
        assert cube >= 0;
        BDD result = uniqueIdTable.get(dd).forAll(uniqueIdTable.get(cube));
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) {
        assert alive;
        assert false;
        return -1;
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) {
        assert alive;
        assert false;
        return -1;
    }

    @Override
    public long abstractMax(Type type, long dd, long cube) throws EPMCException {
        assert alive;
        assert false;
        return -1;
    }

    @Override
    public long abstractMin(Type type, long dd, long cube) throws EPMCException {
        assert alive;
        assert false;
        return -1;
    }

    @Override
    public long abstractAndExist(long dd1, long dd2, long cube)
            throws EPMCException {
        assert alive;
        assert dd1 >= 0;
        assert dd2 >= 0;
        assert cube >= 0;
        BDD result = uniqueIdTable.get(dd1).relProd(uniqueIdTable.get(dd2), uniqueIdTable.get(cube));
        ref(result);
        return result.hashCodeAux();
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) {
        assert alive;
        assert permutation != null;
        // assuming validity has been checked by high level
        Map<Integer,Integer> result = new HashMap<>();
        for(int i = 0; i < permutation.length; i++) {
            result.put(i, permutation[i]);
        }
        return new LowLevelPermutationBeeDeeDee(result);
    }

    @Override
    public void close() {
        assert alive;
        factory.done();
        alive = false;
    }

    @Override
    public ContextDD getContextDD() {
        return contextDD;
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
        return (int) uniqueId;
    }

    @Override
    public boolean hasAndExist() {
        return false;
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
