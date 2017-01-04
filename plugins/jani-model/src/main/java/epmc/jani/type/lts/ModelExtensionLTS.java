package epmc.jani.type.lts;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsLTS;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.JANINode;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;

public class ModelExtensionLTS implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "lts";

	private ModelJANI model;
	private JANINode node;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		assert this.model == null;
		assert model != null;
		this.model = model;
	}

	@Override
	public void setNode(JANINode node) throws EPMCException {
		this.node = node;
	}

	@Override
	public void parseAfter() throws EPMCException {
		if (node instanceof Destinations) {
			Destinations destinations = (Destinations) node;
			ensure(destinations.size() == 1, ProblemsJANILTS.JANI_LTS_ONLY_ONE_DESTINATIONS);
		}
		if (node instanceof Destination) {
			Destination destination = (Destination) node;
			ensure(destination.getProbability() == null, ProblemsJANILTS.JANI_LTS_NO_PROBABILITIES);
		}
		if (node instanceof Location) {
			Location location = (Location) node;
			ensure(location.getTimeProgress() == null, ProblemsJANILTS.JANI_LTS_DISALLOWED_TIME_PROGRESSES);
		}
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsLTS.LTS;
	}
}
