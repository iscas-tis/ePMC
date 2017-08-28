/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.kretinsky;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.value.Value;

public final class KretinskyTest {
    private final static String pluginDir = System.getProperty("user.dir") + "/plugins/kretinsky/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareKretinskyOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsEPMC.PLUGIN, pluginDir);
        prepareOptions(options);
        return options;
    }


    @Test
    public void clusterUntilTest() {
        Options options = prepareKretinskyOptions();
        prepareOptions(options);
        /*
        options.set(Options.PROPERTY_SOLVERS, "generalised-rabin-explicit,generalised-rabin-dd");
        options.set(Options.ENGINE, OptionsSet.Engine.EXPLICIT);
        clusterUntilTestRun(options);
        options.set(Options.ENGINE, OptionsSet.Engine.DD);
        clusterUntilTestRun(options);
         */
        options.set(OptionsEPMC.PROPERTY_SOLVER, "generalised-rabin-incremental-explicit,generalised-rabin-incremental-dd,filter-dd,filter-explicit,propositional-dd,propositional-explicit,operator-dd,operator-explicit");
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        clusterUntilTestRun(options);
        //        options.set(Options.ENGINE, OptionsSet.Engine.DD);
        //      clusterUntilTestRun(options);
    }    

    private void clusterUntilTestRun(Options options) {
        Map<String,Object> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("N", "16");
        options.set(OptionsEPMC.CONST, constants);

        // U-1
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U (right_n!=N)) ]");
        assertEquals("0.5094707891071129", result, tolerance);

        // U-2
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U (right_n!=N))) ]");
        assertEquals("0.5096360303752221", result, tolerance);

        // U-3
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U (right_n!=N)))) ]");
        assertEquals("0.5096412753408213", result, tolerance);

        // U-4
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U (right_n!=N))))) ]");
        assertEquals("0.5096417282249177", result, tolerance);

        // U-5
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U (right_n!=N)))))) ]");
        assertEquals("0.5096417820162603", result, tolerance);

        // U-6
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U (right_n!=N))))))) ]");
        assertEquals("0.5096417883811015", result, tolerance);

        // U-7
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U (right_n!=N)))))))) ]");
        assertEquals("0.5096417890890955", result, tolerance);

        // U-8
        result = computeResult(options, CLUSTER, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U ((left_n=N-8) U (right_n!=N))))))))) ]");
        assertEquals("0.5096417891616309", result, tolerance);
    }

    @Test
    public void twoDiceTest() {
        Options options = prepareKretinskyOptions();
        options.set(OptionsEPMC.PROPERTY_SOLVER, "generalised-rabin-explicit,generalised-rabin-dd");
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        twoDiceTestRun(options);
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.DD);
        options.set(OptionsDD.DD_DEBUG, true);
        twoDiceTestRun(options);
    }

    private void twoDiceTestRun(Options options) {
        double tolerance = 1E-11;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        Value result1 = computeResult(options, TWO_DICE, "Pmin=? [ true & (F s1=7 & s2=7 & d1+d2=2) ]");
        assertEquals("1/36", result1, tolerance);
    }

    @Test
    public void firewireImplPatternTestRun() {
        Options options = prepareKretinskyOptions();
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.PROPERTY_SOLVER, "generalised-rabin-incremental-explicit,generalised-rabin-dd");
        firewireImplPatternTestRun(options);
        //        options.set(Options.ENGINE, OptionsSet.Engine.DD);
        //      firewireImplPatternTestRun(options);
    }

    private void firewireImplPatternTestRun(Options options) {
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("delay", "5");
        constants.put("fast", "0.4");
        options.set(OptionsEPMC.CONST, constants);
        result = computeResult(options, FIREWIRE_IMPL, "Pmax=? [ G(\"RQ\" => (((\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\"))) U (\"RR\")) | (G(\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\")))))) ]");
        assertEquals("0.76", result, tolerance);
    }

    @Test
    public void dining_crypt3Test() {
        Options options = prepareKretinskyOptions();
        options.set(OptionsEPMC.PROPERTY_SOLVER, "generalised-rabin-explicit,generalised-rabin-dd,filter-dd,filter-explicit,propositional-dd,propositional-explicit,operator-dd,operator-explicit");
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        dining_crypt3TestRun(options);
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.DD);
        dining_crypt3TestRun(options);
    }

    private void dining_crypt3TestRun(Options options) {
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, String.format(DINING_CRYPT, 3), "filter(forall, (pay>0) => P>=1 [ true & (F ((\"done\") & (parity!=func(mod, N, 2)))) ])");
        assertEquals(true, result);
    }
}
