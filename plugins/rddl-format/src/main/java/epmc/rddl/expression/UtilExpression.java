package epmc.rddl.expression;

import java.util.List;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.rddl.value.OperatorDistributionBernoulli;
import epmc.rddl.value.OperatorDistributionDiracDelta;
import epmc.rddl.value.OperatorDistributionDiscrete;
import epmc.rddl.value.OperatorDistributionKronDelta;
import epmc.rddl.value.OperatorDistributionNormal;
import epmc.rddl.value.OperatorSwitch;
import epmc.rddl.value.TypeRDDLObject;
import epmc.rddl.value.UtilValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAnd;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorSubtract;

public final class UtilExpression {
    private static boolean alwaysBraces(Operator operator) {
        switch (operator.getIdentifier()) {
        case OperatorNot.IDENTIFIER:
            return false;
        case OperatorAnd.IDENTIFIER:
            return false;
        case OperatorDistributionBernoulli.IDENTIFIER:
            return true;
        case OperatorDistributionKronDelta.IDENTIFIER:
            return true;
        case OperatorDistributionDiracDelta.IDENTIFIER:
            return true;
        case OperatorIff.IDENTIFIER:
            return false;
        case OperatorAdd.IDENTIFIER:
            return false;
        case OperatorSubtract.IDENTIFIER:
            return false;
        case OperatorDivide.IDENTIFIER:
            return false;
        case OperatorMultiply.IDENTIFIER:
            return false;
        case OperatorLt.IDENTIFIER:
            return false;
        case OperatorLe.IDENTIFIER:
            return false;
        case OperatorEq.IDENTIFIER:
            return false;
        case OperatorGe.IDENTIFIER:
            return false;
        case OperatorGt.IDENTIFIER:
            return false;
        case OperatorOr.IDENTIFIER:
            return false;
        case OperatorImplies.IDENTIFIER:
            return false;
        case OperatorNe.IDENTIFIER:
            return false;
        default:
            assert false : operator.getIdentifier();
            return false;
        }
    }
    
    public static String operatorToString(Operator operator) {
        switch (operator.getIdentifier()) {
        case OperatorNot.IDENTIFIER:
            return "~";
        case OperatorAnd.IDENTIFIER:
            return "^";
        case OperatorDistributionBernoulli.IDENTIFIER:
            return "Bernoulli";
        case OperatorDistributionKronDelta.IDENTIFIER:
            return "KronDelta";
        case OperatorDistributionDiracDelta.IDENTIFIER:
            return "DiracDelta";
        case OperatorIff.IDENTIFIER:
            return "<=>";
        case OperatorAdd.IDENTIFIER:
            return "+";
        case OperatorSubtract.IDENTIFIER:
            return "-";
        case OperatorDivide.IDENTIFIER:
            return "/";
        case OperatorMultiply.IDENTIFIER:
            return "*";
        case OperatorLt.IDENTIFIER:
            return "<";
        case OperatorLe.IDENTIFIER:
            return "<=";
        case OperatorEq.IDENTIFIER:
            return "==";
        case OperatorGe.IDENTIFIER:
            return ">=";
        case OperatorGt.IDENTIFIER:
            return ">";
        case OperatorOr.IDENTIFIER:
            return "|";
        case OperatorImplies.IDENTIFIER:
            return "=>";
        case OperatorNe.IDENTIFIER:
            return "~=";
        default:
            assert false : operator.getIdentifier();
            return null;
        }
    }
    
    public static String toString(Expression expression) {
    	assert expression != null;
        StringBuilder builder = new StringBuilder();
        if (expression instanceof ExpressionIdentifierStandard) {
        	ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            builder.append(expressionIdentifier.getName());
        } else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
            ExpressionRDDLQuantifiedIdentifier quantified = (ExpressionRDDLQuantifiedIdentifier) expression;
            builder.append(quantified.getName());
            if (quantified.getParameters().size() > 0) {
                builder.append("(");
                for (int i = 0; i < quantified.getParameters().size(); i++) {
                    builder.append(quantified.getParameters().get(i));
                    if (i < quantified.getParameters().size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append(")");
            }

        } else if (expression instanceof ExpressionOperator) {
        	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Operator operator = expressionOperator.getOperator();
            if (operator.getIdentifier().equals("ite")) {
                builder.append("if (");
                builder.append(toString(expressionOperator.getOperand1()));
                builder.append(") then (");
                builder.append(toString(expressionOperator.getOperand2()));
                builder.append(") else (");
                builder.append(toString(expressionOperator.getOperand3()));
                builder.append(")");
            } else if (operator.getIdentifier().equals(OperatorSwitch.IDENTIFIER)) {
                List<Expression> operands = expressionOperator.getOperands();
                builder.append("switch (");
                builder.append(toString(operands.get(0)));
                builder.append(") {");
                for (int i = 0; i < (operands.size() - 1) / 2; i++) {
                    builder.append("case ");
                    builder.append(toString(operands.get(i * 2 + 1)));
                    builder.append(" : ");
                    builder.append(toString(operands.get(i * 2 + 2)));
                    if (i < (operands.size() - 1) / 2 - 1) {
                        builder.append(", ");
                    }
                }
                builder.append("}");
            } else if (operator.getIdentifier().equals(OperatorDistributionNormal.IDENTIFIER)) {
                builder.append("Normal(");
                builder.append(toString(expression.getChildren().get(0)));
                builder.append(",");
                builder.append(toString(expression.getChildren().get(1)));                
                builder.append(")");
            } else if (operator.getIdentifier().equals(OperatorDistributionDiscrete.IDENTIFIER)) {
                List<Expression> operands = expressionOperator.getOperands();
                builder.append("Discrete(");
                builder.append(operands.get(0));
                builder.append(", ");
                for (int i = 0; i < (operands.size() - 2) / 2; i++) {
                    builder.append(toString(operands.get(i * 2 + 2)));
                    builder.append(" : ");
                    builder.append(toString(operands.get(i * 2 + 3)));
                    if (i < (operands.size() - 2) / 2 - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(")");
            } else {
                String operatorName = operatorToString(operator);
                if (expressionOperator.getOperands().size() == 1) {
                    Expression operand = expressionOperator.getOperand1();
                    boolean needsBraces =
                            alwaysBraces(operator) ||
                            (!(operand instanceof ExpressionIdentifierStandard)
                            && !(operand instanceof ExpressionLiteral)
                            && !(operand instanceof ExpressionRDDLQuantifiedIdentifier))
                            ;
                    builder.append(operatorName);
                    if (needsBraces) {
                        builder.append("(");
                    }
                    builder.append(toString(operand));
                    if (needsBraces) {
                        builder.append(")");
                    }
                } else if (expressionOperator.getOperands().size() == 2) {
                    Expression operand1 = expressionOperator.getOperand1();
                    boolean needsBraces1 = alwaysBraces(operator) ||                            
                            (!(operand1 instanceof ExpressionIdentifierStandard)
                            && !(operand1 instanceof ExpressionLiteral)
                            && !(operand1 instanceof ExpressionRDDLQuantifiedIdentifier));
                    Expression operand2 = expressionOperator.getOperand2();
                    boolean needsBraces2 = alwaysBraces(operator) ||
                            (!(operand1 instanceof ExpressionIdentifierStandard)
                            && !(operand2 instanceof ExpressionLiteral)
                            && !(operand2 instanceof ExpressionRDDLQuantifiedIdentifier));
                    if (needsBraces1) {
                        builder.append("(");
                    }
                    builder.append(toString(operand1));
                    if (needsBraces1) {
                        builder.append(")");
                    }
                    builder.append(" " + operatorName + " ");
                    if (needsBraces2) {
                        builder.append("(");
                    }
                    builder.append(toString(operand2));
                    if (needsBraces2) {
                        builder.append(")");
                    }
                } else {
                    assert false : expression + " " + expressionOperator.getOperator() + " " + expressionOperator.getOperands().size();
                }
            }
        } else if (expression instanceof ExpressionLiteral) {
        	ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
            builder.append(UtilValue.toString(expressionLiteral.getValue()));
        } else if (expression instanceof ExpressionRDDLQuantifier) {
            ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
            Operator operator = quantifier.getOperator();
            switch (operator.getIdentifier()) {
            case "add":
                builder.append("sum");
                break;
            case "multiply":
                builder.append("prod");
                break;
            case "and":
            	builder.append("forall");
            	break;
            case "or":
            	builder.append("exists");
            	break;
            default:
            	assert false : operator.getIdentifier();
                builder.append(operator.toString().toLowerCase());
                break;
            }
            builder.append("_{");
            for (int i = 0; i < quantifier.getParameters().size(); i++) {
                Expression parameter = quantifier.getParameters().get(i);
                TypeRDDLObject range = quantifier.getRanges().get(i);
                builder.append(parameter + " : " + range);
                if (i < quantifier.getParameters().size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append("} ");
            builder.append("[");
            builder.append(toString(quantifier.getChildren().get(1)));
            builder.append("]");
        } else {
            assert false : expression;
        }
        return builder.toString();
    }
    
    private UtilExpression() {
    }
}
