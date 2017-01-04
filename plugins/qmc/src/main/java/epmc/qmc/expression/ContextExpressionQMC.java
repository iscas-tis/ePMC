package epmc.qmc.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.qmc.value.ContextValueQMC;
import epmc.qmc.value.OperatorArray;
import epmc.qmc.value.OperatorBaseBra;
import epmc.qmc.value.OperatorBaseKet;
import epmc.qmc.value.OperatorBraToVector;
import epmc.qmc.value.OperatorComplex;
import epmc.qmc.value.OperatorIdentityMatrix;
import epmc.qmc.value.OperatorKetToVector;
import epmc.qmc.value.OperatorPhaseShift;
import epmc.qmc.value.OperatorSuperOperator;
import epmc.value.ContextValue;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;

// make nonserializable
public final class ContextExpressionQMC {
    private final ContextValueQMC contextValueQMC;
    
    public ContextExpressionQMC(ContextValue contextValue) {
        this.contextValueQMC = new ContextValueQMC(contextValue);
    }
    
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
        		.setOperator(getContextValue().getOperator(OperatorIdentityMatrix.IDENTIFIER))
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
    			.setOperator(getContextValue().getOperator(OperatorComplex.IDENTIFIER))
    			.setOperands(real, imag)
    			.build();
    }

    public Expression newComplex(Value real, Value imag) {
    	Expression realExpr = new ExpressionLiteral.Builder()
    			.setValue(real)
    			.build();
    	Expression imagExpr = new ExpressionLiteral.Builder()
    			.setValue(imag)
    			.build();
    	return newComplex(realExpr, imagExpr);
    }
    
    public Expression newComplex(int real, int imag) {
    	TypeInteger typeInteger = TypeInteger.get(getContextValue());
    	Expression realExpr = new ExpressionLiteral.Builder()
    			.setValue(UtilValue.newValue(typeInteger, real))
    			.build();
    	Expression imagExpr = new ExpressionLiteral.Builder()
    			.setValue(UtilValue.newValue(typeInteger, imag))
    			.build();
    	return newComplex(realExpr, imagExpr);
    }

    public Expression newComplex(String real, String imag) throws EPMCException {
    	Value valueReal = UtilValue.newValue(TypeReal.get(contextValueQMC.getContextValue()), real);
    	Value valueImag = UtilValue.newValue(TypeReal.get(contextValueQMC.getContextValue()), imag);
    	return newComplex(valueReal, valueImag);
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
     * Obtain superoperator expression.
     * Parameter {@code inner} should either be a square two-dimensional array
     * of (hilbert-dim**2) or a list (one-dimensional array) of square
     * two-dimensional arrays (matrix) of the same dimension as the Hilbert
     * space. All matrix entries must evaluate to an algebraic value.
     * 
     * @param inner list or matrix to construct superoperator from
     * @return superoperator expression
     */
    public Expression newSuperOperator(Expression inner, Positional positional) {
        assert assertExpression(inner);
        List<Expression> children = Collections.singletonList(inner);
        return new ExpressionOperator.Builder()
        		.setOperator(getContextValue().getOperator(OperatorSuperOperator.IDENTIFIER))
        		.setPositional(positional)
        		.setOperands(children)
        		.build();
    }

    public Expression newSuperOperator(Expression inner) {
        assert assertExpression(inner);
        return newSuperOperator(inner, null);
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
        return newSuperOperator(inner, positional);
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
        return newSuperOperator(matrix, positional);
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
        		.setOperator(getContextValue().getOperator(OperatorPhaseShift.IDENTIFIER))
        		.setPositional(positional)
        		.setOperands(shift)
        		.build();
    }

    public Expression newPhaseShiftMatrix(Expression shift) {
        assert assertExpression(shift);
        return newPhaseShiftMatrix(shift, null);
    }

    /**
     * Obtain expression representing a matrix as a two-dimensional array.
     * {@code rows} denotes the number of rows and {@code columns} denote the
     * number of columms. The list {@code entries} denotes the entries of the
     * matrix to construct. The number of these expressions is the product the
     * number of rows and columns. These expressions can be of arbitrary type,
     * but should be type compatible. The order of entries is assumed to be in
     * ascending lexicographical order, e.g. entry of position (0,0) before
     * (0,1) before (0,2) before (2,0) etc.
     * 
     * @param rows number of rows in matrix
     * @param columns number of columns in matrix
     * @param entries entries of matrix to be constructed
     * @return matrix constructed from dimension information and entry list
     */
    public Expression newMatrix(int rows, int columns,
            List<Expression> entries, Positional positional) {
        assert rows > 0;
        assert columns > 0;
        assert entries != null;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        assert entries.size() == rows * columns;
        int[] dimSizes = {rows, columns};
        return newArray(dimSizes, entries, positional);
    }

    public Expression newMatrix(int rows, int columns,
            List<Expression> entries) {
        assert rows > 0;
        assert columns > 0;
        assert entries != null;
        for (Expression entry : entries) {
            assert entry != null;
        }
        assert entries.size() == rows * columns;
        return newMatrix(rows, columns, entries, null);
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

    /**
     * Obtain bra (column) base vector.
     * {@code number} should evaluate to an integer and {@code dimensions}
     * should evaluate to a Hilbert space.
     * 
     * @param number number of vector (0 to {@code dimension}-1)
     * @param dimensions dimensionality of vector space
     * @return Bra (column) base vector with given number of given dimension
     */
    public Expression newBaseBra(Expression number, Expression dimensions, Positional positional) {
        assert assertExpression(number);
        assert assertExpression(dimensions);
        return new ExpressionOperator.Builder()
        	.setOperator(getContextValue().getOperator(OperatorBaseBra.IDENTIFIER))
        	.setPositional(positional)
        	.setOperands(number, dimensions)
        	.build();
    }

    public Expression newBaseBra(Expression number, Expression dimensions) {
        assert assertExpression(number);
        assert assertExpression(dimensions);
        return new ExpressionOperator.Builder()
        		.setOperator(getContextValue().getOperator(OperatorBaseBra.IDENTIFIER))
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
        		.setOperator(getContextValue().getOperator(OperatorBaseKet.IDENTIFIER))
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
        		.setOperator(getContextValue().getOperator(OperatorBraToVector.IDENTIFIER))
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
        		.setOperator(getContextValue().getOperator(OperatorKetToVector.IDENTIFIER))
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
        int dimSizes[] = {entries.size()};
        return newArray(dimSizes, entries, positional);
    }

    /**
     * Obtain expression representing an array.
     * The first element of {@code children} stores the number of dimensions of
     * the array to be created. It is required to be of type
     * {@link ExpressionLiteral} which contains a {@link ValueInteger}. With
     * {@code n} being the number of dimensions, a number of {@code n}
     * expressions follow which represent the size of the given dimension. Thus,
     * they also need to be {@code ExpressionLiteral} containing
     * {@code ValueInteger}. Afterwards, a number of {@code Expression} follows
     * which represent the entries of the array. The number of these expressions
     * is the product of the sizes of the dimensions. These expressions can be
     * of arbitrary type, but should be type compatible. The order of entries
     * is assumed to be in ascending lexicographical order, e.g. entry of
     * position (0,0,0) before (0,0,2) before (0,2,0) before (0,2,1) etc.
     * 
     * @param children list of expressions to transform to array
     * @return array obtained from transforming list of children.
     */
    Expression newArray(List<Expression> children, Positional positional) {
        assert children != null;
        for (Expression child : children) {
            assert assertExpression(child);
        }
        return new ExpressionOperator.Builder()
        		.setOperator(getContextValue().getOperator(OperatorArray.IDENTIFIER))
        		.setPositional(positional)
        		.setOperands(children)
        		.build();
    }

    /**
     * Obtain expression representing an array.
     * The length of {@code dimSizes} denotes the number of dimensions of the
     * array and its entries denote the sizes of the dimensions. The list
     * {@code entries} denotes the entries of the array to construct. The number
     * of these expressions is the product of the sizes of the dimensions. These
     * expressions can be of arbitrary type, but should be type compatible.
     * The order of entries is assumed to be in ascending lexicographical order,
     * e.g. entry of positition (0,0,0) before (0,0,2) before (0,2,0) before
     * (0,2,1) etc.
     * 
     * @param dimSizes sizes of dimensions of array to create
     * @param entries entries of the array to be created
     * @return array obtained from transforming list of children.
     */
    public Expression newArray(int[] dimSizes, List<Expression> entries, Positional positional) {
    	TypeInteger typeInteger = TypeInteger.get(getContextValue());
        assert dimSizes != null;
        int numTotalEntries = 1;
        for (int dimSize : dimSizes) {
            assert dimSize >= 0;
            numTotalEntries *= dimSize;
        }
        assert entries != null;
        assert entries.size() == numTotalEntries;
        for (Expression entry : entries) {
            assert assertExpression(entry);
        }
        List<Expression> children = new ArrayList<>();
        children.add(new ExpressionLiteral.Builder()
        		.setValue(UtilValue.newValue(typeInteger, dimSizes.length))
        		.build());
        for (int dim = 0; dim < dimSizes.length; dim++) {
            children.add(new ExpressionLiteral.Builder()
            		.setValue(UtilValue.newValue(typeInteger, dimSizes[dim]))
            		.build());
        }
        children.addAll(entries);
        return newArray(children, positional);
    }
    
    public ContextValueQMC getContextValueQMC() {
        return contextValueQMC;
    }
    
    public ContextValue getContextValue() {
    	return contextValueQMC.getContextValue();
    }
}
