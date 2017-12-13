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

package epmc.modelchecker.options;

import java.util.Map;

import epmc.modelchecker.Engine;

/**
 * Options of model checker part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsModelChecker {
    /** Base name of resource bundle. */
    OPTIONS_MODEL_CHECKER,

    /** {@link Engine engine} used for analysis */
    ENGINE,
    /** List of property solvers to be used. */
    PROPERTY_SOLVER,
    /** List of all property solver classes. */
    PROPERTY_SOLVER_CLASS,
    LOW_LEVEL_ENGINE_CLASS,
    SCHEDULER_PRINTER_CLASS,
    /** constant definitions from command line ({@link Map Map&ltString,Object&gt;}) */
    CONST,
    // TODO following needs fixing
    PROPERTY_INPUT_TYPE,
    PROPERTY_CLASS,

    /** Model input type {@link String}. */
    MODEL_INPUT_TYPE,

    COMPUTE_SCHEDULER,
}
