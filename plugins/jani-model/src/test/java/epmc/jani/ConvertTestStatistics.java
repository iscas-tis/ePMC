package epmc.jani;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.options.Options;
import epmc.value.ContextValue;

public final class ConvertTestStatistics {
	private final static String SPACE = " ";
	private final static String NEWLINE = "\n";

	private final static String MODEL_NAME = "model-name";
	final static String NUM_STATES = "num-states";
	final static String TIME_LOAD_PRISM = "time-load-prism";
	final static String TIME_CONVERT_JANI = "time-convert-jani";
	final static String TIME_CLONE_JANI = "time-clone-jani";
	final static String TIME_EXPLORE_PRISM = "time-explore-prism";
	final static String TIME_EXPLORE_JANI = "time-explore-jani";
	final static String TIME_EXPLORE_JANI_CLONE = "time-explore-jani-clone";
	final static String CONST = "const";

	private String modelName;
	private Map<String, Object> constants;
	private Map<String,Object> data = new LinkedHashMap<>();
	private ModelJANIConverter prismModel;
	private ModelJANI janiModel;
	private ModelJANI janiClonedModel;
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public void setPRISMModel(ModelJANIConverter modelPRISM) {
		this.prismModel = modelPRISM;
	}
	
	public ModelJANIConverter getPrismModel() {
		return prismModel;
	}
	
	public void setJaniModel(ModelJANI janiModel) {
		this.janiModel = janiModel;
	}
	
	public ModelJANI getJaniModel() {
		return janiModel;
	}
	
	public void setJaniClonedModel(ModelJANI janiClonedModel) {
		this.janiClonedModel = janiClonedModel;
	}
	
	public ModelJANI getJaniClonedModel() {
		return janiClonedModel;
	}
	
	public void put(String key, Object value) {
		data.put(key, value);
	}
	
	public Options getOptions() {
		if (prismModel != null) {
			return prismModel.getContextValue().getOptions();
		} else if (janiModel != null) {
			return prismModel.getContextValue().getOptions();
		} else if (janiClonedModel != null) {
			return prismModel.getContextValue().getOptions();
		}
		return null;
	}

	public ContextValue getContextValue() {
		if (prismModel != null) {
			return prismModel.getContextValue();
		} else if (janiModel != null) {
			return prismModel.getContextValue();
		} else if (janiClonedModel != null) {
			return prismModel.getContextValue();
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(MODEL_NAME)
		.append(SPACE)
		.append(modelName)
		.append(NEWLINE);
		for (Entry<String, Object> entry : constants.entrySet()) {
			builder.append(CONST)
			.append(SPACE)
			.append(entry.getKey())
			.append(SPACE)
			.append(entry.getValue())
			.append(NEWLINE);
		}
		for (Entry<String, Object> entry : data.entrySet()) {
			builder.append(entry.getKey())
			.append(SPACE)
			.append(entry.getValue())
			.append(NEWLINE);
		}
		return builder.toString();
	}

	public void setConstants(Map<String, Object> constants) {
		this.constants = constants;
	}
}
