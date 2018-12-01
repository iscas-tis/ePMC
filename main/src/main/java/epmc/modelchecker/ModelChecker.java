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
import epmc.graph.Scheduler;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.main.options.OptionsEPMC;
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
     */
    public ModelChecker(Model model) {
        assert model != null;
        this.model = model;
        Options options = Options.get();
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

    public PropertySolver getSolverFor(Expression property, StateSet states) {
        return getSolverFor(property, states, false);
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
     */
    public PropertySolver getSolverFor(Expression property, StateSet states,
            boolean computeScheduler) {
        assert property != null;
        PropertySolver foundWithoutScheduler = null;
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            solver.setComputeScheduler(computeScheduler);
            if (computeScheduler && solver.canHandle() && !solver.canComputeScheduler()) {
                foundWithoutScheduler = solver;
            }
            if (solver.canHandle() && (!computeScheduler || solver.canComputeScheduler())) {
                return solver;
            }
        }
        if (foundWithoutScheduler != null) {
            // TODO log warning
            return foundWithoutScheduler;
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
        boolean hasProperties = false;
        if (model.getPropertyList() != null) {
            for (RawProperty property : model.getPropertyList().getRawProperties()) {
                String propString;
                propString = property.getName();
                List<String> propertyNames = Options.get().getStringList(OptionsEPMC.PROPERTY_INPUT_NAMES);
                // only check specified properties
                if(propertyNames != null && propertyNames.size() > 0 && !propertyNames.contains(propString)) {
                	continue;
                }
                hasProperties = true;
                if (propString == null) {
                    propString = property.getDefinition();
                }
                getLog().send(MessagesModelChecker.ANALYSING_PROPERTY, propString);
                Expression expression = model.getPropertyList().getParsedProperty(property);
                ModelCheckerResult propRes = null;
                try {
                    propRes = checkProperty(property, expression);
                } catch (EPMCException e) {
                    propRes = new ModelCheckerResult(property, e);
                }
                getLog().send(propRes);
            }
        }
        time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
        if(! hasProperties) {
        	System.out.println("No property has been specified");
        }
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
     */
    private LowLevel prepareLowLevel(Expression property) {
        assert property != null;
        PropertySolver solver = getSolverFor(property, null);
        Set<Object> graphProperties = solver.getRequiredGraphProperties();
        Set<Object> nodeProperties = solver.getRequiredNodeProperties();
        Set<Object> edgeProperties = solver.getRequiredEdgeProperties();
        return lowLevel = UtilModelChecker.buildLowLevel(model, graphProperties, nodeProperties, edgeProperties);
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
     * @param expression property to be checked
     * @return value obtained for the given property
     */
    private ModelCheckerResult checkProperty(RawProperty property, Expression expression) {
        assert property != null;
        assert expression != null;
        if (lowLevel != null) {
            lowLevel.close();
            lowLevel = null;
        }
        lowLevel = prepareLowLevel(expression);

        StateMap stateMap = check(expression, lowLevel.newInitialStateSet());
        Value value = stateMap.subsumeResult(lowLevel.newInitialStateSet());
        Scheduler scheduler = stateMap.getScheduler();
        return new ModelCheckerResult(property, value, scheduler, lowLevel);
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
     */
    public StateMap check(Expression property, StateSet states) {
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
     */
    public Set<Object> getRequiredGraphProperties(Expression property, StateSet states) {
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

    public Set<Object> getRequiredNodeProperties(Expression property, StateSet states) {
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

    public Set<Object> getRequiredEdgeProperties(Expression property, StateSet states) {
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
        return Options.get().get(OptionsMessages.LOG);
    }

    /**
     * Get engine used for model checking.
     * 
     * @return engine used for model checking
     */
    public Engine getEngine() {
        return engine;
    }

    public void ensureCanHandle(Expression property, StateSet states) {
        for (Class<? extends PropertySolver> solverClass : solvers) {
            PropertySolver solver = Util.getInstance(solverClass);
            solver.setModelChecker(this);
            solver.setProperty(property);
            solver.setForStates(states);
            if (solver.canHandle()) {
                return;
            }
        }
        fail(ProblemsModelChecker.NO_SOLVER_AVAILABLE, 
                property.getPositional() != null ? property.getPositional()
                        .getContent() : property);
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
