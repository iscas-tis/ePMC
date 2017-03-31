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
    public final static Problem PRISM_EXPORTER_UNSUPPORTED_INPUT_FEATURE = newProblem("prism-exporter-unsupported-input-feature");
    
    
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
