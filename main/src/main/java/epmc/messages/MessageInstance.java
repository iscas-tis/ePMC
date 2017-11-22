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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * Represents an instantiation of a message.
 * Thus, objects of this class contain a message as well as the according
 * parameters.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessageInstance {
    /** name of time field */
    private final static String TIME = "time";
    /** name of message field */
    private final static String MESSAGE = "message";
    /** name of parameters field */
    private final static String PARAMETERS = "parameters";
    /** time in epoch format at which the message was sent */
    private final long time;
    /** message identifier of this message instance */
    private final Message message;
    /** parameters of this message instance */
    private final List<String> parameters = new ArrayList<>();
    /** write-protected parameters for external usage */
    private final List<String> parametersExternal = Collections.unmodifiableList(parameters);

    /**
     * Creates a new message instance.
     * None of the parameters may be {@code null} or contain {@code null}
     * entries.
     * 
     * @param message message to construct instance of
     * @param parameters parameters of this message instance
     */
    public MessageInstance(long time, Message message, String... parameters) {
        assert time >= 0; // no messages before 1970
        assert message != null;
        assert parameters != null;
        for (Object parameter : parameters) {
            assert parameter != null;
        }
        this.time = time;
        this.message = message;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add(TIME, time)
                .add(MESSAGE, message)
                .add(PARAMETERS, parameters)
                .toString();
    }

    /**
     * Obtain time the message was sent in epoch format.
     * 
     * @return time the message was sent in epoch format
     */
    public long getTime() {
        return time;
    }

    /**
     * Obtain message identifier.
     * 
     * @return message identifier
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Obtain parameters of this message instance.
     * The returned list is write-protected.
     * 
     * @return write-protected parameters of this message
     */
    public List<String> getParameters() {
        return parametersExternal;
    }

    /**
     * Obtain copy of parameters of this message as array.
     * 
     * @return copy of parameters of this message as array
     */
    public String[] getParametersArray() {
        return parameters.toArray(new String[parameters.size()]);
    }
}
