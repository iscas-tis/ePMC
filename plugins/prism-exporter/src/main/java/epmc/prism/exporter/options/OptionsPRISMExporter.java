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

package epmc.prism.exporter.options;

/**
 * Class collecting options used for PRISM converter plugin.
 * 
 * @author Andrea Turrini
 */
public enum OptionsPRISMExporter {
    /** Base name of resource file for options description. */
    OPTIONS_PRISM_EXPORTER,
    /** Category used for PRISM converter options. */
    PRISM_EXPORTER_CATEGORY,
    /** Name of the generated PRISM model file. */
    PRISM_EXPORTER_PRISM_MODEL_NAME,
    /** Name of the generated PRISM model file. */
    PRISM_EXPORTER_PRISM_MODEL_FILE_NAME,
    /** Name of the generated PRISM properties file. */
    PRISM_EXPORTER_PRISM_PROPERTIES_FILE_NAME,
    /** Whether to use the extended PRISM syntax. */
    PRISM_EXPORTER_EXTENDED_PRISM,
    /** Whether to use the non-official PRISM syntax. */
    PRISM_EXPORTER_NON_OFFICIAL_PRISM,
    /** Whether to allow for multiple locations. */
    PRISM_EXPORTER_ALLOW_MULTIPLE_LOCATIONS
    ;
}
