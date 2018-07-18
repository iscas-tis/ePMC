package epmc.param.value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;

import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.util.JNATools;

public final class NativeTools {
    private final static class EPMCPolynomial {
        static native Pointer epmc_polynomial_new(int numParameters, int numTerms);

        static native void epmc_polynomial_delete(Pointer polynomial);

        static native int epmc_polynomial_get_num_parameters(Pointer polynomial);

        static native int epmc_polynomial_get_num_terms(Pointer polynomial);

        static native String epmc_polynomial_get_coefficient(Pointer polynomial, int termNr);

        static native int epmc_polynomial_get_exponent(Pointer polynomial, int paramNr, int termNr);

        static native void epmc_polynomial_set_coefficient(Pointer polynomial, int termNr, String coefficient);

        static native void epmc_polynomial_set_exponent(Pointer polynomial, int paramNr, int termNr, int exponent);
    }

    private final static boolean loaded = JNATools.registerLibrary(EPMCPolynomial.class, "epmc_polynomial");

    public static Pointer polynomialNew(int numParameters, int numTerms) {
        assert loaded;
        assert numParameters >= 0;
        assert numTerms >= 0;
        return EPMCPolynomial.epmc_polynomial_new(numParameters, numTerms);
    }

    public static void polynomialDelete(Pointer polynomial) {
        assert loaded;
        assert polynomial != null;
        EPMCPolynomial.epmc_polynomial_delete(polynomial);
    }

    public static int polynomialGetNumParameters(Pointer polynomial) {
        assert loaded;
        assert polynomial != null;
        return EPMCPolynomial.epmc_polynomial_get_num_parameters(polynomial);
    }

    public static int polynomialGetNumTerms(Pointer polynomial) {
        assert loaded;
        assert polynomial != null;
        return EPMCPolynomial.epmc_polynomial_get_num_terms(polynomial);
    }

    public static BigInteger polynomialGetCoefficient(Pointer polynomial, int termNr) {
        assert loaded;
        assert polynomial != null;
        assert termNr >= 0 : termNr;
        String result = EPMCPolynomial.epmc_polynomial_get_coefficient(polynomial, termNr);
        assert result != null;
        return new BigInteger(result);
    }

    public static int polynomialGetExponent(Pointer polynomial, int paramNr, int termNr) {
        assert loaded;
        assert polynomial != null;
        assert paramNr >= 0 : paramNr;
        assert termNr >= 0 : termNr;
        return EPMCPolynomial.epmc_polynomial_get_exponent(polynomial, paramNr, termNr);
    }

    public static void polynomialSetCoefficient(Pointer polynomial, int termNr, String coefficient) {
        assert loaded;
        assert polynomial != null;
        assert termNr >= 0 : termNr;
        assert coefficient != null;
        EPMCPolynomial.epmc_polynomial_set_coefficient(polynomial, termNr, coefficient);
    }

    public static void polynomialSetCoefficient(Pointer polynomial, int termNr, BigInteger coefficient) {
        assert loaded;
        assert polynomial != null;
        assert termNr >= 0 : termNr;
        assert coefficient != null;
        polynomialSetCoefficient(polynomial, termNr, coefficient.toString());
    }

    public static void polynomialSetExponent(Pointer polynomial, int paramNr, int termNr, int exponent) {
        assert loaded;
        assert polynomial != null;
        assert paramNr >= 0 : paramNr;
        assert termNr >= 0 : termNr;
        assert exponent >= 0 : exponent;
        EPMCPolynomial.epmc_polynomial_set_exponent(polynomial, paramNr, termNr, exponent);
    }

    public static Pointer polynomialToNative(ValuePolynomial polynomial) {
        assert loaded;
        assert polynomial != null;
        Pointer result = NativeTools.polynomialNew(polynomial.getNumParameters(), polynomial.getNumTerms());
        assert result != null;
        for (int termNr = 0; termNr < polynomial.getNumTerms(); termNr++) {
            NativeTools.polynomialSetCoefficient(result, termNr, polynomial.getCoefficient(termNr).toString());
            for (int paramNr = 0; paramNr < polynomial.getNumParameters(); paramNr++) {
                NativeTools.polynomialSetExponent(result, paramNr, termNr, polynomial.getExponent(paramNr, termNr));
            }
        }
        return result;
    }

    public static void nativeToPolynomialDirect(ValuePolynomial result, Pointer polynomial) {
        assert loaded;
        assert result != null;
        assert polynomial != null;
        int numParameters = NativeTools.polynomialGetNumParameters(polynomial);
        int numTerms = NativeTools.polynomialGetNumTerms(polynomial);
        result.resize(numParameters, numTerms);
        for (int termNr = 0; termNr < numTerms; termNr++) {
            BigInteger coefficient = NativeTools.polynomialGetCoefficient(polynomial, termNr);
            result.setCoefficient(termNr, coefficient);
            for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                int exponent = NativeTools.polynomialGetExponent(polynomial, paramNr, termNr);
                result.setExponent(paramNr, termNr, exponent);
            }
        }
    }

    public static void nativeToPolynomialFixOrder(ValuePolynomial result, Pointer polynomial) {
        assert loaded;
        assert result != null;
        assert polynomial != null;
        int numParameters = NativeTools.polynomialGetNumParameters(polynomial);
        int numTerms = NativeTools.polynomialGetNumTerms(polynomial);
        List<Term> terms = new ArrayList<>();
        TypePolynomial type = result.getType();
        for (int termNr = 0; termNr < numTerms; termNr++) {
            Term term = new Term(type);
            BigInteger coefficient = NativeTools.polynomialGetCoefficient(polynomial, termNr);
            term.setCoefficient(coefficient);
            for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                int exponent = NativeTools.polynomialGetExponent(polynomial, paramNr, termNr);
                term.setExponent(paramNr, exponent);
            }
            terms.add(term);
        }
        Term.toPolynomial(result, terms);
    }
    
    private NativeTools() {
    }
}
