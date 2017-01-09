package epmc.prism.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.jani.model.type.JANIType;
import epmc.value.Type;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
/**
 * Guarded command of a PRISM model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Command {
    /** label of command. use empty string if not synchronised, not null */
    private final Expression label;
    /** Guard of the guarded command. */
    private final Expression guard;
    /** The different stochastically chosen alternatives of the command. */
    private final ArrayList<Alternative> alternatives = new ArrayList<>();
    /** Source file position information about the guarded command. */
    private final Positional positional;
    /** Player controlling the guarded command. */
    private int player = -1;

    public Command(Expression label, Expression guard, List<Alternative> alternatives, Positional positional) {
        assert label != null;
        assert guard != null;
        assert alternatives != null;
        for (Alternative alternative : alternatives) {
            assert alternative != null;
        }
        this.label = label;
        this.guard = guard;
        this.alternatives.addAll(alternatives);
        this.positional = positional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + label + "] ");
        builder.append(guard + " -> ");
		if (alternatives.isEmpty()) {
			// this corresponds to a transition with no updates; self-loop
			// represented in prism as "true" update
			builder.append("true");
		}
		
		int altNr = 0;
		for (Alternative alternative : alternatives) {
            builder.append(alternative);
            if (altNr < alternatives.size() - 1) {
                builder.append(" + ");
            }
		    altNr++;
		}
        return builder.toString();
    }
    
    Expression getLabel() {
        return label;
    }
    
    public Expression getAction() {
        return label;
    }

    public Expression getGuard() {
        return guard;
    }
    
    public List<Alternative> getAlternatives() {
        return Collections.unmodifiableList(alternatives);
    }

    Command replaceFormulas(Map<Expression,Expression> formulas) {
        assert formulas != null;
        for (Entry<Expression, Expression> entry : formulas.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression, Expression> entry : formulas.entrySet()) {
            if (!(entry.getKey() instanceof ExpressionIdentifier)) {
                throw new IllegalArgumentException();
            }
        }
        Expression newGuard = guard;
        newGuard = UtilExpressionStandard.replace(newGuard, formulas);
        ArrayList<Alternative> newAlternatives = new ArrayList<>();
        for (Alternative alternative : alternatives) {
            newAlternatives.add(alternative.replaceFormulas(formulas));
        }
        return new Command(this.label, newGuard, newAlternatives, positional);
    }
    
    public void checkExpressionConsistency(
            Map<Expression, JANIType> globalVariables,
            Map<Expression, JANIType> variables,
            Map<Expression, Type> types)
                    throws EPMCException {    
    	// TODO
    	/*
        Type guardRing = guard.getType();
        ensure(guardRing == null || TypeBoolean.isBoolean(guardRing),
                ProblemsPRISM.GUARD_NOT_BOOLEAN, guard);
                */
        for (Alternative alternative : alternatives) {
            alternative.checkExpressionConsistency(globalVariables, variables, types);
        }
    }
    
    void setPlayer(int player) {
        assert player >= -1;
        assert this.player == -1;
        this.player = player;
    }
    
    public int getPlayer() {
        return player;
    }
}
