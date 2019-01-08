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

package epmc.jani.model.component;

import static epmc.error.UtilError.ensure;

import java.util.List;

import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.Edge;
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;

public class PRISMExporter_ComponentSynchronisationVectorsProcessor implements PRISMExporter_ProcessorStrict {

    private ComponentSynchronisationVectors syncVectors = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ComponentSynchronisationVectors; 

        syncVectors = (ComponentSynchronisationVectors) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert syncVectors != null;

        StringBuilder prism = new StringBuilder();

        //Currently it only checks whether automata full-synchronize on common 
        //actions and never synchronize on the internal action.
        //This corresponds to the default behavior of PRISM.

        //TODO: add support for other synchronization methods supported by PRISM.

        List<SynchronisationVectorSync> syncs = syncVectors.getSyncs();
        List<SynchronisationVectorElement> elements = syncVectors.getElements();

        for (SynchronisationVectorElement element : elements) {
            ensure(element.getInputEnableOrEmpty().isEmpty(), 
                    ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_AUTOMATON_INPUT_ENABLED, 
                    element.getAutomaton().getName());
        }

        for (SynchronisationVectorSync sync : syncs) {
            Action result = sync.getResult();
            List<Action> actions = sync.getSynchronise();
            if (JANIComponentRegistrar.isSilentAction(result)) {
                int counter = 0;
                for (Action synched : actions) {
                    if (synched != null) {
                        counter++;
                        ensure(JANIComponentRegistrar.isSilentAction(synched), 
                                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_ACTION_HIDING,
                                synched.getName());
                    }
                }
                ensure(counter == 1, 
                        ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_SYNCHRONIZATION_ON_HIDDEN_ACTION);
            } else {
                int lenght = actions.size();
                for (int index = 0; index < lenght; index++) {
                    Action synched = actions.get(index);
                    Automaton automaton = elements.get(index).getAutomaton();
                    if (synched != null) {
                        ensure(synched.equals(result), 
                                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_ACTION_RENAMING,
                                synched.getName(), 
                                result.getName());
                        boolean canSynchronize = false;
                        for (Edge edge : automaton.getEdges()) {
                            canSynchronize |= result.equals(edge.getActionOrSilent());
                        }
                        ensure(canSynchronize,
                                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_ACTION_NOT_SYNCHRONIZED,
                                automaton.getName(), 
                                synched.getName());
                    } else {
                        boolean cannotSynchronize = true;
                        for (Edge edge : automaton.getEdges()) {
                            cannotSynchronize &= !result.equals(edge.getActionOrSilent());
                        }
                        ensure(cannotSynchronize,
                                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_ACTION_NOT_SYNCHRONIZED,
                                automaton.getName(), 
                                result.getName());
                    }
                }
            }
        }


        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert syncVectors != null;
    }

    @Override
    public boolean usesTransientVariables() {
        assert syncVectors != null;

        return false;
    }
}
