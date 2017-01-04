package epmc.mpfr;

import org.junit.BeforeClass;
import org.junit.Test;

import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static org.junit.Assert.*;

import epmc.ModelNamesPRISM;
import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.TestHelper;
import epmc.mpfr.options.OptionsMPFR;
import epmc.mpfr.value.TypeMPFR;
import epmc.mpfr.value.ValueMPFR;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.ContextValue;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class MPFRTest {
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
     * @throws EPMCException thrown in case problem occurs
     */
    private final static Options prepareIMDPOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
        return options;
    }

    
    @Test
    public void loadAndBasicOperationsTest() throws EPMCException {
        Options options = prepareIMDPOptions();
        prepareOptions(options);
        ContextValue contextValue = TestHelper.getContextValue(options);
        options.set(OptionsMPFR.MPFR_ENABLE, true);
        options.set(OptionsMPFR.MPFR_PRECISION, 200);
        options.set(OptionsMPFR.MPFR_OUTPUT_FORMAT, "%Re");
        assertTrue(TypeReal.get(contextValue) instanceof TypeMPFR);
        System.out.println(TypeReal.get(contextValue).getClass());
        TypeMPFR type = new TypeMPFR(contextValue);
        Value a = type.newValue();
        a.set("10.5222223222");
        Value b = UtilValue.newValue(type, "3");
        ValueMPFR result = type.newValue();
        result.add(a, b);
        System.out.println(result);
        result.multiply(result, b);
        System.out.println(result);
    }
    
    @Test
    public void hermanOpenIntervalTest() throws EPMCException {
        Value result;
        Options options = prepareIMDPOptions();
        prepareOptions(options);
        
        double tolerance = 1E-10;

        options.set(OptionsMPFR.MPFR_ENABLE, true);
        options.set(OptionsMPFR.MPFR_PRECISION, 200);
        options.set(OptionsMPFR.MPFR_OUTPUT_FORMAT, "%Re");

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.35819912049919367", result, tolerance);
        System.out.println("RV " + result);
        
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<=6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<7 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);
    }


}
