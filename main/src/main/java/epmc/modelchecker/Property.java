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

import java.io.InputStream;
import java.io.OutputStream;

import epmc.expression.Expression;
import epmc.value.Type;

/**
 * Class to handle properties of one particular input/output format.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Property {
    /**
     * Return unique identifier for the property type.
     * 
     * @return unique identifier of property type
     */
    String getIdentifier();

    /**
     * Parse a property in the syntax of this property type.
     * An exception might be thrown if the input string is not a valid
     * expression in this format.
     * The input parameter must not be <code>null</code>.
     * 
     * @param expression expression to be parsed.
     * @return parsed expression
     */
    Expression parseExpression(Object identifier, InputStream expression);

    /**
     * Parse a type in the syntax of this property type.
     * An exception might be thrown if the input string is not a valid
     * type in this format.
     * The input parameter must not be {@code null}.
     * 
     * @param expression expression to be parsed.
     * @return parsed expression
     */
    Type parseType(Object identifier, String type);

    /**
     * Reads a complete list of properties in the format of this property type.
     * 
     * @param properties where to store properties
     * @param stream stream from which to read the properties
     */
    void readProperties(Object identifier, RawProperties properties, InputStream stream);

    /**
     * Writes a complete list of properties in the format of this property type.
     * This will only work if all properties are of the property type of this
     * class.
     * 
     * @param properties properties to be stored
     * @param stream stream to store properties to
     */
    void writeProperties(RawProperties properties, OutputStream stream);
}
