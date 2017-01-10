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

package epmc.automaton;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;

public interface AutomatonExporter {
    enum Format {
        DOT
    }
    
    void setAutomaton(Automaton automaton);
    
    void setOutput(OutputStream out);
    
    void setFormat(Format format);
    
    void export() throws EPMCException;
    
    default String exportToString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        setOutput(out);
        try {
            export();
        } catch (EPMCException e) {
            return e.toString();
        }
        return out.toString();
    }
    
    default void print() throws EPMCException {
        setOutput(System.out);
        export();
    }
}
