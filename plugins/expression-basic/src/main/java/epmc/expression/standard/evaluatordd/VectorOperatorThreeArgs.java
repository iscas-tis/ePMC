package epmc.expression.standard.evaluatordd;

import java.util.List;

import epmc.dd.DD;
import epmc.error.EPMCException;

@FunctionalInterface
public interface VectorOperatorThreeArgs {
    List<DD> apply(List<DD> op1, List<DD> op2, List<DD> op3) throws EPMCException;
}
