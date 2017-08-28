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

package epmc.dd;

import epmc.options.Options;

/**
 * <p>
 * Class storing DD module options.
 * </p>
 * <p>
 * The purpose of this class is to store options for DD operations, such as
 * whether the and-exists operator shall be used, whether a leak check shall be
 * performed, etc. Options of this class are prefixed with "dd-".
 * </p>
 * <p>
 * Options which are specific to some DD library (e.g. specific to
 * CUDD, to Sylvan, etc.) are <emph>not</code> be stored in this class.
 * Instead, they are options of the according plugin providing the DD library.
 * These options should be prefixed with "dd-&lt;name-of-library&gt;-".
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsDD {
    /** Base name of resource file for options description. */
    OPTIONS_DD,
    /** Category entry identifier */
    DD_CATEGORY,
    /** Whether extended (and slow) DD debugging shall be enabled. */
    DD_DEBUG,
    /** DD library to use for MTBDDs. */
    DD_MULTI_ENGINE,
    /** DD library to use for BDDs. */
    DD_BINARY_ENGINE,
    /** Whether to use the and-exists operator, if available. */
    DD_AND_EXIST,
    /** Whether to perform extended (and slow) leak checks. */
    DD_LEAK_CHECK,
    /** Key in {@link Options} providing a {@link Map} of {@link String}s
     * identifying {@link Class}es extending {@link LibraryDD} usable as BDD
     * libraries by instantiation.
     * */
    DD_LIBRARY_CLASS,
    /** Key in {@link Options} providing a {@link Map} of {@link String}s
     * identifying {@link Class}es extending {@link LibraryDD} usable as MTBDD
     * libraries by instantiation.
     * */
    DD_MT_LIBRARY_CLASS,
}
