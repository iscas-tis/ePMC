/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.main;

import static epmc.error.UtilError.ensure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
     */
    public RawModelLocalFiles(String[] modelFilenames, String[] propertyFilenames) {
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

    @Override
    public Object getModelInputIdentifier() {
        if (modelFilenames == null) {
            return this;
        }
        if (modelFilenames.length == 1) {
            return modelFilenames[0];
        }
        return Arrays.toString(modelFilenames);
    }

    @Override
    public Object getPropertyInputIdentifier() {
        if (propertyFilenames == null) {
            return this;
        }
        if (propertyFilenames.length == 1) {
            return propertyFilenames[0];
        }
        return Arrays.toString(propertyFilenames);
    }
}
