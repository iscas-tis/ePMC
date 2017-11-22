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

package epmc.options;

import epmc.error.EPMCException;

/**
 * Type information of an option.
 * <p>
 * Classes implementing this interface are used to parse {@link String}s to
 * values to be contained in an {@link Option}. The should also print a short
 * representation of the type.
 * </p>
 * <p>
 * Note that option types can be shared between different options. Thus, they
 * should only contain data for a particular option if a single option uses the
 * option type.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public interface OptionType {
    /**
     * Parse the given value to an object of the correct class.
     * Classes implementing this interface should try to parse the given value
     * parameter. If they cannot parse it, they should throw an according
     * {@link EPMCException} describing the cause of the problem. The
     * parameter for the previous value is used to allow multiple parsing of
     * values which consist of several parts, e.g.
     * <pre>
     * {@code --<optionA> <param1> <param2> --<optionB> ... --<optionA> <param3>}
     * </pre>
     * would result in three subsequent calls to this method for {@code optionA}
     * to parse parameters {@code param1}, {@code param2}, and {@code param3}.
     * In the first call, the previous value would be {@code null} because
     * nothing has been parsed so far, and the method might e.g. return a list
     * containing only the parsed {@code param1}. In the second call, the
     * previous value would be this list, and the method would return a list
     * containing the first two parsed values. In the third call the previous
     * value would be this list, and it would be extended by parsing the third
     * parameter. Examples for such option types include list of files in case
     * a model consists of multiple files, lists of constant definitions, plugin
     * lists, etc. In case an option should be given only once by the command
     * line, the an exception should be thrown if the previous value does not
     * equal {@code null}. The method should not be called with a value
     * parameter of {@code null}, while the previous value might be {@code null}
     * if it was not set previously. The method should not return {@code null},
     * instead, in case values cannot be parsed, an exception should be thrown.
     * 
     * @param value value to be parsed
     * @param previousValue value parsed previously, or {@code null}
     * @return parsed value
     */
    Object parse(String value, Object previousValue);

    /**
     * Obtain short description of option type.
     * For instance, for an integer type the description might be
     * {@code <integer>}, for a list of possible values the description might be
     * {@code <value1|...|valuen>}, etc.
     * 
     * @return short description of option type
     */
    String getInfo();

    /**
     * Obtain default value for this options type.
     * This value is used as the default value of {@link Option}s in case no
     * other default value of the option is provided. The default implementation
     * if this method returns @{code null}, that is no usable default value is
     * provided.
     * 
     * @return default value for this options type
     */
    default Object getDefault() {
        return null;
    }

    /**
     * Format the value of this options type to a {@link String}.
     * The method must not be called with {@code null} parameter. Also, it must
     * only be parsed by a value which the parser could produce as a result of
     * parsing. It will then produce a string which could again be parsed by the
     * method {@link #parse(String, Object)}. The default implementation just
     * uses the method {@link #toString()} to produce the output, which might
     * not be sufficient for all option types.
     * 
     * @param value value to format
     * @return formatted output readable by the same parser
     */
    default String unparse(Object value) {
        assert value != null;
        return value.toString();
    }
}
