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

import java.util.ArrayDeque;
import java.util.Deque;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.BackendFeedback;
import epmc.options.Options;

final class BackendTester {
    private final class TestBackendFeedback implements BackendFeedback {
        @Override
        public void send(Object client, String message) {
            assert client != null;
            assert message != null;
            assert client == this;
            pending.add(message);
        }

        @Override
        public void logOff(Object who) {
            assert who != null;
            alive = false;
        }

    }
    private final TestBackendFeedback feedback = new TestBackendFeedback();
    private final Backend backend;
    private final Deque<String> pending = new ArrayDeque<>();
    private boolean alive = true;

    BackendTester(Options options) {
        assert options != null;
        backend = new Backend(feedback);
    }

    void send(JsonObjectBuilder request) {
        assert request != null;
        send(request.build());
    }


    void send(JsonValue request) {
        assert request != null;
        send(request.toString());
    }

    void send(String message) {
        assert message != null;
        backend.handle(backend, message);
    }

    int size() {
        return pending.size();
    }

    String popPending() {
        return pending.pop();
    }

    boolean alive() {
        return alive;
    }

}
