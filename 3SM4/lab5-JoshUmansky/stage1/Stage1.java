package stage1;

import java.util.concurrent.TimeUnit;
import java.util.*;

public class Stage1 {
    
    private boolean exitFlag;   
    private TreasureMap myMap;
    private TreasureHunter myHunter;

    public Stage1()
    {   
        myMap = new TreasureMap(setGameMode());
        myHunter = new TreasureHunter(1, 1, myMap);        
        exitFlag = false;
    }

    public int runStage1()
    {
        // Program Loop
        do
        {
            // CLEAR SCREEN : This is the ANSI way in Java, but still need validations on different OSes
            System.out.print("\033[H\033[2J");      // need to test on Mac and Linux.  
            System.out.flush();                       // Watch out for M silicon platform.

            myHunter.makeAMove();
            if(myHunter.isCompleted()) 
                exitFlag = true;

            myMap.printMap(); 
            
            //1exitFlag = true; // for testing purposes, remove this line to run the game loop1

            
            System.out.printf("Treasure to Collect: ");
            for(TreasureTile t : myHunter.getLoad())
            {
                if(t.getTileType() != 'D')
                    System.out.printf("%d-%d(%d, %d) ", t.getScore(), t.getWeight(), t.getX(), t.getY());
                else
                    System.out.printf("DEST(%d, %d) ", t.getX(), t.getY());
            }
            
            // LOOP DELAY
            try{
                TimeUnit.MICROSECONDS.sleep(100000);  // delay for 0.5 seconds
            }
            catch(InterruptedException e){
                System.out.println("Interrupted!");
            }
        } while(!exitFlag);

        return myHunter.getScore();
    }


    public static void main(String[] args)
    {
        Stage1 testStage1 = new Stage1();

        testStage1.runStage1();
    }

    private static boolean setGameMode()
    {
        int input;
        Scanner myInputScanner = new Scanner(System.in);

        System.out.println("Select Test Setup:");
        System.out.println("1 - Non-Splittable Treasures (Binary Knapsack)");
        System.out.println("2 - Splittable Treasures (Fractional Knapsack)");
        
        input = myInputScanner.nextInt();
        myInputScanner.close();

        if(input <= 1) return false; // non-splittable treasure
        else return true; // splittable treasure       
    }
}
