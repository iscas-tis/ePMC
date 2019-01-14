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

package epmc.prism.model;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.SMGPlayer;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsMA;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.jani.model.type.JANIType;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.prism.error.ProblemsPRISM;
import epmc.prism.messages.MessagesPRISM;
import epmc.prism.options.OptionsPRISM;
import epmc.util.StopWatch;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeObject;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInteger;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

final class GraphDDPRISM implements GraphDD {
    public enum TransitionEncoding {
        MC,
        MDP_STATE
    }

    private final GraphDDProperties properties;
    private final ModelPRISM model;
    private final ExpressionToDD expressionToDD;
    private final Object2IntOpenHashMap<Expression> actions;
    private final Map<Expression,Type> variables;
    private final DD initial;
    private final DD transitionsBoolean;
    private final DD transitionsBooleanForNext;
    private final boolean withWeights;
    private final DD nextCube;
    private final DD presAndActions;
    private final DD actionsCube;
    private final Permutation nextToPres;
    private final DD nonDetStates;
    private final DD presEqNext;
    private final List<DD> presVars;
    private final List<DD> nextVars;
    private final List<DD> transitionVars;
    private final VariableDD actionVariable;
    private final Map<Module,VariableDD> nondetVariables = new HashMap<>();
    private final Map<Expression,VariableDD> variablesDD;
    private final DD presCube;
    private final StopWatch buildTime = new StopWatch(true);
    private final Log log;
    private boolean closed;
    private final DD nodes;
    private final TransitionEncoding transEnc;
    private final List<DD> players = new ArrayList<>();
    private DD states;
    private int rateIndex = 0;          // rate index for MA
    private final String RATE = "rate"; // rate action for MA
    private Object2IntOpenHashMap<String> playerNameToNumber;

    /* constructors */

    GraphDDPRISM(ModelPRISM model, Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        this.properties = new GraphDDProperties(this);
        assert assertConstructorArguments(model, nodeProperties, edgeProperties);
        ensure(Options.get().getBoolean(OptionsPRISM.PRISM_FLATTEN), ProblemsPRISM.FLATTEN_NEEDED_DD);
        if (SemanticsNonDet.isNonDet(model.getSemantics())) {
            transEnc = TransitionEncoding.MDP_STATE;
        } else {
            transEnc = TransitionEncoding.MC;
        }
        playerNameToNumber = computeNameToNumber(model.getPlayers());

        this.log = Options.get().get(OptionsMessages.LOG);
        log.send(MessagesPRISM.BUILDING_DD_MODEL);

        this.model = model;
        this.withWeights = edgeProperties.contains(CommonProperties.WEIGHT);
        this.variablesDD = new HashMap<>();
        this.actions = collectActions();
        this.variables = new HashMap<>();
        this.variables.putAll(collectVariables(model));
        this.actionVariable = ContextDD.get().newInteger("%actions", 1, 0, actions.size() - 1);
        computeVariableEncoding(variables);
        this.expressionToDD = new ExpressionToDD(variablesDD);
        this.presVars = computePresVars();
        this.nextVars = computeNextVars();
        this.nextCube = ContextDD.get().listToCube(this.nextVars);
        this.nextToPres = ContextDD.get().newPermutationListDD(presVars, nextVars);
        this.transitionVars = computeActionVars();
        this.nonDetStates = ContextDD.get().newConstant(false);
        this.presEqNext = computePresEqNext();
        this.initial = translateInitial();
        expressionToDD.putConstant(new ExpressionIdentifierStandard.Builder()
                .setName("\"init\"")
                .build(), initial);

        ArrayList<DD> modulesDD = new ArrayList<>();
        for (Module module : model.getModules()) {
            modulesDD.add(translateModuleDD(module.asCommands()));
        }
        DD nondetCubePres = ContextDD.get().newConstant(true);
        if (SemanticsNonDet.isNonDet(model.getSemantics())) {
            for (Module module : model.getModules()) {
                DD moduleNondetCubePres = nondetVariables.get(module).newCube(0);
                nondetCubePres = nondetCubePres.andWith(moduleNondetCubePres);
            }
        }
        if (transEnc == TransitionEncoding.MDP_STATE) {
            this.actionsCube = actionVariable.newCube(0).and(nondetCubePres.clone());
            for (Module module : model.getModules()) {
                for (DD dd : nondetVariables.get(module).getDDVariables(0)) {
                    transitionVars.add(dd.clone());
                }
            }
        } else {
            this.actionsCube = actionVariable.newCube(0);
        }

        presCube = ContextDD.get().listToCube(presVars);
        DD presAndActions = actionsCube.and(presCube);
        this.presAndActions = presAndActions.andWith(nondetCubePres);
        this.states = computeStates(transEnc, nondetVariables.values());
        TypeEnum playerType = TypeEnum.get(Player.class);
        Value playerStochastic = playerType.newValue(Player.STOCHASTIC);
        Value playerOne = playerType.newValue(Player.ONE);
        Value playerOneStochastic = playerType.newValue(Player.ONE_STOCHASTIC);
        if (nodeProperties.contains(CommonProperties.PLAYER)) {
            DD player;
            if (transEnc == TransitionEncoding.MDP_STATE) {
                player = ContextDD.get().newConstant(playerOneStochastic);
            } else if (transEnc == TransitionEncoding.MC) {
                player = ContextDD.get().newConstant(playerStochastic);
            } else {
                player = null;
                assert false;
            }
            properties.registerNodeProperty(CommonProperties.PLAYER, player);
        }
        /*
        for (Object p : nodeProperties) {
            if (p instanceof SMGPlayer) {
                SMGPlayer player = (SMGPlayer) p;
                int number = model.getPRISMGamesPlayer(player);
            }
        }
        */

        processGraphProperties();
        processNodeProperties(nodeProperties);
        processEdgeProperties(edgeProperties);

        log.send(MessagesPRISM.BUILDING_DD_MODEL_DONE, buildTime.getTimeSeconds());
        DD weight = modulesDDToModel(modulesDD);
        for (DD dd : modulesDD) {
            dd.dispose();
        }

        DD transitionsBoolean;
        if (withWeights) {
            DD constZero = ContextDD.get().newConstant(0);
            transitionsBoolean = weight.ne(constZero);
            constZero.dispose();
        } else {
            transitionsBoolean = weight.clone();
        }

        DD transitionsBooleanForNext = transitionsBoolean.abstractExist(actionsCube);

        nodes = exploreNodeSpace(log, initial, transitionsBooleanForNext, presCube,
                nextToPres);

        //        DD deadlock = computeDeadlock(model, expressionToDD, states);
        DD nodesAndStates = nodes.and(states);
        DD deadlock = nodesAndStates.abstractAndExist(transitionsBooleanForNext, nextCube).notWith().andWith(nodesAndStates.clone());
        nodesAndStates.dispose();

        expressionToDD.putConstantWith(new ExpressionIdentifierStandard.Builder()
                .setName("\"deadlock\"")
                .build(), deadlock);

        this.transitionsBoolean = fixDeadlocks(transitionsBoolean, false, transEnc,
                deadlock, presEqNext, actionVariable, nextToPres,
                nondetVariables.values());

        weight = fixDeadlocks(weight, withWeights, transEnc,
                deadlock, presEqNext, actionVariable, nextToPres,
                nondetVariables.values());

        if (edgeProperties.contains(CommonProperties.WEIGHT)) {
            properties.registerEdgeProperty(CommonProperties.WEIGHT, weight);
        }

        transitionsBooleanForNext = this.transitionsBoolean.abstractExist(actionsCube);
        nodesAndStates = nodes.and(states);
        deadlock = nodesAndStates.abstractAndExist(transitionsBooleanForNext, nextCube).not().and(nodesAndStates);
        this.transitionsBooleanForNext = transitionsBooleanForNext;
    }

    private void processGraphProperties() {
        properties.registerGraphProperty(CommonProperties.SEMANTICS,
                new TypeObject.Builder()
                .setClazz(Semantics.class)
                .build());
        setGraphPropertyObject(CommonProperties.SEMANTICS,
                model.getSemantics());
        properties.registerGraphProperty(CommonProperties.EXPRESSION_TO_DD,
                new TypeObject.Builder()
                .setClazz(expressionToDD.getClass())
                .build());
        setGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD,
                expressionToDD);
    }

    private void processNodeProperties(Set<Object> nodeProperties) {
        for (Object prop : nodeProperties) {
            if (prop == CommonProperties.STATE) {
                properties.registerNodeProperty(CommonProperties.STATE, states);
            } else if (prop instanceof RewardSpecification) {
                RewardSpecification rewardSpecification = (RewardSpecification) prop;
                RewardStructure rewardStructure = model.getReward(rewardSpecification);
                DD stateReward = translateStateReward(states, rewardStructure);
                properties.registerNodeProperty(prop, stateReward);
            } else if (prop instanceof SMGPlayer) {
                SMGPlayer player = (SMGPlayer) prop;
                int number = getPRISMGamesPlayer(player);
                properties.registerNodeProperty(prop, players.get(number));
            } else if (prop instanceof Expression) {
                Expression expression = (Expression) prop;
                properties.registerNodeProperty(prop, expressionToDD.translate(expression));
            }
        }
    }

    private void processEdgeProperties(Set<Object> edgeProperties) {
        for (Object prop : edgeProperties) {
            if (prop instanceof RewardSpecification) {
                RewardSpecification rewardSpecification = (RewardSpecification) prop;
                RewardStructure rewardStructure = model.getReward(rewardSpecification);
                DD transitionReward = translateTransReward(states, rewardStructure);
                properties.registerEdgeProperty(prop, transitionReward);
            }
        }
    }

    private static DD fixDeadlocks(DD transitions, boolean withWeights,
            TransitionEncoding transEnc, DD sink, DD presEqNext,
            VariableDD actionVariable, Permutation nextToPres,
            Iterable<VariableDD> nondetVariables)
    {
        DD result = transitions.clone();
        if (transEnc == TransitionEncoding.MC) {
            if (withWeights) {
                DD sinkAndPresEqNext = sink.clone().andWith(presEqNext.clone(), actionVariable.newIntValue(0, 0));
                DD loop = sinkAndPresEqNext.toMTWith();
                result = result.addWith(loop);
            } else {
                DD sinkAndPresEqNext = sink.clone().andWith(presEqNext.clone(), actionVariable.newIntValue(0, 0));
                result = result.orWith(sinkAndPresEqNext);
            }
        } else if (transEnc == TransitionEncoding.MDP_STATE) {
            if (withWeights) {
                DD sinkAndPresEqNext = sink.clone().andWith(presEqNext.clone(), actionVariable.newIntValue(0, 0));
                DD loop = sinkAndPresEqNext.toMTWith();
                result = result.addWith(loop);
            } else {
                DD sinkAndPresEqNext = sink.clone().andWith(presEqNext.clone(), actionVariable.newIntValue(0, 0));
                result = result.orWith(sinkAndPresEqNext);
            }
        }
        return result;
    }

    private static boolean assertConstructorArguments(ModelPRISM model,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        assert model != null;
        assert nodeProperties != null;
        for (Object object : nodeProperties) {
            assert object != null;
            assert object == CommonProperties.STATE
                    || object == CommonProperties.PLAYER
                    || object instanceof RewardSpecification
                    || object instanceof SMGPlayer
                    || object instanceof Expression
                    : object + " " + object.getClass();
        }
        assert edgeProperties != null;
        for (Object object : edgeProperties) {
            assert object != null;
            assert object == CommonProperties.WEIGHT
                    || object instanceof RewardSpecification
                    : object + " " + object.getClass();
        }
        return true;
    }

    private static DD computeDeadlock(ModelPRISM model,
            ExpressionToDD expressionToDD, DD states) {
        Map<String,Module> modules = new LinkedHashMap<>();
        for (Module module : model.getModules()) {
            modules.put(module.getName(), module);
        }
        Map<Expression,DD> canMoves = computeCanMode(model.getSystem(), modules,
                expressionToDD);
        DD canMove = ContextDD.get().newConstant(false);
        for (DD can : canMoves.values()) {
            canMove = canMove.orWith(can);
        }
        DD result = canMove.notWith();
        return result;
    }

    private static Map<Expression,DD> computeCanMode(SystemDefinition system,
            Map<String, Module> modules, ExpressionToDD expressionToDD)
    {
        Expression noSync = new ExpressionIdentifierStandard.Builder()
                .setName("")
                .build();
        Map<Expression,DD> result = new HashMap<>();
        if (system.isModule()) {
            ModuleCommands module = modules.get(system.asModule().getModule()).asCommands();
            for (Command command : module.getCommands()) {
                Expression action = command.getLabel();
                DD guards = result.get(action);
                if (guards == null) {
                    guards = ContextDD.get().newConstant(false);
                    result.put(action, guards);
                }
                DD guard = expressionToDD.translate(command.getGuard());
                guards = guards.orWith(guard);
                result.put(action, guards);
            }
        } else if (system.isRename()) {
            SystemRename systemRename = system.asRename();
            Map<Expression,Expression> renaming = systemRename.getRenaming();
            Map<Expression,DD> innerResult = computeCanMode(systemRename.getInner(), modules, expressionToDD);
            for (Entry<Expression,DD> entry : innerResult.entrySet()) {
                Expression renameTo = renaming.get(entry.getKey());
                if (renameTo != null) {
                    result.put(renameTo, entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (system.isHide()) {
            SystemHide systemHide = system.asHide();
            Set<Expression> hiding = systemHide.getHidden();
            Map<Expression,DD> innerResult = computeCanMode(systemHide.getInner(), modules, expressionToDD);
            for (Entry<Expression,DD> entry : innerResult.entrySet()) {
                if (hiding.contains(entry.getKey())) {
                    result.put(noSync, entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (system.isAsyncParallel()) {
            SystemAsyncParallel systemAsync = system.asAsyncParallel();
            Map<Expression,DD> left = computeCanMode(systemAsync.getLeft(), modules, expressionToDD);
            Map<Expression,DD> right = computeCanMode(systemAsync.getRight(), modules, expressionToDD);
            for (Entry<Expression,DD> entry : left.entrySet()) {
                if (right.containsKey(entry.getKey())) {
                    DD combined = entry.getValue().orWith(right.get(entry.getKey()));
                    right.remove(entry.getKey());
                    result.put(entry.getKey(), combined);
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            for (Entry<Expression,DD> entry : right.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else if (system.isAlphaParallel()) {
            SystemAlphaParallel systemAlpha = system.asAlphaParallel();
            Map<Expression,DD> left = computeCanMode(systemAlpha.getLeft(), modules, expressionToDD);
            Map<Expression,DD> right = computeCanMode(systemAlpha.getRight(), modules, expressionToDD);
            for (Entry<Expression,DD> entry : left.entrySet()) {
                Expression action = entry.getKey();
                if (right.containsKey(entry.getKey())) {
                    DD combined;
                    if (action.equals(noSync)) {
                        combined = entry.getValue().orWith(right.get(entry.getKey()));
                    } else {
                        combined = entry.getValue().andWith(right.get(entry.getKey()));
                    }
                    right.remove(entry.getKey());
                    result.put(entry.getKey(), combined);
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            for (Entry<Expression,DD> entry : right.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else if (system.isRestrictedParallel()) {
            SystemRestrictedParallel systemRestricted = system.asRestrictedParallel();
            Set<Expression> sync = systemRestricted.getSync();
            Map<Expression,DD> left = computeCanMode(systemRestricted.getLeft(), modules, expressionToDD);
            Map<Expression,DD> right = computeCanMode(systemRestricted.getRight(), modules, expressionToDD);
            for (Entry<Expression,DD> entry : left.entrySet()) {
                Expression action = entry.getKey();
                if (right.containsKey(entry.getKey())) {
                    DD combined;
                    if (sync.contains(action)) {
                        combined = entry.getValue().andWith(right.get(entry.getKey()));
                    } else {
                        combined = entry.getValue().orWith(right.get(entry.getKey()));
                    }
                    right.remove(entry.getKey());
                    result.put(entry.getKey(), combined);
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            for (Entry<Expression,DD> entry : right.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }            
        } else {
            assert false;
        }
        return result;
    }

    private static Map<Expression,Type> collectVariables(ModelPRISM model) {
        assert model != null;
        Map<Expression,Type> result = new HashMap<>();
        for (Entry<Expression, JANIType> entry : model.getGlobalVariables().entrySet()) {
            result.put(entry.getKey(), entry.getValue().toType());
        }
        for (Module module : model.getModules()) {
            for (Entry<Expression, JANIType> entry : module.getVariables().entrySet()) {
                result.put(entry.getKey(), entry.getValue().toType());
            }
        }
        return result;
    }

    private static DD computeStates(TransitionEncoding transEnc,
            Iterable<VariableDD> nondetVariables)
    {
        return ContextDD.get().newConstant(true);
    }

    @Override
    public DD getInitialNodes() {
        assert !closed;
        return initial;
    }

    @Override
    public DD getTransitions() {
        assert !closed;
        return transitionsBoolean;
    }

    /* private methods */

    private List<DD> computeActionVars() {
        ArrayList<DD> result = new ArrayList<>();
        for (DD var : actionVariable.getDDVariables(0)) {
            result.add(var.clone());
        }
        return result;
    }

    private List<DD> computePresVars() {
        List<DD> result = new ArrayList<>();
        for (Expression variable : variables.keySet()) {
            for (DD var : variablesDD.get(variable).getDDVariables(0)) {
                result.add(var.clone());
            }
        }

        return result;
    }

    private List<DD> computeNextVars() {
        List<DD> result = new ArrayList<>();
        for (Expression variable : variables.keySet()) {
            for (DD var : variablesDD.get(variable).getDDVariables(1)) {
                result.add(var.clone());
            }
        }

        return result;
    }

    private DD computePresEqNext() {
        DD result = ContextDD.get().newConstant(true);
        for (Expression variable : variables.keySet()) {
            List<DD> ddVarsPres = variablesDD.get(variable).getDDVariables(0);
            List<DD> ddVarsNext = variablesDD.get(variable).getDDVariables(1);
            Iterator<DD> presIt = ddVarsPres.iterator();
            Iterator<DD> nextIt = ddVarsNext.iterator();
            while (presIt.hasNext()) {
                DD eq = presIt.next().eq(nextIt.next());
                DD oldResult = result;
                result = result.and(eq);
                oldResult.dispose();
                eq.dispose();
            }
        }
        return result;
    }

    private DD modulesDDToModel(List<DD> modulesDD) {
        assert modulesDD != null;
        assert modulesDD.size() == 1;
        DD result = modulesDD.get(0).clone();
        if (transEnc == TransitionEncoding.MC) {
            if (withWeights) {
                if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
                    DD sum = result.abstractSum(nextCube);
                    result = result.divideIgnoreZeroWith(sum);
                }
            } else {
                DD boolTrans = result.clone();
                DD sink = boolTrans.abstractExistWith(nextCube.clone().andWith(actionVariable.newCube(0)));
                sink = sink.notWith();
                DD sinkAndPresEqNext = sink.andWith(presEqNext.clone(), actionVariable.newIntValue(0, 0));
                result = result.orWith(sinkAndPresEqNext);
            }
        }
        return result;
    }

    private DD translateInitial() {
        DD init = expressionToDD.translate(model.getInitialNodes());
        return init;
    }

    private Object2IntOpenHashMap<Expression> collectActions() {
        Object2IntOpenHashMap<Expression> actions = new Object2IntOpenHashMap<>();
        actions.put(new ExpressionIdentifierStandard.Builder()
                .setName("")
                .build(), 0);
        int nextActionNumber = 1;
        for (Module module : model.getModules()) {
            for (Expression expr : module.getAlphabet()) {
                if (!actions.containsKey(expr)) {
                    actions.put(expr, nextActionNumber);
                    nextActionNumber++;
                }
            }
        }
        return actions;
    }
    // compute the lower bound of nondeterministic variables
    // might be changed later when we support CTMDP, IMC
    private int computeNonDetVarLower() {
        Semantics semactics = model.getSemantics();
        int lower = 0;
        switch (transEnc) {
        case MDP_STATE:
            if (SemanticsMA.isMA(semactics)) lower = -1; // -1 for rate
            else lower = 0;
            break;
        default:
            break;
        }
        return lower;
    }

    private void computeVariableEncoding(Map<Expression,Type> variables)
    {
        int lower = computeNonDetVarLower();
        switch (transEnc) {
        case MDP_STATE:
            for (Module module : model.getModules()) {
                int numCommands = ((ModuleCommands) module).getCommands().size();
                nondetVariables.put(module, ContextDD.get().newInteger("%nondet" + module.getName(), 1, lower, numCommands - 1));
            }
            break;
        case MC:
            break;
        }
        rateIndex = lower;  // for MA
        for (Entry<Expression, Type> entry : variables.entrySet()) {
            Type type = entry.getValue();
            variablesDD.put(entry.getKey(), ContextDD.get().newVariable(entry.getKey().toString(), type, 2));
        }
    }

    private Map<Expression,DD> translateModule(ModuleCommands module)
    {
        int commandNr = 0;
        Map<Expression,DD> result = new HashMap<>();
        for (Command command : module.getCommands()) {
            Expression action = command.getAction();
            DD actionCommands = result.get(action);
            if (actionCommands == null) {
                Value value;
                if (withWeights) {
                    value = UtilValue.newValue(TypeWeight.get(), 0);
                } else {
                    value = UtilValue.newValue(TypeBoolean.get(), false);
                }
                actionCommands = ContextDD.get().newConstant(value);
                result.put(action, actionCommands);
            }
            DD commandDD = translateCommand(command, module, commandNr);
            if (withWeights) {
                actionCommands = actionCommands.addWith(commandDD);
            } else {
                actionCommands = actionCommands.orWith(commandDD);
            }
            result.put(action, actionCommands);
            commandNr++;
        }
        return result;
    }

    private DD translateSystem() {
        Map<String,ModuleCommands> modules = new LinkedHashMap<>();
        for (Module module : model.getModules()) {
            modules.put(module.getName(), module.asCommands());
        }
        //        Map<System,DD> 

        // TODO continue here
        Map<Expression,DD> translated = translateSystem(model.getSystem(), modules);
        return null;
    }

    private Map<Expression, DD> translateSystem(SystemDefinition system, Map<String, ModuleCommands> modules)
    {
        Map<Expression,DD> result = new HashMap<>();
        Expression noSync = new ExpressionIdentifierStandard.Builder()
                .setName("")
                .build();
        if (system.isModule()) {
            SystemModule systemModule = system.asModule();
            String moduleName = systemModule.getModule();
            result.putAll(translateModule(modules.get(moduleName).asCommands()));
        } else if (system.isRename()) {
            SystemRename systemRename = system.asRename();
            Map<Expression, Expression> renaming = systemRename.getRenaming();
            Map<Expression, DD> innerResult = translateSystem(system, modules);
            for (Entry<Expression,DD> entry : innerResult.entrySet()) {
                Expression renameTo = renaming.get(entry.getKey());
                if (renameTo != null) {
                    result.put(renameTo, entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (system.isHide()) {
            SystemHide systemHide = system.asHide();
            Set<Expression> hiding = systemHide.getHidden();
            Map<Expression,DD> innerResult = translateSystem(systemHide.getInner(), modules);
            for (Entry<Expression,DD> entry : innerResult.entrySet()) {
                if (hiding.contains(entry.getKey())) {
                    result.put(noSync, entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (system.isAsyncParallel()) {
            SystemAsyncParallel systemAsync = system.asAsyncParallel();
            Map<Expression,DD> left = translateSystem(systemAsync.getLeft(), modules);
            Map<Expression,DD> right = translateSystem(systemAsync.getRight(), modules);
            for (Entry<Expression,DD> entry : left.entrySet()) {
                if (right.containsKey(entry.getKey())) {
                    DD leftDD = entry.getValue();
                    DD rightDD = right.get(entry.getKey());
                    // TODO modify leftDD and rightDD
                    DD combined = leftDD.orWith(rightDD);
                    right.remove(entry.getKey());
                    result.put(entry.getKey(), combined);
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            for (Entry<Expression,DD> entry : right.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        // TODO Auto-generated method stub
        return result;
    }

    private DD translateModuleDD(ModuleCommands module) {
        int commandNr = 0;
        DD moduleDD = withWeights ? ContextDD.get().newConstant(0) : ContextDD.get().newConstant(false);
        for (Command command : module.getCommands()) {
            DD commandDD = translateCommand(command, module, commandNr);
            DD oldModuleDD = moduleDD;
            if (withWeights) {
                moduleDD = moduleDD.add(commandDD);
            } else {
                moduleDD = moduleDD.or(commandDD);
            }
            commandDD.dispose();
            oldModuleDD.dispose();
            commandNr++;
        }
        return moduleDD;
    }

    private DD translateCommand(Command command, Module module, int number)
    {
        DD guard = expressionToDD.translate(command.getGuard());
        if (withWeights) {
            guard = guard.toMTWith();
        }
        DD commandDD = guard;

        int action = actions.getInt(command.getLabel());
        DD actionDD = actionVariable.newIntValue(0, action);
        if (withWeights) {
            actionDD = actionDD.toMTWith();
            commandDD = commandDD.multiplyWith(actionDD);
        } else {
            commandDD = commandDD.andWith(actionDD);
        }

        boolean isMARateLine = (actions.getInt(
                new ExpressionIdentifierStandard.Builder()
                .setName(RATE)
                .build()) == action)
                && SemanticsMA.isMA(model.getSemantics());      // RATE should change for more elegant way
        int nonDetNumber = isMARateLine ? rateIndex : number;  // if MA has rate, set it with unique number
        if (transEnc == TransitionEncoding.MDP_STATE) {
            DD nondetDDPres = nondetVariables.get(module).newIntValue(0, nonDetNumber);
            if (withWeights) {
                nondetDDPres = nondetDDPres.toMTWith();
                commandDD = commandDD.multiplyWith(nondetDDPres);
            } else {
                commandDD = commandDD.andWith(nondetDDPres);
            }
        }

        DD probDD = withWeights ? ContextDD.get().newConstant(0) : ContextDD.get().newConstant(false);
        for (Alternative alternative : command.getAlternatives()) {
            DD alternativeDD = translateAlternative(alternative);
            if (withWeights) {
                probDD = probDD.addWith(alternativeDD);
            } else {
                probDD = probDD.orWith(alternativeDD);
            }
        }
        if (transEnc == TransitionEncoding.MDP_STATE) {
            if (withWeights) {
                commandDD = commandDD.multiplyWith(probDD);
            } else {
                commandDD = commandDD.andWith(probDD);
            }
        } else if (transEnc == TransitionEncoding.MC) {
            if (withWeights) {
                commandDD = commandDD.multiplyWith(probDD);
            } else {
                commandDD = commandDD.andWith(probDD);
            }
        } else {
            assert false;
        }

        int playerNr = command.getPlayer();
        if (playerNr >= 0) {
            while (players.size() < playerNr + 1) {
                players.add(ContextDD.get().newConstant(false));
            }
            DD playerDD = players.get(playerNr);
            playerDD = playerDD.orWith(expressionToDD.translate(command.getGuard()));
            players.set(playerNr, playerDD);
        }

        return commandDD;
    }

    private DD translateAlternative(Alternative alternative) {
        DD weight;
        if (withWeights) {
            weight = expressionToDD.translate(alternative.getWeight());
        } else {
            weight = ContextDD.get().newConstant(1);
        }
        DD assignments = ContextDD.get().newConstant(true);
        for (Entry<Expression,Expression> entry : alternative.getEffect().entrySet()) {
            DD assignment;
            VariableDD variableDD = variablesDD.get(entry.getKey());
            assignment = expressionToDD.assign(variableDD, 1, entry.getValue());
            assignments = assignments.andWith(assignment);
        }
        if (withWeights) {
            DD assignmentsOld = assignments;
            assignments = assignments.toMT();
            assignmentsOld.dispose();
            assignmentsOld = assignments;
            assignments = assignments.multiply(weight);
            assignmentsOld.dispose();
        } else {
            DD weightOld = weight;
            weight = weight.gt(ContextDD.get().newConstant(0));
            weightOld.dispose();
            DD assignmentsOld = assignments;
            assignments = assignments.and(weight);
            assignmentsOld.dispose();
        }
        weight.dispose();

        return assignments;
    }

    @Override
    public Permutation getSwapPresNext() {
        return nextToPres;
    }

    @Override
    public DD getNodeSpace() {
        return nodes;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        properties.close();
        closed = true;
        initial.dispose();
        transitionsBoolean.dispose();
        transitionsBooleanForNext.dispose();
        nextCube.dispose();
        presAndActions.dispose();
        nonDetStates.dispose();
        presEqNext.dispose();
        presCube.dispose();
        //        actionsCube.dispose();
        for (DD var : presVars) {
            var.dispose();
        }
        for (DD var : nextVars) {
            var.dispose();
        }
        for (DD var : transitionVars) {
            var.dispose();
        }
        states.dispose();
        expressionToDD.close();
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public DD getActionCube() {
        return actionsCube;
    }

    private DD translateStateReward(DD state, RewardStructure rewardStructure)
    {
        assert state != null;
        assert rewardStructure != null;
        DD rewardDD = ContextDD.get().newConstant(0);
        for (StateReward reward : rewardStructure.getStateRewards()) {
            DD guard = expressionToDD.translate(reward.getGuard());
            DD value = expressionToDD.translate(reward.getValue());
            DD trans = state.clone().andWith(guard).toMTWith()
                    .multiplyWith(value);
            rewardDD = rewardDD.addWith(trans);
        }
        return rewardDD;
    }

    private DD translateTransReward(DD state, RewardStructure rewardStructure)
    {
        assert state != null;
        assert rewardStructure != null;
        DD rewardDD = ContextDD.get().newConstant(0);
        for (TransitionReward reward : rewardStructure.getTransitionRewards()) {
            DD guard = expressionToDD.translate(reward.getGuard());
            DD value = expressionToDD.translate(reward.getValue());
            DD action = newLabelDD(reward.getLabel());
            DD trans = state.clone().andWith(guard).andWith(action)
                    .toMTWith().multiplyWith(value);
            rewardDD = rewardDD.addWith(trans);
        }
        return rewardDD;
    }

    private DD newLabelDD(String labelString) {
        assert labelString != null;
        Expression label = new ExpressionIdentifierStandard.Builder()
                .setName(labelString)
                .build();
        assert actions.containsKey(label);
        int action = actions.getInt(label);
        return actionVariable.newIntValue(0, action);
    }


    private static DD exploreNodeSpace(Log log,
            DD initial, DD trans, DD pres, Permutation swap)
    {
        StopWatch timer = new StopWatch(true);
        log.send(MessagesPRISM.EXPLORING);
        DD states = initial.clone();
        DD predecessors = ContextDD.get().newConstant(false);
        while (!states.equals(predecessors)) {
            predecessors.dispose();
            predecessors = states;
            DD next = trans.abstractAndExist(states, pres);
            next = next.permuteWith(swap);
            states = states.clone().orWith(next);
        }
        predecessors.dispose();
        log.send(MessagesPRISM.EXPLORING_DONE, timer.getTimeSeconds());
        return states;
    }    

    @Override
    public GraphDDProperties getProperties() {
        return properties;
    }
    
    int getPRISMGamesPlayer(SMGPlayer player) {
        assert player != null;
        Expression expression = player.getExpression();
        assert expression != null;
        assert expression instanceof ExpressionIdentifier
        || expression instanceof ExpressionLiteral;
        if (expression instanceof ExpressionLiteral) {
            ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
            assert expressionLiteral.getType().equals(ExpressionTypeInteger.TYPE_INTEGER);
            Value value = UtilEvaluatorExplicit.evaluate(expressionLiteral);
            int intValue = ValueInteger.as(value).getInt() - 1;
            assert intValue >= 0 : intValue;
            assert intValue < playerNameToNumber.size();
            return intValue;
        } else {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            String name = expressionIdentifier.getName();
            assert playerNameToNumber.containsKey(name);
            return playerNameToNumber.getInt(name);
        }
    }
    
    private static Object2IntOpenHashMap<String> computeNameToNumber(
            List<PlayerDefinition> players) {
        if (players == null) {
            return null;
        }

        Object2IntOpenHashMap<String> result = new Object2IntOpenHashMap<>();
        int playerNumber = 0;
        for (PlayerDefinition player : players) {
            result.put(player.getName(), playerNumber);
            playerNumber++;
        }
        return result;
    }
}
