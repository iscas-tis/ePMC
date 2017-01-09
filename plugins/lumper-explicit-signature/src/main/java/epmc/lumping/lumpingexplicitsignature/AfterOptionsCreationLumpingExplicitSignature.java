package epmc.lumping.lumpingexplicitsignature;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationLumpingExplicitSignature implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-jani";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> lumpers = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
		assert lumpers != null;
		lumpers.put(LumperExplicitSignatureStrong.IDENTIFIER, LumperExplicitSignatureStrong.class);
		lumpers.put(LumperExplicitSignatureWeakCTMC.IDENTIFIER, LumperExplicitSignatureWeakCTMC.class);
	}

}
