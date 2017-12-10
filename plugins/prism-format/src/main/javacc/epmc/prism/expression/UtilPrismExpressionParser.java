package epmc.prism.expression;

import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.ExpressionType;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.TimeBound;
import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;

public final class UtilPrismExpressionParser {
    private final static String TRUE = "true";
    
    public final static class InfoExpression {
        private Object part;
        private long initialLine;
        private long initialColumn;
        private long line;
        private long column;
        private int exprStart;
        private int exprEnd;
        private String string;

        private long lineInFile() {
            return line + initialLine - 1;
        }
        
        private long columnInFile() {
            if (line == 1) {
                return column + initialColumn - 1;
            } else {
                return column;
            }
        }
        
        InfoExpression(String string) {
            this.string = string;
        }

        public void setPart(Object part) {
            this.part = part;
        }
        
        public void setInitialLine(long initialLine) {
            this.initialLine = initialLine;
        }
        
        public void setInitialColumn(long initialColumn) {
            this.initialColumn = initialColumn;
        }
        
        public void setLine(long line) {
            this.line = line;
        }

        public void setColumn(long column) {
            this.column = column;
        }

        public void setStart(int exprStart) {
            this.exprStart = exprStart;
        }

        public void setEnd(int exprEnd) {
            this.exprEnd = exprEnd;
        }

        public Positional toPositional() {
            String content = null;
            if (string != null) {
                content = string.substring(exprStart, exprEnd);
            }
            Positional result = new Positional.Builder()
                    .setPart(part)
                    .setLine(lineInFile())
                    .setColumn(columnInFile())
                    .setContent(content)
                    .build();
            return result;
        }
    }

    static ExpressionOperator newOperator(Operator operator, Expression operand1, Expression operand2, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operand1, operand2)
                .setPositional(positional)
                .build();
    }

    static ExpressionOperator newOperator(Operator operator, Expression operand1, Expression operand2, Expression operand3, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operand1, operand2, operand3)
                .setPositional(positional)
                .build();
    }

    static ExpressionOperator newOperator(Operator operator, Expression operand, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operand)
                .setPositional(positional)
                .build();
    }

    static Expression and(Expression a, Expression b, InfoExpression info) {
        return newOperator(OperatorAnd.AND, a, b, info);
    }

    static Expression not(Expression expression, InfoExpression info) {
        return newOperator(OperatorNot.NOT, expression, info);
    }

    static Expression addInverse(Expression expression, InfoExpression info) {
        return newOperator(OperatorAddInverse.ADD_INVERSE, expression, info);
    }

    static Expression or(Expression a, Expression b, InfoExpression info) {
        return newOperator(OperatorOr.OR, a, b, info);
    }

    static Expression iff(Expression a, Expression b, InfoExpression info) {
        return newOperator(OperatorIff.IFF, a, b, info);
    }

    static Expression implies(Expression a, Expression b, InfoExpression info) {
        return newOperator(OperatorImplies.IMPLIES, a, b, info);
    }

    static ExpressionReward newRewardSteadyState(Expression structure, InfoExpression info) {
        return new ExpressionReward.Builder()
                .setReward(structure)
                .setRewardType(RewardType.STEADYSTATE)
                .setPositional(info.toPositional())
                .build();
    }

    static TimeBound newTimeBound(Expression left, Expression right,
            boolean leftOpen, boolean rightOpen, InfoExpression info) {
        return new TimeBound.Builder()
                .setLeft(left)
                .setRight(right)
                .setLeftOpen(leftOpen)
                .setRightOpen(rightOpen)
                .setPositional(info.toPositional())
                .build();
    }

    static TimeBound newTimeBound() {
        return new TimeBound.Builder()
                .build();
    }

    static ExpressionTemporalNext newTemporalNext(Expression operand,
            TimeBound timeBound, InfoExpression info) {
        assert operand != null;
        assert timeBound != null;
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionTemporalNext.Builder()
                .setOperand(operand)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }

    static ExpressionTemporalFinally newTemporalFinally(Expression operand,
            TimeBound timeBound, InfoExpression info) {
        assert operand != null;
        assert timeBound != null;
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionTemporalFinally.Builder()
                .setOperand(operand)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }

    static ExpressionTemporalGlobally newTemporalGlobally(Expression operand,
            TimeBound timeBound, InfoExpression info) {
        assert operand != null;
        assert timeBound != null;
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionTemporalGlobally.Builder()
                .setOperand(operand)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }

    static ExpressionTemporalRelease newTemporalRelease(Expression operandLeft,
            Expression operandRight,
            TimeBound timeBound,
            InfoExpression info) {
        assert operandLeft != null;
        assert operandRight != null;
        assert timeBound != null;
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionTemporalRelease.Builder()
                .setOperandLeft(operandLeft)
                .setOperandRight(operandRight)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }

    static ExpressionTemporalUntil newTemporalUntil(Expression operandLeft,
            Expression operandRight,
            TimeBound timeBound,
            InfoExpression info) {
        assert operandLeft != null;
        assert operandRight != null;
        assert timeBound != null;
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionTemporalUntil.Builder()
                .setOperandLeft(operandLeft)
                .setOperandRight(operandRight)
                .setTimeBound(timeBound)
                .setPositional(positional)
                .build();
    }

    static ExpressionReward newRewardInstantaneous
    (Expression structure, Expression time, InfoExpression info) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.INSTANTANEOUS)
                .setReward(structure)
                .setTime(time)
                .setPositional(info.toPositional())
                .build();
    }

    static ExpressionReward newRewardReachability
    (Expression structure, Expression reachSet, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.REACHABILITY)
                .setReward(structure)
                .setReachSet(reachSet)
                .setPositional(positional)
                .build();
    }

    static ExpressionReward newRewardCumulative
    (Expression structure, Expression time, InfoExpression info) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.CUMULATIVE)
                .setReward(structure)
                .setTime(time)
                .setPositional(info.toPositional())
                .build();
    }

    static ExpressionReward newRewardDiscounted
    (Expression structure, Expression timebound, Expression discount,
            InfoExpression info) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.DISCOUNTED)
                .setReward(structure)
                .setTime(timebound)
                .setDiscount(discount)
                .setPositional(info.toPositional())
                .build();
    }

    static Positional newPositional(long line, long column) {
        assert line >= 0;
        assert column >= 0;
        return new Positional.Builder()
                .setLine(line)
                .setColumn(column)
                .build();
    }

    static ExpressionLiteral getTrue() {
        return new ExpressionLiteral.Builder()
                .setPositional(new Positional.Builder()
                        .setContent(TRUE)
                        .build())
                .setValue(TRUE)
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .build();
    }

    static ExpressionIdentifier newIdentifier(Object identifier, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionIdentifierStandard.Builder()
                .setName(identifier.toString())
                // TODO find out why setting the positional leads to problems in later code
//                .setPositional(positional)
                .build();
    }

    static ExpressionIdentifier newIdentifier(Object identifier) {
        return new ExpressionIdentifierStandard.Builder()
                .setName(identifier.toString())
                .build();
    }

    static ExpressionLiteral newLiteral(Object identifier, ExpressionType type,
            InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionLiteral.Builder()
                .setValue(identifier.toString())
                .setType(type)
                .setPositional(positional)
                .build();
    }

    static ExpressionLiteral newLiteral(Object identifier, ExpressionType type) {
        return new ExpressionLiteral.Builder()
                .setValue(identifier.toString())
                .setType(type)
                .build();
    }

    static ExpressionCoalition newCoalition(Expression quantifier, List<Expression> players,
            InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionCoalition.Builder()
                .setPlayers(players)
                .setQuantifier(quantifier)
                .setPositional(positional)
                .build();
    }
    
    private UtilPrismExpressionParser() {
    }
}
