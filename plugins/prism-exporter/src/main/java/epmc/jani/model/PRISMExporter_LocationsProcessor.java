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

package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.prism.exporter.util.Range;

public class PRISMExporter_LocationsProcessor implements PRISMExporter_ProcessorStrict {

    private Locations locations = null;
    private String prefix = null;
    private Automaton automaton = null;

    @Override
    public PRISMExporter_ProcessorStrict setAutomaton(Automaton automaton) {
        assert automaton != null;

        this.automaton = automaton;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Locations; 

        locations = (Locations) obj;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert locations != null;

        // PRISM has no notion of locations, so it must be that there is exactly one location in order to be able to export the model
        ensure(PRISMExporter_ProcessorRegistrar.getAllowMultipleLocations() || locations.size() == 1, 
                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_MULTIPLE_LOCATIONS);

        StringBuilder prism = new StringBuilder();

        for (Location location : locations) {
            JANIComponentRegistrar.registerLocation(automaton, location);
            for (AssignmentSimple assignment : location.getTransientValueAssignmentsOrEmpty()) {
                JANIComponentRegistrar.registerStateRewardExpression(assignment.getRef(), assignment.getValue());
            }
        }

        if (locations.size() == 1) {
            for (Location location : locations) {
                TimeProgress timeProgress = location.getTimeProgress();
                if (timeProgress != null) {
                    prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(timeProgress)
                            .setPrefix(prefix)
                            .toPRISM())
                        .append("\n");
                }
            }
        } else {
            Range range = JANIComponentRegistrar.getLocationRange(automaton);
            prism.append(prefix)
                .append(JANIComponentRegistrar.getLocationName(automaton))
                .append(": [")
                .append(range.low)
                .append("..")
                .append(range.high)
                .append("]");
            if (!JANIComponentRegistrar.areInitialConditionsUsed() && automaton.getInitialLocations().size() == 1) {
                prism.append(" init ");
                for (Location location : automaton.getInitialLocations()) {
                    prism.append(JANIComponentRegistrar.getLocationIdentifier(automaton, location));
                }
            }
            prism.append(";\n");

            if (JANIComponentRegistrar.isTimedModel()) {
                prism.append("\n")
                    .append(prefix)
                    .append("invariant\n");
                boolean remaining = false;
                for (Location location : locations) {
                    TimeProgress timeProgress = location.getTimeProgress();
                    if (timeProgress != null) {
                        if (remaining) {
                            prism.append(prefix)
                                .append(PRISMExporter_ModelJANIProcessor.INDENT)
                                .append("&\n");
                        } else {
                            remaining = true;
                        }
                        prism.append(prefix)
                            .append(PRISMExporter_ModelJANIProcessor.INDENT)
                            .append("(")
                            .append(JANIComponentRegistrar.getLocationName(automaton))
                            .append("=")
                            .append(JANIComponentRegistrar.getLocationIdentifier(automaton, location))
                            .append(" => (")
                            .append(PRISMExporter_ProcessorRegistrar.getProcessor(timeProgress.getExp())
                                .toPRISM())
                            .append("))\n");
                    }
                }
                prism.append(prefix)
                    .append("endinvariant\n");
            }
        }
        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert locations != null;

        for (Location location : locations) {
            PRISMExporter_ProcessorRegistrar.getProcessor(location)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert locations != null;

        boolean usesTransient = false;
        for (Location location : locations) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(location)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
