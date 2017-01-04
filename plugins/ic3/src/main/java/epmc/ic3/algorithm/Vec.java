package epmc.ic3.algorithm;

import java.util.Collection;

// PDR verctor interface
public interface Vec<E> extends Comparable<E>{
	
	int size();      // current size
	
	E get(int index);
	
	void push(E e);  // push element to back
	
	E pop();         // remove and return top elements
	
	E back();        // do not remove top elements
	
	void clear();
	
	int compareTo(Object o);
	
	String toString();
	
}
