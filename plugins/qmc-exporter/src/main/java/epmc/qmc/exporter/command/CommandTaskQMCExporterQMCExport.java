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

package epmc.qmc.exporter.command;

import static epmc.error.UtilError.ensure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.messages.MessagesJANIExporter;
import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.extensions.quantum.ModelExtensionQMC;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.prism.exporter.JANI2PRISMConverter;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.messages.MessagesPRISMExporter;
import epmc.prism.exporter.options.OptionsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.qmc.exporter.error.ErrorsQMCExporter;
import epmc.qmc.exporter.messages.MessagesQMCExporter;
import epmc.qmc.exporter.options.ExportTo;
import epmc.qmc.exporter.options.OptionsQMCExporter;
import epmc.util.Util;
import epmc.util.UtilJSON;
import epmc.value.OptionsValue;

/**
 * Command to start QMC exporter to PRISM or JANI.
 * 
 * @author Andrea Turrini
 */
public final class CommandTaskQMCExporterQMCExport implements CommandTask {
    /** Unique identifier of QMC to PRISM or JANI converter start command. */
    public final static String IDENTIFIER = "qmc-export";

    public static final String JANI_EXTENSION = ".jani";
    public final static String PRISM_EXTENSION = ".prism";
    public final static String PROPERTIES_EXTENSION = ".props";

    private ModelChecker modelChecker;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void executeInServer() {
        Options options = Options.get();
        
        ExportTo exportTo = options.getEnum(OptionsQMCExporter.QMC_EXPORTER_EXPORT_TO); 
        switch (exportTo) {
        case JANI:
            exportToJANI();
            break;
        case PRISM:
            exportToPRISM();
            break;
        default:
            Log log = options.get(OptionsMessages.LOG);
            log.send(MessagesQMCExporter.QMC_EXPORTER_UNKNOWN_EXPORT_TO_TARGET, 
                    exportTo);
        }
    }
    
    private void exportToJANI() {
        Options options = Options.get();
        ensure(options.getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_NEW_EXPORTER),
                ErrorsQMCExporter.QMC_EXPORTER_NEW_JANI_EXPORTER_REQUIRED
                );
        
        Log log = options.get(OptionsMessages.LOG);
        //TODO: probably the reward prefix has to be moved to a more appropriate place independent from the converted model type
//        PRISM2JANIConverter.setRewardPrefix(options.getString(OptionsJANIExporter.JANI_EXPORTER_REWARD_NAME_PREFIX));
        try {
            List<String> modelFilenames = options.get(OptionsEPMC.MODEL_INPUT_FILES);
            ensure(modelFilenames != null, 
                    ProblemsJANIExporter.JANI_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);
            ensure(modelFilenames.size() > 0, 
                    ProblemsJANIExporter.JANI_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);

            String modelFilename = modelFilenames.get(0);

            String modelName = options.get(OptionsJANIExporter.JANI_EXPORTER_JANI_MODEL_NAME);
            if (modelName == null) {
                modelName = new File(modelFilename).getName();
                int index = modelName.lastIndexOf('.');
                if (index >= 0) {
                    modelName = modelName.substring(0, index);
                }
            }

            String janiFilename = options.get(OptionsJANIExporter.JANI_EXPORTER_JANI_FILE_NAME);
            if (janiFilename == null) {
                janiFilename = new File(modelFilename).getPath();
                int index = janiFilename.lastIndexOf('.');
                if (index >= 0) {
                    janiFilename = janiFilename.substring(0, index);
                }
                janiFilename = janiFilename + JANI_EXTENSION;
                log.send(MessagesJANIExporter.JANI_EXPORTER_MISSING_JANI_FILENAME, 
                        janiFilename, 
                        modelFilename);
            }
            File janiFile = new File(janiFilename); 
            if (janiFile.exists()) { 
                if (!janiFile.canWrite()) {
                    log.send(MessagesJANIExporter.JANI_EXPORTER_UNWRITABLE_JANI_FILE, 
                            janiFilename, 
                            modelFilename);
                    return;
                } else {
                    if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_OVERWRITE_JANI_FILE)) {
                        log.send(MessagesJANIExporter.JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_OVERWRITE, 
                                janiFilename, 
                                modelFilename);
                    } else {
                        log.send(MessagesJANIExporter.JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_ABORT, 
                                janiFilename, 
                                modelFilename);
                        log.send(MessagesJANIExporter.JANI_EXPORTER_ALREADY_EXISTING_JANI_FILE_HELP, 
                                janiFilename, 
                                "--" + Options.get()
                                    .getOption(OptionsJANIExporter.JANI_EXPORTER_OVERWRITE_JANI_FILE)
                                    .getIdentifier());
                        return;
                    }
                }
            }

            if (modelChecker.getModel() instanceof ModelJANIConverter) {
                ModelJANIConverter model = (ModelJANIConverter) modelChecker.getModel();
                log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_MODEL_CREATION, 
                        modelName);
                ModelJANI jani = model.toJANI(true);
                jani.setName(modelName);
                List<ModelExtension> extensions = jani.getModelExtensions();
                extensions.add(Util.getInstance(ModelExtensionQMC.class));
                log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_MODEL_CREATION_DONE, 
                        modelName);
                log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_FILE_CREATION, 
                        janiFilename);
                JsonValue jsonContent = null;
                if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_NEW_EXPORTER)) {
                    JANIExporter_ProcessorRegistrar.setModel(jani);
                    jsonContent = JANIExporter_ProcessorRegistrar.getProcessor(jani)
                            .toJSON();
                } else {
                    jsonContent = jani.generate();
                }
                try (PrintWriter out = new PrintWriter(janiFile, StandardCharsets.UTF_8.name())) {
                    out.println(UtilJSON.prettyString(jsonContent));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_FILE_CREATION_DONE, 
                        janiFilename);
            }
        } catch (EPMCException e) {
            log.send(e);
        }
    }

    private void exportToPRISM() {
        Options options = Options.get();
        ensure(options.getBoolean(OptionsPRISMExporter.PRISM_EXPORTER_NON_OFFICIAL_PRISM),
                ErrorsQMCExporter.QMC_EXPORTER_NON_OFFICIAL_PRISM_REQUIRED
                );
        options.set(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE, true);
        if (options.getBoolean(OptionsPRISMExporter.PRISM_EXPORTER_EXTENDED_PRISM)) {
            PRISMExporter_ProcessorRegistrar.useExtendedPRISMSyntax();
        }
        PRISMExporter_ProcessorRegistrar.setAllowMultipleLocations(options.getBoolean(OptionsPRISMExporter.PRISM_EXPORTER_ALLOW_MULTIPLE_LOCATIONS));

        Log log = options.get(OptionsMessages.LOG);
        try {
            List<String> modelFilenames = options.get(OptionsEPMC.MODEL_INPUT_FILES);
            ensure(modelFilenames != null, ProblemsPRISMExporter.PRISM_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);
            ensure(modelFilenames.size() > 0, ProblemsPRISMExporter.PRISM_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);

            String modelFilename = modelFilenames.get(0);

            String modelName = options.get(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_NAME);
            if (modelName == null) {
                modelName = new File(modelFilename).getName();
                int index = modelName.lastIndexOf('.');
                if (index >= 0) {
                    modelName = modelName.substring(0, index);
                }
            }

            String prismModelFilename = options.get(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_FILE_NAME);
            if (prismModelFilename == null) {
                prismModelFilename = new File(modelFilename).getPath();
                int index = prismModelFilename.lastIndexOf('.');
                if (index >= 0) {
                    prismModelFilename = prismModelFilename.substring(0, index);
                }
                prismModelFilename = prismModelFilename + PRISM_EXTENSION;
                log.send(MessagesPRISMExporter.PRISM_EXPORTER_MISSING_PRISM_MODEL_FILENAME, prismModelFilename, modelFilename);
            }
            File prismModelFile = new File(prismModelFilename); 
            if (prismModelFile.exists() && !prismModelFile.canWrite()) {
                log.send(MessagesPRISMExporter.PRISM_EXPORTER_UNWRITABLE_PRISM_MODEL_FILE, prismModelFilename, modelFilename);
            }

            String prismPropertiesFilename = options.get(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_PROPERTIES_FILE_NAME);
            if (prismPropertiesFilename == null) {
                prismPropertiesFilename = new File(modelFilename).getPath();
                int index = prismPropertiesFilename.lastIndexOf('.');
                if (index >= 0) {
                    prismPropertiesFilename = prismPropertiesFilename.substring(0, index);
                }
                prismPropertiesFilename = prismPropertiesFilename + PROPERTIES_EXTENSION;
                log.send(MessagesPRISMExporter.PRISM_EXPORTER_MISSING_PRISM_PROPERTIES_FILENAME, prismPropertiesFilename, modelFilename);
            }
            File prismPropertiesFile = new File(prismPropertiesFilename); 
            if (prismPropertiesFile.exists() && !prismPropertiesFile.canWrite()) {
                log.send(MessagesPRISMExporter.PRISM_EXPORTER_UNWRITABLE_PRISM_MODEL_FILE, prismPropertiesFilename, modelFilename);
            }

            Model model = modelChecker.getModel();
            ModelJANI jani = null;
            if (model instanceof ModelJANIConverter) {
                jani = ((ModelJANIConverter) model).toJANI(true);
                jani.setName(modelName);
            } else if (model instanceof ModelJANI) {
                jani = (ModelJANI) model;
            } else {
                ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNSUPPORTED_MODEL, model.getClass().getSimpleName());
            }
            List<ModelExtension> extensions = jani.getModelExtensions();
            extensions.add(Util.getInstance(ModelExtensionQMC.class));
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_CREATION, modelName);
            JANI2PRISMConverter converter = new JANI2PRISMConverter(jani);
            String modelString = converter.convertModel();
            String propertiesString = converter.convertProperties();
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_CREATION_DONE, modelName);
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_FILE_CREATION, prismModelFilename);
            try (PrintWriter out = new PrintWriter(prismModelFile)) {
                out.println(modelString);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_FILE_CREATION_DONE, prismModelFilename);
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_FILE_CREATION, prismPropertiesFilename);
            try (PrintWriter out = new PrintWriter(prismPropertiesFile)) {
                out.println(propertiesString);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            log.send(MessagesPRISMExporter.PRISM_EXPORTER_PRISM_FILE_CREATION_DONE, prismPropertiesFilename);
        } catch (EPMCException e) {
            log.send(e);
        }
    }
}
