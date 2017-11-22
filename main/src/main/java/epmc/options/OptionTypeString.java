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

import static epmc.error.UtilError.ensure;

/**
 * Option type parsing any string.
 * This option type allows any string to be parsed and stored, after trimming.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeString implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<string>";
    /** String option type. */
    final static OptionTypeString INSTANCE = new OptionTypeString();

    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeString#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeString() {
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        return value;
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public String toString() {
        return getInfo();
    }

    /**
     * Get string option type.
     * 
     * @return string option type
     */
    public static OptionTypeString getInstance() {
        return INSTANCE;
    };    
}
