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

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

import static epmc.modelchecker.TestHelper.*;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public final class JANIInteractionTest {
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
        prepareOptions(options);
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR, "lib/sqlite-jdbc-3.8.11.2.jar");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS, "org.sqlite.JDBC");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL, "jdbc:sqlite:test.db");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME, "asdf");
        options.set(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD, "password");
        return options;
    }

    @Test
    public void getOptionsTest() {
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
    public void webSocketsTest() {
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

    @Test
    public void blaTest() {
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
