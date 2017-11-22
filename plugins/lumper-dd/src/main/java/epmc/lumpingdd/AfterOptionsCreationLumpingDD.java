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

package epmc.lumpingdd;

import java.util.Collection;
import java.util.Map;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Option;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationLumpingDD implements AfterOptionsCreation {
    public static String IDENTIFIER = "after-options-creation-lumping-dd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<?>> lumpersDD = options.get(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);
        assert lumpersDD != null;
        lumpersDD.put(StrongSignature.LumperDDSignatureStrong.IDENTIFIER, StrongSignature.LumperDDSignatureStrong.class);
        lumpersDD.put(CTMCWeakSignature.LumperDDSignatureCTMCWeak.IDENTIFIER, CTMCWeakSignature.LumperDDSignatureCTMCWeak.class);
        lumpersDD.put(DTMCWeakSignature.LumperDDSignatureDTMCWeak.IDENTIFIER, DTMCWeakSignature.LumperDDSignatureDTMCWeak.class);
        lumpersDD.put(MDPOneStepSignature.LumperDDSignatureMDPOneStep.IDENTIFIER, MDPOneStepSignature.LumperDDSignatureMDPOneStep.class);

        Option lumpersString = options.getOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
        assert lumpersString.getDefault() instanceof Collection<?>;
        @SuppressWarnings("unchecked")
        Collection<String> lumperDefault = (Collection<String>) lumpersString.getDefault();
        lumperDefault.addAll(lumpersDD.keySet());
    }

}
