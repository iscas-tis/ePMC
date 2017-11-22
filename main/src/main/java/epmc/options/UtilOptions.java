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

import java.util.Map;

import com.google.common.base.CaseFormat;

import epmc.util.Util;

/**
 * Several static auxiliary method to work with options and option sets.
 * Because this class provides only static methods, it is protected from being
 * instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilOptions {

    // TODO the options part of EPMC might not the most appropriate place for the static methods here

    // TODO documentation
    public static <T> T getInstance(String identifier) {
        assert identifier != null;
        Class<T> clazz = Options.get().get(identifier);
        ensure(clazz != null, ProblemsOptions.OPTIONS_OPTION_NOT_SET, identifier);
        return Util.getInstance(clazz);
    }

    public static <T> T getInstance(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getInstance(identifierString);
    }

    public static <T> T getSingletonInstance(Options options, String identifier) {
        assert options != null;
        assert identifier != null;
        Class<T> clazz = options.get(identifier);
        ensure(clazz != null, ProblemsOptions.OPTIONS_OPTION_NOT_SET, identifier);
        return Util.getSingletonInstance(clazz);
    }

    public static <T> T  getInstance(Options options,
            Enum<?> identifier, String command) {
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getInstance(options, identifierString, command);
    }

    public static <T> T getSingletonInstance(Options options,
            Enum<?> identifier) {
        assert options != null;
        assert identifier != null;
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getSingletonInstance(options, identifierString);
    }

    public static <T> T getInstance(Options options,
            String commandTaskClass, String command) {
        Map<String,Class<T>> map = options.get(commandTaskClass);
        String string = options.get(command);
        return Util.getInstance(map, string);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilOptions() {
    }
}
