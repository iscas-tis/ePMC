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

package epmc.modelchecker;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.main.options.OptionsEPMC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.Properties;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.UtilModelChecker;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterCommandExecution;
import epmc.plugin.AfterModelCreation;
import epmc.plugin.BeforeModelCreation;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Static auxiliary methods and constants for JUnit tests.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TestHelper {
    public final static String USER_DIR = "user.dir";
    private final static String ASSERTIONS_DISABLED = "Note that tests are run with assertions disabled.";
    private final static String NEQ = " != ";
    private final static String COULDNTSTARTLTL2TGBA = "Could not start"
            + "\"ltl2tgba\". Please make sure that the command is in the path.";
    private final static String LTL2TGBA = "ltl2tgba";
    public final static String ITERATION_TOLERANCE = "graphsolver-iterative-tolerance";
    public static final String ITERATION_STOP_CRITERION = "iteration-stop-criterion";
    /** String to set the flatten options of the PRISM plugin. We store a copy
     * of the identical string in the plugin here, to avoid having
     * compile-time dependencies to the PRISM plugin.
     * */
    public final static String PRISM_FLATTEN = "prism-flatten";
    public final static String MODEL_INPUT_TYPE_PRISM = "prism";
    private final static String PLUGIN_LIST_PATTERN = "%s_%s.pluginlist";
    private final static String USER_NAME_PROPERTY = "user.name";
    private final static String UNKNOWN_HOST = "Unknown";
    private final static String PLUGINLIST_NOT_FOUND = "Plugin list file "
            + "\"%s\" not found.";
    
    public enum LogType {
        TRANSLATE,
        NOTRANSLATE,
        SILENT,
        LIST
    }
    
    public static void prepare() {
        boolean assertionsEnabled = false;
        try {
            assert false;
        } catch (AssertionError e) {
            assertionsEnabled = true;
        }
        if (!assertionsEnabled) {
            System.out.println(ASSERTIONS_DISABLED);
        }

        try {
            Runtime.getRuntime().exec(LTL2TGBA);
        } catch (IOException e) {
            System.err.println(COULDNTSTARTLTL2TGBA);
        }
    }
    
    public static Model loadModel(Options options,
            List<InputStream> inputs, InputStream propertyStream) {
        assert options != null;
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        try {
            Model model = UtilOptions.getInstance(options,
                    OptionsModelChecker.MODEL_INPUT_TYPE);
            assert model != null;
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            assert classloader != null;
            InputStream[] inputsArray = inputs.toArray(new InputStream[inputs.size()]);
            model.read(inputsArray);
            if (propertyStream != null) {
                model.getPropertyList().parseProperties(new InputStream[]{propertyStream});
            }
            return model;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Model loadModel(Options options,
            InputStream input, InputStream propertyStream) {
        return loadModel(options, Collections.singletonList(input), propertyStream);
    }
    
    public static Model loadModel(Options options,
            List<String> modelFiles, String propertiesFile) throws EPMCException {
        assert options != null;
        assert modelFiles != null;
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            assert classloader != null;
            List<InputStream> inputs = new ArrayList<>();
            for (String modelFile : modelFiles) {
                InputStream input = classloader.getResourceAsStream(modelFile);
                if (input == null) {
                    input = new FileInputStream(modelFile);
                }
                assert input != null : modelFile;
                inputs.add(input);
            }
            InputStream propertyStream = null;
            if (propertiesFile != null) {
                propertyStream = classloader.getResourceAsStream(propertiesFile);
                if (propertyStream == null) {
                    propertyStream = new FileInputStream(propertiesFile);
                }
                assert propertyStream != null : propertiesFile;
            }
            return loadModel(options, inputs, propertyStream);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
    
    public static Model loadModel(Options options,
            String modelFile, String propertiesFile) throws EPMCException {
        return loadModel(options, Collections.singletonList(modelFile), propertiesFile);
    }
    
    public static Model loadModelFromString(Options options,
            String... modelParts) {
        List<InputStream> inputs = new ArrayList<>();
        for (String modelPart : modelParts) {
            InputStream input = new ByteArrayInputStream(modelPart.getBytes());
            inputs.add(input);
        }
        return loadModel(options, inputs, null);
    }

    public static Model loadModelMulti(Options options, String... modelFiles) throws EPMCException {
        assert options != null;
        assert modelFiles != null;
        for (String modelFile : modelFiles) {
            assert modelFile != null;
        }
        return loadModel(options, Arrays.asList(modelFiles), null);
    }

    
    public static Model loadModel(Options options, String modelFile) throws EPMCException {
        assert options != null;
        assert modelFile != null;
        return loadModel(options, modelFile, null);
    }

    public static Model loadModel(String modelFile) throws EPMCException {
        assert modelFile != null;
        Options options = prepareOptions();
        return loadModel(options, modelFile, null);
    }

    public static void addProperty(Model model, String property) {
        assert model != null;
        assert property != null;
        try {
            Properties properties = model.getPropertyList();
            RawProperty rawProp = new RawProperty();
            rawProp.setDefinition(property);
            rawProp.setDescription(null);
            ByteArrayInputStream input = new ByteArrayInputStream(property.getBytes());
            properties.parseProperties(new InputStream[]{input});
            assert properties.getRawProperties().size() >= 1;
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
    
    public static Log prepareLog(Options options, LogType logType) {
        assert logType != null;
        return new LogTest(options, logType);
    }

    public static Log prepareLog(Options options) {
        return prepareLog(options, LogType.LIST);
    }

    private static String getPluginsListFilename() {
        String username = System.getProperty(USER_NAME_PROPERTY);
        String hostname = UNKNOWN_HOST;
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
        }
        return String.format(PLUGIN_LIST_PATTERN, hostname, username);
    }

    private static void exitPluginListNotFound() {
        System.err.println(String.format(PLUGINLIST_NOT_FOUND,
                getPluginsListFilename()));
        System.exit(1);
    }

    private static List<String> readPluginList() throws EPMCException {
        URL url = TestHelper.class.getClassLoader().getResource(getPluginsListFilename());
        if (url == null) {
            exitPluginListNotFound();
        }
        Path file = null;
        try {
            file = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            /* should not happen, because URL automatically generated */
            throw new RuntimeException(e);
        }
        return UtilPlugin.readPluginList(file);
    }

    public static void prepareOptions(Options options, LogType logType,
            String modelInputType) throws EPMCException {
        List<String> pluginDir = readPluginList();
        List<String> oldPluginDir = options.getStringList(OptionsPlugin.PLUGIN);
        pluginDir.addAll(oldPluginDir);        
        options.set(OptionsPlugin.PLUGIN, pluginDir);
        try {
            UtilPlugin.loadPlugins(options);
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
        Locale locale = Locale.getDefault();
        options.set(OptionsEPMC.LOCALE, locale);
        Log log = prepareLog(options, logType);
        options.set(OptionsMessages.LOG, log);
        ContextValue.set(new ContextValue(options));
        processBeforeModelLoading(options);
    }
    
    public static Options prepareOptions(LogType logType,
            String modelInputType) throws EPMCException {
        assert logType != null;
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options, logType, modelInputType);
        return options;
    }
    
    public static Options prepareOptions() throws EPMCException {
        return prepareOptions(LogType.TRANSLATE, MODEL_INPUT_TYPE_PRISM);
    }

    public static void prepareOptions(Options options, String modelInputType) throws EPMCException {
        prepareOptions(options, LogType.LIST, modelInputType);
    }

    public static void prepareOptions(Options options) throws EPMCException {
        prepareOptions(options, LogType.LIST, MODEL_INPUT_TYPE_PRISM);        
    }

    public static Options prepareOptions(String modelInputType) throws EPMCException {
        return prepareOptions(LogType.LIST, modelInputType);
    }
    
    private static String prepareMessage(Value expected, Value actual,
            double tolerance) {
        String message = expected + NEQ + actual;
        return message;
    }
    
    private static Value stringToValue(String expected) {
        assert expected != null;
        Value expectedValue = null;
        try {
            Options options = ContextValue.get().getOptions();
            String modelInputType = options.getAndUnparse(OptionsModelChecker.PROPERTY_INPUT_TYPE);
            options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, MODEL_INPUT_TYPE_PRISM);
            Expression expectedExpression = UtilModelChecker.parseExpression(expected);
            options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, modelInputType);
            expectedValue = evaluateValue(expectedExpression);
        } catch (EPMCException e) {
        	throw new RuntimeException(e);
        }

        return expectedValue;
    }

    // base assertEquals method
    public static void assertEquals(String message, Value expected,
            Value actual, double tolerance) {
        assert expected != null;
        assert actual != null;
        assert tolerance >= 0.0;
        if (message == null) {
            message = prepareMessage(expected, actual, tolerance);
        }
        try {
            if (!expected.getType().canImport(actual.getType()) && !actual.getType().canImport(expected.getType())) {
                assertTrue("types of expected value \"" + expected + "\" and "
                        + " actual value \""+ actual + "\" are incompatible",
                        false);
            }
            assertTrue(message, expected.distance(actual) < tolerance);
        } catch (EPMCException e) {
            throw new Error(e);
        }
    }
    
    public static void assertEquals(Value expected, Value actual,
            double tolerance) {
        assert expected != null;
        assert actual != null;
        assert tolerance >= 0.0;
        assertEquals(null, expected, actual, tolerance);
    }

    public static void assertEquals(Value expected, int actual,
            double tolerance) {
        assert expected != null;
        assert tolerance >= 0.0;
        Value actualValue = expected.getType().newValue();
        try {
            actualValue.set("" + actual);
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
        assertEquals(null, expected, actualValue, tolerance);
    }

    public static void assertEquals(int expected, Value actual,
            double tolerance) {
        assert actual != null;
        assert tolerance >= 0.0;
        Value expectedValue = actual.getType().newValue();
        try {
            expectedValue.set("" + expected);
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
        assertEquals(null, expectedValue, actual, tolerance);
    }

    public static void assertEquals(int expected, BigInteger actual) {
        assert actual != null;
        Assert.assertEquals(BigInteger.valueOf(expected), actual);
    }

    public static void assertEquals(String message, String expected,
            Value actual, double tolerance) {
        assert expected != null;
        assert actual != null;
        assert tolerance >= 0.0;
        Value expectedValue = stringToValue(expected);
        assertEquals(message, expectedValue, actual, tolerance);
    }

    public static void assertEquals(String expected, Value actual,
            double tolerance) {
        assert expected != null;
        assert actual != null;
        assert tolerance >= 0.0;
        Value expectedValue = stringToValue(expected);
        assertEquals(null, expectedValue, actual, tolerance);
    }
    
    public static void assertEquals(String message, Value expected,
            Value actual) {
        assert expected != null;
        assert actual != null;
        assertEquals(null, expected, actual, 0.0);
    }
    
    public static void assertEquals(Value expected, Value actual) {
        assert expected != null;
        assert actual != null;
        assertEquals(null, expected, actual, 0.0);
    }
    
    public static void assertEquals(boolean expected, Value actual) {
        assert actual != null;
        Value expectedValue = actual.getType().newValue();
        try {
            expectedValue.set(Boolean.toString(expected));
            assertTrue(expectedValue.isEq(actual));
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertEquals(String message,
            String expected, BigInteger actual) {
        assert actual != null;
        BigInteger expectedBigInteger = new BigInteger(expected, 10);
        assertTrue(message, expectedBigInteger.equals(actual));
    }

    public static void assertEquals(String expected, BigInteger actual) {
        assert actual != null;
        String message = expected + NEQ + actual;
        assertEquals(message, expected, actual);
    }

    public static ModelCheckerResults computeResults(Model model, String property) {
        addProperty(model, property);
        return computeResults(model);
    }
    
    public static ModelCheckerResults computeResults(Model model) {
        assert model != null;
        try {
            Options options = ContextValue.get().getOptions();
            ModelChecker checker = new ModelChecker(model);
            LogTest log = options.get(OptionsMessages.LOG);
            log.getResults().clear();
            checker.check();
            checker.close();
            return log.getResults();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Map<String,Value> computeResultsMapName(Model model) {
        ModelCheckerResults mcr = computeResults(model);
        Map<String,Value> result = new LinkedHashMap<>();
        for (RawProperty property : mcr.getProperties()) {
            if (mcr.get(property) instanceof Value) {
                result.put(property.getName(), (Value) mcr.get(property));
            } else if (mcr.get(property) instanceof Exception) {
                throw new RuntimeException((Exception) mcr.get(property));
            }
        }
        return result;
    }
    
    public static Map<String,Value> computeResultsMapDefinition(Model model) {
        ModelCheckerResults mcr = computeResults(model);
        Map<String,Value> result = new LinkedHashMap<>();
        for (RawProperty property : mcr.getProperties()) {
            if (mcr.get(property) instanceof Value) {
                result.put(property.getDefinition(), (Value) mcr.get(property));
            } else if (mcr.get(property) instanceof Exception) {
                throw new RuntimeException((Exception) mcr.get(property));
            }
        }
        return result;
    }
    
    public static Value computeResult(Options options, String modelFile,
            String property) {
        assert options != null;
        assert modelFile != null;
        assert property != null;
        try {
            Model model = loadModel(options, modelFile);
            assert model != null;
            return computeResult(model, property);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Value computeResult(Model model, String property) {
        assert model != null;
        assert property != null;
        try {
            addProperty(model, property);
            ModelCheckerResults results = computeResults(model);
            assert results.getProperties().size() == 1;
            Object result = results.get(results.getProperties().iterator().next());
            if (result instanceof Exception) {
                throw new RuntimeException((Exception) result);
            }
            return (Value) result;
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }

    public static Value computeScheduler(Model model, String property) {
        assert model != null;
        assert property != null;
        try {
            addProperty(model, property);
            ModelCheckerResults results = computeResults(model);
            assert results.getProperties().size() == 1;
            Object result = results.get(results.getProperties().iterator().next());
            if (result instanceof Exception) {
                throw new RuntimeException((Exception) result);
            }
            return (Value) result;
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }

    public static void processAfterCommandExecution()
            throws EPMCException {
        for (Class<? extends AfterCommandExecution> clazz : UtilPlugin.getPluginInterfaceClasses(ContextValue.get().getOptions(), AfterCommandExecution.class)) {
            AfterCommandExecution afterCommandExecution = null;
            afterCommandExecution = Util.getInstance(clazz);
            afterCommandExecution.process();
        }
    }

    public static void processBeforeModelLoading(Options options) throws EPMCException {
        assert options != null;
        for (Class<? extends BeforeModelCreation> clazz : UtilPlugin.getPluginInterfaceClasses(options, BeforeModelCreation.class)) {
            BeforeModelCreation beforeModelLoading = null;
            beforeModelLoading = Util.getInstance(clazz);
            beforeModelLoading.process();
        }
    }
    
    public static void processAfterModelLoading(Options options) throws EPMCException {
        assert options != null;
        for (Class<? extends AfterModelCreation> clazz : UtilPlugin.getPluginInterfaceClasses(options, AfterModelCreation.class)) {
            AfterModelCreation afterModelLoading = null;
            afterModelLoading = Util.getInstance(clazz);
            afterModelLoading.process();
        }
    }
    
    public static void close(Options options) {
        assert options != null;
    }
    
    private static Value evaluateValue(Expression expression) throws EPMCException {
        assert expression != null;
        return UtilEvaluatorExplicit.evaluate(expression, new ExpressionToTypeEmpty());
    }
    
    
    public static Value newValue(Type type, String valueString) throws EPMCException {
        Value value = type.newValue();
        value.set(valueString);
        return value;
    }
    
    public static void readProperties(Property property, RawProperties properties, String fileName) throws EPMCException {
        try (InputStream input = new FileInputStream(fileName)) {
            property.readProperties(properties, input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private TestHelper() {
    }    
}
