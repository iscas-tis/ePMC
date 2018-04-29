package epmc.imdp.lump.cooperative;

import static epmc.modelchecker.TestHelper.*;
import static epmc.value.UtilValue.newValue;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.imdp.lump.cooperative.ProblemSet;
import epmc.imdp.lump.cooperative.ProblemSetNormaliser;
import epmc.imdp.model.ModelIMDP;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;

public final class ProblemSetTest {
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
        processAfterModelLoading(options);

        TypeWeightTransition.set(TypeInterval.get());
        Type typeWeight = TypeWeightTransition.get();
        ProblemSetNormaliser normaliser = new ProblemSetNormaliser();
        ProblemSet problemSet = new ProblemSet();
        problemSet.setDimensions(2, 3);
        problemSet.setChallenger(0, newValue(typeWeight, "[1/3,3/4]"));
        problemSet.setChallenger(1, newValue(typeWeight, "[0,1]"));
        problemSet.setDefender(0, 0, newValue(typeWeight, "[0,1]"));
        problemSet.setDefender(0, 1, newValue(typeWeight, "[1/4,1/4]"));
        problemSet.setDefender(1, 0, newValue(typeWeight, "[1/13,8/9]"));
        problemSet.setDefender(1, 1, newValue(typeWeight, "[1/13,8/9]"));
        problemSet.setDefender(2, 0, newValue(typeWeight, "[0,1]"));
        problemSet.setDefender(2, 1, newValue(typeWeight, "[0,1]"));


        System.out.println(problemSet);
        normaliser.normalise(problemSet);
        System.out.println(problemSet);

        ProblemSet problemSet2 = problemSet.clone();
        problemSet2.setChallenger(1, UtilValue.newValue(typeWeight, "[1/5,3/4]"));
        System.out.println(problemSet2);

        System.out.println(problemSet.compareTo(problemSet2));

        /*
        // ([0,1/4],[1/4,1/2],[1/2,2/3]) 
        ExtremePointsEnumerator enumerator = new ExtremePointsEnumerator(contextValue);
        Value intervals = contextValue.getTypeInterval().getTypeArray().newValue(3);
        intervals.set("[0,1/4]", 0);
        intervals.set("[1/4,1/2]", 1);
        intervals.set("[1/2,2/3]", 2);
        Set<Value> collectedValues = new LinkedHashSet<>();
        enumerator.enumerate(v -> {collectedValues.add(v.clone()); return false;}, intervals, intervals.size());
        assertEquals(4, collectedValues.size());
        Value distribution = contextValue.getTypeReal().getTypeArray().newValue(3);
        distribution.set("0", 0);
        distribution.set("1/2", 1);
        distribution.set("1/2", 2);
        assertTrue(approxContains(collectedValues, distribution));
        distribution.set("1/4", 0);
        distribution.set("1/4", 1);
        distribution.set("1/2", 2);
        assertTrue(approxContains(collectedValues, distribution));
        distribution.set("0", 0);
        distribution.set("1/3", 1);
        distribution.set("2/3", 2);
        assertTrue(approxContains(collectedValues, distribution));
        distribution.set("1/12", 0);
        distribution.set("1/4", 1);
        distribution.set("2/3", 2);
        assertTrue(approxContains(collectedValues, distribution));
         */
    }

    /*
    @Test
    public void pointTest() {
        Options options = prepareIMDPOptions();
        ExtremePointsEnumerator enumerator = new ExtremePointsEnumerator(contextValue);
        Value intervals = contextValue.getTypeInterval().getTypeArray().newValue(3);
        intervals.set("[1/2,1/2]", 0);
        intervals.set("[3/8,3/8]", 1);
        intervals.set("[1/8,1/8]", 2);
        Set<Value> collectedValues = new LinkedHashSet<>();
        enumerator.enumerate(v -> {assertEquals(0, collectedValues.size()); collectedValues.add(v.clone()); return false;}, intervals, intervals.size());
        assertEquals(1, collectedValues.size());
        Value distribution = contextValue.getTypeReal().getTypeArray().newValue(3);
        distribution.set("1/2", 0);
        distribution.set("3/8", 1);
        distribution.set("1/8", 2);
        assertTrue(approxContains(collectedValues, distribution));
    }
     */

    /*
    private boolean approxContains(Set<Value> set, Value value) {
        for (Value ele : set) {
        	if (ele.isEq(value)) {
        		return true;
        	}
        }
        return false;
    }
     */
}
