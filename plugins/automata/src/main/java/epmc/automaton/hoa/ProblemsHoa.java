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

package epmc.automaton.hoa;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsHoa {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_HOA = "ProblemsHoa";

    public final static Problem HOA_PARSING_ERROR = newProblem("hoa-parsing-error");
    public final static Problem HOA_INVALID_ACC_SET = newProblem("hoa-invalid-acc-set");
    public final static Problem HOA_INVALID_FROM_STATE = newProblem("hoa-invalid-from-state");
    public final static Problem HOA_INVALID_TO_STATE = newProblem("hoa-invalid-to-state");
    public final static Problem HOA_TOO_MANY_APS = newProblem("hoa-too-many-aps");
    public final static Problem HOA_INVALID_START_STATE = newProblem("hoa-invalid-start-state");
    public final static Problem HOA_MULTIPLE_ITEMS_STATES = newProblem("hoa-multiple-items-states");
    public final static Problem HOA_MULTIPLE_ITEMS_AP = newProblem("hoa-multiple-items-ap");
    public final static Problem HOA_MULTIPLE_ITEMS_ACCEPTANCE = newProblem("hoa-multiple-items-acceptance");
    public final static Problem HOA_MULTIPLE_ITEMS_ACC_NAME = newProblem("hoa-multiple-items-acc-name");
    public final static Problem HOA_MULTIPLE_ITEMS_TOOL = newProblem("hoa-multiple-items-tool");
    public final static Problem HOA_MULTIPLE_ITEMS_NAME = newProblem("hoa-multiple-items-name");
    public final static Problem HOA_MULTIPLE_ITEMS_PROPERTIES = newProblem("hoa-multiple-items-properties");
    public final static Problem HOA_MISSING_ITEM_ACCEPTANCE = newProblem("hoa-missing-item-acceptance");
    public final static Problem HOA_INVALID_AP_NUMBER = newProblem("hoa-invalid-ap-number");
    public final static Problem HOA_NEGATIVE_AP_NUMBER = newProblem("hoa-negative-ap-number");
    public final static Problem HOA_INVALID_ACC_NUMBER = newProblem("hoa-invalid-acc-number");
    public final static Problem HOA_NEGATIVE_ACC_NUMBER = newProblem("hoa-negative-acc-number");
    public final static Problem HOA_NEGATIVE_FROM_STATE_NUMBER = newProblem("hoa-negative-from-state-number");
    public final static Problem HOA_NEGATIVE_TO_STATE_NUMBER = newProblem("hoa-negative-to-state-number");
    public final static Problem HOA_ANAME_SPECIFIED_TWICE = newProblem("hoa-aname-specified-twice");
    public final static Problem HOA_ANAME_UNKNOWN = newProblem("hoa-aname-unknown");
    public final static Problem HOA_INCONSISTENT_ACCEPTANCE_NAME = newProblem("hoa-inconsistent-acceptance-name");
    public final static Problem HOA_BOTH_STATE_AND_EDGE_LABEL = newProblem("hoa-both-state-and-edge-label");
    public final static Problem HOA_LABELS_EITHER_IMPLICIT_OR_EXPLICIT = newProblem("hoa-labels-either-implicit-or-explicit");
    public final static Problem HOA_TRANSITION_NUMBER_INCORRECT_IMPLICIT_LABELS = newProblem("hoa-transition-number-incorrect-implicit-labels");
    public final static Problem HOA_NUM_STATES_DECLARED_STATE_MISSING = newProblem("hoa-num-states-declared-state-missing");
    public final static Problem HOA_NUM_STATES_UNDECLARED_STATE_GAP = newProblem("hoa-num-states-undeclared-state-gap");
    public final static Problem HOA_NUM_STATES_UNDECLARED_INVALID_TO = newProblem("hoa-num-states-undeclared-invalid-to");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_HOA, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsHoa() {
    }
}
