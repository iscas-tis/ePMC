package epmc.param;

import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.param.ModelNames.CLUSTER;
import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.modelchecker.Model;
import epmc.options.Options;
import epmc.param.graph.MutableGraph;

public final class MutableGraphTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

}
