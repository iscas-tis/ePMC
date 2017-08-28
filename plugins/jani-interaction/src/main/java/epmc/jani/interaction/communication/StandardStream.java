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

package epmc.jani.interaction.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.LinkedBlockingDeque;

// TODO continue
// TODO documentation

/**
 * Implements JANI interaction via {@link System#out} and {@link System#in}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class StandardStream implements BackendFeedback {
    /** String denoting UTF-8 character encoding type. */
    private final static String UTF_8 = "UTF-8";

    /** Whether the main loops waiting for messages are to be terminated. */
    private boolean done;
    /** Incoming and not yet served messages. */
    private final LinkedBlockingDeque<String> input = new LinkedBlockingDeque<>();
    /** Outgoing and not yet distributed messages. */
    private final LinkedBlockingDeque<String> output = new LinkedBlockingDeque<>();
    /** Backend handling the messages. */
    private final Backend backend;
    private Thread handleThread;
    private Thread outputThread;
    private ReadableByteChannel in;

    /**
     * Construct new standard I/O interaction object.
     * The options parameter must not be {@code null}
     * 
     */
    public StandardStream() {
        this.backend = new Backend(this);
    }

    /**
     * Start reading/writing to/from {@link System#out} and {@link System#in}.
     */
    public void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String line = null;
                in = Channels.newChannel(System.in);
                Reader reader = Channels.newReader(in, UTF_8);
                BufferedReader buf = new BufferedReader(reader);
                while (!done) {
                    try {
                        line = buf.readLine();
                    } catch (AsynchronousCloseException e) {
                        /* Will be thrown if converter closed. */
                        break;						
                    } catch (IOException e) {
                        System.err.println(e);
                        assert false;
                    }
                    if (line == null) {
                        break;
                    }
                    try {
                        input.put(line);
                    } catch (InterruptedException e) {
                        /* Should not be thrown, as we're using buffers
                         * of unbounded capacity. */
                        break;
                    }
                }
            }
        }).start();

        handleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!done) {
                    String line = null;
                    try {
                        line = input.take();
                    } catch (InterruptedException e) {
                        break;
                    }
                    backend.handle(StandardStream.this, line);
                }
            }
        });
        handleThread.start();
        outputThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!done) {
                    String line = null;
                    try {
                        line = output.take();
                        System.out.println(line);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        outputThread.start();
    }

    @Override
    public void send(Object where, String message) {
        assert where != null;
        assert where == this : where;
        assert message != null;
        try {
            output.put(message);
        } catch (InterruptedException e) {
            /* Should not be thrown, as buffer capacity is unbounded */
        }
    }

    @Override
    public void logOff(Object who) {
        assert who != null;
        assert who == this;
        done = true;
        try {
            in.close();
        } catch (IOException e) {
        }
        handleThread.interrupt();
        outputThread.interrupt();
        try {
            in.close();
        } catch (IOException e) {
        }
    }
}
