package stage2;

import java.util.concurrent.TimeUnit;
import java.util.*;

public class Stage2 {
    
    private int complexity;
    private TileMap myMap; 
    private Tracer myTracer;
    private boolean exitFlag;   

    public Stage2(int starterScore)
    {
        complexity = 3;
        myMap = new TileMap(complexity);
        myTracer = new Tracer(1, 1, myMap, complexity, starterScore);
        exitFlag = false;
    }

    public int runStage2()
    {
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

            System.out.printf("Score: %d\n", myTracer.getScore());

            System.out.println("Optimal Path: ");
            myTracer.printPath();
            
            // LOOP DELAY
            try{
                TimeUnit.MICROSECONDS.sleep(100000);  // delay for 0.5 seconds
            }
            catch(InterruptedException e){
                System.out.println("Interrupted!");
            }

        } while(!exitFlag);

        return myTracer.getScore();
    }

    public static void main(String[] args)
    {
        Stage2 testStage2 = new Stage2(100);

        testStage2.runStage2();;        
    }
}


