package epmc;

import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.expression.standard.ProblemsExpression;
import epmc.operator.OperatorAdd;
import epmc.options.Options;

import static org.junit.Assert.*;

public class NiceErrorMessagesTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void constantForgottenTest() {
        Options options = prepareOptions();
        boolean thrown = false;
        try {
            computeResult(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=1 (n=50) {n<N}{max} ]");
        } catch (EPMCException e) {
            thrown = true;
            assertEquals(ProblemsExpression.EXPRESSION_COULD_NOT_EVALUATE, e.getProblem());
            assertEquals(1, e.getArguments().length);
            assertEquals("N", e.getArguments()[0]);
        }
        assertTrue(thrown);
        close(options);
    }

    @Test
    public void inconsistentOperatorExpressionTest() {
        Options options = prepareOptions();
        boolean thrown;
        
        thrown = false;
        try {
            computeResult(options, ModelNamesOwn.INCONSISTENT_OPERATOR, "true");
        } catch (EPMCException e) {
            thrown = true;
            assertEquals(ProblemsExpression.EXPRESSION_INCONSISTENT_OPERATOR, e.getProblem());
            assertEquals(2, e.getArguments().length);
            assertEquals(OperatorAdd.ADD.toString(), e.getArguments()[0].toString());
            assertEquals("[bool, int]", e.getArguments()[1]);
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            computeResult(options, ModelNamesOwn.INCONSISTENT_OPERATOR_INIT, "true");
        } catch (EPMCException e) {
            thrown = true;
            assertEquals(ProblemsExpression.EXPRESSION_INCONSISTENT_OPERATOR, e.getProblem());
            assertEquals(2, e.getArguments().length);
            assertEquals(OperatorAdd.ADD.toString(), e.getArguments()[0].toString());
            assertEquals("[bool, int]", e.getArguments()[1]);
            System.out.println(e.getPositional());
        }
        assertTrue(thrown);

        /*
        thrown = false;
        try {
            computeResult(options, ModelNamesOwn.WRONG_ASSIGNMENT_INIT, "true");
        } catch (EPMCException e) {
            thrown = true;
            System.out.println(e);
            assertEquals(ProblemsExpression.EXPRESSION_INCONSISTENT_OPERATOR, e.getProblem());
            assertEquals(1, e.getArguments().length);
            assertEquals("[bool, int]", e.getArguments()[0]);
        }
        assertTrue(thrown);
        */
        
        close(options);
    }
}
