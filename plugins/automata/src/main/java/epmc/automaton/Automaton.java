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

package epmc.automaton;

import java.io.Closeable;

import epmc.expression.Expression;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

// TODO complete documentation

/**
 * Interface to be implemented by an automaton.
 * The interface is provided to have a general means to compose automata to
 * models so thus to obtain a product. For stochastic models, for this to work
 * in general the automaton should be deterministic. As there are some
 * exceptions for the case of unambiguous automata with Markov chains,
 * nondeterministic automata are also supported. The interface also serves to
 * allow an {@link Expression} to be transformed into an automaton by an
 * according plugin, in order to export the automaton from the tool.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Automaton extends Closeable {
    interface Builder {
        default String getIdentifier() {
            return null;
        }

        default Builder setBuechi(Buechi buechi) {
            return this;
        }

        default Builder setExpression(Expression expression, Expression[] expressions) {
            ValueBoolean negate = TypeBoolean.get().newValue(false);
            Buechi buechi = UtilAutomaton.newBuechi(expression, expressions, true, negate);
            setBuechi(buechi);
            return this;
        }

        default Builder setExpression(Expression expression) {
            Expression[] expressions = UtilAutomaton.collectLTLInner(expression).toArray(new Expression[0]);
            setExpression(expression, expressions);
            return this;
        }

        Automaton build();
    }

    int getInitState();

    int getNumStates();

    Object numberToState(int number);

    Object numberToLabel(int number);

    Expression[] getExpressions();

    void queryState(Value[] modelState, int automatonState);

    default String getIdentifier() {
        return null;
    }

    default Buechi getBuechi() {
        assert false;
        return null;
    }

    default int getSuccessorState() {
        assert false;
        return -1;
    }

    default int getSuccessorLabel() {
        assert false;
        return -1;
    }

    default int getNumberSuccessors() {
        assert isDeterministic();
        return 1;
    }

    default int getSuccessorState(int successorNumber) {
        assert successorNumber >= 0;
        assert successorNumber < 1;
        return getSuccessorState();
    }

    default int getSuccessorLabel(int successorNumber) {
        assert successorNumber >= 0;
        assert successorNumber < 1;
        return getSuccessorLabel();
    }

    @Override
    default void close() {
    }

    default boolean isDeterministic() {
        return true;
    }
}
