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

package epmc.main.options;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Options of main part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsEPMC {
    /** base name of resource bundle */
    OPTIONS_EPMC,
    /** {@link Locale} used in these options */
    LOCALE,
    /** {@link String} {@link List} of model input files */
    MODEL_INPUT_FILES,
    /** {@link String} {@link List} of property input files */
    PROPERTY_INPUT_FILES,
    /** port number ({@link Integer}) to use for analysis */
    PORT,
    /** RMI name used for server started */
    SERVER_NAME,
    /** whether to print stack trace if user exception thrown */
    PRINT_STACKTRACE,
    /** run mode of EPMC {@link OptionsTypesEPMC.RunMode}) */
    RUN_MODE,
    /** {@link Map} from command {@link String} to available command {@link Class} */
    COMMAND_CLASS,    
}
