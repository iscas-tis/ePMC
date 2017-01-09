package epmc.plugin;

import epmc.error.Problem;
import epmc.error.UtilError;

// TODO complete documentation

/**
 * Class collecting possible problems during handling of plugins.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsPlugin {
    public final static String PROBLEMS_PLUGIN = "ProblemsPlugin";
    public final static Problem PLUGIN_PLUGIN_FILE_NOT_FOUND = newProblem("plugin-plugin-file-not-found");
    public final static Problem PLUGIN_PLUGIN_FILE_NOT_READABLE = newProblem("plugin-plugin-file-not-readable");
    public final static Problem PLUGIN_IO_PROBLEM = newProblem("plugin-io-problem");
    public final static Problem PLUGIN_CLASS_LOAD_FAILED = newProblem("plugin-class-load-failed");
    public final static Problem PLUGIN_CLOSE_PLUGIN_LOADER_FAILED = newProblem("plugin-close-plugin-loader-failed");
    public final static Problem PLUGIN_CLASS_NOT_FOUND = newProblem("plugin-class-not-found");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_NO_MANIFEST = newProblem("plugin-file-not-plugin-no-manifest");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_MANIFEST_NOT_REGULAR = newProblem("plugin-file-not-plugin-manifest-not-regular");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_EPMC_PLUGIN_MISSING = newProblem("plugin-file-not-plugin-epmc-plugin-missing");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_EPMC_PLUGIN_NOT_TRUE = newProblem("plugin-file-not-plugin-epmc-plugin-not-true");
    public final static Problem PLUGIN_FILE_NOT_REGULAR_OR_DIRECTORY = newProblem("plugin-file-not-regular-or-directory");
    public final static Problem PLUGIN_IO_PROBLEM_MANIFEST = newProblem("plugin-io-problem-manifest");
    public final static Problem PLUGIN_CLASS_INSTANTIATION_FAILED_WITH_NAME = newProblem("plugin-class-instantiation-failed-with-name");
    public final static Problem PLUGIN_CLASS_INSTANTIATION_FAILED = newProblem("plugin-class-instantiation-failed");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_MANIFEST_MISSING_ENTRY = newProblem("plugin-file-not-plugin-manifest-missing-entry");
    public final static Problem PLUGIN_FILE_NOT_PLUGIN_MANIFEST_EPMC_PLUGIN_NOT_TRUE = newProblem("plugin-file-not-plugin-manifest-epmc-plugin-not-true");
    public final static Problem PLUGIN_JAR_FILESYSTEM_FAILED = newProblem("plugin-jar-filesystem-failed");
    public final static Problem PLUGIN_READ_DIRECTORY_FAILED = newProblem("plugin-read-directory-failed");
    public final static Problem PLUGIN_PLUGIN_SPECIFIED_TWICE = newProblem("plugin-plugin-specified-twice");
    public final static Problem PLUGIN_PLUGIN_DEPENDENCIES_MISSING = newProblem("plugin-plugin-dependencies-missing");
    public final static Problem PLUGIN_READ_PLUGIN_LIST_IO_EXCEPTION = newProblem("plugin-read-plugin-list-io-exception");
    
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_PLUGIN, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsPlugin() {
    }
}
