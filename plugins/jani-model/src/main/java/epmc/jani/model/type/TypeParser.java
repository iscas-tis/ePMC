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

package epmc.jani.model.type;

import java.util.Map;

import javax.json.JsonValue;

import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.Util;

public final class TypeParser implements JANINode {
    private JANIType type;
    private ModelJANI model;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        Map<String,Class<? extends JANIType>> types = model.getTypes();
        for (Class<? extends JANIType> clazz : types.values()) {
            JANIType tryType = Util.getInstance(clazz);
            tryType.setModel(model);
            tryType = tryType.parseAsJANIType(value);
            if (tryType != null) {
                type = tryType;
                break;
            }
        }
        assert type != null : value; // TODO exception
        return type;
    }

    @Override
    public JsonValue generate() {
        assert false;
        return null;
    }

    public JANIType getType() {
        return type;
    }	
}
