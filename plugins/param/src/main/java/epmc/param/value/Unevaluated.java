package epmc.param.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;

public final class Unevaluated {
	private final ContextValuePARAM contextValuePARAM;
	private final Operator operator;
	private final ValueFunction[] parameters;
	private final ValueReal[] evalValues;
	
	Unevaluated(ContextValuePARAM contextValuePARAM, Operator operator,
			ValueFunction... parameters) {
		assert parameters != null;
		this.contextValuePARAM = contextValuePARAM;
		this.operator = operator;
		this.parameters = new ValueFunction[parameters.length];
		for (int paramNr = 0; paramNr < parameters.length; paramNr++) {
			this.parameters[paramNr] = UtilValue.clone(parameters[paramNr]);
		}
		this.evalValues = new ValueReal[parameters.length];
		for (int entryNr = 0; entryNr < parameters.length; entryNr++) {
			this.evalValues[entryNr] = contextValuePARAM.getTypeReal().newValue();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(operator);
		result.append("(");
		for (int paramNr = 0; paramNr < parameters.length; paramNr++) {
			result.append(parameters[paramNr]);
			if (paramNr < parameters.length - 1) {
				result.append(",");
			}
		}
		result.append(")");
		return result.toString();
	}
	
	@Override
	public int hashCode() {
        int hash = 0;
        hash = this.operator.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = Arrays.hashCode(this.parameters) + (hash << 6) + (hash << 16) - hash;
        return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof Unevaluated)) {
			return false;
		}
		Unevaluated other = (Unevaluated) obj;
		if (this.operator != other.operator) {
			return false;
		}
		if (!Arrays.equals(this.parameters, other.parameters)) {
			return false;
		}
		return true;
	}
	
	public void evaluate(Value result, Point point) throws EPMCException {
		assert result != null;
		assert point != null;
		point.adjustNumParameters();
//		int begin = contextValuePARAM.getParameterNumber(this) + 1;
		for (int entryNr = 0; entryNr < this.parameters.length; entryNr++) {
			this.parameters[entryNr].evaluate(this.evalValues[entryNr], point);
		}
		this.operator.apply(result, this.evalValues);
		System.out.println(operator + "  " + Arrays.toString(parameters) + "  " + result + " " + Arrays.toString(this.evalValues) + " " + point);
	}
	
	public ContextValuePARAM getContextValuePARAM() {
		return contextValuePARAM;
	}
}
