package epmc.webserver.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsWebserver {
    private final static String ERROR_WEBSERVER = "ErrorWebserver";
    public final static Problem WORKER_MODEL_BUILDER_GENERAL_ERROR = newProblem("worker-model-builder-general-error");
    public final static Problem WORKER_MODEL_CHECKER_GENERAL_ERROR = newProblem("worker-model-checker-general-error");
    public final static Problem WORKER_MODEL_CHECKER_COMMUNICATION_FAILED = newProblem("worker-model-checker-communication-failed");
    public final static Problem WORKER_MODEL_CHECKER_CONNECTION_FAILED = newProblem("worker-model-checker-connection-failed");
    public final static Problem WORKER_MODEL_CHECKER_START_FAILED = newProblem("worker-model-checker-start-failed");
    public final static Problem WORKER_MODEL_CHECKER_TIMED_OUT = newProblem("worker-model-checker-timed-out");
    public final static Problem WORKER_MODEL_CHECKER_INVALID_OPTIONS = newProblem("worker-model-checker-invalid-options");
    public final static Problem WORKER_MODEL_CHECKER_INVALID_OPERATION = newProblem("worker-model-checker-invalid-operation");
    public final static Problem WORKER_MODEL_CHECKER_MEMORY_EXHAUSTED = newProblem("worker-model-checker-memory-exhausted");
    public final static Problem WORKER_MODEL_CHECKER_STACK_EXHAUSTED = newProblem("worker-model-checker-stack-exhausted");
    public final static Problem WORKER_MODEL_CHECKER_INTERNAL_JVM_ERROR = newProblem("worker-model-checker-internal-jvm-error");
    public final static Problem WORKER_MODEL_CHECKER_UNKNOWN_JVM_ERROR = newProblem("worker-model-checker-unknown-jvm-error");
    public final static Problem WORKER_MODEL_CHECKER_JVM_ERROR = newProblem("worker-model-checker-jvm-error");

    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(ERROR_WEBSERVER, ProblemsWebserver.class, name);
    }
    
    private ProblemsWebserver() {
    }
}
