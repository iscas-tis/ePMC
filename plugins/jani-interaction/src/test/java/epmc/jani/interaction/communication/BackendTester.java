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
import java.util.List;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.BackendFeedback;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.PluginInterface;

final class BackendTester {
    private final class TestBackendFeedback implements BackendFeedback {
        @Override
        public void sendToClient(Object client, JsonValue message) {
            assert client != null;
            assert message != null;
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
    private final Deque<JsonValue> pending = new ArrayDeque<>();
    private boolean alive = true;

    BackendTester(Options options) {
        assert options != null;
        List<Class<? extends PluginInterface>> plugins = options.get(OptionsPlugin.PLUGIN_INTERFACE_CLASS);
        backend = new Backend(feedback, plugins);
    }

    void send(JsonObjectBuilder request) {
        assert request != null;
        send(request.build());
    }


    void send(JsonValue request) {
        assert request != null;
        backend.sendToBackend(backend, request);
    }

    int size() {
        return pending.size();
    }

    JsonValue popPending() {
        return pending.pop();
    }

    boolean alive() {
        return alive;
    }

}
