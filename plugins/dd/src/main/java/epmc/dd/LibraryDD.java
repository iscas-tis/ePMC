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

import java.io.Closeable;

import epmc.operator.Operator;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Interface for classes providing support for a given DD library.
 * 
 * @author Ernst Moritz Hahn
 */
public interface LibraryDD extends Closeable {
    /* methods to be implemented by implementing classes */

    String getIdentifier();

    void setContextDD(ContextDD contextDD);

    ContextDD getContextDD();

    boolean canApply(Operator operation, Type resultType, long...operands);

    long apply(Operator operator, Type resultType, long... operands);

    long newConstant(Value value);

    long newVariable();

    boolean isLeaf(long dd);

    Value value(long dd);

    int variable(long dd);

    void reorder();

    void addGroup(int startVariable, int numVariables, boolean fixedOrder);

    long permute(long dd, PermutationLibraryDD permutation)
    ;

    long clone(long uniqueId);

    void free(long uniqueId);

    long getWalker(long uniqueId);

    boolean walkerIsLeaf(long dd);

    Value walkerValue(long dd);

    int walkerVariable(long dd);

    long walkerLow(long uniqueId);

    long walkerHigh(long uniqueId);

    long walkerRegular(long from);

    boolean walkerIsComplement(long node);

    long walkerComplement(long from);

    boolean isComplement(long node);

    // TODO replace by
    //    long abstractApply(long dd, long cube, Operator operator);
    //    boolean supportsAbstract(Operator operator);

    long abstractExist(long dd, long cube);

    long abstractForall(long dd, long cube);

    long abstractSum(Type type, long dd, long cube);

    long abstractProduct(Type type, long dd, long cube);

    long abstractMax(Type type, long dd, long cube);

    long abstractMin(Type type, long dd, long cube);


    long abstractAndExist(long dd1, long dd2, long cube);

    boolean hasAndExist();    

    PermutationLibraryDD newPermutation(int[] permutation);

    boolean equals(long op1, long op2);

    boolean hasInverterArcs();

    public int hashCode(long uniqueId);

    @Override
    void close();


    /* default methods */

    default boolean checkConsistent() {
        return true;
    }
}
