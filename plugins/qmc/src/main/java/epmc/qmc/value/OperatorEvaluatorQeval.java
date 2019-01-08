package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorKronecker;
import epmc.qmc.operator.OperatorQeval;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

// TODO restructure
public final class OperatorEvaluatorQeval implements OperatorEvaluator {
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
            if (operator != OperatorQeval.QEVAL) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            return new OperatorEvaluatorQeval(this);
        }
    }

    private OperatorEvaluatorQeval(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeMatrix.get(TypeComplex.get());
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value sop = operands[0];
        Value op = operands[1];
        if (result == sop || result == op) {
            ValueMatrix resultBuffer = (ValueMatrix) result.getType().newValue();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeMatrix.get(TypeComplex.get()), TypeMatrix.get(TypeComplex.get()));
            set.apply(result, resultBuffer);
            return;
        }
        Type typeSuperOperator;
        if (sop instanceof ValueSuperOperator) {
            typeSuperOperator = null;
        } else {
            typeSuperOperator = TypeSuperOperator.get();
        }
        TypeComplex typeComplex = TypeComplex.get();
        TypeMatrix typeMatrixComplex = TypeMatrix.get(typeComplex);
        Value qevalSuperOperator = null;
        if (typeSuperOperator != null) {
            qevalSuperOperator = typeSuperOperator.newValue();            
        }
        ValueMatrix qevalOperator = typeMatrixComplex.newValue();
        ValueMatrix qevalMaxEntangled = typeMatrixComplex.newValue();
        ValueMatrix qevalId = typeMatrixComplex.newValue();
        ValueMatrix qevalOpKronId = typeMatrixComplex.newValue();
        ValueMatrix qevalOpKronIdMultMaxEnt = typeMatrixComplex.newValue();
        ValueMatrix qevalMultLeft = typeMatrixComplex.newValue();
        ValueMatrix qevalMultRight = typeMatrixComplex.newValue();
        ValueMatrix qevalRowBra = typeMatrixComplex.newValue();
        ValueMatrix qevalColBra = typeMatrixComplex.newValue();
        ValueMatrix qevalEntryArr = typeMatrixComplex.newValue();
        Value qevalEntry = typeComplex.newValue();

        Value superOperator;
        ValueMatrix operator;
        if (ValueSuperOperator.is(sop)) {
            superOperator = sop;
        } else {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, sop.getType(), qevalSuperOperator.getType());
            set.apply(qevalSuperOperator, sop);
            superOperator = qevalSuperOperator;
        }
        if (!ValueMatrix.is(op)) {
            int hilbert = ((ValueSuperOperator) superOperator).getSuperoperatorDimensions();
            qevalOperator.setDimensions(hilbert, hilbert);
            for (int row = 0; row < hilbert; row++) {
                for (int col = 0; col < hilbert; col++) {
                    if (row == col) {
                        qevalOperator.set(op, row, col);                        
                    } else {
                        qevalOperator.set(UtilValue.newValue(typeComplex, 0), row, col);
                    }
                }
            }
            operator = qevalOperator;
        } else {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, op.getType(), qevalOperator.getType());
            set.apply(qevalOperator, op);
            operator = qevalOperator;
        }
        int dimensions = ((ValueSuperOperator) superOperator).getSuperoperatorDimensions();
        if (dimensions == -1) {
            dimensions = operator.getNumRows();
        }
        assert dimensions >= 0 : superOperator;
        qevalMaxEntangled.setDimensions(dimensions * dimensions, 1);
        for (int entry = 0; entry < dimensions * dimensions; entry++) {
            qevalMaxEntangled.set(UtilValue.newValue(typeComplex, 0), entry, 0);
        }
        ValueAlgebra one = UtilValue.newValue(typeComplex, 1);
        for (int entry = 0; entry < dimensions; entry++) {
            qevalMaxEntangled.set(one, entry + dimensions * entry, 0);
        }

        UtilValueQMC.identityMatrix(qevalId, dimensions);
        OperatorEvaluator kroneckerEval = ContextValue.get().getEvaluator(OperatorKronecker.KRONECKER, operator.getType(), qevalId.getType());
        kroneckerEval.apply(qevalOpKronId, operator, qevalId);
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeMatrix.get(typeComplex), TypeMatrix.get(typeComplex));
        multiply.apply(qevalOpKronIdMultMaxEnt, qevalOpKronId, qevalMaxEntangled);
        multiply.apply(qevalMultRight, ((ValueSuperOperator) superOperator).getMatrix(), qevalOpKronIdMultMaxEnt);
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        resultMatrix.setDimensions(dimensions, dimensions);
        for (int row = 0; row < dimensions; row++) {
            UtilValueQMC.toBaseBra(qevalRowBra, row, dimensions);
            for (int col = 0; col < dimensions; col++) {
                UtilValueQMC.toBaseBra(qevalColBra, col, dimensions);
                kroneckerEval = ContextValue.get().getEvaluator(OperatorKronecker.KRONECKER, qevalRowBra.getType(), qevalColBra.getType());
                kroneckerEval.apply(qevalMultLeft, qevalRowBra, qevalColBra);
                multiply.apply(qevalEntryArr, qevalMultLeft, qevalMultRight);
                qevalEntryArr.get(qevalEntry, 0, 0);
                resultMatrix.set(qevalEntry, row, col);
            }
        }
    }
}
