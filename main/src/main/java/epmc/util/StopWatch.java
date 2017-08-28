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

package epmc.util;

import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

/**
 * Class implementing a simple stop watch.
 * The stop watch can either be stopped or running. If the clock is stopped, it
 * can be turned into the running state by {@link #start()} and if it is
 * running, it can be turned into the stopped state by calling {@link #stop()}.
 * It is an error to call {@link StopWatch#start()} in the running state or
 * {@link #stop()} in the stopped state. The clock will count the total time the
 * clock has been in the running state, which can be read by {@link #getTime()}
 * or {@link #getTimeSeconds()}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class StopWatch {
    /** String constant "running". */
    private final static String RUNNING = "running";
    /** String constant "time". */
    private final static String TIME = "time";
    /** Time (milliseconds since epoch) when the stop watch was started. */
    private long timeStarted;
    /** Time in milliseconds since the stop watch was stopped (if it was). */
    private long timePassed;
    /** Indicates whether the stop watch is running. */
    private boolean running;

    /**
     * Create a new stop watch.
     * 
     * @param running whether the stop watch is created already running
     */
    public StopWatch(boolean running) {
        timeStarted = System.currentTimeMillis();
        this.running = running;
    }

    /**
     * Stop the stop watch.
     * The function must not be called if the stop watch is not running.
     */
    public void stop() {
        assert running;
        running = false;
        long time = System.currentTimeMillis();
        timePassed = timePassed + (time - timeStarted);
        timeStarted = 0;
    }

    /**
     * Start the stop watch.
     * The function must not be called if the stop watch is running.
     */
    public void start() {
        assert !running;
        running = true;
        timeStarted = System.currentTimeMillis();
    }

    /**
     * Read the time in milliseconds the clock has been in running state.
     * 
     * @return time in milliseconds the clock has been in running state
     */
    public long getTime() {
        long result;
        if (running) {
            stop();
            result = timePassed;
            start();
        } else {
            result = timePassed;
        }

        return result;
    }

    /**
     * Read the time in seconds the clock has been in running state.
     * 
     * @return time in seconds the clock has been in running state
     */
    public long getTimeSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(getTime());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(RUNNING, running)
                .add(TIME, getTime())
                .toString();
    }
}
