// This is the replacement module for MaxHeap (to hide away Lab 1 solution)

public class HandsBST {
    
    private HandsBSTNode root;

    public HandsBST()
    {
        root = null;
    }

    public boolean isEmpty()
    {
        return (root == null);
    }

    public void insert(Hands thisHand)
    {
        root = insert(thisHand, root);
    }
    
    private HandsBSTNode insert(Hands thisHand, HandsBSTNode thisNode)
    {
        if(thisNode == null)
        {
            thisNode = new HandsBSTNode(thisHand);
            thisHand.printMyHand();
            System.out.println(" Inserted new!");
            return thisNode;
        }
        
        if(thisHand.isMyHandSmaller(thisNode.myHand)) 
        {
            thisNode.left = insert(thisHand, thisNode.left);
            System.out.println("Left");
        }
            
        else if(thisHand.isMyHandLarger(thisNode.myHand))
        {
            thisNode.right = insert(thisHand, thisNode.right);
            System.out.println("Right");
        }
            
        else;
            // node already in tree, do nothing;
        return thisNode;
    }

    public void remove(Hands thisHand)
    {
        root = remove(thisHand, root);
    }

    private HandsBSTNode remove(Hands thisHand, HandsBSTNode thisNode)
    {
        if(thisNode == null)
		    return thisNode;
        
        if(thisHand.isMyHandSmaller(thisNode.myHand)) 
            thisNode.left = remove(thisHand, thisNode.left);
        else if(thisHand.isMyHandLarger(thisNode.myHand))
            thisNode.right = remove(thisHand, thisNode.right);
        else
        {
            if (thisNode.left == null)
                return thisNode.right;
            else if (thisNode.right == null)
                return thisNode.left;

            thisNode.myHand = getMinHand(thisNode.right);

            thisNode.right = remove(thisNode.myHand, thisNode.right);
        }
        
        return thisNode;
    }

    private Hands getMinHand(HandsBSTNode thisNode)
    {
        if(root == null) return new Hands();

        HandsBSTNode pointer = root;
        while(root.left != null)
        {
            pointer = pointer.left;
        }

        return pointer.myHand;
    }
    
    public Hands getMinHand()
    {
        return getMinHand(root);
    }

    public Hands getMaxHand()
    {
        if(root == null) return new Hands();

        HandsBSTNode pointer = root;
        while(pointer.right != null)
        {
            pointer = pointer.right;
        }

        return pointer.myHand;
    }

    public Hands removeMaxHand()
    {
        Hands maxHand = getMaxHand();
        remove(maxHand);
        return maxHand;
    }

    public void printBST()
    {
        System.out.println("====================");
        printBST(root);
        System.out.println("====================");
    }

    private void printBST(HandsBSTNode thisNode)
    {
        if(thisNode == null) 
        {
            return;
        }

        printBST(thisNode.left);
        thisNode.myHand.printMyHand();
        System.out.println();
        printBST(thisNode.right);
    }




    public static void main(String[] args)
    {
        testInsert();
        testRemoveMax();
    }   


    private static void testInsert(){
        // Setup
        System.out.println("============testInsert=============");
        
        HandsBST myBST = new HandsBST();
        Hands myHandsArray[] = new Hands[15];  
        
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
            myBST.insert(myHandsArray[i]);

        // Action
        myBST.printBST();
    }

    private static void testRemoveMax(){
        // Setup
        System.out.println("============testRemoveMax=============");
        
        HandsBST myBST = new HandsBST();
        Hands myHandsArray[] = new Hands[15];  
        Hands removeTarget;
        
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
            myBST.insert(myHandsArray[i]);

        myBST.printBST();

        // Action
        for(int i = 0; i < 5; i++)
        {
            removeTarget = myBST.removeMaxHand();
            System.out.printf("Removal Target: ");
            removeTarget.printMyHand();
            System.out.println();            
            myBST.printBST();
        }        
        
    }

}

