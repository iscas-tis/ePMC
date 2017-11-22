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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

/**
 * Ernst Moritz Hahn
 * TODO should be split into specific classes for finally, until, etc.,
 * to improve extensibility
 */
public final class ExpressionTemporal implements Expression {
    public final static class Builder {
        private Positional positional;
        private List<Expression> children;
        private TemporalType type;
        private List<Boolean> leftOpen;
        private List<Boolean> rightOpen;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setChildren(List<Expression> children) {
            this.children = children;
            return this;
        }

        public Builder setChildren(Expression... children) {
            this.children = Arrays.asList(children);
            return this;
        }

        private List<Expression> getChildren() {
            return children;
        }

        public Builder setType(TemporalType type) {
            this.type = type;
            return this;
        }

        private TemporalType getType() {
            return type;
        }

        public Builder setLeftOpen(List<Boolean> leftOpen) {
            this.leftOpen = leftOpen;
            return this;
        }

        public Builder setLeftOpen(boolean... leftOpen) {
            this.leftOpen = new ArrayList<>();
            for (boolean open : leftOpen) {
                this.leftOpen.add(open);
            }
            return this;
        }

        private List<Boolean> getLeftOpen() {
            return leftOpen;
        }

        public Builder setRightOpen(List<Boolean> rightOpen) {
            this.rightOpen = rightOpen;
            return this;
        }

        public Builder setRightOpen(boolean... rightOpen) {
            this.rightOpen = new ArrayList<>();
            for (boolean open : rightOpen) {
                this.rightOpen.add(open);
            }
            return this;
        }

        private List<Boolean> getRightOpen() {
            return rightOpen;
        }

        public ExpressionTemporal build() {
            return new ExpressionTemporal(this);
        }
    }

    private final Positional positional;
    private final List<Expression> children = new ArrayList<>();
    private final TemporalType type;
    private final List<Boolean> leftOpen;
    private final List<Boolean> rightOpen;

    private ExpressionTemporal(Builder builder) {
        assert builder != null;
        assert builder.getType() != null;
        assert builder.getChildren() != null;
        for (Expression child : builder.getChildren()) {
            assert child != null;
        }
        assert builder.getLeftOpen() != null;
        for (Boolean open : builder.getLeftOpen()) {
            assert open != null;
        }
        assert builder.getRightOpen() != null;
        for (Boolean open : builder.getRightOpen()) {
            assert open != null;
        }
        this.positional = builder.getPositional();
        this.children.addAll(builder.getChildren());
        switch (builder.getType()) {
        case NEXT: case FINALLY: case GLOBALLY:
            assert builder.getChildren().size() == 3;
            assert builder.getLeftOpen().size() == 1;
            assert builder.getRightOpen().size() == 1;
            break;
        case RELEASE:
            assert builder.getChildren().size() == 4;
            assert builder.getLeftOpen().size() == 1;
            assert builder.getRightOpen().size() == 1;
            break;
        case UNTIL:
            assert builder.getChildren().size() >= 4;
            assert (builder.getChildren().size() - 4) % 3 == 0;
            int numOpen = (builder.getChildren().size() - 1) / 3;
            assert builder.getLeftOpen().size() == numOpen;
            assert builder.getRightOpen().size() == numOpen;
            break;
        default:
            assert false;
        }
        this.type = builder.getType();
        this.leftOpen = new ArrayList<>(builder.getLeftOpen());
        this.rightOpen = new ArrayList<>(builder.getRightOpen());
    }

    ExpressionTemporal(List<Expression> children,
            TemporalType type, List<Boolean> leftOpen, List<Boolean> rightOpen, Positional positional) {
        assert type != null;
        assert children != null;
        for (Expression child : children) {
            assert child != null;
        }
        assert leftOpen != null;
        for (Boolean open : leftOpen) {
            assert open != null;
        }
        for (Boolean open : rightOpen) {
            assert open != null;
        }
        this.positional = positional;
        this.children.addAll(children);
        switch (type) {
        case NEXT: case FINALLY: case GLOBALLY:
            assert children.size() == 3;
            assert leftOpen.size() == 1;
            assert rightOpen.size() == 1;
            break;
        case RELEASE:
            assert children.size() == 4;
            assert leftOpen.size() == 1;
            assert rightOpen.size() == 1;
            break;
        case UNTIL:
            assert children.size() >= 4;
            assert (children.size() - 4) % 3 == 0;
            int numOpen = (children.size() - 1) / 3;
            assert leftOpen.size() == numOpen;
            assert rightOpen.size() == numOpen;
            break;
        default:
            assert false;
        }
        this.type = type;
        this.leftOpen = new ArrayList<>(leftOpen);
        this.rightOpen = new ArrayList<>(rightOpen);
    }

    public ExpressionTemporal(Expression operand, TemporalType type, TimeBound bound, Positional positional) {
        this(Arrays.asList(new Expression[]{operand,
                bound.getLeft(), bound.getRight()}),
                type,
                Collections.singletonList(bound.isLeftOpen()),
                Collections.singletonList(bound.isRightOpen()), positional);
    }

    public ExpressionTemporal(Expression op1, Expression op2,
            TemporalType type, TimeBound bound, Positional positional) {
        this(Arrays.asList(new Expression[]{op1, op2,
                bound.getLeft(), bound.getRight()}),
                type,
                Collections.singletonList(bound.isLeftOpen()),
                Collections.singletonList(bound.isRightOpen()), positional);
    }

    public ExpressionTemporal(Expression operand, TemporalType type, Positional positional) {
        this(Collections.singletonList(operand),
                type,
                Collections.<Boolean>emptyList(),
                Collections.<Boolean>emptyList(), positional);
    }

    public ExpressionTemporal(List<Expression> expressions, TemporalType type, 
            List<TimeBound> timeBounds, Positional positional) {
        this(prepareChildren(expressions, timeBounds), type,
                prepareLeftOpen(timeBounds), prepareRightOpen(timeBounds), positional);
    }

    private static List<Expression> prepareChildren(List<Expression> ops,
            List<TimeBound> timeBounds) {
        ArrayList<Expression> children = new ArrayList<>();
        children.addAll(ops);
        for (TimeBound timeBound : timeBounds) {
            children.add(timeBound.getLeft());
            children.add(timeBound.getRight());
        }
        return children;
    }

    private static List<Boolean> prepareLeftOpen(List<TimeBound> timeBounds) {
        List<Boolean> result = new ArrayList<>();
        for (TimeBound timeBound : timeBounds) {
            result.add(timeBound.isLeftOpen());
        }
        return result;
    }

    private static List<Boolean> prepareRightOpen(List<TimeBound> timeBounds) {
        List<Boolean> result = new ArrayList<>();
        for (TimeBound timeBound : timeBounds) {
            result.add(timeBound.isRightOpen());
        }
        return result;
    }

    public TemporalType getTemporalType() {
        return type;
    }

    public boolean hasTimeBounds() {
        for (int boundNr = 0; boundNr < getNumOps(); boundNr++) {
            if (!getTimeBound().isUnbounded()) {
                return true;
            }
        }
        return false;
    }

    public TimeBound getTimeBound(int num) {
        assert getNumOps() + 2 * num + 1 <= getChildren().size();
        Expression left = getChildren().get(getNumOps() + 2 * num);
        Expression right = getChildren().get(getNumOps() + 2 * num + 1);
        return new TimeBound.Builder()
                .setLeft(left)
                .setLeftOpen(leftOpen.get(num))
                .setRight(right)
                .setRightOpen(rightOpen.get(num))
                .build();
    }

    public TimeBound getTimeBound() {
        return getTimeBound(0);
    }    

    public List<Expression> getOperands() {
        return Collections.unmodifiableList(getChildren().subList(0, getNumOps()));
    }

    public Expression getOperand1() {
        return getChildren().get(0);
    }

    public Expression getOperand2() {
        assert getNumOps() >= 2;
        return getChildren().get(1);
    }

    public int getNumOps() {
        switch (type) {
        case NEXT: case FINALLY: case GLOBALLY:
            return 1;
        case RELEASE:
            return 2;
        case UNTIL:
            return (getChildren().size() + 2) / 3;
        default:
            assert false;
            return -1;
        }
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        return new ExpressionTemporal.Builder()
                .setChildren(children)
                .setType(type)
                .setLeftOpen(leftOpen)
                .setRightOpen(rightOpen)
                .setPositional(positional)
                .build();
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    @Override
    public final String toString() {
        Iterator<Expression> opIter;
        StringBuilder builder = new StringBuilder();
        switch (type) {
        case NEXT: case FINALLY: case GLOBALLY: {
            builder.append(type);
            // TODO
            //            builder.append(getTimeBound(null));
            builder.append("(");
            builder.append(getChildren().get(0));
            builder.append(")");
            break;
        }
        case UNTIL: case RELEASE:
            if (type == TemporalType.UNTIL && getNumOps() == 2 && isTrue(getOperand1())) {
                builder.append("F");
                builder.append("(");
                builder.append(getOperand2());
                builder.append(")");
            } else
                if (type == TemporalType.RELEASE && getNumOps() == 2
                && isFalse(getOperand2())) {
                    builder.append("G");
                    builder.append("(");
                    builder.append(getOperand1());
                    builder.append(")");
                } else {
                    opIter = getOperands().iterator();
                    while (opIter.hasNext()) {
                        Expression child = opIter.next();
                        builder.append("(");
                        builder.append(child);
                        builder.append(")");
                        if (opIter.hasNext()) {
                            builder.append(type);
                            //TODO
                            //                        	builder.append(getTimeBound(null, timeBoundIndex));
                        }
                    }
                }
            break;
        default:
            assert (false);
        }
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionTemporal other = (ExpressionTemporal) obj;
        List<Expression> thisChildren = this.getChildren();
        List<Expression> otherChildren = other.getChildren();
        if (thisChildren.size() != otherChildren.size()) {
            return false;
        }
        for (int entry = 0; entry < thisChildren.size(); entry++) {
            if (!thisChildren.get(entry).equals(otherChildren.get(entry))) {
                return false;
            }
        }
        return this.type == other.type && this.leftOpen.equals(other.leftOpen)
                && this.rightOpen.equals(other.rightOpen);
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;            
        for (Boolean open : leftOpen) {
            hash = (open ? 1 : 0) + (hash << 6) + (hash << 16) - hash;
        }
        for (Boolean open : rightOpen) {
            hash = (open ? 1 : 0) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return !Boolean.valueOf(getValue(expressionLiteral));
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(getValue(expressionLiteral));
    }

    private static String getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionTemporal.Builder()
                .setChildren(children)
                .setLeftOpen(leftOpen)
                .setRightOpen(rightOpen)
                .setType(type)
                .setPositional(positional)
                .build();
    }
}
