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
import epmc.graph.SemanticsDTMC;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.DoubleRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;

public class DTMCWeakSignature implements Signature {

    public static class LumperDDSignatureDTMCWeak extends LumperDDSignature {
        public final static String IDENTIFIER = "lumper-dd-signature-dtmc-weak";

        public LumperDDSignatureDTMCWeak() {
            super(new DTMCWeakSignature(), IDENTIFIER);
        }
    }

    private GraphDD original;
    private VariableDD blockIndexVar;
    private ContextDD contextDD;
    private TransitionRepresentation transRepr;
    private DD pVar;
    private DD trans;

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
        if (!SemanticsDTMC.isDTMC(semantics)) {
            return false;
        }
        for(Expression expr: validFor) {
            if(!LumperDDSignature.collectRewards(expr).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setBlockIndexVar(VariableDD blockIndex) {
        this.blockIndexVar = blockIndex;
        this.trans = transRepr.fromTransWeights();
        pVar = contextDD.newBoolean("p", 1).getValueEncoding(0).toMT();
    }

    @Override
    public TransitionRepresentation getTransitionRepresentation() {
        return transRepr;
    }

    @Override
    public DD computeSignatures(DD partitions) {
        Permutation presNext = original.getSwapPresNext();
        TypeWeightTransition typeWeightTransition = TypeWeightTransition.get();
        ValueAlgebra zeroValue = UtilValue.newValue(typeWeightTransition, 0);
        ValueAlgebra zeroOne = UtilValue.newValue(typeWeightTransition, 1);        
        DD zeroDD = contextDD.newConstant(zeroValue);
        DD oneDD  = contextDD.newConstant(zeroOne);
        DD partitionsNext = partitions.permute(presNext);
        DD blockIndexCube = blockIndexVar.newCube(0);
        // Silent(s) = \exists k: \sum t: P(s,k) and T(s,t) and P(t,k)
        DD silentStates = partitions.multiply(trans, partitionsNext)
                .abstractSumWith(original.getNextCube().and(blockIndexCube))
                .eqWith(oneDD.clone()).toMTWith();
        DD nonSilentState = partitions.abstractSum(blockIndexCube).multiplyWith(oneDD.subtract(silentStates));

        // Compute the signatures for non-silent states first
        // They are needed for computing the signatures of silent states
        // We don't need to worry about a divide by zero, because
        // 1 - P(s, [[s]]) = 0  iff s is silent
        DD skipOwnPartition = contextDD.newConstant(1).subtractWith(partitions.clone());
        DD nonSilentSignatures = trans.multiply(partitionsNext, nonSilentState, skipOwnPartition)
                .abstractSumWith(original.getNextCube().clone())
                .divideWith(
                        oneDD.clone().subtractWith(
                                partitions.multiply(trans, partitionsNext)
                                .abstractSumWith(original.getNextCube().and(blockIndexCube))
                                .addWith(silentStates.clone())));
        nonSilentState.dispose();
        skipOwnPartition.dispose();
        partitionsNext.dispose();

        // Reachability analysis:
        // Compute which silent states can reach which signatures
        DD signaturesReachable = nonSilentSignatures.clone();
        DD oldSignaturesReachable = contextDD.newConstant(false);
        DD reverseTrans = original.getTransitions().toMT();
        boolean reachedFixedPoint;
        do {
            oldSignaturesReachable.dispose();
            oldSignaturesReachable = signaturesReachable.multiply(silentStates);
            DD newFrontier = signaturesReachable.permuteWith(presNext)
                    .multiplyWith(reverseTrans.clone())
                    .abstractSumWith(original.getNextCube().clone())
                    .multiplyWith(silentStates.clone());
            signaturesReachable = newFrontier.add(oldSignaturesReachable);
            // Adjust the terminal values for states that have been visited before
            // They have been counted twice, and we don't want to mess up the signatures
            signaturesReachable = signaturesReachable.subtractWith(
                    signaturesReachable.multiply(oldSignaturesReachable).eqWith(zeroDD.clone()).notWith().toMTWith()
                    .multiplyWith(newFrontier));

            // TODO The condition of the if-statement can be 
            // reduced to !signaturesReachable.equals(oldSignaturesReachable)
            // once fraction values are available.
            ValueAlgebra smallValue = UtilValue.newValue(typeWeightTransition, "0.000001");
            DD placesEqual = signaturesReachable.subtract(oldSignaturesReachable).lt(contextDD.newConstant(smallValue));
            reachedFixedPoint = placesEqual.isLeaf() && placesEqual.isTrue();
        } while (!reachedFixedPoint);
        oldSignaturesReachable.dispose();
        reverseTrans.dispose();
        zeroDD.dispose();

        // Since we are working with DTMCs, the sum of probabilities on outgoing
        // transitions (and thus the sum within one signature) should always be one.
        // We can use this to find which silent states can reach exactly one signature.
        DD silentStatesThatReachOneSignature = 
                signaturesReachable.clone().abstractSumWith(blockIndexCube).eqWith(oneDD.clone()).toMTWith();
        DD otherSilentStates = silentStates.multiplyWith(oneDD.subtract(silentStatesThatReachOneSignature));
        oneDD.dispose();
        // Compute the signatures of both classes of silent states
        // The states that reach one signature get that signature
        // The rest of the silent states gets a signature sig(s) = {(-1,partition(s))}
        DD silentSignatures = silentStatesThatReachOneSignature.multiplyWith(signaturesReachable);
        silentSignatures = silentSignatures.addWith(
                otherSilentStates.multiplyWith(partitions.clone()).multiplyWith(contextDD.newConstant(-1)));

        return silentSignatures.addWith(nonSilentSignatures, pVar.multiply(partitions));
    }

}
