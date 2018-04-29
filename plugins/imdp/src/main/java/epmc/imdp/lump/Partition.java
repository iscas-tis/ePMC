package epmc.imdp.lump;

public interface Partition {
    /**
     * Marks a block to be split.
     * The block number parameter must be nonnegative and must be smaller than
     * the number of blocks obtained by {@link #getNumBlocks()}. The function
     * must be called exactly once before each call to {@link #split()}.
     * 
     * @param block block to split
     */
    void markBlockForSplit(int block);

    /**
     * Mark state to be split off from a block.
     * The state parameter denotes which state is to be split off. This value
     * must be a valid state in the model, that is state &ge; 0 and state 
     * &lt; the number of states of the original model. The part denotes the
     * number of the new block it is to be added to, where part &ge; 0 and
     * part &lt; number of new blocks the old block is to be split.
     * Before each call of {@link #split()}, this method must be called
     * exactly once for each state of the block to be split. Numbers of new
     * blocks must be consecutive. That is, e.g. it is not legal to only
     * use part=0, part=2, but not part=1.
     * 
     * @param state state to split off
     * @param part new block state will belong to
     */
    void markStateForSplit(int state, int part);

    /**
     * Split block.
     * Before calling this method, the block to split must have been chosen
     * using {@link #markBlockForSplit(int)}. The new blocks to which this
     * blocks will be split must have been set using
     * {@link #markStateForSplit(int, int)}.
     */
    void split();

    /**
     * Get number of blocks of the partition.
     * 
     * @return number of blocks of the partition.
     */
    int getNumBlocks();

    /**
     * Obtain the block of a state.
     * The state must be a valid state in the original model.
     * 
     * @param state state to get block of
     * @return block of the state
     */
    int getBlockNumberFromState(int state);

    /**
     * Get beginning of a block.
     * The block parameter must be a valid block number.
     * Indices i with
     * {@link #getBlockFrom(int)} &le; i &lt; {@link #getBlockTo(int)} can be
     * used as parameters to {@link #getBlockState(int)} to obtain the states
     * of the given block.
     * 
     * @param block block to get beginning of
     * @return beginning of block index
     */
    int getBlockFrom(int block);

    /**
     * Get end of a block.
     * The block parameter must be a valid block number.
     * Indices i with
     * {@link #getBlockFrom(int)} &le; i &lt; {@link #getBlockTo(int)} can be
     * used as parameters to {@link #getBlockState(int)} to obtain the states
     * of the given block.
     * 
     * @param block block to get end of
     * @return end of block index
     */
    public int getBlockTo(int blockNr);

    /**
     * Get a state of a block.
     * The block state number parameter must be such that
     * {@link #getBlockFrom(int)} &le; blockStateNr &lt;
     * {@link #getBlockTo(int)}.
     * 
     * @param blockStateNr block state number
     * @return state of the block
     */
    int getBlockState(int blockStateNr);
}
