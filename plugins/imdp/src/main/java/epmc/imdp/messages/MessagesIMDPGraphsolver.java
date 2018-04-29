package epmc.imdp.messages;

import epmc.messages.Message;

public final class MessagesIMDPGraphsolver {
    private final static String MESSAGES_IMDP_GRAPHSOLVER = "MessagesIMDPGraphsolver";

    public final static Message IMDP_GRAPHSOLVER_SOLVE_START = newMessage().setIdentifier("imdp-graphsolver-solve-start").build();
    public final static Message IMDP_GRAPHSOLVER_SOLVE_DONE = newMessage().setIdentifier("imdp-graphsolver-solve-done").build();
    public final static Message IMDP_GRAPHSOLVER_BUILD_ITER_START = newMessage().setIdentifier("imdp-graphsolver-build-iter-start").build();
    public final static Message IMDP_GRAPHSOLVER_BUILD_ITER_DONE = newMessage().setIdentifier("imdp-graphsolver-build-iter-done").build();
    public final static Message IMDP_GRAPHSOLVER_ITER_START = newMessage().setIdentifier("imdp-graphsolver-iter-start").build();
    public final static Message IMDP_GRAPHSOLVER_ITER_DONE = newMessage().setIdentifier("imdp-graphsolver-iter-done").build();
    public final static Message IMDP_GRAPHSOLVER_PROGRESS = newMessage().setIdentifier("imdp-graphsolver-progress").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_IMDP_GRAPHSOLVER);
    }

    private MessagesIMDPGraphsolver() {
    }
}
