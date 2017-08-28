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

package epmc.prism.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsPRISM {
    private final static String ERROR_PRISM = "ErrorPRISM";
    public final static Problem WEIGHT_BOOLEAN = newProblem("weight-boolean");
    public final static Problem UNKNOWN_IN_EXPR = newProblem("unknown-in-expr");
    public final static Problem INCOMPATIBLE_ASSIGNMENT = newProblem("incompatible-assignment");
    public final static Problem GUARD_NOT_BOOLEAN = newProblem("guard-not-boolean");
    public final static Problem OUTSIDE_RANGE = newProblem("outside-range");
    public final static Problem INIT_STATE_SET_EMPTY = newProblem("init-state-set-empty");
    public final static Problem CONST_CYCLIC = newProblem("const-cyclic");
    public final static Problem CONST_NON_CONST = newProblem("const-non-const");
    public final static Problem CONST_UNDEFINED = newProblem("const-undefined");
    public final static Problem FLATTEN_NEEDED_DD = newProblem("flatten-needed-dd");
    public final static Problem CONST_ALREADY_IN_MODEL = newProblem("const-already-in-model");
    public final static Problem SYNC_WRITE_GLOBAL = newProblem("sync-write-global");
    public final static Problem NOT_BOTH_INITS = newProblem("not-both-inits");
    public final static Problem MODULE_NOT_IN_SYSTEM = newProblem("module-not-in-system");
    public final static Problem INVALID_ACTION_IN_SYSTEM = newProblem("invalid-action-in-system");
    public final static Problem INVALID_MODULE_IN_SYSTEM = newProblem("invalid-module-in-system");
    public final static Problem MODULE_ALREADY_IN_SYSTEM = newProblem("module-already-in-system");
    public final static Problem INIT_NOT_BOOLEAN = newProblem("init-not-boolean");
    public final static Problem VAR_INIT_INCONSISTENT = newProblem("var-init-inconsistent");
    public final static Problem CONST_TYPE_INCONSISTENT = newProblem("const-type-inconsistent");
    public final static Problem BASE_NOT_FOUND = newProblem("base-not-found");
    public final static Problem NO_MODEL_READ = newProblem("no-model-read");
    public final static Problem IDENTIFIER_UNDECLARED = newProblem("identifier-undeclared");
    public final static Problem NON_DET_QUANT_REQ_DIR = newProblem("non-det-quant-req-dir");
    public final static Problem RANGE_BOUND_LOWER_NOT_INTEGER = newProblem("range-bound-lower-not-integer");
    public final static Problem RANGE_BOUND_UPPER_NOT_INTEGER = newProblem("range-bound-upper-not-integer");
    public final static Problem VAR_NOT_RENAMED = newProblem("var-not-renamed");
    public final static Problem PRISM_PARSER_GENERAL_ERROR = newProblem("prism-parser-general-error");
    public final static Problem PRISM_PARSER_INTERNAL_ERROR = newProblem("prism-parser-internal-error");
    public final static Problem PRISM_PARSER_UNEXPECTED_CHARACTER = newProblem("prism-parser-unexpected-character");
    public final static Problem PRISM_PARSER_NO_MODULE = newProblem("prism-parser-no-module");
    public final static Problem PRISM_PARSER_MULTIPLE_INIT = newProblem("prism-parser-multiple-init");
    public final static Problem PRISM_PARSER_MULTIPLE_TYPE = newProblem("prism-parser-multiple-type");
    public final static Problem PRISM_PARSER_MULTIPLE_SYSTEM = newProblem("prism-parser-multiple-system");
    public final static Problem PRISM_PARSER_REPEATED_FORMULA = newProblem("prism-parser-repeated-formula");
    public final static Problem PRISM_PARSER_REPEATED_LABEL = newProblem("prism-parser-repeated-label");
    public final static Problem PRISM_PARSER_REPEATED_CONSTANT = newProblem("prism-parser-repeated-constant");
    public final static Problem PRISM_PARSER_REPEATED_MODULE = newProblem("prism-parser-repeated-module");
    public final static Problem PRISM_PARSER_REPEATED_PLAYER = newProblem("prism-parser-repeated-player");
    public final static Problem PRISM_PARSER_REPEATED_VARIABLE = newProblem("prism-parser-repeated-variable");
    public final static Problem PRISM_PARSER_REPEATED_REWARD = newProblem("prism-parser-repeated-reward");
    public final static Problem PRISM_PARSER_REPEATED_UPDATE = newProblem("prism-parser-repeated-update");
    public final static Problem PRISM_PARSER_SYNTAX_ERROR = newProblem("prism-parser-syntax-error");
    public final static Problem PRISM_PARSER_UNSUPPORTED_MODEL_TYPE = newProblem("prism-parser-unsupported-model-type");
    public final static Problem EXPRESSION_PARSER_GENERAL_ERROR = newProblem("expression-parser-general-error");
    public final static Problem EXPRESSION_PARSER_INTERNAL_ERROR = newProblem("expression-parser-internal-error");
    public final static Problem EXPRESSION_PARSER_UNEXPECTED_CHARACTER = newProblem("expression-parser-unexpected-character");
    public final static Problem EXPRESSION_PARSER_MATRIX_ROW_DIFFERENT_SIZE = newProblem("expression-parser-matrix-row-different-size");
    public final static Problem EXPRESSION_PARSER_UNEXPECTED_LOGIC_OPERATOR = newProblem("expression-parser-unexpected-logic-operator");
    public final static Problem EXPRESSION_PARSER_UNKNOWN_FILTER_TYPE = newProblem("expression-parser-unknown-filter-type");
    public final static Problem EXPRESSION_PARSER_SYNTAX_ERROR = newProblem("expression-parser-syntax-error");
    public final static Problem PARSE_ERROR_MULTI_UNTIL = newProblem("parse-error-multi-until");
    public static final Problem PRISM_ONE_MODEL_FILE = newProblem("prism-one-input-file");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(ERROR_PRISM, name);
    }

    private ProblemsPRISM() {
    }
}
