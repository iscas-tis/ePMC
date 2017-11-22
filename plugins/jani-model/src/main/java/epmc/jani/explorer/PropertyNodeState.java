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

import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class PropertyNodeState implements PropertyNode {
    /** The explorer to which this property belongs. */
    private final ExplorerJANI explorer;
    /** Type of value stored. */
    private final TypeBoolean type;
    /** Value of this property for the node queried last. */
    private final ValueBoolean value;

    /**
     * Construct new node property.
     * None of the parameters may be {@code null}.
     * 
     * @param explorer explorer to which the property shall belong to
     * @param type type of the property
     */
    public PropertyNodeState(ExplorerJANI explorer) {
        assert explorer != null;
        this.explorer = explorer;
        this.type = TypeBoolean.get();
        this.value = type.newValue();
    }

    @Override
    public Value get() {
        value.set(explorer.isState());
        return value;
    }

    @Override
    public boolean getBoolean() {
        return explorer.isState();
    }

    @Override
    public Type getType() {
        return type;
    }
}
