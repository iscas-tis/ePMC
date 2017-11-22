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

import java.util.concurrent.TimeUnit;

import epmc.options.Options;

public final class TimeStampFormatSecondsAbsolute implements TimeStampFormat {
    public static String IDENTIFIER = "seconds-absolute";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String toString(Options options, long timeStarted, long time) {
        return Long.toString(TimeUnit.MILLISECONDS.toSeconds(time + timeStarted));
    }

}
