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

package epmc.automaton.plugin;

import java.util.Map;

import epmc.automaton.AutomatonExporterDot;
import epmc.automaton.OptionsAutomaton;
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
    public void process(Options options) {
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

        Map<String, Class<?>> automatonExporterMap = new OrderedMap<>(true);
        automatonExporterMap.put(AutomatonExporterDot.IDENTIFIER, AutomatonExporterDot.class);
        options.set(OptionsAutomaton.AUTOMATON_EXPORTER_CLASS, automatonExporterMap);
        
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
