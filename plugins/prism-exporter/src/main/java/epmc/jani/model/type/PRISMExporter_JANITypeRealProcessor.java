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

import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;

public final class PRISMExporter_JANITypeRealProcessor implements PRISMExporter_ProcessorStrict {

    private JANITypeReal real = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj instanceof JANITypeReal;

        real = (JANITypeReal) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert real != null;

        return "double";
    }

    @Override
    public void validateTransientVariables() {
        assert real != null;
    }

    @Override
    public boolean usesTransientVariables() {
        assert real != null;

        return false;
    }	
}
