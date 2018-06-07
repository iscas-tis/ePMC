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

package epmc.plugin;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.options.Option;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.util.Util;

/**
 * Collections of static utility methods to work with plugins.
 * This class contains only static methods and thus is prevented from being
 * instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilPlugin {
    /** String to suppress unchecked warnings. */
    private final static String UNCHECKED = "unchecked";
    /** String containing a comma. */
    private final static String COMMA = ",";
    /** Empty string .*/
    private final static String EMPTY = "";
    /** String specifying US ASCII char set. */
    private final static String US_ASCII = "US-ASCII";

    /**
     * <p>Load plugins as specified in options.</p>
     * <p>A plugin is a collection of classes and resources used to extend the
     * functionality of EPMC. In the approach used, also rather basic
     * functionality is contained in plugins, so as to be able to easily replace
     * it by other functionally equivalent alternatives. Plugins can specify
     * classes implementing interfaces so as to execute functionality at a
     * certain point of time.
     * </p>
     * <p>The parameter options should contain a entry for
     * {@link OptionsPlugin#PLUGIN} which is a {@link List} of {@link String}s.
     * These strings are filenames (valid in the local filesystem) of
     * directories or JAR files containing a plugin. The plugin loader will
     * load all classes implementing {@link PluginInterface} or a derived
     * interface. The classes loaded will be stored in {@link Map}s of
     * identifier {@link String}s to {@link PluginInterface} {@link Class}es,
     * where the identifier is obtained using
     * {@link PluginInterface#getIdentifier()} in {@link Option} of the
     * {@link Options} parameter. Depending on the subinterface of
     * {@link PluginInterface} which the class implements, a class will be
     * stored in one of the following options and processed at the following
     * points of time:
     * </p>
     * <table>
     * <tr>
     *  <th>Interface</th>
     *  <th>Option</th>
     *  <th>Executed</th>
     * </tr>
     * </table>
     * <p>
     * To see examples of usages of these extension points, please take a look
     * at existing plugins. If you think that your plugin classes need to be
     * executed at other occasions than defined above, please contact the author
     * of this class, but please read the source code of other plugins with
     * similar functionality first.
     * </p>
     * <p>
     * Plugin classes of the same row in the table above will be processed in
     * the order they were specified in {@link OptionsPlugin#PLUGIN}.
     * </p>
     * 
     * @param options defines plugin locations and will contain loaded plugins
     */
    public static void loadPlugins(Options options) {
        assert options != null;
        List<String> plugins = getPluginList(options);

        assert plugins != null;
        PluginLoader pluginLoader = new PluginLoader(plugins);

        options.set(OptionsPlugin.PLUGIN_INTERFACE_CLASS, pluginLoader.getPluginInterfaceClasses());

        for (Class<? extends AfterOptionsCreation> clazz : getPluginInterfaceClasses(options, AfterOptionsCreation.class)) {
            AfterOptionsCreation object = Util.getInstance(clazz);
            object.process(options);
        }
    }

    public static List<Class<? extends PluginInterface>> loadPlugins(List<String> plugins) {
        assert plugins != null;
        PluginLoader pluginLoader = new PluginLoader(plugins);
        return pluginLoader.getPluginInterfaceClasses();
    }

    /**
     * Get list of classes implementing a particular plugin interface.
     * The set of available plugin classes are read from a given options set
     * from the key {@link OptionsPlugin#PLUGIN_INTERFACE_CLASS}.
     * For each of these classes, it is checked whether it implements the given
     * interface extending {@link PluginInterface}.
     * All classes for which this is the case are returned as a list.
     * 
     * The options parameter and the parameter for the interface to implement
     * must not be {@code null}.
     * 
     * @param options the options to read plugin list from
     * @param shallImplement the interface classes shall implement
     * @return list of interface classes implementing the interface
     */
    public static <T extends PluginInterface> List<Class<T>> getPluginInterfaceClasses(
            Options options, Class<T> shallImplement) {
        assert options != null;
        assert shallImplement != null;
        List<Class<? extends PluginInterface>> classes =
                options.get(OptionsPlugin.PLUGIN_INTERFACE_CLASS);
        assert classes != null;
        return getPluginInterfaceClasses(classes, shallImplement);
    }

    public static <T extends PluginInterface> List<Class<T>> getPluginInterfaceClasses(
            List<Class<? extends PluginInterface>> classes, Class<T> shallImplement) {
        assert classes != null;
        assert shallImplement != null;
        List<Class<T>> result = new ArrayList<>();
        for (Class<? extends PluginInterface> clazz : classes) {
            if (shallImplement.isAssignableFrom(clazz)) {
                @SuppressWarnings(UNCHECKED)
                Class<T> clazzCasted = (Class<T>) clazz;
                result.add(clazzCasted);
            }
        }

        return result;
    }

    
    /**
     * Get plugin filenames specified in options.
     * The list of plugins does not include the plugins embedded into the
     * distribution.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to read plugin list from
     * @return list of external plugins
     */
    private static List<String> getPluginList(Options options) {
        assert options != null;
        List<String> result = new ArrayList<>();

        /* Read external plugins from plugin files list. */
        String pluginsListFilename = options.getString(OptionsPlugin.PLUGIN_LIST_FILE);
        if (pluginsListFilename != null) {
            Path pluginsListPath = Paths.get(pluginsListFilename);
            List<String> pluginsFromListFile = readPluginList(pluginsListPath);
            result.addAll(pluginsFromListFile);
        }

        /* Read plugins from option (command line).  */
        String plugins = options.getUnparsed(OptionsPlugin.PLUGIN);
        if (plugins == null) {
            plugins = EMPTY;
        }
        for (String plugin : plugins.split(COMMA)) {
            if (plugin.equals(EMPTY)) {
                continue;
            }
            result.add(plugin);
        }

        return result;
    }

    /**
     * Read list of plugins from file.
     * The plugin list parameter file is a text file which should contain one
     * plugin directory/JAR file per line.
     * The function checks the existence of files and will throw according
     * exceptions in case of missing files.
     * The plugin list parameter must not be {@code null}.
     * 
     * @param pluginListPath file to read list of plugins from
     * @return list of plugins
     */
    public static List<String> readPluginList(Path pluginListPath) {
        assert pluginListPath != null;
        Charset charset = Charset.forName(US_ASCII);
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(pluginListPath, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Path pluginPath = Paths.get(line);
                ensure(Files.exists(pluginPath), ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_FOUND, line);
                result.add(line);
            }
        } catch (IOException e) {
            fail(ProblemsPlugin.PLUGIN_READ_PLUGIN_LIST_IO_EXCEPTION, pluginListPath);
        }
        return result;
    }

    /**
     * Add options for the plugin handling mechanism.
     * The options parameters must not be {@code null}
     * 
     * @param options options set to add options to
     */
    public static void addOptions(Options options) {
        assert options != null;
        OptionTypePluginList typePluginList = OptionTypePluginList.getInstance();
        OptionTypeString typeString = OptionTypeString.getInstance();

        options.addOption().setBundleName(OptionsPlugin.OPTIONS_PLUGIN)
        .setIdentifier(OptionsPlugin.PLUGIN)
        .setType(typePluginList).setCommandLine().setGui().build();
        options.addOption().setBundleName(OptionsPlugin.OPTIONS_PLUGIN)
        .setIdentifier(OptionsPlugin.PLUGIN_LIST_FILE)
        .setType(typeString).setCommandLine().setGui().build();
    }

    /**
     * Private constructor to avoid instantiation of this class.
     */
    private UtilPlugin() {
    }
}
