package epmc.jani.type.ctmdp;

import static epmc.error.UtilError.ensure;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMDP;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;

public final class ModelExtensionCTMDP implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "ctmdp";
	private JANINode node;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		// TODO Auto-generated method stub
		
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
	public Semantics getSemantics() {
		return SemanticsCTMDP.CTMDP;
	}
	
	@Override
	public void parseAfter() throws EPMCException {
		if (node instanceof Edge) {
			Edge edge = (Edge) node;
			ensure(edge.getRate() != null, ProblemsJANICTMDP.JANI_CTMDP_EDGE_REQUIRES_RATE);
		}
		if (node instanceof Location) {
			Location location = (Location) node;
			ensure(location.getTimeProgress() == null, ProblemsJANICTMDP.JANI_CTMDP_DISALLOWED_TIME_PROGRESSES);
		}

		// TODO Auto-generated method stub
	}
}
