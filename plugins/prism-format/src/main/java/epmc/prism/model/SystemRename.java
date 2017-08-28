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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemRename implements SystemDefinition {
    private ModelPRISM model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();

    private final Map<Expression,Expression> renaming;

    public SystemRename(SystemDefinition inner, Map<Expression,Expression> renaming, Positional positional) {
        this.positional = positional;
        assert inner != null;
        assert renaming != null;
        for (Entry<Expression, Expression> entry : renaming.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        if (!renaming.isEmpty()) {
            this.renaming = new HashMap<>();
            children.add(inner);
            this.renaming.putAll(renaming);
        } else {
            this.renaming = Collections.emptyMap();
        }
    }

    public SystemDefinition getInner() {
        return children.get(0);
    }

    public Map<Expression,Expression> getRenaming() {
        return Collections.unmodifiableMap(renaming);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(" + children.get(0) + "/{");
        int renameNr = 0;
        for (Entry<Expression,Expression> entry : renaming.entrySet()) {
            builder.append(entry.getKey() + "<-" + entry.getValue());
            if (renameNr < renaming.size() - 1) {
                builder.append(",");
            }
            renameNr++;
        }
        builder.append("})");
        return builder.toString();
    }

    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        for (Expression expression : getInner().getAlphabet()) {
            if (renaming.containsKey(expression)) {
                result.add(renaming.get(expression));
            } else {
                result.add(expression);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public List<SystemDefinition> getChildren() {
        return children;
    }

    @Override
    public void setModel(ModelPRISM model) {
        this.model = model;
        for (SystemDefinition system : getChildren()) {
            system.setModel(model);
        }
    }

    @Override
    public ModelPRISM getModel() {
        return model;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
