package epmc.jani.interaction.communication.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import epmc.error.EPMCException;
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
    public InputStream[] getPropertyInputStreams() throws EPMCException {
        if (propertyData == null) {
            return null;
        }
        InputStream[] inputs = new InputStream[propertyData.length];
        for (int inputNr = 0; inputNr < propertyData.length; inputNr++) {
            inputs[inputNr] = new ByteArrayInputStream(propertyData[inputNr]);
        }
        return inputs;
    }
}
