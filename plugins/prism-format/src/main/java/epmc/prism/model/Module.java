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

import java.util.Map;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.type.JANIType;

public interface Module {
    Positional getPositional();

    default Map<Expression,JANIType> getVariables() {
        assert false;
        return null;
    }

    default Map<Expression, Expression> getInitValues() {
        assert false;
        return null;
    }

    default Set<Expression> getAlphabet() {
        assert false;
        return null;
    }

    default String getName() {
        assert false;
        return null;
    }

    default Module replaceFormulas(Map<Expression, Expression> specifiedConsts) {
        assert false;
        return null;
    }

    default boolean isCommands() {
        return this instanceof ModuleCommands;
    }

    default ModuleCommands asCommands() {
        return (ModuleCommands) this;
    }    
}
