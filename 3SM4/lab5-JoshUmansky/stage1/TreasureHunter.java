package stage1;

import java.util.LinkedList;

enum Direction {STOP, UP, DOWN, LEFT, RIGHT}

public class TreasureHunter {
    public static final int MAXWEIGHT = 25; // kg
    public static final char symbol = 'P';
    
    private int xPos;
    private int yPos;
    private int accScore;
    private int accWeight;
    private int instScore;
    private int instWeight;
    private TreasureMap myMap; 
    private boolean completed;   

    private LinkedList<TreasureTile> myLoad;
    private Direction myDir;
    private int myNodeCount;

    public TreasureHunter(int x, int y, TreasureMap map)
    {
        xPos = x;
        yPos = y;
        accScore = 0;
        accWeight = 0;
        instScore = 0;
        instWeight = 0;
        myMap = map;
        myMap.setHunterRef(this);    
        myDir = Direction.STOP;  
        completed = false;  

        generatePath();
    }

    public int getX() {return xPos;}
    public int getY() {return yPos;}
    public int getScore() {return accScore;}
    public int getWeight() {return accWeight;}
    public int getInstScore() {return instScore;}
    public int getInstWeight() {return instWeight;}
    public boolean isCompleted() {return completed;}
    public LinkedList<TreasureTile> getLoad() {return myLoad;}    

    private void generatePath()
    {
        LinkedList<TreasureTile> treasureList = myMap.getTreasureList();

        // binary or fractional
        if(myMap.isSplittable()) // fractional
        {
            myLoad = planFractionalKnapsack(treasureList);
        }
        else  // binary
        {
            myLoad = planBinaryKnapsackDP(treasureList);
        }

        // Make sure destination is on the path
        myLoad.addFirst(new TreasureTile(TreasureMap.BOARDSIZEX - 2, TreasureMap.BOARDSIZEY - 2, 'D', false));

        myNodeCount = myLoad.size() - 1;
    }

    //Time Complexity: O(n log n) for sorting + O(n) for selection = O(n log n)
    //Space Complexity: O(n) for storing the sorted list
    private LinkedList<TreasureTile> planBinaryKnapsackGreedy(LinkedList<TreasureTile> treasureList)
    {
        // Sort the treasure list by profit ratio (Score / Weight) in descending order
        LinkedList<TreasureTile> sortedList = sortProfitRatio(treasureList);

        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();

        // Iterate through the sorted list and pick treasures until the weight limit is reached
        for (TreasureTile tile : sortedList) {
            int tileWeight = tile.getWeight();
            if (accWeight + tileWeight <= MAXWEIGHT) {
                plannedLoad.add(tile);
                accWeight += tileWeight;
                accScore += tile.getScore();
                //Debugging, believe test case is incorrect
                //System.out.printf("Added Tile: %c%d/%d\n", tile.getTileType(), tile.getScore(), tile.getWeight());
            }
        }
        return plannedLoad;
    }

    //Time Complexity: O(n log n)
    //Space Complexity: O(n) for storing the sorted list
    private LinkedList<TreasureTile> sortProfitRatio(LinkedList<TreasureTile> treasureList)
    {
        treasureList.sort((tile1, tile2) -> {
            double ratio1 = (double) tile1.getScore() / tile1.getWeight();
            double ratio2 = (double) tile2.getScore() / tile2.getWeight();
            return Double.compare(ratio2, ratio1); // Descending order
        });
        return treasureList;
    }

    // Top-Down (Memorization Approach)
    //Time Complexity: O(n * W) where n is the number of items and W is the maximum weight
    //Space Complexity: O(n * W) for the DP table
    private LinkedList<TreasureTile> planBinaryKnapsackDP(LinkedList<TreasureTile> treasureList)
    {
        int n = treasureList.size();
        int[][] dp = new int[n + 1][MAXWEIGHT + 1]; //Define the dp table being created
    
        // Build the DP table
        for (int i = 1; i <= n; i++) {
            TreasureTile tile = treasureList.get(i - 1);
            int weight = tile.getWeight();
            int score = tile.getScore();
    
            for (int w = 0; w <= MAXWEIGHT; w++) {
                if (weight > w) {
                    dp[i][w] = dp[i - 1][w]; // Cannot include this tile
                } else {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weight] + score); //// Include this tile, find max score
                }
            }
        }
    
        // Backtrack to find the selected tiles, this will be the optimal solution
        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();
        int w = MAXWEIGHT;
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) { //// This tile was included
                TreasureTile tile = treasureList.get(i - 1);
                plannedLoad.addFirst(tile);
                w -= tile.getWeight();
            }
        }
    
        // Update the accumulated score and weight
        accScore = dp[n][MAXWEIGHT];
        accWeight = MAXWEIGHT - w;
    
        return plannedLoad;
    }

    //Time Complexity: O(n log n) for sorting + O(n) for selection = O(n log n)
    //Space Complexity: O(n) for storing the sorted list
    private LinkedList<TreasureTile> planFractionalKnapsack(LinkedList<TreasureTile> treasureList)
    {        
        // Sort the treasure list by profit ratio (Score / Weight) in descending order
        LinkedList<TreasureTile> sortedList = sortProfitRatio(treasureList);

        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();        

        // Iterate through the sorted list and pick treasures, split if required
        for (TreasureTile tile : sortedList) {
            int tileWeight = tile.getWeight();
            int tileScore = tile.getScore();

            if (accWeight + tileWeight <= MAXWEIGHT) { //Case where entire tile fits
                // Add the entire tile if it fits
                plannedLoad.add(tile);
                accWeight += tileWeight;
                accScore += tileScore;
            } else {//Case where best tile exceeds the weight limit
                // Add a fraction of the tile if it exceeds the weight limit
                int remainingWeight = MAXWEIGHT - accWeight;
                double fraction = (double) remainingWeight / tileWeight;
                accWeight += remainingWeight;
                accScore += (int) (tileScore * fraction);

                //Make new tile with the fraction of the original tile
                TreasureTile fractionalTile = new TreasureTile(tile.getX(), tile.getY(), tile.getTileType(), tile.isSplittable());
                fractionalTile.getDataPair().setScore((int) (tileScore * fraction));
                fractionalTile.getDataPair().setWeight(remainingWeight);
                plannedLoad.add(fractionalTile);

                break; //Limit reached, exit & return
            }
        }

        return plannedLoad;
    }

    

    public void makeAMove()
    {
        // making moves to collect all the needed treasures and reach the destination

        if(myLoad == null) return;

        char[][] mapRef = myMap.getMapRef();
        // 1) Check the landed Tile property
        //    - Speed UP / DOWN: update the speed param
        //    - Speed Cam / Police: update the risk score
        //    - At Intersection, follow path and change the direction
        //    - if at Destination, set completed to true

        TreasureTile nextTile = myLoad.get(myNodeCount);
        ScoreWeightPair consumedAmount;
               
        if(mapRef[yPos][xPos] == '+' || mapRef[yPos][xPos] == '$' || mapRef[yPos][xPos] == '?')
        {            
            if(nextTile.getX() == xPos && nextTile.getY() == yPos)
            {
                if(myMap.isSplittable() && (instWeight + nextTile.getWeight()) > MAXWEIGHT)
                {
                    // splitting
                    consumedAmount = nextTile.consumeTreasure(MAXWEIGHT - instWeight); // consume the remaining
                }
                else
                {
                    consumedAmount = nextTile.consumeTreasure(nextTile.getWeight());                    
                }

                instScore += consumedAmount.getScore();
                instWeight += consumedAmount.getWeight(); 
                myMap.updateTreasureStatus(nextTile);   

                nextTile = myLoad.get(--myNodeCount);  // move on to the next tile
            }
        }
        else if(mapRef[yPos][xPos] == 'D')
        {            
            completed = true;
            return;
        }         

        if      (xPos > nextTile.getX())   myDir = Direction.LEFT;
        else if (xPos < nextTile.getX())   myDir = Direction.RIGHT;
        else if (yPos > nextTile.getY())   myDir = Direction.UP;
        else if (yPos < nextTile.getY())   myDir = Direction.DOWN;

        // 2) Check direction and update x / y Pos
        //    - and increment Tile count
        if      (myDir == Direction.LEFT)  xPos--;
        else if (myDir == Direction.RIGHT) xPos++;
        else if (myDir == Direction.UP)    yPos--;
        else if (myDir == Direction.DOWN)  yPos++;

    }








    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {        
        // add test here

        // 3 test cases for profit ratio sorting test
        testSortProfitRatioCase1();
        testSortProfitRatioCase2();
        
        testSortProfitRatioCaseCustom();

        // 2 test cases for Greedy Approach of Binary Knapsack
        //  ** and its shortcomings compared to DP counterpart Case1 and Case2
        testBinaryKnapsackGreedyCase1(); //Believe this test case is incorrect, max weight is 25, and there is a way to hit 25 exactly
        testBinaryKnapsackGreedyCase2();
        
        // 5 test cases for binary knapsack
        testBinaryKnapsackCase1();
        testBinaryKnapsackCase2(); //Also believe this test case is incorrect
        testBinaryKnapsackCase3();
        testBinaryKnapsackCase4();
        
        testBinaryKnapsackCaseCustom();

        // 5 test cases for fractional knapsack
        //All test cases will fail, as fractional tiles are added to the file, and the test cases check if the entire tile was added
        testFractionalKnapsackCase1();
        testFractionalKnapsackCase2();
        testFractionalKnapsackCase3();
        testFractionalKnapsackCase4();
        
        testFractionalKnapsackCaseCustom();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n", 
                          totalPassCount, 
                          totalTestCount);
        if(totalPassed)  
        {
            System.out.println("All Tests Passed.");
            System.exit(0);
        }
        else
        {   
            System.out.println("Tests Failed.");
            System.exit(-1);
        }        
    }



    // 3 test cases for profit ratio sorting test
    private static void testSortProfitRatioCase1()
    {
        // Setup
        System.out.println("============testSortProfitRatioCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 9; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testSortProfitRatioCase2()
    {
        // Setup
        System.out.println("============testSortProfitRatioCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 14; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testSortProfitRatioCaseCustom()
    {
        // Setup
        System.out.println("============testSortProfitRatioCaseCustom=============");
        boolean passed = true;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[5];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(15);
        testTiles[0].getDataPair().setWeight(5);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(5);
        testTiles[1].getDataPair().setWeight(1);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(90);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(60);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 5; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 4; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }


    // 2 test cases for binary knapsack using Greedy approach (and bad)
    private static void testBinaryKnapsackGreedyCase1()
    {
        // Setup
        System.out.println("============testBinaryKnapsackGreedyCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);    
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!    
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 100;
        int expectedAccWeight = 18; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackGreedy(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();               

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackGreedyCase2()
    {
        // Setup
        System.out.println("============testBinaryKnapsackGreedyCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 129;
        int expectedAccWeight = 23;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackGreedy(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    


    // 5 test cases for binary knapsack
    private static void testBinaryKnapsackCase1()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);    
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!    
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 115;
        int expectedAccWeight = 25; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();               

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase2()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(6);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(21);
        testTiles[1].getDataPair().setWeight(5);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(15);
        testTiles[2].getDataPair().setWeight(8);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(25);
        testTiles[3].getDataPair().setWeight(7);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(16);
        testTiles[4].getDataPair().setWeight(9);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(12);
        testTiles[5].getDataPair().setWeight(5);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(17);
        testTiles[6].getDataPair().setWeight(7);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(8);

        int expectedAccScore = 75;
        int expectedAccWeight = 24;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 8; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase3()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase3=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 132;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase4()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase4=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 140;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCaseCustom()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCaseCustom=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(1004);
        testTiles[0].getDataPair().setWeight(1);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(40);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(2000);
        testTiles[4].getDataPair().setWeight(20);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        int expectedAccScore = 3044;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 7; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    
    
    // 5 test cases for fractional knapsack
    private static void testFractionalKnapsackCase1()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0; 
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 126;
        int expectedAccWeight = 25; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);        
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight(); 
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;//fractional tile in result
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase2()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(6);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(21);
        testTiles[1].getDataPair().setWeight(5);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(15);
        testTiles[2].getDataPair().setWeight(8);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(25);
        testTiles[3].getDataPair().setWeight(7);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(16);
        testTiles[4].getDataPair().setWeight(9);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(12);
        testTiles[5].getDataPair().setWeight(5);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(17);
        testTiles[6].getDataPair().setWeight(7);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(8);

        int expectedAccScore = 77;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 8; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;//fractional tile in result
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase3()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase3=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 135;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false; //fractional tile in result
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase4()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase4=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 140;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();  

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = false; //fractional tile in result
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCaseCustom()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCaseCustom=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10000);
        testTiles[0].getDataPair().setWeight(25);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        int expectedAccScore = 10000;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 8; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();  

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    ////// ASSERTIONS //////
    ////// ASSERTIONS //////
    ////// ASSERTIONS //////

    private static boolean assertEquals(TreasureTile expected, TreasureTile actual)
    {
        if(!expected.isEqual(actual))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected:");
            expected.printTile();
            expected.printTileCoord();
            System.out.printf("\tActual:");
            actual.printTile();
            actual.printTileCoord();
            return false;
        }

        return true;
    }

    private static boolean assertEquals(int expected, int actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", expected, actual);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean expected, boolean actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", expected, actual);
            return false;
        }

        return true;
    }
    
}
