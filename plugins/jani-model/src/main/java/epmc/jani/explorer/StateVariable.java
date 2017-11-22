package epmc.jani.explorer;

import epmc.expression.Expression;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.Type;
import epmc.value.TypeNumBitsKnown;
import epmc.value.Value;

public final class StateVariable {
    public final static class Builder {
        private Expression identifier;
        private boolean permanent;
        private Type type;
        private Expression initialValue;
        private boolean decision;

        public Builder setIdentifier(Expression identifier) {
            this.identifier = identifier;
            return this;
        }

        private Expression getIdentifier() {
            return identifier;
        }

        public Builder setPermanent(boolean permanent) {
            this.permanent = permanent;
            return this;
        }

        private boolean isPermanent() {
            return permanent;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        private Type getType() {
            return type;
        }

        public Builder setInitialValue(Expression initialValue) {
            this.initialValue = initialValue;
            return this;
        }

        private Expression getInitialValue() {
            return initialValue;
        }

        public Builder setDecision(boolean decision) {
            this.decision = decision;
            return this;
        }

        private boolean isDecision() {
            return decision;
        }

        public StateVariable build() {
            return new StateVariable(this);
        }
    }

    private final Expression identifier;
    private final Expression initialValueExpression;
    private final boolean permanent;
    private final Type type;
    private final int numBits;
    private final Value initialValue;
    private final boolean decision;

    private StateVariable(Builder builder) {
        assert builder != null;
        assert builder.getIdentifier() != null;
        assert builder.getType() != null;

        identifier = builder.getIdentifier();
        initialValueExpression = builder.getInitialValue();
        permanent = builder.isPermanent();
        type = builder.getType();
        if (TypeNumBitsKnown.getNumBits(type) == TypeNumBitsKnown.UNKNOWN) {
            numBits = Integer.MAX_VALUE;
        } else {
            numBits = TypeNumBitsKnown.getNumBits(type);
        }
        if (initialValueExpression != null) {
            initialValue = UtilEvaluatorExplicit.evaluate(initialValueExpression);
        } else {
            initialValue = null;
        }
        decision = builder.isDecision();
    }

    public Expression getIdentifier() {
        return identifier;
    }

    public Expression getInitialValueExpression() {
        return initialValueExpression;
    }

    public Value getInitialValue() {
        return initialValue;
    }

    public int getNumBits() {
        return numBits;
    }

    public Type getType() {
        return type;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isDecision() {
        return decision;
    }
}
