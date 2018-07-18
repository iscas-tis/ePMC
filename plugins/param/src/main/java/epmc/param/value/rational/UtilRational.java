package epmc.param.value.rational;

import java.math.BigInteger;

import epmc.value.UtilValue;

public final class UtilRational {
    private final static String DIVIDED = "/";
    private final static BigInteger MONE = BigInteger.ONE.negate();

    public static void set(ValueRational result, String string) {
        assert result != null;
        assert string != null;
        if (string.trim().equals(UtilValue.POS_INF)) {
            result.set(BigInteger.ONE, BigInteger.ZERO);
        } else if (string.trim().equals(UtilValue.NEG_INF)) {
            result.set(MONE, BigInteger.ZERO);            
        } else if (string.contains(DIVIDED)) {
            setFraction(result, string);
        } else {
            setFloatingPoint(result, string);
        }
    }
    
    private static void setFraction(ValueRational result, String string) {
        assert result != null;
        assert string != null;
        String[] parts = string.split(DIVIDED);
        assert parts.length == 2;
        String numString = parts[0].trim();
        String denString = parts[1].trim();
        BigInteger num = new BigInteger(numString);
        BigInteger den = new BigInteger(denString);
        BigInteger gcd = num.gcd(den);
        num = num.divide(gcd);
        den = den.divide(gcd);
        if (num.compareTo(BigInteger.ZERO) < 0
                && den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        } else if (num.compareTo(BigInteger.ZERO) > 0
                && den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        result.set(num, den);
    }

    private static void setFloatingPoint(ValueRational result, String string) {
        assert result != null;
        assert string != null;
        string = string.trim();
        int exponentPosition = string.indexOf('e');
        if (exponentPosition == -1) {
            exponentPosition = string.indexOf('E');
        }
        String baseString = null;
        String exponentString = "0";
        if (exponentPosition == -1) {
            baseString = string;
        } else {
            baseString = string.substring(0, exponentPosition);
            exponentString = string.substring(exponentPosition + 1, string.length());
        }
        int exponent = Integer.valueOf(exponentString);
        int decimalPlaces = 0;
        int decimalPos = baseString.indexOf('.');
        if (decimalPos != -1) {
            decimalPlaces = baseString.length() - decimalPos - 1;
        }
        String integerString = baseString.replace(".", "");
        int dividePlaces = decimalPlaces - exponent;
        BigInteger num = new BigInteger(integerString);
        BigInteger den = BigInteger.ONE;
        BigInteger w = BigInteger.TEN;
        if (dividePlaces < 0) {
            w = w.pow(-dividePlaces);
            num = num.multiply(w);
        } else {
            w = w.pow(dividePlaces);
            den = den.multiply(w);
        }
        BigInteger gcd = num.gcd(den);
        num = num.divide(gcd);
        den = den.divide(gcd);
        result.set(num, den);
    }

    private UtilRational() {
    }
}
