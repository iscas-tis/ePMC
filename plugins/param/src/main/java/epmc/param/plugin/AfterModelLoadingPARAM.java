package epmc.param.plugin;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.param.value.ContextValuePARAM;
import epmc.plugin.AfterModelCreation;
import epmc.value.ContextValue;
import epmc.value.TypeReal;

public class AfterModelLoadingPARAM implements AfterModelCreation {
    public final static String IDENTIFIER = "param-after-model-loading";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		Options options = contextValue.getOptions();
        ContextValuePARAM contextValuePARAM = options.get(BeforeModelLoadingPARAM.PARAM_CONTEXT_VALUE_PARAM);
		TypeReal.set(contextValuePARAM.getTypeReal());
//		contextValue.setTypeInteger(contextValuePARAM.getOldTypeInteger());
	}
}
