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

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.prism.exporter.processor.Range;

public class LocationsProcessor implements JANI2PRISMProcessorStrict {

	private Locations locations = null;
	private boolean withInitialValue = false;
	private String prefix = null;
	private Automaton automaton = null;
	
	@Override
	public void setWithInitialValue(boolean withInitialValue) {
		this.withInitialValue = withInitialValue;
	}

	@Override
	public void setAutomaton(Automaton automaton) {
		assert automaton != null;
		
		this.automaton = automaton;
	}
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Locations; 
		
		locations = (Locations) obj;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert locations != null;
		
		// PRISM has no notion of locations, so it must be that there is exactly one location in order to be able to export the model
		ensure(ProcessorRegistrar.getAllowMultipleLocations() || locations.size() == 1, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_MULTIPLE_LOCATIONS);
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		for (Location location : locations) {
			JANIComponentRegistrar.registerLocation(automaton, location);
			Assignments assignments = location.getTransientValueAssignmentsOrEmpty();
			for (AssignmentSimple assignment : assignments) {
				Variable reward = assignment.getRef();
				Expression expression = assignment.getValue();
				JANIComponentRegistrar.registerStateRewardExpression(reward, expression);
			}
		}
		
		if (locations.size() == 1) {
			for (Location location : locations) {
				TimeProgress timeProgress = location.getTimeProgress();
				if (timeProgress != null) {
					processor = ProcessorRegistrar.getProcessor(timeProgress);
					processor.setPrefix(prefix);
					prism.append(processor.toPRISM().toString()).append("\n");
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
			if (withInitialValue && automaton.getInitialLocations().size() == 1) {
				prism.append(" init ");
				for (Location location : automaton.getInitialLocations()) {
					prism.append(JANIComponentRegistrar.getLocationIdentifier(automaton, location));
				}
			}
			prism.append(";\n");
			
			if (JANIComponentRegistrar.isTimedModel()) {
				prism.append(prefix).append("invariant\n");
				boolean remaining = false;
				for (Location location : locations) {
					TimeProgress timeProgress = location.getTimeProgress();
					if (timeProgress != null) {
						processor = ProcessorRegistrar.getProcessor(timeProgress.getExp());
						if (remaining) {
							prism.append(prefix).append(prefix).append("&\n");
						} else {
							remaining = true;
						}
						prism.append(prefix).append(prefix)
							 .append("(")
							 .append(JANIComponentRegistrar.getLocationName(automaton))
							 .append("=")
							 .append(JANIComponentRegistrar.getLocationIdentifier(automaton, location))
							 .append(" => (")
							 .append(processor.toPRISM().toString())
							 .append("))\n");
					}
				}
				prism.append(prefix).append("endinvariant\n");
			}
		}
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert locations != null;
		
		for (Location location : locations) {
			ProcessorRegistrar.getProcessor(location).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert locations != null;
		
		boolean usesTransient = false;
		for (Location location : locations) {
			usesTransient |= ProcessorRegistrar.getProcessor(location).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
