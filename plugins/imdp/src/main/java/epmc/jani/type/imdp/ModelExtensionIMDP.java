package epmc.jani.type.imdp;

import static epmc.error.UtilError.ensure;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.imdp.value.OperatorInterval;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.type.mdp.ProblemsJANIMDP;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;

public final class ModelExtensionIMDP implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "imdp";
	private JANINode node;
	private ModelJANI model;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		this.model = model;
		TypeWeightTransition.set(TypeInterval.get(model.getContextValue()));
	}

	@Override
	public void setNode(JANINode node) throws EPMCException {
		this.node = node;
	}

	@Override
	public void setJsonValue(JsonValue value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parseBefore() throws EPMCException {
		JANIOperators operators = model.getJANIOperators();
		assert operators != null;
		operators.add().setJANI(OperatorInterval.IDENTIFIER)
			.setEPMC(OperatorInterval.IDENTIFIER)
			.setArity(2).build();
	}

	@Override
	public void parseAfter() throws EPMCException {
		if (node instanceof Edge) {
			Edge edge = (Edge) node;
			ensure(edge.getRate() == null, ProblemsJANIMDP.JANI_MDP_EDGE_FORBIDS_RATE);
		}
		if (node instanceof Location) {
			Location location = (Location) node;
			ensure(location.getTimeProgress() == null, ProblemsJANIMDP.JANI_MDP_DISALLOWED_TIME_PROGRESSES);
		}
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsIMDP.IMDP;
	}
}
