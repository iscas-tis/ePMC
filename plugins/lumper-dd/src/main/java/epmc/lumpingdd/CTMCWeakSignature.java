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

import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.DoubleRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public final class CTMCWeakSignature implements Signature {

    public static class LumperDDSignatureCTMCWeak extends LumperDDSignature {
        public final static String IDENTIFIER = "lumper-dd-signature-ctmc-weak";

        public LumperDDSignatureCTMCWeak() {
            super(new CTMCWeakSignature(), IDENTIFIER);
        }
    }

    private GraphDD original;
    private ContextDD contextDD;
    private TransitionRepresentation transRepr;
    private DD pVar;
    private DD transStateSpace;

    @Override
    public void setOriginal(GraphDD original) {
        this.original = original;
        this.contextDD = original.getContextDD();
        this.transRepr = new DoubleRepresentation();
        this.transRepr.setOriginal(original);
    }

    @Override
    public boolean canLump(List<Expression> validFor) {
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsCTMC.isCTMC(semantics)) {
            return false;
        }
        for (Expression expr: validFor) {
            if (!LumperDDSignature.collectRewards(expr).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setBlockIndexVar(VariableDD blockIndex) {
        // Only do this after the block index variables have been created
        DD stateSpace = original.getNodeSpace().toMT();
        transStateSpace = transRepr.fromTransWeights().multiply(stateSpace);
        stateSpace.dispose();
        pVar = contextDD.newBoolean("p", 1).getValueEncoding(0).toMT();
    }

    @Override
    public TransitionRepresentation getTransitionRepresentation() {
        return transRepr;
    }

    @Override
    public DD computeSignatures(DD partitions) {
        Permutation p = original.getSwapPresNext();
        DD partitionsNext = partitions.permute(p);
        DD skipOwnPartition = contextDD.newConstant(1).subtractWith(partitions.clone());
        return transStateSpace.clone().multiplyWith(skipOwnPartition, partitionsNext)
                .abstractSumWith(original.getNextCube().clone())
                .addWith(pVar.multiply(partitions));
    }

}
