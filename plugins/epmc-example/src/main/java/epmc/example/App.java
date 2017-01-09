package epmc.example;

import java.io.IOException;

import fi.iki.elonen.NanoWSD;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        run();
    }
    
    public static void run(){    	
    	NanoWSD ws = new Server(8080, false);        
        try {
        	ws.start();
            System.out.println("Server started, hit Enter to stop.\n");
            System.in.read();
        } catch (IOException ignored) {
        	System.out.println("exception:" + ignored.getMessage());
        }
        ws.stop();
        System.out.println("Server stopped.\n");
    }
}
