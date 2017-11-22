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

package epmc.lumping.lumpingexplicitsignature;

import java.util.Map;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationLumpingExplicitSignature implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-jani";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<?>> lumpers = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        assert lumpers != null;
        lumpers.put(LumperExplicitSignatureStrong.IDENTIFIER, LumperExplicitSignatureStrong.class);
        lumpers.put(LumperExplicitSignatureWeakCTMC.IDENTIFIER, LumperExplicitSignatureWeakCTMC.class);
    }

}
