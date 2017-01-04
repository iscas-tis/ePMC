package epmc.param.plugin;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.value.ContextValuePARAM;
import epmc.param.value.OperatorWrapper;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;

public class BeforeModelLoadingPARAM implements BeforeModelCreation {
    public final static String PARAM_CONTEXT_VALUE_PARAM = "param-context-value-param";
    public final static String IDENTIFIER = "param-before-model-loading";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }


	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
        prepareTypes(contextValue);
        prepareParameters(contextValue);
        replaceOperators(contextValue);
	}

	private void prepareTypes(ContextValue contextValue) {
		Options options = contextValue.getOptions();
        ContextValuePARAM contextValuePARAM = options.get(PARAM_CONTEXT_VALUE_PARAM);
        if (contextValuePARAM == null) {
            contextValuePARAM = new ContextValuePARAM(contextValue);
            options.set(PARAM_CONTEXT_VALUE_PARAM, contextValuePARAM);
        }
        TypeWeight.set(contextValuePARAM.getTypeFunction());
        TypeWeightTransition.set(contextValuePARAM.getTypeFunction());
		TypeReal.set(contextValuePARAM.getTypeFunction());
//		contextValuePARAM.setOldTypeInteger(contextValue.getTypeInteger());
//		contextValue.setTypeInteger(contextValuePARAM.getTypeFunction());
	}

	private void prepareParameters(ContextValue contextValue) {
		Options options = contextValue.getOptions();
        ContextValuePARAM contextValuePARAM = options.get(PARAM_CONTEXT_VALUE_PARAM);
        
        Map<String,Object> constants = options.get(OptionsModelChecker.CONST);
        assert constants != null : contextValue;
        List<String> parameters = options.get(OptionsParam.PARAM_PARAMETER);
        assert parameters != null;
        for (String parameter : parameters) {
            Value parameterValue = contextValuePARAM.newParameterFunction(parameter);
            assert parameterValue != null;
            Expression parameterExpression = new ExpressionLiteral.Builder()
            		.setValue(parameterValue)
            		.build();
            constants.put(parameter, parameterExpression);
        }
        options.getOption(OptionsModelChecker.CONST).setDirect(constants);
	}
	
	private void replaceOperators(ContextValue contextValue) {
		Options options = contextValue.getOptions();
        ContextValuePARAM contextValuePARAM = options.get(PARAM_CONTEXT_VALUE_PARAM);
        Map<String,Operator> oldOperators = contextValue.getOperators();
        contextValuePARAM.setOriginalOperators(oldOperators);
        for (Entry<String, Operator> entry : oldOperators.entrySet()) {
        	Operator wrapper = new OperatorWrapper(contextValuePARAM, entry.getValue());
        	contextValue.addOrSetOperator(wrapper);
        }
	}
}
