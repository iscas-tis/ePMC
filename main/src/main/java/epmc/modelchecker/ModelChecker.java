package epmc.modelchecker;

import static epmc.error.UtilError.fail;

import java.io.Closeable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.LowLevel;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.error.ProblemsModelChecker;
import epmc.modelchecker.messages.MessagesModelChecker;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.Util;
import epmc.value.Value;

//TODO could add some functionality on the server to allow the user to select
//messages which are more or less important

// TODO complete documentation

// TODO could optionally construct graph for all properties together

/**
 * Class to check the properties of a model.
 * The model checker will check all properties obtained by
 * {@link Model#getPropertyList()}..
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelChecker implements Closeable {
    /** String "unchecked". */
    private final static String UNCHECKED = "unchecked";
    /** Model to be checked. */
    private final Model model;
    /** Available solvers. */
    private final List<Class<? extends PropertySolver>> solvers;
    /** Model checking engine to use. */
    private final Engine engine;
    /** Current low-level instantiation of the model used. */
    private LowLevel lowLevel;
    /** Whether the model checker has already been closed. */
    private boolean closed;

    /**
     * Prepare model checker for the given model.
     * The model parameter must not be {@code null}.
     * 
     * @param model model to create model checker for
     * @throws EPMCException thrown in case of problems
     */
    public ModelChecker(Model model) throws EPMCException {
        assert model != null;
        this.model = model;
        Options options = model.getContextValue().getOptions();
        engine = UtilOptions.getSingletonInstance(options,
                OptionsModelChecker.ENGINE);
        solvers = preparePropertySolvers(options);
    }

    /**
     * Prepare list of solver classes.
     * The list will consist of the solvers chosen to be used by
     * {@link OptionsModelChecker#PROPERTY_SOLVER}
     * from
     * {@link OptionsModelChecker#PROPERTY_CLASS}.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to read solver classes from
     * @return list of solver classes chosen to be used
     */
    private static List<Class<? extends PropertySolver>> preparePropertySolvers(Options options) {
        assert options != null;
        Collection<String> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER);
        List<Class<? extends PropertySolver>> result = new ArrayList<>();
        Map<String,Class<PropertySolver>> external = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        for (Entry<String, Class<PropertySolver>> entry : external.entrySet()) {
            if (solvers.contains(entry.getKey())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    
    // TODO currently used my multi objective plugin, but should be private
    /**
     * Obtain solver for given property and state set.
     * If the state set is {@code null}, values for the initial states shall be
     * computed.
     * First solver from the list of solvers chosen to be used which can solve
     * the combination is returned.
     * 
     * @param property property to obtain solver for
     * @param states state set to obtain values for, or {@code null}
     * @return solver for given property and state set
     * @throws EPMCException
     */
    public PropertySolver getSolverFor(Expression property, StateSet states)
            throws EPMCException {
        assert property != null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return solver;
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
        return null;
    }
    
    /**
     * Get model the model checker is used for.
     * 
     * @return model the model checker is used for
     */
    public Model getModel() {
        return model;
    }

    /**
     * Check all properties of the model.
     */
    public void check() {
        long time = System.nanoTime();
        getLog().send(MessagesModelChecker.MODEL_CHECKING);
        for (RawProperty property : model.getPropertyList().getRawProperties()) {
            String propString;
            propString = property.getDefinition();
            if (propString == null) {
                propString = property.getName();
            }
            getLog().send(MessagesModelChecker.ANALYSING_PROPERTY, propString);
            Expression expression = model.getPropertyList().getParsedProperty(property);
            ModelCheckerResult propRes = null;
            try {
                propRes = new ModelCheckerResult(property, checkProperty(expression));
            } catch (EPMCException e) {
                propRes = new ModelCheckerResult(property, e);
            }
            getLog().send(propRes);
        }
        time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
        getLog().send(MessagesModelChecker.MODEL_CHECKING_DONE, time);
    }

    /**
     * Prepare low-level model usable to check the given property.
     * Low-level models are property-dependent, because for efficiency, certain
     * graph, node, and edge properties not needed for a given property might
     * not be attached to the low-level model if not explicitly requested.
     * The property parameter must not be {@code null}.
     * 
     * @param property property to construct low-level model for
     * @return low-level model suitable for checking the given property
     * @throws EPMCException thrown in case of problems
     */
    private LowLevel prepareLowLevel(Expression property) throws EPMCException {
        assert property != null;
        PropertySolver solver = getSolverFor(property, null);
        Set<Object> graphProperties = solver.getRequiredGraphProperties();
        Set<Object> nodeProperties = solver.getRequiredNodeProperties();
        Set<Object> edgeProperties = solver.getRequiredEdgeProperties();
        Options options = model.getContextValue().getOptions();
        Engine engine = UtilOptions.getSingletonInstance(options,
                OptionsModelChecker.ENGINE);
        return model.newLowLevel(engine, graphProperties, nodeProperties, edgeProperties);
    }
    
    /**
     * Check the given property.
     * In contrast to {@link #check(Expression, StateSet)},
     * this method shall be used to each property of
     * {@link Model#getPropertyList()},
     * not for solving subformulas of the property under consideration.
     * This method already returns a single value, rather than a map from states
     * to values.
     * The method also prepares the low-level model to be used for the given
     * property.
     * The property parameter must not be {@code null}.
     * 
     * @param property property to be checked
     * @return value obtained for the given property
     * @throws EPMCException thrown in case of problems
     */
    private Value checkProperty(Expression property) throws EPMCException {
        assert property != null;
        if (lowLevel != null) {
            lowLevel.close();
        }
        lowLevel = prepareLowLevel(property);
        
        StateMap result = check(property, lowLevel.newInitialStateSet());
        return result.subsumeResult(lowLevel.newInitialStateSet());
    }

    /**
     * Checks given property.
     * This method is intended to check properties obtained by
     * {@link Model#getPropertyList()}
     * as well as subformulas of such properties.
     * In contrast to {@link #checkProperty(Expression)}, this property does
     * not prepare the low-level model for the property and returns values
     * for each state it should compute results for.
     * The property and states parameters must not be {@code null}.
     * 
     * @param property property to be checked
     * @param states set of states to check property for
     * @return result of checking property for states specified
     * @throws EPMCException thrown in case of problems
     */
    public StateMap check(Expression property, StateSet states)
            throws EPMCException {
        assert property != null;
        assert states != null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return solver.solve();
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
        return null;
    }

    // TODO the following three methods should be subsumed.
    // Therefore, we would need an auxiliary class containing graph, edge, and
    // node properties.
    
    /**
     * Get graph properties required to check given property.
     * Low-level models have to be constructed in such a way that they have the
     * required graph properties available for the given solver.
     * This method returns the properties required by the solver that will be
     * used to check the given property.
     * The property parameter must not be {@code null}.
     * If the states parameter is {@code null}, this means that the property
     * shall be checked for the set of initial states.
     * 
     * @param property property to be checked
     * @param states states for which result shall be obtained
     * @return set of graph properties required
     * @throws EPMCException
     */
    public Set<Object> getRequiredGraphProperties(Expression property, StateSet states) throws EPMCException {
        assert property != null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return solver.getRequiredGraphProperties();
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
        return null;
    }

    public Set<Object> getRequiredNodeProperties(Expression property, StateSet states) throws EPMCException {
        assert property != null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return solver.getRequiredNodeProperties();
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
        return null;
    }

    public Set<Object> getRequiredEdgeProperties(Expression property, StateSet states) throws EPMCException {
        assert property != null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return solver.getRequiredEdgeProperties();
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
        return null;
    }

    /**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
        Options options = model.getContextValue().getOptions();
        return options.get(OptionsMessages.LOG);
    }
    
    /**
     * Get engine used for model checking.
     * 
     * @return engine used for model checking
     */
    public Engine getEngine() {
        return engine;
    }

    public void ensureCanHandle(Expression property, StateSet states)
            throws EPMCException {
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return;
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, property);
    }

    /**
     * Get low-level model used for model checking.
     * 
     * @return low-level model used for model checking
     */
    @SuppressWarnings(UNCHECKED)
    public <T extends LowLevel> T getLowLevel() {
        return (T) lowLevel;
    }
    
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (lowLevel != null) {
            lowLevel.close();
            lowLevel = null;
        }
    }
}