package epmc.automaton;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import epmc.error.EPMCException;

public interface AutomatonExporter {
    enum Format {
        DOT
    }
    
    void setAutomaton(Automaton automaton);
    
    void setOutput(OutputStream out);
    
    void setFormat(Format format);
    
    void export() throws EPMCException;
    
    default String exportToString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        setOutput(out);
        try {
            export();
        } catch (EPMCException e) {
            return e.toString();
        }
        return out.toString();
    }
    
    default void print() throws EPMCException {
        setOutput(System.out);
        export();
    }
}
