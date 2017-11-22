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

import java.util.List;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;

public interface SystemDefinition {
    List<SystemDefinition> getChildren();

    Set<Expression> getAlphabet();

    void setModel(ModelPRISM model);

    ModelPRISM getModel();

    Positional getPositional();

    default boolean isAlphaParallel() {
        return this instanceof SystemAlphaParallel;
    }

    default boolean isAsyncParallel() {
        return this instanceof SystemAsyncParallel;
    }

    default boolean isHide() {
        return this instanceof SystemHide;
    }

    default boolean isModule() {
        return this instanceof SystemModule;
    }

    default boolean isRename() {
        return this instanceof SystemRename;
    }

    default boolean isRestrictedParallel() {
        return this instanceof SystemRestrictedParallel;
    }

    default SystemAlphaParallel asAlphaParallel() {
        assert isAlphaParallel();
        return (SystemAlphaParallel) this;
    }

    default SystemAsyncParallel asAsyncParallel() {
        assert isAsyncParallel();
        return (SystemAsyncParallel) this;
    }

    default SystemHide asHide() {
        assert isHide();
        return (SystemHide) this;
    }

    default SystemModule asModule() {
        assert isModule();
        return (SystemModule) this;
    }

    default SystemRename asRename() {
        assert isRename();
        return (SystemRename) this;
    }

    default SystemRestrictedParallel asRestrictedParallel() {
        assert isRestrictedParallel();
        return (SystemRestrictedParallel) this;
    }
}
