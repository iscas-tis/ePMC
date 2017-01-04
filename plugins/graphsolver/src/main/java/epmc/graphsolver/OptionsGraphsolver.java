package epmc.graphsolver;

import java.util.Collection;

public enum OptionsGraphsolver {
	OPTIONS_GRAPHSOLVER,
	GRAPHSOLVER_CATEGORY,

    GRAPHSOLVER_PREPROCESSOR_EXPLICIT,
    GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS,
	
    /** {@link Collection} of {@link String} of graph solver identifiers to try to solve graph problems */
    GRAPHSOLVER_SOLVER,
    GRAPHSOLVER_SOLVER_CLASS,

    /** whether to perform lumping before calling a graph solver */
    GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING,

    GRAPHSOLVER_LUMPER_EXPLICIT_CLASS,
    GRAPHSOLVER_LUMPER_EXPLICIT,

    GRAPHSOLVER_LUMPER_DD,
    GRAPHSOLVER_DD_LUMPER_CLASS,
}
