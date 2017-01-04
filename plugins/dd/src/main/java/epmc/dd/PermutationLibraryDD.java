package epmc.dd;

/**
 * Library-specific permutation.
 * This class is intended to be used in combination with
 * {@link LibraryDD#newPermutation(int[])} and
 * {@link LibraryDD#permute(long, PermutationLibraryDD)} to generate and apply
 * library-specific permutations, the content of which depends on the
 * requirements of the underlying library. This is in contrast to
 * {@link Permutation}, which is library-independent and used to abstract from
 * the implementation details of DD libraries.
 * 
 * @author Ernst Moritz Hahn
 */
public interface PermutationLibraryDD {
}
