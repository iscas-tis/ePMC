package epmc.qmc.expression;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.TimeBound;
import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorPow;

public final class UtilQMCExpressionParser {
    private final static String TRUE = "true";
    private final static String ONE_HALF = "0.5";
    
    public final static class InfoExpression {
        private long line;
        private long column;
        private int exprStart;
        private int exprEnd;
        private String string;

        InfoExpression(String string) {
            this.string = string;
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
            return new Positional.Builder()
                    .setLine(line)
                    .setColumn(column)
                    .setContent(content)
                    .build();
        }
    }

    static ExpressionOperator newOperator(Operator operator, Expression... operands) {
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operands)
                .build();
    }

    static ExpressionOperator newOperator(Operator operator, Expression operand1, Expression operand2, InfoExpression info) {
        Positional positional = info != null ? info.toPositional() : null;
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(operand1, operand2)
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

    static Expression addInverse(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAddInverse.ADD_INVERSE)
                .setOperands(expression)
                .build();
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

    static ExpressionReward newRewardSteadyState(Expression structure) {
        return new ExpressionReward.Builder()
                .setReward(structure)
                .setRewardType(RewardType.STEADYSTATE)
                .build();
    }

    static TimeBound newTimeBound(Expression left, Expression right,
            boolean leftOpen, boolean rightOpen) {
        return new TimeBound.Builder()
                .setLeft(left)
                .setRight(right)
                .setLeftOpen(leftOpen)
                .setRightOpen(rightOpen)
                .build();
    }

    static TimeBound newTimeBound() {
        return new TimeBound.Builder()
                .build();
    }

    static ExpressionReward newRewardInstantaneous
    (Expression structure, Expression time) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.INSTANTANEOUS)
                .setReward(structure)
                .setTime(time)
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
    (Expression structure, Expression time) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.CUMULATIVE)
                .setReward(structure)
                .setTime(time)
                .build();
    }

    static ExpressionReward newRewardDiscounted
    (Expression structure, Expression timebound, Expression discount) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.DISCOUNTED)
                .setReward(structure)
                .setTime(timebound)
                .setDiscount(discount)
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

    public static Expression newHadamardEntry() {
        ExpressionLiteral oneHalf = new ExpressionLiteral.Builder()
                .setValue(ONE_HALF)
                .setType(ExpressionTypeReal.TYPE_REAL)
                .build();
        return new ExpressionOperator.Builder()
            .setOperands(oneHalf, oneHalf)
            .setOperator(OperatorPow.POW)
            .build();
    }
    
    public static Expression newMHadamardEntry() {
        ExpressionLiteral oneHalf = new ExpressionLiteral.Builder()
                .setValue(ONE_HALF)
                .setType(ExpressionTypeReal.TYPE_REAL)
                .build();
        ExpressionOperator powOneHalf = new ExpressionOperator.Builder()
            .setOperands(oneHalf, oneHalf)
            .setOperator(OperatorPow.POW)
            .build();
        return new ExpressionOperator.Builder()
                .setOperands(powOneHalf)
                .setOperator(OperatorAddInverse.ADD_INVERSE)
                .build();
    }

    private UtilQMCExpressionParser() {
    }
}
