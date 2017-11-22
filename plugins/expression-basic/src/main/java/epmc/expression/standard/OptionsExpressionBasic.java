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

package epmc.expression.standard;

// TODO complete documentation

public enum OptionsExpressionBasic {
    /** Base name of resource file for options description. */
    OPTIONS_EXPRESSION_BASIC,

    /**
     * Store already constructed decision diagrams in cache when converting
     * expressions to decision diagrams. */
    DD_EXPRESSION_CACHE,
    /**
     * Use a bitvector representation when converting expressions to decision
     * diagrams to avoid having to construct MTBDDs with very many terminal
     * nodes.
     */
    DD_EXPRESSION_VECTOR,

    EXPRESSION_EVALUTOR_EXPLICIT_CLASS,
    EXPRESSION_EXPRESSION_TO_CODE_CLASS,
    EXPRESSION_EVALUTOR_DD_CLASS,
    EXPRESSION_SIMPLIFIER_CLASS,
}
