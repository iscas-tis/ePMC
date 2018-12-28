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

import epmc.expression.Expression;
import epmc.graph.explicit.GraphExplicit;
import epmc.value.Value;

// TODO complete documentation
// TODO change interface to the one of Automata, because
// 1.) the current interface is irritating
// 2.) the change would allow composing Buechi automata with models, e.g. for
//     non-probabilistic model checking
// TODO move implementation (by SPOT) to separate plugin
// TODO if we switch to GPL, use SPOT library rather than SPOT command line tool

public interface Buechi {
    /* methods to be implemented by implementing classes */
    String getIdentifier();    

    boolean isDeterministic();

    Expression[] getExpressions();

    int getNumLabels();

    int getTrueState();

    GraphExplicit getGraph();

    void query(Value[] get);

    String getName();
    
    /* default methods */

    default int getNumStates() {
        return getGraph().getNumNodes();
    }
}
