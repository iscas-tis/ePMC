package epmc.qmc.value;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueReal;

// TODO most functionality still here to be moved to OperatorEvaluators
public final class ValueSuperOperator implements ValueAlgebra {
    private final static String SOP = "sop(%s)";
    private final ValueMatrix matrix;
    private final ValueReal valueReal;
    private final ValueReal zeroReal;
    private final ValueComplex accComplex;
    private final ValueComplex accComplex2;
    private final ValueComplex accComplex3;
    private final ValueComplex zeroComplex;
    private final TypeSuperOperator type;

    public static boolean is(Value value) {
        return value instanceof ValueSuperOperator;
    }

    public static ValueSuperOperator as(Value value) {
        if (is(value)) {
            return (ValueSuperOperator) value;
        } else {
            return null;
        }
    }

    ValueSuperOperator(TypeSuperOperator type, ValueMatrix matrix) {
        this.type = type;
        assert matrix != null;
        this.matrix = matrix;
        this.accComplex = getTypeComplex().newValue();
        this.accComplex2 = getTypeComplex().newValue();
        this.accComplex3 = getTypeComplex().newValue();
        this.zeroComplex = UtilValue.newValue(getTypeComplex(), 0);
        this.valueReal = TypeReal.get().newValue();
        this.zeroReal = UtilValue.newValue(TypeReal.get(), 0);
    }

    ValueSuperOperator(TypeSuperOperator type) {
        this(type, type.getTypeMatrix().newValue());
    }

    ValueSuperOperator(ValueSuperOperator other) {
        this(other.getType(), UtilValue.clone(other.matrix));
    }

    @Override
    public TypeSuperOperator getType() {
        return type;
    }

    private TypeComplex getTypeComplex() {
        return getType().getTypeComplex();
    }

    @Override
    public ValueSuperOperator clone() {
        return new ValueSuperOperator(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueSuperOperator)) {
            return false;
        }
        ValueSuperOperator other = (ValueSuperOperator) obj;
        return this.matrix.equals(other.matrix);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = matrix.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return String.format(SOP, matrix.toString());
    }

    public ValueMatrix getMatrix() {
        return matrix;
    }

    public int getSuperoperatorDimensions() {
        if (isDimensionsUnspecified()) {
            return -1;
        } else {
            return (int) Math.sqrt(matrix.getNumRows());
        }
    }

    public void setDimensions(int dim) {
        assert dim == TypeSuperOperator.DIMENSIONS_UNSPECIFIED || dim >= 0;
        if (dim == TypeSuperOperator.DIMENSIONS_UNSPECIFIED) {
            matrix.setDimensionsUnspecified();
        } else {
            matrix.setDimensions(dim * dim, dim * dim);
        }
    }

    @Override
    public void set(int value) {
        matrix.set(value);
    }

    public boolean isOne() {
        ValueSuperOperator one = getType().newValue();
        ValueMatrix oneMatrix = one.getMatrix();
        for (int entry = 0; entry < oneMatrix.getNumRows(); entry++) {
            oneMatrix.set(UtilValue.newValue(getTypeComplex(), 1), entry, entry);
        }
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, getType(), one.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        eq.apply(cmp, this, one);
        return cmp.getBoolean();
    }

    private void maxEigenDiff(ValueReal result, Value operand) {
        assert operand != null;
        if (is(operand)) {
            maxEigenDiffSuperOperator(result, as(operand));
        } else if (ValueAlgebra.is(operand)) {
            maxEigenDiffAlgebra(result, ValueAlgebra.as(operand));        	
        } else {
            assert false;
        }
    }

    private void maxEigenDiffSuperOperator(ValueReal result, ValueSuperOperator operand) {
        assert result != null;
        assert operand != null;
        int defaultHilbert = 1;
        if (getMatrix().isDimensionsUnspecified() && !operand.isDimensionsUnspecified()) {
            defaultHilbert = operand.getSuperoperatorDimensions();
        } else if (!getMatrix().isDimensionsUnspecified() && operand.isDimensionsUnspecified()) {
            defaultHilbert = getSuperoperatorDimensions();    		
        }
        ValueMatrix preparedOp1 = prepareForEigen(this, defaultHilbert);
        ValueMatrix preparedOp2 = prepareForEigen(operand, defaultHilbert);
        int hilbertDimension = preparedOp1.getNumRows();
        ValueMatrix subtracted = getTypeMatrix().newValue();
        subtracted.setDimensions(hilbertDimension, hilbertDimension);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, preparedOp1.getType(), preparedOp2.getType());
        subtract.apply(subtracted, preparedOp1, preparedOp2);

        ValueReal[] eigen = eigenvalues(subtracted);
        result.set(0);
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, result.getType(), result.getType());
        for (Value value : eigen) {
            max.apply(result, result, value);
        }
    }

    public static void maxEigenDiff(ValueReal result, ValueSuperOperator operand1, ValueSuperOperator operand2) {
        assert result != null;
        assert operand2 != null;
        int defaultHilbert = 1;
        if (operand1.getMatrix().isDimensionsUnspecified() && !operand2.isDimensionsUnspecified()) {
            defaultHilbert = operand2.getSuperoperatorDimensions();
        } else if (!operand1.getMatrix().isDimensionsUnspecified() && operand2.isDimensionsUnspecified()) {
            defaultHilbert = operand1.getSuperoperatorDimensions();          
        }
        ValueMatrix preparedOp1 = prepareForEigen(operand1, defaultHilbert);
        ValueMatrix preparedOp2 = prepareForEigen(operand2, defaultHilbert);
        
        int hilbertDimension = preparedOp1.getNumRows();
        ValueMatrix subtracted = operand1.getTypeMatrix().newValue();
        subtracted.setDimensions(hilbertDimension, hilbertDimension);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, preparedOp1.getType(), preparedOp2.getType());
        subtract.apply(subtracted, preparedOp1, preparedOp2);

        ValueReal[] eigen = eigenvalues(subtracted);
        OperatorEvaluator set = ContextValue.get().getEvaluatorOrNull(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(result, UtilValue.newValue(result.getType(), UtilValue.NEG_INF));
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, result.getType(), result.getType());
        for (Value value : eigen) {
            max.apply(result, result, value);
        }
    }

    
    private void maxEigenDiffAlgebra(ValueReal result, ValueAlgebra operand) {
        assert result != null;
        assert operand != null;
        ValueMatrix preparedOp1 = prepareForEigen(this, getSuperoperatorDimensions());
        // TODO check!
        ValueMatrix preparedOp2 = getTypeMatrix().newValue();
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, operand.getType(), preparedOp2.getType());
        set.apply(preparedOp2, operand);
        int hilbertDimension = preparedOp1.getNumRows();
        ValueMatrix subtracted = getTypeMatrix().newValue();
        subtracted.setDimensions(hilbertDimension, hilbertDimension);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, preparedOp1.getType(), preparedOp2.getType());
        subtract.apply(subtracted, preparedOp1, preparedOp2);

        ValueReal[] eigen = eigenvalues(subtracted);
        result.set(0);
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, result.getType(), result.getType());
        for (Value value : eigen) {
            max.apply(result, result, value);
        }
    }

    public boolean isLt(Value operand) {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        lt.apply(cmp, valueReal, zeroReal);
        return cmp.getBoolean();
    }

    public boolean isLe(Value operand) {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        OperatorEvaluator le = ContextValue.get().getEvaluator(OperatorLe.LE, valueReal.getType(), zeroReal.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        le.apply(cmp, valueReal, zeroReal);
        return cmp.getBoolean();
    }

    public boolean isGt(Value operand) {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        gt.apply(cmp, valueReal, zeroReal);
        return cmp.getBoolean();
    }

    public boolean isGe(Value operand) {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        OperatorEvaluator ge = ContextValue.get().getEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        ge.apply(cmp, valueReal, zeroReal);
        return cmp.getBoolean();
    }

    public static ValueMatrix prepareForEigen(ValueSuperOperator operator, int defaultHilbert) {
        assert operator != null;
        if (defaultHilbert == -1) {
            defaultHilbert = 1;
        }
        if (operator.getMatrix().isDimensionsUnspecified()) {
            ValueMatrix result = operator.getTypeMatrix().newValue();
            result.setDimensions(defaultHilbert, defaultHilbert);
            operator.getMatrix().getValues().get(operator.accComplex, 0);
            for (int index = 0; index < defaultHilbert; index++) {
                result.set(operator.accComplex, index, index);
            }
            return result;
        } else {
            int matDim = operator.getMatrix().getNumRows();
            int hilbertDimension = (int) Math.sqrt(matDim);
            ValueMatrix result = operator.getTypeMatrix().newValue();
            result.setDimensions(hilbertDimension, hilbertDimension);
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeComplex.get(), TypeComplex.get());
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeComplex.get(), TypeComplex.get());
            for (int targetRow = 0; targetRow < hilbertDimension; targetRow++) {
                for (int targetColumn = 0; targetColumn < hilbertDimension; targetColumn++) {
                    set.apply(operator.accComplex, operator.zeroComplex);
                    for (int inner = 0; inner < hilbertDimension; inner++) {
                        int origRow = inner * hilbertDimension + inner;
                        int origColumn = targetColumn * hilbertDimension + targetRow;
                        operator.getMatrix().get(operator.accComplex2, origRow, origColumn);
                        add.apply(operator.accComplex3, operator.accComplex, operator.accComplex2);
                        set.apply(operator.accComplex, operator.accComplex3);
                    }
                    result.set(operator.accComplex, targetRow, targetColumn);
                }
            }
            return result;
        }
    }

    static ValueReal[] eigenvalues(ValueMatrix matrix) {
        assert matrix != null;
        return Eigen.eigenvalues(matrix);
    }

    private TypeMatrix getTypeMatrix() {
        return getType().getTypeMatrix();
    }

    // TODO still needed?
    void adjustDimensions(Value... operands) {
        if (isDimensionsUnspecified()) {
            int dim = TypeSuperOperator.DIMENSIONS_UNSPECIFIED;
            for (Value value : operands) {
                if (value instanceof ValueSuperOperator) {
                    ValueSuperOperator valueSuper = (ValueSuperOperator) value;
                    if (!isDimensionsUnspecified()) {
                        dim = valueSuper.getSuperoperatorDimensions();
                    }
                }
            }
            if (dim != TypeSuperOperator.DIMENSIONS_UNSPECIFIED) {
                this.matrix.setDimensions(dim * dim, dim * dim);
            }
        }
    }

    public boolean isDimensionsUnspecified() {
        return matrix.isDimensionsUnspecified();
    }
}
