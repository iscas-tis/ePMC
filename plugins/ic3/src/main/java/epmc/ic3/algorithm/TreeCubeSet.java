package epmc.ic3.algorithm;

import java.util.Iterator;
import java.util.TreeSet;

public class TreeCubeSet<E extends Comparable<?>> implements CubeSet<E>, Iterator<E> {

	private TreeSet<E> treeSet = null;
	public TreeCubeSet() {
		treeSet = new TreeSet<>();
	}
	public TreeCubeSet(TreeSet<E> set) {
		treeSet = set;
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return treeSet.size();
	}
	
	private TreeSet<E> getSet() {
		return treeSet;
	}
    
	@Override
	public TreeCubeSet<E> diff(CubeSet<E> a, CubeSet<E> b) {
		// TODO Auto-generated method stub
		TreeSet<E> result = new TreeSet<>();
		TreeSet<E> aTree = ((TreeCubeSet<E>)a).getSet();
		TreeSet<E> bTree = ((TreeCubeSet<E>)b).getSet();
        result.addAll(aTree);
        result.removeAll(bTree);
		return new TreeCubeSet<E>(result);
	}

	@Override
	public TreeCubeSet<E> union(CubeSet<E> a, CubeSet<E> b) {
		
		TreeSet<E> aTree = ((TreeCubeSet<E>)a).getSet();
		TreeSet<E> bTree = ((TreeCubeSet<E>)b).getSet();
		TreeSet<E> result = new TreeSet<>(aTree);
        result.addAll(bTree);
		return new TreeCubeSet<E>(result);
	}

	@Override
	public void swap( CubeSet<E> b) {
		TreeSet<E> bTree = ((TreeCubeSet<E>)b).getSet();
		TreeSet<E> tmp = treeSet;
		treeSet = bTree;
		((TreeCubeSet<E>)b).treeSet = tmp;
	}
	
	Iterator<E> iter;
	
	@Override
	public boolean insert(E cube) {
		return treeSet.add(cube);
	}
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return iter.hasNext();
	}
	@Override
	public E next() {
		// TODO Auto-generated method stub
		return iter.next();
	}
	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		iter = treeSet.iterator();
		return iter;
	}
	@Override
	public boolean contains(E cube) {
		// TODO Auto-generated method stub
		return treeSet.contains(cube);
	}
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return treeSet.isEmpty();
	}
	@Override
	public boolean remove(E cube) {
		// TODO Auto-generated method stub
		return treeSet.remove(cube);
	}
	@Override
	public boolean removeAll(CubeSet<E> set) {
		// TODO Auto-generated method stub
		TreeCubeSet<E> treeCube = (TreeCubeSet<E>) set;
		return treeSet.removeAll(treeCube.getSet());
	}
	
	public String toString() {
		Iterator<E> iter = iterator();
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		while(iter.hasNext()) {
			E e = iter.next();
			builder.append( e.toString() + ", ");
		}
		builder.append("}\n");
		return builder.toString();
	}

}
