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

package epmc.jani.model;

import epmc.modelchecker.Model;

// TODO documentation
// TODO use this for PRISM, QMC, and RDDL models
/**
 * Model which can be converted to a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ModelJANIConverter extends Model {
    /**
     * Convert model to equivalent JANI representation.
     * An exception might be thrown in case the concrete model instance uses
     * features which cannot be converted to a JANI model (at present).
     * 
     * @param forExporting when set to true, the method transforms the JANI structures so to agree with the JANI-specification
     * @return JANI representation of model
     */
    ModelJANI toJANI(boolean forExporting);
}
