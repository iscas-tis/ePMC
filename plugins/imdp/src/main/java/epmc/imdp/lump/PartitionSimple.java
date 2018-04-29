package epmc.imdp.lump;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to support signature-based partition refinement.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PartitionSimple implements Partition {
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** String containing a single comma. */
    private final static String COMMA = ",";
    /** String containing a single left curly brace. */
    private final static String LCURLY = "{";
    /** String containing a single right curly brace. */
    private final static String RCURLY = "}";
    /** String containing a single left square brace. */
    private final static String LBRACK = "[";
    /** String containing a single right square brace. */
    private final static String RBRACK = "]";
    /** Integer to represent an undefined index. */
    private final static int UNDEFINED_INDEX = -1;

    /** Number of blocks in the partition. */
    private int numBlocks;
    /** Maps states to block numbers. */
    private final int[] origToQuotientState;
    /** Contains states divided by blocks.
     * See {@link #blockFromTo}. */
    private final int[] blockStates;
    /** Denotes the index bounds of partition blocks.
     * The states of block <code>i</code> are contained in the indices
     * <code>{@link #blockFromTo}[i * 2]</code> (inclusive) to
     * <code>{@link #blockFromTo}[i * 2 + 1]</code> (exclusive) of
     * {@link #blockStates}.
     * */
    private final int[] blockFromTo;
    /** Index of block being prepared for being split. */
    private int splitBlock = UNDEFINED_INDEX;
    /** Number of parts prepared for splitting block.
     * That is, this number denotes the number of prepared split off parts
     * rather than <code>preparedBlocks.length</code>. */
    private int numPreparedBlocks;
    /** Parts prepared for splitting block.
     * See also {@link #preparedBlocksSizes} and {@link #numPreparedBlocks}. */
    private int[][] preparedBlocks = new int[0][];
    /** Sizes of the prepared split parts.
     * That is, the size of part <code>i</code> to be split can be found in
     * <code>preparedBlocksSizes[i]</code> rather than in
     * <code>preparedBlocks[i].length</code>.*/
    private int[] preparedBlocksSizes = new int[0];

    /**
     * Creates a new partition.
     * The partition will initially have one block containing all states of
     * the model.
     * 
     * @param numStates number of states of the original model
     */
    public PartitionSimple(int numStates) {
        assert numStates >= 0;
        origToQuotientState = new int[numStates];
        numBlocks = 1;
        blockFromTo = new int[numStates * 2];
        blockFromTo[1] = numStates;
        blockStates = new int[numStates];
        for (int state = 0; state < numStates; state++) {
            blockStates[state] = state;
        }
    }

    @Override
    public void markBlockForSplit(int block) {
        assert block >= 0;
        assert block < numBlocks : block + SPACE + numBlocks;
        assert splitBlock == UNDEFINED_INDEX : splitBlock;
        splitBlock = block;
    }

    @Override
    public void markStateForSplit(int state, int part) {
        assert state >= 0 : state;
        assert state < this.origToQuotientState.length : state;
        assert part >= 0 : part;
        numPreparedBlocks = Math.max(numPreparedBlocks, part + 1);
        if (preparedBlocks.length <= part) {
            int oldLength = preparedBlocks.length;
            preparedBlocks = Arrays.copyOf(preparedBlocks, part + 1);
            preparedBlocksSizes = Arrays.copyOf(preparedBlocksSizes, part + 1);
            for (int i = oldLength; i < part + 1; i++) {
                preparedBlocks[i] = new int[1];
            }
        }
        if (preparedBlocks[part].length <= preparedBlocksSizes[part]) {
            preparedBlocks[part] = Arrays.copyOf(preparedBlocks[part],
                    preparedBlocks[part].length * 2);
        }
        preparedBlocks[part][preparedBlocksSizes[part]] = state;
        preparedBlocksSizes[part]++;
    }

    @Override
    public void split() {
        assert assertSplit();
        blockFromTo[splitBlock * 2 + 1] = blockFromTo[splitBlock * 2]
                + preparedBlocksSizes[0];
        int write = blockFromTo[splitBlock * 2];
        {
            int[] block = preparedBlocks[0];
            int blockSize = preparedBlocksSizes[0];
            for (int j = 0; j < blockSize; j++) {
                int state = block[j];
                blockStates[write] = state;
                write++;
            }
        }

        for (int i = 1; i < numPreparedBlocks; i++) {
            blockFromTo[numBlocks * 2] = write;
            int[] block = preparedBlocks[i];
            int blockSize = preparedBlocksSizes[i];
            for (int j = 0; j < blockSize; j++) {
                int state = block[j];
                origToQuotientState[state] = numBlocks;
                blockStates[write] = state;
                write++;
            }
            blockFromTo[numBlocks * 2 + 1] = write;
            numBlocks++;
        }
        for (int i = 0; i < numPreparedBlocks; i++) {
            preparedBlocksSizes[i] = 0;
        }
        numPreparedBlocks = 0;
        splitBlock = UNDEFINED_INDEX;
    }

    /**
     * Assert that {@link #split()} can be correctly executed.
     * That is, the block to be split has been chosen, and the new blocks to
     * be generated have been correctly set.
     * 
     * @return {@code true} in case no assertions are violated
     */
    private boolean assertSplit() {
        assert splitBlock != UNDEFINED_INDEX;
        int origSize = blockFromTo[splitBlock * 2 + 1] - blockFromTo[splitBlock * 2];
        int preparedBlocksSize = 0;
        /* check that all new blocks are nonempty */
        for (int blockNr = 0; blockNr < numPreparedBlocks; blockNr++) {
            assert preparedBlocksSizes[blockNr] > 0;
        }
        /* check that no states is contained in more than new block */
        for (int blockNr = 0; blockNr < numPreparedBlocks; blockNr++) {
            preparedBlocksSize += preparedBlocksSizes[blockNr];
        }
        assert origSize == preparedBlocksSize : origSize + SPACE + preparedBlocksSize;
        /* check that union of new blocks is old block */
        Set<Integer> origBlockStates = new HashSet<>();
        for (int origBlockIndex = blockFromTo[splitBlock * 2];
                origBlockIndex < blockFromTo[splitBlock * 2 + 1];
                origBlockIndex++) {
            origBlockStates.add(blockStates[origBlockIndex]);
        }
        Set<Integer> newBlockStates = new HashSet<>();
        for (int blockNr = 0; blockNr < numPreparedBlocks; blockNr++) {
            for (int bsNr = 0; bsNr < preparedBlocksSizes[blockNr]; bsNr++) {
                newBlockStates.add(preparedBlocks[blockNr][bsNr]);
            }
        }
        assert origBlockStates.equals(newBlockStates);
        return true;
    }

    @Override
    public int getNumBlocks() {
        return numBlocks;
    }

    @Override
    public int getBlockNumberFromState(int state) {
        assert state >= 0 : state;
        assert state < origToQuotientState.length : state + SPACE + origToQuotientState.length;
        return origToQuotientState[state];
    }

    @Override
    public int getBlockFrom(int block) {
        assert block >= 0;
        assert block < numBlocks;
        return this.blockFromTo[block * 2];
    }

    @Override
    public int getBlockTo(int blockNr) {
        assert blockNr >= 0 : blockNr;
        assert blockNr < numBlocks : blockNr + SPACE + numBlocks;
        return blockFromTo[blockNr * 2 + 1];
    }

    @Override
    public int getBlockState(int blockStateNr) {
        assert blockStateNr >= 0 : blockStateNr;
        assert blockStateNr < blockStates.length
        : blockStateNr + SPACE + blockStates.length;
        return blockStates[blockStateNr];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(LCURLY);
        for (int block = 0; block < numBlocks; block++) {
            int from = blockFromTo[block * 2];
            int to = blockFromTo[block * 2 + 1];
            builder.append(LBRACK);
            for (int stateNr = from; stateNr < to; stateNr++) {
                int state = blockStates[stateNr];
                builder.append(state);
                builder.append(COMMA);
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append(RBRACK);
            builder.append(COMMA);
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(RCURLY);
        return builder.toString();
    }
}
