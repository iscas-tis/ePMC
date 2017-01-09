package epmc.guardedcommand.model.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.FilterType;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.SemanticsNonDet;
import epmc.guardedcommand.model.Alternative;
import epmc.guardedcommand.model.Command;
import epmc.guardedcommand.model.Formulas;
import epmc.guardedcommand.model.ModelGuardedCommand;
import epmc.guardedcommand.model.Module;
import epmc.guardedcommand.model.ModuleCommands;
import epmc.guardedcommand.model.RewardStructure;
import epmc.jani.extensions.derivedoperators.ModelExtensionDerivedOperators;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Assignments;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.Constant;
import epmc.jani.model.Constants;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Guard;
import epmc.jani.model.InitialStates;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.TimeProgress;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.jani.model.property.ExpressionDeadlock;
import epmc.jani.model.property.ExpressionInitial;
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeReal;
import epmc.modelchecker.Properties;
import epmc.modelchecker.PropertiesImpl;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.ValueBoolean;

// TODO optionally use "F" and "G" operators directly using extension
// TODO maybe make usage of derived operators optional

/**
 * Class to convert GuardedCommand model to JANI model representation.
 *
 * @author Ernst Moritz Hahn
 */
public final class GuardedCommand2JANIConverter {
	/** name to use for newly generated location variable. */
	final static String LOCATION_NAME = "location";
	/** Empty string. */
	final static String EMPTY = "";
	/** String to use as model name for model created. */
	private final static String MODEL_NAME = "Converted from GuardedCommand by EPMC";
	/** Name of silent identifier. */
	private final static String TAU = "τ";
	/** JANI version to which the converter converts. */
	private final static int JANI_VERSION = 1;
	private final static String INIT = "\"init\"";
	private final static String DEADLOCK = "\"deadlock\"";
	
	static String REWARD_PREFIX = "reward_";
	
	static String prefixRewardName(String name) {
		return REWARD_PREFIX + name;
	}
	
	public static void setRewardPrefix(String prefix) {
		REWARD_PREFIX = prefix;
	}
	
	/** GuardedCommand model to convert. */
	private final ModelGuardedCommand modelGuardedCommand;
	/** JANI model created. */
	private final ModelJANI modelJANI;
	/** Tau action replacing silent action. */
	private Action tauAction;

	/**
	 * Construct new converter for given GuardedCommand model.
	 * The model must not be {@code null}.
	 * 
	 * @param modelGuardedCommand GuardedCommand model to convert
	 */
	public GuardedCommand2JANIConverter(ModelGuardedCommand modelGuardedCommand) {
		assert modelGuardedCommand != null;
		this.modelGuardedCommand = modelGuardedCommand;
    	modelJANI = new ModelJANI();
		this.tauAction = new Action();
		tauAction.setModel(modelJANI);
		tauAction.setName(TAU);
	}
	
	/**
	 * Convert GuardedCommand model to JANI format.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	public ModelJANI convert() throws EPMCException {
    	modelJANI.setContext(getContextValue());
    	modelJANI.setSemantics(modelGuardedCommand.getSemantics().toString());
    	modelJANI.setName(MODEL_NAME);
    	modelJANI.setVersion(JANI_VERSION);
    	
    	convertExtensions();

		Variables globalVariables = buildGlobalVariables();
    	modelJANI.setGlobalVariables(globalVariables);
    	modelJANI.setModelConstants(buildConstants());
    	Actions actions = computeActions();
    	modelJANI.setActions(actions);
    	modelJANI.setRestrictInitial(computeInitialStates());
    	modelJANI.setAutomata(computeAutomata(actions, globalVariables));

    	convertSystem();
		convertRewards();
    	
    	modelJANI.setProperties(buildProperties());

    	return modelJANI;
	}

	private void convertExtensions() throws EPMCException {
    	List<ModelExtension> modelExtensions = new ArrayList<>();
    	ModelExtensionDerivedOperators extension = Util.getInstance(ModelExtensionDerivedOperators.class);
    	extension.setModel(modelJANI);
    	modelExtensions.add(extension);
    	modelJANI.setModelExtensions(modelExtensions);
    	modelJANI.parseBeforeModelNodeExtensions(modelJANI, null);
	}

	private void convertSystem() throws EPMCException {
    	SystemType systemType = getOptions().get(OptionsGuardedCommandConverter.GUARDEDCOMMAND_CONVERTER_SYSTEM_METHOD);
    	SystemConverter systemConverter;
    	switch (systemType) {
		case SYNCHRONISATION_VECTORS:
			systemConverter = new SystemConverterSynchronisationVectors();
			break;
		case RECURSIVE:
			systemConverter = new SystemConverterRecursive();
			break;
		default:
			assert false;
			systemConverter = null;
			break;
    	}
		systemConverter.setJANIModel(modelJANI);
		systemConverter.setGuardedCommandModel(modelGuardedCommand);
		systemConverter.convert();
	}

	private void convertRewards() throws EPMCException {
    	RewardsConverter rewardsConverter = new RewardsConverter();
    	rewardsConverter.setJANIModel(modelJANI);
    	rewardsConverter.setGuardedCommandModel(modelGuardedCommand);
    	rewardsConverter.setTauAction(tauAction);
    	rewardsConverter.attachRewards();
	}

	private Constants buildConstants() {
		PropertiesImpl properties = modelGuardedCommand.getPropertyList();
		Set<String> guardedCommandConstants = properties.getConstants().keySet();
		Constants janiConstants = new Constants();
		janiConstants.setModel(modelJANI);
		Formulas formulas = modelGuardedCommand.getFormulas();
		for (String name : guardedCommandConstants) {
			ExpressionIdentifierStandard identifier = new ExpressionIdentifierStandard.Builder()
					.setName(name)
					.build();
			JANIType type = formulas.getConstantType(name);
			if (type == null) {
				Type typeNative = properties.getConstantType(name);
				if (TypeInteger.isInteger(typeNative)) {
					type = new JANITypeInt();
				} else if (TypeBoolean.isBoolean(typeNative)) {
					type = new JANITypeBool();
				} else if (TypeReal.isReal(typeNative)) {
					type = new JANITypeReal();
				} else {
					assert false;
				}
			}
			type.setModel(modelJANI);
			type.setContextValue(getContextValue());
			Constant constant = new Constant();
			constant.setModel(modelJANI);
			constant.setName(name);
			constant.setIdentifier(identifier);
			constant.setType(type);
			Expression value = properties.getConstantValue(name);
			constant.setValue(value);
			janiConstants.put(name, constant);
		}
		return janiConstants;
	}

	private JANIProperties buildProperties() throws EPMCException {
		JANIProperties properties = new JANIProperties();
		properties.setModel(modelJANI);
		//TODO: get the right valid identifiers
		properties.setValidIdentifiers(Collections.emptyMap()); 
		Properties oldProperties = modelGuardedCommand.getPropertyList();
		Map<Expression,RawProperty> newProperties = new LinkedHashMap<>();
		for (RawProperty raw : oldProperties.getRawProperties()) {
			Expression property = useNamedRewardsOnly(oldProperties.getParsedProperty(raw));
			property = replaceSpecialIdentifiers(property);
			property = useQuantitativePropertiesOnly(property);
			List<Expression> wrapped = wrapWithFilter(property);
			if (wrapped.size() == 1) {
				newProperties.put(wrapped.get(0), raw);
				properties.addProperty(raw.getName(), property, null);
			} else if (wrapped.size() == 2) {
				RawProperty raw1 = raw.clone();
				RawProperty raw2 = raw.clone();
				raw1.setName(null);
				raw2.setName(null);
				properties.addProperty(null, wrapped.get(0), null);
				properties.addProperty(null, wrapped.get(1), null);
			} else {
				assert false;
			}
		}
		return properties;
	}
	
    private Expression replaceSpecialIdentifiers(Expression property) {
    	assert property != null;
    	Expression initLabel = new ExpressionIdentifierStandard.Builder()
    			.setName(INIT)
    			.build();
    	Expression deadlockLabel = new ExpressionIdentifierStandard.Builder()
    			.setName(DEADLOCK)
    			.build();
    	Expression initJani = new ExpressionInitial(null);
    	Expression deadlockJani = new ExpressionDeadlock(null);
    	Map<Expression,Expression> replacement = new HashMap<>();
    	replacement.put(initLabel, initJani);
    	replacement.put(deadlockLabel, deadlockJani);
    	return UtilExpressionStandard.replace(property, replacement);
	}

	/**
     * Transform the expression so to be wrapped by a filter construction.
     * 
     * @param expression the expression to wrap
     * @return a list of filter-wrapped expressions
     */
	private List<Expression> wrapWithFilter(Expression expression) {
		List<Expression> list = new LinkedList<>();
		if (ExpressionFilter.isFilter(expression)) {
			list.add(expression);
		} else {
			if (expression instanceof ExpressionQuantifier
					&& ((ExpressionQuantifier) expression).getCompareType().isIs()) {
				list.add(new ExpressionFilter.Builder()
						.setFilterType(FilterType.MIN)
						.setProp(expression)
						.setStates(ExpressionInitial.getExpressionInitial())
						.build());
                list.add(new ExpressionFilter.Builder()
                        .setFilterType(FilterType.MAX)
                        .setProp(expression)
                        .setStates(ExpressionInitial.getExpressionInitial())
                        .build());
			} else {
				//it is not a quantitative quantifier, so it should be a boolean operator
                list.add(new ExpressionFilter.Builder()
                        .setFilterType(FilterType.FORALL)
                        .setProp(expression)
                        .setStates(ExpressionInitial.getExpressionInitial())
                        .build());
			}
		}
		return list;
	}
	
    /**
     * Transform the expression so to use only named reward structures.
     * 
     * Properties can refer to reward structures by name, number, or nothing (defaulting to the first reward structure).
     * By calling this method, the obtained {@link Expression} refers to reward structures by name only.
     * 
     * @param expression the expression to convert
     * @return an equivalent expression using only named reward structures
     */
	private Expression useNamedRewardsOnly(Expression expression) throws EPMCException {
		List<Expression> oldChildren = expression.getChildren();
		List<Expression> newChildren = new ArrayList<>(oldChildren.size());
		if (expression instanceof ExpressionReward) {
	        RewardStructure reward = modelGuardedCommand.getReward(((ExpressionReward) expression).getReward());
	        newChildren.add(new ExpressionIdentifierStandard.Builder()
	        		.setName(prefixRewardName(reward.getName()))
	        		.build());
	        newChildren.add(useNamedRewardsOnly(oldChildren.get(1)));
	        newChildren.add(oldChildren.get(2));
	        newChildren.add(oldChildren.get(3));
	        
	        return expression.replaceChildren(newChildren);
		} else {
			for (Expression child:oldChildren) {
				newChildren.add(useNamedRewardsOnly(child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
    /**
     * Transform the expression so to use only quantitative properties, i.e., properties of the form {P,S}[min/max]=?.
     * 
     * Probabilistic and steady-state properties can be quantitative (e.g., P=?) or bounded (e.g., P&gt;=0.7).
     * By calling this method, the obtained {@link Expression} involves only quantitative properties, e.g., P&gt;=p[phi] is transformed to p &lt;=P=?[phi].
     * 
     * @param expression the expression to convert
     * @return an equivalent expression using only quantitative properties
     */
	private Expression useQuantitativePropertiesOnly(Expression expression) {
		if ((expression instanceof ExpressionQuantifier)
				&& !((ExpressionQuantifier) expression).getCompareType().isIs()) {
			ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
	        Expression qExp;
	        Expression rewritten;
	        switch(expressionQuantifier.getCompareType()) {
	        case GE:
	        	switch (expressionQuantifier.getDirType()) {
	        	case NONE:
	        		if (SemanticsNonDet.isNonDet(modelGuardedCommand.getSemantics())) {
	        			qExp = new ExpressionQuantifier.Builder()
	        					.setDirType(DirType.MIN)
	        					.setCmpType(CmpType.IS)
	        					.setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	        					.build();
	        		} else {
                        qExp = new ExpressionQuantifier.Builder()
                                .setDirType(expressionQuantifier.getDirType())
                                .setCmpType(CmpType.IS)
                                .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                                .build();	        			
	        		}
	        		break;
        		default:
                    qExp = new ExpressionQuantifier.Builder()
                    	.setDirType(expressionQuantifier.getDirType())
                    	.setCmpType(CmpType.IS)
                    	.setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                    	.build();                       
			        break;
	        	}
	            rewritten = new ExpressionOperator.Builder()
	            		.setOperator(getContextValue().getOperator(OperatorLe.IDENTIFIER))
	            		.setOperands(useQuantitativePropertiesOnly(expressionQuantifier.getCompare()), qExp)
	            		.build();
	            break;
	        case GT:
	        	switch (expressionQuantifier.getDirType()) {
	        	case NONE:
	        		if (SemanticsNonDet.isNonDet(modelGuardedCommand.getSemantics())) {
	                    qExp = new ExpressionQuantifier.Builder()
	                            .setDirType(DirType.MIN)
	                            .setCmpType(CmpType.IS)
	                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	                            .build();                       
	        		} else {
                        qExp = new ExpressionQuantifier.Builder()
                                .setDirType(expressionQuantifier.getDirType())
                                .setCmpType(CmpType.IS)
                                .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                                .build();                       
	        		}
	        		break;
        		default:
                    qExp = new ExpressionQuantifier.Builder()
                            .setDirType(expressionQuantifier.getDirType())
                            .setCmpType(CmpType.IS)
                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                            .build();
			        break;
	        	}
	            rewritten = new ExpressionOperator.Builder()
	            		.setOperator(getContextValue().getOperator(OperatorLt.IDENTIFIER))
	            		.setOperands(useQuantitativePropertiesOnly(expressionQuantifier.getCompare()), qExp)
	            		.build();
	            break;
	        case LE:
	        	switch (expressionQuantifier.getDirType()) {
	        	case NONE:
	        		if (SemanticsNonDet.isNonDet(modelGuardedCommand.getSemantics())) {
	                    qExp = new ExpressionQuantifier.Builder()
	                            .setDirType(DirType.MAX)
	                            .setCmpType(CmpType.IS)
	                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	                            .build();

	        		} else {
	                    qExp = new ExpressionQuantifier.Builder()
	                            .setDirType(expressionQuantifier.getDirType())
	                            .setCmpType(CmpType.IS)
	                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	                            .build();
	        		}
	        		break;
        		default:
                    qExp = new ExpressionQuantifier.Builder()
                            .setDirType(expressionQuantifier.getDirType())
                            .setCmpType(CmpType.IS)
                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                            .build();
			        break;
	        	}
	            rewritten = new ExpressionOperator.Builder()
	            		.setOperator(getContextValue().getOperator(OperatorLe.IDENTIFIER))
	            		.setOperands(qExp, useQuantitativePropertiesOnly(expressionQuantifier.getCompare()))
	            		.build();
	            break;
	        case LT:
	        	switch (expressionQuantifier.getDirType()) {
	        	case NONE:
	        		if (SemanticsNonDet.isNonDet(modelGuardedCommand.getSemantics())) {
	                    qExp = new ExpressionQuantifier.Builder()
	                            .setDirType(DirType.MAX)
	                            .setCmpType(CmpType.IS)
	                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	                            .build();
	        		} else {
	                    qExp = new ExpressionQuantifier.Builder()
	                            .setDirType(expressionQuantifier.getDirType())
	                            .setCmpType(CmpType.IS)
	                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
	                            .build();
	        		}
	        		break;
        		default:
                    qExp = new ExpressionQuantifier.Builder()
                            .setDirType(expressionQuantifier.getDirType())
                            .setCmpType(CmpType.IS)
                            .setQuantified(useQuantitativePropertiesOnly(expressionQuantifier.getQuantified()))
                            .build();
			        break;
	        	}
	            rewritten = new ExpressionOperator.Builder()
	            		.setOperator(getContextValue().getOperator(OperatorLt.IDENTIFIER))
	            		.setOperands(qExp, useQuantitativePropertiesOnly(expressionQuantifier.getCompare()))
	            		.build();
	            break;
	        default:
	        	//this should never happen...
	            rewritten = expression;
	        }
	        return rewritten;
		} else {
			List<Expression> oldChildren = expression.getChildren();
			List<Expression> newChildren = new ArrayList<>(oldChildren.size());
			for (Expression child:oldChildren) {
				newChildren.add(useQuantitativePropertiesOnly(child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
	private Automata computeAutomata(Actions actions, Variables globalVariables) throws EPMCException {
		Automata automata = new Automata();
		automata.setModel(modelJANI);
		int number = 0;
    	for (Module module : modelGuardedCommand.getModules()) {
    		assert module.isCommands();
    		ModuleCommands moduleCommands = (ModuleCommands) module;
    		Automaton automaton = moduleToAutomaton(moduleCommands, actions, globalVariables);
    		automaton.setNumber(number);
    		assert automaton != null;
        	modelJANI.getAutomata().addAutomaton(automaton);
        	automata.addAutomaton(automaton);
        	number++;
    	}
    	return automata;
	}

	private InitialStates computeInitialStates() {
		InitialStates initialStates = new InitialStates();
		initialStates.setModel(modelJANI);
		initialStates.setExp(modelGuardedCommand.getInitialNodes());
		return initialStates;
	}

	/**
	 * Collect global variables.
	 * This includes the global variables of the GuardedCommand model. It curently also 
	 * includes all local variables of modules, because these are potentially
	 * read and written by several modules.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	private Variables buildGlobalVariables() throws EPMCException {
		Variables globalVariables = new Variables();

    	for (Entry<Expression, JANIType> entry : modelGuardedCommand.getGlobalVariables().entrySet()) {
    		Variable variable = convertVariable((ExpressionIdentifierStandard) entry.getKey(), entry.getValue(), null);
    		globalVariables.addVariable(variable);
    	}
    	for (Module module : modelGuardedCommand.getModules()) {
    		assert module.isCommands();
    		for (Entry<Expression, JANIType> entry : module.getVariables().entrySet()) {
    			Expression varInit = module.getInitValues().get(entry.getKey());
    			Variable variable = convertVariable((ExpressionIdentifierStandard) entry.getKey(), entry.getValue(), varInit);
    			globalVariables.addVariable(variable);
    		}
		}
    	return globalVariables;
	}

	private Variable convertVariable(ExpressionIdentifierStandard identifier, JANIType type,
			Expression varInit) throws EPMCException {
		String varName = identifier.getName();
		type.setModel(modelJANI);
		Variable variable = new Variable();
		variable.setModel(modelJANI);
		variable.setName(varName);
		variable.setIdentifier(identifier);
		variable.setAutomaton(null);
		variable.setType(type);
		return variable;
	}

	private Actions computeActions() {
		Actions result = new Actions();
		for (Module module : modelGuardedCommand.getModules()) {
			ModuleCommands moduleCommands = (ModuleCommands) module;
			for (Command command : moduleCommands.getCommands()) {
				ExpressionIdentifierStandard exprAct = (ExpressionIdentifierStandard) command.getAction();
				if (!result.containsKey(exprAct)
						&& !exprAct.getName().equals(EMPTY)) {
					Action action = new Action();
					action.setName(exprAct.getName());
					result.addAction(action);
					result.addAction(action);
				} else if (!result.containsKey(exprAct)
						&& exprAct.getName().equals(EMPTY)) {
					result.addAction(tauAction);
				}
			}
		}
		return result;
	}

	/**
	 * Convert a single module to an automaton.
	 * 
	 * @param module module to convert to automaton
	 * @param actions
	 * @param globalVariables 
	 * @return automaton converted from module
	 * @throws EPMCException thrown in case of problems
	 */
	private Automaton moduleToAutomaton(ModuleCommands module, Actions actions,
			Variables globalVariables) throws EPMCException {
		Automaton automaton = new Automaton();
		Edges edges = new Edges();
		edges.setModel(modelJANI);
		automaton.setModel(modelJANI);
		Location location = new Location();
		location.setModel(modelJANI);
		location.setName(LOCATION_NAME);
		automaton.getLocations().add(location);
		if (module.getInvariants() != null
			&& !isTrue(module.getInvariants())) {
			//AT: in GuardedCommand, these are called invariants; 
			//in JANI, time progress (conditions)
			TimeProgress timeProgress = new TimeProgress();
			timeProgress.setModel(modelJANI);
			Expression invEx = guardedCommand2jani(module.getInvariants());
			timeProgress.setExp(invEx);
			location.setTimeProgress(timeProgress);
		}
		Set<Location> initialLocations = new LinkedHashSet<>();
		initialLocations.add(location);
		automaton.setInitialLocations(initialLocations);
	
		for (Command command : module.getCommands()) {
			Edge edge = new Edge();
			edge.setModel(modelJANI);
			Action action = actions.get(((ExpressionIdentifierStandard) (command.getAction())).getName());
			if (action == null) {
				action = tauAction;
			}
			edge.setAction(action);
			edge.setLocation(location);
			Guard guard = new Guard();
			guard.setModel(modelJANI);
			guard.setExp(command.getGuard());
			edge.setGuard(guard);
			Destinations destinations = edge.getDestinations();
			
			for (Alternative alternative : command.getAlternatives()) {
				Destination destination = new Destination();
				destination.setModel(modelJANI);
				destination.setLocation(location);
				Assignments assignments = new Assignments();
				assignments.setModel(modelJANI);
				for (Entry<Expression, Expression> entry : alternative.getEffect().entrySet()) {
					Variable variable = globalVariables.get(((ExpressionIdentifierStandard) entry.getKey()).getName());
					assert variable != null;
					AssignmentSimple assignment = new AssignmentSimple();
					assignment.setModel(modelJANI);
					assignment.setRef(variable);
					assignment.setValue(entry.getValue());
					assignments.addAssignment(assignment);
				}
				destination.setAssignments(assignments);
				destinations.addDestination(destination);
			}
			edges.addEdge(edge);
		}
		automaton.setEdges(edges);
		automaton.setName(module.getName());
		return automaton;
	}

	/**
	 * Replaces parts of expression from GuardedCommand model not supported in JANI.
	 * The expression parameter must no be {@code null}.
	 * 
	 * @param expression expression to be converted
	 * @return converted expression directly representable in JANI
	 * @throws EPMCException 
	 */
	private Expression guardedCommand2jani(Expression expression) throws EPMCException {
		if (expression instanceof ExpressionOperator 
				&& ((ExpressionOperator) expression).getOperator()
				.getIdentifier()
				.equals(OperatorAddInverse.IDENTIFIER)) {
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
			Expression operand = guardedCommand2jani(expressionOperator.getOperand1());
			Expression zero = new ExpressionLiteral.Builder()
					.setValue(TypeReal.get(getContextValue()).getZero())
					.build();
			return new ExpressionOperator.Builder()
					.setOperator(getContextValue().getOperator(OperatorSubtract.IDENTIFIER))
					.setOperands(zero, operand)
					.build();
		} else {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(guardedCommand2jani(child));
			}
			return expression.replaceChildren(newChildren);
		}
	}
	
	Action getTauAction() {
		return tauAction;
	}
	
	/**
	 * Get value context used.
	 * 
	 * @return value context used
	 */
	private ContextValue getContextValue() {
		return modelGuardedCommand.getContextValue();
	}
	
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
    }

	/**
	 * Get options used for GuardedCommand models.
	 * 
	 * @return options used
	 */
	private Options getOptions() {
		return modelGuardedCommand.getContextValue().getOptions();
	}
}
