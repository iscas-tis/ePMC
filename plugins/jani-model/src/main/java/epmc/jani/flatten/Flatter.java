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

package epmc.jani.flatten;

import epmc.jani.model.Automata;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;

public final class Flatter {
    private ModelJANI model;

    public void setModel(ModelJANI model) {
        this.model = model;
    }

    public void flatten() {
        Component origSystem = model.getSystem();
        ComponentAutomaton newSystem = flatten(origSystem);
        Automata newAutomata = new Automata();
        newAutomata.setModel(model);
        newAutomata.addAutomaton(newSystem.getAutomaton());
        model.setAutomata(newAutomata);
        model.setSystem(newSystem);
    }

    private ComponentAutomaton flatten(Component origSystem) {
        // TODO Auto-generated method stub
        return null;
    }
}
