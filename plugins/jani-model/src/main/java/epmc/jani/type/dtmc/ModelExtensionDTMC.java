package epmc.jani.type.dtmc;

import static epmc.error.UtilError.ensure;

import javax.json.JsonObjectBuilder;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMCStandard;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;

public class ModelExtensionDTMC implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "dtmc";

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
		if (node instanceof Edge) {
			Edge edge = (Edge) node;
			ensure(edge.getRate() == null, ProblemsJANIDTMC.JANI_DTMC_EDGE_FORBIDS_RATE);
		}
		if (node instanceof Location) {
			Location location = (Location) node;
			ensure(location.getTimeProgress() == null, ProblemsJANIDTMC.JANI_DTMC_DISALLOWED_TIME_PROGRESSES);
		}
	}

	@Override
	public void generate(JsonObjectBuilder generate) throws EPMCException {
		assert generate != null;
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsDTMCStandard.DTMC;
	}

}
