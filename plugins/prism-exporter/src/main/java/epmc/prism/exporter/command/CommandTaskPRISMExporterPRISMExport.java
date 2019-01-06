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

package epmc.prism.exporter.command;

import static epmc.error.UtilError.ensure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import epmc.error.EPMCException;
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
import epmc.value.OptionsValue;

/**
 * Command to start PRISM exporter.
 * 
 * @author Andrea Turrini
 */
public final class CommandTaskPRISMExporterPRISMExport implements CommandTask {
    /** Unique identifier of JANI converter start command. */
    public final static String IDENTIFIER = "prism-export";

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
