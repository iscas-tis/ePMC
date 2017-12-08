package epmc.util.compiler;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.*;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 * @param <MemoryClassLoader>
 */
public class UtilCompiler  {
    public static Class<?> compile(final String className,
            final String source) throws ClassNotFoundException {
        final Map<String, byte[]> classBytes = compile(className + ".java", source, new PrintWriter(System.err), null, null);        
        final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        final Class<?> clazz = classLoader.loadClass(className);
        try {
            classLoader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    /**
     * compile given String source and return bytecodes as a Map.
     *
     * @param fileName source fileName to be used for error messages etc.
     * @param source Java source as String
     * @param err error writer where diagnostic messages are written
     * @param sourcePath location of additional .java source files
     * @param classPath location of additional .class files
     */
    private static Map<String, byte[]> compile(String fileName, String source,
            Writer err, String sourcePath, String classPath) {
        System.out.println("AAA");
        javax.tools.JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null) {
            throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
        }
        StandardJavaFileManager stdManager = tool.getStandardFileManager(null, null, null);

        // create a new memory JavaFileManager
        MemoryJavaFileManager fileManager = new MemoryJavaFileManager(stdManager);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<>(1);
        compUnits.add(MemoryJavaFileManager.makeStringSource(fileName, source));
        System.out.println("BBBcc");

        return compile(compUnits, fileManager, err, sourcePath, classPath);
    }

    private static Map<String, byte[]> compile(final List<JavaFileObject> compUnits, 
            final MemoryJavaFileManager fileManager,
            Writer err, String sourcePath, String classPath) {
        // to collect errors, warnings etc.
        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        // javac options
        List<String> options = new ArrayList<>();
        options.add("-Xlint:all");
        //      options.add("-g:none");
        options.add("-deprecation");
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath);
        }

        if (classPath != null) {
            options.add("-classpath");
            options.add(classPath);
        }
        javax.tools.JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null) {
            throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
        }

        // create a compilation task
        javax.tools.JavaCompiler.CompilationTask task =
                tool.getTask(err, fileManager, diagnostics,
                        options, null, compUnits);

        if (task.call() == false) {
            PrintWriter perr = new PrintWriter(err);
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                perr.println(diagnostic);
            }
            perr.flush();
            return null;
        }

        Map<String, byte[]> classBytes = fileManager.getClassBytes();
        try {
            fileManager.close();
        } catch (IOException exp) {
        }

        return classBytes;
    }
}
