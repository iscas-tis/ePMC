package epmc.util.compile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import epmc.util.compile.MemoryJavaCompiler;

import org.junit.Assert;

public class TestCompile {
    private final static MemoryJavaCompiler COMPILER = new MemoryJavaCompiler();

    @Test
    public void testCompile() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
        final String source = "public final class Solution {\n"
            + "public static int add(int a, int b) {\n"
            + "\treturn a + b;\n" + "}\n}\n";
        final Method greeting = COMPILER.compileStaticMethod("add", "Solution", source);
        final Object result = greeting.invoke(null, 423, 2345);
        Assert.assertEquals(2768, result);
    }
}
