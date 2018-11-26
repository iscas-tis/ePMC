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

package epmc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.jar.Manifest;

import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.graph.SchedulerPrinter;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

/**
 * Several utility functions not found in the standard Java library.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Util {
    /** location of a Java manifest file */
    public final static String MANIFEST_LOCATION = "META-INF/MANIFEST.MF";
    /** used to obtain SVN revision number */
    public final static String SCM_REVISION = "SCM-Revision";
    /** String containing line end character. */
    private final static String LINE_END = "\n";
    /** Name of {@code getInstance()} method. */
    private static final String GET_INSTANCE = "getInstance";

    /**
     * Obtain an instance of a given class.
     * If the creation of the class fails, no exception is thrown but {@code
     * null} is returned. None of the parameters may be {@code null}.
     * 
     * @param clazz name of the class to create instance of
     * @return
     */
    public static <T> T getInstance(Class<T> clazz) {
        assert clazz != null;
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Obtain the singleton instance of a class implementing singleton pattern.
     * The instance will be obtained by calling the static method
     * {@code getInstance()} on the class parameter. If there is no such method
     * or the method cannot be called, {@code null} will be returned. If the
     * method does not return an object of the class parameter, a
     * {@link ClassCastException} will be thrown. 
     * The class parameter must not be {@code null}.
     * 
     * @param clazz class to obtain singleton of
     * @return singleton of the class (or {@code null})
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSingletonInstance(Class<T> clazz) {
        assert clazz != null;
        try {
            return (T) clazz.getMethod(GET_INSTANCE).invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            return null;
        }
    }

    /**
     * Obtain current line executed as integer.
     * This function is meant for debugging purposes.
     * 
     * @return current line number being executed
     */
    public static int __LINE__() {
        int line = Thread.currentThread().getStackTrace()[2].getLineNumber();
        return line;
    }

    /**
     * Obtain entry from manifest.
     * None of the parameters may be {@code null}. If no entry with the given
     * name is found, {@code null} is returned.
     * 
     * @param name name of manifest entry to obtain
     * @return manifest entry
     */
    public static String getManifestEntry(String name) {
        assert name != null;
        String result = null;
        Enumeration<URL> resources;
        try {
            resources = Util.class.getClassLoader()
                    .getResources(MANIFEST_LOCATION);
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                result = manifest.getMainAttributes().getValue(name);
            }
        } catch (IOException e) {
        }
        return result;
    }

    /**
     * Transform given trace to string.
     * This function is meant for debugging purposes. None of the parameters
     * may be {@code null} and may not contain {@code null} entries.
     * 
     * @param stackTrace stacktrace to transform to string
     * @return string description of stack trace
     */
    public static String stackTraceToString(StackTraceElement[] stackTrace) {
        assert stackTrace != null;
        for (StackTraceElement element : stackTrace) {
            assert element != null;
        }
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            builder.append(element);
            builder.append(LINE_END);
        }
        return builder.toString();
    }

    /**
     * Print current stack trace to standard output.
     * This function is meant for debugging purposes.
     */
    public static void printStackTrace() {
        System.out.println(stackTraceToString(Thread.currentThread().getStackTrace()));
    }

    /**
     * Obtain a resource from a given class loader as string.
     * None of the parameters may be {@code null}. In case the resource could
     * not be loaded, {@code null} will be returned.
     * 
     * @param loader class loader to use to load resource
     * @param name name of resource
     * @return string representation of resource loaded
     */
    public static String getResourceAsString(ClassLoader loader, String name) {
        assert loader != null;
        assert name != null;
        try {
            return new String(Files.readAllBytes(Paths.get(loader.getResource(name).toURI())));
        } catch (IOException e) {
        } catch (URISyntaxException e) {
        }
        return null;
    }

    /**
     * Obtain a resource from class loader of given class as string.
     * None of the parameters may be {@code null}. In case the resource could
     * not be loaded, {@code null} will be returned.
     * 
     * @param clazz class of which to use loader to load resource
     * @param name name of resource
     * @return string representation of resource loaded
     */
    public static String getResourceAsString(Class<?> clazz, String name) {
        return getResourceAsString(clazz.getClassLoader(), name);
    }

    /**
     * Obtain instance identified by string from class map.
     * The first parameter is a map from strings to class. The second parameter
     * is used to identify a class in the map of which then an instance is
     * created and returned. In case the class fails to be created, {@code null}
     * will be returned. None of the parameters may be {@code null} or contain
     * {@code null} entries. The second parameter must be a key in the class
     * map.
     * 
     * @param map map from {@link String} to candidate @{link Class}es
     * @param identifier identifier within map of class to obtain instance of
     * @return instance of class with given identifier
     */
    public static <T> T getInstance(Map<String,Class<T>> map, String identifier) {
        assert map != null;
        for (Entry<String, Class<T>> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        assert identifier != null;
        assert map.containsKey(identifier) : identifier;
        Class<T> clazz = map.get(identifier);
        return getInstance(clazz);
    }

    // TODO document
    public static <T> T getSingletonInstance(Map<String,Class<T>> map, String identifier) {
        assert map != null;
        for (Entry<String, Class<T>> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        assert identifier != null;
        assert map.containsKey(identifier) : identifier;
        Class<T> clazz = map.get(identifier);
        return getSingletonInstance(clazz);
    }

    /**
     * Obtain instance of a class which fulfills a given criterion.
     * The first parameter is a map from strings to classes. The second
     * parameter is a predicate on instances of this class. The function will
     * create instances of these classes and return an instance which fulfills
     * the predicate, if such a class exists. Otherwise, {@code null} will be
     * returned. None of the parameters may be {@code null} or contain
     * {@code null} entries.
     * 
     * @param map map from {@link String} to candidate @{link Class}es
     * @param tester predicate to test class instances against.
     * @return class instance from the map fulfilling criterion or {@code null}
     */
    public static <T> T getInstance(Map<String,Class<? extends T>> map, Predicate<T> tester) {
        assert map != null;
        assert tester != null;
        for (Entry<String, Class<? extends T>> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Class<? extends T> entry : map.values()) {
            T instance = getInstance(entry);
            if (tester.test(instance)) {
                return instance;
            }
        }
        return null;
    }

    public static <T> T getInstance(List<Class<T>> list, Predicate<T> tester) {
        assert list != null;
        assert tester != null;
        for (Class<T> entry : list) {
            assert entry != null;
        }
        for (Class<T> entry : list) {
            T instance = getInstance(entry);
            if (tester.test(instance)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Obtain instance of a class which fulfills a given criterion.
     * The first parameter is a map from strings to classes. The second
     * parameter is a predicate over classes. The function will check whether
     * there is a class in the map which fulfills the predicate and return it
     * if such a class exists. Otherwise, {@code null} will be returned. None of
     * the parameters may be {@code null} or contain {@code null} entries.
     * 
     * @param map map from {@link String} to candidate @{link Class}es
     * @param tester predicate to test class instances against.
     * @return class instance from the map fulfilling criterion or {@code null}
     */
    public static <T> T getInstanceByClass(Map<String,Class<T>> map, Predicate<Class<T>> tester) {
        assert map != null;
        assert tester != null;
        for (Entry<String, Class<T>> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Class<T> entry : map.values()) {
            if (tester.test(entry)) {
                return getInstance(entry);
            }
        }
        return null;
    }

    /**
     * Obtain instance of a class which fulfills a given criterion.
     * The first parameter is a map from strings to classes. The second
     * parameter identifies candidate classes of the map. The third parameter is
     * a predicate on instances of this class. The function will create
     * instances of candidate classes and return an instance which fulfills the
     * predicate, if such a class exists. Otherwise, {@code null} will be
     * returned. None of the parameters may be {@code null} or contain
     * {@code null} entries. All the candidate strings must be keys in the map.
     * 
     * @param map map from {@link String} to candidate @{link Class}es
     * @param candidates defines candidates of classes to check
     * @param tester predicate to test class instances against.
     * @return class instance from the map fulfilling criterion or {@code null}
     */
    public static <T> T getInstance(Map<String,Class<T>> map,
            Iterable<String> candidates,
            Predicate<T> tester) {
        assert map != null;
        assert candidates != null;
        assert tester != null;
        for (Entry<String, Class<T>> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (String candidate : candidates) {
            assert map.containsKey(candidate) : "\"" + candidate + "\"";
        }
        for (String candidate : candidates) {
            Class<T> clazz = map.get(candidate);
            T instance = getInstance(clazz);
            if (tester.test(instance)) {
                return instance;
            }
        }
        return null;
    }

    public static void printScheduler(OutputStream out, LowLevel graph, Scheduler scheduler) {
        Map<String,Class<SchedulerPrinter>> schedulerPrinters = Options.get().get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS);
        assert schedulerPrinters != null;
        for (Entry<String, Class<SchedulerPrinter>> entry : schedulerPrinters.entrySet()) {
            SchedulerPrinter printer = Util.getInstance(entry.getValue());
            printer.setScheduler(scheduler);
            printer.setLowLevel(graph);
            printer.setOutput(out);
            if (printer.canHandle()) {
                printer.print();
                return;
            }
        }
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Util() {
    }
}
