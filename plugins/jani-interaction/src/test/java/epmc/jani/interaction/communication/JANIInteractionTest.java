package epmc.jani.interaction.communication;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

import static epmc.modelchecker.TestHelper.*;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public final class JANIInteractionTest {
	/** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     * @throws EPMCException thrown in case problem occurs
     */
    private final static Options prepareJANIInteractionOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
		options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR, "lib/sqlite-jdbc-3.8.11.2.jar");
		options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS, "org.sqlite.JDBC");
		options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL, "jdbc:sqlite:test.db");
		options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME, "asdf");
		options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD, "password");
        return options;
    }
    
    @Test
    public void getOptionsTest() throws EPMCException {
        Options options = prepareJANIInteractionOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        BackendTester feedback = new BackendTester(options);
        JsonObjectBuilder request = Json.createObjectBuilder();
        request.add("jani-versions", Json.createArrayBuilder().add(1));
        feedback.send(request.build());
        System.out.println(feedback.size());
        System.out.println(feedback.popPending());
    }

    @Test
    public void webSocketsTest() throws EPMCException {
        Options options = prepareJANIInteractionOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_TYPE, JANIInteractionIO.WEBSOCKETS);
        BackendTester feedback = new BackendTester(options);
        JsonObjectBuilder request = Json.createObjectBuilder();
        request.add("jani-versions", Json.createArrayBuilder().add(1));
        feedback.send(request);
        System.out.println(feedback.size());
        System.out.println(feedback.popPending());
    }

}
