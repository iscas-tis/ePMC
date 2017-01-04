package epmc.jani.interaction.remote;

import java.rmi.RemoteException;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.messages.Message;

/**
 * RMI remote channel to send data back from the server to the client.
 * The data sent back from the server includes exceptions, model checking
 * results, and messages.
 * 
 * @author Ernst Moritz Hahn
 */
public interface EPMCChannel extends java.rmi.Remote {
    // TODO maybe get rid of this function and just always use absolute time
    void setTimeStarted(long time) throws RemoteException;

    void send(EPMCException exception) throws RemoteException;

    void send(String name, JsonValue result) throws RemoteException;    
    
    /**
     * Send back a status message to the client.
     * 
     * @param time time message was created
     * @param key message type specification
     * @param arguments arguments of message
     * @throws RemoteException thrown in case of connection problems
     */
    void send(long time, Message key, String... arguments) throws RemoteException;
}
