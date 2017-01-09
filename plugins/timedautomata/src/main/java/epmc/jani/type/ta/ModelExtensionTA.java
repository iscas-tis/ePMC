package epmc.jani.type.ta;

import static epmc.error.UtilError.ensure;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsTA;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.time.UtilTime;

public final class ModelExtensionTA implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "ta";
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
		if (node instanceof Destinations) {
			Destinations destinations = (Destinations) node;
			ensure(destinations.size() == 1, ProblemsJANITA.JANI_TA_ONLY_ONE_DESTINATIONS);
		}
		if (node instanceof Destination) {
			Destination destination = (Destination) node;
			ensure(destination.getProbability() == null, ProblemsJANITA.JANI_TA_NO_PROBABILITIES);
		}
	}

	@Override
	public void generate(JsonObjectBuilder generate) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsTA.TA;
	}
}
