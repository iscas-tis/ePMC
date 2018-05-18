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

package epmc.jani.interaction.communication;

import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.jani.interaction.command.CommandTaskJaniInteractionStartServer;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.TestHelper.LogType;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

public class WebSocketTest {
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
     */
    private final static Options prepareJANIInteractionOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, LogType.NOTRANSLATE, TestHelper.MODEL_INPUT_TYPE_PRISM);        
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR, "lib/sqlite-jdbc-3.8.11.2.jar");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS, "org.sqlite.JDBC");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL, "jdbc:sqlite:test.db");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME, "asdf");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD, "password");
        return options;
    }

    @Test
    public void webSocketsTest() {
        Options options = prepareJANIInteractionOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_TYPE, JANIInteractionIO.WEBSOCKETS);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_ANONYMOUS_LOGINS, true);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_START_GUI, true);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_PRINT_MESSAGES, true);
        CommandTaskJaniInteractionStartServer task = new CommandTaskJaniInteractionStartServer();
        task.executeInClientBeforeServer();
    }

    @Ignore
    @Test
    public void userManagementTest() {
        Options options = prepareJANIInteractionOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_TYPE, JANIInteractionIO.WEBSOCKETS);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_ANONYMOUS_LOGINS, true);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_START_GUI, true);
        CommandTaskJaniInteractionStartServer task = new CommandTaskJaniInteractionStartServer();
        task.executeInClientBeforeServer();
    }
}
