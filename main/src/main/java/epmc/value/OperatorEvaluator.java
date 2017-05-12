package epmc.value;

import epmc.error.EPMCException;

public interface OperatorEvaluator {
	// TODO change back to Operator instead of String once restructuring done
    boolean canApply(String operator, Type... types);

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
     * Apply the operator.
     * The type of the result value is computed by calling
     * {@link #resultType(Type...)} on the types of the operands parameter of this
     * function.
     * If {@link #resultType(Type...)} returns {@code null}, then the operator
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
}
