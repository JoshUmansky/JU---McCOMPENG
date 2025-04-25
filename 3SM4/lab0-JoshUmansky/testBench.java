public class testBench {
 
    private static boolean passed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String[] args)
    {
        // Run all tests
        Test1();
        Test2();
        Test3();
        CustomTest1();
        CustomTest2();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n", 
                          totalPassCount, 
                          totalTestCount);
        if(passed)  
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

    private static void Test1()
    {
        // Setup
        System.out.println("============Test1=============");
        totalTestCount++;

        ModelCode_CardGame.myCards = new Card[5];
        ModelCode_CardGame.pocketSize = 5;
        ModelCode_CardGame.myCards[0] = new Card(7, 'S');
        ModelCode_CardGame.myCards[1] = new Card(9, 'C');
        ModelCode_CardGame.myCards[2] = new Card(2, 'D');
        ModelCode_CardGame.myCards[3] = new Card(14, 'H');
        ModelCode_CardGame.myCards[4] = new Card(8, 'C');        
        ModelCode_CardGame.sortCards();

        // Action
        passed &= assertEquals(2, ModelCode_CardGame.myCards[0].rank);
        passed &= assertEquals(7, ModelCode_CardGame.myCards[1].rank);
        passed &= assertEquals(8, ModelCode_CardGame.myCards[2].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[3].rank);
        passed &= assertEquals(14, ModelCode_CardGame.myCards[4].rank);

        // Tear Down
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void Test2()
    {
        // Setup
        System.out.println("============Test2=============");
        totalTestCount++;

        ModelCode_CardGame.myCards = new Card[8];
        ModelCode_CardGame.pocketSize = 8;
        ModelCode_CardGame.myCards[0] = new Card(4, 'S');
        ModelCode_CardGame.myCards[1] = new Card(12, 'C');
        ModelCode_CardGame.myCards[2] = new Card(3, 'D');
        ModelCode_CardGame.myCards[3] = new Card(6, 'H');
        ModelCode_CardGame.myCards[4] = new Card(11, 'C'); 
        ModelCode_CardGame.myCards[5] = new Card(10, 'D');
        ModelCode_CardGame.myCards[6] = new Card(14, 'H');
        ModelCode_CardGame.myCards[7] = new Card(8, 'C');        
        ModelCode_CardGame.sortCards();

        // Action
        passed &= assertEquals(3, ModelCode_CardGame.myCards[0].rank);
        passed &= assertEquals(4, ModelCode_CardGame.myCards[1].rank);
        passed &= assertEquals(6, ModelCode_CardGame.myCards[2].rank);
        passed &= assertEquals(8, ModelCode_CardGame.myCards[3].rank);
        passed &= assertEquals(10, ModelCode_CardGame.myCards[4].rank);
        passed &= assertEquals(11, ModelCode_CardGame.myCards[5].rank);
        passed &= assertEquals(12, ModelCode_CardGame.myCards[6].rank);
        passed &= assertEquals(14, ModelCode_CardGame.myCards[7].rank);

        // Tear Down 
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }  
    }

    private static void Test3()
    {
        // Setup
        System.out.println("============Test3=============");
        totalTestCount++;

        ModelCode_CardGame.myCards = new Card[12];
        ModelCode_CardGame.pocketSize = 12;
        ModelCode_CardGame.myCards[0] = new Card(9, 'S');
        ModelCode_CardGame.myCards[1] = new Card(2, 'C');
        ModelCode_CardGame.myCards[2] = new Card(7, 'D');
        ModelCode_CardGame.myCards[3] = new Card(8, 'H');
        ModelCode_CardGame.myCards[4] = new Card(14, 'C'); 
        ModelCode_CardGame.myCards[5] = new Card(10, 'D');
        ModelCode_CardGame.myCards[6] = new Card(5, 'S');
        ModelCode_CardGame.myCards[7] = new Card(11, 'C');
        ModelCode_CardGame.myCards[8] = new Card(13, 'D'); 
        ModelCode_CardGame.myCards[9] = new Card(10, 'S');
        ModelCode_CardGame.myCards[10] = new Card(4, 'H');
        ModelCode_CardGame.myCards[11] = new Card(8, 'C');         
        ModelCode_CardGame.sortCards();

        // Action
        passed &= assertEquals(2, ModelCode_CardGame.myCards[0].rank);
        passed &= assertEquals(4, ModelCode_CardGame.myCards[1].rank);
        passed &= assertEquals(5, ModelCode_CardGame.myCards[2].rank);
        passed &= assertEquals(7, ModelCode_CardGame.myCards[3].rank);
        passed &= assertEquals(8, ModelCode_CardGame.myCards[4].rank);
        passed &= assertEquals(8, ModelCode_CardGame.myCards[5].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[6].rank);
        passed &= assertEquals(10, ModelCode_CardGame.myCards[7].rank);
        passed &= assertEquals(10, ModelCode_CardGame.myCards[8].rank);
        passed &= assertEquals(11, ModelCode_CardGame.myCards[9].rank);
        passed &= assertEquals(13, ModelCode_CardGame.myCards[10].rank);
        passed &= assertEquals(14, ModelCode_CardGame.myCards[11].rank);

        // Tear Down 
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }  
    }

    private static void CustomTest1()
    {
        // Setup
        System.out.println("============CustomTest1=============");
        totalTestCount++;

        // Add your own custom test here
        ModelCode_CardGame.myCards = new Card[6];
        ModelCode_CardGame.pocketSize = 6;
        ModelCode_CardGame.myCards[0] = new Card(9, 'S');
        ModelCode_CardGame.myCards[1] = new Card(9, 'D');
        ModelCode_CardGame.myCards[2] = new Card(9, 'C'); 
        ModelCode_CardGame.myCards[3] = new Card(9, 'S');
        ModelCode_CardGame.myCards[4] = new Card(9, 'D'); 
        ModelCode_CardGame.myCards[5] = new Card(41, 'H');     
        ModelCode_CardGame.sortCards();

        // Action
        passed &= assertEquals(9, ModelCode_CardGame.myCards[0].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[1].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[2].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[3].rank);
        passed &= assertEquals(9, ModelCode_CardGame.myCards[4].rank);
        passed &= assertEquals(14, ModelCode_CardGame.myCards[5].rank);

        // Tear Down 
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }  
    }

    private static void CustomTest2()
    {
        // Setup
        System.out.println("============CustomTest2=============");
        totalTestCount++;

        ModelCode_CardGame.myCards = new Card[2];
        ModelCode_CardGame.pocketSize = 2;
        ModelCode_CardGame.myCards[0] = new Card(1, 'S');
        ModelCode_CardGame.myCards[1] = new Card(8, 'C');         
        ModelCode_CardGame.sortCards();

        // Action
        passed &= assertEquals(2, ModelCode_CardGame.myCards[0].rank);
        passed &= assertEquals(8, ModelCode_CardGame.myCards[1].rank);

        // Tear Down 
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static boolean assertEquals(int a, int b)
    {
        if(a != b)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", a, b);
            return false;
        }

        return true;
    }

}