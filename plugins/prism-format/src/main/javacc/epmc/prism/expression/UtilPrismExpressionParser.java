package epmc.prism.expression;

import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.value.Operator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.operator.OperatorAddInverse;
import epmc.value.operator.OperatorAnd;
import epmc.value.operator.OperatorIff;
import epmc.value.operator.OperatorImplies;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorOr;

public final class UtilPrismExpressionParser {
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

    private static final class ParseValueProviderReal implements ExpressionLiteral.ValueProvider {
        private final String string;

        private ParseValueProviderReal(String string) {
            assert string != null;
            this.string = string;
        }

        @Override
        public Value provideValue() {
            return UtilValue.newValue(TypeReal.get(), string);
        }
    }

    private static final class ParseValueProviderInteger implements ExpressionLiteral.ValueProvider {
        private final String string;

        private ParseValueProviderInteger(String string) {
            assert string != null;
            this.string = string;
        }

        @Override
        public Value provideValue() {
            return UtilValue.newValue(TypeInteger.get(), string);
        }
    }

    private static final class ParseValueProviderBoolean implements ExpressionLiteral.ValueProvider {
        private final String string;

        private ParseValueProviderBoolean(String string) {
            assert string != null;
            this.string = string;
        }

        @Override
        public Value provideValue() {
            return UtilValue.newValue(TypeBoolean.get(), string);
        }
    }

    public static ExpressionLiteral.ValueProvider newParseValueProviderReal(String string) {
        assert string != null;
        return new ParseValueProviderReal(string);
    }

    public static ExpressionLiteral.ValueProvider newParseValueProviderInteger(String string) {
        assert string != null;
        return new ParseValueProviderInteger(string);
    }

    public static ExpressionLiteral.ValueProvider newParseValueProviderBoolean(String string) {
        assert string != null;
        return new ParseValueProviderBoolean(string);
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

    static ExpressionTemporal newTemporal
    (TemporalType type, List<Expression> operands,
            List<TimeBound> timeBounds) {
        assert type != null;
        assert operands != null;
        assert operands != null;
        assert timeBounds != null;
        for (TimeBound timeBound : timeBounds) {
            assert timeBound != null;
        }
        return new ExpressionTemporal
                (operands, type, timeBounds, null);
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

    static ExpressionTemporal newTemporal
    (TemporalType type, Expression operand, TimeBound bound) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (operand, type, bound, null);
    }

    static ExpressionTemporal newTemporal
    (TemporalType type, Expression op1, Expression op2,
            TimeBound bound) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (op1, op2, type, bound, null);
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
                        .setContent("true")
                        .build())
                .setValueProvider(new ParseValueProviderBoolean("true"))
                .build();
    }

    private UtilPrismExpressionParser() {
    }
}
