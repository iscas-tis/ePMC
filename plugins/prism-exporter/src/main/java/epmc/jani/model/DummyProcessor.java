package epmc.jani.model;

import epmc.error.EPMCException;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;

public class DummyProcessor implements JANI2PRISMProcessorStrict {

	@Override
	public void setElement(Object obj) throws EPMCException {
	}


	@Override
	public StringBuilder toPRISM() throws EPMCException {
		return new StringBuilder("Dummy processor");
	}
}
