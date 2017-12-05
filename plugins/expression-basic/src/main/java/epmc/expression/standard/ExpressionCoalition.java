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
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

public final class ExpressionCoalition implements Expression {
    public final static class Builder {
        private Positional positional;
        private Expression quantifier;
        private List<Expression> players;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }

        private Positional getPositional() {
            return positional;
        }

        public Builder setQuantifier(Expression quantifier) {
            this.quantifier = quantifier;
            return this;
        }

        private Expression getQuantifier() {
            return quantifier;
        }

        public Builder setPlayers(List<Expression> players) {
            this.players = players;
            return this;
        }

        private List<Expression> getPlayers() {
            return players;
        }

        public Builder setChildren(List<Expression> children) {
            this.quantifier = children.get(0);
            this.players = new ArrayList<>(children.size() - 1);
            this.players.addAll(children.subList(1, children.size()));
            return this;
        }

        public ExpressionCoalition build() {
            return new ExpressionCoalition(this);
        }
    }

    public static boolean is(Expression expression) {
        return expression instanceof ExpressionCoalition;
    }

    public static ExpressionCoalition as(Expression expression) {
        if (!is(expression)) {
            return null;
        } else {
            return (ExpressionCoalition) expression;
        }
    }

    private final Positional positional;
    private final Expression quantifier;
    private final List<Expression> players;

    private ExpressionCoalition(Builder builder) {
        assert builder != null;
        assert builder.getQuantifier() != null;
        assert builder.getPlayers() != null;
        for (Expression player : builder.getPlayers()) {
            assert player != null;
            assert player instanceof ExpressionIdentifier
            || ExpressionLiteral.is(player);
        }
        this.positional = builder.getPositional();
        this.quantifier = builder.getQuantifier();
        this.players = new ArrayList<>(builder.getPlayers());
    }

    public Expression getInner() {
        return quantifier;
    }

    public List<SMGPlayer> getPlayers() {
        List<SMGPlayer> players = new ArrayList<>();
        for (Expression playerExpr : this.players) {
            players.add(new SMGPlayerImpl(playerExpr));
        }
        return players;
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        //    	System.out.println("A " + this);
        //  	System.out.println("B " + children);
        return new Builder().setPositional(positional)
                .setChildren(children).build();
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> result = new ArrayList<>();
        result.add(quantifier);
        result.addAll(players);
        return result;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<<");
        int playerNumber = 0;
        List<SMGPlayer> players = getPlayers();
        for (SMGPlayer player : players) {
            builder.append(player.getExpression());
            if (playerNumber < players.size()) {
                builder.append(",");
            }
            playerNumber++;
        }
        builder.append(">> ");
        builder.append(getInner());
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        builder.append(getInner());
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Expression other = (Expression) obj;
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
        return true;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }

        return hash;
    }

    @Override
    public Expression replacePositional(Positional positional) {
        return new ExpressionCoalition.Builder()
                .setPlayers(players)
                .setQuantifier(quantifier)
                .setPositional(positional)
                .build();
    }
}
