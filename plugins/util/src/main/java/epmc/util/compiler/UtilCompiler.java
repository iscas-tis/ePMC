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
 */
public class UtilCompiler  {
    private final static String JAVA_EXTENSION = ".java";
    private final static String UNCHECKED = "unchecked";
    private final static String JDK_UNAVAILABLE
    = "Could not get Java compiler. Please, ensure that JDK is used instead of JRE.";
    private final static String XLINT_ALL = "-Xlint:all";
    private final static String DEPRECATION = "-deprecation";
    private final static String SOURCEPATH = "-sourcepath";
    private final static String CLASSPATH = "-classpath";
//    private final static String G_NONE = "-g:none";
    
    public static <T> Class<T> compile(final String className,
            final String source) throws ClassNotFoundException {
        final Map<String, byte[]> classBytes = compile(className + JAVA_EXTENSION, source, new PrintWriter(System.err), null, null);        
        final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        @SuppressWarnings(UNCHECKED)
        final Class<T> clazz = (Class<T>) classLoader.loadClass(className);
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
        javax.tools.JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null) {
            throw new RuntimeException(JDK_UNAVAILABLE);
        }
        StandardJavaFileManager stdManager = tool.getStandardFileManager(null, null, null);

        // create a new memory JavaFileManager
        MemoryJavaFileManager fileManager = new MemoryJavaFileManager(stdManager);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<>(1);
        compUnits.add(MemoryJavaFileManager.makeStringSource(fileName, source));

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
        options.add(XLINT_ALL);
        //      options.add(G_NONE);
        options.add(DEPRECATION);
        if (sourcePath != null) {
            options.add(SOURCEPATH);
            options.add(sourcePath);
        }

        if (classPath != null) {
            options.add(CLASSPATH);
            options.add(classPath);
        }
        javax.tools.JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null) {
            throw new RuntimeException(JDK_UNAVAILABLE);
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
