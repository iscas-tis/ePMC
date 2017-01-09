package epmc.jani.model;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.value.ContextValue;
import epmc.value.Operator;

public final class JANIOperators {
	private Map<String,JANIOperator> janiToOperator = new LinkedHashMap<>();
	private Map<String,JANIOperator> iscasMCToOperator = new LinkedHashMap<>();
	
	public JANIOperator.Builder add() {
		JANIOperator.Builder builder = new JANIOperator.Builder();
		builder.setJANIOperators(this);
		return builder;
	}
	
	void add(JANIOperator operator) {
		assert operator != null;
		assert !janiToOperator.containsKey(operator.getJANI()) : operator.getJANI();
		assert !iscasMCToOperator.containsKey(operator.getEPMC());
		janiToOperator.put(operator.getJANI(), operator);
		iscasMCToOperator.put(operator.getEPMC(), operator);
	}
	
	public Operator getOperator(ContextValue context, String jani) {
		assert context != null;
		assert jani != null;
		String iscasMCName = janiToOperator.get(jani).getJANI();
		return context.getOperator(iscasMCName);
	}
	
	public String janiToEPMCName(String jani) {
		assert jani != null;
		assert janiToOperator.containsKey(jani);
		return janiToOperator.get(jani).getEPMC();
	}
	
	public boolean containsOperatorByJANI(String jani) {
		assert jani != null;
		return janiToOperator.containsKey(jani);
	}
	
	public JANIOperator getOperatorByJANI(String jani) {
		assert jani != null;
		assert janiToOperator.containsKey(jani);
		return janiToOperator.get(jani);
	}

	public JANIOperator getOperator(Operator operator) {
		assert operator != null;
		assert iscasMCToOperator.containsKey(operator.getIdentifier()) :
			operator.getIdentifier();
		return iscasMCToOperator.get(operator.getIdentifier());
	}
}
