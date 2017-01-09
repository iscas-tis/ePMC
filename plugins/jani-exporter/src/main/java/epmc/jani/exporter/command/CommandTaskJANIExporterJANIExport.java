package epmc.jani.exporter.command;

import static epmc.error.UtilError.ensure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import epmc.error.EPMCException;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.messages.MessagesJANIExporter;
import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.jani.model.UtilModelParser;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.prism.model.convert.PRISM2JANIConverter;

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
	public final static String IDENTIFIER = "jani-export";
	
	public final static String JANI_EXTENSION = ".jani";
	
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
		Options options = modelChecker.getModel().getContextValue().getOptions();
		Log log = options.get(OptionsMessages.LOG);
		//TODO: probably the reward prefix has to be moved to a more appropriate place independent from the converted model type
		PRISM2JANIConverter.setRewardPrefix(options.getString(OptionsJANIExporter.JANI_EXPORTER_REWARD_NAME_PREFIX));
    	try {
            List<String> modelFilenames = options.get(OptionsEPMC.MODEL_INPUT_FILES);
            ensure(modelFilenames != null, ProblemsJANIExporter.JANI_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);
            ensure(modelFilenames.size() > 0, ProblemsJANIExporter.JANI_EXPORTER_MISSING_INPUT_MODEL_FILENAMES);

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
    			log.send(MessagesJANIExporter.JANI_EXPORTER_MISSING_JANI_FILENAME, janiFilename, modelFilename);
    		}
    		File janiFile = new File(janiFilename); 
    		if (janiFile.exists() && !janiFile.canWrite()) {
    			log.send(MessagesJANIExporter.JANI_EXPORTER_UNWRITEABLE_JANI_FILE, janiFilename, modelFilename);
    		}
    		
    		if (modelChecker.getModel() instanceof ModelJANIConverter) {
            	ModelJANIConverter model = (ModelJANIConverter) modelChecker.getModel();
            	log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_MODEL_CREATION, modelName);
            	ModelJANI jani = model.toJANI();
            	log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_MODEL_CREATION_DONE, modelName);
            	jani.setName(modelName);
            	log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_FILE_CREATION, janiFilename);
            	try (PrintWriter out = new PrintWriter(janiFile)) {
            	    out.println(UtilModelParser.prettyString(jani));
            	} catch (FileNotFoundException e) {
            		throw new RuntimeException(e);
        		}
            	log.send(MessagesJANIExporter.JANI_EXPORTER_JANI_FILE_CREATION_DONE, janiFilename);
        	}
		} catch (EPMCException e) {
			log.send(e);
		}
    }
}
