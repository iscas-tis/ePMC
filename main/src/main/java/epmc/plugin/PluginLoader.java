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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import epmc.util.Util;

// TODO continue documentation

/**
 * Plugin loader.
 * 
 * @author Ernst Moritz Hahn
 */
final class PluginLoader {
    /* Be careful when changing anything in this class. It is very easy to break
     * its functionality without even noticing it. In particular, if you
     * perform any changes, please check whether the class still works on other
     * operating systems than the one you are working on. E.g., if you are
     * working on Linux, you must check whether the plugin loader also still
     * works on windows after a change.
     */

    /** String "plugins:\n" for {@link #toString()} method. */
    private final static String TOSTRING_PLUGINS = "plugins:\n";
    /** String "\n" for {@link #toString()} method. */
    private final static String TOSTRING_ENDLINE = "\n";
    /** String "classes:\n" for {@link #toString()} method. */
    private static final Object TOSTRING_CLASSES = "classes:\n";
    /** String "----------\n" for {@link #toString()} method. */    
    private static final Object TOSTRING_TERMINATOR = "----------\n";
    /** String to suppress unchecked warnings. */
    private final static String UNCHECKED = "unchecked";
    /** Separator character.
     * Note that indeed we need the slash here, not the system-dependent path
     * separator. */
    private final static char SEPARATOR_CHAR = '/';
    /** Separator character between path paths of class names. */
    private final static char CLASS_NAME_SEPARATOR = '.';
    /** String containing the separator character {@link #SEPARATOR_CHAR}. */
    private final static String SEPARATOR = String.valueOf(SEPARATOR_CHAR);
    /** File ending of a JAR file. */
    private final static String JAR_ENDING = ".jar";
    /** Directory containing the embedded plugins.
     * This directory should be contained at the top level of the JAR file or
     * main directory of EPMC. */
    private final static String EMBEDDED_PLUGINS_DIR = "embeddedplugins";
    /** File containing list of embedded plugins. 
     * This file should be contained in the embedded plugins directory. */
    private final static String EMBEDDED_PLUGINS_LIST = "embeddedplugins.txt";
    /** Meta-Inf directory in plugin JAR file containing the manifest. */
    private final static String META_INF = "META-INF";
    /** Name of the manifest file. */
    private final static String MANIFEST = "MANIFEST.MF";
    /** String for manifest attribute to declare JAR/directory is plugin. */
    private final static String EPMC_PLUGIN_ATTRIBUTE = "EPMC-Plugin";
    /** Manifest attribute to declare JAR/directory is a plugin. */
    private final static Attributes.Name EPMC_PLUGIN = new Attributes.Name(
            EPMC_PLUGIN_ATTRIBUTE);
    /** String for manifest attribute for plugin name. */
    private final static String PLUGIN_NAME_ATTRIBUTE = "Plugin-Name";
    /** Manifest attribute for plugin name. */
    private final static Attributes.Name PLUGIN_NAME = new Attributes.Name(
            PLUGIN_NAME_ATTRIBUTE);
    /** String for manifest attribute for plugin dependencies. */
    private final static String PLUGIN_DEPENDENCIES_ATTRIBUTE = "Plugin-Dependencies";
    /** Manifest attribute for plugin dependencies. */
    private final static Attributes.Name PLUGIN_DEPENDENCIES = new Attributes.Name(
            PLUGIN_DEPENDENCIES_ATTRIBUTE);
    /** String containing "true", for manifest reading. */
    private final static String TRUE = "true";
    /** Name of method to obtain plugin class identifier. */
    public final static String GET_IDENTIFIER = "getIdentifier";
    /** File ending of a Java class file. */
    private final static String CLASS_FILE_ENDING = ".class";
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** String describing names of plugin class list. */
    private final static String CLASSES_TXT = "classes.txt";
    /** Empty String. */
    private final static String EMPTY = "";
    private final static String SLASH = "/";
    
    /** Class loader constructed by this plugin loader. */
    private final ClassLoader classLoader;
    /** List of all plugins loaded. */
    private final List<Plugin> plugins = new ArrayList<>();
    /** List of all plugin classes loaded. */
    private final List<Class<? extends PluginInterface>> classes = new ArrayList<>();
    private final boolean checkSanity;

    PluginLoader(List<String> pluginPathStrings) {
        this(pluginPathStrings, true);
    }
    
    private PluginLoader(List<String> pluginPathStrings, boolean checkSanity) {
        assert pluginPathStrings != null;
        for (String name : pluginPathStrings) {
            assert name != null;
        }
        this.checkSanity = checkSanity;
        List<Path> pluginFiles = stringsToPaths(pluginPathStrings);
        List<Path> allPluginPaths = buildAllPluginPaths(pluginFiles);
        this.classLoader = buildClassLoader(allPluginPaths);
        this.plugins.addAll(loadPlugins(this.classLoader, allPluginPaths));
        this.classes.addAll(extractClasses(this.plugins));        
    }

    private ClassLoader buildClassLoader(List<Path> pluginPaths) {
        assert pluginPaths != null;
        for (Path path : pluginPaths) {
            assert path != null;
        }
        URL[] urls = new URL[pluginPaths.size()];
        for (int pluginNr = 0; pluginNr < pluginPaths.size(); pluginNr++) {
            Path pluginPath = pluginPaths.get(pluginNr);
            try {
                String urlString = pluginPath.toUri().toURL().toString();
                /* since Java 9 or 10, separator at end might get lost :-| */
                if (!urlString.endsWith(SEPARATOR)) {
                    urlString = urlString + SEPARATOR;
                }
                urls[pluginNr] = new URL(urlString);
            } catch (MalformedURLException e) {
                // should not happen, because the URL is automatically generated
                throw new RuntimeException(e);
            }
        }
        /* not using default class loader important when running from Maven */
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

    private List<Path> buildAllPluginPaths(List<Path> pluginFiles) {
        assert pluginFiles != null;
        for (Path plugin : pluginFiles) {
            assert plugin != null;
        }
        List<Path> embeddedPluginList = getEmbeddedPluginsList();
        List<Path> allPlugins = new ArrayList<>();
        allPlugins.addAll(embeddedPluginList);
        allPlugins.addAll(pluginFiles);
        return allPlugins;
    }

    <T extends PluginInterface> List<Class<T>> get(Class<T> shallImplement) {
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

    List<Class<? extends PluginInterface>> getPluginInterfaceClasses() {
        return classes;
    }

    private static List<Path> stringsToPaths(List<String> plugins) {
        assert plugins != null;
        for (String plugin : plugins) {
            assert plugin != null;
        }
        List<Path> result = new ArrayList<>();
        for (int pluginNr = 0; pluginNr < plugins.size(); pluginNr++) {
            String fileName = plugins.get(pluginNr).trim();
            Path path = Paths.get(fileName);
            ensure(path != null, ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_FOUND, fileName);
            ensure(Files.exists(path), ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_FOUND, fileName);
            ensure(Files.isReadable(path), ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_FOUND, fileName);
            result.add(path);
        }
        return result;
    }

    private static List<Class<? extends PluginInterface>> extractClasses(
            List<Plugin> plugins) {
        List<Class<? extends PluginInterface>> result = new ArrayList<>();
        for (Plugin plugin : plugins) {
            result.addAll(plugin.getClasses());
        }
        return result;
    }

    private void loadPluginClasses(Plugin plugin) {
        assert plugin != null;
        Path directory = plugin.getPath();
        boolean isJar = directory.toString().endsWith(JAR_ENDING);
        List<String> classNames = tryReadClassNames(isJar, directory);
        if (classNames != null) {
            readClassesFromList(plugin, classNames);
        } else {
            readClassesRecursively(directory, isJar, plugin, directory);
        }
    }

    private void readClassesFromList(Plugin plugin, List<String> classNames) {
        for (String className : classNames) {
            className = className.trim();
            if (className.equals(EMPTY)) {
                continue;
            }
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoClassDefFoundError e) {
                throw new RuntimeException(e);
            }
            processClassUnchecked(plugin, clazz);
        }
    }
    
    @SuppressWarnings(UNCHECKED)
    private void processClassUnchecked(Plugin plugin, Class<?> clazz) {
        assert plugin != null;
        assert clazz != null;
        plugin.add((Class<PluginInterface>) clazz);
    }


    private List<String> tryReadClassNames(boolean isJAR, Path directory) {
        if (isJAR) {
            return tryReadClassNamesJAR(directory);
        }
        try {
            return Files.readAllLines(directory.resolve(CLASSES_TXT));
        } catch (IOException e) {
            return null;
        }
    }

    private List<String> tryReadClassNamesJAR(Path directory) {
        List<String> result = null;
        try (FileSystem fs = FileSystems.newFileSystem(directory, null)) {
            for (Path root : fs.getRootDirectories()) {
                if (result == null) {
                    result = tryReadClassNames(false, root);
                } else {
                    List<String> nextResult = tryReadClassNames(false, root);
                    if (nextResult != null) {
                        List<String> newResult = new ArrayList<>();
                        newResult.addAll(result);
                        newResult.addAll(nextResult);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            fail(ProblemsPlugin.PLUGIN_JAR_FILESYSTEM_FAILED, e, directory);
        }
        return result;
    }
    
    private void readClassesRecursively(Path path, boolean isJAR, Plugin plugin, Path directory) {
        assert path != null;
        assert plugin != null;
        assert directory != null;

        if (Files.isRegularFile(path) && path.toString().endsWith(CLASS_FILE_ENDING)) {
            readClassesClass(path, isJAR, plugin, directory);
        } else if (Files.isRegularFile(path) && path.toString().endsWith(JAR_ENDING)) {
            readClassesJAR(path, isJAR, plugin, directory);
        } else if (Files.isDirectory(path)) {
            readClassesDirectory(path, isJAR, plugin, directory);
        }
    }

    private void readClassesClass(Path path, boolean isJAR, Plugin plugin,
            Path directory) {
        assert path != null;
        assert plugin != null;
        assert path != null;

        String pathString = null;
        if (isJAR) {
            /* subpath in following line is indeed necessary, don't remove */
            pathString = path.subpath(0, path.getNameCount()).toString();
        } else {
            pathString = path.subpath(directory.getNameCount(), path.getNameCount()).toString();
        }
        String className = processClassName(pathString);
        Class<?> clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
        } catch (NoClassDefFoundError e) {
            /* Has occurred when some JAR files were included in plugins.
             */
        }
        if (clazz != null) {
            processClass(plugin, clazz);
        }
    }

    private void readClassesJAR(Path path, boolean isJAR, Plugin plugin,
            Path directory) {
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            for (Path root : fs.getRootDirectories()) {
                readClassesRecursively(root, isJAR, plugin, directory);
            }
        } catch (IOException e) {
            fail(ProblemsPlugin.PLUGIN_JAR_FILESYSTEM_FAILED, e, path);
        }
    }

    private void readClassesDirectory(Path path, boolean isJAR,
            Plugin plugin, Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path sub : stream) {
                readClassesRecursively(sub, isJAR, plugin, directory);
            }
        } catch (IOException e) {
            fail(ProblemsPlugin.PLUGIN_READ_DIRECTORY_FAILED, e, path);
        }
    }

    @SuppressWarnings(UNCHECKED)
    private void processClass(Plugin plugin, Class<?> clazz)
    {
        assert plugin != null;
        assert clazz != null;
        if (isPluginClass(clazz)) {
            plugin.add((Class<PluginInterface>) clazz);
        }
    }

    private static String processClassName(String name) {
        assert name != null;
        return name.substring(0, name.length() - 6).replace(SEPARATOR_CHAR, CLASS_NAME_SEPARATOR);
    }

    private static boolean isPluginClass(Class<?> clazz) {
        assert clazz != null;
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        if (!PluginInterface.class.isAssignableFrom(clazz)) {
            return false;
        }
        boolean constructorFound = false;
        Constructor<?>[] constructors = null;
        try {
            constructors = clazz.getConstructors();
        } catch (NoClassDefFoundError e) {
            return false;
        }
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length != 0) {
                continue;
            }
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            constructorFound = true;
            break;
        }
        if (!constructorFound) {
            return false;
        }
        for (Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                return false;
            }
        }
        Object instance = Util.getInstance(clazz);
        try {
            Method getIdentifier = clazz.getMethod(GET_IDENTIFIER);
            Object result = getIdentifier.invoke(instance);
            if (result == null) {
                return false;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private List<Plugin> loadPlugins(ClassLoader classLoader, List<Path> pluginPaths) {
        assert classLoader != null;
        assert pluginPaths != null;
        for (Path path : pluginPaths) {
            assert path != null;
        }
        List<Plugin> result = new ArrayList<>();
        /* following allows to Thread.currentThread().getContextClassLoader()
         * to load resources which are either in a plugin or in the main part of
         * EPMC (remember that the URLClassLoader has to load resources using
         * its parent class loader if it does not find a certain resource).
         */
        Thread.currentThread().setContextClassLoader(this.classLoader);
        for (Path path : pluginPaths) {
            Plugin plugin = new Plugin();
            plugin.setPath(path);
            addManifestInformation(plugin);
            result.add(plugin);
        }
        checkPluginSanity(result);
        for (Plugin plugin : result) {
            loadPluginClasses(plugin);
        }
        return result;
    }

    /**
     * Perform sanity checks for plugins used.
     * It is checked whether each plugin is loaded only once and whether plugins
     * depending on other plugins are loaded after their dependencies. 
     * This list of plugins must not be {@code null} and must not contain
     * {@code null} entries.
     * Note that the order of plugins is strict in the sense depending plugins
     * must be loaded after their dependencies. In principle, we could also have
     * ordered the plugins ourselves using a topological sort. The reason we do
     * not do this is, that often the order of plugins plays a role. For
     * instance, if a plugin for solver A is loaded after solve B, A should have
     * priority over B if both are applicable to the same problem instance.
     * Therefore, we do not order plugins automatically, as this might make the
     * user careless about the order in which they are specified.
     * 
     * @param plugins list of plugins to be checked
     */
    private void checkPluginSanity(List<Plugin> plugins) {
        if (!checkSanity) {
            return;
        }
        assert plugins != null;
        for (Plugin plugin : plugins) {
            assert plugin != null;
            assert plugin.getName() != null;
        }
        Set<String> seen = new HashSet<>();
        for (Plugin plugin : plugins) {
            ensure(!seen.contains(plugin.getName()),
                    ProblemsPlugin.PLUGIN_PLUGIN_SPECIFIED_TWICE,
                    plugin.getName());
            for (String dependency : plugin.getDependencies()) {
                ensure(seen.contains(dependency),
                        ProblemsPlugin.PLUGIN_PLUGIN_DEPENDENCIES_MISSING,
                        plugin.getName(), dependency);
            }
            seen.add(plugin.getName());
        }
    }

    private static void addManifestInformation(Plugin plugin) {
        assert plugin != null;
        Path path = preparePathForReadingManifest(plugin.getPath());
        ensure(Files.isDirectory(path), ProblemsPlugin.PLUGIN_IO_PROBLEM, path);
        Path manifestPath = path.resolve(META_INF).resolve(MANIFEST);
        ensure(Files.isReadable(manifestPath), ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_NO_MANIFEST, path);
        ensure(Files.isRegularFile(manifestPath), ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_MANIFEST_NOT_REGULAR, path);

        Manifest manifest = null;
        try (InputStream manifestIS = Files.newInputStream(manifestPath)) {
            manifest = new Manifest(manifestIS);
        } catch (IOException e) {
            fail(ProblemsPlugin.PLUGIN_IO_PROBLEM_MANIFEST, e, path);
        }
        ensure(manifest != null, ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_NO_MANIFEST, path);
        Attributes attributes = manifest.getMainAttributes();
        String iscasMCPluginString = attributes.getValue(EPMC_PLUGIN);
        ensure(iscasMCPluginString != null,
                ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_MANIFEST_MISSING_ENTRY,
                path, EPMC_PLUGIN_ATTRIBUTE);
        ensure(iscasMCPluginString.equals(TRUE), ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_EPMC_PLUGIN_NOT_TRUE);
        String pluginNameString = attributes.getValue(PLUGIN_NAME);
        ensure(pluginNameString != null,
                ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_MANIFEST_MISSING_ENTRY,
                path, PLUGIN_NAME_ATTRIBUTE);
        plugin.setName(pluginNameString);
        String pluginDependenciesString = attributes.getValue(PLUGIN_DEPENDENCIES);
        ensure(pluginDependenciesString != null,
                ProblemsPlugin.PLUGIN_FILE_NOT_PLUGIN_MANIFEST_MISSING_ENTRY,
                path, PLUGIN_DEPENDENCIES_ATTRIBUTE);
        String[] pluginDependenciesArray = pluginDependenciesString.split(SPACE);
        if (pluginDependenciesArray.length == 1 && pluginDependenciesArray[0].length() == 0) {
            pluginDependenciesArray = new String[0];
        }
        plugin.addDependencies(pluginDependenciesArray);
    }

    private static Path preparePathForReadingManifest(Path path) {
        assert path != null;
        if (!Files.isDirectory(path)) {
            try {
                // TODO leads to resource leak;
                // however, cannot return path of already closed file system
                FileSystem system = FileSystems.newFileSystem(path,
                        PluginLoader.class.getClassLoader());
                Iterator<Path> iterator = system.getRootDirectories().iterator();
                assert iterator.hasNext();
                path = iterator.next();
                assert !iterator.hasNext();
            } catch (IOException e) {
                fail(ProblemsPlugin.PLUGIN_IO_PROBLEM, e, path);
            }
        }
        assert path != null;
        return path;
    }

    private List<Path> getEmbeddedPluginsList() {
        Path origPath = null;
        boolean origIsJar = false;
        List<Path> result = null;
        try {
            origPath = Paths.get(PluginLoader.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            // should not happen, as URI not user-generated
            throw new RuntimeException(e);
        }
        Path embeddedPluginsPath = null;
        if (Files.isDirectory(origPath)) {
            embeddedPluginsPath = origPath.resolve(EMBEDDED_PLUGINS_DIR);
        } else {
            origIsJar = true;
            FileSystem system = null;
            try {
                system = FileSystems.newFileSystem(origPath,
                        PluginLoader.class.getClassLoader());
                embeddedPluginsPath = system.getPath(SEPARATOR + EMBEDDED_PLUGINS_DIR);
            } catch (IOException e) {
                // Rethrown as runtime exception, because a problem here is not
                // caused by the user, but by the one having prepared the JAR.
                throw new RuntimeException(e);
            }
            // TODO
            //AT: system remains open; if it is closed as it should be before leaving the method,
            //    then a ClosedFileSystemException is thrown when the returned list is used
        }
        if (embeddedPluginsPath == null || !Files.isReadable(embeddedPluginsPath)) {
            result = Collections.emptyList();
        }
        List<String> pluginSubs = null;
        try {
            pluginSubs = Files.readAllLines(embeddedPluginsPath.resolve(EMBEDDED_PLUGINS_LIST));
        } catch (IOException e2) {
            result = Collections.emptyList();
        }
        if (result == null) {
            result = new ArrayList<>();
            for (String pluginSub : pluginSubs) {
                pluginSub = pluginSub + SEPARATOR;
                Path embeddedPluginPath = embeddedPluginsPath.resolve(pluginSub);
                ensure(Files.exists(embeddedPluginPath), ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_FOUND, embeddedPluginPath);
                ensure(Files.isReadable(embeddedPluginPath), ProblemsPlugin.PLUGIN_PLUGIN_FILE_NOT_READABLE, embeddedPluginPath);
                //                checkPluginPath(embeddedPluginPath);
                assert Files.isDirectory(embeddedPluginPath) || !origIsJar;
                result.add(embeddedPluginPath);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(TOSTRING_PLUGINS);
        for (Plugin plugin : this.plugins) {
            builder.append(plugin + TOSTRING_ENDLINE);
        }
        builder.append(TOSTRING_CLASSES);
        for (Class<?> clazz : this.classes) {
            builder.append(clazz + TOSTRING_ENDLINE);
            builder.append(TOSTRING_TERMINATOR);
        }
        return builder.toString();
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException();
        }
        String base = args[0];
        Path out = Paths.get(base + SLASH + CLASSES_TXT);
        if (Files.exists(out)) {
            Files.delete(out);
        }
        List<String> pluginList = new ArrayList<>();
        pluginList.add(base);
        PluginLoader loader = new PluginLoader(pluginList, false);
        List<Class<? extends PluginInterface>> classes = loader.getPluginInterfaceClasses();
        List<String> result = new ArrayList<>();
        for (Class<? extends PluginInterface> clazz : classes) {
            result.add(clazz.getName());
        }
        Files.write(out, result);
    }
}
