package epmc.util.compile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.*;

import epmc.util.Util;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 * 
 * @param <MemoryClassLoader>
 */
public final class MemoryJavaCompiler  {
    private final static String JAVA_ENDING = ".java";
    
    private javax.tools.JavaCompiler tool;
    private StandardJavaFileManager stdManager;

    public MemoryJavaCompiler() {
        tool = ToolProvider.getSystemJavaCompiler();
        if (tool == null) {
            throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
        }
        stdManager = tool.getStandardFileManager(null, null, null);
    }

    /**
     * Compile a single static method.
     * @throws IOException 
     */
    public Method compileStaticMethod(final String methodName, final String className,
            final String source)
        throws ClassNotFoundException, IOException {
        final Map<String, byte[]> classBytes = compile(className + JAVA_ENDING, source);
        final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        final Class<?> clazz = classLoader.loadClass(className);
        final Method[] methods = clazz.getDeclaredMethods();
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                classLoader.close();
                return method;
            }
        }
        classLoader.close();
        throw new NoSuchMethodError(methodName);
    }

    public Class<?> compileClass(final String className, final String source)
        throws ClassNotFoundException, IOException {
        final Map<String, byte[]> classBytes = compile(className + JAVA_ENDING, source);
        final MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        final Class<?> clazz = classLoader.loadClass(className);
        classLoader.close();
        return clazz;
    }

    public <T> T compileInstance(String className, String source) {
        Class<?> clazz;
        try {
            clazz = compileClass(className, source);
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
        return (T) Util.getInstance(clazz);
    }
    
    public Map<String, byte[]> compile(String fileName, String source) {
        return compile(fileName, source, new PrintWriter(System.err), null, null);
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
    private Map<String, byte[]> compile(String fileName, String source,
        Writer err, String sourcePath, String classPath) {
        // to collect errors, warnings etc.
        /*
        DiagnosticCollector<JavaFileObject> diagnostics =
            new DiagnosticCollector<JavaFileObject>();
         */
        // create a new memory JavaFileManager
        MemoryJavaFileManager<StandardJavaFileManager> fileManager = new MemoryJavaFileManager<>(stdManager);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<>(1);
        compUnits.add(MemoryJavaFileManager.makeStringSource(fileName, source));

        return compile(compUnits, fileManager, err, sourcePath, classPath);
    }

    private Map<String, byte[]> compile(final List<JavaFileObject> compUnits, 
        final MemoryJavaFileManager<StandardJavaFileManager> fileManager,
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
