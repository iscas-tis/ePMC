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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * Class representing a plugin loaded from JAR file or directory.
 * 
 * @author Ernst Moritz Hahn
 */
final class Plugin {
    /** String for {@link #toString()} method. */
    private final static String PATH = "path";
    /** String for {@link #toString()} method. */
    private final static String NAME = "name";
    /** String for {@link #toString()} method. */
    private final static String DEPENDENCIES = "dependencies";
    /** String for {@link #toString()} method. */
    private final static String CLASSES = "classes";

    /** Path from which the plugin is loaded (JAR or directory). */
    private Path path;
    /** Name of the plugin. */
    private String name;
    /** List of dependencies (plugin names) of the plugin. */
    private final List<String> dependencies = new ArrayList<>();
    /** Plugin classes of the plugin. */
    private final List<Class<? extends PluginInterface>> classes = new ArrayList<>();

    /**
     * Add a class belonging to this plugin.
     * Classes to be added will be used to execute certain tasks at certain
     * occasions, depending which derived interface of {@link PluginInterface}
     * they implement. Parameters may not be {@code null}.
     * 
     * @param clazz class to be added
     */
    void add(Class<? extends PluginInterface> clazz) {
        assert clazz != null;
        classes.add(clazz);
    }

    /**
     * Get plugin interface classes of plugin.
     * 
     * @return plugin interface classes of plugin.
     */
    List<Class<? extends PluginInterface>> getClasses() {
        return classes;
    }

    /**
     * Set the name of the plugin.
     * 
     * @param name name to set for plugin
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of the plugin.
     * 
     * @return name of the plugin
     */
    String getName() {
        return name;
    }

    /**
     * Add dependencies of plugin.
     * The dependencies should be a list of the names of plugins this plugin
     * depends on.
     * The dependencies parameter must not be {@code null}, and must not contain
     * any {@code null} entries.
     * 
     * @param dependencies dependencies of plugin to add
     */
    void addDependencies(String[] dependencies) {
        assert dependencies != null;
        for (String dependency : dependencies) {
            assert dependency != null;
        }
        this.dependencies.addAll(Arrays.asList(dependencies));
    }

    /**
     * Get list of names of plugins this plugin depends on.
     * 
     * @return list of names of plugins this plugin depends on
     */
    List<String> getDependencies() {
        return dependencies;
    }

    /**
     * Set path of the plugin.
     * The path can be either a directory or a JAR file with a manifest valid
     * for a plugin.
     * 
     * @param path
     */
    void setPath(Path path) {
        this.path = path;
    }

    /**
     * Get path of the plugin.
     * 
     * @return
     */
    Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(PATH, path)
                .add(NAME, name)
                .add(DEPENDENCIES, dependencies)
                .add(CLASSES, classes)
                .toString();
    }
}
