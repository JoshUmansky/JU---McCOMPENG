import java.util.Random;

public class HandsMaxHeap {
    private Hands[] myHeap;  // array
    private int size;      // heap size (number of items stored in the heap)
    private int capacity;  // heap capacity (the maximum number of items the heap could store)

    // Constructor 1: creates an empty heap with a given capacity
    // O(1) time, O(n) space
    public HandsMaxHeap(int bufSize)
    {
            
        capacity = bufSize;
        size = 0;
        myHeap = new Hands[capacity + 1];
        myHeap[0] = new Hands();    
    }

    // Constructor 2: constructs a heap out of the array someHands 
    // the first element in the array is treated as a dummy, the remaining elements are organized as a heap using 
    // O(n) time, O(n) space    
    public HandsMaxHeap(Hands[] someHands)
    {
        capacity = someHands.length - 1;
        size = capacity;
        myHeap = new Hands[capacity + 1];
        System.arraycopy(someHands, 0, myHeap, 0, someHands.length);
        buildMaxHeap();  
    }

    
    
    // Organizes the array as a max heap
    // O(n) time, O(1) space
    private void buildMaxHeap()
    {
        for (int i = size / 2; i > 0; i--)
        {
            downHeapify(i);
        }

    }
  
    

    // [Problem 1-1] Implement Private Utility Methods
    //All just O(1) time, O(1) space
    // 1. A private method calculating the parent index
    private int parent(int index)
    {
        return index / 2;
    }
    
    // 2. A private method calculating the left-child index
    private int leftChild(int index)
    {
        return 2 * index;
    }

    // 3. A private method calculating the right-child index
    private int rightChild(int index)
    {
        return 2 * index + 1;
    }

    // 4. A private method to reorganize the heap from a given index downward
    //O(logn) time, O(1) space
    private void downHeapify(int index)
    {
        
        int left = leftChild(index);
        int right = rightChild(index);
        int largest = index;

        if(left <= size && myHeap[left].isMyHandLarger(myHeap[largest]))
            largest = left;
            
        if(right <= size && myHeap[right].isMyHandLarger(myHeap[largest]))
            largest = right;

        if(largest != index){
            Hands temp = myHeap[index];
            myHeap[index] = myHeap[largest];
            myHeap[largest] = temp;
            downHeapify(largest);
        }             
    } 

    // [Problem 1-3] Implement Upward Heap Reorganization Private Method from the provided index 
    //Same as downheap, but in reverse
    // O(logn) time, O(1) space
    private void heapifyUp(int index)
    {   
        
        Hands temp = myHeap[index];

        while(index > 1 && temp.isMyHandLarger(myHeap[parent(index)]))
        {
            myHeap[index] = myHeap[parent(index)];
            index = parent(index);
        }
        myHeap[index] = temp;
    }

    // Insert Method
    // O(logn) time (Unless array needs to be resized then O(n)), O(1) space (O(n) if resizing)
    public void insert(Hands thisHand)
    {
        // insert thisHand into the heap; if there is no room for insertion allocate a bigger array (the capacity of the new heap should be twice larger) and copy the data over
        if(size == capacity)
        {
            Hands[] newHeap = new Hands[capacity * 2 + 1];
            for(int i = 0; i <= size; i++)
                newHeap[i] = myHeap[i];
            myHeap = newHeap;
            capacity = capacity * 2;
        }
        // insert the hand at the end of the heap and reorganize the heap from the last index upward
        size++;
        myHeap[size] = thisHand;
        heapifyUp(size);           
    }
    //remove the largest Hand from the heap; if the heap is empty throw a RuntimeException
    // O(logn) time, O(1) space
    public Hands removeMax() throws RuntimeException
    {        
        if(size == 0)
            throw new RuntimeException("Heap is Empty");
        else{
            Hands maxHand = myHeap[1];
            myHeap[1] = myHeap[size];
            size--;
            downHeapify(1);
            return maxHand;
        }
    }

    //Accessor Methods
    // O(1) time, O(1) space
    public int getSize()
    {
        return size;
    }

    public boolean isEmpty()
    {
        if(size == 0)
            return true;
        else
            return false;
    }
    // For Debugging Purpose - Print all the heap elements (i.e. Hands) by traversing the heap in level order        
    //  For valid hands, print the hand using the respective method in Hands class
    //  For invalid hands, just print "--INV--"
    //  Use the required method in Hands class to determine whether a Hand is valid.
    // O(n) time, O(1) space
    public void printHeap()
    {        
        for(int i = 1; i <= size; i++)
        {
            if(myHeap[i].isAValidHand())
                myHeap[i].printMyHand();
            else
                System.out.println("--INV--");
        }
   }
    // Sort In place the incoming array myHands in Decsending order
    // using the heap sort algorithm
    // O(nlogn) time, O(1) space
    public static void heapSort(Hands[] myHands)
    {        
        Hands[] myModHands = new Hands[myHands.length + 1];
        System.arraycopy(myHands, 0, myModHands, 1, myHands.length);
        //Copies the array into a new array with a dummy element at the beginning for use in the Heap constructor
        HandsMaxHeap myHeap = new HandsMaxHeap(myModHands);
        for (int i = 0; i <= myHands.length - 1; i++) {            
            myHands[i] = myHeap.removeMax();
        }
    }


   //************************************************************* This is the Test Bench!! ***********************************************************************

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {
        testParent();
        testLeftChild();
        testRightChild();
        testHandsMaxHeap1();        
        testHandsMaxHeap2();
        testInsert1();
        testInsert2();
        
        testGetSize1();
        testGetSize2();
        testRemoveMax1();
        testRemoveMax2();

        CustomTestInsert1();
        CustomTestInsert2();
        CustomTestRemoveMax1();
        CustomTestRemoveMax2();

        testHeapSort1();
        testHeapSort2();

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

    private static void testParent(){
        
        // Setup
        System.out.println("============testParent=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(10);
        int result, expected;
       
        // Action
        for(int i = 1; i < 50; i++)
        {
            result = myMaxHeap.parent(i);
            expected = i / 2;
            passed &= assertEquals(expected, result);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
        
    }
    
    private static void testLeftChild(){
        // Setup
        System.out.println("============testLeftChild=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(10);
        int result, expected;
        
        // Action
        for(int i = 1; i < 50; i++)
        {
            result = myMaxHeap.leftChild(i);
            expected = 2 * i;
            passed &= assertEquals(expected, result);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    
    private static void testRightChild(){
        // Setup
        System.out.println("============testRightChild=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(10);
        int result, expected;
        
        // Action
        for(int i = 1; i < 50; i++)
        {
            result = myMaxHeap.rightChild(i);
            expected = (2 * i) + 1;
            passed &= assertEquals(expected, result);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testHandsMaxHeap1(){
        // Setup
        System.out.println("============testHandsMaxHeap1=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);
        int result, expected;

        // Action
        result = myMaxHeap.capacity;
        expected = 20;
        passed &= assertEquals(expected, result);
        
        result = myMaxHeap.size;
        expected = 0;
        passed &= assertEquals(expected, result);

        Hands expectedHand = new Hands();
        Hands actualHand = myMaxHeap.myHeap[0];
        passed &= assertEquals(expectedHand, actualHand);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testHandsMaxHeap2(){
        // Setup
        System.out.println("============testHandsMaxHeap2=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(50);
        int result, expected;

        // Action
        result = myMaxHeap.capacity;
        expected = 50;
        passed &= assertEquals(expected, result);
        
        result = myMaxHeap.size;
        expected = 0;
        passed &= assertEquals(expected, result);

        Hands expectedHand = new Hands();
        Hands actualHand = myMaxHeap.myHeap[0];
        passed &= assertEquals(expectedHand, actualHand);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testInsert1(){
        // Setup
        System.out.println("============testInsert1=============");
        boolean passed = true;
        totalTestCount++;
        
        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);
        Hands myHandsArray[] = new Hands[15];  
        Hands expectedHandsArray[] = new Hands[20];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H'));
        myHandsArray[2] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H'));
        myHandsArray[5] = new Hands(new Card(6, 'S'), new Card(7, 'D'), new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'));
        myHandsArray[6] = new Hands(new Card(14, 'C'), new Card(14, 'D'), new Card(6, 'C'), new Card(14, 'S'), new Card(14, 'H'));
        myHandsArray[7] = new Hands(new Card(11, 'H'), new Card(11, 'D'), new Card(11, 'C'), new Card(5, 'H'), new Card(5, 'S'));
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'));
        myHandsArray[9] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(8, 'H'));
        myHandsArray[10] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(13, 'S'), new Card(14, 'S'));
        myHandsArray[11] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        myHandsArray[12] = new Hands(new Card(5, 'S'), new Card(10, 'D'), new Card(10, 'C'), new Card(5, 'C'), new Card(10, 'H'));
        myHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(4, 'H'), new Card(3, 'H'));
        myHandsArray[14] = new Hands(new Card(3, 'C'), new Card(5, 'D'), new Card(3, 'S'), new Card(5, 'S'), new Card(3, 'D'));
        
        for(int i = 0; i < 15; i++)
            myMaxHeap.insert(myHandsArray[i]);

        expectedHandsArray[0] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(13, 'S'), new Card(14, 'S'));
        expectedHandsArray[1] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'));
        expectedHandsArray[2] = new Hands(new Card(14, 'C'), new Card(14, 'D'), new Card(6, 'C'), new Card(14, 'S'), new Card(14, 'H'));
        expectedHandsArray[3] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H'));
        expectedHandsArray[4] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
        expectedHandsArray[5] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        expectedHandsArray[6] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H'));
        expectedHandsArray[7] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H'));
        expectedHandsArray[8] = new Hands(new Card(11, 'H'), new Card(11, 'D'), new Card(11, 'C'), new Card(5, 'H'), new Card(5, 'S'));
        expectedHandsArray[9] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(8, 'H'));
        expectedHandsArray[10] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        expectedHandsArray[11] = new Hands(new Card(6, 'S'), new Card(7, 'D'), new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'));
        expectedHandsArray[12] = new Hands(new Card(5, 'S'), new Card(10, 'D'), new Card(10, 'C'), new Card(5, 'C'), new Card(10, 'H'));
        expectedHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(4, 'H'), new Card(3, 'H'));
        expectedHandsArray[14] = new Hands(new Card(3, 'C'), new Card(5, 'D'), new Card(3, 'S'), new Card(5, 'S'), new Card(3, 'D'));
                

        // Action
        for(int i = 0; i < myMaxHeap.size; i++)
            passed &= assertEquals(expectedHandsArray[i], myMaxHeap.myHeap[i + 1]);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testInsert2(){
        // Setup
        System.out.println("============testInsert2=============");
        boolean passed = true;
        totalTestCount++;
        
        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);
        Hands myHandsArray[] = new Hands[22];  
        Hands expectedHandsArray[] = new Hands[20];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        myHandsArray[6] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
        myHandsArray[7] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        myHandsArray[9] = new Hands(new Card(2, 'S'), new Card(2, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(2, 'H'));
        myHandsArray[10] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        myHandsArray[11] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        myHandsArray[12] = new Hands(new Card(6, 'S'), new Card(5, 'D'), new Card(6, 'C'), new Card(5, 'C'), new Card(5, 'H'));
        myHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(8, 'H'), new Card(9, 'H'));
        myHandsArray[14] = new Hands(new Card(13, 'C'), new Card(5, 'D'), new Card(13, 'S'), new Card(5, 'S'), new Card(13, 'D'));
        myHandsArray[15] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        myHandsArray[16] = new Hands(new Card(4, 'C'), new Card(4, 'D'), new Card(4, 'S'), new Card(6, 'S'), new Card(4, 'H'));
        myHandsArray[17] = new Hands(new Card(3, 'H'), new Card(5, 'H'), new Card(3, 'S'), new Card(5, 'S'), new Card(5, 'D'));
        myHandsArray[18] = new Hands(new Card(10, 'S'), new Card(8, 'C'), new Card(8, 'S'), new Card(10, 'H'), new Card(10, 'D'));
        myHandsArray[19] = new Hands(new Card(5, 'C'), new Card(8, 'D'), new Card(7, 'S'), new Card(6, 'S'), new Card(9, 'D'));
        myHandsArray[20] = new Hands(new Card(7, 'S'), new Card(7, 'D'), new Card(4, 'S'), new Card(4, 'D'), new Card(7, 'C'));
        myHandsArray[21] = new Hands(new Card(9, 'D'), new Card(10, 'D'), new Card(13, 'D'), new Card(11, 'D'), new Card(12, 'D'));

        
        for(int i = 0; i < 20; i++)
            myMaxHeap.insert(myHandsArray[i]);
        

        expectedHandsArray[0] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        expectedHandsArray[1] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        expectedHandsArray[2] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
        expectedHandsArray[3] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        expectedHandsArray[4] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        expectedHandsArray[5] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        expectedHandsArray[6] = new Hands(new Card(13, 'C'), new Card(5, 'D'), new Card(13, 'S'), new Card(5, 'S'), new Card(13, 'D'));
        expectedHandsArray[7] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        expectedHandsArray[8] = new Hands(new Card(10, 'S'), new Card(8, 'C'), new Card(8, 'S'), new Card(10, 'H'), new Card(10, 'D'));
        expectedHandsArray[9] = new Hands(new Card(2, 'S'), new Card(2, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(2, 'H'));
        expectedHandsArray[10] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        expectedHandsArray[11] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        expectedHandsArray[12] = new Hands(new Card(6, 'S'), new Card(5, 'D'), new Card(6, 'C'), new Card(5, 'C'), new Card(5, 'H'));
        expectedHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(8, 'H'), new Card(9, 'H'));
        expectedHandsArray[14] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        expectedHandsArray[15] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        expectedHandsArray[16] = new Hands(new Card(4, 'C'), new Card(4, 'D'), new Card(4, 'S'), new Card(6, 'S'), new Card(4, 'H'));
        expectedHandsArray[17] = new Hands(new Card(3, 'H'), new Card(5, 'H'), new Card(3, 'S'), new Card(5, 'S'), new Card(5, 'D'));
        expectedHandsArray[18] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        expectedHandsArray[19] = new Hands(new Card(5, 'C'), new Card(8, 'D'), new Card(7, 'S'), new Card(6, 'S'), new Card(9, 'D'));
            
        // Action
        for(int i = 0; i < myMaxHeap.size; i++)
            passed &= assertEquals(expectedHandsArray[i], myMaxHeap.myHeap[i + 1]);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void CustomTestInsert1(){
        // Setup
        System.out.println("============CustomTestInsert1=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(10);
        Hands myHandsArray[] = new Hands[5];  
        Hands expectedHandsArray[] = new Hands[5];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H'));
        myHandsArray[2] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H'));
        
        for(int i = 0; i < 5; i++)
            myMaxHeap.insert(myHandsArray[i]);

        expectedHandsArray[0] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
        expectedHandsArray[1] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H'));
        expectedHandsArray[2] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H'));
        expectedHandsArray[3] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H'));
        expectedHandsArray[4] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));              

        // Action
        for(int i = 0; i < myMaxHeap.size; i++)
            passed &= assertEquals(expectedHandsArray[i], myMaxHeap.myHeap[i + 1]);


        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void CustomTestInsert2(){
        // Setup
        System.out.println("============CustomTestInsert2=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(5);
        Hands myHandsArray[] = new Hands[2];  
        Hands expectedHandsArray[] = new Hands[2];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));

        
        for(int i = 0; i < 2; i++)
            myMaxHeap.insert(myHandsArray[i]);
        

        expectedHandsArray[0] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        expectedHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
            
        // Action
        for(int i = 0; i < myMaxHeap.size; i++)
            passed &= assertEquals(expectedHandsArray[i], myMaxHeap.myHeap[i + 1]);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testGetSize1(){
        // Setup
        System.out.println("============testGetSize1=============");
        boolean passed = true;
        totalTestCount++;

        Random rn = new Random();
        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);

        int expected = rn.nextInt(10);
        for(int i = 0; i < expected; i++)
            myMaxHeap.insert(
                new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'))
            );

        // Action
        passed &= assertEquals(expected, myMaxHeap.getSize());

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testGetSize2(){
        // Setup
        System.out.println("============testGetSize2=============");
        boolean passed = true;
        totalTestCount++;

        Random rn = new Random();
        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);

        int expected = rn.nextInt(19);
        for(int i = 0; i < expected; i++)
            myMaxHeap.insert(
                new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'))
            );

        // Action
        passed &= assertEquals(expected, myMaxHeap.getSize());

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testRemoveMax1(){
        // Setup
        System.out.println("============testRemoveMax1=============");
        boolean passed = true;
        totalTestCount++;
        
        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);
        Hands myHandsArray[] = new Hands[15];  
        Hands expectedMaxHand[] = new Hands[3];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H'));
        myHandsArray[2] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H'));
        myHandsArray[5] = new Hands(new Card(6, 'S'), new Card(7, 'D'), new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'));
        myHandsArray[6] = new Hands(new Card(14, 'C'), new Card(14, 'D'), new Card(6, 'C'), new Card(14, 'S'), new Card(14, 'H'));
        myHandsArray[7] = new Hands(new Card(11, 'H'), new Card(11, 'D'), new Card(11, 'C'), new Card(5, 'H'), new Card(5, 'S'));
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'));
        myHandsArray[9] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(8, 'H'));
        myHandsArray[10] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(13, 'S'), new Card(14, 'S'));
        myHandsArray[11] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        myHandsArray[12] = new Hands(new Card(5, 'S'), new Card(10, 'D'), new Card(10, 'C'), new Card(5, 'C'), new Card(10, 'H'));
        myHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(4, 'H'), new Card(3, 'H'));
        myHandsArray[14] = new Hands(new Card(3, 'C'), new Card(5, 'D'), new Card(3, 'S'), new Card(5, 'S'), new Card(3, 'D'));
        
        for(int i = 0; i < 15; i++)
            myMaxHeap.insert(myHandsArray[i]);

        
        expectedMaxHand[0] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(13, 'S'), new Card(14, 'S'));        
        expectedMaxHand[1] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H'));        
        expectedMaxHand[2] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C'));
               
        // Action
        for(int i = 0; i < 3; i++)
        {
            passed &= assertEquals(expectedMaxHand[i], myMaxHeap.removeMax());            
        }
            
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testRemoveMax2(){
        // Setup
        System.out.println("============testRemoveMax2=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(20);
        Hands myHandsArray[] = new Hands[22];  
        Hands expectedMaxHand[] = new Hands[5];

        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        myHandsArray[6] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
        myHandsArray[7] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        myHandsArray[9] = new Hands(new Card(2, 'S'), new Card(2, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(2, 'H'));
        myHandsArray[10] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        myHandsArray[11] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        myHandsArray[12] = new Hands(new Card(6, 'S'), new Card(5, 'D'), new Card(6, 'C'), new Card(5, 'C'), new Card(5, 'H'));
        myHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(8, 'H'), new Card(9, 'H'));
        myHandsArray[14] = new Hands(new Card(13, 'C'), new Card(5, 'D'), new Card(13, 'S'), new Card(5, 'S'), new Card(13, 'D'));
        myHandsArray[15] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        myHandsArray[16] = new Hands(new Card(4, 'C'), new Card(4, 'D'), new Card(4, 'S'), new Card(6, 'S'), new Card(4, 'H'));
        myHandsArray[17] = new Hands(new Card(3, 'H'), new Card(5, 'H'), new Card(3, 'S'), new Card(5, 'S'), new Card(5, 'D'));
        myHandsArray[18] = new Hands(new Card(10, 'S'), new Card(8, 'C'), new Card(8, 'S'), new Card(10, 'H'), new Card(10, 'D'));
        myHandsArray[19] = new Hands(new Card(5, 'C'), new Card(8, 'D'), new Card(7, 'S'), new Card(6, 'S'), new Card(9, 'D'));
        myHandsArray[20] = new Hands(new Card(7, 'S'), new Card(7, 'D'), new Card(4, 'S'), new Card(4, 'D'), new Card(7, 'C'));
        myHandsArray[21] = new Hands(new Card(9, 'D'), new Card(10, 'D'), new Card(13, 'D'), new Card(11, 'D'), new Card(12, 'D'));


        for(int i = 0; i < 20; i++)
        myMaxHeap.insert(myHandsArray[i]);


        expectedMaxHand[0] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        expectedMaxHand[1] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        expectedMaxHand[2] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        expectedMaxHand[3] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        expectedMaxHand[4] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
                
        // Action
        for(int i = 0; i < 5; i++)
        {
            passed &= assertEquals(expectedMaxHand[i], myMaxHeap.removeMax());            
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void CustomTestRemoveMax1(){
        // Setup
        System.out.println("============CustomTestRemoveMax1=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(1);
        Hands myHandsArray[] = new Hands[1];  
        Hands expectedMaxHand[] = new Hands[1];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(2, 'H'), new Card(2, 'H'), new Card(6, 'H'), new Card(6, 'H'), new Card(6, 'H'));
        
        for(int i = 0; i < 1; i++)
            myMaxHeap.insert(myHandsArray[i]);

        
        expectedMaxHand[0] = new Hands(new Card(2, 'H'), new Card(2, 'H'), new Card(6, 'H'), new Card(6, 'H'), new Card(6, 'H'));
               
        // Action
        for(int i = 0; i < 1; i++)
        {
            passed &= assertEquals(expectedMaxHand[i], myMaxHeap.removeMax());            
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void CustomTestRemoveMax2(){
        // Setup
        System.out.println("============CustomTestRemoveMax2=============");
        boolean passed = true;
        totalTestCount++;

        HandsMaxHeap myMaxHeap = new HandsMaxHeap(5);
        Hands myHandsArray[] = new Hands[5];  
        Hands expectedMaxHand[] = new Hands[5];

        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(7, 'S'), new Card(8, 'D'), new Card(9, 'C'), new Card(7, 'H'), new Card(2, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(14, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));


        for(int i = 0; i < 5; i++)
        myMaxHeap.insert(myHandsArray[i]);


        expectedMaxHand[0] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        expectedMaxHand[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        expectedMaxHand[2] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        expectedMaxHand[3] = new Hands(new Card(9, 'C'), new Card(14, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        expectedMaxHand[4] = new Hands(new Card(7, 'S'), new Card(8, 'D'), new Card(9, 'C'), new Card(7, 'H'), new Card(2, 'H'));
                
        // Action
        for(int i = 0; i < 5; i++)
        {
            passed &= assertEquals(expectedMaxHand[i], myMaxHeap.removeMax());            
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }


    private static void testHeapSort1(){
        // Setup
        System.out.println("============testHeapSort1=============");
        boolean passed = true;
        totalTestCount++;

        Hands myHandsArray[] = new Hands[10];  
        Hands expectedHandsArray[] = new Hands[10];
        
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H')); // FH 6
        myHandsArray[1] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H')); // S HQ
        myHandsArray[2] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C')); // SF C8
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D')); // FH A
        myHandsArray[4] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H')); // FK 10
        myHandsArray[5] = new Hands(new Card(6, 'S'), new Card(7, 'D'), new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H')); // SF H10
        myHandsArray[6] = new Hands(new Card(14, 'C'), new Card(14, 'D'), new Card(6, 'C'), new Card(14, 'S'), new Card(14, 'H')); // FK A
        myHandsArray[7] = new Hands(new Card(11, 'H'), new Card(11, 'D'), new Card(11, 'C'), new Card(5, 'H'), new Card(5, 'S')); // FH J
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H')); // SF HQ
        myHandsArray[9] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(8, 'H'));  // FH 8
        
        expectedHandsArray[0] = new Hands(new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H'), new Card(11, 'H'), new Card(12, 'H')); // SF HQ
        expectedHandsArray[1] = new Hands(new Card(4, 'C'), new Card(5, 'C'), new Card(6, 'C'), new Card(7, 'C'), new Card(8, 'C')); // SF C8
        expectedHandsArray[2] = new Hands(new Card(14, 'C'), new Card(14, 'D'), new Card(6, 'C'), new Card(14, 'S'), new Card(14, 'H')); // FK A
        expectedHandsArray[3] = new Hands(new Card(10, 'C'), new Card(11, 'D'), new Card(10, 'D'), new Card(10, 'S'), new Card(10, 'H')); // FK 10
        expectedHandsArray[4] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D')); // FH A
        expectedHandsArray[5] = new Hands(new Card(11, 'H'), new Card(11, 'D'), new Card(11, 'C'), new Card(5, 'H'), new Card(5, 'S')); // FH J
        expectedHandsArray[6] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(8, 'H'));  // FH 8
        expectedHandsArray[7] = new Hands(new Card(2, 'C'), new Card(2, 'D'), new Card(6, 'C'), new Card(6, 'S'), new Card(6, 'H')); // FH 6
        expectedHandsArray[8] = new Hands(new Card(8, 'D'), new Card(9, 'D'), new Card(10, 'H'), new Card(11, 'D'), new Card(12, 'H')); // S HQ
        expectedHandsArray[9] = new Hands(new Card(6, 'S'), new Card(7, 'D'), new Card(8, 'H'), new Card(9, 'H'), new Card(10, 'H')); // S H10
        
        // Action
        heapSort(myHandsArray);

        for(int i = 0; i < 10; i++)
        {            
            passed &= assertEquals(expectedHandsArray[i], myHandsArray[i]);            
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testHeapSort2(){
        // Setup
        System.out.println("============testHeapSort2=============");
        boolean passed = true;
        totalTestCount++;

        Hands myHandsArray[] = new Hands[20];  
        Hands expectedHandsArray[] = new Hands[20];
       
        // [Scott] Need initialization of all hands
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        myHandsArray[6] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
        myHandsArray[7] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[8] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        myHandsArray[9] = new Hands(new Card(2, 'S'), new Card(2, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(2, 'H'));
        myHandsArray[10] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        myHandsArray[11] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        myHandsArray[12] = new Hands(new Card(6, 'S'), new Card(5, 'D'), new Card(6, 'C'), new Card(5, 'C'), new Card(5, 'H'));
        myHandsArray[13] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(8, 'H'), new Card(9, 'H'));
        myHandsArray[14] = new Hands(new Card(13, 'C'), new Card(5, 'D'), new Card(13, 'S'), new Card(5, 'S'), new Card(13, 'D'));
        myHandsArray[15] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        myHandsArray[16] = new Hands(new Card(4, 'C'), new Card(4, 'D'), new Card(4, 'S'), new Card(6, 'S'), new Card(4, 'H'));
        myHandsArray[17] = new Hands(new Card(3, 'H'), new Card(5, 'H'), new Card(3, 'S'), new Card(5, 'S'), new Card(5, 'D'));
        myHandsArray[18] = new Hands(new Card(10, 'S'), new Card(8, 'C'), new Card(8, 'S'), new Card(10, 'H'), new Card(10, 'D'));
        myHandsArray[19] = new Hands(new Card(5, 'C'), new Card(8, 'D'), new Card(7, 'S'), new Card(6, 'S'), new Card(9, 'D'));
        
        expectedHandsArray[0] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        expectedHandsArray[1] = new Hands(new Card(10, 'S'), new Card(11, 'S'), new Card(12, 'S'), new Card(8, 'S'), new Card(9, 'S'));
        expectedHandsArray[2] = new Hands(new Card(8, 'H'), new Card(10, 'H'), new Card(9, 'H'), new Card(11, 'H'), new Card(7, 'H'));
        expectedHandsArray[3] = new Hands(new Card(7, 'C'), new Card(8, 'C'), new Card(10, 'C'), new Card(11, 'C'), new Card(9, 'C'));
        expectedHandsArray[4] = new Hands(new Card(11, 'C'), new Card(12, 'D'), new Card(12, 'C'), new Card(12, 'S'), new Card(12, 'H'));
        expectedHandsArray[5] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        expectedHandsArray[6] = new Hands(new Card(4, 'C'), new Card(4, 'D'), new Card(4, 'S'), new Card(6, 'S'), new Card(4, 'H'));
        expectedHandsArray[7] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        expectedHandsArray[8] = new Hands(new Card(13, 'C'), new Card(5, 'D'), new Card(13, 'S'), new Card(5, 'S'), new Card(13, 'D'));
        expectedHandsArray[9] = new Hands(new Card(12, 'D'), new Card(12, 'C'), new Card(9, 'C'), new Card(12, 'S'), new Card(9, 'H'));
        expectedHandsArray[10] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        expectedHandsArray[11] = new Hands(new Card(10, 'S'), new Card(8, 'C'), new Card(8, 'S'), new Card(10, 'H'), new Card(10, 'D'));
        expectedHandsArray[12] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        expectedHandsArray[13] = new Hands(new Card(3, 'H'), new Card(5, 'H'), new Card(3, 'S'), new Card(5, 'S'), new Card(5, 'D'));
        expectedHandsArray[14] = new Hands(new Card(6, 'S'), new Card(5, 'D'), new Card(6, 'C'), new Card(5, 'C'), new Card(5, 'H'));
        expectedHandsArray[15] = new Hands(new Card(2, 'S'), new Card(2, 'D'), new Card(7, 'S'), new Card(7, 'C'), new Card(2, 'H'));
        expectedHandsArray[16] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        expectedHandsArray[17] = new Hands(new Card(7, 'S'), new Card(6, 'C'), new Card(5, 'C'), new Card(8, 'H'), new Card(9, 'H'));
        expectedHandsArray[18] = new Hands(new Card(5, 'C'), new Card(8, 'D'), new Card(7, 'S'), new Card(6, 'S'), new Card(9, 'D'));
        expectedHandsArray[19] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
           
            
            
        
        // Action
        heapSort(myHandsArray);

        for(int i = 0; i < 20; i++)
        {
            passed &= assertEquals(expectedHandsArray[i], myHandsArray[i]);            
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }


    
    private static boolean assertEquals(Hands a, Hands b)
    {
        if(!a.isMyHandEqual(b))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: ");
            a.printMyHand();
            System.out.printf(", Actual: ");
            b.printMyHand();
            System.out.printf("\n");
            return false;
        }

        return true;
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
