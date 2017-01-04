package epmc.expression.standard.evaluatordd;

import java.util.List;

import epmc.dd.DD;
import epmc.error.EPMCException;

@FunctionalInterface
public interface VectorOperator {
    List<DD> apply(List<DD>... operands) throws EPMCException;
}
