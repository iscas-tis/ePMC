package epmc.qmc;

import epmc.plugin.AfterCommandExecution;
import epmc.value.ContextValue;

public final class AfterCommandExecutionQMC implements AfterCommandExecution {
    private final static String IDENTIFIER = "after-command-execution-qmc";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(ContextValue contextValue) {
//        options.removeOption(OptionsQMC.CONTEXT_EXPRESSION_QMC);
  //      options.removeOption(OptionsQMC.CONTEXT_VALUE_QMC);
    }

}
