package epmc.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream which ignores all outputs.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NullOutputStream extends OutputStream {
    /** instance of output stream ignoring all output */
    private final static NullOutputStream INSTANCE = new NullOutputStream();
    
    @Override
    public void write(int b) throws IOException {
    }
    
    /**
     * Obtain an instance of the null output stream.
     * Because the output is ignored anyway rather than being distributed to
     * different places, it suffices to have a single object of this class.
     * 
     * @return instance of null output stream
     */
    public static NullOutputStream getInstance() {
        return INSTANCE;
    }
}
