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

import epmc.error.EPMCException;

/**
 * Class to periodically generate progress information.
 * This class should be used instead of manually starting threads to show
 * periodic progress information, so as to avoid bugs like forgetting to
 * terminate the thread showing progess information and the like.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RunningInfo {
    @FunctionalInterface
    public interface GuardedCall <T> {
        T call(RunningInfo info);
    }

    @FunctionalInterface
    public interface GuardedCallVoid {
        void call(RunningInfo info);
    }

    @FunctionalInterface
    public interface SendInformation {
        void call();
    }

    /** Thread periodically sending user information. */
    private final Thread observerThread = constructObserverThread();
    /** Indicates whether thread is to be stopped on next occasion. */
    private boolean done;
    /** Which information sender is to be used. */
    private SendInformation information;
    /** Time interval between sending user feedback. */
    private long sleepTime = 1000l;

    public static void startWithInfoVoid(GuardedCallVoid call) {
        startWithInfo(info -> {call.call(info); return (Object) null;});
    }
    
    public static <T> T startWithInfo(GuardedCall<T> call) {
        assert call != null;
        RunningInfo info = new RunningInfo();
        T result = null;
        try {
            result = call.call(info);
        } catch (EPMCException e) {
            info.done();
            throw e;
        } catch (Throwable e) {
            info.done();
            throw new RuntimeException(e);
        }
        info.done();
        return result;
    }

    public void setInformationSender(SendInformation information) {
        this.information = information;
    }
    
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
    
    private Thread constructObserverThread() {
        Thread observerThread = new Thread(() -> {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
            while (!done) {
                if (information != null) {
                    try {
                        information.call();
                    } catch (Throwable t) {
                        t.printStackTrace(System.out);
                        System.exit(1);
                    }
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                }
            }
        });
        observerThread.start();
        return observerThread;
    }

    /**
     * Terminates information threat.
     */
    private void done() {
        done = true;
        observerThread.interrupt();
    }

    /**
     * Private constructor.
     * We want to ensure that {{@link #startWithInfo(GuardedCall)}} is used
     * to construct instances of this class.
     */
    private RunningInfo() {
    }
}
