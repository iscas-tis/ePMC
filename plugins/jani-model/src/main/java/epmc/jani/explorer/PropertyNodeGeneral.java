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

import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

/**
 * Explorer node property for JANI explorers and their components.
 * Note that this class stores values only temporarily for a given call of
 * {@link Explorer#queryNode(epmc.graph.ExplorerNode)}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertyNodeGeneral implements PropertyNode {
    /** Type of value stored. */
    private final Type type;
    /** Value of this property for the node queried last. */
    private final Value value;

    /**
     * Construct new node property.
     * None of the parameters may be {@code null}.
     * 
     * @param explorer explorer to which the property shall belong to
     * @param type type of the property
     */
    public PropertyNodeGeneral(Explorer explorer, Type type) {
        assert explorer != null;
        assert type != null;
        this.type = type;
        this.value = type.newValue();
    }

    /**
     * Set value of node property.
     * The parameter may not be {@code null}.
     * 
     * @param value value to set
     */
    public void set(Value value) {
        assert value != null;
        this.value.set(value);
    }

    /**
     * Set boolean value of node property.
     * The property type must be boolean for this function to be allowed to be
     * called.
     * 
     * @param value boolean value to set for this property
     */
    public void set(boolean value) {
        assert TypeBoolean.isBoolean(type);
        ValueBoolean.asBoolean(this.value).set(value);
    }

    public void set(Object object) {
        assert object != null;
        assert TypeObject.isObject(type) : type;
        ValueObject.asObject(value).set(object);
    }

    public void set(Enum<?> value) {
        assert value != null;
        assert TypeEnum.isEnum(type);
        ValueEnum.asEnum(this.value).set(value);
    }

    @Override
    public Value get() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }
}
