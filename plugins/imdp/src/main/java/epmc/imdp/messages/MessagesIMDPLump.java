package epmc.imdp.messages;

import epmc.messages.Message;

public final class MessagesIMDPLump {
    private final static String MESSAGES_IMDP_LUMP = "MessagesIMDPLump";

    public final static Message IMDP_LUMP_LUMP_START = newMessage().setIdentifier("imdp-lump-lump-start").build();
    public final static Message IMDP_LUMP_LUMP_DONE = newMessage().setIdentifier("imdp-lump-lump-done").build();
    public final static Message IMDP_LUMP_COMPUTE_INITIAL_START = newMessage().setIdentifier("imdp-lump-compute-initial-start").build();
    public final static Message IMDP_LUMP_COMPUTE_INITIAL_DONE = newMessage().setIdentifier("imdp-lump-compute-initial-done").build();
    public final static Message IMDP_LUMP_REFINEMENT_START = newMessage().setIdentifier("imdp-lump-refinement-start").build();
    public final static Message IMDP_LUMP_REFINEMENT_DONE = newMessage().setIdentifier("imdp-lump-refinement-done").build();
    public final static Message IMDP_LUMP_LP_STATISTICS = newMessage().setIdentifier("imdp-lump-lp-statistics").build();
    public final static Message IMDP_LUMP_SIGNATURE_STATISTICS = newMessage().setIdentifier("imdp-lump-signature-statistics").build();
    public final static Message IMDP_LUMP_BUILD_QUOTIENT_START = newMessage().setIdentifier("imdp-lump-build-quotient-start").build();
    public final static Message IMDP_LUMP_BUILD_QUOTIENT_DONE = newMessage().setIdentifier("imdp-lump-build-quotient-done").build();

    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_IMDP_LUMP);
    }

    private MessagesIMDPLump() {
    }
}
