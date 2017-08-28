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

package epmc.main.messages;

import epmc.messages.Message;

/**
 * Messages used in the main part of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class MessagesEPMC {
    /** Base name of resource bundle for the messages. */
    private final static String MESSAGES_EPMC = "MessagesEPMC";    
    /**
     * Assertions are enabled.
     * Thus, the tool will run somewhat slower, but error messages in case of
     * crashes are more useful than with disabled assertions. The tool should
     * not be run with assertions enabled for performance evaluation.
     */
    public final static Message ASSERTIONS_ENABLED = newMessage().setIdentifier("assertions-enabled").build();
    /**
     * Assertions are disabled.
     * Thus, the tool will run faster than with assertions enabled. However,
     * error messages in case of crashes will be less useful. The tool should be
     * run without assertions enabled for performance evaluation.
     */
    public final static Message ASSERTIONS_DISABLED = newMessage().setIdentifier("assertions-disabled").build();
    /** Prints the SVN revision of EPMC, if built using Maven. */
    public final static Message RUNNING_EPMC_REVISION = newMessage().setIdentifier("running-epmc-revision").build();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(MESSAGES_EPMC);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MessagesEPMC() {
    }
}
