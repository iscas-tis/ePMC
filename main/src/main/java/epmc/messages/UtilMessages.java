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

package epmc.messages;

import java.util.Map;

import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.OrderedMap;

/**
 * Static auxiliary methods for the messages module.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilMessages {
    /** String containing left bracket. */
    private final static String LBRACKET = "[";
    /** String containing right bracket. */
    private final static String RBRACKET = "]";
    /** String containing space. */
    private final static String SPACE = " ";
    /** Empty string. */
    private final static String EMPTY = "";

    public static String translateTimeStamp(Options options, long timeStarted, long time) {
        assert options != null;
        assert timeStarted >= 0;
        assert time >= 0;
        TimeStampFormat solver;
        solver = UtilOptions.getInstance(OptionsMessages.TIME_STAMPS);
        if (solver instanceof TimeStampFormatNone) {
            return EMPTY;
        }
        return LBRACKET + solver.toString(options, timeStarted, time)
        + RBRACKET + SPACE;
    }

    /**
     * Add options for message module to given options.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to add message options to
     */
    public static void addOptions(Options options) {
        assert options != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsMessages.OPTIONS_MESSAGES)
        .setIdentifier(OptionsMessages.TRANSLATE_MESSAGES)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb().build();
        Map<String, Class<?>> timeStampFormatMap = new OrderedMap<>(true);
        timeStampFormatMap.put(TimeStampFormatNone.IDENTIFIER, TimeStampFormatNone.class);
        timeStampFormatMap.put(TimeStampFormatJavaDate.IDENTIFIER, TimeStampFormatJavaDate.class);
        timeStampFormatMap.put(TimeStampFormatMillisecondsStarted.IDENTIFIER, TimeStampFormatMillisecondsStarted.class);
        timeStampFormatMap.put(TimeStampFormatMillisecondsAbsolute.IDENTIFIER, TimeStampFormatMillisecondsAbsolute.class);
        timeStampFormatMap.put(TimeStampFormatSecondsStarted.IDENTIFIER, TimeStampFormatSecondsStarted.class);
        timeStampFormatMap.put(TimeStampFormatSecondsAbsolute.IDENTIFIER, TimeStampFormatSecondsAbsolute.class);
        OptionTypeMap<Class<?>> timeStampFormatType = new OptionTypeMap<>(timeStampFormatMap);
        options.addOption().setBundleName(OptionsMessages.OPTIONS_MESSAGES)
        .setIdentifier(OptionsMessages.TIME_STAMPS)
        .setDefault(TimeStampFormatNone.class)
        .setType(timeStampFormatType)
        .setCommandLine().setGui().setWeb().build();
    }

    /**
     * Private constructor to avoid creation of objects of this class.
     */
    private UtilMessages() {
    }
}
