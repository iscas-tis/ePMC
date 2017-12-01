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

package epmc.jani.interaction.communication.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import epmc.modelchecker.RawModel;

// TODO document class

public final class RawModelByteArray implements RawModel {
    private static final long serialVersionUID = 1L;
    private final byte[][] modelData;
    private final byte[][] propertyData;

    public RawModelByteArray(byte[][] modelInputs, byte[][] propertyInputs) {
        assert modelInputs != null;
        for (byte[] input : modelInputs) {
            assert input != null;
        }
        this.modelData = modelInputs;
        if (propertyInputs == null) {
            propertyData = null;
        } else {
            for (byte[] input : propertyInputs) {
                assert input != null;
            }
            this.propertyData = propertyInputs;
        }
    }

    @Override
    public InputStream[] getModelInputStreams() {
        InputStream[] inputs = new InputStream[modelData.length];
        for (int inputNr = 0; inputNr < modelData.length; inputNr++) {
            inputs[inputNr] = new ByteArrayInputStream(modelData[inputNr]);
        }
        return inputs;
    }

    @Override
    public InputStream[] getPropertyInputStreams() {
        if (propertyData == null) {
            return null;
        }
        InputStream[] inputs = new InputStream[propertyData.length];
        for (int inputNr = 0; inputNr < propertyData.length; inputNr++) {
            inputs[inputNr] = new ByteArrayInputStream(propertyData[inputNr]);
        }
        return inputs;
    }

    @Override
    public Object getModelInputIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getPropertyInputIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }
}
