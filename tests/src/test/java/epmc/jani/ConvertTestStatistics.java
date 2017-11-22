/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;

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
