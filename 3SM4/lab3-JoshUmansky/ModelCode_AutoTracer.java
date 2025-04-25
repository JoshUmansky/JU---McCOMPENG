package Lab3Model;

import java.util.concurrent.TimeUnit;
import java.util.*;

public class ModelCode_AutoTracer {
    
    public static void main(String args[]) throws Exception
    {
        TileMap myMap = new TileMap(0);
        //myMap.printMap();
        //System.out.println("Map Created");
        Tracer myTracer = new Tracer(1, 1, myMap);
        //System.out.println("Tracer Created");
        boolean exitFlag = false;   
        
        // Program Loop
        do
        {
            // CLEAR SCREEN : This is the ANSI way in Java, but still need validations on different OSes
            System.out.print("\033[H\033[2J");      // need to test on Mac and Linux.  
            System.out.flush();                       // Watch out for M silicon platform.
            
            // RUN LOGIC
            // Tracer makes path finding at Start (S) : Lab 3 / Every Intersection (I) : Lab 4 and 5
            // Tracer follows path at every loop iter.
            // Game Loop Ends when 
            //  a) Tracer arrives at D - draw, then END.
            //  b) Tracer sees risk score > 100, leading arrest - draw, then END.
            // Record 1) Tiles Travelled, 2) Total Time to Reach D, 3) Risk Score to Reach D

            myTracer.makeAMove();
            if(myTracer.isCompleted()) exitFlag = true;


            // DRAW SCREEN
            Tile[][] mapRef = myMap.getMapRef();
            LinkedList<Tile> path = myTracer.getFullPath();
            // need an overall Draw() routine.    
            for(int i = 0; i < TileMap.BOARDSIZEY; i++)
            {
                for(int j = 0; j < TileMap.BOARDSIZEX; j++)
                {
                    
                    if(myTracer.getY() == i && myTracer.getX() == j)    // Top: Tracer
                    {
                        System.out.printf("%c", Tracer.SYMBOL);
                    }
                    else if(path.contains(mapRef[i][j]) && mapRef[i][j].getTileType() == ' ')   // Middle: Path
                    {
                        System.out.printf("%c", '.');
                    }
                    else     // Bottom: Map
                    {
                        System.out.printf("%c", mapRef[i][j].getTileType());
                    }
                }
                System.out.printf("\n");
            }       

            System.out.printf("Tiles Travelled: %d\n", myTracer.getTileCount());

            System.out.println("Optimal Path: ");
            myTracer.printPath();
            
            // LOOP DELAY
            TimeUnit.MICROSECONDS.sleep(100000);  // delay for 0.5 seconds

        } while(!exitFlag);
    }

}



// Lab 3 notes
// build unweighted, directed graph to map all possible paths from S to D
// all intersections are considered nodes, identified by their Tile coordinate (x,y)
//
// find the path with the Least Number of Intersections to reach D from S.  
//  This is considered the shortest possible path because each intersection may
//  require a small increase in "stop time" 
//
// plot the path on the map using '.', but do not plot over any tiles except ' ' (empty paths)
//
// Then, observe P (the tracer) follows the path to reach the destination.



