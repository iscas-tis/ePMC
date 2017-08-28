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

package epmc.expression.standard;

import java.util.List;

import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.prism.value.OperatorPRISMPow;
import epmc.value.Operator;
import epmc.value.operator.OperatorAdd;
import epmc.value.operator.OperatorAddInverse;
import epmc.value.operator.OperatorAnd;
import epmc.value.operator.OperatorCeil;
import epmc.value.operator.OperatorDivideIgnoreZero;
import epmc.value.operator.OperatorFloor;
import epmc.value.operator.OperatorGe;
import epmc.value.operator.OperatorIff;
import epmc.value.operator.OperatorImplies;
import epmc.value.operator.OperatorIte;
import epmc.value.operator.OperatorLe;
import epmc.value.operator.OperatorLog;
import epmc.value.operator.OperatorMax;
import epmc.value.operator.OperatorMin;
import epmc.value.operator.OperatorMod;
import epmc.value.operator.OperatorMultiply;
import epmc.value.operator.OperatorMultiplyInverse;
import epmc.value.operator.OperatorNe;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorOr;
import epmc.value.operator.OperatorPow;
import epmc.value.operator.OperatorSubtract;

public class ExpressionOperatorProcessor implements JANI2PRISMProcessorStrict {

    private ExpressionOperator expressionOperator = null;
    private String prefix = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionOperator; 

        expressionOperator = (ExpressionOperator) obj;
        return this;
    }

    @Override
    public JANI2PRISMProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert expressionOperator != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null) {
            prism.append(prefix);
        }

        Operator operator = expressionOperator.getOperator();
        if (operator.equals(OperatorNot.NOT)
                || operator.equals(OperatorFloor.FLOOR)
                || operator.equals(OperatorCeil.CEIL)) {
            prism.append(operator);
            prism.append("(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(")");
        } else if (operator.equals(OperatorAddInverse.ADD_INVERSE)) {
            prism.append("-(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(")");
        } else if (operator.equals(OperatorIte.ITE)) {
            boolean needBraces = true;
            prism.append("(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(")");
            prism.append(" ? ");
            Expression exp = expressionOperator.getOperand2();
            if (exp instanceof ExpressionLiteral || exp instanceof ExpressionIdentifier) {
                needBraces = false;
            }
            if (needBraces) {
                prism.append("(");
            }
            prism.append(ProcessorRegistrar.getProcessor(exp).toPRISM());
            if (needBraces) {
                prism.append(")");
            }
            prism.append(" : ");
            needBraces = true;
            exp = expressionOperator.getOperand3();
            if (exp instanceof ExpressionLiteral || exp instanceof ExpressionIdentifier) {
                needBraces = false;
            }
            if (needBraces) {
                prism.append("(");
            }
            prism.append(ProcessorRegistrar.getProcessor(exp).toPRISM());
            if (needBraces) {
                prism.append(")");
            }
        } else if (operator.equals(OperatorMin.MIN)
                || operator.equals(OperatorMax.MAX)
                || operator.equals(OperatorPow.POW)
                || operator.equals(OperatorLog.LOG)) {
            prism.append(operator);
            prism.append("(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
        } else if (operator.equals(OperatorPRISMPow.PRISM_POW)) {
            prism.append("pow(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
        } else if (operator.equals(OperatorMod.MOD)) {
            prism.append("mod(");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand1()).toPRISM());
            prism.append(", ");
            prism.append(ProcessorRegistrar.getProcessor(expressionOperator.getOperand2()).toPRISM());
            prism.append(")");
        } else {
            List<Expression> children = expressionOperator.getChildren();
            String operatorSymbol = null;
            if (operator.equals(OperatorAnd.AND)) {
                operatorSymbol = "&";
            } else if (operator.equals(OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO)) {
                operatorSymbol = "/";
            } else if (operator.equals(OperatorGe.GE)) {
                operatorSymbol = ">=";
            } else if (operator.equals(OperatorLe.LE)) {
                operatorSymbol = "<=";
            } else if (operator.equals(OperatorIff.IFF)) {
                operatorSymbol = "<=>";
            } else if (operator.equals(OperatorImplies.IMPLIES)) {
                operatorSymbol = "=>";
            } else if (operator.equals(OperatorOr.OR)) {
                operatorSymbol = "|";
            } else if (operator.equals(OperatorMultiplyInverse.MULTIPLY_INVERSE)) {
                operatorSymbol = "1/";
            } else if (operator.equals(OperatorNe.NE)) {
                operatorSymbol = "!=";
            } else {
                //TODO: Maybe the following operators can be recovered
                /*
    		case OperatorAbs.IDENTIFIER:
    		case OperatorExp.IDENTIFIER:
    		case OperatorSgn.IDENTIFIER:
    		case OperatorTrunc.IDENTIFIER:
                 */
                //these no.
                /*
    		case OperatorAcosh.IDENTIFIER:
    		case OperatorAsinh.IDENTIFIER:
    		case OperatorAtanh.IDENTIFIER:
    		case OperatorCosh.IDENTIFIER:
    		case OperatorSinh.IDENTIFIER:
    		case OperatorTanh.IDENTIFIER:
    		case OperatorAcos.IDENTIFIER:
    		case OperatorAsin.IDENTIFIER:
    		case OperatorAtan.IDENTIFIER:
    		case OperatorCos.IDENTIFIER:
    		case OperatorSin.IDENTIFIER:
    		case OperatorTan.IDENTIFIER:
    			ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNKNOWN_OPERATOR, operator);
                 */
                operatorSymbol = operator.toString();
            }
            if (children.size() == 1) {
                prism.append(operatorSymbol)
                .append("(")
                .append(ProcessorRegistrar.getProcessor(children.get(0)).toPRISM())
                .append(")");
            } else {
                boolean remaining = false;
                for (Expression child : children) {
                    boolean needBraces = true;
                    if (remaining) {
                        prism.append(" " + operatorSymbol + " ");
                    } else {
                        remaining = true;
                    }
                    if (child instanceof ExpressionOperator) {
                        ExpressionOperator childOp = (ExpressionOperator) child;
                        if (operator.equals(childOp.getOperator())) {
                            needBraces = false;
                        }
                        if ((OperatorAdd.ADD.equals(expressionOperator.getOperator())
                                || OperatorSubtract.SUBTRACT.equals(expressionOperator.getOperator()))
                                && (OperatorMultiply.MULTIPLY.equals(childOp.getOperator())
                                        || OperatorDivideIgnoreZero.DIVIDE_IGNORE_ZERO.equals(childOp.getOperator()))) {
                            needBraces = false;
                        }
                    }
                    if (child instanceof ExpressionLiteral || child instanceof ExpressionIdentifier) {
                        needBraces = false;
                    }
                    if (needBraces) {
                        prism.append("(");
                    }
                    prism.append(ProcessorRegistrar.getProcessor(child)
                            .toPRISM());
                    if (needBraces) {
                        prism.append(")");
                    }
                }
            }
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert expressionOperator != null;

        for (Expression child : expressionOperator.getChildren()) {
            ProcessorRegistrar.getProcessor(child).validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert expressionOperator != null;

        boolean usesTransient = false;
        for (Expression child : expressionOperator.getChildren()) {
            usesTransient |= ProcessorRegistrar.getProcessor(child).usesTransientVariables();
        }

        return usesTransient;
    }	
}
