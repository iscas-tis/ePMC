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

package epmc.jani;

import static epmc.ModelNamesPRISM.BEAUQUIER_MODEL;
import static epmc.ModelNamesPRISM.BRP_MODEL;
import static epmc.ModelNamesPRISM.CELL_MODEL;
import static epmc.ModelNamesPRISM.CLUSTER_MODEL;
import static epmc.ModelNamesPRISM.COIN_MODEL;
import static epmc.ModelNamesPRISM.CSMA_MODEL;
import static epmc.ModelNamesPRISM.DICE_MODEL;
import static epmc.ModelNamesPRISM.DINING_CRYPT_MODEL;
import static epmc.ModelNamesPRISM.FMS_MODEL;
import static epmc.ModelNamesPRISM.HERMAN_MODEL;
import static epmc.ModelNamesPRISM.IJ_MODEL;
import static epmc.ModelNamesPRISM.KANBAN_MODEL;
import static epmc.ModelNamesPRISM.KNACL_MODEL;
import static epmc.ModelNamesPRISM.LEADER_ASYNC_MODEL;
import static epmc.ModelNamesPRISM.LEADER_SYNC_MODEL;
import static epmc.ModelNamesPRISM.MC_MODEL;
import static epmc.ModelNamesPRISM.MUTUAL_MODEL;
import static epmc.ModelNamesPRISM.NACL_MODEL;
import static epmc.ModelNamesPRISM.PEER2PEER_MODEL;
import static epmc.ModelNamesPRISM.PHIL_LSS_MODEL;
import static epmc.ModelNamesPRISM.PHIL_MODEL;
import static epmc.ModelNamesPRISM.PHIL_NOFAIR_MODEL;
import static epmc.ModelNamesPRISM.POLLING_MODEL;
import static epmc.ModelNamesPRISM.RABIN_MODEL;
import static epmc.ModelNamesPRISM.TANDEM_MODEL;
import static epmc.ModelNamesPRISM.TWO_DICE_MODEL;
import static epmc.ModelNamesPRISM.WLAN_COLLIDE_MODEL;
import static epmc.ModelNamesPRISM.WLAN_MODEL;
import static epmc.ModelNamesPRISM.WLAN_TIME_BOUNDED_MODEL;
import static epmc.ModelNamesPRISM.ZEROCONF_MODEL;
import static epmc.ModelNamesPRISM.ZEROCONF_TIME_BOUNDED_MODEL;
import static epmc.jani.ModelNames.JANI_EXPORT_DIR;
import static epmc.jani.ModelNames.JANI_EXTENSION;
import static epmc.jani.ModelNames.getJANIFilenameFromPRISMFilename;
import static epmc.modelchecker.TestHelper.prepare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.json.JsonValue;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.ModelJANI;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.util.UtilJSON;

public final class ExportJANIToJANIModels {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void convertTest() {
        export(System.getProperty("user.home") + "/exporter.jani", System.getProperty("user.home") + "/exporter.jani-exported");
    }

    @Test
    public void convertSingle() {
        export(String.format(PEER2PEER_MODEL, 4, 4));
    }

    @Test
    public void convertPRISMIncluded() {
        export(BRP_MODEL);
        export(CELL_MODEL);
        export(CLUSTER_MODEL);
        for (int i : new int[]{2,4,6,8,10}) {
            export(String.format(COIN_MODEL, i));
        }
        for (int i : new int[]{2,3,4}) {
            for (int j : new int[]{2,4,6}) {
                export(String.format(CSMA_MODEL, i, j));
            }
        }
        export(DICE_MODEL);
        export(TWO_DICE_MODEL);
        for (int i : new int[]{3,4,5,6,7,8,9,10,15}) {
            export(String.format(DINING_CRYPT_MODEL, i));
        }
        //    	export(EMBEDDED_MODEL);
        export(JANI_EXPORT_DIR + "firewire_abs" + JANI_EXTENSION, JANI_EXPORT_DIR + "firewire_abs" + JANI_EXTENSION + "-exported");
        export(JANI_EXPORT_DIR + "firewire_impl" + JANI_EXTENSION, JANI_EXPORT_DIR + "firewire_impl" + JANI_EXTENSION + "-exported");
        export(FMS_MODEL);
        export(KANBAN_MODEL);
        //Before enabling this test, fix the LEADER_ASYNC_PROPERTY file since it contains
        //the wrong property   fiter(forall, leaders<=1)  instead of the correct one
        //  filter(forall, leaders<=1)  
        for (int i : new int[]{3,4,5,6,7,8,9,10}) {
            export(String.format(JANI_EXPORT_DIR + "leader_async_%d" + JANI_EXTENSION, i), String.format(LEADER_ASYNC_MODEL, i));
        }
        for (int i : new int[]{3,4,5,6}) {
            for (int j : new int[]{2,3,4,5,6,8}) {
                export(String.format(JANI_EXPORT_DIR + "leader_sync_%d_%d" + JANI_EXTENSION, i, j), String.format(LEADER_SYNC_MODEL, i, j));
            }
        }
        export(KNACL_MODEL);
        export(NACL_MODEL);
        export(MC_MODEL);
        for (int i : new int[]{3,4,5,8,10}) {
            export(String.format(MUTUAL_MODEL, i));
        }
        for (int i : new int[]{4,5}) {
            for (int j : new int[]{4,5,6,7,8}) {
                export(String.format(PEER2PEER_MODEL, i, j));
            }
        }
        for (int i : new int[]{3,4,5,6,7,8,9,10,15,20,25,30}) {
            export(String.format(PHIL_MODEL, i));
        }
        for (int i : new int[]{3,4,5,6,7,8,9,10}) {
            export(String.format(PHIL_NOFAIR_MODEL, i));
        }
        for (int i : new int[]{3,4}) {
            export(String.format(PHIL_LSS_MODEL, i));
        }
        for (int i : new int[]{2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}) {
            export(String.format(POLLING_MODEL, i));
        }
        for (int i : new int[]{3,4,5,6,7,8,9,10}) {
            export(String.format(RABIN_MODEL, i));
        }
        for (int i : new int[]{3,5,7,9,11}) {
            export(String.format(BEAUQUIER_MODEL, i));
        }
        for (int i : new int[]{3,5,7,9,11,13,15,17,19,21}) {
            export(String.format(HERMAN_MODEL, i));
        }
        for (int i : new int[]{3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21}) {
            export(String.format(IJ_MODEL, i));
        }
        export(TANDEM_MODEL);
        for (int i : new int[]{0,1,2,3,4,5,6}) {
            export(String.format(WLAN_MODEL, i));
        }
        for (int i : new int[]{0,1,2,3,4,5,6}) {
            export(String.format(WLAN_COLLIDE_MODEL, i));
        }
        for (int i : new int[]{0,1,2,3,4,5,6}) {
            export(String.format(WLAN_TIME_BOUNDED_MODEL, i));
        }
        export(ZEROCONF_MODEL);
        export(ZEROCONF_TIME_BOUNDED_MODEL);
    }

    private static void export(String prismFilename) {
        String modelName = new File(prismFilename).getName();
        modelName = modelName.substring(0, modelName.lastIndexOf('.'));
        String janiFilename = getJANIFilenameFromPRISMFilename(prismFilename);
        export(janiFilename, janiFilename + "-exported");
    }

    private static void export(String janiFilename, String exportedJaniFilename) {
        System.out.println("Exporting " + janiFilename + ":");
        System.out.println("Loading");
        Options options = ConvertTestConfiguration.prepareJANIOptions(null);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        ModelJANI jani = (ModelJANI) TestHelper.loadModelMulti(options, janiFilename);
        
        System.out.println("Generating JSON");
        JsonValue json = null;
        if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_NEW_EXPORTER)) {
            JANIExporter_ProcessorRegistrar.setModel(jani);
            json = JANIExporter_ProcessorRegistrar.getProcessor(jani)
                    .toJSON();
        } else {
            json = jani.generate();
        }

        System.out.println("Writing " + exportedJaniFilename);
        try (PrintWriter out = new PrintWriter(exportedJaniFilename)) {
            out.println(UtilJSON.prettyString(json));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Done");
        System.out.println();
    }
}
