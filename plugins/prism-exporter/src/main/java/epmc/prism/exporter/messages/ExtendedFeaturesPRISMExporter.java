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

package epmc.prism.exporter.messages;

import epmc.messages.Message;

/**
 * Messages used in the PRISM exporter plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Andrea Turrini
 */
public final class ExtendedFeaturesPRISMExporter {
    /** Base name of resource bundle for the messages. */
    public final static String EXTENDED_FEATURES_PRISM_EXPORTER = "ExtendedFeaturesPRISMExporter";
    public final static String PRISM_EXPORTER_EXTENDED_FEATURE_SEMANTIC_TYPE_SMG = newMessage().setIdentifier("prism-exporter-extended-feature-semantic-type-smg").build().toString();
    public final static String PRISM_EXPORTER_EXTENDED_FEATURE_SMG_COALITION = newMessage().setIdentifier("prism-exporter-extended-feature-smg-coalition").build().toString();
    public final static String PRISM_EXPORTER_EXTENDED_FEATURE_PLAYER_DEFINITION = newMessage().setIdentifier("prism-exporter-extended-feature-player-definition").build().toString();

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(EXTENDED_FEATURES_PRISM_EXPORTER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ExtendedFeaturesPRISMExporter() {
    }
}
