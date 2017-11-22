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

package epmc.prism.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;

public final class ModuleRename implements Module {
    private final String name;
    private final String base;
    private final Map<Expression,Expression> map;
    private Positional positional;

    public ModuleRename(Expression name, Expression base, Map<Expression,Expression> map, Positional positional) {
        assert name != null;
        assert base != null;
        assert map != null;
        this.positional = positional;
        for (Entry<Expression, Expression> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.map = new HashMap<>();
        ExpressionIdentifierStandard nameI = (ExpressionIdentifierStandard) name;
        ExpressionIdentifierStandard baseI = (ExpressionIdentifierStandard) base;
        this.name = nameI.getName();
        this.base = baseI.getName();
        this.map.putAll(map);
    }

    @Override
    public String getName() {
        return name;
    }

    String getBase() {
        return base;
    }

    Map<Expression,Expression> getMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
