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

import epmc.graph.Semantics;

/**
 * A model description.
 * Classes implementing this interface are the starting point when reading
 * models and properties as input. They are used to create the objects
 * necessary for further model checking and other analysis. In particular,
 * they can be used to generate explicit-state and symbolic
 * decision-diagram-based low-level models, and to obtain the properties
 * associated to a given model.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Model {
    /**
     * Return unique identifier for the model type the model is of.
     * 
     * @return unique identifier of model type
     */
    String getIdentifier();

    /**
     * Reads the model from its input file(s).
     * For some model classes (e.g. RDDL, MRMC), the model might be split into
     * several files, such that it is necessary to allow several input streams
     * as parameters. Also, property files might be given as secondary
     * parameters. An exception might be thrown if the model contains syntax
     * errors, the model type has been chosen incorrectly, or if the model is
     * too large to be parsed. None of the parameters may be
     * <code>null</code>.
     * @param object 
     * 
     * @param inputs input files of this model
     */
    void read(Object object, InputStream... inputs);

    /**
     * Get the semantics type of the model.
     * This method must not be called before the model has been read using
     * {@link #read(InputStream...) read}.
     * 
     * @return semantics type of the model
     */
    Semantics getSemantics();

    /**
     * Returns the list of properties associated to this model.
     * These properties might be part of the model files itself (e.g.
     * discounted reward properties in RDDL models) or given in property files
     * parsed along the model files. This method must not be called before the
     * model has been read using {@link #read(InputStream...) read}. Reading
     * the property list might fail for multiple reasons. In this case an
     * exception might be thrown, which should state as precise as possible
     * the reason for the failure.
     * 
     * @return list of properties
     */
    Properties getPropertyList();

    // TODO add functionality to write models
}
