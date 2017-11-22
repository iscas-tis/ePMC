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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemAsyncParallel implements SystemDefinition {
    private ModelPRISM model;
    private final Positional positional;
    private List<SystemDefinition> children = new ArrayList<>();

    public SystemAsyncParallel(SystemDefinition left, SystemDefinition right, Positional positional) {
        this.positional = positional;
        assert left != null;
        assert right != null;
        children.add(left);
        children.add(right);
    }

    public SystemDefinition getLeft() {
        return children.get(0);
    }

    public SystemDefinition getRight() {
        return children.get(1);
    }

    @Override
    public String toString() {
        return "(" + children.get(0) + "|||" + children.get(1) + ")";
    }

    @Override
    public Set<Expression> getAlphabet() {
        Set<Expression> result = new HashSet<>();
        result.addAll(getLeft().getAlphabet());
        result.addAll(getRight().getAlphabet());
        return Collections.unmodifiableSet(result);
    }

    @Override
    public List<SystemDefinition> getChildren() {
        return children;
    }

    @Override
    public void setModel(ModelPRISM model) {
        assert model != null;
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
