package epmc.webserver.backend.worker.channel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import epmc.EPMCServer;
import epmc.messages.EPMCMessageChannel;
import epmc.messages.Message;
import epmc.modelchecker.RawProperty;
import epmc.webserver.backend.DataStore;
import epmc.webserver.backend.worker.task.Task;
import epmc.webserver.backend.worker.task.worked.intermediate.PartialTask;
import epmc.webserver.backend.worker.task.worked.intermediate.SingleFormulaTask;

/**
 *
 * @author ori
 */
public class PrismModelMessageChannel extends UnicastRemoteObject implements EPMCMessageChannel {

	private transient final DataStore ds;
	private transient final Task task;
	private transient final Map<RawProperty, Integer> propToId;

	/**
	 * Create a new channel for communicating with an {@linkplain EPMCServer}
	 * @param ds the {@linkplain DataStore datastore} to be used to store received data
	 * @param task the {@linkplain Task task} computed by the {@linkplain EPMCServer}
	 * @param propToId a {@linkplain Map map} from properties to identifiers, used by {@linkplain #sendResult(epmc.modelchecker.RawProperty, java.lang.Object) sendResult}
	 * @throws RemoteException inherited
	 */
	public PrismModelMessageChannel(DataStore ds, Task task, Map<RawProperty, Integer> propToId) throws RemoteException {
		super();
		this.ds = ds;
		this.task = task;
		this.propToId = propToId;
	}

	private static final long serialVersionUID = 1L;
	private static MessageFormat formatter = new MessageFormat("");
    private static Locale locale;

	/**
	 * Send a message through this channel
	 * @param time time the message was send (in milliseconds)
	 * @param key the key of the message
	 * @param arguments the corresponding arguments
	 * @throws RemoteException inherited
	 */
	@Override
	public void send(long time, Message key, String... arguments) throws RemoteException {
	    // Moritz: note that time is in *milliseconds*
		PartialTask pt;
//		switch (key) {
//			case "" :
				formatter.applyPattern(key.getMessage(locale));
				pt = new PartialTask(task.getUserId(), task.getTaskId(), task.getOperation(), formatter.format(arguments));
				ds.addPartialTask(pt);
//				break;
//			default :
//		}
	}
	
	/**
	 * Send the result of the evaluation of a property
	 * @param property the {@linkplain RawProperty property} that has been evaluated
	 * @param value its corresponding value
	 * @throws RemoteException inherited
	 */
	public void sendResult(RawProperty property, Object value) throws RemoteException {
		SingleFormulaTask sft = new SingleFormulaTask(task.getUserId(), task.getTaskId(), task.getOperation(), propToId.get(property), property, value.toString());
		ds.addSingleFormulaTask(sft);
	}

	/**
	 * Set the locale for having localized messages
	 * @param locale the new locale
	 */
	public static void setLocale(Locale locale) {
	    PrismModelMessageChannel.locale = locale;	    
		formatter = new MessageFormat("");
		formatter.setLocale(locale);
	}

    @Override
    public void setTimeStarted(long time) {
        // Moritz: note that time is in *milliseconds*
        // Moritz: use e.g. new Date(time)
        // TODO Auto-generated method stub
    }
}
