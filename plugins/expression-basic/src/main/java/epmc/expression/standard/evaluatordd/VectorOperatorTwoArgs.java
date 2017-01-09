package epmc.expression.standard.evaluatordd;

import java.util.List;

import epmc.dd.DD;
import epmc.error.EPMCException;

@FunctionalInterface
public interface VectorOperatorTwoArgs {
    List<DD> apply(List<DD> op1, List<DD> op2) throws EPMCException;
}
