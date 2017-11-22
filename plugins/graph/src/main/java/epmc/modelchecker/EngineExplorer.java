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

package epmc.modelchecker;

import epmc.modelchecker.Engine;

// TODO move outside main part

/**
 * Explorer-based engine.
 * This engine servers as the base for e.g. Monte-Carlo simulation or other
 * techniques where a state-based representation of the model is not required
 * or where management of nodes should be done completely manually.
 * 
 * @author Ernst Moritz Hahn
 */
public enum EngineExplorer implements Engine {
    /** The singleton instance of this engine. */
    ENGINE_EXPLORER;

    /** Unique identifier of the engine. */
    public final static String IDENTIFIER = "explorer";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Obtain the singleton object of this engine.
     * 
     * @return singleton object of this engine
     */
    public static Engine getInstance() {
        return ENGINE_EXPLORER;
    }
}
