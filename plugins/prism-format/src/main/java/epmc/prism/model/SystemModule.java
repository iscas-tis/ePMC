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
import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class SystemModule implements SystemDefinition {
    private final static String QUOT = "\"";
    private final static String SPACE = " ";

    private ModelPRISM model;
    private final Positional positional;
    private final List<SystemDefinition> children = new ArrayList<>();
    private final String module;
    private final String instanceName;

    SystemModule(String module, String instanceName, Positional positional) {
        assert module != null;
        this.positional = positional;
        this.module = module;
        this.instanceName = instanceName;
    }

    public SystemModule(String module, Positional positional) {
        this(module, null, positional);
    }

    public String getModule() {
        return module;
    }

    @Override
    public String toString() {
        return module;
    }

    @Override
    public Set<Expression> getAlphabet() {
        assert model != null;
        for (Module module : getModel().getModules()) {
            if (module.getName().equals(getModule())) {
                return module.getAlphabet();
            }
        }
        assert false : QUOT + module + QUOT + SPACE + SPACE + QUOT + getModel().getModules() + QUOT;
        return null;
    }

    String getInstanceName() {
        return instanceName;
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
        return this.model;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
