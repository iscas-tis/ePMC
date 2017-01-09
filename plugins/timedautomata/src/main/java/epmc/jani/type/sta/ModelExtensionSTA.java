package epmc.jani.type.sta;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsSTA;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.time.UtilTime;

public final class ModelExtensionSTA implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "sta";
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
	public void parseBefore() throws EPMCException {
		if (node instanceof ModelJANI) {
			ModelJANI model = (ModelJANI) node;
			UtilTime.addClockType(model);
		}

		// TODO Auto-generated method stub
		
	}

	@Override
	public void parseAfter() throws EPMCException {
		if (node instanceof Edge) {
			Edge edge = (Edge) node;
		}
	}

	@Override
	public void generate(JsonObjectBuilder generate) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsSTA.STA;
	}
}
