package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.rddl.expression.ContextExpressionRDDL;
import epmc.rddl.value.ContextValueRDDL;
import epmc.rddl.value.OperatorDistributionBernoulli;
import epmc.rddl.value.OperatorDistributionDiracDelta;
import epmc.rddl.value.OperatorDistributionDiscrete;
import epmc.rddl.value.OperatorDistributionKronDelta;
import epmc.rddl.value.OperatorDistributionNormal;
import epmc.rddl.value.OperatorSwitch;
import epmc.rddl.value.TypeRDDLEnum;
import epmc.rddl.value.TypeRDDLObject;
import epmc.rddl.value.ValueRDDLEnum;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAnd;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import rddl.RDDL;
import rddl.RDDL.AGG_EXPR;
import rddl.RDDL.BOOL_CONST_EXPR;
import rddl.RDDL.BOOL_EXPR;
import rddl.RDDL.Bernoulli;
import rddl.RDDL.CASE;
import rddl.RDDL.COMP_EXPR;
import rddl.RDDL.CONN_EXPR;
import rddl.RDDL.CPF_DEF;
import rddl.RDDL.DOMAIN;
import rddl.RDDL.DiracDelta;
import rddl.RDDL.Discrete;
import rddl.RDDL.ENUM_TYPE_DEF;
import rddl.RDDL.ENUM_VAL;
import rddl.RDDL.EXPR;
import rddl.RDDL.IF_EXPR;
import rddl.RDDL.INSTANCE;
import rddl.RDDL.INT_CONST_EXPR;
import rddl.RDDL.KronDelta;
import rddl.RDDL.LCONST;
import rddl.RDDL.LTERM;
import rddl.RDDL.LTYPED_VAR;
import rddl.RDDL.LVAR;
import rddl.RDDL.NEG_EXPR;
import rddl.RDDL.NONFLUENTS;
import rddl.RDDL.Normal;
import rddl.RDDL.OBJECTS_DEF;
import rddl.RDDL.OBJ_COMP_EXPR;
import rddl.RDDL.OPER_EXPR;
import rddl.RDDL.PVARIABLE_ACTION_DEF;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVARIABLE_INTERM_DEF;
import rddl.RDDL.PVARIABLE_OBS_DEF;
import rddl.RDDL.PVARIABLE_STATE_DEF;
import rddl.RDDL.PVAR_EXPR;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.PVAR_NAME;
import rddl.RDDL.QUANT_EXPR;
import rddl.RDDL.REAL_CONST_EXPR;
import rddl.RDDL.SWITCH_EXPR;
import rddl.RDDL.TVAR_EXPR;
import rddl.RDDL.TYPE_DEF;
import rddl.RDDL.TYPE_NAME;

final class ModelBuilder {
    public final static String BOOL = "bool";
    public final static String INT = "int";
    public final static String REAL = "real";
    private final static String ENUM = "enum";
    private final static String SUM = "sum";
    private final static String PROD = "prod";
    private final static String OBJECT = "object";
    private final static String FALSE = "false";
    private final static String TRUE = "true";
    private final static String FORALL = "forall";
    private final static String EXISTS = "exists";
    private final static String OP_RDDL_AND = "^";
    private final static String OP_RDDL_IFF = "<=>";
    private final static String OP_RDDL_ADD = "+";
    private final static String OP_RDDL_SUBTRACT = "-";
    private final static String OP_RDDL_MULTIPLY = "*";
    private final static String OP_RDDL_DIVIDE = "/";
    private final static String OP_RDDL_IMPLIES = "=>";
    private final static String OP_RDDL_OR = "|";
    private final static String OP_RDDL_LT = "<";
    private final static String OP_RDDL_LE = "<=";
    private final static String OP_RDDL_EQ = "==";
    private final static String OP_RDDL_GE = ">=";
    private final static String OP_RDDL_GT = ">";
    private final static String OP_RDDL_NE = "~=";
    private final static String DEFENUM = "%defenum";
    private final static String NULL = "null";
    private final static String SPACE = " ";
    private final List<RDDL> rddls = new ArrayList<>();
    private final Map<String, Domain> domains = new LinkedHashMap<>();
    private final Map<String, Domain> domainsExternal = Collections.unmodifiableMap(domains);
    private final Map<String, Instance> instances = new LinkedHashMap<>();
    private final Map<String, Instance> instancesExternal = Collections.unmodifiableMap(instances);
    private final Map<String, NonFluents> nonFluents = new LinkedHashMap<>();
    private final Map<String, NonFluents> nonFluentsExternal = Collections.unmodifiableMap(nonFluents);
    private ContextValue contextValue;
    private ContextValueRDDL contextValueRDDL;
    private ContextExpressionRDDL contextExpressionRDDL;

    public void setRDDL(List<RDDL> rddls) {
        assert rddls != null;
        for (RDDL rddl : rddls) {
            assert rddl != null;
        }
        this.rddls.clear();
        this.rddls.addAll(rddls);
    }

    public void setContextExpressionRDDL(ContextExpressionRDDL contextExpressionRDDL) {
        this.contextExpressionRDDL = contextExpressionRDDL;
        this.contextValueRDDL = contextExpressionRDDL.getContextValueRDDL();
    }
    
    public void build() throws EPMCException {
        this.contextValue = contextExpressionRDDL.getContextValue();
        for (RDDL rddl : rddls) {
            for (Entry<String, DOMAIN> entry : rddl._tmDomainNodes.entrySet()) {
                this.domains.put(entry.getKey(), buildDomain(entry.getValue()));
            }
        }
        for (RDDL rddl : rddls) {
            for (Entry<String, NONFLUENTS> entry : rddl._tmNonFluentNodes.entrySet()) {
                this.nonFluents.put(entry.getKey(), buildNonFluents(entry.getValue()));
            }
        }
        for (RDDL rddl : rddls) {
            for (Entry<String, INSTANCE> entry : rddl._tmInstanceNodes.entrySet()) {
                this.instances.put(entry.getKey(), buildInstance(entry.getValue()));
            }
        }
    }

    private NonFluents buildNonFluents(NONFLUENTS _nonFluents) throws EPMCException {
    	assert _nonFluents != null;
        NonFluents nonFluents = new NonFluents();
        nonFluents.setName(_nonFluents._sName);
        String domainName = _nonFluents._sDomain;
        Domain domain = this.domains.get(domainName);
        assert domain != null : domainName + SPACE + domains.keySet();
        nonFluents.setDomain(domain);
        for (Entry<TYPE_NAME, OBJECTS_DEF> entry : _nonFluents._hmObjects.entrySet()) {
            String objectName = entry.getKey()._STypeName;
            List<String> objectValues = new ArrayList<>();
            for (LCONST e : entry.getValue()._alObjects) {
                objectValues.add(e._sConstValue);
            }
            nonFluents.setObjectValues(objectName, objectValues);
        }
        for (PVAR_INST_DEF entry : _nonFluents._alNonFluents) {
            String nonFluentName = entry._sPredName.toString();
            List<String> parametersValues = new ArrayList<>();
            for (LCONST e : entry._alTerms) {
                parametersValues.add(e._sConstValue);
            }
            PVariable pVariable = domain.getPVariable(nonFluentName);
            assert pVariable != null : nonFluentName;
            Type type = pVariable.getType();
            Value value = buildValue(type, entry._oValue);
            NonFluent nonFluent = domain.getNonFluent(nonFluentName);
            assert nonFluent != null : nonFluentName;
        	int pNumParameters = pVariable.getParameterStrings().size();
            if (parametersValues.size() == 0 && pNumParameters > 0) {
            	enumerateValues(nonFluents, pVariable, nonFluent, value);
            } else {
            	nonFluents.setNonFluent(nonFluent, parametersValues, value);
            }
        }
        return nonFluents;
    }

	private void enumerateValues(NonFluents nonFluents, PVariable pVariable, NonFluent nonFluent, Value value) {
    	List<String> parameters = pVariable.getParameterStrings();
    	int[] parameterSizes = new int[parameters.size()];
    	for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
    		String paramName = parameters.get(paramNr);
    		parameterSizes[paramNr] = nonFluents.getObjectValue(paramName).size();
    	}
    	int[] paramIntValues = newAssignmentArray(parameterSizes);
    	int numValues = computeNumCopies(parameterSizes);
        List<String> parametersValues = new ArrayList<>();
    	for (int valueNr = 0; valueNr < numValues; valueNr++) {
    		parametersValues.clear();
    		setAssignmentFromInt(paramIntValues, valueNr, parameterSizes);
        	for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
            	List<RDDLObjectValue> pValues = nonFluents.getObjectValue(parameters.get(paramNr));
            	int paramIntValue = paramIntValues[paramNr];
        		String paramValueName = pValues.get(paramIntValue).getName();
        		parametersValues.add(paramValueName);
        	}
        	nonFluents.setNonFluent(nonFluent, parametersValues, value);
    	}		
	}

	private int[] newAssignmentArray(int[] parameters) {
    	assert parameters != null;
    	return new int[parameters.length];
	}
	
	private int computeNumCopies(int[] parameters) {
    	assert parameters != null;
    	int size = 1;
    	for (int parameter : parameters) {
    		size *= parameter;
    	}
    	return size;
	}


	private void setAssignmentFromInt(int[] assignment, int number, int[] parameters) {
    	int remaining = number;
    	int parameterNr = 0;
    	for (int parameter : parameters) {
    		int paramValue = remaining % parameter;
    		remaining /= parameter;
    		assignment[parameterNr] = paramValue;
    		parameterNr++;
    	}
    }

    private Domain buildDomain(DOMAIN domain) throws EPMCException {
        assert domain != null;
        Domain result = new Domain();
        result.setContextExpressionRDDL(contextExpressionRDDL);
        result.setRewardDeterministic(domain._bRewardDeterministic);
        result.setMultiAction(domain._bConcurrent);
        result.setContinuousVariables(domain._bContinuous);
        result.setProbabilistic(!domain._bCPFDeterministic);
        result.setUsesIntegers(domain._bInteger);
        result.setUsesEnums(domain._bMultivalued);
        result.setHasIntermediateNodes(domain._bIntermediateNodes);
        result.setPartiallyObservable(domain._bPartiallyObserved);
        result.setHasStateConstraints(domain._bStateConstraints);
        result.setName(domain._sDomainName);
        result.setCPFHeader(domain._sCPFHeader);
        buildTypes(result, domain._hmTypes);
        buildPVariables(result, domain._hmPVariables);
        buildStateActionConstraints(result, domain._alStateConstraints);
        result.setReward(buildExpression(result, domain._exprReward));
        buildCpfs(result, domain._hmCPF);

        return result;
    }

    private void buildStateActionConstraints(Domain domain,
            List<BOOL_EXPR> _alStateConstraints) throws EPMCException {
        for (BOOL_EXPR rddlExpr : _alStateConstraints) {
            domain.addStateActionConstraint(buildExpression(domain, rddlExpr));
        }
    }

    private void buildTypes(Domain domain, HashMap<TYPE_NAME, TYPE_DEF> _hmTypes) {
        domain.addType(BOOL, TypeBoolean.get(contextValue));
        domain.addType(INT, TypeInteger.get(contextValue));
        domain.addType(REAL, TypeReal.get(contextValue));
        for (Entry<TYPE_NAME, TYPE_DEF> entry : _hmTypes.entrySet()) {
            TYPE_DEF _type = entry.getValue();
            if (_type._sType.equals(ENUM)) {
                ENUM_TYPE_DEF _enum = (ENUM_TYPE_DEF) _type;
                for (ENUM_VAL value : _enum._alPossibleValues) {
                    assert contextValueRDDL != null;
                    contextValueRDDL.addEnumConstant(value._sConstValue);
                }
            }
        }
        domain.addType(DEFENUM, contextValueRDDL.newTypeEnum(DEFENUM, contextValueRDDL.getNumberToEnumConstant()));
        
        for (Entry<TYPE_NAME, TYPE_DEF> entry : _hmTypes.entrySet()) {
            TYPE_NAME _name = entry.getKey();
            TYPE_DEF _type = entry.getValue();
            switch (_type._sType) {
            case ENUM:
                ENUM_TYPE_DEF _enum = (ENUM_TYPE_DEF) _type;
                List<String> values = new ArrayList<>();
                for (ENUM_VAL value : _enum._alPossibleValues) {
                    values.add(value._sConstValue);
                }
                domain.addType(_type._sName._STypeName, contextValueRDDL.newTypeEnum(_name._STypeName, values));
                break;
            case OBJECT:
//                OBJECT_TYPE_DEF _object = (OBJECT_TYPE_DEF) _type;
                domain.addType(_type._sName._STypeName, contextValueRDDL.newTypeObject(_name._STypeName));
                break;
            default:
                assert false : _type._sType;
            }
        }
    }

    private void buildPVariables(Domain result,
            Map<PVAR_NAME, PVARIABLE_DEF> _hmPVariables) throws EPMCException {
        for (PVARIABLE_DEF entry : _hmPVariables.values()) {
            List<String> parameters = new ArrayList<>();
            for (TYPE_NAME var : entry._alParamTypes) {
                parameters.add(var._STypeName);
            }
            String name = entry._sName._sPVarName;
            Type type = result.getType(entry._sRange._STypeName);
            assert type != null : entry._sRange._STypeName;
            if (entry instanceof PVARIABLE_STATE_DEF) {
                PVARIABLE_STATE_DEF stateEntry = (PVARIABLE_STATE_DEF) entry;
                if (!stateEntry._bNonFluent) {
                    StateFluent state = new StateFluent();
                    state.setName(name);
                    state.setType(type);
                    state.setDefault(buildValue(type, stateEntry._oDefValue));
                    state.setParameters(parameters);
                    result.addStateFluent(state);
                } else {
                    NonFluent constant = new NonFluent();
                    constant.setName(name);
                    constant.setType(type);
                    constant.setDefault(buildValue(type, stateEntry._oDefValue));
                    constant.setParameters(parameters);
                    result.addNonFluent(constant);
                }
            } else if (entry instanceof PVARIABLE_ACTION_DEF) {
                PVARIABLE_ACTION_DEF actionEntry = (PVARIABLE_ACTION_DEF) entry;
                ActionFluent action = new ActionFluent();
                action.setName(name);
                action.setType(type);
                action.setDefault(buildValue(type, actionEntry._oDefValue));
                action.setParameters(parameters);
                result.addActionFluent(action);
            } else if (entry instanceof PVARIABLE_INTERM_DEF) {
                PVARIABLE_INTERM_DEF interm = (PVARIABLE_INTERM_DEF) entry;
                IntermediateFluent fluent = new IntermediateFluent();
                fluent.setName(name);
                fluent.setType(type);
                fluent.setLevel(interm._nLevel);
                fluent.setParameters(parameters);
                result.addIntermediateFluent(fluent);
            } else if (entry instanceof PVARIABLE_OBS_DEF) {
                ObserverFluent fluent = new ObserverFluent();
                fluent.setName(name);
                fluent.setType(type);
                fluent.setParameters(parameters);
                result.addObserverFluent(fluent);
            } else {
                assert false : entry;
            }
        }
    }

    private Value buildValue(Type type, Object object) throws EPMCException {
        assert type != null;
        Value value = type.newValue();
        if (TypeBoolean.isBoolean(type)) {
            assert object instanceof Boolean;
            ValueBoolean.asBoolean(value).set((Boolean) object ? true : false);
        } else if (TypeReal.isReal(type)) {
            String string = object.toString().trim();
            if (string.equals(FALSE)) {
                ValueReal.asReal(value).set(0);
            } else if (string.equals(TRUE)) {
                ValueReal.asReal(value).set(1);                
            } else {
                value.set(string);
            }
        } else if (type instanceof TypeRDDLEnum) {
            value.set(object.toString());
        } else {
            assert false : type + SPACE + object;
        }
        return value;
    }

    private void buildCpfs(Domain result, HashMap<PVAR_NAME, CPF_DEF> _hmCPF) throws EPMCException {
        for (CPF_DEF entry : _hmCPF.values()) {
            String name = entry._exprVarName._sName._sPVarName;
            List<Expression> parameters = new ArrayList<>();
            for (LTERM e : entry._exprVarName._alTerms) {
            	Expression parameter = new ExpressionIdentifierStandard.Builder()
            			.setName(e.toString())
            			.build();
                parameters.add(parameter);
            }
            Expression expression = buildExpression(result, entry._exprEquals);
            result.addCpfs(name, parameters, expression);
        }
    }

    private Expression buildExpression(Domain domain, EXPR rddlExpr) throws EPMCException {
        if (rddlExpr instanceof IF_EXPR) {
            return newIfExpr(domain, (IF_EXPR) rddlExpr);
        } else if (rddlExpr instanceof Bernoulli) {
            return newBernoulli(domain, (Bernoulli) rddlExpr);
        } else if (rddlExpr instanceof CONN_EXPR) {
        	return newConnExpr(domain, (CONN_EXPR) rddlExpr);
        } else if (rddlExpr instanceof NEG_EXPR) {
        	return newNegExpr(domain, (NEG_EXPR) rddlExpr);
        } else if (rddlExpr instanceof PVAR_EXPR) {
            return newExpressionQuantifiedIdentifier(domain, (PVAR_EXPR) rddlExpr);
        } else if (rddlExpr instanceof INT_CONST_EXPR) {
            return newIntConstExpr(domain, (INT_CONST_EXPR) rddlExpr);
        } else if (rddlExpr instanceof REAL_CONST_EXPR) {
            return newRealConstExpr(domain, (REAL_CONST_EXPR) rddlExpr);
        } else if (rddlExpr instanceof KronDelta) {
            return newKronDeltaExpr(domain, (KronDelta) rddlExpr);
        } else if (rddlExpr instanceof OPER_EXPR) {
            return newOperExpr(domain, (OPER_EXPR) rddlExpr);
        } else if (rddlExpr instanceof COMP_EXPR) {
        	return newCompExpr(domain, (COMP_EXPR) rddlExpr);
        } else if (rddlExpr instanceof ENUM_VAL) {
        	return newEnumValExpr(domain, (ENUM_VAL) rddlExpr);
        } else if (rddlExpr instanceof SWITCH_EXPR) {
            return newSwitchExpr(domain, (SWITCH_EXPR) rddlExpr);
        } else if (rddlExpr instanceof Normal) {
            return newNormalDistrExpr(domain, (Normal) rddlExpr);
        } else if (rddlExpr instanceof Discrete) {
        	return newDiscreteDistrExpr(domain, (Discrete) rddlExpr);
        } else if (rddlExpr instanceof QUANT_EXPR) {
        	return newQuantExpr(domain, (QUANT_EXPR) rddlExpr);
        } else if (rddlExpr instanceof AGG_EXPR) {
        	return newAggExpr(domain, (AGG_EXPR) rddlExpr);
        } else if (rddlExpr instanceof BOOL_CONST_EXPR) {
        	return newBoolConstExpr(domain, (BOOL_CONST_EXPR) rddlExpr);
        } else if (rddlExpr instanceof OBJ_COMP_EXPR) {
        	return newCompExpr(domain, (OBJ_COMP_EXPR) rddlExpr);
        } else if (rddlExpr instanceof DiracDelta) {
            return newDiracDeltaDistrExpr(domain, (DiracDelta) rddlExpr);
        } else if (rddlExpr instanceof LVAR) {
        	return newLVarExpr(domain, (LVAR) rddlExpr);
        } else if (rddlExpr instanceof TVAR_EXPR) {
        	return newTVarExpr(domain, (TVAR_EXPR) rddlExpr);
        } else {
            assert false : rddlExpr.getClass() + SPACE + rddlExpr;
        }
        return null;
    }

    private Expression newTVarExpr(Domain domain, TVAR_EXPR rddlExpr) throws EPMCException {
    	assert domain != null;
    	assert rddlExpr != null;
        Expression name = new ExpressionIdentifierStandard.Builder()
        		.setName(rddlExpr._sName._sPVarName)
        		.build();
        List<Expression> parameters = new ArrayList<>();
        for (LTERM entry : rddlExpr._alTerms) {
        	Expression parameter = buildExpression(domain, entry);
            parameters.add(parameter);
        }
        return contextExpressionRDDL.newExpressionQuantifiedIdentifier(name, parameters);
	}

	private Expression newLVarExpr(Domain domain, LVAR rddlExpr) {
    	assert domain != null;
    	assert rddlExpr != null;
    	return new ExpressionIdentifierStandard.Builder()
    			.setName(rddlExpr._sVarName)
    			.build();
	}

	private Expression newDiracDeltaDistrExpr(Domain domain, DiracDelta _kro) throws EPMCException {
    	assert domain != null;
    	assert _kro != null;
        Expression inner = buildExpression(domain, _kro._exprRealValue);
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorDistributionDiracDelta.IDENTIFIER))
        		.setOperands(inner)
        		.build();
	}

	private Expression newCompExpr(Domain domain, OBJ_COMP_EXPR _comp) {
    	assert domain != null;
    	assert _comp != null;
        Operator operator = buildOperator(_comp._comp);
        Expression id1 = new ExpressionIdentifierStandard.Builder()
        		.setName(_comp._t1.toString())
        		.build();
        Expression id2 = new ExpressionIdentifierStandard.Builder()
        		.setName(_comp._t2.toString())
        		.build();
        return new ExpressionOperator.Builder()
        		.setOperator(operator)
        		.setOperands(id1, id2)
        		.build();
	}

	private Expression newBoolConstExpr(Domain domain, BOOL_CONST_EXPR _bce) {
    	assert domain != null;
    	assert _bce != null;
        boolean value = _bce._bValue;
        return new ExpressionLiteral.Builder()
                .setValue(getBoolean(value))
                .build();
	}

	private Expression newAggExpr(Domain domain, AGG_EXPR _agg) throws EPMCException {
    	assert domain != null;
    	assert _agg != null;
        Operator operator;
        Expression initialValue = null;
        switch (_agg._op) {
        case SUM:
            operator = contextValue.getOperator(OperatorAdd.IDENTIFIER);
            initialValue = new ExpressionLiteral.Builder()
            		.setValue(getInteger(0))
            		.build();
            break;
        case PROD:
            operator = contextValue.getOperator(OperatorMultiply.IDENTIFIER);
            initialValue = new ExpressionLiteral.Builder()
            		.setValue(getInteger(1))
            		.build();
            break;
        default:
            operator = null;
            assert false;
        }
        Expression over = buildExpression(domain, _agg._e);
        List<Expression> parameters = new ArrayList<>();
        List<TypeRDDLObject> ranges = new ArrayList<>();
        for (LTYPED_VAR entry : _agg._alVariables) {
            TypeRDDLObject type = (TypeRDDLObject) domain.getType(entry._sType._STypeName);
            String name = entry._sVarName._sVarName;
            Expression parameter = new ExpressionIdentifierStandard.Builder()
            		.setName(name)
            		.build();
            parameters.add(parameter);
            ranges.add(type);
        }
        return contextExpressionRDDL.newExpressionQuantifier(operator, parameters, ranges, initialValue, over);
	}

	private Expression newQuantExpr(Domain domain, QUANT_EXPR _quant) throws EPMCException {
    	assert domain != null;
    	assert _quant != null;
        Operator operator;
        Expression initialValue = null;
        switch (_quant._sQuantType) {
        case FORALL:
            operator = contextValue.getOperator(OperatorAnd.IDENTIFIER);
            initialValue = new ExpressionLiteral.Builder()
                    .setValue(getBoolean(true))
                    .build();
            break;
        case EXISTS:
            operator = contextValue.getOperator(OperatorOr.IDENTIFIER);
            initialValue = new ExpressionLiteral.Builder()
                    .setValue(getBoolean(false))
                    .build();
            break;
        default:
            operator = null;
            assert false;
        }
        Expression over = buildExpression(domain, _quant._expr);
        List<Expression> parameters = new ArrayList<>();
        List<TypeRDDLObject> ranges = new ArrayList<>();
        for (LTYPED_VAR entry : _quant._alVariables) {
            TypeRDDLObject type = (TypeRDDLObject) domain.getType(entry._sType._STypeName);
            String name = entry._sVarName._sVarName;
            Expression parameter = new ExpressionIdentifierStandard.Builder()
            		.setName(name)
            		.build();
            parameters.add(parameter);
            ranges.add(type);
        }
        return contextExpressionRDDL.newExpressionQuantifier(operator, parameters, ranges, initialValue, over);
	}

	private Expression newDiscreteDistrExpr(Domain domain, Discrete _discrete) throws EPMCException {
    	assert domain != null;
    	assert _discrete != null;
        List<Expression> operands = new ArrayList<>();
        operands.add(new ExpressionIdentifierStandard.Builder()
        		.setName(_discrete._sEnumType._STypeName)
        		.build());
        operands.add(new ExpressionLiteral.Builder()
        		.setValue(getInteger(_discrete._exprProbs.size() / 2))
        		.build());
        for (int i = 0; i < _discrete._exprProbs.size() / 2; i++) {
            Expression supp = buildExpression(domain, _discrete._exprProbs.get(i * 2));
            Expression weight = buildExpression(domain, _discrete._exprProbs.get(i * 2 + 1));
            operands.add(supp);
            operands.add(weight);
        }
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorDistributionDiscrete.IDENTIFIER))
        		.setOperands(operands)
        		.build();
	}

	private Expression newNormalDistrExpr(Domain domain, Normal _normal) throws EPMCException {
    	assert domain != null;
    	assert _normal != null;
        Expression mean = buildExpression(domain, _normal._normalMeanReal);
        Expression variance = buildExpression(domain, _normal._normalVarReal);
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorDistributionNormal.IDENTIFIER))
        		.setOperands(mean, variance)
        		.build();
	}

	private Expression newSwitchExpr(Domain domain, SWITCH_EXPR _switchExpr) throws EPMCException {
    	assert domain != null;
    	assert _switchExpr != null;
        List<Expression> parameters = new ArrayList<>();
        PVariable variable = domain.getPVariable(_switchExpr._enumVar._sName.toString());
        assert variable != null;
        TypeRDDLEnum typeEnum = (TypeRDDLEnum) variable.getType();
        String typeName = _switchExpr._enumVar.toString();
        parameters.add(newExpressionQuantifiedIdentifier(domain, _switchExpr._enumVar));
        assert typeEnum != null : typeName;
        ArrayList<CASE> _cases = _switchExpr._cases;
        for (int i = 0; i < _cases.size(); i++) {
            parameters.add(null);
            parameters.add(null);
        }
        for (CASE _case : _switchExpr._cases) {
            int number = typeEnum.toInternalNumber(_case._sEnumValue._sConstValue);
            Expression enumValue = newEnumValExpr(domain, _case._sEnumValue);
            Expression expression = buildExpression(domain, _case._expr);
            parameters.set(number * 2 + 1, enumValue);
            parameters.set(number * 2 + 2, expression);
        }
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorSwitch.IDENTIFIER))
        		.setOperands(parameters)
        		.build();
	}

	private Expression newEnumValExpr(Domain domain, ENUM_VAL _enumVal) {
    	assert domain != null;
    	assert _enumVal != null;
        ValueRDDLEnum value = (ValueRDDLEnum) domain.getType(DEFENUM).newValue();
        value.set(_enumVal._sConstValue);
        return new ExpressionLiteral.Builder()
        		.setValue(value)
        		.build();
	}

	private Expression newCompExpr(Domain domain, COMP_EXPR _compExpr) throws EPMCException {
    	assert domain != null;
    	assert _compExpr != null;
        Operator operator = buildOperator(_compExpr._comp);
        Expression operand1 = buildExpression(domain, _compExpr._e1);
        Expression operand2 = buildExpression(domain, _compExpr._e2);
        return new ExpressionOperator.Builder()
        		.setOperator(operator)
        		.setOperands(operand1, operand2)
        		.build();
	}

	private Expression newOperExpr(Domain domain, OPER_EXPR operExpr) throws EPMCException {
    	assert domain != null;
    	assert operExpr != null;
        Operator operator = buildOperator(operExpr._op);
        Expression operand1 = buildExpression(domain, operExpr._e1);
        Expression operand2 = buildExpression(domain, operExpr._e2);
        assert operand1 != null : operExpr._e1;
        assert operand2 != null : operExpr._e2;
        return new ExpressionOperator.Builder()
        		.setOperator(operator)
        		.setOperands(operand1, operand2)
        		.build();
	}

	private Expression newKronDeltaExpr(Domain domain, KronDelta _kro) throws EPMCException {
    	assert domain != null;
    	assert _kro != null;
        Expression inner = buildExpression(domain, _kro._exprIntValue);
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorDistributionKronDelta.IDENTIFIER))
        		.setOperands(inner)
        		.build();
	}

	private Expression newRealConstExpr(Domain domain, REAL_CONST_EXPR _real) throws EPMCException {
    	assert domain != null;
    	assert _real != null;
        String numberString = _real.toString();
        Value value;
        if (_real._dValue == ((int) (double) _real._dValue)) {
            value = UtilValue.newValue(TypeInteger.get(contextValue), (int) (double) _real._dValue);
        } else {
            value = UtilValue.newValue(TypeWeight.get(contextValue), numberString);
        }
        return new ExpressionLiteral.Builder()
        		.setValue(value)
        		.build();
	}

	private Expression newIntConstExpr(Domain domain, INT_CONST_EXPR _int) {
    	assert domain != null;
    	assert _int != null;
        Value value = UtilValue.newValue(TypeInteger.get(contextValue), _int._nValue.intValue());
        return new ExpressionLiteral.Builder()
        		.setValue(value)
        		.build();
	}

	private Expression newNegExpr(Domain domain, NEG_EXPR _neg) throws EPMCException {
    	assert domain != null;
    	assert _neg != null;
        Expression inner = buildExpression(domain, _neg._subnode);
        return not(inner);
	}

	private Expression newConnExpr(Domain domain, CONN_EXPR _conn) throws EPMCException {
    	assert domain != null;
    	assert _conn != null;
        List<Expression> operands = new ArrayList<>();
        for (BOOL_EXPR _operand : _conn._alSubNodes) {
            operands.add(buildExpression(domain, _operand));
        }
        Operator operator = buildOperator(_conn._sConn);
        Expression result = new ExpressionOperator.Builder()
        		.setOperator(operator)
        		.setOperands(operands.get(0), operands.get(1))
        		.build();
        for (int i = 2; i < operands.size(); i++) {
            result = new ExpressionOperator.Builder()
            		.setOperator(operator)
            		.setOperands(result, operands.get(i))
            		.build();
        }
        return result;
	}

	private Expression newBernoulli(Domain domain, Bernoulli _bernoulli) throws EPMCException {
    	assert domain != null;
    	assert _bernoulli != null;
        Expression prob = buildExpression(domain, _bernoulli._exprProb);
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorDistributionBernoulli.IDENTIFIER))
        		.setOperands(prob)
        		.build();
	}

	private Expression newIfExpr(Domain domain, IF_EXPR _ifExpr) throws EPMCException {
    	assert domain != null;
    	assert _ifExpr != null;
        Expression condition = buildExpression(domain, _ifExpr._test);
        Expression trueBranch = buildExpression(domain, _ifExpr._trueBranch);
        Expression falseBranch = buildExpression(domain, _ifExpr._falseBranch);
        return new ExpressionOperator.Builder()
        		.setOperator(contextValue.getOperator(OperatorIte.IDENTIFIER))
        		.setOperands(condition, trueBranch, falseBranch)
        		.build();
	}

	private Expression newExpressionQuantifiedIdentifier(Domain domain, PVAR_EXPR _var) throws EPMCException {
		assert domain != null;
    	assert _var != null;
        Expression name = new ExpressionIdentifierStandard.Builder()
        		.setName(_var._sName._sPVarName)
        		.build();
        List<Expression> parameters = new ArrayList<>();
        for (LTERM entry : _var._alTerms) {
        	Expression parameter = buildExpression(domain, entry);
            parameters.add(parameter);
        }
        return contextExpressionRDDL.newExpressionQuantifiedIdentifier(name, parameters);
	}

	private Operator buildOperator(String string) {
        assert string != null;
        switch (string) {
        case OP_RDDL_AND:
            return contextValue.getOperator(OperatorAnd.IDENTIFIER);
        case OP_RDDL_IFF :
            return contextValue.getOperator(OperatorIff.IDENTIFIER);
        case OP_RDDL_ADD:
            return contextValue.getOperator(OperatorAdd.IDENTIFIER);
        case OP_RDDL_SUBTRACT:
            return contextValue.getOperator(OperatorSubtract.IDENTIFIER);
        case OP_RDDL_MULTIPLY:
            return contextValue.getOperator(OperatorMultiply.IDENTIFIER);
        case OP_RDDL_DIVIDE:
            return contextValue.getOperator(OperatorDivide.IDENTIFIER);
        case OP_RDDL_IMPLIES:
            return contextValue.getOperator(OperatorImplies.IDENTIFIER);
        case OP_RDDL_OR:
            return contextValue.getOperator(OperatorOr.IDENTIFIER);
        case OP_RDDL_LT:
            return contextValue.getOperator(OperatorLt.IDENTIFIER);
        case OP_RDDL_LE:
            return contextValue.getOperator(OperatorLe.IDENTIFIER);
        case OP_RDDL_EQ:
            return contextValue.getOperator(OperatorEq.IDENTIFIER);
        case OP_RDDL_GE:
            return contextValue.getOperator(OperatorGe.IDENTIFIER);
        case OP_RDDL_GT:
            return contextValue.getOperator(OperatorGt.IDENTIFIER);
        case OP_RDDL_NE:
            return contextValue.getOperator(OperatorNe.IDENTIFIER);
        default:
            assert false : string;
        }
        // TODO Auto-generated method stub
        return null;
    }

    private Instance buildInstance(INSTANCE instance) throws EPMCException {
    	assert instance != null;
        Domain domain = domains.get(instance._sDomain);
        Instance result = new Instance();
        result.setName(instance._sName);
        Value discount = newValueWeight();
        discount.set(Double.toString(instance._dDiscount));
        result.setDiscount(discount);
        result.setHorizon(instance._nHorizon);
        result.setDomain(domain);
        result.setMaxNonDefActions(instance._nNonDefActions);
        String nonFluentsName = instance._sNonFluents;
        // note: check !nonFluentsName.equals(NULL) actually necessary
        if (nonFluentsName != null && !nonFluentsName.equals(NULL)) {
            result.setNonFluents(nonFluents.get(nonFluentsName));
        }
        for (PVAR_INST_DEF entry : instance._alInitState) {
            String name = entry._sPredName._sPVarName;
            assert domain.getStateFluent(name) != null : name;
            Type type = domain.getStateFluent(name).getType();
            Value value = buildValue(type, entry._oValue);
            StateFluent fluent = domain.getStateFluent(name);
            List<String> parameters = new ArrayList<>();
            for (LCONST e : entry._alTerms) {
                parameters.add(e._sConstValue);
            }
            result.setInitialValue(fluent, parameters, value);
        }
        for (Entry<TYPE_NAME, OBJECTS_DEF> entry : instance._hmObjects.entrySet()) {
            String objectName = entry.getKey()._STypeName;
            List<String> objectValues = new ArrayList<>();
            for (LCONST e : entry.getValue()._alObjects) {
                objectValues.add(e._sConstValue);
            }
            result.setObjectValues(objectName, objectValues);
        }

        return result;
    }

    public Map<String, Domain> getDomains() {
        return domainsExternal;
    }

    public Map<String, NonFluents> getNonFluents() {
        return nonFluentsExternal;
    }
    
    public Map<String, Instance> getInstances() {
        return instancesExternal;
    }
    
    private Value newValueWeight() {
    	return TypeWeight.get(getContextValue()).newValue();
    }
    
    private ContextValue getContextValue() {
    	return contextValue;
    }
    
    private Expression not(Expression expression) {
    	return new ExpressionOperator.Builder()
        	.setOperator(getContextValue().getOperator(OperatorNot.IDENTIFIER))
        	.setOperands(expression)
        	.build();
    }
    
    private ValueBoolean getBoolean(boolean bool) {
    	TypeBoolean typeBoolean = TypeBoolean.get(contextValue);
    	return bool ? typeBoolean.getTrue() : typeBoolean.getFalse();
    }
    
    private ValueInteger getInteger(int valueInt) {
    	TypeInteger typeInteger = TypeInteger.get(contextValue);
    	return UtilValue.newValue(typeInteger, valueInt);
    }

}
