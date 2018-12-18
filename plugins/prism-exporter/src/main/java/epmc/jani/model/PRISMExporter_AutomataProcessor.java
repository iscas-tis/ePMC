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
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PRISMExporter_AutomataProcessor implements JANI2PRISMProcessorStrict {

    private Automata automata = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
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
            String automatonString = ProcessorRegistrar.getProcessor(automaton).toPRISM();
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
            ProcessorRegistrar.getProcessor(automaton)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert automata != null;

        boolean usesTransient = false;
        for (Automaton automaton : automata) {
            usesTransient |= ProcessorRegistrar.getProcessor(automaton)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
