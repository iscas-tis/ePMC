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

package epmc.jani.model;

import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_AutomataProcessor implements PRISMExporter_ProcessorStrict {

    private Automata automata = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Automata;

        automata = (Automata) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert automata != null;

        StringBuilder prism = new StringBuilder();

        for (Automaton automaton : automata) {
            //AT: the location renaming is performed during the computation of AutomatonProcessor.toPRISM()
            String automatonString = PRISMExporter_ProcessorRegistrar.getProcessor(automaton)
                    .toPRISM();
            prism.append(JANIComponentRegistrar.locationRenaming(automaton))
                .append(automatonString)
                .append("\n");
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert automata != null;

        for (Automaton automaton : automata) {
            PRISMExporter_ProcessorRegistrar.getProcessor(automaton)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert automata != null;

        boolean usesTransient = false;
        for (Automaton automaton : automata) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(automaton)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
