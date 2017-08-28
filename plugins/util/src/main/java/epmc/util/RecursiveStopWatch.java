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

import epmc.util.StopWatch;

/**
 * Stop watch counting how many time it was started.
 * <p>
 * The stop watch maintains an integer counter. Each time the method
 * {@link #start()} is called, the counter is increased, and each time the
 * method {@link #stop()} is called, the counter is decreased. The stopwatch
 * is running if this counter is larger than zero, that is the running time
 * of the stop watch is increased by time only in this case. It is an error to
 * call the {@link #stop()} method if the stop/start counter is zero. Thus, the
 * {@link #stop()} method must not be called more often than the
 * {@link #start()} method has been called. The time the stop watch has been
 * running can be obtained by {@link #getTime()} or {@link #getTimeSeconds()}.
 * </p>
 * <p>
 * The behaviour of the stop watch is suited for situations in which one wants
 * to measure the total time which a program spends in a given family of
 * methods. For instance, if we are given methods {@code methodA},
 * {@code methodB} where {@link methodA} might call {@link methodB}, and we want
 * to measure the total time spend in any of them, we could write them according
 * to the following scheme (where {@code watch} is a field and thus accessible
 * by both methods):
 * </p>
 * <pre>
 * {@code
 * methodA() {
 *   watch.start();
 *   ...
 *   methodB()
 *   ...
 *   watch.stop()
 * }
 * 
 * methodB() {
 *   watch.start();
 *   ...
 *   watch.stop()
 * }
 * }
 * </pre>
 * <p>
 * With {@link StopWatch}, such a scheme is not possible, because it cannot be
 * started multiple times. However, if a stop watch is expected to be started
 * only once, we propose to use a {@link StopWatch} rather than a
 * {@link RecursiveStopWatch} to be able to detect possible bugs earlier,
 * because {@link StopWatch} can throw an {@link AssertionError} in case it is
 * started multiple times.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public final class RecursiveStopWatch {
    /** String constant "running". */
    private final static String RUNNING = "running";
    /** String constant "time". */
    private final static String TIME = "time";
    /** Time (milliseconds since epoch) when the stop watch was started. */
    private long timeStarted;
    /** Time in milliseconds since the stop watch was stopped (if it was). */
    private long timePassed;
    /** Number of times {@link #start()} has been called minus number of times
     * {@link #stop()} has been called.
     * */
    private int running;

    /**
     * Create a new stop watch.
     * The stop watch will initially not be running, because the start/stop
     * counter is zero.
     */
    public RecursiveStopWatch() {
        timeStarted = System.currentTimeMillis();
    }

    /**
     * Decrease start/stop counter, stopping the watch if it reaches zero.
     */
    public void stop() {
        assert running > 0;
        running--;
        if (running == 0) {
            long time = System.currentTimeMillis();
            timePassed = timePassed + (time - timeStarted);
            timeStarted = 0;
        }
    }

    /**
     * Starting start/stop counter, starting the clock if it not yet running.
     */
    public void start() {
        if (running == 0) {
            timeStarted = System.currentTimeMillis();
        }
        running++;
    }

    /**
     * Read the time the clock has been in running state.
     * 
     * @return time the clock has been in running state
     */
    public long getTime() {
        long result;
        if (running > 0) {
            long time = System.currentTimeMillis();
            result = timePassed + (time - timeStarted);
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

    /**
     * Read the start/stop counter.
     * 
     * @return start/stop counter
     */
    public int getRunning() {
        return running;
    }
}
