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

package epmc.modelchecker;

import java.io.InputStream;
import java.io.Serializable;

// TODO complete documentation

/**
 * Represents the data of an unparsed model.
 * This class is meant to allow the server to read the raw data of models in
 * arbitrary format from different sources. Classes implementing this interface
 * might store might
 * <ul>
 * <li>refer e.g. to local files (which is efficient but only
 * works if the server and client reside on the same machine), or</li>
 * <li>store model data in {@code byte} arrays (which works for remote servers
 * but might require too much memory for large models), or</li>
 * <li>transfer the data on demand from the client to the server (which should
 * work well for large models but is harder to implement and might lead to
 * issues in connection with firewalls).</li>
 * </ul>
 * Instances of this class might be obtained by functions of
 * {@link UtilModelChecker}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface RawModel extends Serializable {
    /**
     * Obtain array of input streams of the unparsed model.
     * 
     * @return input streams for this model
     * 
     */
    InputStream[] getModelInputStreams();

    InputStream[] getPropertyInputStreams();
    
    Object getModelInputIdentifier();
    
    Object getPropertyInputIdentifier();
}
