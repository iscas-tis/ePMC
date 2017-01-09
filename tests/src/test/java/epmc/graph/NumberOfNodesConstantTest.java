package epmc.graph;

import static epmc.graph.TestHelperGraph.buildGraphDD;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.ModelNamesPRISM;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

public class NumberOfNodesConstantTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void cluster() throws EPMCException {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "16");
        options.set(OptionsModelChecker.CONST, constants);
        GraphDD graphDD = buildGraphDD(options, ModelNamesPRISM.CLUSTER_MODEL);
        DD transitionsBoolean = graphDD.getTransitions();
        DD transitions = graphDD.getEdgeProperty(CommonProperties.WEIGHT);
        DD states = graphDD.getNodeProperty(CommonProperties.STATE);
        BigInteger transitionsBoolean16 = transitionsBoolean.countNodes();
        BigInteger transitions16 = transitions.countNodes();
        BigInteger states16 = states.countNodes();
        for (int i = 0; i < 3; i++) {
            options = prepareOptions();
            constants = new HashMap<>();
            constants.put("N", "16");
            options.set(OptionsModelChecker.CONST, constants);
            graphDD = buildGraphDD(options, ModelNamesPRISM.CLUSTER_MODEL);
            transitionsBoolean = graphDD.getTransitions();
            transitions = graphDD.getEdgeProperty(CommonProperties.WEIGHT);
            states = graphDD.getNodeProperty(CommonProperties.STATE);
            assertEquals(transitionsBoolean16, transitionsBoolean.countNodes());
            assertEquals(transitions16, transitions.countNodes());
            assertEquals(states16, states.countNodes());
            graphDD.close();
        }
        options = prepareOptions();
        constants = new HashMap<>();
        constants.put("N", "512");
        options.set(OptionsModelChecker.CONST, constants);
        graphDD = buildGraphDD(options, ModelNamesPRISM.CLUSTER_MODEL);
        transitionsBoolean = graphDD.getTransitions();
        transitions = graphDD.getEdgeProperty(CommonProperties.WEIGHT);
        states = graphDD.getNodeProperty(CommonProperties.STATE);
        BigInteger transitionsBoolean512 = transitionsBoolean.countNodes();
        BigInteger transitions512 = transitions.countNodes();
        BigInteger states512 = states.countNodes();
        for (int i = 0; i < 3; i++) {
            options = prepareOptions();
            constants = new HashMap<>();
            constants.put("N", "512");
            options.set(OptionsModelChecker.CONST, constants);
            graphDD = buildGraphDD(options, ModelNamesPRISM.CLUSTER_MODEL);
            transitionsBoolean = graphDD.getTransitions();
            transitions = graphDD.getEdgeProperty(CommonProperties.WEIGHT);
            states = graphDD.getNodeProperty(CommonProperties.STATE);
            assertEquals (transitionsBoolean512, transitionsBoolean.countNodes());
            assertEquals (transitions512, transitions.countNodes());
            assertEquals (states512, states.countNodes());
            graphDD.close();
        }
        close(options);
    }

}
