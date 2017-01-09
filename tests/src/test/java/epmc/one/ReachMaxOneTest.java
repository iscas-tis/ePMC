package epmc.one;

import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.TypeEnum;
import epmc.value.TypeWeightTransition;

public class ReachMaxOneTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void reachMaxOneLiYongsMailTest() throws EPMCException {
        Options options = prepareOptions();
        ContextValue contextValue = options.get(TestHelper.CONTEXT_VALUE);
        GraphExplicitWrapper graph = new GraphExplicitWrapper(contextValue);
        EdgeProperty weights = graph.addSettableEdgeProperty(CommonProperties.WEIGHT, TypeWeightTransition.get(contextValue));
        NodeProperty player = graph.addSettableNodeProperty(CommonProperties.PLAYER, TypeEnum.get(contextValue, Player.class));
        graph.queryNode(0);
        player.set(Player.STOCHASTIC);
        graph.prepareNode(2);
        graph.setSuccessorNode(0, 0);
        weights.set("0.5", 0);
        graph.setSuccessorNode(1, 1);
        weights.set("0.5", 1);
        graph.queryNode(1);
        player.set(Player.STOCHASTIC);
        graph.prepareNode(2);
        graph.setSuccessorNode(0, 1);
        weights.set("0.5", 0);
        graph.setSuccessorNode(1, 2);
        weights.set("0.5", 1);
        graph.queryNode(2);
        graph.prepareNode(0);
        player.set(Player.STOCHASTIC);
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
