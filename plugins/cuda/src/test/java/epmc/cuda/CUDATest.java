package epmc.cuda;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.cuda.graphsolver.GraphSolverCUDA;
import epmc.error.EPMCException;
import epmc.options.OptionsEPMC;
import epmc.options.Options;
import epmc.options.OptionsTypesEPMC;
import epmc.options.UtilOptionsEPMC;
import epmc.value.Value;
import static epmc.modelchecker.TestHelper.*;
import static epmc.ModelNames.*;

public final class CUDATest {
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";
    private final static String CSMA_FN = "csma_%01d_%01d";
//    private final static int[] CSMA_N = {2,3};
//    private final static int[] CSMA_K = {2,4,6};
    private final static int[] CSMA_N = {3};
    private final static int[] CSMA_K = {4,6,8};
    private final static String DINING_CRYPT_FN = "dining_crypt_%02d";
//    private final static int[] NUMBERS_CRYPT = new int[]{3,4,5,6,7,8,9,10,15};
    private final static int[] NUMBERS_CRYPT = new int[]{3,4,5,6,7};
    private final static String LEADER_ASYNC_FN = "leader_async_%01d";
    private final static int[] NUMBERS_LEADER_ASYNC = new int[]{3,4,5,6,7,8,9};
//    private final static int[] STEP_BOUNDS_LEADER_ASYNC = new int[]{40,80,120,160};
    private final static int[] STEP_BOUNDS_LEADER_ASYNC = new int[]{160};
    private final static String PHIL_NOFAIR_FN = "phil-nofair%02d";
    private final static String PHIL_LSS_FN = "phil_lss%d";
//    private final static int[] K_PHIL_LSS = new int[]{4,5,6,7,8,9,10};
    private final static int[] K_PHIL_LSS = new int[]{4,5};
    private final static int[] NUMBERS_PHIL_LSS = new int[]{3,4};
    private final static int[] STEP_BOUNDS_PHIL_LSS = new int[]{160};
//  private final static int[] STEP_BOUNDS_PHIL_LSS = new int[]{40,80,120,160};
//    private final static int[] STEP_BOUNDS_PHIL_NOFAIR = new int[]{40,80,120,160};
    private final static int[] STEP_BOUNDS_PHIL_NOFAIR = new int[]{160};
//    private final static int[] NUMBERS_PHIL_NOFAIR = new int[]{3,4,5,6,7,8,9,10};
    //     private final static int[] NUMBERS_PHIL_NOFAIR = new int[]{3,4,5,6};
    private final static int[] NUMBERS_PHIL_NOFAIR = new int[]{7};
    private final static String RABIN_FN = "rabin_%02d";
//    private final static int[] NUMBERS_RABIN = new int[]{3,4,5,6,7,8,9,10};
//    private final static int[] K_RABIN = new int[]{0,1,2,3,4,5,6,7,8};
    private final static int[] NUMBERS_RABIN = new int[]{6,7,8,9,10};
    private final static int[] K_RABIN = new int[]{8};
//    private final static int[] COIN_N = {2,4,6,8,10};
//    private final static int[] COIN_K = {2,4,8,16};
    private final static int[] COIN_N = {8};
    private final static int[] COIN_K = {2};
    private final static String COIN_STRING = "coin_%02d_%02d";
    private final static String FIREWIRE_PROP1_FN = "firewireImpl_%03d_prop1";
    private final static String FIREWIRE_PROP2_FN = "firewireImpl_%03d_prop2";
    private final static int[] FIREWIRE_DELAYS = {10,50,100,200};

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareCUDAOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options);
        options.set(OptionsEPMC.PLUGIN, PLUGIN_DIR);
        Collection<String> solvers = options.get(OptionsEPMC.GRAPH_SOLVER);
        solvers.clear();
        solvers.add(GraphSolverCUDA.IDENTIFIER);
        return options;
    }
    
    @Test
    public void twoDiceTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-11;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        computeResultCUDA(options, TWO_DICE, "Pmin=? [ F s1=7 & s2=7 & d1+d2=2 ]", "twoDice");
    }
    
    @Ignore
    @Test
    public void firewireImplPatternTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        Map<String,String> constants = new HashMap<>();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int delay : FIREWIRE_DELAYS) {
            constants.put("delay", String.valueOf(delay));
        	constants.put("fast", "0.4");
        	options.set(OptionsEPMC.CONST, constants);
//        	computeResultCUDA(options, FIREWIRE_IMPL, "Pmax=? [ G(\"RQ\" => (((\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\"))) U (\"RR\")) | (G(\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\")))))) ]",
  //      			String.format(FIREWIRE_PROP1_FN, delay));
        	computeResultCUDA(options, FIREWIRE_IMPL, "Pmin=? [ F ( (s1=8) & (s2=7) ) | ( (s1=7) & (s2=8) ) ]",
        			String.format(FIREWIRE_PROP2_FN, delay));
        }
    }

    @Ignore
    @Test
    public void dining_cryptTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int num_crypt : NUMBERS_CRYPT) {
            computeResultCUDA(options,
            		String.format(DINING_CRYPT, num_crypt),
            		"Pmin=? [ F \"done\" & outcome = 0 ]",
            		String.format(DINING_CRYPT_FN, num_crypt) + "_prop1");
            computeResultCUDA(options,
            		String.format(DINING_CRYPT, num_crypt),
            		"Pmax=? [ F \"done\" & outcome = 0 ]",
            		String.format(DINING_CRYPT_FN, num_crypt) + "_prop2");
        }
    }
    
    @Ignore
    @Test
    public void coinTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsEPMC.CONST, constants);
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        
        for (int n : COIN_N) {
        	for (int k : COIN_K) {
                constants.put("K", String.valueOf(k));
                computeResultCUDA(options, String.format(COIN, n), "Pmax=? [ F \"finished\"&!\"agree\" ]",
                		String.format(COIN_STRING, n, k));        		
        	}
        }
    }

    @Test
    @Ignore
    public void csmaTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int N : CSMA_N) {
        	for (int K : CSMA_K) {
        		computeResultCUDA(options, String.format(CSMA, N, K),
        				"Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\"]",
        				String.format(CSMA_FN, N, K) + "prop1");
        		/*
        		computeResultCUDA(options, String.format(CSMA, N, K),
        				"Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\"]",
        				String.format(CSMA_FN, N, K) + "prop2");
        		for (int k = 1; k <= 3; k++) {
            		computeResultCUDA(options, String.format(CSMA, N, K),
            				String.format("Pmin=? [ F min_backoff_after_success<=%d ]", k),
            				String.format(CSMA_FN, N, K) + "prop3_" + k);
            		computeResultCUDA(options, String.format(CSMA, N, K),
            				String.format("Pmin=? [ F min_backoff_after_success<=%d ]", k),
            				String.format(CSMA_FN, N, K) + "prop4_" + k);
        		}
        		*/
        	}
        }
    }
    
    @Ignore
    @Test
    public void leaderAsyncTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int numProcesses : NUMBERS_LEADER_ASYNC) {
        	StringBuilder numProcStr = new StringBuilder();
        	for (int procNr = 1; procNr <= numProcesses; procNr++) {
        		if (procNr != 1) {
        			numProcStr.append("| ");
        		}
        		numProcStr.append("s" + procNr + "=4 ");
        	}
        	for (int numSteps : STEP_BOUNDS_LEADER_ASYNC) {
        		computeResultCUDA(options, String.format(LEADER_ASYNC, numProcesses),
        				String.format("Pmin=?[ true U<=%d (" + numProcStr + ") ]", numSteps),
        				String.format(LEADER_ASYNC_FN, numProcesses) + "prop1_" + numSteps);
        		computeResultCUDA(options, String.format(LEADER_ASYNC, numProcesses),
        				String.format("Pmax=?[ true U<=%d (" + numProcStr + ") ]", numSteps),
        				String.format(LEADER_ASYNC_FN, numProcesses) + "prop2_" + numSteps);
        	}
        }
    }

    @Ignore
    @Test
    public void philNofairTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int numProcesses : NUMBERS_PHIL_NOFAIR) {
        	for (int numSteps : STEP_BOUNDS_PHIL_NOFAIR) {
        		computeResultCUDA(options, String.format(PHIL_NOFAIR, numProcesses),
        				String.format("Pmin=?[true U<=%d \"eat\" {\"hungry\"}{min}]", numSteps),
        				String.format(PHIL_NOFAIR_FN, numProcesses) + "prop1_" + numSteps);
        	}
        }
    }

    @Ignore
    @Test
    public void philLssTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int numProcesses : NUMBERS_PHIL_LSS) {
        	for (int K : K_PHIL_LSS) {
                Map<String,String> constants = new HashMap<>();
                constants.put("K", Integer.toString(K));
                options.set(OptionsEPMC.CONST, constants);
        		for (int numSteps : STEP_BOUNDS_PHIL_LSS) {
        			computeResultCUDA(options, String.format(PHIL_LSS, numProcesses),
        					String.format("Pmin=? [ true U<=%d \"entered\" {\"trying\"}{min} ]", numSteps),
        					String.format(PHIL_LSS_FN, numProcesses) + "_" + K + "_prop1_" + numSteps);
        		}
        	}
        }
    }

    @Test
    public void rabinTest() throws EPMCException {
        Options options = prepareCUDAOptions();
        double tolerance = 1E-10;
        options.set(OptionsEPMC.ENGINE, OptionsTypesEPMC.Engine.EXPLICIT);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        for (int numProcesses : NUMBERS_RABIN) {
        	for (int k : K_RABIN) {
                Map<String,String> constants = new HashMap<>();
                options.set(OptionsEPMC.CONST, constants);
                computeResultCUDA(options, String.format(RABIN, numProcesses),
                		String.format("Pmin=?[ !\"one_critical\" U (p1=2) {draw1=1 & !\"one_critical\" & maxb<=%d}{min} ]", k),
                		String.format(RABIN_FN, numProcesses) + "_" + k + "_prop1");
        	}
        }
    }

    private static Value computeResultCUDA(Options options, String modelFile,
            String property, String cudaFilename) {
    	System.out.println("CFN: " + cudaFilename);
    	cudaFilename = System.getProperty("user.home") + "/cuda-models/" + cudaFilename;
        options.set("cuda-filename", cudaFilename);
    	Value result = computeResult(options, modelFile, property);
    	try (PrintWriter out = new PrintWriter(cudaFilename + "/property.txt")) {
    		out.println(property);
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
			assert false;
		}
    	return result;
    }
}
