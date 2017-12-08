package test.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import epmc.util.Util;
import epmc.util.compiler.UtilCompiler;

public class TestLoader {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println("FFF");
        final String source = "public final class Solution {\n"
                + "public String greeting(String name) {\n"
                + "test.compiler.TestLoader.print();\n"
                + "\treturn \"Hello \" + name;\n" + "}\n}\n";
        Class<?> clazz = UtilCompiler.compile("Solution", source);
        try {
            Object object = Util.getInstance(clazz);
            Method method = clazz.getMethod("greeting", String.class);
            String asdf = (String) method.invoke(object, "Moritz");
            System.out.println(asdf);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method called by generated code.
     */
    public final static void print() {
        System.out.println("HIHO");
    }
}
