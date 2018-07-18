package epmc.param.plugin;

import epmc.error.EPMCException;
import epmc.plugin.AfterModelCreation;

public class AfterModelLoadingPARAM implements AfterModelCreation {
    public final static String IDENTIFIER = "param-after-model-loading";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process() throws EPMCException {
//		Options options = Options.get();
//        TypeReal.set(ContextValue.get().makeUnique(new TypeFractionBigInteger()));
//		contextValue.setTypeInteger(contextValuePARAM.getOldTypeInteger());
	}
}
