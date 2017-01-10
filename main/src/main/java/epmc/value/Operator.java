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

package epmc.value;

import epmc.error.EPMCException;

// TODO change to using builder pattern such that the value context cannot be
// changed after creation

/**
 * Operator on values.
 * Classes implementing this interface are used to manipulate values by
 * performing a certain operation such as addition, multiplication, logical AND,
 * etc. on a several operand values. The most important method of operators is
 * {@link #apply(Value, Value...)}, which performs the actual operation of the
 * operator. For efficiency, so as to avoid creating new objects frequently, the
 * method operates by changing the result value rather than creating and
 * returning a new one.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Operator {
    /**
     * Get the identifier of this operator.
     * The identifier is a user-readable string used e.g. if using the operator
     * object directly is not possible, or for debugging.
     * 
     * @return identifier of this operator
     */
    String getIdentifier();

    /**
     * Set the value context of the operator.
     * This function should only be used once immediately after the creation
     * of the operator.
     * 
     * @param context value context to set for the operator
     */
    void setContext(ContextValue context);
    
    /**
     * Get the value context of the operator.
     * 
     * @return value context of the operator
     */
    ContextValue getContext();

    /**
     * Apply the operator.
     * The type of the result value is computed by calling
     * {@link #resultType(Value...)} on the operands parameter of this function.
     * If {@link #resultType(Value...)} returns {@code null}, then the operator
     * cannot be applied on the given operands.
     * The result parameter must be a such that a call to the method
     * {@link Type#canImport(Type)} of this type on the result parameter
     * returns {@code true}.
     * Note that for efficiency it might be worthwhile using a preallocated
     * array as the operands parameter, so as to avoid that a new array is
     * created each time the method is called.
     * None of the parameters may be {@code null}. The length of the operands
     * parameter array needs not match exactly the arity of the operator.
     * The first few entries of the operands array needed according to the arity
     * of the operator also must not be {@code null}, while the following array
     * entries may be {@code null}. 
     * 
     * @param result will be assigned the result of the operation
     * @param operands operands of the operation
     * @throws EPMCException thrown in case of problems performing operation
     */
    void apply(Value result, Value... operands) throws EPMCException;

    /**
     * Computes the result type.
     * The result type is the type the result parameter of
     * {@link #apply(Value, Value...)} should have when applying it on operand
     * parameters of the given types.
     * None of the parameters may be {@code null}. The length of the types
     * parameter array needs not match exactly the arity of the operator.
     * The first few entries of the types array needed according to the arity of
     * the operator also must not be {@code null}, while the following array
     * entries may be {@code null}. If this method returns {@code null}, it
     * means that the operator cannot be applied on the according operand
     * values.
     * 
     * @param types types of operands on which apply method be called
     * @return type the result parameter of the apply method should have
     */
    Type resultType(Type... types);
    
    /**
     * Compute the result type.
     * The result is computed by collecting the types of the operands parameter
     * and calling {@link #resultType(Type...)}.
     * 
     * @param operands operands to compute result type of
     * @return result type
     */
    default Type resultType(Value... operands) {
        assert operands != null;
        Type[] types = new Type[operands.length];
        for (int typeNr = 0; typeNr < operands.length; typeNr++) {
            Value value = operands[typeNr];
            if (value != null) {
                types[typeNr] = operands[typeNr].getType();
            }
        }
        return resultType(types);
    }
}
