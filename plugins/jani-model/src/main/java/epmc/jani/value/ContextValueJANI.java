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

package epmc.jani.value;

import epmc.jani.model.Locations;
import epmc.value.ContextValue;

/**
 * Context value extension for JANI.
 * Class to generate types specific types for the JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ContextValueJANI {
	/** Context value used for the plugin. */
	private final ContextValue contextValue;

	/**
	 * Construct new context value extension for JANI.
	 * 
	 * @param contextValue context value to use
	 */
	public ContextValueJANI(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
	}
	
	/**
	 * Construct type to store location from a set of locations.
	 * 
	 * @param locations set of locations the type will represent
	 * @return type to store location from a set of locations
	 */
	public TypeLocation getTypeLocation(Locations locations) {
		assert locations != null;
		TypeLocation type = new TypeLocation(contextValue, locations);
		return contextValue.makeUnique(type);
	}
}
