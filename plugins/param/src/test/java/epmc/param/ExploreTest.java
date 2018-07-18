package epmc.param;

import static epmc.graph.TestHelperGraph.*;
import static epmc.modelchecker.TestHelper.*;
import static epmc.param.ModelNames.CLUSTER;
import static epmc.param.ModelNames.BRP;
import static epmc.param.PARAMTestHelper.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.param.options.OptionsParam;

public final class ExploreTest {
    
	@Test
    public void loadClusterTest() throws EPMCException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        List<String> parameters = new ArrayList<>();
        parameters.add("ws_fail");
        parameters.add("switch_fail");
        parameters.add("line_fail");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        Map<String,String> constants = new HashMap<>();
        constants.put("N", "2");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = loadModel(options, CLUSTER);
//        System.out.println(model);
        processAfterModelLoading(options);
        GraphExplicit result = exploreModelGraph(model);
        System.out.println(result);
    }
	
	@Test
	public void loadBrpTest() throws EPMCException {
	    Options options = preparePARAMOptions();
	    options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
	    List<String> parameters = new ArrayList<>();
	    parameters.add("pK");
	    parameters.add("pL");
	    options.set(OptionsParam.PARAM_PARAMETER, parameters);
	    Map<String,String> constants = new HashMap<>();
	    constants.put("N", "16");
	    constants.put("MAX", "5");
	    options.set(OptionsModelChecker.CONST, constants);
	    Model model = loadModel(options, BRP);
//	         System.out.println(model);
	    processAfterModelLoading(options);
	    GraphExplicit result = exploreModelGraph(model);
	    System.out.println(result);
	}
}
