package epmc.jani.dd;

import java.io.Closeable;

import epmc.dd.DD;
import epmc.dd.VariableDD;

final class VariableValid implements Cloneable, Closeable {
	private boolean closed;
	private VariableDD variable;
	private DD valid;

	void setVariable(VariableDD variable) {
		this.variable = variable;
	}
	
	VariableDD getVariable() {
		return variable;
	}
	
	void setValid(DD valid) {
		this.valid = valid;
	}
	
	DD getValid() {
		return valid;
	}
	
	@Override
	protected VariableValid clone() {
		VariableValid clone = new VariableValid();
		clone.variable = variable;
		clone.valid = valid.clone();
		return clone;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		valid.dispose();
	}
}
