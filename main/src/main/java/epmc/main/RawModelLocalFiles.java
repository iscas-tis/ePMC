package epmc.main;

import static epmc.error.UtilError.ensure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import epmc.error.EPMCException;
import epmc.modelchecker.RawModel;
import epmc.modelchecker.error.ProblemsModelChecker;

// TODO document class

/**
 * Class reading raw model from local files.
 * 
 * @author Ernst Moritz Hahn
 */
public final class RawModelLocalFiles implements RawModel {
    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** Set of filenames used for the model to be parsed. */
    private final String[] modelFilenames;
    /** Set of filenames for properties to be parsed. */
    private final String[] propertyFilenames;

    /**
     * Create object to read raw model and properties from local files.
     * The model filenames parameter must not be {@code null} and must not
     * contain {@code null} entries.
     * If the property filenames parameters is not {@code null}, then it must
     * not contain {@code null} entries.
     * 
     * @param modelFilenames model filenames
     * @param propertyFilenames property filenames
     * @throws EPMCException thrown in case of problems
     */
    public RawModelLocalFiles(String[] modelFilenames, String[] propertyFilenames) throws EPMCException {
        assert modelFilenames != null;
        for (String filename : modelFilenames) {
            assert filename != null;
            ensure(Files.exists(Paths.get(filename)), ProblemsModelChecker.FILE_NOT_EXISTS, filename);
        }
        this.modelFilenames = modelFilenames;
        if (propertyFilenames == null) {
            this.propertyFilenames = null;
        } else {
            assert propertyFilenames != null;
            for (String filename : propertyFilenames) {
                assert filename != null;
                ensure(Files.exists(Paths.get(filename)), ProblemsModelChecker.FILE_NOT_EXISTS, filename);
            }
            this.propertyFilenames = propertyFilenames;
        }
    }

    @Override
    public InputStream[] getModelInputStreams() {
        // TODO improve user feedback in case of errors
        InputStream[] result = new InputStream[modelFilenames.length];
        for (int fileNr = 0; fileNr < modelFilenames.length; fileNr++) {
            Path path = Paths.get(modelFilenames[fileNr].trim());
            try {
                result[fileNr] = Files.newInputStream(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }
    
    @Override
    public InputStream[] getPropertyInputStreams() {
        // TODO improve user feedback in case of errors
        InputStream[] result = new InputStream[propertyFilenames.length];
        for (int fileNr = 0; fileNr < propertyFilenames.length; fileNr++) {
            Path path = Paths.get(propertyFilenames[fileNr].trim());
            try {
                result[fileNr] = Files.newInputStream(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }
}
