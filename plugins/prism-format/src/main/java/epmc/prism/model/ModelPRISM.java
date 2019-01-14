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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMA;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.SemanticsSMG;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.JANITypeInt;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Log;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNot;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.prism.error.ProblemsPRISM;
import epmc.prism.messages.MessagesPRISM;
import epmc.prism.model.convert.PRISM2JANIConverter;
import epmc.prism.options.OptionsPRISM;
import epmc.time.JANITypeClock;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueInteger;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

// TODO probably better to do multiplicity checking just here, not in parser
// TODO can already include some checks on probabilities, though not in all case
// TODO there are almost no checks for validity of Markov automata

/**
 * PRISM model representation.
 * This file represents a PRISM high-level model. The semantical models which
 * can be represented are the ones originally from PRISM, plus Markov automata.
 * Quantum Markov chains are <emph>not</emph> handled by this model. Instead,
 * they are handled by the according file in the QMC plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelPRISM implements ModelJANIConverter {
    public final static class Builder {
        private Semantics semantics;
        private List<Module> modules;
        private Expression initialStates;
        private Map<Expression,JANIType> globalVariables;
        private Map<Expression,Expression> globalInitValues;
        private SystemDefinition system;
        private Formulas formulas;
        private List<RewardStructure> rewards;
        private List<PlayerDefinition> players;

        public Builder setSemantics(Semantics semantics) {
            this.semantics = semantics;
            return this;
        }

        private Semantics getSemantics() {
            return semantics;
        }

        public Builder setModules(List<Module> modules) {
            this.modules = modules;
            return this;
        }

        private List<Module> getModules() {
            return modules;
        }

        public Builder setInitialStates(Expression initialStates) {
            this.initialStates = initialStates;
            return this;
        }

        private Expression getInitialStates() {
            return initialStates;
        }

        public Builder setGlobalVariables(Map<Expression,JANIType> globalVariables) {
            this.globalVariables = globalVariables;
            return this;
        }

        private Map<Expression, JANIType> getGlobalVariables() {
            return globalVariables;
        }

        public Builder setGlobalInitValues(Map<Expression,Expression> globalInitValues) {
            this.globalInitValues = globalInitValues;
            return this;
        }

        private Map<Expression, Expression> getGlobalInitValues() {
            return globalInitValues;
        }

        public Builder setSystem(SystemDefinition system) {
            this.system = system;
            return this;
        }

        private SystemDefinition getSystem() {
            return system;
        }

        public Builder setFormulas(Formulas formulas) {
            this.formulas = formulas;
            return this;
        }

        private Formulas getFormulas() {
            return formulas;
        }

        public Builder setRewards(List<RewardStructure> rewards) {
            this.rewards = rewards;
            return this;
        }

        private List<RewardStructure> getRewards() {
            return rewards;
        }

        public Builder setPlayers(List<PlayerDefinition> players) {
            this.players = players;
            return this;
        }

        private List<PlayerDefinition> getPlayers() {
            return players;
        }
    }

    public final static String IDENTIFIER = "prism";
    private final static String DEADLOCK = "\"deadlock\"";
    private final static String INIT = "\"init\"";
    private final static String RATE = "rate";
    private final static String EMPTY_LABEL = "";
    private final static String SPACE = " ";
    private final static String CTMC = "ctmc";
    private final static String DTMC = "dtmc";
    private final static String MDP = "mdp";
    private final static String NEWLINE = "\n";
    private final static String _PAR_ = "_PAR_";
    private final static String UNDERSCORE = "_";

    private Semantics semanticsType;
    private final List<Module> modules = new ArrayList<>();
    private final List<Module> publicModules = Collections.unmodifiableList(modules);
    private Expression initialStates;
    private Map<Expression,JANIType> globalVariables;
    private Map<Expression,Expression> globalInitValues;
    private Map<Expression,Expression> publicGlobalInitValues;
    private SystemDefinition system;
    private boolean multipleInit;
    private Formulas formulas;
    /** used to store properties defined in property file (not for PRISM models)
     * as well as constants, formulas and labels defined there */
    private PropertiesImpl properties;
    private Set<Expression> unspecifiedConsts;
    private Map<Expression,Expression> specifiedConsts;
    private List<RewardStructure> rewards;
    private final List<PlayerDefinition> players = new ArrayList<>();
    private Expression rateIdentifier;
    private Expression rateLabel;

    public void build(Builder builder) {
        assert builder != null;
        assert builder.getSemantics() != null;
        assert builder.getModules() != null;
        assert !builder.getModules().isEmpty();
        this.rateIdentifier = new ExpressionIdentifierStandard.Builder()
                .setName(RATE)
                .build();
        this.rateLabel = rateIdentifier;
        Options options = Options.get();
        List<PlayerDefinition> players = builder.getPlayers();
        if (players != null) {
            this.players.addAll(players);
        }
        Formulas formulas = builder.getFormulas();
        Map<String,Object> optionsConsts = options.getMap(OptionsModelChecker.CONST);
        if (optionsConsts != null) {
            for (Entry<String, Object> entry : optionsConsts.entrySet()) {
                String identifier = entry.getKey();
                JANIType type = formulas.getConstantType(identifier);
                if (type != null) {
                    ensure(!formulas.isConstantDefined(identifier),
                            ProblemsPRISM.CONST_ALREADY_IN_MODEL, identifier);
                    formulas.addConstant(identifier, entry.getValue(), type);
                }
            }
        }
        this.unspecifiedConsts = new HashSet<>();
        this.specifiedConsts = new HashMap<>();
        this.globalVariables = new HashMap<>();
        this.globalInitValues = new HashMap<>();
        this.publicGlobalInitValues = Collections.unmodifiableMap(this.globalInitValues);
        Map<Expression,JANIType> globalVariables = builder.getGlobalVariables();
        if (globalVariables == null) {
            globalVariables = new HashMap<>();
        }
        for (Entry<Expression, JANIType> entry : globalVariables.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression,JANIType> entry : globalVariables.entrySet()) {
            if (!(entry.getKey() instanceof ExpressionIdentifier)) {
                throw new IllegalArgumentException();
            }
        }
        Map<Expression,Expression> globalInitValues = builder.getGlobalInitValues();
        if (globalInitValues == null) {
            globalInitValues = new HashMap<>();
        }
        for (Entry<Expression, Expression> entry : globalInitValues.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression,Expression> entry : globalInitValues.entrySet()) {
            if (!(entry.getKey() instanceof ExpressionIdentifier)) {
                throw new IllegalArgumentException();
            }
        }
        List<RewardStructure> rewards = builder.getRewards();
        if (rewards == null) {
            rewards = new ArrayList<>();
        }
        for (RewardStructure rew : rewards) {
            assert rew != null;
        }
        this.rewards = new ArrayList<>(rewards);
        formulas.check();
        formulas.expandFormulas();
        this.formulas = formulas;
        expandModules(builder.getModules());
        this.semanticsType = builder.getSemantics();
        if (SemanticsSMG.isSMG(builder.getSemantics())) {
            setPlayers(this.modules, players);
        }
        this.globalVariables.putAll(globalVariables);
        this.globalInitValues.putAll(globalInitValues);
        expandGlobalVariables();
        this.system = builder.getSystem() == null ? createDefaultSystem() : builder.getSystem();
        this.system.setModel(this);
        checkSystemDefinition();
        automataToCommands();
        if (options.getBoolean(OptionsPRISM.PRISM_FLATTEN)) {
            flatten();
        }
        Engine engine = UtilOptions.getSingletonInstance(Options.get(),
                OptionsModelChecker.ENGINE);
        if (builder.getInitialStates() != null) {
            multipleInit = true;
            Expression initialStates = builder.getInitialStates();
            initialStates = UtilExpressionStandard.replace(initialStates, formulas.getFormulas());
            initialStates = UtilExpressionStandard.replace(initialStates, specifiedConsts);
            this.initialStates = initialStates;
            ensureNoInitAtVars();
        } else {
            createDefaultInitialValues();
            multipleInit = false;
            this.initialStates = createDefaultInitialStates();
        }
        replaceRewardsConstants(formulas.getFormulas(), specifiedConsts);
        //        checkExpressionConsistency();
        // TODO following should be handled by DD engine itself
        if (engine instanceof EngineDD) {
            fixUnchangedVariables();
        }
        createProperties();
    }

    /**
     * Postprocessing steps for Markov automata.
     * Subsumes all commands labelled with "rate" to a single module, such that
     * there is a race between all the stochastic alternatives of all active
     * commands (commands with guard valid in current state). In addition,
     * the constructed command is deactivated if there are any other commands
     * active, due to the maximal progress assumption.
     * 
     * @param globalModule module to be transformed
     * @return transformed module
     */
    private ModuleCommands postprocessMA(ModuleCommands globalModule) {
        assert globalModule != null;
        List<Command> commands = new ArrayList<>();
        List<Alternative> rateAlternatives = new ArrayList<>();
        Expression rateGuard = ExpressionLiteral.getTrue();
        for (Command command : globalModule.getCommands()) {
            Expression guard = command.getGuard();
            ExpressionIdentifierStandard commandAction = (ExpressionIdentifierStandard) command.getAction();
            if (!commandAction.getName().equals(RATE)) {
                commands.add(command);
                rateGuard = opAndNot(rateGuard, guard);
            } else {
                for (Alternative alternative : command.getAlternatives()) {
                    Expression weight = alternative.getWeight();
                    weight = new ExpressionOperator.Builder()
                            .setOperator(OperatorIte.ITE)
                            .setOperands(guard, weight, ExpressionLiteral.getZero())
                            .build();
                    Map<Expression, Expression> effect = alternative.getEffect();
                    rateAlternatives.add(new Alternative(weight, effect, alternative.getPositional()));
                }
            }
        }
        commands.add(new Command(rateLabel, rateGuard, rateAlternatives, null));
        return new ModuleCommands(globalModule.getName(), globalModule.getVariables(), globalModule.getInitValues(), commands, globalModule.getInvariants(), globalModule.getPositional());
    }

    private void replaceRewardsConstants(Map<Expression, Expression> formulas,
            Map<Expression, Expression> consts) {
        Map<Expression,Expression> map = new HashMap<>();
        map.putAll(formulas);
        map.putAll(consts);
        List<RewardStructure> newRewardStructure = new ArrayList<>();
        for (RewardStructure rew : this.rewards) {
            newRewardStructure.add(rew.replace(map));
        }
        this.rewards = newRewardStructure;
    }

    private void setPlayers(List<Module> modules,
            List<PlayerDefinition> players) {
        // TODO consistency checks

        Object2IntOpenHashMap<String> moduleToPlayer = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<String> labelToPlayer = new Object2IntOpenHashMap<>();
        int playerNumber = 0;
        for (PlayerDefinition player : players) {
            for (String module : player.getModules()) {
                moduleToPlayer.put(module, playerNumber);
            }
            for (String label : player.getLabels()) {
                labelToPlayer.put(label, playerNumber);
            }
            playerNumber++;
        }

        for (Module module : modules) {
            assert module.isCommands();
            ModuleCommands moduleCommands = module.asCommands();
            String moduleName = moduleCommands.getName();
            int modulePlayer = moduleToPlayer.getInt(moduleName);
            for (Command command : moduleCommands.getCommands()) {
                ExpressionIdentifierStandard labelE = (ExpressionIdentifierStandard) command.getLabel();
                String label = labelE.getName();
                assert label != null;
                assert semanticsType != null;
                if (label.equals(EMPTY_LABEL) || (SemanticsMA.isMA(semanticsType) && label.equals(RATE))) {
                    assert moduleToPlayer.containsKey(moduleName) :
                        moduleName + SPACE + moduleToPlayer;
                    command.setPlayer(modulePlayer);
                } else {
                    assert labelToPlayer.containsKey(label) :
                        label + SPACE + labelToPlayer;
                    int labelPlayer = labelToPlayer.getInt(label);
                    command.setPlayer(labelPlayer);
                }
            }
        }
    }

    private void automataToCommands() {
        List<Module> newModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.isCommands()) {
                newModules.add(module);
            } else {
                assert false;
            }
        }
        modules.clear();
        modules.addAll(newModules);
    }

    // TODO fix for global variables
    private void fixUnchangedVariables() {
        ArrayList<ModuleCommands> newModules = new ArrayList<>();

        for (Module module : modules) {
            ModuleCommands moduleGC = module.asCommands();
            ArrayList<Command> newCommands = new ArrayList<>();
            for (Command command : moduleGC.getCommands()) {
                ArrayList<Alternative> newAlternatives = new ArrayList<>();
                for (Alternative alternative : command.getAlternatives()) {
                    Map<Expression,Expression> newEffect = new HashMap<>();
                    newEffect.putAll(alternative.getEffect());
                    for (Expression variable : moduleGC.getVariables().keySet()) {
                        if (!alternative.getEffect().containsKey(variable)) {
                            newEffect.put(variable, variable);
                        }
                    }
                    Alternative newAlternative = new Alternative(alternative.getWeight(), newEffect, null);
                    newAlternatives.add(newAlternative);
                }
                Command newCommand = new Command(command.getLabel(), command.getGuard(), newAlternatives, null);
                newCommand.setPlayer(command.getPlayer());
                newCommands.add(newCommand);
            }
            ModuleCommands newModule = new ModuleCommands
                    (moduleGC.getName(), moduleGC.getVariables(),
                            moduleGC.getInitValues(), newCommands, moduleGC.getInvariants(), null);
            newModules.add(newModule);
        }

        modules.clear();
        modules.addAll(newModules);
    }

    private void ensureNoInitAtVars() {
        for (Expression init : globalInitValues.values()) {
            ensure(init == null, ProblemsPRISM.NOT_BOTH_INITS);
        }
        for (Module module : modules) {
            for (Expression init : module.getInitValues().values()) {
                ensure(init == null, ProblemsPRISM.NOT_BOTH_INITS);
            }   
        }
    }

    private void checkSystemDefinition() {
        Set<Expression> allActions = new HashSet<>();
        Set<String> allModuleNames = new LinkedHashSet<>();
        Set<String> moduleNamesSeen = new LinkedHashSet<>();
        for (Module module : modules) {
            allActions.addAll(module.getAlphabet());
            allModuleNames.add(module.getName());
        }
        collectSystemActions(system, allActions);
        checkSystemDefinition(system, allActions, allModuleNames, moduleNamesSeen);
        for (String module : allModuleNames) {
            ensure(moduleNamesSeen.contains(module), ProblemsPRISM.MODULE_NOT_IN_SYSTEM, module);
        }
    }

    private void collectSystemActions(SystemDefinition system,
            Set<Expression> allActions) {
        if (system.isRename()) {
            SystemRename sysRen = (SystemRename) system;
            allActions.addAll(sysRen.getRenaming().values());
        }
        for (SystemDefinition child : system.getChildren()) {
            collectSystemActions(child, allActions);
        }
    }

    private void checkSystemDefinition(SystemDefinition system,
            Set<Expression> allActions,
            Set<String> allModuleNames,
            Set<String> moduleNamesSeen) {
        if (system instanceof SystemRestrictedParallel) {
            SystemRestrictedParallel parSystem = (SystemRestrictedParallel) system;
            for (Expression action : parSystem.getSync()) {
                ensure(allActions.contains(action), ProblemsPRISM.INVALID_ACTION_IN_SYSTEM, action);
            }
        } else if (system instanceof SystemHide) {
            SystemHide sysHide = (SystemHide) system;
            for (Expression action : sysHide.getHidden()) {
                ensure(allActions.contains(action), ProblemsPRISM.INVALID_ACTION_IN_SYSTEM, action);
            }
        } else if (system.isRename()) {
            SystemRename sysRen = (SystemRename) system;
            for (Expression action : sysRen.getRenaming().keySet()) {
                ensure(allActions.contains(action), ProblemsPRISM.INVALID_ACTION_IN_SYSTEM, action);
            }
        } else if (system.isModule()) {
            SystemModule sysModule = (SystemModule) system;
            String name = sysModule.getModule();
            ensure(allModuleNames.contains(name), ProblemsPRISM.INVALID_MODULE_IN_SYSTEM, name);
            ensure(!moduleNamesSeen.contains(name),
                    ProblemsPRISM.MODULE_ALREADY_IN_SYSTEM, name);
            moduleNamesSeen.add(name);
        }
        for (SystemDefinition child : system.getChildren()) {
            checkSystemDefinition(child, allActions,
                    allModuleNames, moduleNamesSeen);
        }
    }

    private void collectConstants() {
        for (Entry<Expression,Expression> entry : formulas.getConstants().entrySet()) {
            if (entry.getValue() == null) {
                unspecifiedConsts.add(entry.getKey());
            } else {
                specifiedConsts.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void expandGlobalVariables() {
        for (Entry<Expression,JANIType> entry : globalVariables.entrySet()) {
            JANIType type = entry.getValue();
            if (type != null) {
                entry.setValue(type.replace(formulas.getFormulas()).replace(specifiedConsts));
            }
        }
        for (Entry<Expression,Expression> entry : globalInitValues.entrySet()) {
            Expression init = entry.getValue();
            if (init != null) {
                entry.setValue(UtilExpressionStandard.replace(UtilExpressionStandard.replace(init, formulas.getFormulas()), specifiedConsts));
            }
        }
    }

    private void expandModules(List<Module> modules) {
        Map<String,Module> moduleByName = new LinkedHashMap<>();

        for (Module module : modules) {
            if (module.isCommands()) {
                ModuleCommands expanded = module.asCommands().replaceFormulas(formulas.getFormulas());
                this.modules.add(expanded);
                moduleByName.put(expanded.getName(), expanded);
            }
        }

        for (Module module : modules) {
            if (module instanceof ModuleRename) {
                ModuleRename rename = (ModuleRename) module;
                String name = rename.getName();
                String baseName = rename.getBase();
                Module base = moduleByName.get(baseName);
                ensure(base != null, ProblemsPRISM.BASE_NOT_FOUND, baseName, name);
                if (base.isCommands()) {
                    this.modules.add(base.asCommands().rename(name, rename.getMap()));
                }
            }
        }
        formulas.expandConstants();
        collectConstants();

        List<Module> newModules = new ArrayList<>();
        for (Module module : this.modules) {
            newModules.add(module.replaceFormulas(specifiedConsts));
        }
        this.modules.clear();
        this.modules.addAll(newModules);
    }

    boolean isMultipleInit() {
        return multipleInit;
    }

    private void createProperties() {
        properties = new PropertiesImpl(this);
        for (Entry<Expression,Expression> entry : formulas.getConstants().entrySet()) {
            ExpressionIdentifierStandard key = (ExpressionIdentifierStandard) entry.getKey();
            String name = key.getName();
            Type type = formulas.getConstantType(key.getName()).toType();
            properties.addConst(name, type, entry.getValue());
        }
        for (Entry<Expression,Expression> entry : formulas.getFormulas().entrySet()) {
            ExpressionIdentifierStandard key = (ExpressionIdentifierStandard) entry.getKey();
            String name = key.getName();
            properties.addFormula(name, entry.getValue());
        }
        for (Entry<Expression,Expression> entry : formulas.getLabels().entrySet()) {
            ExpressionIdentifierStandard key = (ExpressionIdentifierStandard) entry.getKey();
            String name = key.getName();
            properties.addLabel(name, entry.getValue());
        }
        properties.expandAndCheckWithDefinedCheck();
    }

    public Expression getInitialNodes() {
        return initialStates;
    }

    public List<Module> getModules() {
        return publicModules;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        assert semanticsType != null;
        if (SemanticsCTMC.isCTMC(semanticsType)) {
            builder.append(CTMC);            
        } else if (SemanticsDTMC.isDTMC(semanticsType)) {
            builder.append(DTMC);
        } else if (SemanticsMDP.isMDP(semanticsType)) {
            builder.append(MDP);
        } else {
            assert false;
        }
        builder.append(NEWLINE + NEWLINE);

        for (Entry<Expression,JANIType> entry : globalVariables.entrySet()) {
            //            try {
            // TODO gettypte
            builder.append(entry.getKey() + " : " + entry.getKey());
            //          } catch (EPMCException e) {
            //            e.printStackTrace();
            //          assert false;
            //    }
            if (globalInitValues.containsKey(entry.getKey())) {
                builder.append(" init " + globalInitValues.get(entry.getKey()));
            }
            builder.append(";\n");
        }

        for (Entry <Expression, Expression> entry : formulas.getConstants().entrySet()) {
            builder.append("const ").append(formulas.getConstantTypes().get(entry.getKey())).append(" ").append(entry.getKey().toString());
            if (entry.getValue()!=null)
                builder.append(" = ").append(entry.getValue().toString());
            builder.append(";\n");
        }

        for (Entry <Expression, Expression> entry : formulas.getFormulas().entrySet()) {
            builder.append("formula ").append(entry.getKey().toString()).append(" = ").append(entry.getValue().toString()).append(";\n");
        }

        for (Entry <Expression, Expression> entry : formulas.getLabels().entrySet()) {
            builder.append("label ").append(entry.getKey().toString()).append(" = ").append(entry.getValue().toString()).append(";\n");
        }

        for (Module module : modules) {
            builder.append(module + "\n");
        }
        if (multipleInit) {
            builder.append("init " + initialStates + " endinit\n\n");
        }
        builder.append("system " + system + " endsystem\n");
        return builder.toString();
    }

    @Override
    public Semantics getSemantics() {
        return semanticsType;
    }

    @Override
    public PropertiesImpl getPropertyList() {
        return properties;
    }

    private boolean isValidStateQuery(String query) {
        Expression queryExpr = new ExpressionIdentifierStandard.Builder()
                .setName(query)
                .build();
        if (globalVariables.containsKey(queryExpr)) {
            return true;
        }
        for (Module module : modules) {
            if (module.getVariables().containsKey(queryExpr)) {
                return true;
            }
        }
        if (INIT.equals(query)) {
            return true;
        }
        if (DEADLOCK.equals(query)) {
            return true;
        }

        return false;
    }

    // TODO should become superfluous in a while
    private Expression defaultInitialValue(JANIType type) {
        Expression value;
        if (type instanceof JANITypeBool) {
            value = ExpressionLiteral.getFalse();
        } else if (type instanceof JANITypeBounded) {
            JANITypeBounded typeBounded = (JANITypeBounded) type;
            value = typeBounded.getLowerBound();
        } else if (type instanceof JANITypeInt) {
            value = new ExpressionLiteral.Builder()
                    .setValue("0")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build();
        } else if (type instanceof JANITypeClock) {
            // TODO ??
            value = new ExpressionLiteral.Builder()
                    .setValue("0")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build();
        } else {
            value = null;
            assert false ;
        }
        return value;
    }

    private void createDefaultInitialValues() {
        for (Entry<Expression,JANIType> entry : globalVariables.entrySet()) {
            if (!globalInitValues.containsKey(entry.getKey())) {
                Expression value = defaultInitialValue(entry.getValue());
                globalInitValues.put(entry.getKey(), value);
            }
        }
        ArrayList<Module> newModules = new ArrayList<>();
        for (Module module : modules) {
            Map<Expression,Expression> newInitValues = new HashMap<>();
            for (Entry<Expression,JANIType> entry : module.getVariables().entrySet()) {
                if (!module.getInitValues().containsKey(entry.getKey())) {
                    Expression value = defaultInitialValue(entry.getValue());
                    newInitValues.put(entry.getKey(), value);
                } else {
                    newInitValues.put(entry.getKey(), module.getInitValues().get(entry.getKey()));
                }
            }
            Module newModule;
            if (module.isCommands()) {
                ModuleCommands commandsModule = module.asCommands();
                newModule = new ModuleCommands(module.getName(), module.getVariables(),
                        newInitValues, commandsModule.getCommands(), commandsModule.getInvariants(), null);
            } else {
                assert false;
                newModule = null;
            }
            newModules.add(newModule);
        }
        modules.clear();
        modules.addAll(newModules);
    }

    private void flatten() {
        ModuleCommands globalModule = flatten(system);
        globalVariables.putAll(globalModule.getVariables());
        globalInitValues.putAll(globalModule.getInitValues());
        globalModule = new ModuleCommands(globalModule.getName(), globalVariables,
                globalInitValues, globalModule.getCommands(), globalModule.getInvariants(), null);
        modules.clear();
        if (SemanticsMA.isMA(semanticsType)) {
            globalModule = postprocessMA(globalModule);
        }
        modules.add(globalModule);
        globalVariables.clear();
        globalInitValues.clear();
        system = new SystemModule(globalModule.getName(), null);
        system.setModel(this);
    }

    private ModuleCommands flatten(SystemDefinition system) {
        if (system.isModule()) {
            SystemModule systemModule = system.asModule();
            for (Module module : modules) {
                if (module.getName().equals(systemModule.getModule())) {
                    assert module.isCommands();
                    return module.asCommands();
                }
            }
            assert false;
            return null;
        } else if (system.isAlphaParallel()) {
            SystemAlphaParallel systemParallel = system.asAlphaParallel();
            ModuleCommands left = flatten(systemParallel.getLeft());
            ModuleCommands right = flatten(systemParallel.getRight());
            Set<Expression> labels = new HashSet<>();
            labels.addAll(left.getAlphabet());
            Set<Expression> labelsRight = right.getAlphabet();
            labels.retainAll(labelsRight);
            labels.remove(new ExpressionIdentifierStandard.Builder()
                    .setName(EMPTY_LABEL)
                    .build());
            if (SemanticsMA.isMA(semanticsType)) {
                labels.remove(rateIdentifier);
            }
            return moduleProduct(left, right, labels);
        } else if (system.isAsyncParallel()) {
            SystemAsyncParallel systemParallel = system.asAsyncParallel();
            ModuleCommands left = flatten(systemParallel.getLeft());
            ModuleCommands right = flatten(systemParallel.getRight());
            return moduleProduct(left, right, Collections.<Expression> emptySet());
        } else if (system.isRestrictedParallel()) {
            SystemRestrictedParallel systemParallel = system.asRestrictedParallel();
            ModuleCommands left = flatten(systemParallel.getLeft());
            ModuleCommands right = flatten(systemParallel.getRight());
            Set<Expression> labels = systemParallel.getSync();
            return moduleProduct(left, right, labels);            
        } else if (system.isHide()) {
            SystemHide systemHide = system.asHide();
            ModuleCommands inner = flatten(systemHide.getInner());
            return inner.hideActions(systemHide.getHidden());
        } else if (system.isRename()) {
            SystemRename systemRename = system.asRename();
            ModuleCommands inner = flatten(systemRename.getInner());
            return inner.renameActions(systemRename.getRenaming());
        } else {
            assert(false);
            return null;
        }
    }

    private ModuleCommands moduleProduct(ModuleCommands left, ModuleCommands right,
            Set<Expression> labels) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(left.getName() + _PAR_);
        int labelNr = 0;
        for (Expression label : labels) {
            nameBuilder.append(label);
            if (labelNr < labels.size() - 1) {
                nameBuilder.append(UNDERSCORE);
            }
            labelNr++;
        }
        nameBuilder.append(_PAR_ + right.getName());

        String newName = nameBuilder.toString();
        Map<Expression,JANIType> newVariables = new HashMap<>();
        newVariables.putAll(left.getVariables());
        newVariables.putAll(right.getVariables());
        Map<Expression,Expression> newInitValues = new HashMap<>();
        newInitValues.putAll(left.getInitValues());
        newInitValues.putAll(right.getInitValues());
        ArrayList<Command> newCommands = new ArrayList<>();
        List<Command> leftCmds = left.getCommands();
        List<Command> rightCmds = right.getCommands();
        Map<Expression,ArrayList<Command>> leftMap = new HashMap<>();
        Map<Expression,ArrayList<Command>> rightMap = new HashMap<>();
        for (Expression label : labels) {
            ArrayList<Command> leftList = new ArrayList<>();
            leftMap.put(label, leftList);
            ArrayList<Command> rightList = new ArrayList<>();
            rightMap.put(label, rightList);
        }

        for (Command leftCmd : leftCmds) {
            if (labels.contains(leftCmd.getLabel())) {
                ArrayList<Command> leftList = leftMap.get(leftCmd.getLabel());
                leftList.add(leftCmd);
            } else {
                newCommands.add(leftCmd);
            }
        }
        for (Command rightCmd : rightCmds) {
            if (labels.contains(rightCmd.getLabel())) {
                ArrayList<Command> rightList = rightMap.get(rightCmd.getLabel());
                rightList.add(rightCmd);
            } else {
                newCommands.add(rightCmd);
            }
        }
        for (Expression label : labels) {
            for (Command leftCmd : leftMap.get(label)) {
                for (Command rightCmd : rightMap.get(label)) {
                    ArrayList<Alternative> newAlternatives = new ArrayList<>();
                    Expression newGuard = and(leftCmd.getGuard(),
                            rightCmd.getGuard());
                    for (Alternative leftAlt : leftCmd.getAlternatives()) {
                        for (Alternative rightAlt : rightCmd.getAlternatives()) {
                            Expression newWeight = new ExpressionOperator.Builder()
                                    .setOperator(OperatorMultiply.MULTIPLY)
                                    .setOperands(
                                            leftAlt.getWeight(),
                                            rightAlt.getWeight())
                                    .build();
                            Map<Expression,Expression> newEffects = new HashMap<>();
                            newEffects.putAll(leftAlt.getEffect());
                            newEffects.putAll(rightAlt.getEffect());
                            Alternative newAlternative = new Alternative(newWeight, newEffects, null);
                            newAlternatives.add(newAlternative);
                        }
                    }
                    Expression newLabel = null;
                    newLabel = label;
                    Command newCommand = new Command(newLabel, newGuard, newAlternatives, null);
                    if (SemanticsSMG.isSMG(semanticsType)) {
                        newCommand.setPlayer(leftCmd.getPlayer());
                    }
                    newCommands.add(newCommand);
                }
            }
        }
        Expression leftInvariant = left.getInvariants();
        Expression rightInvariant = right.getInvariants();
        Expression newInvariant = null;
        if (leftInvariant != null && rightInvariant != null) {
            newInvariant = UtilExpressionStandard.opAnd(leftInvariant, rightInvariant);
        } else if (leftInvariant != null) {
            newInvariant = leftInvariant;
        } else if (rightInvariant != null) {
            newInvariant = rightInvariant;
        } else {
            newInvariant = null;
        }
        return new ModuleCommands(newName, newVariables, newInitValues, newCommands, newInvariant, null);
    }

    public Map<Expression, JANIType> getGlobalVariables() {
        return Collections.unmodifiableMap(globalVariables);
    }

    private Expression createDefaultInitialStates() {
        Expression initialStates = null;
        for (Expression var : globalVariables.keySet()) {
            Expression value = globalInitValues.get(var);
            Expression assg = eq(var, value);
            if (initialStates == null) {
                initialStates = assg;
            } else {
                initialStates = UtilExpressionStandard.opAnd(initialStates, assg);
            }
        }
        for (Module module : modules) {
            for (Expression var : module.getVariables().keySet()) {
                Expression value = module.getInitValues().get(var);
                Expression assg = eq(var, value);
                if (initialStates == null) {
                    initialStates = assg;
                } else {
                    initialStates = UtilExpressionStandard.opAnd(initialStates, assg);
                }
            }
        }
        if (initialStates == null) {
            ExpressionLiteral.getTrue();
        }
        return initialStates;
    }

    private SystemDefinition createDefaultSystem() {
        SystemDefinition system = new SystemModule(modules.get(0).getName(), null);
        for (int moduleNr = 1; moduleNr < modules.size(); moduleNr++) {
            SystemDefinition systemModule = new SystemModule(modules.get(moduleNr).getName(), null);
            system = new SystemAlphaParallel(system, systemModule, null);
        }
        return system;
    }

    public Formulas getFormulas() {
        return formulas;
    }

    Map<Expression,Expression> getGlobalInitValues() {
        return publicGlobalInitValues;
    }

    public SystemDefinition getSystem() {
        return system;
    }

    private void checkPropertiesCompatible() {
        Log log = Options.get().get(OptionsMessages.LOG);
        for (RawProperty raw : getPropertyList().getRawProperties()) {
            Expression expr = getPropertyList().getParsedProperty(raw);
            checkExpressionCompatible(expr, log);
        }
    }

    private void checkExpressionCompatible(Expression value, Log log) {
        if (value instanceof ExpressionIdentifier) {
            ensure(isValidStateQuery(value.toString()),
                    ProblemsPRISM.IDENTIFIER_UNDECLARED, value);
        } else if (value instanceof ExpressionQuantifier) {
            ExpressionQuantifier property = (ExpressionQuantifier) value;
            DirType dirType = property.getDirType();
            if (dirType == DirType.NONE) {
                switch (property.getCompareType()) {
                case IS: case EQ: case NE:
                    break;
                case GT: case GE:
                    dirType = DirType.MIN;
                    break;
                case LT: case LE:
                    dirType = DirType.MAX;
                    break;
                default:
                    assert false;
                }
            }
            ensure(!SemanticsNonDet.isNonDet(getSemantics()) || dirType != DirType.NONE,
                    ProblemsPRISM.NON_DET_QUANT_REQ_DIR);
            if (!SemanticsNonDet.isNonDet(getSemantics()) && property.getDirType() != DirType.NONE) {
                log.send(MessagesPRISM.PURE_PROB_WITH_DIR, value);
            }
        }
        if (value instanceof ExpressionCoalition) {
            ExpressionCoalition valueCoalition = (ExpressionCoalition) value;
            checkExpressionCompatible(valueCoalition.getInner(), log);
        } else if (!(value instanceof ExpressionReward)) {
            for (Expression child : value.getChildren()) {
                checkExpressionCompatible(child, log);
            }
        }
    }

    void prepareAndCheckReady() {
        properties.expandAndCheckWithDefinedCheck();
        checkPropertiesCompatible();
    }

    public List<RewardStructure> getRewards() {
        return rewards;
    }

    public RewardStructure getReward(RewardSpecification rewardSpecification) {
        assert rewardSpecification != null;
        List<RewardStructure> rewards = getRewards();
        Expression expression = rewardSpecification.getExpression();
        if (expression instanceof ExpressionIdentifier) {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            String name = expressionIdentifier.getName();
            for (RewardStructure rewardStructure : rewards) {
                if (rewardStructure.getName().equals(name)) {
                    return rewardStructure;
                }
            }
            return null;
        } else if (isTrue(expression)) {
            return rewards.get(0);
        } else {
            Value rewardValue = UtilEvaluatorExplicit.evaluate(expression);
            if (ValueInteger.is(rewardValue)) {
                int rewardIndex = ValueInteger.as(rewardValue).getInt() - 1;
                return rewards.get(rewardIndex);
            } else {
                return null;
            }
        }
    }

    @Override
    public void read(Object part, InputStream... inputs) {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        getLog().send(MessagesPRISM.START_PARSING);
        ensure(inputs.length == 1, ProblemsPRISM.PRISM_ONE_MODEL_FILE, inputs.length);
        PrismParser parser = new PrismParser(inputs[0]);    
        parser.setPart(part);
        parser.setModel(this);
        parser.parseModel();
        getLog().send(MessagesPRISM.DONE_PARSING);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public ModelJANI toJANI(boolean forExporting) {
        PRISM2JANIConverter converter = new PRISM2JANIConverter(this, forExporting);
        return converter.convert();
    }

    public List<PlayerDefinition> getPlayers() {
        return players;
    }

    private Expression and(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAnd.AND)
                .setOperands(a, b)
                .build();
    }

    private Expression eq(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorEq.EQ)
                .setOperands(a, b)
                .build();
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(expressionLiteral.getValue());
    }

    public Expression opAndNot(Expression op1, Expression op2) {
        assert op1 != null;
        assert op2 != null;
        return newOperator(OperatorAnd.AND, op1,
                newOperator(OperatorNot.NOT, op2));
    }

    private Expression newOperator(Operator operator, Expression... operands) {
        return new ExpressionOperator.Builder()
                .setOperator(operator)
                .setOperands(Arrays.asList(operands))
                .build();
    }
    
    /**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
