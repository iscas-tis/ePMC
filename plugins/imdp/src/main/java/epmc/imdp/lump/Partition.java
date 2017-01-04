package epmc.imdp.lump;

import java.util.Arrays;

import epmc.util.BitSet;
import epmc.util.BitSetUnboundedIntArray;

/**
 * Class to support signature-based partition refinement.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Partition {
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
	private int[] blockStates;
	/** Denotes the index bounds of partition blocks.
	 * The states of block <code>i</code> are contained in the indices
	 * <code>{@link #blockFromTo}[i * 2]</code> (inclusive) to
	 * <code>{@link #blockFromTo}[i * 2 + 1]</code> (exclusive) of
	 * {@link #blockStates}.
	 * */
	private int[] blockFromTo;
	/** Index of block being prepared for being split. */
	private int splitBlock = UNDEFINED_INDEX;
	/** Number of parts prepared for splitting block.
	 * That is, this number denotes the number of prepared split off parts
	 * rather than <code>preparedBlocks.length</code>. */
	private int numPreparedBlocks;
	/** Parts prepared for splitting block.
	 * See also {@link #preparedBlocksSizes} and {@link #numPreparedBlocks}. */
	private int[][] preparedBlocks = new int[0][];
	/** Sizes of the prepard split parts.
	 * That is, the size of part <code>i</code> to be split can be found in
	 * <code>preparedBlocksSizes[i]</code> rather than in
	 * <code>preparedBlocks[i].length</code>.*/
	private int[] preparedBlocksSizes = new int[0];
	
	/**
	 * Construct a new partition.
	 * The parameter of the function must not be {@code null}. It must be an
	 * array mapping each index to a partition number. Partition numbers must
	 * start from 0 and must be consecutive. The array provided by this
	 * parameter will be consumed by this function, and must not be used
	 * outside the partition after calling this constructor.
	 * 
	 * @param origToQuotientState array mapping states to partition numbers
	 */
	public Partition(int[] origToQuotientState) {
		assert assertOrigToQuotientState(origToQuotientState);
		this.origToQuotientState = origToQuotientState;
		computeInitialBlocks(origToQuotientState);
	}
	
	/**
	 * Assert that given array is a correct map from state to block numbers.
	 * 
	 * @param origToQuotientState
	 * @return always {@code true}, to allow being used with {@code assert}
	 */
    private boolean assertOrigToQuotientState(int[] origToQuotientState) {
		assert origToQuotientState != null;
		BitSet usedIndices = new BitSetUnboundedIntArray();
		for (int block : origToQuotientState) {
			assert block >= 0;
			usedIndices.set(block);
		}
		assert usedIndices.length() == usedIndices.cardinality();
		return true;
	}

    /**
     * Compute initial blocks from state-to-block array.
     * 
     * @param partition state-to-block array
     * @return list of blocks computed
     */
	private void computeInitialBlocks(int[] partition) {
        int numBlocks = 0;
        for (int i = 0; i < partition.length; i++) {
            numBlocks = Math.max(numBlocks, partition[i] + 1);
        }
        this.numBlocks = numBlocks;
        int[] sizes = new int[numBlocks];
        for (int i = 0; i < partition.length; i++) {
            int blockNr = partition[i];
            sizes[blockNr]++;
        }
        int lastTo = 0;
        this.blockFromTo = new int[partition.length * 2];
        for (int block = 0; block < numBlocks; block++) {
        	this.blockFromTo[block * 2] = lastTo;
        	lastTo += sizes[block];
        	this.blockFromTo[block * 2 + 1] = lastTo;
        }
        int lastSize = 0;
        for (int block = 0; block < numBlocks; block++) {
        	int nextSize = sizes[block] + lastSize;
        	sizes[block] = lastSize;
        	lastSize = nextSize;
        }
        blockStates = new int[partition.length];
        for (int i = 0; i < partition.length; i++) {
            int blockNr = partition[i];
            this.blockStates[sizes[blockNr]] = i;
            sizes[blockNr]++;
        }
    }
	
	/**
	 * Marks a block to be split.
	 * The block number parameter must be nonnegative and must be smaller than
	 * the number of blocks obtained by {@link #getNumBlocks()}. The function
	 * must be called exactly once before each call to {@link #split()}.
	 * 
	 * @param block block to split
	 */
	public void markBlockForSplit(int block) {
		assert block >= 0;
		assert block < numBlocks;
		assert this.splitBlock == UNDEFINED_INDEX;
		splitBlock = block;
	}
	
	public void markStateForSplit(int state, int part) {
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
	
	public void split() {
		assert splitBlock != UNDEFINED_INDEX;
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
		
		int preparedBlocksSize = numPreparedBlocks;
		for (int i = 1; i < preparedBlocksSize; i++) {
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

	public int getNumBlocks() {
		return numBlocks;
	}
	
	public int size() {
		return numBlocks;
	}

	public int getBlockNumberFromState(int node) {
		assert node >= 0 : node;
		assert node < origToQuotientState.length : node + SPACE + origToQuotientState.length;
		return origToQuotientState[node];
	}

	public int getBlockFrom(int blockNr) {
		return this.blockFromTo[blockNr * 2];
	}

	public int getBlockTo(int blockNr) {
		return this.blockFromTo[blockNr * 2 + 1];
	}

	public int getBlockState(int blockStateNr) {
		return this.blockStates[blockStateNr];
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
	
	public int[] getOrigToQuotientState() {
		return origToQuotientState;
	}
}
