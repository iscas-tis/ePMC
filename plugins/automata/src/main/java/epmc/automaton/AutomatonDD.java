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

package epmc.automaton;

import java.io.Closeable;
import java.util.List;

import epmc.dd.DD;

// TODO probably should also extend GraphDD

public interface AutomatonDD extends Closeable {
    DD getInitial();

    DD getTransitions();

    List<DD> getPresVars();

    List<DD> getNextVars();

    List<DD> getLabelVars();

    DD getPresCube();

    DD getNextCube();

    DD getLabelCube();

    @Override
    public void close();
}
