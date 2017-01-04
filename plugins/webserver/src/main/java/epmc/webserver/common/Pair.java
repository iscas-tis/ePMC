package epmc.webserver.common;

import java.util.Objects;

/**
 * A simple class for pairing objects
 * @author ori
 * @param <T1> the type of the first element of the pair
 * @param <T2> the type of the second element of the pair
 */
public class Pair<T1, T2> {

	/**
	 * The first element of the pair
	 */
	public T1 fst;

	/**
	 * The second element of the pair
	 */
	public T2 snd;
	
	/**
	 * Construct a new pair of objects
	 * @param t1 the first element of the pair
	 * @param t2 the second element of the pair
	 */
	public Pair(T1 t1, T2 t2) {
		fst = t1;
		snd = t2;
	}
	
    @Override
    public String toString () {
            return "(" + fst + "," + snd + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                    Pair o = (Pair)obj;
                    return fst.equals(o.fst) && snd.equals(o.snd);
            }
            return false;
    }

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.fst);
		hash = 67 * hash + Objects.hashCode(this.snd);
		return hash;
	}
}
