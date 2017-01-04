package epmc.sat;

public interface Lit extends Comparable{

    Lit mkLit(int var, boolean neg);
    
    int var() ;   /* variable index*/
    
    Lit clone(); 
    
    Lit not();
    
    Lit fromInt(int p);  /* from some exact integer */
    
    default boolean sign() {
        return (this.var() & 1) == 0 ? false : true;
    }
    
    int hashCode();
    
    int toInt();
    
    boolean undef();
    
    String toString();
}
