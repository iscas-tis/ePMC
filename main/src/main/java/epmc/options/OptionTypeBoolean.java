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
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

/**
 * Option type for boolean options.
 * The parser interprets the {@link String}s {@code "true"}, {@code "TRUE"},
 * {@code "True"}, {@code " TrUe "} or other strings obtained by replacing parts
 * of characters in {@code "true"} by capital characters and adding whitespace
 * before or after the string as the boolean value {@code true}. Accordingly,
 * this is the case for {@code "false}. Strings not of this form will result in
 * an {@link EPMCException} being thrown.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeBoolean implements OptionType {
    /** String containing "true". */
    private final static String TRUE = "true";
    /** String containing "false". */
    private final static String FALSE = "false";
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<boolean>";
    /** Boolean option type. */
    private final static OptionTypeBoolean INSTANCE = new OptionTypeBoolean();

    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeBoolean#getInstance()} rather than by directly calling the
     * constructor.
     */
    private OptionTypeBoolean() {
    }

    @Override
    public Object parse(String value, Object previousValue)
    {
        assert value != null;
        ensure(previousValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        value = value.toLowerCase();
        if (value.equals(TRUE)) {
            return true;
        } else if (value.equals(FALSE)) {
            return false;
        } else {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            return null;
        }
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public String unparse(Object value) {
        assert value != null;
        if (value == Boolean.TRUE) {
            return TRUE;
        } else if (value == Boolean.FALSE) {
            return FALSE;
        } else {
            assert false;
            return null;
        }
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public Object getDefault() {
        return false;
    }

    /**
     * Get boolean option type.
     * 
     * @return boolean option type
     */
    public static OptionTypeBoolean getInstance() {
        return INSTANCE;
    }
}
