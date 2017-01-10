/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

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
