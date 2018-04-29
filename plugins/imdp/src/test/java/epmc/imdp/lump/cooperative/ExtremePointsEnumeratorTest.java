package epmc.imdp.lump.cooperative;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static org.junit.Assert.*;

import epmc.imdp.lump.cooperative.ExtremePointsEnumerator;
import epmc.imdp.model.ModelIMDP;
import epmc.main.options.UtilOptionsEPMC;
import epmc.operator.OperatorEq;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueSetString;

public final class ExtremePointsEnumeratorTest {
    /** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     */
    private final static Options prepareIMDPOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelIMDP.IDENTIFIER);
        return options;
    }

    @Test
    public void klinkTest() {
        Options options = prepareIMDPOptions();
        prepareOptions(options, ModelIMDP.IDENTIFIER);
        // ([0,1/4],[1/4,1/2],[1/2,2/3]) 
        ExtremePointsEnumerator enumerator = new ExtremePointsEnumerator();
        ValueArrayAlgebra intervals = UtilValue.newArray(TypeInterval.get().getTypeArray(), 3);
        set(intervals, "[0,1/4]", 0);
        set(intervals, "[1/4,1/2]", 1);
        set(intervals, "[1/2,2/3]", 2);
        Set<ValueArrayAlgebra> collectedValues = new LinkedHashSet<>();
        enumerator.enumerate(v -> {collectedValues.add(UtilValue.clone(v)); return false;}, intervals, intervals.size());
        assertEquals(4, collectedValues.size());
        ValueArrayAlgebra distribution = UtilValue.newArray(TypeReal.get().getTypeArray(), 3);
        set(distribution, "0", 0);
        set(distribution, "1/2", 1);
        set(distribution, "1/2", 2);
        assertTrue(approxContains(collectedValues, distribution));
        set(distribution, "1/4", 0);
        set(distribution, "1/4", 1);
        set(distribution, "1/2", 2);
        assertTrue(approxContains(collectedValues, distribution));
        set(distribution, "0", 0);
        set(distribution, "1/3", 1);
        set(distribution, "2/3", 2);
        assertTrue(approxContains(collectedValues, distribution));
        set(distribution, "1/12", 0);
        set(distribution, "1/4", 1);
        set(distribution, "2/3", 2);
        assertTrue(approxContains(collectedValues, distribution));
    }

    @Test
    public void pointTest() {
        Options options = prepareIMDPOptions();
        prepareOptions(options, ModelIMDP.IDENTIFIER);
        ExtremePointsEnumerator enumerator = new ExtremePointsEnumerator();
        ValueArrayAlgebra intervals = UtilValue.newArray(TypeInterval.get().getTypeArray(), 3);
        set(intervals, "[1/2,1/2]", 0);
        set(intervals, "[3/8,3/8]", 1);
        set(intervals, "[1/8,1/8]", 2);
        Set<ValueArrayAlgebra> collectedValues = new LinkedHashSet<>();
        enumerator.enumerate(v -> {assertEquals(0, collectedValues.size()); collectedValues.add(UtilValue.clone(v)); return false;}, intervals, intervals.size());
        assertEquals(1, collectedValues.size());
        ValueArrayAlgebra distribution = UtilValue.newArray(TypeReal.get().getTypeArray(), 3);
        set(distribution, "1/2", 0);
        set(distribution, "3/8", 1);
        set(distribution, "1/8", 2);
        assertTrue(approxContains(collectedValues, distribution));
    }

    private boolean approxContains(Set<ValueArrayAlgebra> set, ValueArrayAlgebra value) {
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (Value ele : set) {
            OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, ele.getType(), value.getType());
            eq.apply(cmp, ele, value);
            if (cmp.getBoolean()) {
                return true;
            }
        }
        return false;
    }

    private static void set(ValueArray valueArray, String entry, int pos) {
        Value valueEntry = valueArray.getType().getEntryType().newValue();
        ValueSetString.as(valueEntry).set(entry);
        valueArray.set(valueEntry, pos);
    }

}
