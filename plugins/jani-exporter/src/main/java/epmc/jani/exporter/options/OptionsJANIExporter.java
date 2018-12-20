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

package epmc.jani.exporter.options;

/**
 * Class collecting options used for JANI converter plugin.
 * 
 * @author Andrea Turrini
 */
public enum OptionsJANIExporter {
    /** Base name of resource file for options description. */
    OPTIONS_JANI_EXPORTER,
    /** Category used for JANI converter options. */
    JANI_EXPORTER_CATEGORY,
    /** Name of the generated JANI file. */
    JANI_EXPORTER_JANI_FILE_NAME,
    /** Name of the generated JANI model. */
    JANI_EXPORTER_JANI_MODEL_NAME,
    /** Whether to overwrite an existing output JANI file. */
    JANI_EXPORTER_OVERWRITE_JANI_FILE,
    /** Whether to print out messages during the export. */
    JANI_EXPORTER_PRINT_MESSAGES,
    /** Prefix for reward structure names. */
    JANI_EXPORTER_REWARD_NAME_PREFIX,
    /** Whether to use the new exporter. */
    JANI_EXPORTER_USE_NEW_EXPORTER,
    /** Whether to use derived operators instead of only native JANI operators. */ 
    JANI_EXPORTER_USE_DERIVED_OPERATORS,
    /** Whether to generate the synchronisation information for the silent action. */
    JANI_EXPORTER_SYNCHRONISE_SILENT
    ;
}
