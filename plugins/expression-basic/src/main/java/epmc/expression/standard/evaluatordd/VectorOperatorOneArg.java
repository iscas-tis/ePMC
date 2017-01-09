package epmc.expression.standard.evaluatordd;

import java.util.List;

import epmc.dd.DD;
import epmc.error.EPMCException;

@FunctionalInterface
public interface VectorOperatorOneArg {
    List<DD> apply(List<DD> op) throws EPMCException;
}
