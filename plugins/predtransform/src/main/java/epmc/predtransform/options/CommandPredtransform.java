package epmc.predtransform.options;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import epmc.error.EPMCException;
import epmc.options.Command;
import epmc.options.Option;
import epmc.options.OptionsEPMC;
import epmc.options.Options;

public class CommandPredtransform implements Command {
    public final static String IDENTIFIER = "predtransform";
    private final static String PREDTRANSFORM_OPTIONS = "PredtransformOptions";
    private final static String PARAMETER_DESCRIPTION = "<input-files>";
    private final static String SHORT_ = "short-";
    private Options options;

    public CommandPredtransform() {
        ResourceBundle.getBundle(PREDTRANSFORM_OPTIONS, Locale.getDefault());
    }    

    @Override
    public String getParameterDescription() {
        return PARAMETER_DESCRIPTION;
    }

    @Override
    public void parse(String... parameters) throws EPMCException {
        Option fileList = options.getOption(OptionsEPMC.INPUT_FILES);
        for (String parameter : parameters) {
            fileList.parse(parameter);
        }
        if (parameters.length == 0) {
            fileList.set(new ArrayList<String>());
        }
    }

    @Override
    public String getShortDescription() {
        Locale locale = options.getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(PREDTRANSFORM_OPTIONS, locale);
        return poMsg.getString(SHORT_ + IDENTIFIER);
    }
    
    @Override
    public String getLongDescription() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(Options options) {
		this.options = options;
	}
}
