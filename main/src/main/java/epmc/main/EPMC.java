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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import epmc.plugin.PluginInterface;
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
        List<Class<? extends PluginInterface>> plugins = getPlugins(args);
        for (Class<? extends StartInConsole> clazz : UtilPlugin.getPluginInterfaceClasses(plugins, StartInConsole.class)) {
            StartInConsole object = Util.getInstance(clazz);
            object.process(args, plugins);
        }
    }
    
    private static List<Class<? extends PluginInterface>> getPlugins(String[] args) {
        return UtilPlugin.loadPlugins(getPluginList(args));
    }
    
    private static List<String> getPluginList(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        List<String> result = new ArrayList<>();
        String plugins = null;
        String pluginsListFilename = null;
        int index = 0;
        while (index < args.length) {
            if (args[index].trim().equals("--plugin")) {
                index++;
                while (index < args.length && !args[index].startsWith("--")) {
                    if (plugins == null) {
                        plugins = args[index].trim();
                    } else {
                        plugins += "," + args[index].trim();
                    }
                    index++;
                }
            } else if (args[index].equals("--plugin-list-file")) {
                index++;
                while (index < args.length && !args[index].startsWith("--")) {
                    pluginsListFilename = args[index];
                    index++;
                }
            }
            index++;
        }
        /* Read external plugins from plugin files list. */
        if (pluginsListFilename != null) {
            Path pluginsListPath = Paths.get(pluginsListFilename);
            List<String> pluginsFromListFile = UtilPlugin.readPluginList(pluginsListPath);
            result.addAll(pluginsFromListFile);
        }

        /* Read plugins from option (command line).  */
        if (plugins == null) {
            plugins = "";
        }
        for (String plugin : plugins.split(",")) {
            if (plugin.equals("")) {
                continue;
            }
            result.add(plugin);
        }
        return result;
    }
}
