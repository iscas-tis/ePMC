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

/**
 * <p>
 * Decision-diagram module.
 * </p>
 * <p>
 * This module of EPMC provides the functionality to work with decision
 * diagrams. Currently, the module supports BDDs and MTBDDs. It basically
 * provides a wrapper around several possible low-level libraries, such as
 * e.g. CUDD, which can either be written in C or Java. It provides
 * functionality to generate new DD variables and constants, combine existing
 * DDs using the usual operations (AND, OR, etc. for BDDs, ADD, MULTIPLY, etc.
 * for MTBDDs).
 * </p>
 * <p>
 * The package allows to operate on MTBDDs over arbitrary value
 * types of the {@link epmc.value} package. As not all MTBDD
 * packages support operating on arbitrary value types, there is an EPMC
 * plugin to provide an adapted version of CUDD to do so. Also, recent versions
 * of the Sylvan BDD package, for which also a plugin exist, allow to do so.
 * </p>
 * <p>
 * The package also contains a class {@link epmc.dd.VariableDD} to
 * represent decision diagrams over arbitrary non-boolean variables with finite
 * domain, such as for instance enums or finite range integers.
 * </p>
 * <p>
 * The naming conventions (e.g. the usage of "...with" for functions which
 * automatically dispose a given DD node) and other parts of this module are
 * inspired by
 * <a href="http://javabdd.sourceforge.net/">JavaBDD</a>.
 * However, EPMC does not build on the code of JavaBDD, because e.g.
 * </p>
 * <ul>
 * <li>the library has not be maintained for quite a while ("Last Update: 2013-04-25"),</li>
 * <li>we also need multi-terminal binary decision diagrams not supported by JavaBDD,</li>
 * <li>the support for CUDD seems not to be so great,</li>
 * <li>the DD module needs to be integrated with the plugin concept of EPMC,
 * which might not have been so easy with JavaBDD.</li>
 * </ul>
 * <p>
 * Currently, the package is restricted to work with <emph>binary</emph>
 * decision diagrams (BDDs). However, as it may later be extended to e.g.
 * support also MDDs, the package name is "dd", not "bdd".
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
package epmc.dd;
