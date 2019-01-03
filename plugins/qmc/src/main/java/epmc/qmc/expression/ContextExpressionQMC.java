package epmc.qmc.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.qmc.operator.OperatorArray;
import epmc.qmc.operator.OperatorBaseBra;
import epmc.qmc.operator.OperatorBaseKet;
import epmc.qmc.operator.OperatorBraToVector;
import epmc.qmc.operator.OperatorComplex;
import epmc.qmc.operator.OperatorIdentityMatrix;
import epmc.qmc.operator.OperatorKetToVector;
import epmc.qmc.operator.OperatorMatrix;
import epmc.qmc.operator.OperatorPhaseShift;
import epmc.qmc.operator.OperatorSuperOperatorList;
import epmc.qmc.operator.OperatorSuperOperatorMatrix;

// TODO get rid of this class
public final class ContextExpressionQMC {
    /**
     * Obtain identity matrix expression.
     * Parameter {@code size} specifies the expression the identity matrix shall
     * have. The expression must later evaluate to an integer value.
     * 
     * @param size size of identity matrix
     * @return identity matrix expression
     */
    public Expression newIdentityMatrix(Expression size, Positional positional) {
        assert assertExpression(size);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorIdentityMatrix.IDENTITY_MATRIX)
                .setPositional(positional)
                .setOperands(size)
                .build();
    }

    public Expression newIdentityMatrix(Expression size) {
        assert assertExpression(size);
        return newIdentityMatrix(size, null);
    }

    public Expression newComplex(Expression real, Expression imag) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorComplex.COMPLEX)
                .setOperands(real, imag)
                .build();
    }

    public Expression newComplex(int real, int imag) {
        Expression realExpr = new ExpressionLiteral.Builder()
                .setValue(Integer.toString(real))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
        Expression imagExpr = new ExpressionLiteral.Builder()
                .setValue(Integer.toString(imag))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build();
        return newComplex(realExpr, imagExpr);
    }

    public Expression newComplex(String real, String imag) {
        Expression realExpr = new ExpressionLiteral.Builder()
                .setValue(real)
                .setType(ExpressionTypeReal.TYPE_REAL)
                .build();
        Expression imagExpr = new ExpressionLiteral.Builder()
                .setValue(imag)
                .setType(ExpressionTypeReal.TYPE_REAL)
                .build();
        return newComplex(realExpr, imagExpr);
    }

    private boolean assertExpression(Expression expression) {
        assert expression != null;
        return true;
    }

    public Expression newList(List<Expression> entries) {
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        return newList(entries, null);
    }

    /**
     * Obtain superoperator expression from list of matrices.
     * Parameter {@code} entries should consist of square two-dimensional arrays
     * (matrices) of the same dimension as the Hilbert space.  All matrix
     * entries must evaluate to an algebraic value.
     * 
     * @param entries list of matrices to obtain superoperator from
     * @return superoperator expression
     */
    public Expression newSuperOperatorFromList(List<Expression> entries, Positional positional) {
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
            assert entry != null;
        }
        Expression inner = newList(entries, positional);
        assert assertExpression(inner);
        List<Expression> children = Collections.singletonList(inner);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorSuperOperatorList.SUPEROPERATOR_LIST)
                .setPositional(positional)
                .setOperands(children)
                .build();
    }

    public Expression newSuperOperatorFromList(List<Expression> entries) {
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        return newSuperOperatorFromList(entries, null);
    }

    /**
     * Obtain superoperator expression from single matrix.
     * Parameter {@code matrix} should be a two-dimensional square array
     * (matrix) of dimension (hilbert-dim^2) x (hilbert-dim^2).  All matrix
     * entries must evaluate to an algebraic value.
     * 
     * @param matrix matrix to transform to superoperator
     * @return superoperator expression
     */
    public Expression newSuperOperatorFromMatrix(Expression matrix,
            Positional positional) {
        assert assertExpression(matrix);
        List<Expression> children = Collections.singletonList(matrix);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorSuperOperatorMatrix.SUPEROPERATOR_MATRIX)
                .setPositional(positional)
                .setOperands(children)
                .build();
    }

    public Expression newSuperOperatorFromMatrix(Expression matrix) {
        assert assertExpression(matrix);
        return newSuperOperatorFromMatrix(matrix, null);
    }

    /**
     * Obtain a quantum-mechanic phase-shift matrix expression.
     * Parameter {@code shift} specifies the rotation angle. This expression
     * should evaluate to a real value.
     * 
     * @param shift
     * @return
     */
    public Expression newPhaseShiftMatrix(Expression shift, Positional positional) {
        assert assertExpression(shift);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorPhaseShift.PHASE_SHIFT)
                .setPositional(positional)
                .setOperands(shift)
                .build();
    }

    public Expression newPhaseShiftMatrix(Expression shift) {
        assert assertExpression(shift);
        return newPhaseShiftMatrix(shift, null);
    }

    private Expression newMatrix(int rows, int columns,
            List<Expression> entries, Positional positional) {
        assert rows > 0;
        assert columns > 0;
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        assert entries.size() == rows * columns;
        List<Expression> children = new ArrayList<>();
        children.add(new ExpressionLiteral.Builder()
                .setValue(Integer.toString(rows))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build());
        children.add(new ExpressionLiteral.Builder()
                .setValue(Integer.toString(columns))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build());
        children.addAll(entries);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorMatrix.MATRIX)
                .setPositional(positional)
                .setOperands(children)
                .build();
    }

    /**
     * Obtain expression representing a matrix as a two-dimensional array.
     * {@code matrix} is a list containing the rows of the matrix to be
     * constructed. Each row is itself a list containing the entries of this
     * particular row. Thus, all such lists are required to have the same
     * length.
     * 
     * @param matrix list of rows to construct matrix expression of
     * @return matrix expression obtain from given list of rows
     */
    public Expression newMatrix(List<List<Expression>> matrix, Positional positional) {
        assert matrix != null;
        assert matrix.size() > 0;
        int rows = matrix.size();
        int columns = -1;
        List<Expression> entries = new ArrayList<>();
        for (List<Expression> row : matrix) {
            assert row != null;
            for (Expression expr : row) {
                assert assertExpression(expr);
            }
            if (columns == -1) {
                columns = row.size();
            } else {
                assert columns == row.size();
            }
            entries.addAll(row);
        }
        return newMatrix(rows, columns, entries, positional);
    }

    public Expression newMatrix(List<List<Expression>> matrix) {
        assert matrix != null;
        assert matrix.size() > 0;
        return newMatrix(matrix, null);
    }

    /**
     * Obtain a vector (one-dimensional array) of expressions
     * 
     * @param entries entries to obtain list from in terms of collection
     * @return list of expressions in terms of one-dimensional array
     */
    public Expression newVector(List<Expression> vector, Positional positional) {
        assert vector != null;
        for (Expression expression : vector) {
            assert assertExpression(expression);
        }
        return newList(vector, positional);
    }

    public Expression newVector(List<Expression> vector) {
        assert vector != null;
        for (Expression expression : vector) {
            assert assertExpression(expression);
        }
        return newVector(vector, null);
    }

    public Expression newBaseBra(Expression number, Expression dimensions) {
        assert assertExpression(number);
        assert assertExpression(dimensions);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorBaseBra.BASE_BRA)
                .setOperands(number, dimensions)
                .build();
    }

    /**
     * Obtain ket (row) base vector.
     * {@code number} should evaluate to an integer and {@code dimensions}
     * should evaluate to a Hilbert space.
     * 
     * @param number number of vector (0 to {@code dimension}-1)
     * @param dimensions dimensionality of vector space
     * @return Ket (row) base vector with given number of given dimension
     */
    public Expression newBaseKet(Expression number, Expression dimensions, Positional positional) {
        assert assertExpression(number);
        assert assertExpression(dimensions);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorBaseKet.BASE_KET)
                .setPositional(positional)
                .setOperands(number, dimensions)
                .build();
    }

    public Expression newBaseKet(Expression number, Expression dimensions) {
        assert assertExpression(number);
        assert assertExpression(dimensions);
        return newBaseKet(number, dimensions, null);
    }

    /**
     * Obtain vector from bra matrix.
     * {@code bra} should evaluate to a 1xn matrix. After evaluation, the result
     * will be a vector (one-dimensional array) of size n in which the entries
     * of {@code bra} are conjugated and appear in the same order as in the
     * matrix.
     * 
     * @param bra bra matrix to transform into vector
     * @return vector obtained from bra matrix
     */
    public Expression newBraToVector(Expression bra, Positional positional) {
        assert assertExpression(bra);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorBraToVector.BRA_TO_VECTOR)
                .setPositional(positional)
                .setOperands(bra)
                .build();
    }

    public Expression newBraToVector(Expression bra) {
        assert assertExpression(bra);
        return newBraToVector(bra, null);
    }

    /**
     * Obtain vector from ket matrix.
     * {@code ket} should evaluate to a nx1 matrix. After evaluation, the result
     * will be a vector (one-dimensional array) of size n in which the entries
     * of {@code ket} appear in the same order as in the matrix.
     * 
     * @param ket ket matrix to transform into vector
     * @return vector obtained from ket matrix
     */
    public Expression newKetToVector(Expression ket, Positional positional) {
        assert assertExpression(ket);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorKetToVector.KET_TO_VECTOR)
                .setPositional(positional)
                .setOperands(ket)
                .build();
    }

    public Expression newKetToVector(Expression ket) {
        assert assertExpression(ket);
        return newKetToVector(ket, null);
    }


    /**
     * Obtain a list (one-dimensional array) of expressions
     * 
     * @param entries entries to obtain list from in terms of collection
     * @return list of expressions in terms of one-dimensional array
     */
    public Expression newList(List<Expression> entries, Positional positional) {
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        int size = entries.size();
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        List<Expression> children = new ArrayList<>();
        children.add(new ExpressionLiteral.Builder()
                .setValue(Integer.toString(size))
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build());
        children.addAll(entries);
        return new ExpressionOperator.Builder()
                .setOperator(OperatorArray.ARRAY)
                .setPositional(positional)
                .setOperands(children)
                .build();
    }
}
