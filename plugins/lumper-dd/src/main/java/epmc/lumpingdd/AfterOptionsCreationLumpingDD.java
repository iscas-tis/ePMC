package epmc.lumpingdd;

import java.util.Collection;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Option;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationLumpingDD implements AfterOptionsCreation {
	public static String IDENTIFIER = "after-options-creation-lumping-dd";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> lumpersDD = options.get(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);
		assert lumpersDD != null;
		lumpersDD.put(StrongSignature.LumperDDSignatureStrong.IDENTIFIER, StrongSignature.LumperDDSignatureStrong.class);
		lumpersDD.put(CTMCWeakSignature.LumperDDSignatureCTMCWeak.IDENTIFIER, CTMCWeakSignature.LumperDDSignatureCTMCWeak.class);
		lumpersDD.put(DTMCWeakSignature.LumperDDSignatureDTMCWeak.IDENTIFIER, DTMCWeakSignature.LumperDDSignatureDTMCWeak.class);
		lumpersDD.put(MDPOneStepSignature.LumperDDSignatureMDPOneStep.IDENTIFIER, MDPOneStepSignature.LumperDDSignatureMDPOneStep.class);
		
		Option lumpersString = options.getOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
		assert lumpersString.getDefault() instanceof Collection<?>;
		@SuppressWarnings("unchecked")
		Collection<String> lumperDefault = (Collection<String>) lumpersString.getDefault();
		lumperDefault.addAll(lumpersDD.keySet());
	}

}
