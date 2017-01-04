package epmc.unambiguous.util;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * compute powerset using iterator 
 * */

public class PowerSetIter<E> implements Iterator<Set<E>>,Iterable<Set<E>>{
    private E[] elemArr = null;
    private BitSet bset = null;

    @SuppressWarnings("unchecked")
	public PowerSetIter(Set<E> set)
    {
    	elemArr = (E[])set.toArray();
        bset = new BitSet(elemArr.length + 1); /** add another bit to indicate end of elements */
    }

    /**
     * whether reach the last element 
     * */
    @Override
    public boolean hasNext() {
        return !bset.get(elemArr.length);
    }
    
    /**
     * bset store the flag indicates whether add element at index i
     * */
    @Override
    public Set<E> next() {
        Set<E> returnSet = new HashSet<E>();
        for(int i = 0; i < elemArr.length; i++)
        {
            if(bset.get(i))
                returnSet.add(elemArr[i]);
        }
        /** prepare for next iteration, increment bitset
         *  like integer, but stored in bitset in reverse 
         *  direction 
         * */
        for(int i = 0; i < bset.size(); i++)
        {
            if(!bset.get(i)) /**/
            {
                bset.set(i);
                break;
            }else
                bset.clear(i);
        }

        return returnSet;
    }

    @Override
    public void remove() {
        assert false : "invalid operation";
    }

    @Override
    public Iterator<Set<E>> iterator() {
        return this;
    }
    
    @SuppressWarnings("unchecked")
	public void reset(Set<E> set) {
    	elemArr = (E[])set.toArray();
        bset = new BitSet(elemArr.length + 1);
    }

}
