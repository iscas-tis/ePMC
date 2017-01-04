package epmc.unambiguous.options;

public final class OptionsLTLUBA {
	public final static String OPTIONS_LTL_UBA = "OptionsLTLUBA";
    public static final String LTL_UBA_LTLFILT_CMD = "ltl-uba-ltlfilt-cmd";
    public final static String LTL_UBA_CATEGORY = "ltl-uba-category";
    public static final String LTL_UBA_LP_SOLVE_METHOD = "ltl-uba-lp-solve-method";

	private OptionsLTLUBA() {
	}
	
	public enum LPMethod {
		LP,
		ITERATION
	}
}
