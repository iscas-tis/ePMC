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

package epmc.automaton;

/**
 * Options of automaton plugin of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsAutomaton {
    /** Base name of resource bundle. */
    OPTIONS_AUTOMATON,
    AUTOMATON_CATEGORY,
    /** number of states reserved for DD-based automata ({@link Integer}) */
    AUTOMATON_DD_MAX_STATES,
    AUTOMATON_BUILDER,
    AUTOMATON_SPOT_LTL2TGBA_CMD,
    AUTOMATON_SUBSUME_APS,
    AUTOMATON_DET_NEG,
    AUTOMATA_REPLACE_NE,
    AUTOMATON_CLASS,
    AUTOMATON_EXPORTER_CLASS,
    ;

    public static enum Ltl2BaAutomatonBuilder {
        SPOT
    }

    public static enum Ltl2BaDetNeg {
        NEVER,
        BETTER,
        ALWAYS
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private OptionsAutomaton() {
    }
}
