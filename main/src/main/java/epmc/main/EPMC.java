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

package epmc.main;

import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.StartInConsole;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;

/**
 * Main class of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EPMC {
    /**
     * The {@code main} entry point of EPMC.
     * 
     * @param args parameters of EPMC.
     */
    public static void main(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        Options options = UtilOptionsEPMC.newOptions();
        Options.set(options);
        startInConsole(options, args);
    }

    /**
     * Start the command to be executed with output shown in standard output.
     * The command to be executed will be read for {@link Options#COMMAND}.
     * Then, the client part of the command will be executed.
     * Afterwards, a task server will be created and the server part of the
     * command will be executed there.
     * The options parameter must not be {@code null}.
     * @param options 
     * 
     * @param options options to use
     */
    private static void startInConsole(Options options, String[] args) {
        assert options != null;
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        for (Class<? extends StartInConsole> clazz : UtilPlugin.getPluginInterfaceClasses(options, StartInConsole.class)) {
            StartInConsole object = Util.getInstance(clazz);
            object.process(args);
        }
    }

}
