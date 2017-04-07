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

package epmc.prism.exporter.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in PRISM converter plugin.
 * 
 * @author Andrea Turrini
 */
public final class ProblemsPRISMExporter {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String ERROR_PRISM_EXPORTER = "ErrorPRISMExporter";
    /** Missing PRISM file name. */
    public final static Problem PRISM_EXPORTER_MISSING_INPUT_MODEL_FILENAMES = newProblem("prism-exporter-missing-input-model-filenames");
    /** Unaccessible PRISM file name. */
    public final static Problem PRISM_EXPORTER_UNACCESSIBLE_FILENAME = newProblem("prism-exporter-unaccessible-filename");
    /** The input model can not exported as PRISM model. */ 
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE = newProblem("prism-exporter-unsupported-feature");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNKNOWN_OPERATOR = newProblem("prism-exporter-unsupported-feature-unknown-operator");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_FOR_NORMAL_VARIABLE = newProblem("prism-exporter-unsupported-feature-transient-variable-for-normal-variable");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_MULTIPLE_LOCATIONS = newProblem("prism-exporter-unsupported-feature-multiple-locations");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_AUTOMATON_INPUT_ENABLED = newProblem("prism-exporter-unsupported-feature-automaton-input-enabled");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_UNSUPPORTED_MODEL = newProblem("prism-exporter-unsupported-feature-unsupported-model");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_VARIABLE_ASSIGNED_MULTIPLE_AUTOMATA = newProblem("prism-exporter-unsupported-feature-variable-defined-assigned-multiple-automata");
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_DIFFERENT_EXPRESSIONS = newProblem("prism-exporter-unsupported-feature-transient-variable-different-expression");
    /** Errors in the input model*/
    public final static Problem PRISM_EXPORTER_ERROR_UNDEFINED_USED_ACTION = newProblem("prism-exporter-error-undefined-used-action");
    public final static Problem PRISM_EXPORTER_ERROR_UNDEFINED_USED_CONSTANT = newProblem("prism-exporter-error-undefined-used-constant");
    public final static Problem PRISM_EXPORTER_ERROR_UNDEFINED_USED_VARIABLE = newProblem("prism-exporter-error-undefined-used-variable");
    public final static Problem PRISM_EXPORTER_ERROR_CONSTANT_DEFINED_TWICE = newProblem("prism-exporter-error-constant-defined-twice");
    public final static Problem PRISM_EXPORTER_ERROR_VARIABLE_DEFINED_TWICE = newProblem("prism-exporter-error-variable-defined-twice");
    public final static Problem PRISM_EXPORTER_ERROR_UNKNOWN_PROCESSOR = newProblem("prism-exporter-error-unknown-processor");
    public final static Problem PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED = newProblem("prism-exporter-error-extended-syntax-required");
    
   
    
	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(ERROR_PRISM_EXPORTER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsPRISMExporter() {
    }
}
