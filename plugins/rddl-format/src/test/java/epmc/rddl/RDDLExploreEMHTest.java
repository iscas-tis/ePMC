package epmc.rddl;

import static epmc.graph.TestHelperGraph.exploreModelGraph;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.rddl.ModelNames.*;
import static epmc.rddl.RDDLTestHelper.prepareRDDLOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.rddl.model.ModelRDDL;

public class RDDLExploreEMHTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    // Status: works. Fast enough.
    public void emh_intermediateTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result;
        result = exploreModelGraph(options, EMH_INTERMEDIATE);
        System.out.println(result);
    }

    @Test
    // Status: works. Fast enough.
    public void emh_parametricTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result;
        result = exploreModelGraph(options, EMH_PARAMETRIC);
        System.out.println(result);
    }

    @Test
    // Status: works. Fast enough.
    public void emh_interm_paramTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result;
        result = exploreModelGraph(options, EMH_INTERM_PARAM);
        System.out.println(result);
    }

    @Test
    // Status: works. Fast enough.
    public void emh_quantifier_forallTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result;
        result = exploreModelGraph(options, EMH_QUANTIFIER_FORLL);
        System.out.println(result);
    }
}
