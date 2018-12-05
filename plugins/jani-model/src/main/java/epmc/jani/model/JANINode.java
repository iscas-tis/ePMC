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

import java.util.Map;

import javax.json.JsonValue;

/**
 * Interface for JANI model parts which can be read and written from/to JANI.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANINode {
    /**
     * Set model which this node belongs to.
     * For some nodes implementing this interface, it might be necessary to call
     * this function before parsing and similar functionality.
     * 
     * @param model model this node is part of
     */
    void setModel(ModelJANI model);

    ModelJANI getModel();

    /**
     * Parse JSON to JANI node.
     * By performing this function, the JANI node will be filled with content
     * and can be used afterwards. The method may only be called once. It must
     * not be called with any {@code null} parameters. Before performing this
     * method, additional calls to set context objects may be necessary. After
     * this call, the object should become immutable. The method may only be
     * called once.
     * 
     * @param model model to which this node belongs
     * @param value JSON to convert to JANI
     * @return the JANINode corresponding to the given value, or {@code null} if the parsing is not possible
     */
    JANINode parse(JsonValue value);

    /**
     * Generate JSON from this JANI node.
     * Must only be called after {@link #parseExpression(JsonValue)} has been
     * called successfully.
     * 
     * @return JSON representing this JANI node
     */
    JsonValue generate();
}
