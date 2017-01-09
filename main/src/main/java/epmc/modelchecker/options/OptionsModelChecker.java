package epmc.modelchecker.options;

import java.util.Map;

import epmc.modelchecker.Engine;

/**
 * Options of model checker part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsModelChecker {
    /** Base name of resource bundle. */
    OPTIONS_MODEL_CHECKER,
    
    /** {@link Engine engine} used for analysis */
    ENGINE,
    /** List of property solvers to be used. */
    PROPERTY_SOLVER,
    /** List of all property solver classes. */
    PROPERTY_SOLVER_CLASS,
    /** constant definitions from command line ({@link Map Map&ltString,Object&gt;}) */
    CONST,
    // TODO following needs fixing
    PROPERTY_INPUT_TYPE,
    PROPERTY_CLASS,

    /** Model input type {@link String}. */
    MODEL_INPUT_TYPE,
}
