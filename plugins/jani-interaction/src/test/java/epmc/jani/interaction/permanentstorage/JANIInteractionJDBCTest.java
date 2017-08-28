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

package epmc.jani.interaction.permanentstorage;

import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.interaction.OptionsManager;
import epmc.jani.interaction.UserManager;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

public final class JANIInteractionJDBCTest {
    private final static String PLUGIN_DIR_JI = System.getProperty("user.dir") + "/../iscasmc-jani-interaction/target/classes/";
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
    private final static Options prepareJANIInteractionJDBCOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_DIR_JI);
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
        return options;
    }

    @Test
    public void fileTest() throws MalformedURLException {
        File file = new File("asdf.jar");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.toURI().toURL());
    }

    @Test
    public void connectionTest() {
        Options options = prepareJANIInteractionJDBCOptions();
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR, "lib/sqlite-jdbc-3.8.11.2.jar");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS, "org.sqlite.JDBC");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL, "jdbc:sqlite:test.db");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME, "asdf");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD, "password");

        Database permanentStorage = new Database();
        UserManager userManager = new UserManager(permanentStorage);
        System.out.println(userManager.checkLogin("fdsa", "asdf"));
        System.out.println(userManager.createUser("fdsa", "asdf"));
        System.out.println(userManager.checkLogin("fdsa", "asdf"));
        OptionsManager optionsManager = new OptionsManager(permanentStorage);
        optionsManager.write(1, false, options);
    }
}
