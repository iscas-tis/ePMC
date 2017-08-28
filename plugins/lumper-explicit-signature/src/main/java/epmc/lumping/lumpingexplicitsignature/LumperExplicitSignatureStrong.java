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

import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

public final class LumperExplicitSignatureStrong implements LumperExplicit {
    private LumperExplicitSignature inner = new LumperExplicitSignature(EquivalenceStrong.class);
    public final static String IDENTIFIER = "lumper-explicit-signature-strong";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean canLump() {
        return inner.canLump();
    }

    @Override
    public void lump() {
        inner.lump();
    }

    @Override
    public void setOriginal(GraphSolverObjectiveExplicit objective) {
        inner.setOriginal(objective);
    }

    @Override
    public GraphSolverObjectiveExplicit getQuotient() {
        return inner.getQuotient();
    }

    @Override
    public void quotientToOriginal() {
        inner.quotientToOriginal();
    }

}
