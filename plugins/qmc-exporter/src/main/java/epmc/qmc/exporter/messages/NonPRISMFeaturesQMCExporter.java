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

package epmc.qmc.exporter.messages;

import epmc.messages.Message;

/**
 * Messages used in the PRISM exporter plugin of EPMC.
 * This class only contains static fields and methods, and thus is protected
 * from being instantiated.
 * 
 * @author Andrea Turrini
 */
public final class NonPRISMFeaturesQMCExporter {
    /** Base name of resource bundle for the messages. */
    public final static String NONPRISM_FEATURES_QMC_EXPORTER = "NonPRISMFeaturesQMCExporter";
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ABS = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-abs").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_DISTANCE = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-distance").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_DIVIDEIGNOREZERO = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-divideignorezero").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ID = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-id").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ISNEGINF = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-isneginf").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ISONE = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-isone").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ISPOSINF = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-isposinf").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_ISZERO = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-iszero").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_OVERFLOW = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-overflow").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_SET = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-set").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_UNDERFLOW = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-underflow").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_OPERATOR_WIDEN = newMessage().setIdentifier("qmc-exporter-nonprism-feature-operator-widen").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_SEMANTIC_TYPE_QMC = newMessage().setIdentifier("qmc-exporter-nonprism-feature-semantic-type-qmc").build().toString();
    public final static String QMC_EXPORTER_NONPRISM_FEATURE_TYPE_ARRAY = newMessage().setIdentifier("qmc-exporter-nonprism-feature-type-array").build().toString();
  

    /**
     * Creates a new message with given identifier with this resource bundle.
     * The parameter may not be {@code null}.
     * 
     * @param message identifier of message to be created
     * @return message created
     */
    private static Message.Builder newMessage() {
        return new Message.Builder().setBundle(NONPRISM_FEATURES_QMC_EXPORTER);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private NonPRISMFeaturesQMCExporter() {
    }
}
