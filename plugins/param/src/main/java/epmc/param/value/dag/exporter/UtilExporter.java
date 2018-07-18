package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class UtilExporter {
    private final static MathContext DOWN = new MathContext(34, RoundingMode.FLOOR);
    private final static MathContext UP = new MathContext(34, RoundingMode.CEILING);
    
    public static void appendStackFixToIncludes(Appendable result) throws IOException {
        // http://nadeausoftware.com/articles/2012/01/c_c_tip_how_use_compiler_predefined_macros_detect_operating_system#HowtodetectPOSIXandUNIX
        result.append("#if !defined(_WIN32) && (defined(__unix__) || defined(__unix) || (defined(__APPLE__) && defined(__MACH__)))\n");
        result.append("#include <unistd.h>\n");
        result.append("#if defined(_POSIX_VERSION)\n");
        result.append("#define FIX_STACK_SIZE\n");
        result.append("#endif\n");
        result.append("#endif\n");
        result.append("#ifdef FIX_STACK_SIZE\n");
        result.append("#include <sys/resource.h>\n");
        result.append("#endif\n");
    }
    
    public static void appendStackFixFunction(Appendable result, int stackSize) throws IOException {
        result.append("#ifdef FIX_STACK_SIZE\n");
        result.append("void fixStackSize() {\n");
        appendFormat(result, "  const rlim_t kStackSize = %d;\n", stackSize);
        result.append("  struct rlimit rl;\n");
        result.append("  int result;\n");
        result.append("\n");
        result.append("  result = getrlimit(RLIMIT_STACK, &rl);\n");
        result.append("  if (result == 0) {\n");
        result.append("    if (rl.rlim_cur < kStackSize) {\n");
        result.append("      rl.rlim_cur = kStackSize;\n");
        result.append("      result = setrlimit(RLIMIT_STACK, &rl);\n");
        result.append("      if (result != 0) {\n");
        result.append("        fprintf(stderr, \"setrlimit returned result = %d\\n\", result);\n");
        result.append("        exit(EXIT_FAILURE);\n");
        result.append("      }\n");
        result.append("    }\n");
        result.append("  }\n");
        result.append("}\n");
        result.append("#endif\n\n");
    }

    private static Appendable appendFormat(Appendable builder, String format, Object... args) throws IOException {
        return builder.append(String.format(format, args));
    }

    public static void appendGlobalCIncludes(Appendable builder, String... headers) throws IOException {
        for (String header : headers) {
            appendGlobalCInclude(builder, header);
        }
    }

    public static void appendGlobalCInclude(Appendable builder, String header) throws IOException {
        builder.append("#include <")
            .append(header)
            .append(".h>\n");
    }
    
    public static void appendStackFixCall(Appendable result) throws IOException {
        result.append("#ifdef FIX_STACK_SIZE\n");
        result.append("  fixStackSize();\n");
        result.append("#endif\n");
    }
    
    public static double fractionToDouble(BigInteger num, BigInteger den) {
        BigDecimal bigNum = new BigDecimal(num);
        BigDecimal bigDen = new BigDecimal(den);
        BigDecimal result = bigNum.divide(bigDen, MathContext.DECIMAL128);
        return result.doubleValue();
    }

    public static double fractionToDoubleDown(BigInteger num, BigInteger den) {
        BigDecimal bigNum = new BigDecimal(num);
        BigDecimal bigDen = new BigDecimal(den);
        BigDecimal divided = bigNum.divide(bigDen, DOWN);
        double dividedDouble = divided.doubleValue();
        BigDecimal dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        if (dividedDoubleBigDecimal.compareTo(divided) > 0) {
            dividedDouble = Math.nextDown(dividedDouble);
        }
        dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        assert dividedDoubleBigDecimal.compareTo(divided) <= 0;
        return dividedDouble;
    }

    public static double fractionToDoubleUp(BigInteger num, BigInteger den) {
        BigDecimal bigNum = new BigDecimal(num);
        BigDecimal bigDen = new BigDecimal(den);
        BigDecimal divided = bigNum.divide(bigDen, UP);
        double dividedDouble = divided.doubleValue();
        BigDecimal dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        if (dividedDoubleBigDecimal.compareTo(divided) < 0) {
            dividedDouble = Math.nextUp(dividedDouble);
        }
        dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        assert dividedDoubleBigDecimal.compareTo(divided) >= 0;
        return dividedDouble;
    }

    public static String fractionToHex(BigInteger num, BigInteger den) {
        return Double.toHexString(fractionToDouble(num, den));
    }

    public static String fractionToHexDown(BigInteger num, BigInteger den) {
        return Double.toHexString(fractionToDoubleDown(num, den));
    }
    
    public static String fractionToHexUp(BigInteger num, BigInteger den) {
        return Double.toHexString(fractionToDoubleUp(num, den));
    }

    private UtilExporter() {
    }
}
