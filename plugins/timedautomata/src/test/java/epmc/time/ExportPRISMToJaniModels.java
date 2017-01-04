package epmc.time;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.prepare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.json.JsonValue;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.ConvertTestConfiguration;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.util.UtilJSON;

public final class ExportPRISMToJaniModels {
	private final static String JANI_EXPORT_DIR = "/Users/emhahn/exported-jani-models/";
	private final static String JANI_EXTENSION = ".jani";
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void convert() throws EPMCException {
    	export(PTA_CSMA_ABST_MODEL);
    	export(PTA_CSMA_FULL_MODEL);
    	export(PTA_FIREWIRE_ABST_MODEL);
    	export(PTA_FIREWIRE_IMPL_MODEL);
    	export(PTA_REPUDIATION_HONEST_MODEL);
        export(PTA_REPUDIATION_MALICIOUS_MODEL);
        export(PTA_SIMPLE_MODEL);
        export(PTA_ZEROCONF_MODEL);
    }
    
    private static void export(String prismFilename) throws EPMCException {
    	export(prismFilename, null);
    }
    
    private static void export(String prismFilename, String janiFilename) throws EPMCException {
    	if (janiFilename == null) {
    		janiFilename = new File(prismFilename).getName();
    		janiFilename = janiFilename.substring(0, janiFilename.lastIndexOf('.'));
    		janiFilename = JANI_EXPORT_DIR + "pta_" + janiFilename + JANI_EXTENSION;
    	}
    	System.out.println("Exporting " + prismFilename + ":");
    	System.out.println("Loading");
    	Options options = ConvertTestConfiguration.prepareJANIOptions(null);
    	options.set("prism-flatten", false);
    	ModelJANIConverter prism = (ModelJANIConverter) TestHelper.loadModel(options, prismFilename);
       	System.out.println("Converting");       
    	ModelJANI jani = prism.toJANI();
    	System.out.println("Generating JSON");
    	JsonValue json = jani.generate();
    	System.out.println("Writing");
    	try (PrintWriter out = new PrintWriter(janiFilename)) {
    	    out.println(UtilJSON.prettyString(json));
    	} catch (FileNotFoundException e) {
    		throw new RuntimeException(e);
		}
    	System.out.println("Done");
    	System.out.println();
    }
}
