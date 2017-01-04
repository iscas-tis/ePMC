package epmc.qmc.value;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.qmc.error.ProblemsQMC;
import epmc.value.TypeArray;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueReal;

// numerical routines based on EISPACK

public final class Eigen {
    private final static int MAX_ITER = 100000;
    
    public static Value[] eigenvalues(ValueArray matrixValue)
            throws EPMCException {
        ensure(isHermitian(matrixValue), ProblemsQMC.SUPER_OPERATOR_NON_HERMITIAN_MATRIX);
        return computeEigenvalues(matrixValue);
    }

    private static boolean isHermitian(ValueArray matrixValue) throws EPMCException {
        return matrixValue.isEq(computeHermitianAdjoint(matrixValue));
    }

    private static Value computeHermitianAdjoint(ValueArray matrix)
            throws EPMCException {
        TypeArray matrixType = matrix.getType();
        TypeComplex entryType = (TypeComplex) matrixType.getEntryType();
        ValueArray result = matrixType.newValue();
        result.setDimensions(matrix.getDimensions());
        ValueComplex entry = entryType.newValue();
        for (int rowNr = 0; rowNr < matrix.getLength(0); rowNr++) {
            for (int colNr = 0; colNr < matrix.getLength(1); colNr++) {
                matrix.get(entry, rowNr, colNr);
                ValueReal entryImag = entry.getImagPart();
                entryImag.addInverse(entryImag);
                result.set(entry, colNr, rowNr);
            }
        }
        return result;
    }
    
    private static Value[] computeEigenvalues(ValueArray matrix) throws EPMCException {
        List<Value[]> eigenvectors = new ArrayList<>();
        int size = matrix.getLength(0);
        TypeArray matrixType = matrix.getType();
        TypeComplex typeEntry = (TypeComplex) matrixType.getEntryType();
        TypeReal typeReal = typeEntry.getTypeReal();
        TypeMatrix typeMatrixReal = new TypeMatrix.Builder()
        		.setTypeArray(typeReal.getTypeArray())
        		.setNumRows(2*matrix.getLength(0))
        		.setNumColumns(2*matrix.getLength(1))
        		.build();
        ValueMatrix matrixDoubled = typeMatrixReal.newValue
                (2*matrix.getLength(0), 2*matrix.getLength(1));
        ValueComplex entry = typeEntry.newValue();
        ValueReal mImag = typeReal.newValue();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                matrix.get(entry, row, col);
                Value real = entry.getRealPart();
                Value imag = entry.getImagPart();
                mImag.addInverse(imag);
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
        Value eigenvalue[] = new Value[size];
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
            ValueMatrix matrix, ValueReal[] diag, ValueReal[] offdiag) throws EPMCException {
        int n = diag.length;
        TypeReal typeReal = TypeReal.asReal(matrix.getType().getEntryType());
        ValueReal f = typeReal.newValue();
        ValueReal g = typeReal.newValue();
        ValueReal h = typeReal.newValue();
        ValueReal scale = typeReal.newValue();
        ValueReal entry = typeReal.newValue();
        ValueReal tmp = typeReal.newValue();
        ValueReal zero = UtilValue.newValue(typeReal, 0);
        ValueReal one = UtilValue.newValue(typeReal, 1);
        ValueReal hh = typeReal.newValue();
        for (int i = n - 1; i > 0; i--) {
            int l = i - 1;
            h.set(0);
            scale.set(0);
            if (l > 0) {
                for (int k = 0; k <= l; k++) {
                    matrix.get(entry, i, k);
                    entry.abs(entry);
                    scale.add(scale, entry);
                }
                if (scale.isZero()) {
                    matrix.get(offdiag[i], i, l);
                } else {
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, i, k);
                        tmp.divide(entry, scale);
                        matrix.set(tmp, i, k);
                        tmp.multiply(tmp, tmp);
                        h.add(h, tmp);
                    }
                    matrix.get(f, i, l);
                    g.sqrt(h);
                    if (f.isGe(zero)) {
                        g.addInverse(g);
                    }
                    offdiag[i].multiply(scale, g);
                    tmp.multiply(f, g);
                    h.subtract(h, tmp);
                    entry.subtract(f, g);
                    matrix.set(entry, i, l);
                    f.set(0);
                    for (int j = 0; j <= l; j++) {
                        matrix.get(entry, i, j);
                        entry.divide(entry, h);
                        matrix.set(entry, j, i);
                        g.set(0);
                        for (int k = 0; k <= j; k++) {
                            matrix.get(entry, j, k);
                            matrix.get(tmp,  i, k);
                            tmp.multiply(entry, tmp);
                            g.add(g, tmp);
                        }
                        for (int k = j + 1; k <= l; k++) {
                            matrix.get(entry, k, j);
                            matrix.get(tmp, i, k);
                            entry.multiply(entry, tmp);
                            g.add(g, entry);
                        }
                        offdiag[j].divide(g, h);
                        matrix.get(entry, i, j);
                        tmp.multiply(offdiag[j], entry);
                        f.add(f, tmp);
                    }
                    tmp.add(h, h);
                    hh.divide(f, tmp);
                    for (int j = 0; j <= l; j++) {
                        matrix.get(f, i, j);
                        tmp.multiply(hh, f);
                        g.subtract(offdiag[j], tmp);
                        offdiag[j].set(g);
                        for (int k = 0; k <= j; k++) {
                            tmp.multiply(f, offdiag[k]);
                            matrix.get(entry, i, k);
                            entry.multiply(g, entry);
                            tmp.add(tmp, entry);
                            matrix.get(entry, j, k);
                            entry.subtract(entry, tmp);
                            matrix.set(entry, j, k);                            
                        }
                    }
                }
            } else {
                matrix.get(offdiag[i], i, l);
            }
            diag[i].set(h);
        }
        offdiag[0].set(0);
        diag[0].set(0);
        for (int i = 0; i < n; i++) {
            int l = i - 1;
            if (!diag[i].isZero()) {
                for (int j = 0; j <= l; j++) {
                    g.set(0);
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, i, k);
                        matrix.get(tmp, k, j);
                        tmp.multiply(entry, tmp);
                        g.add(g, tmp);                        
                    }
                    for (int k = 0; k <= l; k++) {
                        matrix.get(entry, k, i);
                        tmp.multiply(g, entry);
                        matrix.get(entry, k, j);
                        entry.subtract(entry, tmp);
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
            ValueReal[] diag, ValueReal[] offdiag, ValueMatrix transf) throws EPMCException {
        TypeReal typeReal = TypeReal.asReal(transf.getType().getEntryType());
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
        for (l = 0; l < n; l++) {
            iteration = 0;
            do {
                for (m = l; m < nm1; m++) {
                    tmp1.abs(diag[m]);
                    tmp2.abs(diag[m+1]);
                    dd.add(tmp1, tmp2);
                    tmp1.abs(offdiag[m]);
                    tmp1.add(tmp1, dd);
                    if (tmp1.isEq(dd)) {
                        break;
                    }
                }
                if (m != l) {
                    ensure(iteration < MAX_ITER, ProblemsQMC.SUPER_OPERATOR_EIGENVALUES_ERROR);
                    iteration++;
                    tmp1.subtract(diag[l + 1], diag[l]);
                    tmp2.multiply(two, offdiag[l]);
                    g.divide(tmp1, tmp2);
                    tmp1.multiply(g, g);
                    tmp1.add(tmp1, one);
                    r.sqrt(tmp1);
                    tmp1.abs(r);
                    if (g.isLt(zero)) {
                        tmp1.addInverse(tmp1);
                    }
                    tmp1.add(g, tmp1);
                    tmp1.divide(offdiag[l], tmp1);
                    tmp2.subtract(diag[m], diag[l]);
                    g.add(tmp2, tmp1);
                    s.set(one);
                    c.set(one);
                    p.set(zero);
                    for (i = m - 1; i >= l; i--) {
                        f.multiply(s, offdiag[i]);
                        b.multiply(c, offdiag[i]);
                        tmp1.abs(f);
                        tmp2.abs(g);
                        if (tmp1.isGe(tmp2)) {
                            c.divide(g, f);
                            tmp1.multiply(c, c);
                            tmp1.add(tmp1, one);
                            r.sqrt(tmp1);
                            offdiag[i + 1].multiply(f, r);
                            s.divide(one, r);
                            c.multiply(c, s);
                        } else {
                            s.divide(f, g);
                            tmp1.multiply(s, s);
                            tmp1.add(tmp1, one);
                            r.sqrt(tmp1);
                            offdiag[i+1].multiply(g, r);
                            c.divide(one, r);
                            s.multiply(s, c);
                        }
                        g.subtract(diag[i+1], p);
                        tmp1.subtract(diag[i], g);
                        tmp1.multiply(tmp1, s);
                        tmp2.multiply(two, c);
                        tmp2.multiply(tmp2, b);
                        r.add(tmp1, tmp2);
                        p.multiply(s, r);
                        diag[i+1].add(g, p);
                        tmp1.multiply(c, r);
                        g.subtract(tmp1, b);
                        for (k = 0; k < n; k++) {
                            transf.get(f, k, i+1);
                            transf.get(entry, k, i);
                            tmp1.multiply(s, entry);
                            tmp2.add(c, f);
                            entry.add(tmp1, tmp2);
                            transf.set(entry, k, i+1);
                            transf.get(entry, k, i);
                            tmp1.multiply(c, entry);
                            tmp2.multiply(s, f);
                            entry.subtract(tmp1, tmp2);
                            transf.set(entry, k, i);
                        }
                    }
                    diag[l].subtract(diag[l], p);
                    offdiag[l].set(g);
                    offdiag[m].set(zero);
                }
            } while (m != l);
        }
    }

}
