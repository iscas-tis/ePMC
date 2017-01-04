package epmc.jani.interaction.communication;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
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
	
	BackendTester(Options options) throws EPMCException {
		assert options != null;
		backend = new Backend(options, feedback);
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
