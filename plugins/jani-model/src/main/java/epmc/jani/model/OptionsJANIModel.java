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

/**
 * Class collecting options used for parser part of JANI model plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsJANIModel {
    /** Base name of resource file for options description. */
    OPTIONS_JANI_MODEL,
    /** Category of JANI model options. */
    JANI_MODEL_CATEGORY,
    /** Option whether deadlocks are allowed and will be fixed automatically. */
    JANI_FIX_DEADLOCKS,
    /** Number of action encoding bits to reserve. */
    JANI_ACTION_BITS,
    /** Storage point for JANI model extensions. */
    JANI_MODEL_EXTENSION_CLASS,
    /** JANI model extension semantics. */
    JANI_MODEL_EXTENSION_SEMANTICS,
}
