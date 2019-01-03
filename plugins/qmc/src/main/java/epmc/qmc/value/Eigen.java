package epmc.qmc.value;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import epmc.operator.OperatorAbs;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSqrt;
import epmc.operator.OperatorSubtract;
import epmc.qmc.error.ProblemsQMC;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueReal;

// numerical routines based on EISPACK

public final class Eigen {
    private final static int MAX_ITER = 100000;

    public static ValueReal[] eigenvalues(ValueMatrix matrix)
    {
        assert matrix != null;
        ensure(isHermitian(matrix), ProblemsQMC.SUPER_OPERATOR_NON_HERMITIAN_MATRIX);
        return computeEigenvalues(matrix);
    }

    private static boolean isHermitian(ValueMatrix matrix) {
        assert matrix != null;
        ValueBoolean cmp = TypeBoolean.get().newValue();
        Value hermitianAdjoint = computeHermitianAdjoint(matrix);
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, matrix.getType(), hermitianAdjoint.getType());
        eq.apply(cmp, matrix, hermitianAdjoint);
        return cmp.getBoolean();
    }

    private static Value computeHermitianAdjoint(ValueMatrix matrix) {
        assert matrix != null;
        TypeMatrix matrixType = matrix.getType();
        TypeComplex entryType = (TypeComplex) matrixType.getEntryType();
        ValueMatrix result = matrixType.newValue();
        result.setDimensions(matrix.getNumRows(), matrix.getNumColumns());
        ValueComplex entry = entryType.newValue();
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        for (int rowNr = 0; rowNr < matrix.getNumRows(); rowNr++) {
            for (int colNr = 0; colNr < matrix.getNumColumns(); colNr++) {
                matrix.get(entry, rowNr, colNr);
                ValueReal entryImag = entry.getImagPart();
                addInverse.apply(entryImag, entryImag);
                result.set(entry, colNr, rowNr);
            }
        }
        return result;
    }

    private static ValueReal[] computeEigenvalues(ValueMatrix matrix) {
        List<Value[]> eigenvectors = new ArrayList<>();
        int size = matrix.getNumRows();
        TypeMatrix matrixType = matrix.getType();
        TypeComplex typeEntry = (TypeComplex) matrixType.getEntryType();
        TypeReal typeReal = typeEntry.getTypeReal();
        TypeMatrix typeMatrixReal = TypeMatrix.get(typeReal);
        ValueMatrix matrixDoubled = typeMatrixReal.newValue
                (2 * matrix.getNumRows(), 2 * matrix.getNumColumns());
        ValueComplex entry = typeEntry.newValue();
        ValueReal mImag = typeReal.newValue();
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                matrix.get(entry, row, col);
                Value real = entry.getRealPart();
                Value imag = entry.getImagPart();
                addInverse.apply(mImag, imag);
                matrixDoubled.set(real, row, col);
                matrixDoubled.set(real, size + row, size + col);
                matrixDoubled.set(imag, size + row, col);
                matrixDoubled.set(mImag, row, size + col);
            }
        }
        ValueReal[] eigenValuesDoubled = new ValueReal[2*size];
        for (int i = 0; i < eigenValuesDoubled.length; i++) {
            eigenValuesDoubled[i] = typeReal.newValue();
        }
        ValueReal[] offdiag = new ValueReal[2*size];
        for (int i = 0; i < offdiag.length; i++) {
            offdiag[i] = typeReal.newValue();
        }
        reduceSymmetricSquareToTridiagonal(matrixDoubled, eigenValuesDoubled, offdiag);
        System.arraycopy(offdiag, 1, offdiag, 0, size-1);
        offdiag[size - 1] = UtilValue.newValue(typeReal, 0);
        eigenSolveSymmetricTridiagonalMatrix(eigenValuesDoubled, offdiag, matrixDoubled);
        ValueReal eigenvalue[] = new ValueReal[size];
        for (int row = 0; row < size; row++) {
            eigenvalue[row] = eigenValuesDoubled[row];
            Value[] vector = new ValueComplex[size];
            for (int col = 0; col < size; col++) {
                vector[col] = typeEntry.newValue();
                Value real = ((ValueComplex) vector[col]).getRealPart();
                Value imag = ((ValueComplex) vector[col]).getImagPart();
                matrixDoubled.get(real, col, row);
                matrixDoubled.get(imag, col + size, row);
            }
            eigenvectors.add(vector);
        }
        return eigenvalue;
    }

    private static void reduceSymmetricSquareToTridiagonal(
            ValueMatrix matrix, ValueReal[] diag, ValueReal[] offdiag) {
        OperatorEvaluator sqrt = ContextValue.get().getEvaluator(OperatorSqrt.SQRT, TypeReal.get());
        int n = diag.length;
        TypeReal typeReal = TypeReal.as(matrix.getType().getEntryType());
        ValueReal f = typeReal.newValue();
        ValueReal g = typeReal.newValue();
        ValueReal h = typeReal.newValue();
        ValueReal scale = typeReal.newValue();
        ValueReal entry = typeReal.newValue();
        ValueReal tmp = typeReal.newValue();
        ValueReal zero = UtilValue.newValue(typeReal, 0);
        ValueReal one = UtilValue.newValue(typeReal, 1);
        ValueReal hh = typeReal.newValue();
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator ge = ContextValue.get().getEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeReal.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (int i = n - 1; i > 0; i--) {
            int l = i - 1;
            h.set(0);
            scale.set(0);
            if (l > 0) {
                for (int k = 0; k <= l; k++) {
                    matrix.get(entry, i, k);
                    OperatorEvaluator abs = ContextValue.get().getEvaluator(OperatorAbs.ABS, TypeReal.get());
                    abs.apply(entry, entry);
                    add.apply(scale, scale, entry);
                }
                isZero.apply(cmp, scale);
                if (cmp.getBoolean()) {
                    matrix.get(offdiag[i], i, l);
                } else {
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, i, k);
                        divide.apply(tmp, entry, scale);
                        matrix.set(tmp, i, k);
                        multiply.apply(tmp, tmp, tmp);
                        add.apply(h, h, tmp);
                    }
                    matrix.get(f, i, l);
                    sqrt.apply(g, h);
                    ge.apply(cmp, f, zero);
                    if (cmp.getBoolean()) {
                        addInverse.apply(g, g);
                    }
                    multiply.apply(offdiag[i], scale, g);
                    multiply.apply(tmp, f, g);
                    subtract.apply(h, h, tmp);
                    subtract.apply(entry, f, g);
                    matrix.set(entry, i, l);
                    f.set(0);
                    for (int j = 0; j <= l; j++) {
                        matrix.get(entry, i, j);
                        divide.apply(entry, entry, h);
                        matrix.set(entry, j, i);
                        g.set(0);
                        for (int k = 0; k <= j; k++) {
                            matrix.get(entry, j, k);
                            matrix.get(tmp,  i, k);
                            multiply.apply(tmp, entry, tmp);
                            add.apply(g, g, tmp);
                        }
                        for (int k = j + 1; k <= l; k++) {
                            matrix.get(entry, k, j);
                            matrix.get(tmp, i, k);
                            multiply.apply(entry, entry, tmp);
                            add.apply(g, g, entry);
                        }
                        divide.apply(offdiag[j], g, h);
                        matrix.get(entry, i, j);
                        multiply.apply(tmp, offdiag[j], entry);
                        add.apply(f, f, tmp);
                    }
                    add.apply(tmp, h, h);
                    divide.apply(hh, f, tmp);
                    for (int j = 0; j <= l; j++) {
                        matrix.get(f, i, j);
                        multiply.apply(tmp, hh, f);
                        subtract.apply(g, offdiag[j], tmp);
                        set.apply(offdiag[j], g);
                        for (int k = 0; k <= j; k++) {
                            multiply.apply(tmp, f, offdiag[k]);
                            matrix.get(entry, i, k);
                            multiply.apply(entry, g, entry);
                            add.apply(tmp, tmp, entry);
                            matrix.get(entry, j, k);
                            subtract.apply(entry, entry, tmp);
                            matrix.set(entry, j, k);                            
                        }
                    }
                }
            } else {
                matrix.get(offdiag[i], i, l);
            }
            set.apply(diag[i], h);
        }
        offdiag[0].set(0);
        diag[0].set(0);
        for (int i = 0; i < n; i++) {
            int l = i - 1;
            isZero.apply(cmp, diag[i]);
            if (!cmp.getBoolean()) {
                for (int j = 0; j <= l; j++) {
                    g.set(0);
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, i, k);
                        matrix.get(tmp, k, j);
                        multiply.apply(tmp, entry, tmp);
                        add.apply(g, g, tmp);
                    }
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, k, i);
                        multiply.apply(tmp, g, entry);
                        matrix.get(entry, k, j);
                        subtract.apply(entry, entry, tmp);
                        matrix.set(entry, k, j);
                    }
                }
            }
            matrix.get(diag[i], i, i);
            matrix.set(one, i, i);
            for (int j = 0; j <= l; j++) {
                matrix.set(zero, j, i);
                matrix.set(zero, i, j);
            }
        }
    }

    private static void eigenSolveSymmetricTridiagonalMatrix(
            ValueReal[] diag, ValueReal[] offdiag, ValueMatrix transf) {
        OperatorEvaluator abs = ContextValue.get().getEvaluator(OperatorAbs.ABS, TypeReal.get());
        TypeReal typeReal = TypeReal.as(transf.getType().getEntryType());
        int n = diag.length;
        int nm1 = n - 1;
        int m;
        int l;
        int iteration;
        int i;
        int k;
        ValueReal s = typeReal.newValue();
        ValueReal r = typeReal.newValue();
        ValueReal p = typeReal.newValue();
        ValueReal g = typeReal.newValue();
        ValueReal f = typeReal.newValue();
        ValueReal dd = typeReal.newValue();
        ValueReal c = typeReal.newValue();
        ValueReal b = typeReal.newValue();
        ValueReal tmp1 = typeReal.newValue();
        ValueReal tmp2 = typeReal.newValue();
        ValueReal one = UtilValue.newValue(typeReal, 1);
        ValueReal two = UtilValue.newValue(typeReal, 2);
        ValueReal zero = UtilValue.newValue(typeReal, 0);
        ValueReal entry = typeReal.newValue();
        OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        OperatorEvaluator sqrt = ContextValue.get().getEvaluator(OperatorSqrt.SQRT, TypeReal.get());
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeReal.get(), TypeReal.get());
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator ge = ContextValue.get().getEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (l = 0; l < n; l++) {
            iteration = 0;
            do {
                for (m = l; m < nm1; m++) {
                    abs.apply(tmp1, diag[m]);
                    abs.apply(tmp2, diag[m+1]);
                    add.apply(dd, tmp1, tmp2);
                    abs.apply(tmp1, offdiag[m]);
                    add.apply(tmp1, tmp1, dd);
                    eq.apply(cmp, tmp1, dd);
                    if (cmp.getBoolean()) {
                        break;
                    }
                }
                if (m != l) {
                    ensure(iteration < MAX_ITER, ProblemsQMC.SUPER_OPERATOR_EIGENVALUES_ERROR);
                    iteration++;
                    subtract.apply(tmp1, diag[l + 1], diag[l]);
                    multiply.apply(tmp2, two, offdiag[l]);
                    divide.apply(g, tmp1, tmp2);
                    multiply.apply(tmp1, g, g);
                    add.apply(tmp1, tmp1, one);
                    sqrt.apply(r, tmp1);
                    abs.apply(tmp1, r);
                    lt.apply(cmp, g, zero);
                    if (cmp.getBoolean()) {
                        addInverse.apply(tmp1, tmp1);
                    }
                    add.apply(tmp1, g, tmp1);
                    divide.apply(tmp1, offdiag[l], tmp1);
                    subtract.apply(tmp2, diag[m], diag[l]);
                    add.apply(g, tmp2, tmp1);
                    set.apply(s, one);
                    set.apply(c, one);
                    set.apply(p, zero);
                    for (i = m - 1; i >= l; i--) {
                        multiply.apply(f, s, offdiag[i]);
                        multiply.apply(b, c, offdiag[i]);
                        abs.apply(tmp1, f);
                        abs.apply(tmp2, g);
                        ge.apply(cmp, tmp1, tmp2);
                        if (cmp.getBoolean()) {
                            divide.apply(c, g, f);
                            multiply.apply(tmp1, c, c);
                            add.apply(tmp1, tmp1, one);
                            sqrt.apply(r, tmp1);
                            multiply.apply(offdiag[i + 1], f, r);
                            divide.apply(s, one, r);
                            multiply.apply(c, c, s);
                        } else {
                            divide.apply(s, f, g);
                            multiply.apply(tmp1, s, s);
                            add.apply(tmp1, tmp1, one);
                            sqrt.apply(r, tmp1);
                            multiply.apply(offdiag[i+1], g, r);
                            divide.apply(c, one, r);
                            multiply.apply(s, s, c);
                        }
                        subtract.apply(g, diag[i+1], p);
                        subtract.apply(tmp1, diag[i], g);
                        multiply.apply(tmp1, tmp1, s);
                        multiply.apply(tmp2, two, c);
                        multiply.apply(tmp2, tmp2, b);
                        add.apply(r, tmp1, tmp2);
                        multiply.apply(p, s, r);
                        add.apply(diag[i+1], g, p);
                        multiply.apply(tmp1, c, r);
                        subtract.apply(g, tmp1, b);
                        for (k = 0; k < n; k++) {
                            transf.get(f, k, i+1);
                            transf.get(entry, k, i);
                            multiply.apply(tmp1, s, entry);
                            add.apply(tmp2, c, f);
                            add.apply(entry, tmp1, tmp2);
                            transf.set(entry, k, i+1);
                            transf.get(entry, k, i);
                            multiply.apply(tmp1, c, entry);
                            multiply.apply(tmp2, s, f);
                            subtract.apply(entry, tmp1, tmp2);
                            transf.set(entry, k, i);
                        }
                    }
                    subtract.apply(diag[l], diag[l], p);
                    set.apply(offdiag[l], g);
                    set.apply(offdiag[m], zero);
                }
            } while (m != l);
        }
    }
}
