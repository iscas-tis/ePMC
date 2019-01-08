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

package epmc.jani.exporter.command;

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
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.util.UtilJSON;

//TODO: Add support for property files
//TODO: Add support for property name pattern
//TODO: Add support for JANI file name pattern

/**
 * Command to start JANI exporter.
 * 
 * @author Andrea Turrini
 */
public final class CommandTaskJANIExporterJANIExport implements CommandTask {
    /** Unique identifier of JANI converter start command. */
    public static final String IDENTIFIER = "jani-export";

    public static final String JANI_EXTENSION = ".jani";

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
}
