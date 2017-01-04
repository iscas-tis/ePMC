package epmc.gmp;

import epmc.util.JNATools;

public final class GMP {
    private final static boolean LOADED =
            JNATools.registerLibrary(GMP.class, "gmp");

    public static boolean isLoaded() {
    	return LOADED;
    }
    
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private GMP() {
    }
}
