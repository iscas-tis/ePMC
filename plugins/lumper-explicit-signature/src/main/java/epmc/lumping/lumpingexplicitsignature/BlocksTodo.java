package epmc.lumping.lumpingexplicitsignature;

interface BlocksTodo {
    final static int DONE = -1;
    
    void add(int block);
    
    int popNext();
    
    void done(int block);
}
