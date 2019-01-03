package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.qmc.operator.OperatorKronecker;
import epmc.qmc.operator.OperatorSuperOperatorList;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSuperOperatorList implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorSuperOperatorList.SUPEROPERATOR_LIST) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorSuperOperatorList(this);
        }
    }

    private final TypeComplex typeComplex;
    private final TypeMatrix typeMatrix;
    private final ValueComplex zeroComplex;
    private final ValueComplex accComplex;
    private final ValueComplex accComplex2;
    private final ValueMatrix importedMatrix;
    private final ValueArray importedList;
    private final ValueMatrix tensor;
    private final ValueMatrix left;
    private final ValueMatrix right;
    private final ValueMatrix accMatrix;
    private final ValueMatrix one;
    private final ValueMatrix check;
    private final OperatorEvaluator subtract;
    private final OperatorEvaluator evaluator;
    private final OperatorEvaluator add;
    
    private OperatorEvaluatorSuperOperatorList(Builder builder) {
        typeComplex = TypeComplex.get();
        typeMatrix = TypeMatrix.get(typeComplex);
        zeroComplex = UtilValue.newValue(typeComplex, 0);
        accComplex = typeComplex.newValue();
        accComplex2 = typeComplex.newValue();
        importedMatrix = typeMatrix.newValue();
        importedList = typeMatrix.getTypeArray().newValue();
        tensor = typeMatrix.newValue();
        left = typeMatrix.newValue();
        right = typeMatrix.newValue();
        accMatrix = typeMatrix.newValue();
        one = typeMatrix.newValue();
        check = typeMatrix.newValue();
        subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, one.getType(), typeMatrix);
        evaluator = ContextValue.get().getEvaluator(OperatorKronecker.KRONECKER, left.getType(), right.getType());
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeMatrix.get(typeComplex), tensor.getType());
    }

    @Override
    public Type resultType() {
        return TypeSuperOperator.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value value : operands) {
            assert value != null;
        }
        ValueSuperOperator resultSuperOperator = (ValueSuperOperator) result;
        fromMatrixList(resultSuperOperator, ValueArray.as(operands[0]));
    }
    
    private void fromMatrixList(ValueSuperOperator superOperator, ValueArray list) {
        assert list != null;
        OperatorEvaluator setList = ContextValue.get().getEvaluator(OperatorSet.SET, list.getType(), importedList.getType());
        setList.apply(importedList, list);
        assert importedList.size() > 0;
        importedList.get(importedMatrix, 0);
        int hilbertDimension = importedMatrix.getNumRows();
        int squaredDim = hilbertDimension * hilbertDimension;

        ValueMatrix resultMatrix = superOperator.getMatrix();
        resultMatrix.setDimensions(squaredDim, squaredDim);
        tensor.setDimensions(squaredDim, squaredDim);
        int numEntries = importedList.size();
        right.setDimensions(hilbertDimension, hilbertDimension);
        for (int row = 0; row < squaredDim; row++) {
            for (int column = 0; column < squaredDim; column++) {
                resultMatrix.set(zeroComplex, row, column);
            }
        }
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
        for (int entryNr = 0; entryNr < numEntries; entryNr++) {
            importedList.get(left, entryNr);
            for (int row = 0; row < hilbertDimension; row++) {
                for (int column = 0; column < hilbertDimension; column++) {
                    left.get(accComplex2, row, column);
                    UtilValueQMC.conjugate(accComplex, accComplex2);
                    right.set(accComplex, row, column);
                }
            }
            evaluator.apply(tensor, left, right);
            add.apply(accMatrix, resultMatrix, tensor);
            set.apply(resultMatrix, accMatrix);
        }
        checkHermitian(superOperator);
    }

    private void checkHermitian(ValueSuperOperator superOperator) {
        assert superOperator != null;
        ValueMatrix array = ValueSuperOperator.prepareForEigen(superOperator, 1);
        int hilbertDimension = array.getNumRows();
        one.setDimensions(hilbertDimension, hilbertDimension);
        for (int index = 0; index < hilbertDimension; index++) {
            one.set(UtilValue.newValue(typeComplex, 1), index, index);
        }
        check.setDimensions(hilbertDimension, hilbertDimension);
        subtract.apply(check, one, array);
        ValueSuperOperator.eigenvalues(check);
    }
}
