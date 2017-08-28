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

import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.value.TypeBoolean;
import epmc.value.ValueBoolean;

/**
 * Node property stating whether a certain node is a deadlock node.
 * 
 * @author Ernst Moritz Hahn
 */
final class PropertyNodeDeadlock implements ExplorerNodeProperty {
    /** Explorer to which this property belongs. */
    private final ExplorerJANI explorer;
    /** Used to return the value of the property. */
    private final ValueBoolean value;

    PropertyNodeDeadlock(ExplorerJANI explorer) {
        assert explorer != null;
        this.explorer = explorer;
        TypeBoolean type = TypeBoolean.get();
        value = type.newValue();
    }

    @Override
    public ValueBoolean get() {
        value.set(explorer.isDeadlock());
        return value;
    }

    @Override
    public boolean getBoolean() {
        return explorer.isDeadlock();
    }

    @Override
    public TypeBoolean getType() {
        return TypeBoolean.get();
    }
}
