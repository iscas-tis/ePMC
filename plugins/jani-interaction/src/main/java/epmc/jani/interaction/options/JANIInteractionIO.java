package epmc.jani.interaction.options;

/**
 * JANI interaction I/O methods.
 * 
 * @author Ernst Moritz Hahn
 */
public enum JANIInteractionIO {
	/** Communication using {@link System#out} and {@link System#in}. */
	STDIO,
	/** Communication using WebSockets. */
	WEBSOCKETS
}
