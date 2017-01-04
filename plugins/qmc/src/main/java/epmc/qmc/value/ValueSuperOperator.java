package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.qmc.error.ProblemsQMC;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueRange;
import epmc.value.ValueReal;

public final class ValueSuperOperator implements ValueAlgebra, ValueRange {
    private static final long serialVersionUID = 1L;
    private static final int NUM_IMPORT_VALUES = 2;
    private final ValueArrayAlgebra matrix;
    private final ValueReal valueReal;
    private final ValueReal zeroReal;
    private final ValueComplex accComplex;
    private final ValueComplex accComplex2;
    private final ValueComplex accComplex3;
    private final ValueComplex zeroComplex;
    private final ValueArray importedMatrix;
    private final ValueArray importedList;
    private final ValueArray tensor;
    private final ValueArray left;
    private final ValueArray right;
    private final ValueArrayAlgebra accMatrix;
    private final ValueSuperOperator importSuperOperators[]
            = new ValueSuperOperator[NUM_IMPORT_VALUES];
    private boolean unspecifiedDim;
    private final TypeSuperOperator type;
    private boolean immutable;

    ValueSuperOperator(TypeSuperOperator type, ValueArray matrix) {
        this.type = type;
        assert matrix != null;
        assert matrix.getNumDimensions() == 2;
        assert type.getSuperoperatorDimensions() * type.getSuperoperatorDimensions() == matrix.getDimensions()[0]
                || type.getSuperoperatorDimensions() == -1 :
            type.getSuperoperatorDimensions() + " " + matrix;
        assert type.getSuperoperatorDimensions() * type.getSuperoperatorDimensions() == matrix.getDimensions()[1]
                || type.getSuperoperatorDimensions() == -1;
        this.matrix = (ValueArrayAlgebra) matrix;
        this.accComplex = getTypeComplex().newValue();
        this.accComplex2 = getTypeComplex().newValue();
        this.accComplex3 = getTypeComplex().newValue();
        this.zeroComplex = UtilValue.newValue(getTypeComplex(), 0);
        this.importedMatrix = (ValueArray) getTypeMatrix().newValue();
        this.importedList = (ValueArray) getTypeList().newValue();
        this.tensor = (ValueArray) getTypeMatrix().newValue();
        this.left = (ValueArray) getTypeMatrix().newValue();
        this.right = (ValueArray) getTypeMatrix().newValue();
        this.accMatrix = getTypeMatrix().newValue();
        this.valueReal = TypeReal.get(type.getContext()).newValue();
        this.zeroReal = UtilValue.newValue(TypeReal.get(type.getContext()), 0);
    }

    ValueSuperOperator(TypeSuperOperator type) {
        this(type, prepareMatrix(type));
        unspecifiedDim = !type.hasDimensions();
    }
    
    ValueSuperOperator(ValueSuperOperator other) throws EPMCException {
        this((TypeSuperOperator) other.getType(), UtilValue.clone(other.matrix));
    }
    
    @Override
    public void setImmutable() {
        this.immutable = true;
        matrix.setImmutable();
    }
    
    @Override
    public TypeSuperOperator getType() {
        return type;
    }
    
    private TypeComplex getTypeComplex() {
        return getType().getTypeComplex();
    }
    
    private Type getTypeList() {
        return ((TypeSuperOperator) getType()).getTypeList();
    }

    private static ValueArray prepareMatrix(TypeSuperOperator type) {
        assert type != null;
        int dim = type.getSuperoperatorDimensions() * type.getSuperoperatorDimensions();
        ValueArray result = type.getTypeMatrix().newValue();
        result.setDimensions(dim, dim);
        return result;
    }
    
    @Override
    public ValueSuperOperator clone() {
        try {
			return new ValueSuperOperator(this);
		} catch (EPMCException e) {
			throw new RuntimeException(e);
		}
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
        return "sop(" + matrix.toString() + ")";
    }
    
    public ValueArrayAlgebra getMatrix() {
        return matrix;
    }
    
    public int getSuperoperatorDimensions() {
        if (unspecifiedDim) {
            return TypeSuperOperator.DIMENSIONS_UNSPECIFIED;
        } else {
            return (int) Math.sqrt(matrix.getLength(0));
        }
    }
    
    public void setDimensions(int dim) {
        assert dim >= -1;
        assert ((TypeSuperOperator) getType()).getSuperoperatorDimensions() == TypeSuperOperator.DIMENSIONS_UNSPECIFIED;
        matrix.setDimensions(dim * dim, dim * dim);
    }

    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        adjustDimensions(op);
        ValueSuperOperator opSuperOperator = castOrImport(op, 0);
        getMatrix().set(opSuperOperator.getMatrix());
    }
    
    @Override
    public void set(int value) {
        set(UtilValue.<ValueAlgebra,TypeAlgebra>newValue(TypeReal.get(getType().getContext()), value));
    }
    
    @Override
    public void add(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        adjustDimensions(operand1, operand2);
        ValueSuperOperator op1SuperOperator = castOrImport(operand1, 0);
        ValueSuperOperator op2SuperOperator = castOrImport(operand2, 1);
        ValueArray matrix1 = op1SuperOperator.getMatrix();
        ValueArray matrix2 = op2SuperOperator.getMatrix();
        int maxMatDim = Math.max(matrix1.getDimensions()[0],
        		matrix2.getDimensions()[0]);
        if (matrix1.getDimensions()[0] == 1) {
        	matrix1.get(accComplex, 0);
        	matrix1 = matrix1.getType().newValue();
        	matrix1.setDimensions(maxMatDim, maxMatDim);
        	for (int i = 0; i < maxMatDim; i++) {
            	matrix1.set(accComplex, i, i);
        	}
        }
        if (matrix2.getDimensions()[0] == 1) {
        	matrix2.get(accComplex, 0);
        	matrix2 = matrix2.getType().newValue();
        	matrix2.setDimensions(maxMatDim, maxMatDim);
        	for (int i = 0; i < maxMatDim; i++) {
            	matrix2.set(accComplex, i, i);
        	}
        }

        getMatrix().add(matrix1, matrix2);
    }

    @Override
    public void multiply(Value operand1, Value operand2)
            throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        adjustDimensions(operand1, operand2);
        ValueSuperOperator op1SuperOperator = castOrImport(operand1, 0);
        ValueSuperOperator op2SuperOperator = castOrImport(operand2, 1);
        ValueArray matrix1 = op1SuperOperator.getMatrix();
        ValueArray matrix2 = op2SuperOperator.getMatrix();
        int maxMatDim = Math.max(matrix1.getDimensions()[0],
        		matrix2.getDimensions()[0]);
        if (matrix1.getDimensions()[0] == 1) {
        	matrix1.get(accComplex, 0);
        	matrix1 = matrix1.getType().newValue();
        	matrix1.setDimensions(maxMatDim, maxMatDim);
        	for (int i = 0; i < maxMatDim; i++) {
            	matrix1.set(accComplex, i, i);
        	}
        }
        if (matrix2.getDimensions()[0] == 1) {
        	matrix2.get(accComplex, 0);
        	matrix2 = matrix2.getType().newValue();
        	matrix2.setDimensions(maxMatDim, maxMatDim);
        	for (int i = 0; i < maxMatDim; i++) {
            	matrix2.set(accComplex, i, i);
        	}
        }

        matrix.matrixMultiply(matrix1, matrix2);
    }

    @Override
    public void multInverse(Value op) throws EPMCException {
        assert !isImmutable();
        assert op != null;
        adjustDimensions(op);
        ValueSuperOperator opSuperOperator = castOrImport(op, 0);
        getMatrix().matrixMultInverse(opSuperOperator.getMatrix());
    }

    @Override
    public void addInverse(Value op) throws EPMCException {
        assert !isImmutable();
        assert op != null;
        adjustDimensions(op);
        ValueSuperOperator opSuperOperator = castOrImport(op, 0);
        getMatrix().addInverse(opSuperOperator.getMatrix());
    }

    @Override
    public void divide(Value operand1, Value operand2)
            throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        adjustDimensions(operand1, operand2);
        ValueSuperOperator op1SuperOperator = castOrImport(operand1, 0);
        if (getTypeComplex().canImport(operand2.getType())) {
            getMatrix().divide(op1SuperOperator.getMatrix(), operand2);
        } else {
            ValueSuperOperator op2SuperOperator = castOrImport(operand2, 1);
            getMatrix().divide(op1SuperOperator.getMatrix(), op2SuperOperator.getMatrix());
        }
    }

    @Override
    public void subtract(Value operand1, Value operand2)
            throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        adjustDimensions(operand1, operand2);
        ValueSuperOperator op1SuperOperator = castOrImport(operand1, 0);
        ValueSuperOperator op2SuperOperator = castOrImport(operand2, 1);
        getMatrix().subtract(op1SuperOperator.getMatrix(), op2SuperOperator.getMatrix());
    }

    @Override
    public boolean isZero() {
        // TODO check
        int squaredDim = getMatrix().getLength(0);
        for (int row = 0; row < squaredDim; row++) {
            for (int column = 0; column < squaredDim; column++) {
                try {
                    getMatrix().get(accComplex, row, column);
                } catch (EPMCException e) {
                    e.printStackTrace();
                    assert false;
                }
                if (!accComplex.isZero()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isOne() {
        ValueSuperOperator one = (ValueSuperOperator) getType().newValue();
        ValueArray oneMatrix = one.getMatrix();
        for (int entry = 0; entry < oneMatrix.getLength(0); entry++) {
            oneMatrix.set(getTypeComplex().getOne(), entry, entry);
        }
        boolean result;
        try {
            result = isEq(one);
        } catch (EPMCException e) {
            e.printStackTrace();
            assert false;
            return false;
        }
        return result;
    }

    @Override
    public double norm() throws EPMCException {
        double iterNorm = 0.0;
        int squaredDim = getMatrix().getLength(0);
        for (int row = 0; row < squaredDim; row++) {
            for (int column = 0; column < squaredDim; column++) {
                getMatrix().get(accComplex, row, column);
                iterNorm = Math.max(iterNorm, accComplex.norm());
            }
        }

        return iterNorm;
    }

    private void fromMatrix(ValueArray matrix) throws EPMCException {
        assert !isImmutable();
        assert matrix != null;
        getMatrix().set(matrix);
    }

    void fromMatrixList(ValueSuperOperator result, ValueArray list)
            throws EPMCException {
        assert !isImmutable();
        assert result != null;
        assert list != null;
        importedList.set(list);
        assert importedList.getNumDimensions() == 1;
        assert importedList.getLength(0) > 0;
        importedList.get(importedMatrix, 0);
        int hilbertDimension = importedMatrix.getLength(0);
        int squaredDim = hilbertDimension * hilbertDimension;
        
        ValueArray resultArray = result.getMatrix();
        int[] resultDim = {squaredDim, squaredDim};
        resultArray.setDimensions(resultDim);
        tensor.setDimensions(resultDim);
        int numEntries = importedList.getLength(0);
        int[] mDim = {hilbertDimension, hilbertDimension};
        right.setDimensions(mDim);
        for (int row = 0; row < squaredDim; row++) {
            for (int column = 0; column < squaredDim; column++) {
                resultArray.set(zeroComplex, row, column);
            }
        }
        for (int entryNr = 0; entryNr < numEntries; entryNr++) {
            importedList.get(left, entryNr);
            for (int row = 0; row < hilbertDimension; row++) {
                for (int column = 0; column < hilbertDimension; column++) {
                    left.get(accComplex2, row, column);
                    UtilValueQMC.conjugate(accComplex, accComplex2);
                    right.set(accComplex, row, column);
                }
            }
            getType().getContext().getOperator(OperatorKronecker.IDENTIFIER).apply(tensor, left, right);
            accMatrix.add(resultArray, tensor);
            resultArray.set(accMatrix);
        }
        checkHermitian(result);
    }

    private void checkHermitian(ValueSuperOperator superOperator)
            throws EPMCException {
        assert superOperator != null;
        ValueArray array = prepareForEigen(superOperator);
        int hilbertDimension = array.getLength(0);
        ValueArray one = getTypeMatrix().newValue();
        one.setDimensions(hilbertDimension, hilbertDimension);
        for (int index = 0; index < hilbertDimension; index++) {
            one.set(getTypeComplex().getOne(), index, index);
        }
        ValueArrayAlgebra check = getTypeMatrix().newValue();
        check.setDimensions(hilbertDimension, hilbertDimension);
        check.subtract(one, array);
        eigenvalues(check);
    }

    private void maxEigenDiff(ValueReal result, Value operand) throws EPMCException {
        assert operand != null;
        ValueSuperOperator opSuper = castOrImport(operand, 0);
        ValueArray preparedOp1 = prepareForEigen(this);
        ValueArray preparedOp2 = prepareForEigen(opSuper);
        int hilbertDimension = preparedOp1.getLength(0);
        ValueArrayAlgebra subtracted = getTypeMatrix().newValue();
        subtracted.setDimensions(hilbertDimension, hilbertDimension);
        subtracted.subtract(preparedOp1, preparedOp2);
        Value[] eigen = null;
        eigen = eigenvalues(subtracted);
        result.set(0);
        for (Value value : eigen) {
        	result.max(result, value);
        }
    }
    
    @Override
    public boolean isLt(Value operand) throws EPMCException {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        return valueReal.isLt(zeroReal);
    }

    @Override
    public boolean isLe(Value operand) throws EPMCException {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        return valueReal.isLe(zeroReal);
    }
    
    @Override
    public boolean isEq(Value operand) throws EPMCException {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        return valueReal.isEq(zeroReal);
    }

    @Override
    public boolean isGt(Value operand) throws EPMCException {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        return valueReal.isGt(zeroReal);
    }
    
    @Override
    public boolean isGe(Value operand) throws EPMCException {
        assert operand != null;
        maxEigenDiff(valueReal, operand);
        return valueReal.isGe(zeroReal);
    }

    public void superoperator(Value base) throws EPMCException {
        assert !isImmutable();
        assert base != null;
        ValueArray baseArray = (ValueArray) base;
        int numDimensions = baseArray.getNumDimensions();
        if (numDimensions == 2) {
            fromMatrix(baseArray);
        } else if (numDimensions == 1) {
            fromMatrixList(this, baseArray);
        } else {
            assert false;
        }
    }
    
    private ValueArray prepareForEigen(ValueSuperOperator operator)
            throws EPMCException {
        assert operator != null;
        int matDim = operator.getMatrix().getLength(0);
        int hilbertDimension = (int) Math.sqrt(matDim);
        ValueArray result = getTypeMatrix().newValue();
        result.setDimensions(hilbertDimension, hilbertDimension);
        for (int targetRow = 0; targetRow < hilbertDimension; targetRow++) {
            for (int targetColumn = 0; targetColumn < hilbertDimension; targetColumn++) {
                accComplex.set(zeroComplex);
                for (int inner = 0; inner < hilbertDimension; inner++) {
                    int origRow = inner * hilbertDimension + inner;
                    int origColumn = targetColumn * hilbertDimension + targetRow;
                    operator.getMatrix().get(accComplex2, origRow, origColumn);
                    accComplex3.add(accComplex, accComplex2);
                    accComplex.set(accComplex3);
                }
                result.set(accComplex, targetRow, targetColumn);
            }
        }
        return result;
    }

    private Value[] eigenvalues(ValueArray matrix) throws EPMCException {
        assert matrix != null;
        return Eigen.eigenvalues(matrix);
    }
    
    private TypeArrayAlgebra getTypeMatrix() {
        return ((TypeSuperOperator) getType()).getTypeMatrix();
    }
    
    private void adjustDimensions(Value... operands) {
        if (unspecifiedDim) {
            int dim = TypeSuperOperator.DIMENSIONS_UNSPECIFIED;
            for (Value value : operands) {
                if (value instanceof ValueSuperOperator) {
                    ValueSuperOperator valueSuper = (ValueSuperOperator) value;
                    if (!valueSuper.unspecifiedDim) {
                        dim = valueSuper.getSuperoperatorDimensions();
                    }
                }
            }
            if (dim != TypeSuperOperator.DIMENSIONS_UNSPECIFIED) {
                this.matrix.setDimensions(dim * dim, dim * dim);
                this.unspecifiedDim = false;
            }
        }
    }
    
    private ValueSuperOperator castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (operand instanceof ValueSuperOperator) {
            ValueSuperOperator operandSuper = (ValueSuperOperator) operand;
            if (!unspecifiedDim && operandSuper.unspecifiedDim) {
                operandSuper.getMatrix().get(accComplex, 0);
                operandSuper = (ValueSuperOperator) operandSuper.getType().newValue();
                operandSuper.getMatrix().setDimensions(
                        getSuperoperatorDimensions() * getSuperoperatorDimensions(),
                        getSuperoperatorDimensions() * getSuperoperatorDimensions());
                operandSuper.unspecifiedDim = false;
                for (int i = 0; i < getSuperoperatorDimensions() * getSuperoperatorDimensions(); i++) {
                    operandSuper.getMatrix().set(accComplex, i, i);
                }
            }
            return operandSuper;
        } else if (getTypeComplex().canImport(operand.getType())) {
            if (importSuperOperators[number] == null) {
                importSuperOperators[number] = (ValueSuperOperator) getType().newValue();
            }
            ValueArray matrix = importSuperOperators[number].getMatrix();
            matrix.setDimensions(getMatrix().getDimensions());
            for (int index = 0; index < matrix.getLength(0); index++) {
                matrix.set(operand, index, index);
            }
            return importSuperOperators[number];
        } else {
            assert false : operand + " " + operand.getType();
            return null;
        }
    }
    
    @Override
    public boolean checkRange() throws EPMCException {
        try {
            checkHermitian(this);
        } catch (EPMCException e) {
            if (e.getProblem() == ProblemsQMC.SUPER_OPERATOR_NON_HERMITIAN_MATRIX) {
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Value other) {
        assert !isImmutable();
        assert other != null;
        ValueSuperOperator opSuperOperator = castOrImport(other, 0);
        return matrix.compareTo(opSuperOperator.matrix);
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }
    
    @Override
    public double distance(Value other) throws EPMCException {
    	ValueSuperOperator otherSuperOperator = castOrImport(other, 0);
        double distance = 0.0;
        int squaredDim = getMatrix().getLength(0);
        for (int row = 0; row < squaredDim; row++) {
            for (int column = 0; column < squaredDim; column++) {
                getMatrix().get(accComplex, row, column);
                otherSuperOperator.getMatrix().get(otherSuperOperator.accComplex, row, column);
                distance = Math.max(distance, accComplex.distance(otherSuperOperator.accComplex));
            }
        }
        return distance;
    }

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}    
}
