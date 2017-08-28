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

package epmc.jani.dd;

import java.io.Closeable;

import epmc.dd.DD;
import epmc.dd.VariableDD;

final class VariableValid implements Cloneable, Closeable {
    private boolean closed;
    private VariableDD variable;
    private DD valid;

    void setVariable(VariableDD variable) {
        this.variable = variable;
    }

    VariableDD getVariable() {
        return variable;
    }

    void setValid(DD valid) {
        this.valid = valid;
    }

    DD getValid() {
        return valid;
    }

    @Override
    protected VariableValid clone() {
        VariableValid clone = new VariableValid();
        clone.variable = variable;
        clone.valid = valid.clone();
        return clone;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        valid.dispose();
    }
}
