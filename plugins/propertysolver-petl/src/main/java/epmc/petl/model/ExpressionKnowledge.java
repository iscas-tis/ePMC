package epmc.petl.model;

import java.util.ArrayList;
import java.util.List;

import epmc.error.Positional;
import epmc.expression.Expression;

public class ExpressionKnowledge implements Expression{
	public final static class Builder {
		private Positional positional;
		private KnowledgeType type;
		private Expression quantifier;
		private List<String> players;
		
		public Positional getPositional() {
			return positional;
		}
		public Builder setPositional(Positional positional) {
			this.positional = positional;
			return this;
		}
		public KnowledgeType getType() {
			return type;
		}
		public Builder setType(KnowledgeType type) {
			this.type = type;
			return this;
		}
		public Expression getQuantifier() {
			return quantifier;
		}
		public Builder setQuantifier(Expression quantifier) {
			this.quantifier = quantifier;
			return this;
		}
		public List<String> getPlayers() {
			return players;
		}
		public Builder setPlayers(List<String> players) {
			this.players = players;
			return this;
		}
		public ExpressionKnowledge build() {
			return new ExpressionKnowledge(this);
		}
		
	}

	private Positional positional;
	private KnowledgeType type;
	private Expression quantifier;
	private List<String> players;
	
	public ExpressionKnowledge (Builder builder) {
		assert builder != null;
        assert builder.getQuantifier() != null;
        assert builder.getPlayers() != null;
        assert builder.getType() != null;
        for (String player : builder.getPlayers()) {
            assert player != null;
        }
        
        this.players = builder.players;
        this.quantifier = builder.quantifier;
        this.type = builder.type;
        this.positional = builder.positional;
	}
	
	public static boolean isKnowledge(Expression expression)
	{
		return expression instanceof ExpressionKnowledge;
	}
	
	public static ExpressionKnowledge asKnowledge(Expression expression)
	{
		if(isKnowledge(expression))
			return (ExpressionKnowledge) expression;
		else
			return null;
	}
	
	@Override
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<Expression>();
		result.add(quantifier);
		return result;
	}

	@Override
	public Expression replaceChildren(List<Expression> newChildren) {
		return new Builder()
				.setPlayers(players)
				.setPositional(positional)
				.setQuantifier(newChildren.get(0))
				.setType(type)
				.build();
	}

	@Override
	public Expression replacePositional(Positional positional) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Positional getPositional() {
		return positional;
	}

	@Override
	public int hashCode() {
		int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        for(String player : this.players)
        {
        	hash = player.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
	}

	@Override
	public boolean equals(Object obj) {
		assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionKnowledge other = (ExpressionKnowledge) obj;
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
        return this.type == other.type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append(" {");
        int playerNumber = 0;
        List<String> players = getPlayers();
        for (String player : players) {
            builder.append(player);
            if (playerNumber < players.size()-1) {
                builder.append(",");
            }
            playerNumber++;
        }
        builder.append("} ");
        builder.append(quantifier);
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
	}

	public KnowledgeType getType() {
		return type;
	}

	public void setType(KnowledgeType type) {
		this.type = type;
	}

	public Expression getQuantifier() {
		return quantifier;
	}

	public void setQuantifier(Expression quantifier) {
		this.quantifier = quantifier;
	}

	public List<String> getPlayers() {
		return players;
	}

	public void setPlayers(List<String> players) {
		this.players = players;
	}

	public void setPositional(Positional positional) {
		this.positional = positional;
	}

	
}
