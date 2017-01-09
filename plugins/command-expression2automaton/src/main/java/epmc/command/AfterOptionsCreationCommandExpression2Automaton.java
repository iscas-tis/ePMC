package epmc.command;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.error.EPMCException;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCommandExpression2Automaton implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-command-expression2automaton";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        
        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;
        options.addCommand()
        	.setBundleName(OptionsCommandExpression2Automaton.OPTIONS_COMMAND_EXPRESSION2AUTOMATON)
        	.setIdentifier(CommandTaskExpression2Automaton.IDENTIFIER)
        	.setCommandLine()
        	.setGui()
        	.setWeb()
        	.build();
        commandTaskClasses.put(OptionsCommandExpression2Automaton.EXPRESSION2AUTOMATON.name().toLowerCase(), CommandTaskExpression2Automaton.class);
        Map<String, Class<?>> automatonMap = options.get(OptionsAutomaton.AUTOMATON_CLASS);
        OptionTypeMap<Class<?>> typeAutomaton = new OptionTypeMap<>(automatonMap);
        options.addOption().setBundleName(OptionsCommandExpression2Automaton.OPTIONS_COMMAND_EXPRESSION2AUTOMATON)
        	.setIdentifier(OptionsCommandExpression2Automaton.AUTOMATON_EXPRESSION2TYPE)
        	.setType(typeAutomaton)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(OptionsAutomaton.AUTOMATON_CATEGORY).build();
    }
}
