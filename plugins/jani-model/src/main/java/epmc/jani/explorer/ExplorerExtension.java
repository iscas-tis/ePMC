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

package epmc.jani.explorer;

import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.model.ModelExtension;
import epmc.value.Value;

public interface ExplorerExtension {
    String getIdentifier();
    
    default void setExplorer(ExplorerJANI explorer) {
    }

    default void afterSystemCreation() {
    }
    
    default boolean isUsedGetNodeProperty() {
        return false;
    }

    default Value getGraphProperty(Object property) {
        return null;
    }

    default ExplorerNodeProperty getNodeProperty(Object property) {
        return null;
    }

    default ExplorerEdgeProperty getEdgeProperty(Object property) {
        return null;
    }

    default void handleNoSuccessors(NodeJANI node) {
    }

    default void handleSelfLoop(NodeJANI node) {
    }

    default void beforeQuerySystem(NodeJANI nodeJANI) {
    }

    default void afterQuerySystem(NodeJANI node) {
    }

    default void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
    }

    default void setModelExtension(ModelExtension modelExtension) {
    }
}
