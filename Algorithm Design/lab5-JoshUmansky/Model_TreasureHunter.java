import java.util.concurrent.TimeUnit;

import stage1.Stage1;
import stage2.Stage2;

public class Model_TreasureHunter {
    
    public static void main(String[] args)
    {
        int carryOverScore = 0;
        Stage1 testStage1 = new Stage1();        
        Stage2 testStage2;

        displayStageInfo("STAGE 1: TREASURE HUNTER - SCAVENGER MISSION", carryOverScore);
        carryOverScore = testStage1.runStage1();

        testStage2 = new Stage2(carryOverScore);

        displayStageInfo("STAGE 2: TREASURE HUNTER - CITY ESCAPE MISSION", carryOverScore);
        carryOverScore = testStage2.runStage2();

        displayStageInfo("GAME ENDED", carryOverScore);
    }

    public static void displayStageInfo(String str, int score)
    {
        System.out.print("\033[H\033[2J");      // need to test on Mac and Linux.  
        System.out.flush();                       // Watch out for M silicon platform.

        System.out.println(str);
        System.out.printf("Score: %d\n", score);
        try{
            TimeUnit.SECONDS.sleep(3);  // delay for 3 seconds
        }
        catch(InterruptedException e){
            System.out.println("Interrupted!");
        }
    }

}
