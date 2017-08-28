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

package epmc.modelchecker;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.RawProperty;

// TODO continue documentation

/**
 * Class collecting all results of a model checking run.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelCheckerResults {
    /** String "Results:\n". */
    private final static String RESULTS = "Results:\n";
    private final static String TWOSPACE = "  ";
    private final static String COLONSPACE = ": ";
    private final static String ENDLINE = "\n";
    private Object commonResult;
    private final Map<RawProperty,Object> results = new LinkedHashMap<>();
    private final Map<RawProperty, Scheduler> schedulers = new LinkedHashMap<>();
    private final Map<RawProperty, LowLevel> lowLevels = new LinkedHashMap<>();

    public void set(ModelCheckerResult result) {
        assert result != null;
        if (result.getProperty() == null) {
            this.commonResult = result.getResult();
        } else {
            results.put(result.getProperty(), result.getResult());
            Scheduler scheduler = result.getScheduler();
            if (scheduler != null) {
                schedulers.put(result.getProperty(), scheduler);
            }
            LowLevel lowLevel = result.getLowLevel();
            if (lowLevel != null) {
                lowLevels.put(result.getProperty(), lowLevel);
            }
        }
    }

    public void set(Object commonResult) {
        assert commonResult != null;
        this.commonResult = commonResult;
    }

    public Object getCommonResult() {
        return commonResult;
    }

    public String getCommonResultString() {
        if (commonResult == null) {
            return null;
        }
        return commonResult.toString();
    }

    public Object get(RawProperty property) {
        assert property != null;
        return results.get(property);
    }

    public String getString(RawProperty property) {
        assert property != null;
        Object get = get(property);
        if (get == null) {
            return null;
        }
        return get.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(RESULTS);
        for (Entry<RawProperty,Object> entry : results.entrySet()) {
            builder.append(TWOSPACE);
            builder.append(entry.getKey().getDefinition());
            builder.append(COLONSPACE);
            builder.append(entry.getValue());
            builder.append(ENDLINE);
        }
        return builder.toString();
    }

    public Collection<RawProperty> getProperties() {
        return results.keySet();
    }

    public Object getResult(RawProperty property) {
        assert property != null;
        return results.get(property);
    }

    public Scheduler getScheduler(RawProperty property) {
        assert property != null;
        return schedulers.get(property);
    }

    public LowLevel getLowLevel(RawProperty property) {
        assert property != null;
        return lowLevels.get(property);
    }

    public void clear() {
        this.commonResult = null;
        this.results.clear();
    }
}
