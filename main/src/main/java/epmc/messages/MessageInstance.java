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
