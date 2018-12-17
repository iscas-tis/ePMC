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

package epmc.prism.model.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsCTMDP;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Assignments;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Location;
import epmc.jani.model.Locations;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.Rate;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;
import epmc.jani.model.component.ComponentRename;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.SynchronisationVectorElement;
import epmc.jani.model.component.SynchronisationVectorSync;
import epmc.jani.model.type.JANITypeReal;
import epmc.operator.OperatorIsZero;
import epmc.options.Options;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.RewardStructure;
import epmc.prism.model.StateReward;
import epmc.prism.model.TransitionReward;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

/**
 * Converter for rewards from PRISM to JANI.
 * 
 * @author Ernst Moritz Hahn
 */
final class RewardsConverter {
    /** Name of automaton which will handle rewards. */
    private final static String REWARD_AUTOMATON = "reward_automaton";
    /** Name of the single location of the reward automaton. */
    private final static String LOCATION_NAME = PRISM2JANIConverter.LOCATION_NAME;
    /** Empty string. */
    private final static String EMPTY = PRISM2JANIConverter.EMPTY;

    /** PRISM model to be transformed to JANI model. */
    private ModelPRISM modelPRISM;
    /** JANI model resulting from transformation from PRISM model. */
    private ModelJANI modelJANI;
    /** Action used as silent action. */
    private Action silentAction;
    /** Rewards have to be renamed for exporting but not for internal use;
     * this variable controls the way rewards are managed. */
    private boolean forExporting;

    void setForExporting(boolean forExporting) {
        this.forExporting = forExporting;
    }

    void setPRISMModel(ModelPRISM modelPrism) {
        this.modelPRISM = modelPrism;
    }

    void setJANIModel(ModelJANI modelJani) {
        this.modelJANI = modelJani;
    }

    void setTauAction(Action tauAction) {
        this.silentAction = tauAction;
    }

    void attachRewards() {
        assert modelJANI != null;
        assert modelPRISM != null;
        assert silentAction != null;

        RewardMethod rewardMethod = Options.get().getEnum(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD);
        switch (rewardMethod) {
        case INTEGRATE:
            attachRewardsIntegrate();
            break;
        case EXTERNAL:
            attachRewardsExternal();
            break;
        case NONE:
            break;
        default:
            assert false;
            break;		
        }
    }

    private void attachRewardsExternal() {
        Variables rewards = buildRewards();
        addRewards(rewards);
        Automaton rewardAutomaton = computeRewardAutomaton();
        Automata automata = modelJANI.getAutomata();
        automata.addAutomaton(rewardAutomaton);
        composeRewardAutomatonWithSystem(rewardAutomaton);
    }

    private void addRewards(Variables rewards) {
        Variables globalVariables = modelJANI.getGlobalVariables();
        if (globalVariables == null) {
            globalVariables = new Variables();
            globalVariables.setModel(modelJANI);
            modelJANI.setGlobalVariables(globalVariables);
        }
        for (Variable variable : rewards) {
            globalVariables.addVariable(variable);
        }
    }

    private Variables buildRewards() {
        Variables rewards = new Variables();
        rewards.setModel(modelJANI);
        JANITypeReal rewardType = new JANITypeReal();
        rewardType.setModel(modelJANI);
        for (RewardStructure reward : modelPRISM.getRewards()) {
            String name = reward.getName();
            if (forExporting) {
                name = PRISM2JANIConverter.prefixRewardName(reward.getName());
            }
            Variable variable = new Variable();
            variable.setModel(modelJANI);
            variable.setName(name);
            ExpressionIdentifierStandard identifier = new ExpressionIdentifierStandard.Builder()
                    .setName(name)
                    .build();
            variable.setIdentifier(identifier);
            variable.setType(rewardType);
            variable.setTransient(true);
            variable.setInitial(new ExpressionLiteral.Builder()
                    .setValue("0")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build());
            rewards.addVariable(variable);
        }
        return rewards;
    }

    private Automaton computeRewardAutomaton() {
        Automaton automaton = new Automaton();
        automaton.setModel(modelJANI);
        automaton.setName(REWARD_AUTOMATON);
        Location location = new Location();
        location.setModel(modelJANI);
        location.setName(LOCATION_NAME);
        Assignments locationRewardAssignments = computeLocationRewardAssignments();
        location.setTransientValueAssignments(locationRewardAssignments);
        Locations locations = new Locations();
        locations.add(location);
        automaton.setLocations(locations);
        automaton.setInitialLocations(Collections.singleton(location));
        Edges edges = computeRewardAutomatonEdges(location);
        automaton.setEdges(edges);		
        return automaton;
    }

    private Edges computeRewardAutomatonEdges(Location location) {
        assert location != null;
        Edges edges = new Edges();
        edges.setModel(modelJANI);
        Actions actions = modelJANI.getActions();
        for (Action action : actions) {
            Edge edge = new Edge();
            edge.setModel(modelJANI);
            edge.setAction(action);
            edge.setLocation(location);
            Destinations destinations = new Destinations();
            destinations.setModel(modelJANI);
            edge.setDestinations(destinations);
            Destination destination = new Destination();
            destination.setModel(modelJANI);
            destination.setLocation(location);
            rewardSetRateIfAppropriate(edge);
            Assignments assignments = new Assignments();
            computeEdgeRewardAssignments(action, assignments);
            destination.setAssignments(assignments);
            destinations.addDestination(destination);
            if (assignments.size() > 0) {
                edges.addEdge(edge);
            }
        }
        return edges;
    }

    private void rewardSetRateIfAppropriate(Edge edge) {
        assert edge != null;
        if (SemanticsCTMC.isCTMC(modelPRISM.getSemantics())
                || SemanticsCTMDP.isCTMDP(modelPRISM.getSemantics())) {
            Rate rate = new Rate();
            rate.setModel(modelJANI);
            rate.setExp(new ExpressionLiteral.Builder()
                    .setValue("1")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build());
            edge.setRate(rate);
        }
    }

    private void computeEdgeRewardAssignments(Action action, Assignments assignments) {
        Variables rewards = modelJANI.getGlobalVariablesTransient();
        assignments.setModel(modelJANI);
        List<RewardStructure> rewardStructures = modelPRISM.getRewards();
        for (RewardStructure structure : rewardStructures) {
            String rewardName = structure.getName(); 
            if (forExporting) {
                rewardName = PRISM2JANIConverter.prefixRewardName(rewardName);
            }
            Variable variable = rewards.get(rewardName);
            Expression assignedToVariable = null;
            for (TransitionReward transitionReward : structure.getTransitionRewards()) {
                String label = transitionReward.getLabel();
                if (!action.getName().equals(label)
                        && (!silentAction.equals(action) || !label.equals(EMPTY))) {
                    continue;
                }
                Expression guard = transitionReward.getGuard();
                Expression value = transitionReward.getValue();
                Expression guardedReward = null;
                if (isTrue(guard)) {
                    guardedReward = value;
                } else {
                    guardedReward = UtilExpressionStandard.opIte(guard, value, 0);
                }
                if (assignedToVariable == null) {
                    assignedToVariable = guardedReward;
                } else {
                    assignedToVariable = UtilExpressionStandard.opAdd(assignedToVariable, guardedReward);
                }
            }
            if (assignedToVariable == null) {
                continue;
            }
            AssignmentSimple rewardAssignment = new AssignmentSimple();
            rewardAssignment.setModel(modelJANI);
            rewardAssignment.setRef(variable);
            rewardAssignment.setValue(assignedToVariable);
            assignments.add(rewardAssignment);
        }
    }

    private Assignments computeLocationRewardAssignments() {
        Variables rewards = modelJANI.getGlobalVariablesTransient();
        Assignments locationRewardAssignments = new Assignments();
        locationRewardAssignments.setModel(modelJANI);
        List<RewardStructure> rewardStructures = modelPRISM.getRewards();
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (RewardStructure structure : rewardStructures) {
            Expression stateRewards = convertStateRewards(structure.getStateRewards());
            if (ExpressionLiteral.is(stateRewards)) {
                Value value = UtilEvaluatorExplicit.evaluate(stateRewards);
                OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, value.getType());
                isZero.apply(cmp, value);
            }
            if (ExpressionLiteral.is(stateRewards)
                    && cmp.getBoolean()) {
                continue;
            }
            String rewardName = structure.getName();
            if (forExporting) {
                rewardName = PRISM2JANIConverter.prefixRewardName(rewardName);
            }
            Variable rewardVariable = rewards.get(rewardName);
            AssignmentSimple locationRewardAssignment = new AssignmentSimple();
            locationRewardAssignment.setModel(modelJANI);
            locationRewardAssignment.setRef(rewardVariable);
            locationRewardAssignment.setValue(stateRewards);
            locationRewardAssignments.add(locationRewardAssignment);
        }
        if (locationRewardAssignments.size() == 0) {
            return null;
        }
        return locationRewardAssignments;
    }

    private Expression convertStateRewards(List<StateReward> stateRewards) {
        assert stateRewards != null;
        Expression result = null;
        for (StateReward stateReward : stateRewards) {
            Expression guard = stateReward.getGuard();
            Expression value = stateReward.getValue();
            Expression guardedReward = null;
            if (isTrue(guard)) {
                guardedReward = value;
            } else {
                guardedReward = UtilExpressionStandard.opIte(guard, value, 0);				
            }
            if (result == null) {
                result = guardedReward;
            } else {
                result = UtilExpressionStandard.opAdd(result, guardedReward);
            }
        }
        if (result == null) {
            return new ExpressionLiteral.Builder()
                    .setValue("0")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build();
        }
        return result;
    }

    private void composeRewardAutomatonWithSystem(Automaton rewardAutomaton) {
        Component system = modelJANI.getSystem();
        List<Action> rewardAutomatonActions = new ArrayList<>();
        for (Edge edge : rewardAutomaton.getEdges()) {
            rewardAutomatonActions.add(edge.getAction());
        }
        if (system instanceof ComponentSynchronisationVectors) {
            ComponentSynchronisationVectors systemSync = (ComponentSynchronisationVectors) system;
            SynchronisationVectorElement rewardElement = new SynchronisationVectorElement();
            rewardElement.setModel(modelJANI);
            rewardElement.setAutomaton(rewardAutomaton);
            List<SynchronisationVectorElement> newElements = new ArrayList<>(systemSync.getElements());
            newElements.add(rewardElement);
            systemSync.setElements(newElements);
            for (SynchronisationVectorSync sync : systemSync.getSyncs()) {
                Action result = sync.getResult();
                List<Action> newActions = new ArrayList<>(sync.getSynchronise());
                if (!rewardAutomatonActions.contains(result)) {
                    result = null;
                }
                newActions.add(result);
                sync.setSynchronise(newActions);
            }
        } else {
            ComponentParallel result = new ComponentParallel();
            result.setModel(modelJANI);
            result.setLeft(system);
            ComponentAutomaton componentRewardAutomaton = new ComponentAutomaton();
            componentRewardAutomaton.setModel(modelJANI);
            componentRewardAutomaton.setAutomaton(rewardAutomaton);
            result.setRight(componentRewardAutomaton);
            result.addActions(rewardAutomatonActions);
            modelJANI.setSystem(result);
        }
    }

    private void attachRewardsIntegrate() {
        Variables rewards = buildRewards();
        addRewards(rewards);
        Actions actions = modelJANI.getActions();
        for (Action action : actions) {
            attachRewards(modelJANI.getSystem(), action, action);
        }
        Automaton automaton = modelJANI.getAutomata().iterator().next();
        for (Location location : automaton.getLocations()) {
            location.setTransientValueAssignments(computeLocationRewardAssignments());
        }
    }

    private void attachRewards(Component system, Action toAction, Action effectOf) {
        assert system != null;
        if (system instanceof ComponentAutomaton) {
            attachRewardsAutomaton((ComponentAutomaton) system, toAction, effectOf);
        } else if (system instanceof ComponentParallel) {
            attachRewardsParallel((ComponentParallel) system, toAction, effectOf);
        } else if (system instanceof ComponentRename) {
            attachRewardsRename((ComponentRename) system, toAction, effectOf);
        } else if (system instanceof ComponentSynchronisationVectors) {
            attachRewardsSynchronisationVectors((ComponentSynchronisationVectors) system, toAction, effectOf);
        } else {
            assert false;
        }
    }

    private void attachRewardsSynchronisationVectors(
            ComponentSynchronisationVectors system, Action toAction,
            Action effectOf) {
        /* Note that this way of attaching rewards only works for
         * synchronisation vectors produced by the translation from PRISM,
         * but not for general synchronisation vectors. */
        List<SynchronisationVectorElement> elements = system.getElements();
        List<SynchronisationVectorSync> oldSyncs = system.getSyncs();
        while (!oldSyncs.isEmpty()) {
            List<SynchronisationVectorSync> newSyncs = new ArrayList<>();
            SynchronisationVectorSync oldSync = null;
            for (SynchronisationVectorSync oldSyncc : oldSyncs) {
                if (oldSyncc.getResult() == effectOf) {
                    oldSync = oldSyncc;
                    break;
                }
            }
            if (oldSync == null) {
                return;
            }
            List<Action> actions = oldSync.getSynchronise();
            int elementNr = 0;
            Action automatonAction = null;
            for (Action action : actions) {
                if (action != null) {
                    automatonAction = action;
                    break;
                }
                elementNr++;
            }
            Automaton automaton = elements.get(elementNr).getAutomaton();
            attachRewardsAutomaton(automaton, automatonAction, effectOf);
            for (SynchronisationVectorSync oldSyncc : oldSyncs) {
                Action action = oldSyncc.getSynchronise().get(elementNr);
                if (action != automatonAction) {
                    newSyncs.add(oldSyncc);
                }
            }
            oldSyncs = newSyncs;
        }

        //		system.setSyncs(oldSyncs);

        // TODO Auto-generated method stub

    }

    private void attachRewardsAutomaton(Automaton automaton, Action toAction, Action effectOf) {
        assert automaton != null;
        assert toAction != null;
        for (Edge edge : automaton.getEdges()) {
            if (!edge.getAction().getName().equals(toAction.getName())) {
                continue;
            }
            //			AT: there are no transient/observable assignment in the JANI specification
            for (Destination destination : edge.getDestinations()) {
                Assignments assignments = destination.getAssignments();
                computeEdgeRewardAssignments(effectOf, assignments);
            }
        }
    }

    private void attachRewardsAutomaton(ComponentAutomaton system, Action toAction, Action effectOf) {
        assert system != null;
        assert toAction != null;
        Automaton automaton = system.getAutomaton();
        attachRewardsAutomaton(automaton, toAction, effectOf);
    }

    private void attachRewardsParallel(ComponentParallel system, Action toAction, Action effectOf) {
        Component componentLeft = system.getLeft();
        Component componentRight = system.getRight();
        Set<Action> systemActions = system.getActions();
        boolean found = false;
        for (Action sysAction : systemActions) {
            if (sysAction.getName().equals(toAction.getName())) {
                found = true;
            }
        }
        if (found) {
            attachRewards(componentLeft, toAction, effectOf);
        } else {
            attachRewards(componentLeft, toAction, effectOf);
            attachRewards(componentRight, toAction, effectOf);			
        }
    }

    private void attachRewardsRename(ComponentRename system, Action toAction, Action effectOf) {
        assert system != null;
        assert toAction != null;
        assert effectOf != null;
        Component renamed = system.getRenamed();
        for (Entry<Action, Action> entry : system.getRenaming().entrySet()) {
            Action entryFromAction = entry.getKey();
            Action entryToAction = entry.getValue();
            if (entryToAction.getName().equals(toAction.getName())) {
                attachRewards(renamed, entryFromAction, effectOf);
            }
        }
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(expressionLiteral.getValue());
    }
}
