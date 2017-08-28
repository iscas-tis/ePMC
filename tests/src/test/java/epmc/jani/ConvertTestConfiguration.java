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

import static epmc.graph.TestHelperGraph.exploreModel;
import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.jani.model.OptionsJANIModel;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.TestHelper.LogType;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.util.StopWatch;

public final class ConvertTestConfiguration {
    /** Location of plugin directory in file system. */
    //    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    private String modelName;
    private final Map<String,Object> constants = new LinkedHashMap<>();
    private boolean prismFlatten;
    private boolean explorePRISM;
    private boolean exploreJANI;
    private boolean exploreJANICloned;
    private String plugin;

    public ConvertTestConfiguration setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public ConvertTestConfiguration putConstant(String key, Object value) {
        assert key != null;
        assert value != null;
        constants.put(key, value);
        return this;
    }

    public ConvertTestConfiguration setPrismFlatten(boolean prismFlatten) {
        this.prismFlatten = prismFlatten;
        return this;
    }

    public ConvertTestConfiguration setExplorePRISM(boolean explorePRISM) {
        this.explorePRISM = explorePRISM;
        return this;
    }

    public ConvertTestConfiguration setExploreJANI(boolean exploreJANI) {
        this.exploreJANI = exploreJANI;
        return this;
    }

    public ConvertTestConfiguration setExploreJANI() {
        this.exploreJANI = true;
        return this;
    }

    public ConvertTestConfiguration setExploreJANICloned(boolean exploreJANICloned) {
        this.exploreJANICloned = exploreJANICloned;
        return this;
    }

    public ConvertTestConfiguration setPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public ConvertTestConfiguration setExploreAll() {
        setExplorePRISM(true);
        setExploreJANI(true);
        setExploreJANICloned(true);
        return this;
    }

    public ConvertTestStatistics run() {
        ConvertTestStatistics result = new ConvertTestStatistics();
        assert modelName != null;
        Path path = Paths.get(modelName);
        String base = path.getFileName().toString();
        String modelId = base.substring(0, base.lastIndexOf('.'));
        result.setModelName(modelId);
        Options options = ConvertTestConfiguration.prepareJANIOptions(plugin);
        options.set(OptionsJANIModel.JANI_FIX_DEADLOCKS, true);
        options.set(TestHelper.PRISM_FLATTEN, prismFlatten);
        if (constants != null) {
            options.set(OptionsModelChecker.CONST, constants);
            result.setConstants(constants);
        }
        StopWatch timeLoadPRISM = new StopWatch(true);
        ModelJANIConverter modelPRISM = (ModelJANIConverter) loadModel(options, modelName);
        result.put(ConvertTestStatistics.TIME_LOAD_PRISM, timeLoadPRISM.getTime());
        result.setPRISMModel(modelPRISM);
        StopWatch timeConvertJANI = new StopWatch(true);
        ModelJANI modelJANI = modelPRISM.toJANI(false);
        result.put(ConvertTestStatistics.TIME_CONVERT_JANI, timeConvertJANI.getTime());
        result.setJaniModel(modelJANI);
        StopWatch timeCloneJANI = new StopWatch(true);
        ModelJANI modelJANICloned = modelJANI != null ? modelJANI.clone() : null;
        result.put(ConvertTestStatistics.TIME_CLONE_JANI, timeCloneJANI.getTime());
        result.setJaniClonedModel(modelJANICloned);
        StopWatch timeExplorePRISM = new StopWatch(true);
        ExploreStatistics statisticsPRISM = 
                explorePRISM ? exploreModel(modelPRISM) : null;
                result.put(ConvertTestStatistics.TIME_EXPLORE_PRISM, timeExplorePRISM.getTime());
                StopWatch timeExploreJANI = new StopWatch(true);
                ExploreStatistics statisticsJANI =
                        exploreJANI ? exploreModel(modelJANI) : null;
                        result.put(ConvertTestStatistics.TIME_EXPLORE_JANI, timeExploreJANI.getTime());
                        StopWatch timeExploreJANIClone = new StopWatch(true);
                        ExploreStatistics statisticsJANICloned =
                                exploreJANICloned ? exploreModel(modelJANI) : null;
                                result.put(ConvertTestStatistics.TIME_EXPLORE_JANI_CLONE, timeExploreJANIClone.getTime());
                                ExploreStatistics statisticsCompare = null;
                                if (statisticsPRISM != null) {
                                    statisticsCompare = statisticsPRISM;
                                } else if (statisticsJANI != null) {
                                    statisticsCompare = statisticsJANI;
                                } else if (statisticsJANICloned != null) {
                                    statisticsCompare = statisticsJANICloned;
                                }
                                if (statisticsCompare != null) {
                                    result.put(ConvertTestStatistics.NUM_STATES, statisticsCompare.getNumStates());
                                }
                                if (statisticsCompare != null && statisticsPRISM != null) {
                                    assertEquals(statisticsCompare.getNumStates(),
                                            statisticsPRISM.getNumStates());
                                }
                                if (statisticsCompare != null && statisticsJANI != null) {
                                    assertEquals(statisticsCompare.getNumStates(),
                                            statisticsJANI.getNumStates());
                                }
                                if (statisticsCompare != null && statisticsJANICloned != null) {
                                    assertEquals(statisticsCompare.getNumStates(),
                                            statisticsJANICloned.getNumStates());
                                }
                                return result;
    }

    /**
     * Prepare options including loading JANI plugin.
     * @param additionalPlugin 
     * 
     * @return options usable for JANI model analysis
     */
    public static Options prepareJANIOptions(String additionalPlugin) {
        Options options = UtilOptionsEPMC.newOptions();
        //	    String plugin = PLUGIN_DIR;
        //    if (additionalPlugin != null) {
        //   	plugin = plugin + COMMA + additionalPlugin;
        //   }
        //   options.set(OptionsPlugin.PLUGIN, plugin);

        prepareOptions(options, LogType.TRANSLATE, ModelJANI.IDENTIFIER);
        return options;
    }
}
