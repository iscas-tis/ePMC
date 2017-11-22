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
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.RewardSpecification;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMarkovChain;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.MultisetIndexRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public class StrongSignature implements Signature {

    public static class LumperDDSignatureStrong extends LumperDDSignature {

        public final static String IDENTIFIER = "lumper-dd-signature-strong";

        public LumperDDSignatureStrong() {
            super(new StrongSignature(), IDENTIFIER);
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
        this.transRepr = new MultisetIndexRepresentation();
        this.transRepr.setOriginal(original);
    }

    @Override
    public boolean canLump(List<Expression> validFor) {
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMarkovChain.isMarkovChain(semantics)) {
            return false;
        }
        DD zeroDD = contextDD.newConstant(0);
        for(Expression expr: validFor) {
            for (RewardSpecification r : LumperDDSignature.collectRewards(expr)) {
                if(!original.getEdgeProperty(r).equals(zeroDD)) {
                    return false;
                }
            }
        }
        zeroDD.dispose();
        return true;
    }

    @Override
    public void setBlockIndexVar(VariableDD blockIndex) {
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
        DD partitionsNext = partitions.permute(original.getSwapPresNext());
        return transStateSpace.clone().multiplyWith(partitionsNext)
                .abstractSumWith(original.getNextCube().clone())
                .addWith(pVar.multiply(partitions));
    }

}
