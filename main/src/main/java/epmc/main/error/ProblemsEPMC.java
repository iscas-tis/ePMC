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

package epmc.main.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in main part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsEPMC {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_EPMC = "ProblemsEPMC";
    /** File to read properties from does not exist. */
    public static final Problem FILE_NOT_EXISTS = newProblem("file-not-exists");
    /** Could not create a certain result output file. */
    public static final Problem NOT_CREATE_RESULT_OUTPUT = newProblem("not-create-result-output");
    /** Error writing result output. */
    public static final Problem ERROR_WRITING_RESULT_OUTPUT = newProblem("error-writing-result-output");    
    
    /**
     * Generate new problem reading descriptions from EPMC property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_EPMC, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsEPMC() {
    }
}
