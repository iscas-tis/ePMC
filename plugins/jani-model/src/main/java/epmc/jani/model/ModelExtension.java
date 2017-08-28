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

package epmc.jani.model;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Extension for JANI models.
 * This interface is used for classes which extend the parsing process of JANI
 * models.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ModelExtension {
	String getIdentifier();
	
	default void setModel(ModelJANI model) {
	}
	

	default ModelJANI getModel() {
		return null;
	}

	
	default void setNode(JANINode node) {
	}
	
	default void setJsonValue(JsonValue value) {
	}

	default void parseBefore() {
	}
	
	default void parseAfter() {
	}
	
	default void generate(JsonObjectBuilder generate) {
	}
}
