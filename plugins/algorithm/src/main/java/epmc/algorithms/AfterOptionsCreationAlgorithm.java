package epmc.algorithms;

import epmc.error.EPMCException;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationAlgorithm implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-algorithm";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        options.addOption().setIdentifier(OptionsAlgorithm.DD_SCC_ALGORITHM)
        	.setBundleName(OptionsAlgorithm.OPTIONS_ALGORITHM)
        	.setType(new OptionTypeEnum(DdSccAlgorithm.class))
        	.setDefault(DdSccAlgorithm.BLOEM)
        	.setCommandLine().setGui().setWeb().build();
    }
}
