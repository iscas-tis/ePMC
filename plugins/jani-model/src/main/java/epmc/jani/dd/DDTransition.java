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

package epmc.jani.dd;

import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.jani.model.Action;
import epmc.jani.model.TransientValue;

/**
 * Symbolically represents the transitions induced by a system component.
 * 
 * @author Ernst Moritz Hahn
 */
final class DDTransition implements Closeable, Cloneable {
    /** String "closed", used in the {@link #toString()} method. */
    private final static String CLOSED = "closed";
    /** String "invalid", used in the {@link #toString()} method. */
    private final static String INVALID = "invalid";
    /** String "action", used in the {@link #toString()} method. */
    private final static String ACTION = "action";
    /** String "writes", used in the {@link #toString()} method. */
    private final static String WRITES = "writes";

    /** Whether DD transition was already closed and cannot be used further. */
    private boolean closed;
    /** Action on which the transition synchronises. */
    private Action action;
    /** Guard of the transition. */
    private DD guard;
    /** Transitions performed, that is guard times variable assignmens. */
    private DD transitions;
    /** Set of variables to which this transition writes. */
    private Set<VariableDD> writes;
    /** Indicates whether this transition is invalid due to multiple writes to
     * global variables. */
    private boolean invalid;
    /** Validity indicators. */
    private Set<VariableValid> validFor;
    /** Transient values. */
    private Map<TransientValue, DD> transientValues;

    /**
     * Set the action of this symbolic transition.
     * 
     * @param action action to set
     */
    void setAction(Action action) {
        this.action = action;
    }

    /**
     * Get the action of this symbolic transition.
     * 
     * @return action of this symbolic transition
     */
    Action getAction() {
        return action;
    }

    /**
     * Set the guard of this symbolic transition.
     * Note that the guard is not cloned, and thus consumed by this function.
     * Thus, it should not be disposed by the caller of this function.
     * 
     * @param guard guard of this symbolic transition to set
     */
    void setGuard(DD guard) {
        this.guard = guard;
    }

    /**
     * Get the guard of this symbolic transition.
     * Note that the guard is not cloned, and should not be disposed by the
     * caller of this function.
     * 
     * @return guard of this symbolic transition
     */
    public DD getGuard() {
        return guard;
    }

    /**
     * Set the symbolic transition DD of this symbolic transition.
     * The transition DD is a DD for the guard of the edge combined with the
     * weighted (by probabilities or rates) destinations of the edge, assigning
     * values to local and some global variables.
     * Note that the DD is not cloned, and thus consumed by this function.
     * Thus, it should not be disposed by the caller of this function.
     * 
     * @param transitions transition DD of this symbolic transition to set
     */
    void setTransitions(DD transitions) {
        this.transitions = transitions;
    }

    /**
     * Get symbolic transition DD of this symbolic transition.
     * 
     * @return symbolic transition DD of this symbolic transition
     */
    DD getTransitions() {
        return transitions;
    }

    /**
     * Set the set of DD variables written by this transition.
     * This include local variables, variables encoding locations of automata,
     * as well as global variables.
     * 
     * @param writes variables written by this transition
     */
    void setWrites(Set<VariableDD> writes) {
        this.writes = writes;
    }

    /**
     * Get the set of variables written by this transition.
     * 
     * @return variables written by this transition
     */
    Set<VariableDD> getWrites() {
        return writes;
    }

    /**
     * Sets whether this transition is invalid.
     * A transition is invalid if it results from the parallel synchronisation
     * of two transitions which potentially write the same global variables.
     * For correct models, such a synchronisation should result in a transition
     * the guard of which is {@code false}, which means that the transition can
     * never be executed. For efficiency, we only store transitions the guard
     * of which is not {@code false}. We need to store invalid transitions with
     * non-{@code false} guard, to later check potential modeling errors, as
     * such errors can only be detected after or during node space exploration.
     * 
     * @param invalid whether this transition is invalid
     */
    void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    /**
     * Gets whether this transition is invalid.
     * 
     * @return whether this transition is invalid
     */
    boolean isInvalid() {
        return invalid;
    }

    void setVariableValid(Set<VariableValid> validFor) {
        this.validFor = validFor;
    }

    Set<VariableValid> getValidFor() {
        return validFor;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        guard.dispose();
        transitions.dispose();
        for (VariableValid valid : validFor) {
            valid.close();
        }
    }

    @Override
    protected DDTransition clone() {
        DDTransition clone = new DDTransition();
        clone.setAction(action);
        clone.setWrites(writes);
        clone.setGuard(guard.clone());
        clone.setInvalid(invalid);
        clone.setTransitions(transitions.clone());
        Set<VariableValid> clonedVariables = new LinkedHashSet<>();
        for (VariableValid valid : this.validFor) {
            clonedVariables.add(valid.clone());
        }
        clone.setVariableValid(clonedVariables);
        return clone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(CLOSED, closed)
                .add(INVALID, invalid)
                .add(ACTION, action)
                .add(WRITES, writes)
                .toString();
    }

    void setTransitionTransientValues(Map<TransientValue, DD> transientValues) {
        this.transientValues = transientValues;
    }

    Map<TransientValue, DD> getTransitionTransientValues() {
        return transientValues;
    }
}
