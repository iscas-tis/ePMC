package epmc.error;

import java.util.Arrays;

// TODO remove unused methods and fields once sure that they are no longer needed
// TODO complete documentation

/**
 * Exception class to be used by EPMC and its plugins.
 * This class should in general be used in methods which need to throw
 * non-runtime exceptions, which usually will finally be reported to the user of
 * EPMC. This class uses the class {@link Problem} to support localised error
 * messages. It is however also possible to obtain the exact cause of an
 * exception programmatically and e.g. replace an exception by another one.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EPMCException extends Exception {
    /**
     * Builder for {@link EPMCException}.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        private boolean built;
        private Problem problem;
        private String message;
        private Throwable cause;
        private Positional positional;
        private Object[] arguments;

        public Builder setProblem(Problem problem) {
            assert !built;
            this.problem = problem;
            return this;
        }
        
        private Problem getProblem() {
            return problem;
        }

        public Builder setMessage(String message) {
            assert !built;
            this.message = message;
            return this;
        }
        
        private String getMessage() {
            return message;
        }
        
        public Builder setCause(Throwable cause) {
            assert !built;
            this.cause = cause;
            return this;
        }
        
        private Throwable getCause() {
            return cause;
        }
        
        public Builder setPositional(Positional positional) {
            assert !built;
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public Builder setArguments(Object... arguments) {
            assert !built;
            this.arguments = arguments;
            return this;
        }
        
        private Object[] getArguments() {
            return arguments;
        }

        public EPMCException build() {
            assert !built;
            built = true;
            return new EPMCException(this);
        }
    }
    
    /** Serial version ID - 1L as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** String containing opening brace. */
    private final static String BRACE_OPEN = "(";
    /** String containing closing brace. */
    private final static String BRACE_CLOSE = ")";

    /** Problem causing the exception. */
    private final Problem problem;
    /** Arguments further specifying why the exception occurred. */
    private final String[] arguments;
    /** Position in the input data cause the exception, or {@code null}. */
    private final Positional positional;

    /**
     * Create new exception from given builder.
     * The builder parameter must not be {@code null}.
     * 
     * @param builder builer to build exception from
     */
    private EPMCException(Builder builder) {
        super(buildMessage(builder), builder.getCause());
        assert builder != null;
        Problem problem = builder.getProblem();
        String message = builder.getMessage();
        Object[] arguments = builder.getArguments();
        assert problem != null;
        if (message == null) {
            message = buildMessage(problem, builder.getPositional(), arguments);
        }
        if (arguments == null) {
            arguments = new Object[0];
        }
        for (Object argument : arguments) {
            assert argument != null : problem + SPACE + Arrays.toString(arguments);
        }
        this.problem = problem;
        this.arguments = new String[arguments.length];
        for (int argNr = 0; argNr < arguments.length; argNr++) {
            this.arguments[argNr] = arguments[argNr].toString();
        }
        this.positional = builder.getPositional();
    }

    /**
     * Obtain the identifier of the problem causing the exception.
     * 
     * @return identifier of the problem causing the exception
     */
    public String getProblemIdentifier() {
        return problem.getIdentifier();
    }

    /**
     * Obtain problem description of this exception
     * 
     * @return problem description of this exception
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Obtain parameters of problem description.
     * Parameters include information such as the name of a problematic
     * variable. It should not contain file names or position information,
     * because these shall be stored using {@link Positional} objects.
     * 
     * @return parameters of this exception
     */
    public Object[] getArguments() {
        Object[] result = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            result[i] = arguments[i];
        }
        return result;
    }
    
    /**
     * Construct message to provide to parent class of {@link EPMCException}.
     * The message will then be obtained using
     * {@link EPMCException#getMessage()}.
     * The function is should only be used for this purpose. Other user-readable
     * messages shall be constructed by other methods. The positional parameter
     * may be {@code null} but the other parameters must not be {@code null}.
     * 
     * @param problem problem identifier
     * @param positional position in input causing the problem
     * @param arguments argument for problem identifier
     * @return problem description for using feeding to parent constructor
     */
    private static String buildMessage(Problem problem, Positional positional,
            Object[] arguments) {
        assert problem != null;
        assert arguments != null;
        StringBuilder result = new StringBuilder();
        result.append(problem.getIdentifier());
        for (Object argument : arguments) {
            result.append(SPACE + argument);
        }
        if (positional != null) {
            result.append(SPACE + SPACE + BRACE_OPEN + positional + BRACE_CLOSE);
        }
        return result.toString();
    }

    /**
     * Construct message to provide to parent class of {@link EPMCException}.
     * The message will then be obtained using
     * {@link EPMCException#getMessage()}.
     * The function is should only be used for this purpose.
     * Other user-readable messages shall be constructed by other methods.
     * If {@link Builder#getMessage()} returns a non-{@code null} value, this
     * value will be used.
     * Otherwise, the message will be constructed using
     * {@link #buildMessage(Problem, Positional, Object[])}
     * using the parameters of the builder.
     * 
     * @param builder builder to read data from
     * @return message to provide to parent class of {@link EPMCException}
     */
    private static String buildMessage(Builder builder) {
        assert builder != null;
        String message = builder.getMessage();
        if (message == null) {
            Problem problem = builder.getProblem();
            Positional positional = builder.getPositional();
            Object[] arguments = builder.getArguments();
            return buildMessage(problem, positional, arguments);
        } else {
            return message;
        }
    }
    
    /**
     * Obtain the location of the input which caused the problem.
     * 
     * @return location of the input which caused the problem
     */
    public Positional getPositional() {
        return positional;
    }
}
