/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.constraintsolver.smtlib;

import static epmc.sexpression.UtilSExpression.newSExpression;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.constraintsolver.smtlib.options.SMTLibVersion;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.modelchecker.UtilModelChecker;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorIsNegInf;
import epmc.operator.OperatorIsPosInf;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLn;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorSubtract;
import epmc.options.Options;
import epmc.sexpression.SExpression;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class InputWriter {
    private final static String SPACE = " ";
    private final static String COLON = ":";
    private final static String LANGLE = "(";
    private final static String RANGLE = ")";
    private final static String GEQ = ">=";
    private final static String LEQ = "<=";
    private final static String ASSERT = "assert";
    private final static String DECLARE_CONST = "declare-const";
    private final static String DECLARE_FUN = "declare-fun";
    private final static String SET_OPTION = "set-option";
    private final static String PRINT_SUCCESS = "print-success";
    private final static String PRODUCE_MODELS = "produce-models";
    private final static String GET_VALUE = "get-value";
    private final static String FALSE = "false";
    private final static String TRUE = "true";
    private final static String SET_LOGIC = "set-logic";
    private final static String CHECK_SAT = "check-sat";
    private final static String TYPE_BOOLEAN = "Boolean";
    private final static String TYPE_INT = "Int";
    private final static String TYPE_REAL = "Real";
    private final static String QF_NRA = "QF_NRA";
    private final static String QF_NIA = "QF_NIA";
    private final static String AUFLIRA = "AUFLIRA";
    private final static Map<Operator,SMTLibOperator> EPMC_TO_SMTLIB;
    static {
        Map<Operator,SMTLibOperator> iscasMCToISMTLib = new HashMap<>();
        iscasMCToISMTLib.put(OperatorAnd.AND, newOperator()
                .setIdentifier("and").build());
        iscasMCToISMTLib.put(OperatorOr.OR, newOperator()
                .setIdentifier("or").build());
        /* "nand" not supported */ 
        /* "nor" not supported */ 
        /* "xor" not supported */ 
        iscasMCToISMTLib.put(OperatorIff.IFF, newOperator()
                .setIdentifier("nxor").build());
        iscasMCToISMTLib.put(OperatorImplies.IMPLIES, newOperator()
                .setIdentifier("impl").build());
        iscasMCToISMTLib.put(OperatorNot.NOT, newOperator()
                .setIdentifier("not").build());

        iscasMCToISMTLib.put(OperatorAddInverse.ADD_INVERSE, newOperator()
                .setIdentifier("-").build());
        iscasMCToISMTLib.put(OperatorAdd.ADD, newOperator()
                .setIdentifier("+").build());
        iscasMCToISMTLib.put(OperatorSubtract.SUBTRACT, newOperator()
                .setIdentifier("-").build());
        iscasMCToISMTLib.put(OperatorMultiply.MULTIPLY, newOperator()
                .setIdentifier("*").build());
        iscasMCToISMTLib.put(OperatorDivide.DIVIDE, newOperator()
                .setIdentifier("/").build());
        /* "abs" not supported */ 
        iscasMCToISMTLib.put(OperatorMin.MIN, newOperator()
                .setIdentifier("min").build());
        iscasMCToISMTLib.put(OperatorMax.MAX, newOperator()
                .setIdentifier("max").build());
        /* "exp" not supported */
        /* "exp2" not supported */
        /* "exp10" not supported */
//        iscasMCToISMTLib.put(OperatorLn.LN, newOperator()
  //              .setIdentifier("log").build());
        /* "log2" not supported */
        /* "log10" not supported */
        /* "sin" not supported */
        /* "cos" not supported */
        /* "pow" not supported */
        /* "nrt" not supported */

        iscasMCToISMTLib.put(OperatorLt.LT, newOperator()
                .setIdentifier("<").build());
        iscasMCToISMTLib.put(OperatorLe.LE, newOperator()
                .setIdentifier("<=").build());
        iscasMCToISMTLib.put(OperatorEq.EQ, newOperator()
                .setIdentifier("=").build());
        iscasMCToISMTLib.put(OperatorNe.NE, newOperator()
                .setIdentifier("!=").build());
        iscasMCToISMTLib.put(OperatorGt.GT, newOperator()
                .setIdentifier(">").build());
        iscasMCToISMTLib.put(OperatorGe.GE, newOperator()
                .setIdentifier(">=").build());

        EPMC_TO_SMTLIB = Collections.unmodifiableMap(iscasMCToISMTLib);
    }
    private static SMTLibOperator.Builder newOperator() {
        return new SMTLibOperator.Builder();
    }

    private final OutputStream outStream;
    private final ConstraintSolverSMTLib solver;
    private final SMTLibVersion version;

    InputWriter(ConstraintSolverSMTLib solver, OutputStream outStream) {
        assert solver != null;
        assert outStream != null;
        this.solver = solver;
        this.outStream = outStream;
        version = Options.get().get(OptionsSMTLib.SMTLIB_VERSION);
    }

    void write() {
        try (PrintStream out = new PrintStream(outStream);) {
            setOption(out, PRINT_SUCCESS, FALSE);
            setOption(out, PRODUCE_MODELS, TRUE);
            String logic = computeLogic();
            command(out, SET_LOGIC, logic);
            writeVariableDeclarations(out);
            writeConstraints(out);
            command(out, CHECK_SAT);
            for (SMTLibVariable variable : solver.getVariables()) {
                command(out, GET_VALUE, (Object) new String[]{variable.getName()});
            }
        }		
    }

    private void writeVariableDeclarations(PrintStream out)  {
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (SMTLibVariable variable : solver.getVariables()) {
            String typeString = typeToString(variable.getType());
            String name = variable.getName();
            if (version == SMTLibVersion.V20) {
                command(out, DECLARE_FUN, name, new Object[0], typeString);
            } else if (version == SMTLibVersion.V25) {
                command(out, DECLARE_CONST, name, typeString);
            } else {
                assert false;
            }
            Value lower = variable.getLower();
            Expression varExpr = null;
            OperatorEvaluator isNegInf = ContextValue.get().getEvaluator(OperatorIsNegInf.IS_NEG_INF, lower.getType());
            isNegInf.apply(cmp, lower);
            if (lower != null && !cmp.getBoolean()) {
                varExpr = UtilModelChecker.parseExpression(lower.toString());
                command(out, ASSERT,
                        LANGLE + GEQ + SPACE + name + SPACE + translateExpression(varExpr) + RANGLE);
            }
            Value upper = variable.getUpper();
            OperatorEvaluator isPosInf = ContextValue.get().getEvaluator(OperatorIsPosInf.IS_POS_INF, TypeReal.get());
            isPosInf.apply(cmp, lower);
            if (upper != null && !cmp.getBoolean()) {
                varExpr = UtilModelChecker.parseExpression(upper.toString());
                command(out, ASSERT,
                        LANGLE + LEQ + SPACE + name + SPACE + translateExpression(varExpr) + RANGLE);
            }
        }
    }

    private void writeConstraints(PrintStream out) {
        for (Expression expression : solver.getConstraints()) {
            command(out, ASSERT, expression);
        }
    }

    private SExpression translateExpression(Expression expression) {
        assert expression != null;
        if (expression instanceof ExpressionIdentifier) {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            return newSExpression(expressionIdentifier.getName());
        } else if (ExpressionLiteral.is(expression)) {
            return newSExpression(getValue(expression));
        } else if (expression instanceof ExpressionOperator) {
            return translateExpressionOperator((ExpressionOperator) expression);
        } else {
            assert false;
            return null;
        }
    }

    private SExpression translateExpressionOperator(ExpressionOperator expression)
    {
        SMTLibOperator operator = EPMC_TO_SMTLIB.get(expression.getOperator());
        assert operator != null : expression.getOperator();
        SExpression[] result = new SExpression[expression.getChildren().size() + 1];
        result[0] = newSExpression(operator.getIdentifer());

        int i = 0;
        for (Expression operand : expression.getOperands()) {
            result[i + 1] = translateExpression(operand);
            i++;
        }

        return newSExpression(result);
    }

    private void setOption(PrintStream out, String option, String value) {
        out.print(LANGLE + SET_OPTION + SPACE + COLON);
        out.print(option);
        out.print(SPACE);
        out.print(value);
        out.println(RANGLE);
    }

    private void command(PrintStream out, String command, Object... parameters) {
        assert command != null;
        assert parameters != null;
        out.print(LANGLE);
        out.print(command);
        if (parameters.length > 0) {
            out.print(SPACE);
        }
        for (int i = 0; i < parameters.length; i++) {
            writeObject(out, parameters[i]);
            if (i < parameters.length - 1) {
                out.print(SPACE);
            }
        }
        out.println(RANGLE);
    }

    private void writeObject(PrintStream out, Object object)
    {
        if (object instanceof Object[]) {
            out.print(LANGLE);
            Object[] array = (Object[]) object;
            for (int j = 0; j < array.length; j++) {
                writeObject(out, array[j]);
                if (j < array.length - 1) {
                    out.print(SPACE);
                }
            }
            out.print(RANGLE);
        } else if (object instanceof Expression) {
            out.print(translateExpression((Expression) object));
        } else {
            out.print(object);
        }
    }

    private String computeLogic() {
        boolean hasReal = false;
        boolean hasInt = false;
        for (SMTLibVariable variable : solver.getVariables()) {
            hasReal = hasReal | !TypeInteger.is(variable.getType()) && TypeReal.is(variable.getType());
            hasInt = hasInt | TypeInteger.is(variable.getType());
        }
        if (hasReal && !hasInt) {
            return QF_NRA;
        } else if (!hasReal && hasInt) {
            return QF_NIA;			
        } else {
            return AUFLIRA;
        }
    }

    private String typeToString(Type type) {
        assert type != null;
        if (TypeBoolean.is(type)) {
            return TYPE_BOOLEAN;
        } else if (TypeInteger.is(type)) {
            return TYPE_INT;
        } else if (TypeReal.is(type)) {
            return TYPE_REAL;
        } else {
            assert false : type;
        return null;
        }
    }

    private static String getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.is(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return expressionLiteral.getValue();
    }
}
