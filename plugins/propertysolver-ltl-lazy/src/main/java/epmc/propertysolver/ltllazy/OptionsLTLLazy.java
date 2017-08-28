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

package epmc.propertysolver.ltllazy;

/**
 * Class collecting options used for our lazy LTL solver.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsLTLLazy {
    /** Base name of resource file for options description. */
    OPTIONS_LTL_LAZY,
    LTL_LAZY_CATEGORY,
    /** Whether the incremental approach shall be used.
     * If yes, the subset construction will be used and its components will be
     * evaluated by subset, breakpoint, Rabin, or multi-breakpoint criteria.
     * Otherwise, the algorithm will try to decide the model using complete
     * products of subset, breakpoint, etc. automata, which potentially uses
     * more space.
     **/
    LTL_LAZY_INCREMENTAL,
    /** Whether to use the subset criterion. */
    LTL_LAZY_USE_SUBSET,
    /** Whether the breakpoint criterion may be used. */
    LTL_LAZY_USE_BREAKPOINT,
    /** Whether the multi-breakpoint approach may be used . */
    LTL_LAZY_USE_BREAKPOINT_SINGLETONS,
    /** Whether the Rabin-automaton-based approach may be used. */
    LTL_LAZY_USE_RABIN,
    /** Whether the component decomposition may skip transient components.
     * These are components consisting of a single state with no self loop.
     * This involves nodes which can reach with probability one a component
     * which is already known to be accepting.
     * */
    LTL_LAZY_SCC_SKIP_TRANSIENT,
    /** Whether the algorithm may remove states which are known to be accepting.
     * */
    LTL_LAZY_REMOVE_DECIDED,
    /** Whether algorithm may stop once value for initial states is decided. */
    LTL_LAZY_STOP_IF_INIT_DECIDED,
}
