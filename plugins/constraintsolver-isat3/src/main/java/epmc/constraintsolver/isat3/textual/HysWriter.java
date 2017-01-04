package epmc.constraintsolver.isat3.textual;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;

final class HysWriter {
	private final static String SPACE = " ";
	private final static String COMMA = ",";
	private final static String LBRACE = "(";
	private final static String RBRACE = ")";
	private final static String LBRACK = "[";
	private final static String RBRACK = "]";
	private final static String SEMICOLON = ";";
	private final static String DECL = "DECL";
	private final static String EXPR = "EXPR";
	private final static String TYPE_FLOAT = "float";
	private final static String TYPE_INT = "int";
	private final static String TYPE_BOOLE = "boole";

	private final static Map<String,ISatOperator> EPMC_TO_ISAT3;
	static {
		Map<String,ISatOperator> iscasMCToISat3 = new HashMap<>();
		iscasMCToISat3.put(OperatorAnd.IDENTIFIER, newOperator()
				.setIdentifier("and").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorOr.IDENTIFIER, newOperator()
				.setIdentifier("or").setType(ISatOperator.Type.INFIX).build());
		/* "nand" not supported */ 
		/* "nor" not supported */ 
		/* "xor" not supported */ 
		iscasMCToISat3.put(OperatorIff.IDENTIFIER, newOperator()
				.setIdentifier("nxor").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorImplies.IDENTIFIER, newOperator()
				.setIdentifier("impl").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorNot.IDENTIFIER, newOperator()
				.setIdentifier("not").setType(ISatOperator.Type.PREFIX).build());

		iscasMCToISat3.put(OperatorAddInverse.IDENTIFIER, newOperator()
				.setIdentifier("-").setType(ISatOperator.Type.PREFIX).build());
		iscasMCToISat3.put(OperatorAdd.IDENTIFIER, newOperator()
				.setIdentifier("+").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorSubtract.IDENTIFIER, newOperator()
				.setIdentifier("-").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorMultiply.IDENTIFIER, newOperator()
				.setIdentifier("*").setType(ISatOperator.Type.INFIX).build());
		// does this work?
		iscasMCToISat3.put(OperatorDivide.IDENTIFIER, newOperator()
				.setIdentifier("/").setType(ISatOperator.Type.INFIX).build());

		/* "abs" not supported */ 
		iscasMCToISat3.put(OperatorMin.IDENTIFIER, newOperator()
				.setIdentifier("min").setType(ISatOperator.Type.PREFIX).build());
		iscasMCToISat3.put(OperatorMax.IDENTIFIER, newOperator()
				.setIdentifier("max").setType(ISatOperator.Type.PREFIX).build());
		/* "exp" not supported */
		/* "exp2" not supported */
		/* "exp10" not supported */
		iscasMCToISat3.put(OperatorLog.IDENTIFIER, newOperator()
				.setIdentifier("log").setType(ISatOperator.Type.PREFIX).build());
		/* "log2" not supported */
		/* "log10" not supported */
		/* "sin" not supported */
		/* "cos" not supported */
		/* "pow" not supported */
		/* "nrt" not supported */
		
		iscasMCToISat3.put(OperatorLt.IDENTIFIER, newOperator()
				.setIdentifier("<").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorLe.IDENTIFIER, newOperator()
				.setIdentifier("<=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorEq.IDENTIFIER, newOperator()
				.setIdentifier("=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorNe.IDENTIFIER, newOperator()
				.setIdentifier("!=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorGt.IDENTIFIER, newOperator()
				.setIdentifier(">").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorGe.IDENTIFIER, newOperator()
				.setIdentifier(">=").setType(ISatOperator.Type.INFIX).build());

		EPMC_TO_ISAT3 = Collections.unmodifiableMap(iscasMCToISat3);
	}
	private static ISatOperator.Builder newOperator() {
		return new ISatOperator.Builder();
	}


	private OutputStream outStream;
	private ConstraintSolverISat3Textual solver;

	HysWriter(ConstraintSolverISat3Textual solver, OutputStream outStream) {
		assert solver != null;
		assert outStream != null;
		this.solver = solver;
		this.outStream = outStream;
	}

	void write() {
		try (PrintStream out = new PrintStream(outStream);) {
			writeVariableDeclarations(out);
			writeConstraints(out);
		}
	}
	
	private void writeVariableDeclarations(PrintStream out) {
		out.println(DECL);
		for (ISatVariable variable : this.solver.getVariables()) {
			String name = variable.getName();
			Type type = variable.getType();
			out.print(SPACE + SPACE);
			if (TypeBoolean.isBoolean(type)) {
				out.print(TYPE_BOOLE + SPACE);
			} else if (TypeInteger.isInteger(type)) {
				out.print(TYPE_INT + SPACE);
				out.print(LBRACK);
				out.print(variable.getLower());
				out.print(COMMA);
				out.print(variable.getUpper());
				out.print(RBRACK + SPACE);
			} else if (TypeReal.isReal(type)) {
				out.print(TYPE_FLOAT + SPACE);
				out.print(LBRACK);
				out.print(variable.getLower());
				out.print(COMMA);
				out.print(variable.getUpper());
				out.print(RBRACK + SPACE);
			} else {
				assert false : type;
			}
			out.print(name);
			out.println(SEMICOLON);
		}
		out.println();
	}

	private void writeConstraints(PrintStream out) {
//		Expression constraintExpression = null;
//		for (Expression expression : solver.getConstraints()) {
//			if (constraintExpression == null) {
//				constraintExpression = expression;
//			} else {
//				constraintExpression = constraintExpression.and(expression);
//			}
//		}
		out.println(EXPR);
		for (Expression expression : solver.getConstraints()) {
		    out.print(SPACE + SPACE);
			out.print(translateExpression(expression));
			out.println(SEMICOLON);
		}
//		out.print(translateExpression(constraintExpression));
//		out.println(SEMICOLON);
	}

	private String translateExpression(Expression expression) {
		assert expression != null;
		if (expression instanceof ExpressionIdentifierStandard) {
			ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
			return expressionIdentifier.getName();
		} else if (ExpressionLiteral.isLiteral(expression)) {
			return getValue(expression).toString();
		} else if (expression instanceof ExpressionOperator) {
			return translateExpressionOperator((ExpressionOperator) expression);
		} else {
			assert false;
			return null;
		}
	}

	private String translateExpressionOperator(ExpressionOperator expression) {
		ISatOperator operator = EPMC_TO_ISAT3.get(expression.getOperator().getIdentifier());
		assert operator != null : expression.getOperator();
		StringBuilder result = new StringBuilder();
		if (operator.isPrefix()) {
			result.append(operator.getIdentifer());
			result.append(SPACE);
			result.append(LBRACE);
			for (Expression operand : expression.getOperands()) {
				result.append(translateExpression(operand));
				result.append(COMMA);
			}
			result.delete(result.length() - 1, result.length());
			result.append(RBRACE);
		} else if (operator.isInfix()) {
			result.append(LBRACE);
			for (Expression operand : expression.getOperands()) {
				result.append(translateExpression(operand));
				result.append(operator.getIdentifer());
			}
			result.delete(result.length() - operator.getIdentifer().length(),
					result.length());
			result.append(RBRACE);
		} else {
			assert false;
			return null;
		}
		return result.toString();
	}
	
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.isLiteral(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression);
        return expressionLiteral.getValue();
    }
}
