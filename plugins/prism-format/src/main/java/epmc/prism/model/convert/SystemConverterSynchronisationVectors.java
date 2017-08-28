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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.Edge;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.SynchronisationVectorElement;
import epmc.jani.model.component.SynchronisationVectorSync;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.SystemAlphaParallel;
import epmc.prism.model.SystemAsyncParallel;
import epmc.prism.model.SystemDefinition;
import epmc.prism.model.SystemHide;
import epmc.prism.model.SystemModule;
import epmc.prism.model.SystemRename;
import epmc.prism.model.SystemRestrictedParallel;

final class SystemConverterSynchronisationVectors implements SystemConverter {
    /** Empty string. */
    final static String EMPTY = "";

    private ModelPRISM modelPRISM;
    private ModelJANI modelJANI;
    /** Identifier for the silent action. */
    private Expression silentActionIdentifier;

    @Override
    public void setPRISMModel(ModelPRISM modelPrism) {
        this.modelPRISM = modelPrism;
    }

    @Override
    public void setJANIModel(ModelJANI modelJani) {
        this.modelJANI = modelJani;
    }

    @Override
    public void convert() {
        this.silentActionIdentifier = new ExpressionIdentifierStandard.Builder()
                .setName(EMPTY)
                .build();
        Automata automata = modelJANI.getAutomata();
        Actions actions = modelJANI.getActions();
        ComponentSynchronisationVectors system = convertSystem(modelPRISM.getSystem(), automata, actions);
        modelJANI.setSystem(system);
    }

    private ComponentSynchronisationVectors convertSystem(SystemDefinition system,
            Map<String,Automaton> automata,
            Actions actions) {
        assert system != null;
        assert automata != null;
        if (system.isModule()) {
            return convertSystemModule(system.asModule(), automata, actions);
        } else if (system.isAlphaParallel()) {
            return convertSystemAlphaParallel(system.asAlphaParallel(), automata, actions);
        } else if (system.isAsyncParallel()) {
            return convertSystemAsyncParallel(system.asAsyncParallel(), automata, actions);
        } else if (system.isHide()) {
            return convertSystemHide(system.asHide(), automata, actions);
        } else if (system.isRename()) {
            return convertSystemRename(system.asRename(), automata, actions);
        } else if (system.isRestrictedParallel()) {
            return convertSystemRestrictedParallel(system.asRestrictedParallel(), automata, actions);
        } else {
            assert false; // TODO
            return null;
        }
    }

    private ComponentSynchronisationVectors convertSystemModule(SystemModule systemModule, Map<String, Automaton> automata, Actions actions) {
        String instance = systemModule.getModule();
        Automaton automaton = automata.get(instance);
        ComponentSynchronisationVectors result = new ComponentSynchronisationVectors();
        result.setModel(modelJANI);
        SynchronisationVectorElement synchronisationVectorElement = new SynchronisationVectorElement();
        synchronisationVectorElement.setModel(modelJANI);
        synchronisationVectorElement.setAutomaton(automaton);
        result.setElements(Collections.singletonList(synchronisationVectorElement));
        List<SynchronisationVectorSync> synchronisationVectors = new ArrayList<>();
        Set<Action> allActions = new LinkedHashSet<>();
        for (Edge edge : automaton.getEdges()) {
            Action action = edge.getAction();
            allActions.add(action);
        }
        for (Action action : allActions) {
            SynchronisationVectorSync synchronisationVectorSync = new SynchronisationVectorSync();
            synchronisationVectorSync.setModel(modelJANI);
            synchronisationVectorSync.setSynchronise(Collections.singletonList(action));
            synchronisationVectorSync.setResult(action);
            synchronisationVectors.add(synchronisationVectorSync);
        }
        result.setSyncs(synchronisationVectors);
        return result;
    }

    private ComponentSynchronisationVectors convertSystemAlphaParallel(SystemAlphaParallel systemAlphaParallel, Map<String, Automaton> automata, Actions actions) {
        SystemDefinition left = systemAlphaParallel.getLeft();
        SystemDefinition right = systemAlphaParallel.getRight();
        ComponentSynchronisationVectors leftComponent = convertSystem(left, automata, actions);
        ComponentSynchronisationVectors rightComponent = convertSystem(right, automata, actions);

        Set<Expression> mid = new HashSet<>();
        mid.addAll(left.getAlphabet());
        mid.retainAll(right.getAlphabet());
        mid.remove(silentActionIdentifier);
        Set<Action> syncActions = mapActions(mid, actions);
        ComponentSynchronisationVectors result = parallel(leftComponent, rightComponent, syncActions);
        return result;
    }

    private ComponentSynchronisationVectors convertSystemAsyncParallel(SystemAsyncParallel systemAsyncParallel, Map<String, Automaton> automata,
            Actions actions) {
        SystemDefinition left = systemAsyncParallel.getLeft();
        SystemDefinition right = systemAsyncParallel.getRight();
        ComponentSynchronisationVectors leftComponent = convertSystem(left, automata, actions);
        ComponentSynchronisationVectors rightComponent = convertSystem(right, automata, actions);
        return parallel(leftComponent, rightComponent, Collections.emptySet());
    }

    private ComponentSynchronisationVectors convertSystemRestrictedParallel(SystemRestrictedParallel systemRestrictedParallel,
            Map<String, Automaton> automata, Actions actions) {
        SystemDefinition left = systemRestrictedParallel.getLeft();
        SystemDefinition right = systemRestrictedParallel.getRight();
        ComponentSynchronisationVectors leftComponent = convertSystem(left, automata, actions);
        ComponentSynchronisationVectors rightComponent = convertSystem(right, automata, actions);
        Set<Action> syncActions = mapActions(systemRestrictedParallel.getSync(), actions);
        return parallel(leftComponent, rightComponent, syncActions);
    }

    private ComponentSynchronisationVectors parallel(
            ComponentSynchronisationVectors left,
            ComponentSynchronisationVectors right,
            Set<Action> syncActions) {
        ComponentSynchronisationVectors result = new ComponentSynchronisationVectors();
        result.setModel(modelJANI);
        List<SynchronisationVectorElement> elements = new ArrayList<>();
        for (SynchronisationVectorElement element : left.getElements()) {
            elements.add(element);
        }
        for (SynchronisationVectorElement element : right.getElements()) {
            elements.add(element);
        }
        result.setElements(elements);
        List<SynchronisationVectorSync> syncVectors = new ArrayList<>();
        for (SynchronisationVectorSync leftSync : left.getSyncs()) {
            if (!syncActions.contains(leftSync.getResult())) {
                continue;
            }
            for (SynchronisationVectorSync rightSync : right.getSyncs()) {
                if (leftSync.getResult() != rightSync.getResult()) {
                    continue;
                }
                SynchronisationVectorSync resultSyn = new SynchronisationVectorSync();
                resultSyn.setModel(modelJANI);
                List<Action> resultSyncActions = new ArrayList<>();
                resultSyncActions.addAll(leftSync.getSynchronise());
                resultSyncActions.addAll(rightSync.getSynchronise());
                resultSyn.setSynchronise(resultSyncActions);
                resultSyn.setResult(leftSync.getResult());
                syncVectors.add(resultSyn);
            }			
        }
        result.setSyncs(syncVectors);
        for (SynchronisationVectorSync leftSync : left.getSyncs()) {
            if (syncActions.contains(leftSync.getResult())) {
                continue;
            }
            SynchronisationVectorSync resultSyn = new SynchronisationVectorSync();
            resultSyn.setModel(modelJANI);
            List<Action> resultSyncActions = new ArrayList<>();
            resultSyncActions.addAll(leftSync.getSynchronise());
            for (int i = 0; i < right.getElements().size(); i++) {
                resultSyncActions.add(null);
            }
            resultSyn.setSynchronise(resultSyncActions);
            resultSyn.setResult(leftSync.getResult());
            syncVectors.add(resultSyn);
        }
        for (SynchronisationVectorSync rightSync : right.getSyncs()) {
            if (syncActions.contains(rightSync.getResult())) {
                continue;
            }
            SynchronisationVectorSync resultSyn = new SynchronisationVectorSync();
            resultSyn.setModel(modelJANI);
            List<Action> resultSyncActions = new ArrayList<>();
            for (int i = 0; i < left.getElements().size(); i++) {
                resultSyncActions.add(null);
            }
            resultSyncActions.addAll(rightSync.getSynchronise());
            resultSyn.setSynchronise(resultSyncActions);
            resultSyn.setResult(rightSync.getResult());
            syncVectors.add(resultSyn);
        }
        return result;
    }

    private ComponentSynchronisationVectors convertSystemHide(SystemHide systemHide, Map<String, Automaton> automata, Actions actions) {
        SystemDefinition inner = systemHide.getInner();
        ComponentSynchronisationVectors innerComponent = convertSystem(inner, automata, actions);
        ComponentSynchronisationVectors result = new ComponentSynchronisationVectors();
        result.setModel(modelJANI);
        result.setElements(innerComponent.getElements());
        result.setSyncs(innerComponent.getSyncs());
        Map<Action, Action> renamed = hideActions(systemHide.getHidden(), actions);
        for (SynchronisationVectorSync sync : result.getSyncs()) {
            Action oldAction = sync.getResult();
            if (renamed.containsKey(oldAction)) {
                sync.setResult(renamed.get(oldAction));
            }
        }
        return result;
    }

    private ComponentSynchronisationVectors convertSystemRename(SystemRename systemRename, Map<String, Automaton> automata,
            Actions actions) {
        SystemDefinition inner = systemRename.getInner();
        ComponentSynchronisationVectors innerComponent = convertSystem(inner, automata, actions);
        ComponentSynchronisationVectors result = new ComponentSynchronisationVectors();
        result.setModel(modelJANI);
        result.setElements(innerComponent.getElements());
        result.setSyncs(innerComponent.getSyncs());
        Map<Action, Action> renamed = renameActions(systemRename.getRenaming(), actions);
        for (SynchronisationVectorSync sync : result.getSyncs()) {
            Action oldAction = sync.getResult();
            if (renamed.containsKey(oldAction)) {
                sync.setResult(renamed.get(oldAction));
            }
        }
        return result;
    }

    private Map<Action, Action> renameActions(Map<Expression, Expression> renaming, Actions actions) {
        Map<Action,Action> result = new LinkedHashMap<>();
        for (Entry<Expression,Expression> entry : renaming.entrySet()) {
            result.put(actions.get(entry.getKey()), actions.get(entry.getValue()));
        }
        return result;
    }

    private Map<Action, Action> hideActions(Set<Expression> hidden, Actions actions) {
        Map<Action,Action> result = new LinkedHashMap<>();
        for (Expression expression : hidden) {
            Action from = actions.get(expression);
            result.put(from, getSilentAction());
        }
        return result;
    }

    private Set<Action> mapActions(Collection<Expression> expressions, Actions allActions) {
        assert expressions != null;
        for (Expression expression : expressions) {
            assert expression != null;
        }
        Set<Action> result = new LinkedHashSet<>();
        for (Expression expression : expressions) {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            result.add(allActions.get(expressionIdentifier.getName()));
        }
        return result;
    }

    private Action getSilentAction() {
        return modelJANI.getSilentAction();
    }
}
