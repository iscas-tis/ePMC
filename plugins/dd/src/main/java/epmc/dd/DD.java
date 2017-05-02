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

import gnu.trove.set.TIntSet;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivide;
import epmc.value.OperatorDivideIgnoreZero;
import epmc.value.OperatorEq;
import epmc.value.OperatorFloor;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

// Note: MTBDD DDs always store values of a single value.Type.

/**
 * Abstraction of a single node of a decision diagram.
 * 
 * @author Ernst Moritz Hahn
 */
public final class DD implements Cloneable {
    /** Context this node belongs to. */
    private final ContextDD context;
    /** DD library this node belongs to. */
    private final LibraryDD libraryDD;
    /** Unique ID in the DD library the node belongs to which this node wraps. */
    private final long uniqueId;
    /** Whether the node is still alive and can be operated with. */
    private boolean alive;
    /** In debug mode, stores the stack at the point the node was created. */
    private StackTraceElement[] createdTrace;
    /** In debug mode, stores the stack at the point the node was disposed. */
    private StackTraceElement[] disposeTrace;
    
    DD(LibraryDD lowLevelDD, long uniqueId) {
        assert lowLevelDD != null;
        this.libraryDD = lowLevelDD;
        this.context = lowLevelDD.getContextDD();
        this.uniqueId = uniqueId;
        if (this.context.isDebugDD()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            this.createdTrace = trace.clone();
        }
        this.alive = true;
    }
    
    @Override
    public String toString() {
        assert alive() : alreadyDeadMessage();
        return getContext().toString(this);
    }
    
    /**
     * Get the context to which this node belongs.
     * 
     * @return context to which this node belongs
     */
    public ContextDD getContext() {
        return context;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert alive() : alreadyDeadMessage();
        if (!(obj instanceof DD)) {
            return false;
        }
        DD other = (DD) obj;
        return libraryDD.equals(uniqueId, other.uniqueId());
    }
    
    @Override
    public final int hashCode() {
        assert alive() : alreadyDeadMessage();
        return libraryDD.hashCode(uniqueId);
    }
    
    long uniqueId() {
        return uniqueId;
    }
    
    /**
     * Checks whether this node is still alive and may be used.
     * The node is alive if it has not been disposed and its context has not
     * yet been closed.
     * 
     * @return whether this node is still alive and may be used
     */
    public boolean alive() {
        return getContext().alive() && alive;
    }
    
    public LibraryDD getLowLevel() {
        return libraryDD;
    }
    
    @Override
    public DD clone() {
        assert alive() : alreadyDeadMessage();
        return libraryDD.getContextDD().cloneDD(this);
    }
    
    /**
     * Dispose the DD node.
     * After disposing the node, it may not be longer be operated with, that is,
     * {@code apply} operations etc. are all illegal on this node. All DD nodes
     * created must finally be disposed using this function. A node must not be
     * disposed more than once.
     */
    public void dispose() {
        assert alive : alreadyDeadMessage();
        alive = false;
        getContext().removeDD(this);
        if (getContext().isDebugDD()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            this.disposeTrace = trace.clone();
        }
    }
    
    /**
     * Checks whether the node has not been disposed.
     * The function does still return true if the node has not been disposed but
     * its context has already been closed. It is intended for internal usage.
     * 
     * @return whether the node has not been disposed
     */
    boolean internalAlive() {
        return alive;
    }
    
    String buildCreateTraceString() {
        assert createdTrace != null;
        return Util.stackTraceToString(createdTrace);
    }
    
    String buildCloseTraceString() {
        assert disposeTrace != null;
        return Util.stackTraceToString(disposeTrace);
    }

    TIntSet support() {
        assert alive() : alreadyDeadMessage();
        return getContext().support(this);
    }
    
    public Type getType() {
        return getContext().getType(this);
    }
    
    private String alreadyDeadMessage() {
        if (getContext().isDebugDD()) {
            return "DD already closed. Allocated at\n"
                    + buildCreateTraceString() + "\nPreviously closed at\n"
                    + buildCloseTraceString();
        } else {
            return ContextDD.WRONG_USAGE_NO_DEBUG;
        }
    }

    public DD apply(Operator operator) throws EPMCException {
        assert operator != null;
        return getContext().apply(operator, this);
    }
    
    public DD apply(DD other, Operator Operator) throws EPMCException {
        return getContext().apply(Operator, this, other);
    }

    public DD apply(DD other1, DD other2, Operator Operator) throws EPMCException {
        return getContext().apply(Operator, this, other1, other2);
    }
    
    public DD add(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorAdd.IDENTIFIER));
    }

    public DD multiply(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorMultiply.IDENTIFIER));
    }

    public DD subtract(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorSubtract.IDENTIFIER));
    }

    public DD subtractWith(DD other) throws EPMCException {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        DD result = subtract(other);
        dispose();
        other.dispose();
        return result;
    }
    
    public DD divide(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorDivide.IDENTIFIER));
    }

    public DD divideWith(DD other) throws EPMCException {
        DD result = apply(other, ContextValue.get().getOperator(OperatorDivide.IDENTIFIER));
        dispose();
        other.dispose();
        return result;
    }

    public DD eq(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorEq.IDENTIFIER));
    }

    public DD gt(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorGt.IDENTIFIER));
    }

    public DD gtWith(DD other) throws EPMCException {
        DD result = apply(other, ContextValue.get().getOperator(OperatorGt.IDENTIFIER));
        dispose();
        other.dispose();
        return result;
    }

    public DD ge(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorGe.IDENTIFIER));
    }

    public DD ne(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorNe.IDENTIFIER));
    }

    public DD neWith(DD other) throws EPMCException {
        DD result = apply(other, ContextValue.get().getOperator(OperatorNe.IDENTIFIER));
        dispose();
        other.dispose();
        return result;
    }

    public DD geWith(DD other) throws EPMCException {
        DD result = ge(other);
        other.dispose();
        dispose();
        return result;
    }
    
    public DD lt(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorLt.IDENTIFIER));
    }

    public DD le(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorLe.IDENTIFIER));
    }
    
    public DD leWith(DD other) throws EPMCException {
        DD result = le(other);
        other.dispose();
        dispose();
        return result;
    }

    public DD and(DD other) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert isBoolean() : this;
        assert assertValidDD(other);
        assert other.isBoolean();
        return apply(other, ContextValue.get().getOperator(OperatorAnd.IDENTIFIER));
    }
    
    public DD and(DD... others) throws EPMCException {
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.and(other);
            resultOld.dispose();
        }
        return result;
    }

    public DD andWith(DD... others) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
            assert other.alive() : other.alreadyDeadMessage();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.and(other);
            resultOld.dispose();
            other.dispose();
        }
        dispose();
        
        return result;
    }

    public DD orWith(DD... others) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
            assert other.alive() : other.alreadyDeadMessage();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.or(other);
            resultOld.dispose();
            other.dispose();
        }
        dispose();
        
        return result;
    }

    public DD or(DD... others) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
            assert other.alive() : other.alreadyDeadMessage();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.or(other);
            resultOld.dispose();
        }
        
        return result;
    }

    public DD add(DD... others) throws EPMCException {
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.add(other);
            resultOld.dispose();
        }
        return result;
    }

    public DD addWith(DD... others) throws EPMCException {
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.add(other);
            resultOld.dispose();
        }
        dispose();
        for (DD other : others) {
            other.dispose();
        }
        return result;
    }

    public DD multiply(DD... others) throws EPMCException {
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.multiply(other);
            resultOld.dispose();
        }
        return result;
    }

    public DD or(DD other) throws EPMCException {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        return apply(other, ContextValue.get().getOperator(OperatorOr.IDENTIFIER));
    }
    
    public DD orWith(DD other) throws EPMCException {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        DD result = or(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD maxWith(DD other) throws EPMCException {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        DD result = max(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD andWith(DD other) throws EPMCException {
        DD result = and(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD abstractAndExistWith(DD other, DD cube) throws EPMCException {
        DD result = abstractAndExist(other, cube);
        this.dispose();
        other.dispose();
        cube.dispose();
        return result;
    }    

    public DD multiplyWith(DD other) throws EPMCException {
        DD result = multiply(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD abstractExistWith(DD other) throws EPMCException {
        DD result = abstractExist(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD abstractMinWith(DD other) throws EPMCException {
        DD result = abstractMin(other);
        this.dispose();
        other.dispose();
        return result;
    }

    public DD abstractMaxWith(DD other) throws EPMCException {
        DD result = abstractMax(other);
        this.dispose();
        other.dispose();
        return result;
    }

    public DD notWith() throws EPMCException {
        DD result = not();
        this.dispose();
        return result;
    }    

    public DD andNotWith(DD other) throws EPMCException {
        DD result = andNot(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD eqWith(DD other) throws EPMCException {
        DD result = eq(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD addWith(DD other) throws EPMCException {
        DD result = add(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD modWith(DD other) throws EPMCException {
        DD result = mod(other);
        this.dispose();
        other.dispose();
        return result;
    }    

    public DD not() throws EPMCException {
        return apply(ContextValue.get().getOperator(OperatorNot.IDENTIFIER));
    }

    public DD iff(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorIff.IDENTIFIER));
    }

    public DD iffWith(DD other) throws EPMCException {
        DD result = iff(other);
        dispose();
        other.dispose();
        
        return result;
    }

    public DD implies(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorImplies.IDENTIFIER));
    }

    public DD impliesWith(DD andEx) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert isBoolean();
        assert assertValidDD(andEx);
        assert andEx.isBoolean();
        DD result = implies(andEx);
        dispose();
        andEx.dispose();
        return result;
    }

    public DD ceil() throws EPMCException {
        return apply(ContextValue.get().getOperator(OperatorCeil.IDENTIFIER));
    }

    public DD floor() throws EPMCException {
        return apply(ContextValue.get().getOperator(OperatorFloor.IDENTIFIER));
    }

    public DD log(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorLog.IDENTIFIER));
    }

    public DD max(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorMax.IDENTIFIER));
    }

    public DD min(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorMin.IDENTIFIER));
    }

    public DD mod(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorMod.IDENTIFIER));
    }

    public DD pow(DD other) throws EPMCException {
        return apply(other, ContextValue.get().getOperator(OperatorPow.IDENTIFIER));
    }
    public DD ite(DD thenNode, DD elseNode) throws EPMCException {
        return apply(thenNode, elseNode, ContextValue.get().getOperator(OperatorIte.IDENTIFIER));
    }

    public DD iteWith(DD constValDD, DD singleDD) throws EPMCException {
        DD result = ite(constValDD, singleDD);
        dispose();
        constValDD.dispose();
        singleDD.dispose();
        return result;
    }

    public DD abstractExist(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractExist(this, cube);
    }
    
    public DD abstractExist(DD... cubes) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cubes != null;
        for (DD cube : cubes) {
            assert cube != null;
            assert cube.alive() : cube.alreadyDeadMessage();
        }
        DD allCubes = getContext().newConstant(true);
        for (DD cube : cubes) {
            allCubes = allCubes.andWith(cube.clone());
        }
        DD result = abstractExist(allCubes);
        allCubes.dispose();
        return result;
    }
    
    public DD abstractExistWith(DD... cubes) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cubes != null;
        for (DD cube : cubes) {
            assert cube != null;
            assert cube.alive() : alreadyDeadMessage();
        }
        DD result = abstractExist(cubes);
        dispose();
        for (DD cube : cubes) {
            cube.dispose();
        }
        return result;
    }

    public DD abstractForall(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractForall(this, cube);
    }

    public DD abstractSum(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractSum(this, cube);
    }
    
    public DD abstractSumWith(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        DD result = abstractSum(cube);
        cube.dispose();
        dispose();        
        return result;
    }

    public DD abstractProduct(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractProduct(this, cube);
    }

    public DD abstractMax(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractMax(this, cube);
    }

    public DD abstractMin(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractMin(this, cube);
    }
    
    public DD abstractAndExist(DD other, DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert other != null;
        assert cube != null;
        return getContext().abstractAndExist(this, other, cube);
    }

    /* other public functions */
    
    public boolean isLeaf() {
        assert alive() : alreadyDeadMessage();
        return getContext().isLeaf(this);
    }
    
    public Value value() {
        assert alive() : alreadyDeadMessage();
        return getContext().getValue(this);
    }

    public int variable() {
        assert alive() : alreadyDeadMessage();
        return getContext().variable(this);
    }
    
    public DD permute(Permutation permutation) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert permutation != null;
        return getContext().permute(this, permutation);
    }

    public DD permuteWith(Permutation permutation) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert permutation != null;
        DD result = getContext().permute(this, permutation);
        dispose();
        return result;
    }

    public BigInteger countSat(DD variablesCube) {
        assert alive() : alreadyDeadMessage();
        return getContext().countSat(this, variablesCube);
    }

    public BigInteger countSatWith(DD variablesCube) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(variablesCube);
        BigInteger result = getContext().countSat(this, variablesCube);
        variablesCube.dispose();
        dispose();
        return result;
    }

    public BigInteger countNodes() {
        assert alive() : alreadyDeadMessage();
        return getContext().countNodes(this);
    }

    public int countNodesInt() {
        assert alive() : alreadyDeadMessage();
        return getContext().countNodes(this).intValue();
    }

    public DD findSat(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().findSat(this, cube);
    }

    public DD findSatWith(DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        DD result = findSat(cube);
        dispose();
        cube.dispose();
        return result;
    }

    public boolean isFalse() {
        assert alive() : alreadyDeadMessage();
        return isLeaf() && ValueBoolean.isFalse(value());
    }
    
    public boolean isFalseWith() {
        assert alive() : alreadyDeadMessage();
        boolean result = isFalse();
        dispose();
        return result;
    }

    public boolean isTrue() {
        assert alive() : alreadyDeadMessage();
        return isLeaf() && ValueBoolean.isTrue(value());
    }
    
    public boolean isTrueWith() {
        assert alive() : alreadyDeadMessage();
        boolean result = isLeaf() && ValueBoolean.isTrue(value());
        dispose();
        return result;
    }
    
    public Set<VariableDD> highLevelSupport() {
        assert alive() : alreadyDeadMessage();
        return getContext().highLevelSupport(this);
    }
    
    public Walker walker(boolean autoComplement) {
        assert alive() : alreadyDeadMessage();
        return getContext().walker(this, autoComplement);
    }
    
    public SupportWalker supportWalker(DD support, boolean stopAtFalse, boolean stopAtZero) {
        assert alive() : alreadyDeadMessage();
        assert support != null;
        assert support.getContext() == getContext();
        assert support.assertCube();
        return getContext().supportWalker(this, support, stopAtFalse, stopAtZero);
    }
    
    public SupportWalker supportWalker(DD support) {
        assert alive() : alreadyDeadMessage();
        assert support != null;
        assert support.getContext() == getContext();
        assert support.assertCube();
        return getContext().supportWalker(this, support);
    }
    
    public SupportWalker supportWalker(DD support, Collection<Value> stopWhere) {
        assert alive() : alreadyDeadMessage();
        assert support != null;
        assert support.getContext() == getContext();
        assert support.assertCube();
        return getContext().supportWalker(this, support, stopWhere);
    }
    
    public SupportWalker supportWalkerWith(DD support) {
        SupportWalker result = supportWalker(support);
        dispose();
        return result;
    }

    public SupportWalker supportWalker() throws EPMCException {
        assert alive() : alreadyDeadMessage();
        DD support = supportDD();
        SupportWalker result = getContext().supportWalker(this, support);
        support.dispose();
        return result;
    }
    
    
    public Walker walker() {
        assert alive() : alreadyDeadMessage();
        return getContext().walker(this, true);        
    }

    public DD andNot(DD scc) throws EPMCException {
        DD sccNot = scc.not();
        DD result = and(sccNot);
        sccNot.dispose();
        return result;
    }

    public boolean isComplement() {
        return getContext().isComplement(this);
    }
    
    /* internal functions */
    
    public DD toMT(Value forTrue, Value forFalse) throws EPMCException {
        return getContext().toMT(this, forTrue, forFalse);
    }
    
    public DD toMT(int forTrue, int forFalse) throws EPMCException {
        return getContext().toMT(this, forTrue, forFalse);
    }
    
    public DD toMTWith(int forTrue, int forFalse) throws EPMCException {
        DD result = toMT(forTrue, forFalse);
        dispose();
        return result;
    }

    public DD toMTWith(Value forTrue, Value forFalse) throws EPMCException {
        DD result = getContext().toMT(this, forTrue, forFalse);
        dispose();
        return result;
    }

    public DD toMT() throws EPMCException {
        return getContext().toInt(this);
    }

    public DD toMTWith()  throws EPMCException {
        DD result = toMT();
        dispose();
        return result;
    }
    
    public void printSupport() throws EPMCException {
        getContext().printSupport(this);
    }

    public String supportString() throws EPMCException {
        return getContext().supportString(this);
    }

    public DD abstractExist(Iterable<DD> nextVars) throws EPMCException {
        return getContext().abstractExist(this, nextVars);
    }

 // TODO get rid of dependency to TIntSet
    public TIntSet findSatSet(DD cube) throws EPMCException {
        return getContext().findSatSet(this, cube);
    }

    public DD divide(int intValue) throws EPMCException {
        return getContext().divide(this, intValue);
    }

    public Value applyOverSat(Operator operator, DD sat)
            throws EPMCException {
        assert operator != null;
        assert assertValidDD(sat);
        assert TypeBoolean.isBoolean(sat.getType());
        return getContext().applyOverSat(operator, this, sat);
    }

    public Value applyOverSat(Operator operator, DD support, DD sat)
            throws EPMCException {
        assert operator != null;
        assert assertValidDD(sat);
        assert TypeBoolean.isBoolean(sat.getType());
        return getContext().applyOverSat(operator, this, support, sat);
    }

    public Value maxOverSat(DD sat)
            throws EPMCException {
        return getContext().maxOverSat(this, sat);
    }

    public Value minOverSat(DD sat)
            throws EPMCException {
        return getContext().minOverSat(this, sat);
    }
    
    public DD supportDD() throws EPMCException {
        return getContext().supportDD(this);
    }
    
    public boolean assertCube() {
        return getContext().assertCube(this);
    }
    
    public DD xor(DD other) throws EPMCException {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive();
        return apply(other, ContextValue.get().getOperator(OperatorNe.IDENTIFIER));
    }

    public DD xor(DD... others) throws EPMCException {
        assert others != null;
        for (DD other : others) {
            assert other != null;
            assert other.getContext() == getContext();
        }
        DD result = this.clone();
        for (DD other : others) {
            DD resultOld = result;
            result = result.xor(other);
            resultOld.dispose();
        }
        return result;
    }
    
    public boolean isBoolean() {
        assert alive() : alreadyDeadMessage();
        return getContext().isBoolean(this);
    }

    public boolean assertValidDD(DD dd) {
        assert dd != null;
        assert dd.alive() : dd.alreadyDeadMessage();
        return true;
    }
    
    public Value getSomeLeafValue() {
        assert alive() : alreadyDeadMessage();
        return getContext().getSomeLeafValue(this);
    }

    public Value getSomeLeafValue(DD sat) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(sat);
        assert sat.isBoolean();
        return getContext().getSomeLeafValue(this, sat);
    }

    public Value andOverSat(DD sat) throws EPMCException {
        return getContext().andOverSat(this, sat);
    }

    public Value orOverSat(DD sat) throws EPMCException {
        return getContext().orOverSat(this, sat);
    }

    public DD gt(int i) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        DD constant = getContext().newConstant(i);
        DD result = gt(constant);
        constant.dispose();
        return result;
    }

    public DD divideIgnoreZero(DD other) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        return apply(other, ContextValue.get().getOperator(OperatorDivideIgnoreZero.IDENTIFIER));
    }

    public DD divideIgnoreZeroWith(DD other) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        DD result = divideIgnoreZero(other);
        dispose();
        other.dispose();
        return result;
    }

    
    public DD multiply(int value) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        DD constant = getContext().newConstant(value);
        DD result = multiply(constant);
        constant.dispose();
        return result;
    }

    public DD multiplyWith(int value) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        DD result = multiply(value);
        dispose();
        return result;
    }

    public DD ite(int value, DD encoding) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(encoding);
        DD constant = getContext().newConstant(value);
        DD result = ite(value, constant);
        constant.dispose();
        return result;
    }

    public DD multiplyWith(DD... others) throws EPMCException {
        DD result = multiply(others);
        for (DD other : others) {
            other.dispose();
        }
        dispose();
        return result;
    }

    public boolean isSubset(DD other) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        return andNot(other).isFalseWith();
    }
    
    public boolean isSubsetWith(DD other)  throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        boolean result = andNot(other).isFalseWith();
        other.dispose();
        dispose();
        return result;
    }

    public boolean isDisjoint(DD other) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        return and(other).isFalseWith();
    }

    public DD andNot(List<DD> dds) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert isBoolean();
        assert dds != null;
        for (DD dd : dds) {
            assert assertValidDD(dd);
            assert dd.isBoolean();
        }
        DD result = this.clone();
        for (DD dd : dds) {
            result = result.andNotWith(dd.clone());
        }
        return result;
    }

    public DD andNot(DD... dds) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert isBoolean();
        assert dds != null;
        for (DD dd : dds) {
            assert assertValidDD(dd);
            assert dd.isBoolean();
        }
        DD result = this.clone();
        for (DD dd : dds) {
            result = result.andNotWith(dd.clone());
        }
        return result;
    }

    public void collectValues(Set<Value> values, DD sat) throws EPMCException {
        getContext().collectValues(values, this, sat);
    }
    
    public boolean assertSupport(DD... support) {
        return getContext().assertSupport(this, support);
    }

    public DD abstractImpliesForall(DD other, DD cube) throws EPMCException {
        assert alive() : alreadyDeadMessage();
        assert other != null;
        assert cube != null;
        return getContext().abstractImpliesForall(this, other, cube);        
    }

}
