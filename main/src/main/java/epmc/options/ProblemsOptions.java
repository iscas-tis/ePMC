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

package epmc.options;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Options which can occur in the context of program options.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsOptions {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_OPTIONS = "ProblemsOptions";

    /** Given command is not a valid command of the options set. */
    public final static Problem OPTIONS_COMMAND_NOT_VALID = newProblem("options-command-not-valid");
    /** Given option is not a valid option of the options set. */
    public final static Problem OPTIONS_PROGRAM_OPTION_NOT_VALID = newProblem("options-program-option-not-valid");
    /** No value was provided for the given option. */
    public final static Problem OPTIONS_NO_VALUE_FOR_OPTION = newProblem("options-no-value-for-option");
    /** The value provided for the option is invalid for the option type. */
    public final static Problem OPTIONS_INV_PRG_OPT_VALUE = newProblem("options-inv-prg-opt-value");
    /** An option value was set multiple times. */
    public final static Problem OPTIONS_OPT_CMD_LINE_SET_MULTIPLE = newProblem("options-opt-cmd-line-set-multiple");
    /** An option value was given without a preceding option. */
    public final static Problem OPTIONS_NO_OPTION_FOR_VALUE = newProblem("options-no-option-for-value");
    /** The given option could not be parsed. */
    public final static Problem OPTIONS_PARSE_OPTION_FAILED = newProblem("options-parse-option-failed");
    // TODO document
    public final static Problem OPTIONS_OPTION_NOT_SET = newProblem("options-option-not-set");
    // TODO move out of main part
    /** Value parsed by interval option type outside its interval. */
    public final static Problem OPTIONS_VALUE_OUTSIDE_INTERVAL = newProblem("options-value-outside-interval");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter may not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_OPTIONS, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsOptions() {
    }
}
