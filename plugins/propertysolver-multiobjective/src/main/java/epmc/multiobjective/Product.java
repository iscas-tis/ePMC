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

package epmc.multiobjective;

import epmc.graph.explicit.GraphExplicit;

final class Product {
    private final GraphExplicit graph;
    private final IterationRewards rewards;
    private final int numAutomata;

    Product(GraphExplicit graph, IterationRewards rewards, int numAutomata) {
        assert graph != null;
        assert rewards != null;
        this.graph = graph;
        this.rewards = rewards;
        this.numAutomata = numAutomata;
    }

    GraphExplicit getGraph() {
        return graph;
    }

    IterationRewards getRewards() {
        return rewards;
    }

    int getNumAutomata() {
        return numAutomata;
    }
}
