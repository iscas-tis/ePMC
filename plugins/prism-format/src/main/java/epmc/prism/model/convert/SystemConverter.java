package epmc.prism.model.convert;

import epmc.error.EPMCException;
import epmc.jani.model.ModelJANI;
import epmc.prism.model.ModelPRISM;

public interface SystemConverter {
	void setPRISMModel(ModelPRISM modelPrism);
	
	void setJANIModel(ModelJANI modelJani);
	
	void convert() throws EPMCException;
}
