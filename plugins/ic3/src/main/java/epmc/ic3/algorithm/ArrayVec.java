package epmc.ic3.algorithm;

import java.util.ArrayList;
import java.util.List;

public class ArrayVec<E> implements Vec<E> {

	private List<E> vec = new ArrayList<>();
	@Override
	public int size() {
		return vec.size();
	}

	@Override
	public E get(int index) {
		// TODO Auto-generated method stub
		return vec.get(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void push(Object e) {
		// TODO Auto-generated method stub
		int idx = size();
		vec.add((E) e);
		assert vec.get(idx) == e;
	}

	@Override
	public E pop() {
		// TODO Auto-generated method stub
		E e = back();
		vec.remove(size() - 1);
		return e;
	}

	@Override
	public E back() {
		// TODO Auto-generated method stub
		return vec.get(size() - 1);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		vec.clear();
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		ArrayVec<E> arrVec = (ArrayVec<E>)o;
		if(this.size() < arrVec.size()) return -1;
		if(this.size() > arrVec.size()) return 1;
		
		for(int i = 0; i < size() ; i ++) {
			int h1 = get(i).hashCode();
			int h2 = arrVec.get(i).hashCode();
			if(h1 < h2) return -1;
			if(h1 > h2) return 1;
		}
		
		return 0;
	}
	@Override
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < size() ; i ++) {
			if(i == 0) builder.append(get(i));
			else builder.append(" & " + get(i));
		}
		return builder.toString();
	}
	
	public int hashCode() {
		return 0;
	}
	
	

}
