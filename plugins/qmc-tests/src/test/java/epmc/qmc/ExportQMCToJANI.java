package epmc.qmc;

import static epmc.jani.ModelNames.getJANIFilenameFromPRISMFilename;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static epmc.qmc.ModelNames.KEY_DISTRIBUTION_MODEL;
import static epmc.qmc.ModelNames.KEY_DISTRIBUTION_PROPERTIES;
import static epmc.qmc.ModelNames.LOOP_ALTERNATIVE_MODEL;
import static epmc.qmc.ModelNames.LOOP_ALTERNATIVE_PROPERTIES;
import static epmc.qmc.ModelNames.LOOP_ALLOPERATORS_MODEL;
import static epmc.qmc.ModelNames.LOOP_ALLOPERATORS_PROPERTIES;
import static epmc.qmc.ModelNames.LOOP_MODEL;
import static epmc.qmc.ModelNames.LOOP_PROPERTIES;
import static epmc.qmc.ModelNames.SUPERDENSE_CODING_MODEL;
import static epmc.qmc.ModelNames.SUPERDENSE_CODING_PROPERTIES;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonValue;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.extensions.quantum.ModelExtensionQMC;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.model.PropertyPRISMQMC;
import epmc.util.Util;
import epmc.util.UtilJSON;

// TODO check why the tests have such a long set up time
public final class ExportQMCToJANI {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareQMCOptions() {
        List<String> qmcPlugins = new ArrayList<>();
        qmcPlugins.add(System.getProperty("user.dir") + "/../qmc/target/classes/");
        qmcPlugins.add(System.getProperty("user.dir") + "/../qmc-exporter/target/classes/");
        
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, qmcPlugins);
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        return options;
    }

    @Test
    public void keyDistributionTest() {
        export(KEY_DISTRIBUTION_MODEL, KEY_DISTRIBUTION_PROPERTIES);
    }

    @Test
    public void loopTest() {
        export(LOOP_MODEL, LOOP_PROPERTIES);
    }

    @Test
    public void loopAlloperatorsTest() {
        export(LOOP_ALLOPERATORS_MODEL, LOOP_ALLOPERATORS_PROPERTIES);
    }

    /**
     * Alternative version of loop program.
     * We use values from the original model in the theory paper at
     * <a href="https://arxiv.org/pdf/1205.2187.pdf">https://arxiv.org/pdf/1205.2187.pdf</a>
     * rather than the values from the tool paper.
     * Model checking results must be the same.
     */
    @Test
    public void loopAlternativeTest() {
        export(LOOP_ALTERNATIVE_MODEL, LOOP_ALTERNATIVE_PROPERTIES);
    }

    @Test
    public void superdenseCodingTest() {
        export(SUPERDENSE_CODING_MODEL, SUPERDENSE_CODING_PROPERTIES);
    }

    private static void export(String prismFilename, String propertyFilename) {
        String modelName = new File(prismFilename).getName();
        modelName = modelName.substring(0, modelName.lastIndexOf('.'));
        String janiFilename = getJANIFilenameFromPRISMFilename(prismFilename);
        System.out.println("Exporting " + prismFilename + ":");
        System.out.println("Loading");
        Options options = prepareQMCOptions();
        options.set("prism-flatten", false);
        options.set(OptionsJANIExporter.JANI_EXPORTER_SYNCHRONISE_SILENT, false);
        ModelJANIConverter prism;
        prism = (ModelJANIConverter) TestHelper.loadModel(options, prismFilename, propertyFilename);
        System.out.println("Converting");       
        ModelJANI jani = prism.toJANI(true);
        jani.setName(modelName);
        List<ModelExtension> extensions = jani.getModelExtensions();
        extensions.add(Util.getInstance(ModelExtensionQMC.class));
        System.out.println("Generating JSON");
        JsonValue json = null;
        if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_NEW_EXPORTER)) {
            JANIExporter_ProcessorRegistrar.setModel(jani);
            json = JANIExporter_ProcessorRegistrar.getProcessor(jani)
                    .toJSON();
        } else {
            json = jani.generate();
        }
        System.out.println("Writing " + janiFilename);
        try (PrintWriter out = new PrintWriter(janiFilename)) {
            out.println(UtilJSON.prettyString(json));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Done");
        System.out.println();
    }
}
