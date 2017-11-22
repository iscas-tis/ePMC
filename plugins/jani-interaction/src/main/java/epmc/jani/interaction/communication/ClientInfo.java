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

import static epmc.error.UtilError.ensure;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.jani.interaction.remote.TaskServer;
import epmc.options.Options;

/**
 * Client data of a connection on the backend.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ClientInfo {
    public final static class Builder {
        private Object client;
        private int id;
        private Options options;
        private Set<String> extensions;

        public Builder setClient(Object client) {
            this.client = client;
            return this;
        }

        private Object getClient() {
            return client;
        }

        public Builder setID(int id) {
            this.id = id;
            return this;
        }

        private int getID() {
            return id;
        }

        public Builder setOptions(Options options) {
            this.options = options;
            return this;
        }

        private Options getOptions() {
            return options;
        }

        public Builder setExtensions(Set<String> extensions) {
            this.extensions = extensions;
            return this;
        }

        private Set<String> getExtensions() {
            return extensions;
        }

        public ClientInfo build() {
            return new ClientInfo(this);
        }
    }

    /** Running analyses started by the client. */
    private final Map<BigInteger,TaskServer> runningAnalyses = new LinkedHashMap<>();
    /** Identifier object of the client. */
    private final Object client;
    /** Options used by the client. These options are different from the global
     * options in the backend, as they can be set by the individual clients
     * rather than being set at the start of EPMC. */
    private final Options options;
    /** ID reference in user manager. */
    private int userManagerID;
    private Set<String> usableExtensions;

    private ClientInfo(Builder builder) {
        assert builder != null;
        assert builder.getClient() != null;
        assert builder.getOptions() != null;
        this.client = builder.getClient();
        this.userManagerID = builder.getID();
        this.options = builder.getOptions().clone();
        this.usableExtensions = builder.getExtensions();
    }

    /**
     * Get client identifying object.
     * 
     * @return client identifying object
     */
    public Object getClient() {
        return client;
    }

    /**
     * Get user manager ID.
     * 
     * @return user manager ID
     */
    public int getUserManagerID() {
        return userManagerID;
    }

    /**
     * Get client options.
     * 
     * @return client options
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Check whether the client already contains an analysis with given ID.
     * The ID parameter must not be {@code null}.
     * 
     * @param id ID of which to check whether already contained
     * @return whether the client already constains an analysis with given ID
     */
    public synchronized boolean containsAnalysis(BigInteger id) {
        assert id != null;
        return runningAnalyses.containsKey(id);
    }

    /**
     * Register an analysis.
     * The analysis must already be running.
     * If there is already an analysis with the same ID, an exception will be
     * thrown.
     * None of the parameters may be {@code null}.
     * 
     * @param id ID of the analysis
     * @param server task server running the analysis
     */
    public synchronized void registerAnalysis(BigInteger id, TaskServer server) {
        assert id != null;
        assert server != null;
        ensure(!runningAnalyses.containsKey(id), ProblemsJANIInteraction.JANI_INTERACTION_ANALYSIS_SAME_ID);
        runningAnalyses.put(id, server);
    }

    /**
     * Remove analysis ID from client information.
     * This method shall be called when an analysis has finished successfully.
     * The ID parameter must not be {@code null}.
     * If there is no analysis with the given ID, calling this method does not
     * have any effect.
     * 
     * @param id ID to remove
     */
    public synchronized void analysisDone(BigInteger id) {
        assert id != null;
        runningAnalyses.remove(id);
    }

    /**
     * Stop the analysis with the given ID.
     * This method shall be called to stop an analysis which is still running.
     * After stopping the analysis, the analysis with the given ID will be
     * removed from the client. If there is no analysis with the given ID in the
     * client, calling this method has no effect.
     * The ID parameter must not be {@code null}.
     * 
     * @param id ID of analysis to stop
     */
    public synchronized void stopAnalysis(BigInteger id) {
        assert id != null;
        if (!runningAnalyses.containsKey(id)) {
            return;
        }
        TaskServer server = runningAnalyses.get(id);
        if (server == null) {
            return;
        }
        try {
            server.stop();
        } catch (EPMCException e) {
            /* We don't care. */
        }
        runningAnalyses.remove(id);
    }

    public synchronized void terminate() {
        for (TaskServer server : runningAnalyses.values()) {
            try {
                server.stop();
            } catch (EPMCException e) {
                /* We don't care. */
            }
        }
        runningAnalyses.clear();
    }

    public Set<String> getExtensions() {
        return usableExtensions;
    }
}
