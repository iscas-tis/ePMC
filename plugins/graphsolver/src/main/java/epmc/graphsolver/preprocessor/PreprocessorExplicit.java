package epmc.graphsolver.preprocessor;

import epmc.error.EPMCException;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

public interface PreprocessorExplicit {
	String getIdentifier();
	
	void setObjective(GraphSolverObjectiveExplicit objective);
	
	GraphSolverObjectiveExplicit getObjective();
	
	boolean canHandle();
	
	void process() throws EPMCException;
}
