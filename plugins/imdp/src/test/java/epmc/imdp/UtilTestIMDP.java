package epmc.imdp;

import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import epmc.imdp.model.ModelIMDP;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Value;

final class UtilTestIMDP {
    private final static String GNUPLOT_COMMAND = "/opt/local/bin/gnuplot";
    /** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";
    private final static String PLUGIN_MULTIOBJECTIVE_DIR = System.getProperty("user.dir") + "/../ePMC/plugins/propertysolver-multiobjective/target/classes/";
    private final static String SPACE = " ";

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     */
    final static Options prepareIMDPOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_MULTIOBJECTIVE_DIR);
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelIMDP.IDENTIFIER);
        return options;
    }

    public static void generateGraph(PrintStream out,
            Options options, String modelFn, String property,
            double from, double to, int numSteps) {
        assert options != null;
        double step = (to - from) / (numSteps - 1);
        List<Value> results = new ArrayList<>();
        for (int i = 0; i < numSteps; i++) {
            Model model = loadModel(options, modelFn);
            Value result = TestHelper.computeResult(model, String.format(property, from+i*step));
            results.add(result);
        }

        for (int i = 0; i < numSteps; i++) {
            out
            .append(Double.toString(from + i*step))
            .append(SPACE)
            .append(results.get(i).toString())
            .append('\n');
        }
    }

    public static void showGraph(Options options, String modelFn, String property,
            double from, double to, int numSteps) {
        try {
            File datFile = File.createTempFile("plot", "dat");
            PrintStream out = new PrintStream(datFile);
            generateGraph(out, options, modelFn, property, from, to, numSteps);
            out.close();
            File graphFile = File.createTempFile("plot", "gnuplot");
            out = new PrintStream(graphFile);
            out.println("set terminal aqua");
            out.println(String.format("plot \"%s\" using 1:2 with lines title ''", datFile.getAbsolutePath()));
            out.close();
            new ProcessBuilder(GNUPLOT_COMMAND, graphFile.getAbsolutePath()).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void exportGraph(String baseName, Options options, String modelFn, String property,
            double from, double to, int numSteps) {
        exportGraph(baseName, options, modelFn, property, from, to, numSteps, false);
    }
    
    public static void exportGraph(String baseName, Options options, String modelFn, String property,
            double from, double to, int numSteps, boolean exchangeAxes) {
        try {
            Path basePath = Paths.get(baseName);
            String relPathName = basePath.getName(basePath.getNameCount() - 1).toString();
            String datName = baseName + ".dat";
            PrintStream out = new PrintStream(new File(datName));
            generateGraph(out, options, modelFn, property, from, to, numSteps);
            out.close();
            String gnuplotName = baseName + ".gnuplot";
            out = new PrintStream(new File(gnuplotName));
            out.println("set terminal pdf");
            String outputName = relPathName + ".pdf";
            out.println(String.format("set output \"%s\"", outputName));
            String relDatName = relPathName + ".dat";
            String axes = exchangeAxes ? "2:1" : "1:2";
            out.println(String.format("plot \"%s\" using %s with lines title ''",
                    relDatName, axes));
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UtilTestIMDP() {
    }
}
