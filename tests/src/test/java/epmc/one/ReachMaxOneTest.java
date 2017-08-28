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

package epmc.one;

import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeEnum;
import epmc.value.TypeWeightTransition;

public class ReachMaxOneTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void reachMaxOneLiYongsMailTest() {
        Options options = prepareOptions();
        GraphExplicitWrapper graph = new GraphExplicitWrapper();
        EdgeProperty weights = graph.addSettableEdgeProperty(CommonProperties.WEIGHT, TypeWeightTransition.get());
        NodeProperty player = graph.addSettableNodeProperty(CommonProperties.PLAYER, TypeEnum.get(Player.class));
        player.set(0, Player.STOCHASTIC);
        graph.prepareNode(0, 2);
        graph.setSuccessorNode(0, 0, 0);
        weights.set(0, 0, "0.5");
        graph.setSuccessorNode(0, 1, 1);
        weights.set(0, 1, "0.5");
        player.set(1, Player.STOCHASTIC);
        graph.prepareNode(1, 2);
        graph.setSuccessorNode(1, 0, 1);
        weights.set(1, 0, "0.5");
        graph.setSuccessorNode(1, 1, 2);
        weights.set(1, 1, "0.5");
        graph.prepareNode(2, 0);
        player.set(2, Player.STOCHASTIC);
        BitSet targets = UtilBitSet.newBitSetUnbounded();
        targets.set(1);
        ComponentsExplicit components = new ComponentsExplicit();
        BitSet result = components.reachMaxOne(graph, targets);
        Assert.assertTrue(result.get(0));
        Assert.assertTrue(result.get(1));
        Assert.assertFalse(result.get(2));
        close(options);
    }
}
