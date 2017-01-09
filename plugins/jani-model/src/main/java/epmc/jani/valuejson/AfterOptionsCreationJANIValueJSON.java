package epmc.jani.valuejson;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationJANIValueJSON implements AfterOptionsCreation {
	private final static String IDENTIFIER = "jani-valuejson";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		List<Class<? extends ValueJSON>> valueJsonClasses = new ArrayList<>();
		valueJsonClasses.add(0, ValueJSONGeneral.class);
		valueJsonClasses.add(0, ValueJSONBoolean.class);
		valueJsonClasses.add(0, ValueJSONInt.class);
		valueJsonClasses.add(0, ValueJSONDouble.class);
		options.set(OptionsJANIValueJSON.JANI_VALUEJSON_CLASS, valueJsonClasses);
	}
}
