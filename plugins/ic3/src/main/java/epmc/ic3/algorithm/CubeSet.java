package epmc.ic3.algorithm;

import java.util.Collection;
import java.util.Iterator;

public interface CubeSet<E extends Comparable<?>> extends Iterator<E>{
	
	int size();
	CubeSet<E> diff(CubeSet<E> a, CubeSet<E> b);  // difference of two sets, return elements of a
	CubeSet<E> union(CubeSet<E> a, CubeSet<E> b); // union of two sets
	void swap( CubeSet<E> b);
	boolean insert(E cube);
	Iterator<E> iterator();
	boolean isEmpty();
	boolean contains(E cube);
	boolean remove(E cube);
	boolean removeAll(CubeSet<E> set);

}
