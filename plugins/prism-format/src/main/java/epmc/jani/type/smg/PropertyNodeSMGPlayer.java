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

import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.PropertyNode;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class PropertyNodeSMGPlayer implements PropertyNode {
    private final static String PLAYER = "PLAYER";

    private final ExplorerExtensionSMG extension;
    private final int player;
    private final TypeBoolean type;
    private final ValueBoolean value;

    public PropertyNodeSMGPlayer(ExplorerJANI explorer, ExplorerExtensionSMG extension, int player) {
        assert explorer != null;
        assert extension != null;
        assert player >= 0;
        this.extension = extension;
        this.player = player;
        this.type = TypeBoolean.get();
        this.value = type.newValue();
    }

    @Override
    public Value get() {
        value.set(extension.getNodePlayer() == player);
        return value;
    }

    @Override
    public boolean getBoolean() {
        return extension.getNodePlayer() == player;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(PLAYER);
        builder.append(player + 1);
        return builder.toString();
    }
}
