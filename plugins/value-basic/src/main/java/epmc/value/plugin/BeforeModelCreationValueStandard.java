package epmc.value.plugin;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivide;
import epmc.value.OperatorDivideIgnoreZero;
import epmc.value.OperatorEq;
import epmc.value.OperatorFloor;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorId;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorLt;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeUnknown;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

public final class BeforeModelCreationValueStandard implements BeforeModelCreation {
	private final static String IDENTIFIER = "before-model-creation-value-standard";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}


	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		addOperators(contextValue);
		addTypes(contextValue);
	}

	private static void addOperators(ContextValue context) {
        assert context != null;
        context.addOrSetOperator(OperatorAdd.class);
        context.addOrSetOperator(OperatorAddInverse.class);
        context.addOrSetOperator(OperatorAnd.class);
        context.addOrSetOperator(OperatorCeil.class);
        context.addOrSetOperator(OperatorDivide.class);
        context.addOrSetOperator(OperatorDivideIgnoreZero.class);
        context.addOrSetOperator(OperatorEq.class);
        context.addOrSetOperator(OperatorFloor.class);
        context.addOrSetOperator(OperatorGe.class);
        context.addOrSetOperator(OperatorGt.class);
        context.addOrSetOperator(OperatorId.class);
        context.addOrSetOperator(OperatorIff.class);
        context.addOrSetOperator(OperatorImplies.class);
        context.addOrSetOperator(OperatorIte.class);
        context.addOrSetOperator(OperatorLe.class);
        context.addOrSetOperator(OperatorLog.class);
        context.addOrSetOperator(OperatorLt.class);
        context.addOrSetOperator(OperatorMax.class);
        context.addOrSetOperator(OperatorMin.class);
        context.addOrSetOperator(OperatorMod.class);
        context.addOrSetOperator(OperatorMultiply.class);
        context.addOrSetOperator(OperatorMultiplyInverse.class);
        context.addOrSetOperator(OperatorNe.class);
        context.addOrSetOperator(OperatorNot.class);
        context.addOrSetOperator(OperatorOr.class);
        context.addOrSetOperator(OperatorPow.class);
        context.addOrSetOperator(OperatorSubtract.class);
    }

    private static void addTypes(ContextValue context) {
    	assert context != null;
    	TypeWeight.set(new TypeDouble(context, null, null));
    	TypeWeightTransition.set(new TypeDouble(context, null, null));
    	TypeReal.set(new TypeDouble(context, null, null));
    	TypeInterval.set(new TypeInterval(context));
    	TypeBoolean.set(new TypeBoolean(context));
    	TypeUnknown.set(new TypeUnknown(context));
    	TypeInteger.set(new TypeInteger(context));
	}
}
