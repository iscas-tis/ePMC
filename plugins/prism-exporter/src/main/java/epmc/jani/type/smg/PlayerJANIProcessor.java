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

package epmc.jani.type.smg;

import java.util.LinkedList;
import java.util.List;

import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelJANIProcessor;
import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PlayerJANIProcessor implements JANI2PRISMProcessorExtended {

    private PlayerJANI player = null;

    @Override
    public JANI2PRISMProcessorExtended setElement(Object obj) {
        assert obj != null;
        assert obj instanceof PlayerJANI; 

        player = (PlayerJANI) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert player != null;

        StringBuilder prism = new StringBuilder();
        boolean remaining = false;

        prism.append("player ")
            .append(player.getName());

        for (Automaton automaton: player.getAutomataOrEmpty()) {
            if (remaining) {
                prism.append(", ");
            } else {
                remaining = true;
            }
            prism.append("\n")
                .append(ModelJANIProcessor.INDENT)
                .append(automaton.getName());
        }
        for (Action action: player.getActionsOrEmpty()) {
            if (remaining) {
                prism.append(", ");
            } else {
                remaining = true;
            }
            prism.append("\n")
                .append(ModelJANIProcessor.INDENT)
                .append("[")
                .append(JANIComponentRegistrar.getActionName(action))
                .append("]");
        }

        prism.append("\nendplayer\n");

        return prism.toString();
    }


    @Override
    public List<String> getUnsupportedFeature() {
        assert player != null;

        List<String> ll = new LinkedList<>();
        ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_PLAYER_DEFINITION);
        return ll;
    }

    @Override
    public void validateTransientVariables() {
        assert player != null;

        for (Action action: player.getActionsOrEmpty()) {
            ProcessorRegistrar.getProcessor(action).validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert player != null;

        boolean usesTransient = false;

        for (Action action: player.getActionsOrEmpty()) {
            usesTransient |= ProcessorRegistrar.getProcessor(action)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
