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

package epmc.prism.operator;

import epmc.operator.Operator;

/**
 * Operator "pow" of PRISM.
 * This operator is different from the one of the JANI language:
 * In this version, if both operands are integers, then the result type is an
 * integer too. In the JANI version however, the result is a real number in
 * any case.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OperatorPRISMPow implements Operator {
    PRISM_POW
}
