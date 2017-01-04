package epmc.automata.determinisation;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationAutomatonSchewe implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-automaton-schewe";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Map<String, Class<?>> automatonMap = options.get(OptionsAutomaton.AUTOMATON_CLASS);
        assert automatonMap != null;
        automatonMap.put(AutomatonScheweParity.IDENTIFIER, AutomatonScheweParity.Builder.class);
        automatonMap.put(AutomatonScheweRabin.IDENTIFIER, AutomatonScheweRabin.Builder.class);
    }
}
