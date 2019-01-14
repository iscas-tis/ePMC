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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorCeil;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorDivideIgnoreZero;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMod;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorSubtract;
import epmc.util.Util;
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
    /**
     * Unique ID in the DD library the node belongs to which this node wraps.
     */
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
        return uniqueId == other.uniqueId;
//        return libraryDD.equals(uniqueId, other.uniqueId());
    }

    @Override
    public final int hashCode() {
        assert alive() : alreadyDeadMessage();
        return Long.hashCode(uniqueId);
//        return libraryDD.hashCode(uniqueId);
    }

    public long uniqueId() {
        return uniqueId;
    }

    /**
     * Checks whether this node is still alive and may be used. The node is
     * alive if it has not been disposed and its context has not yet been
     * closed.
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
        return context.cloneDD(this);
    }

    /**
     * Dispose the DD node. After disposing the node, it may not be longer be
     * operated with, that is, {@code apply} operations etc. are all illegal on
     * this node. All DD nodes created must finally be disposed using this
     * function. A node must not be disposed more than once.
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
     * Checks whether the node has not been disposed. The function does still
     * return true if the node has not been disposed but its context has already
     * been closed. It is intended for internal usage.
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

    IntOpenHashSet support() {
        assert alive() : alreadyDeadMessage();
        return getContext().support(this);
    }

    public Type getType() {
        return getContext().getType(this);
    }

    private String alreadyDeadMessage() {
        if (getContext().isDebugDD()) {
            return "DD already closed. Allocated at\n" + buildCreateTraceString() + "\nPreviously closed at\n"
                    + buildCloseTraceString();
        } else {
            return ContextDD.WRONG_USAGE_NO_DEBUG;
        }
    }

    public DD apply(Operator operator) {
        assert operator != null;
        return getContext().apply(operator, this);
    }

    public DD apply(DD other, Operator operator) {
        return getContext().apply(operator, this, other);
    }

    public DD apply(DD other1, DD other2, Operator operator) {
        return getContext().apply(operator, this, other1, other2);
    }

    // TODO get rid of this function
    public DD add(DD other) {
        return apply(other, OperatorAdd.ADD);
    }

    // TODO get rid of this function
    public DD multiply(DD other) {
        return apply(other, OperatorMultiply.MULTIPLY);
    }

    // TODO get rid of this function
    public DD subtract(DD other) {
        return apply(other, OperatorSubtract.SUBTRACT);
    }

    // TODO get rid of this function
    public DD subtractWith(DD other) {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        DD result = subtract(other);
        dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD divideWith(DD other) {
        DD result = apply(other, OperatorDivide.DIVIDE);
        dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD eq(DD other) {
        return apply(other, OperatorEq.EQ);
    }

    // TODO get rid of this function
    public DD gt(DD other) {
        return apply(other, OperatorGt.GT);
    }

    // TODO get rid of this function
    public DD gtWith(DD other) {
        DD result = apply(other, OperatorGt.GT);
        dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD ge(DD other) {
        return apply(other, OperatorGe.GE);
    }

    // TODO get rid of this function
    public DD ne(DD other) {
        return apply(other, OperatorNe.NE);
    }

    // TODO get rid of this function
    public DD neWith(DD other) {
        DD result = apply(other, OperatorNe.NE);
        dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD geWith(DD other) {
        DD result = ge(other);
        other.dispose();
        dispose();
        return result;
    }

    // TODO get rid of this function
    public DD lt(DD other) {
        return apply(other, OperatorLt.LT);
    }

    // TODO get rid of this function
    public DD le(DD other) {
        return apply(other, OperatorLe.LE);
    }

    // TODO get rid of this function
    public DD and(DD other) {
        assert alive() : alreadyDeadMessage();
        assert isBoolean() : this;
        assert assertValidDD(other);
        assert other.isBoolean();
        return apply(other, OperatorAnd.AND);
    }

    // TODO get rid of this function
    public DD and(DD... others) {
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

    // TODO get rid of this function
    public DD andWith(DD... others) {
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

    // TODO get rid of this function
    public DD orWith(DD... others) {
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

    // TODO get rid of this function
    public DD or(DD... others) {
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

    // TODO get rid of this function
    public DD add(DD... others) {
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

    // TODO get rid of this function
    public DD addWith(DD... others) {
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

    // TODO get rid of this function
    public DD multiply(DD... others) {
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

    // TODO get rid of this function
    public DD or(DD other) {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        return apply(other, OperatorOr.OR);
    }

    // TODO get rid of this function
    public DD orWith(DD other) {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive() : other.alreadyDeadMessage();
        DD result = or(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD andWith(DD other) {
        DD result = and(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD abstractAndExistWith(DD other, DD cube) {
        DD result = abstractAndExist(other, cube);
        this.dispose();
        other.dispose();
        cube.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD multiplyWith(DD other) {
        DD result = multiply(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD abstractExistWith(DD other) {
        DD result = abstractExist(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD abstractMaxWith(DD other) {
        DD result = abstractMax(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD notWith() {
        DD result = not();
        this.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD andNotWith(DD other) {
        DD result = andNot(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD eqWith(DD other) {
        DD result = eq(other);
        this.dispose();
        other.dispose();
        return result;
    }

    // TODO get rid of this function
    public DD addWith(DD other) {
        DD result = add(other);
        this.dispose();
        other.dispose();
        return result;
    }

    public DD modWith(DD other) {
        DD result = mod(other);
        this.dispose();
        other.dispose();
        return result;
    }

    public DD not() {
        return apply(OperatorNot.NOT);
    }

    public DD iff(DD other) {
        return apply(other, OperatorIff.IFF);
    }

    public DD iffWith(DD other) {
        DD result = iff(other);
        dispose();
        other.dispose();

        return result;
    }

    public DD implies(DD other) {
        return apply(other, OperatorImplies.IMPLIES);
    }

    public DD impliesWith(DD andEx) {
        assert alive() : alreadyDeadMessage();
        assert isBoolean();
        assert assertValidDD(andEx);
        assert andEx.isBoolean();
        DD result = implies(andEx);
        dispose();
        andEx.dispose();
        return result;
    }

    public DD ceil() {
        return apply(OperatorCeil.CEIL);
    }

    public DD max(DD other) {
        return apply(other, OperatorMax.MAX);
    }

    public DD min(DD other) {
        return apply(other, OperatorMin.MIN);
    }

    public DD mod(DD other) {
        return apply(other, OperatorMod.MOD);
    }

    // TODO get rid of this function
    public DD ite(DD thenNode, DD elseNode) {
        return apply(thenNode, elseNode, OperatorIte.ITE);
    }

    public DD iteWith(DD constValDD, DD singleDD) {
        DD result = ite(constValDD, singleDD);
        dispose();
        constValDD.dispose();
        singleDD.dispose();
        return result;
    }

    public DD abstractExist(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractExist(this, cube);
    }

    public DD abstractExist(DD... cubes) {
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

    public DD abstractExistWith(DD... cubes) {
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

    public DD abstractForall(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractForall(this, cube);
    }

    public DD abstractSum(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractSum(this, cube);
    }

    public DD abstractSumWith(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        DD result = abstractSum(cube);
        cube.dispose();
        dispose();
        return result;
    }

    public DD abstractProduct(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractProduct(this, cube);
    }

    public DD abstractMax(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractMax(this, cube);
    }

    public DD abstractMin(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().abstractMin(this, cube);
    }

    public DD abstractAndExist(DD other, DD cube) {
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

    public DD permute(Permutation permutation) {
        assert alive() : alreadyDeadMessage();
        assert permutation != null;
        return getContext().permute(this, permutation);
    }

    public DD permuteWith(Permutation permutation) {
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

    public DD findSat(DD cube) {
        assert alive() : alreadyDeadMessage();
        assert cube != null;
        return getContext().findSat(this, cube);
    }

    public DD findSatWith(DD cube) {
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

    public SupportWalker supportWalker() {
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

    public DD andNot(DD scc) {
        DD sccNot = scc.not();
        DD result = and(sccNot);
        sccNot.dispose();
        return result;
    }

    public boolean isComplement() {
        return getContext().isComplement(this);
    }

    /* internal functions */

    public DD toMT(Value forTrue, Value forFalse) {
        return getContext().toMT(this, forTrue, forFalse);
    }

    public DD toMT(int forTrue, int forFalse) {
        return getContext().toMT(this, forTrue, forFalse);
    }

    public DD toMTWith(int forTrue, int forFalse) {
        DD result = toMT(forTrue, forFalse);
        dispose();
        return result;
    }

    public DD toMTWith(Value forTrue, Value forFalse) {
        DD result = getContext().toMT(this, forTrue, forFalse);
        dispose();
        return result;
    }

    public DD toMT() {
        return getContext().toInt(this);
    }

    public DD toMTWith() {
        DD result = toMT();
        dispose();
        return result;
    }

    public void printSupport() {
        getContext().printSupport(this);
    }

    public String supportString() {
        return getContext().supportString(this);
    }

    public DD abstractExist(Iterable<DD> nextVars) {
        return getContext().abstractExist(this, nextVars);
    }

    // TODO get rid of dependency to TIntSet
    public IntOpenHashSet findSatSet(DD cube) {
        return getContext().findSatSet(this, cube);
    }

    public DD divide(int intValue) {
        return getContext().divide(this, intValue);
    }

    public Value applyOverSat(Operator operator, DD sat) {
        assert operator != null;
        assert assertValidDD(sat);
        assert TypeBoolean.is(sat.getType());
        return getContext().applyOverSat(operator, this, sat);
    }

    public Value applyOverSat(Operator operator, DD support, DD sat) {
        assert operator != null;
        assert assertValidDD(sat);
        assert TypeBoolean.is(sat.getType());
        return getContext().applyOverSat(operator, this, support, sat);
    }

    public Value maxOverSat(DD sat) {
        return getContext().maxOverSat(this, sat);
    }

    public Value minOverSat(DD sat) {
        return getContext().minOverSat(this, sat);
    }

    public DD supportDD() {
        return getContext().supportDD(this);
    }

    public boolean assertCube() {
        return getContext().assertCube(this);
    }

    public DD xor(DD other) {
        assert other != null;
        assert alive() : alreadyDeadMessage();
        assert other.alive();
        return apply(other, OperatorNe.NE);
    }

    public DD xor(DD... others) {
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

    public Value getSomeLeafValue(DD sat) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(sat);
        assert sat.isBoolean();
        return getContext().getSomeLeafValue(this, sat);
    }

    public Value andOverSat(DD sat) {
        return getContext().andOverSat(this, sat);
    }

    public Value orOverSat(DD sat) {
        return getContext().orOverSat(this, sat);
    }

    public DD gt(int i) {
        assert alive() : alreadyDeadMessage();
        DD constant = getContext().newConstant(i);
        DD result = gt(constant);
        constant.dispose();
        return result;
    }

    public DD divideIgnoreZero(DD other) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        return apply(other, OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO);
    }

    public DD divideIgnoreZeroWith(DD other) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        DD result = divideIgnoreZero(other);
        dispose();
        other.dispose();
        return result;
    }

    public DD multiply(int value) {
        assert alive() : alreadyDeadMessage();
        DD constant = getContext().newConstant(value);
        DD result = multiply(constant);
        constant.dispose();
        return result;
    }

    public DD multiplyWith(int value) {
        assert alive() : alreadyDeadMessage();
        DD result = multiply(value);
        dispose();
        return result;
    }

    public DD ite(int value, DD encoding) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(encoding);
        DD constant = getContext().newConstant(value);
        DD result = ite(value, constant);
        constant.dispose();
        return result;
    }

    public DD multiplyWith(DD... others) {
        DD result = multiply(others);
        for (DD other : others) {
            other.dispose();
        }
        dispose();
        return result;
    }

    public boolean isSubset(DD other) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        return andNot(other).isFalseWith();
    }

    public boolean isSubsetWith(DD other) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        boolean result = andNot(other).isFalseWith();
        other.dispose();
        dispose();
        return result;
    }

    public boolean isDisjoint(DD other) {
        assert alive() : alreadyDeadMessage();
        assert assertValidDD(other);
        assert isBoolean();
        assert other.isBoolean();
        return and(other).isFalseWith();
    }

    public DD andNot(List<DD> dds) {
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

    public DD andNot(DD... dds) {
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

    public void collectValues(Set<Value> values, DD sat) {
        getContext().collectValues(values, this, sat);
    }

    public boolean assertSupport(DD... support) {
        return getContext().assertSupport(this, support);
    }

    public DD abstractImpliesForall(DD other, DD cube) {
        assert alive() : alreadyDeadMessage();
        assert other != null;
        assert cube != null;
        return getContext().abstractImpliesForall(this, other, cube);
    }

}
