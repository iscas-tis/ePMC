package epmc.jani.interaction.remote;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Utility functions to control EPMC task server.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilRemote {
    // TODO unclear whether this will stay here
    /** String to filer for EPMCException. */
    public final static String EPMCEXCEPTION_INDICATOR = "EPMCException";
    // TODO unclear whether this will stay here
    /** Server waiting for incoming connections. */
    public static final String SERVER_WAITING_INCOMING = "serverWaitingIncoming";
    /** string containing single whitespace */
    private static final String SPACE = " ";

    /**
     * Obtain RMI data from an output stream of EPMC task server.
     * If the server indicates it could not start up correctly, but was able to
     * report an error via standard output, the according
     * {@link EPMCException} will be thrown instead. If the server was not
     * able to provide proper output at all, another {@link EPMCException}
     * will be thrown.
     * 
     * @param stream stream to read data/problems from
     * @return data about RMI connection to use
     * @throws EPMCException thrown if the server indicates problems
     */
    public static RMIConnectionData readServerStatus(InputStream stream)
            throws EPMCException {
        assert stream != null;
        Reader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            fail(ProblemsRemote.REMOTE_PROCESS_READ_OUTPUT_IO_EXCEPTION, e.getMessage(), e);
        }
        ensure(line != null, ProblemsRemote.REMOTE_PROCESS_OUTPUT_NULL);
        String[] split = line.split(SPACE);
        ensure(split.length >= 3, ProblemsRemote.REMOTE_OUTPUT_INCORRECT, line);
        if (split[0].equals(EPMCEXCEPTION_INDICATOR)) {
            String resourceBundleString = split[1];
            String name = split[2];
            Problem problem = UtilError.newProblem(resourceBundleString, name);
            Object[] parameters = Arrays.copyOfRange(split, 3, split.length);
            fail(problem, parameters);
        }
        ensure(split.length == 3, ProblemsRemote.REMOTE_OUTPUT_INCORRECT, line);
        ensure(split[0].equals(SERVER_WAITING_INCOMING),
                ProblemsRemote.REMOTE_OUTPUT_INCORRECT, line);
        String name = split[1];
        int port = 0;
        try {
            port = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            fail(ProblemsRemote.REMOTE_OUTPUT_INCORRECT, line);
        }
        return new RMIConnectionData(name, port);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilRemote() {
    }
}