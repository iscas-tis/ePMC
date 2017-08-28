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

package epmc.automata.determinisation;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationAutomatonSchewe implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-automaton-schewe";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String, Class<?>> automatonMap = options.get(OptionsAutomaton.AUTOMATON_CLASS);
        assert automatonMap != null;
        automatonMap.put(AutomatonScheweParity.IDENTIFIER, AutomatonScheweParity.Builder.class);
        automatonMap.put(AutomatonScheweRabin.IDENTIFIER, AutomatonScheweRabin.Builder.class);
    }
}
