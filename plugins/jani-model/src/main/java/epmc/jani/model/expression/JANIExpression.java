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

package epmc.jani.model.expression;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;

/**
 * JANI representation of expressions and expression parser.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANIExpression extends JANINode {
    default void setForProperty(boolean forProperty) {
    }

    Expression getExpression();

    /**
     * Match the given expression and returns the corresponding {@link epmc.jani.model.expression.JANIExpression}.
     * This function tries to match the given expression in the  context of the given model; in case of successful matching, the corresponding  
     * {@link epmc.jani.model.expression.JANIExpression} is returned, {@code null} otherwise.
     * 
     * @param model the model to use as reference
     * @param expression the expression to match
     * @return the {@link epmc.jani.model.expression.JANIExpression} representing the expression, {@code null} if no matching is possible
     */
    JANIExpression matchExpression(ModelJANI model, Expression expression);

    JANIExpression parseAsJANIExpression(JsonValue value);

    void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers);
    
    void setPositional(Positional positional);
    
    Positional getPositional();
}
