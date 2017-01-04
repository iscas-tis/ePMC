package epmc.automaton.plugin;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationAutomata implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-automata";
    /** Default ltl2tgba command. */
    private final static String DEFAULT_LTL2TGBA_COMMAND = "ltl2tgba";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        assert options != null;
        OptionTypeInteger typeInteger = OptionTypeInteger.getInstance();
        OptionTypeString typeString = OptionTypeString.getInstance();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        Category category = options.addCategory()
        		.setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
        		.setIdentifier(OptionsAutomaton.AUTOMATON_CATEGORY)
        		.build();
        
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATON_DD_MAX_STATES)
            .setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
            .setType(typeInteger).setDefault("1000")
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        
        Map<String, Class<?>> automatonMap = new OrderedMap<>(true);
        options.set(OptionsAutomaton.AUTOMATON_CLASS, automatonMap);
        
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATON_BUILDER)
            .setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
            .setType(new OptionTypeEnum(OptionsAutomaton.Ltl2BaAutomatonBuilder.class))
            .setCommandLine().setGui()
            .setCategory(category).build();
        
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATON_SPOT_LTL2TGBA_CMD)
            .setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
            .setType(typeString).setDefault(DEFAULT_LTL2TGBA_COMMAND)
            .setCommandLine().setGui()
            .setCategory(category).build();
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATON_SUBSUME_APS)
            .setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
            .setType(typeBoolean).setDefault(true)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATON_DET_NEG)
            .setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
            .setType(new OptionTypeEnum(OptionsAutomaton.Ltl2BaDetNeg.class))
            .setDefault(OptionsAutomaton.Ltl2BaDetNeg.BETTER)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
        
        options.addOption().setIdentifier(OptionsAutomaton.AUTOMATA_REPLACE_NE)
        	.setBundleName(OptionsAutomaton.OPTIONS_AUTOMATON)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
	}

}
