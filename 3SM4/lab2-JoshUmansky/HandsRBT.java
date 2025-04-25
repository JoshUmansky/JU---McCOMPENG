import java.util.Vector;

public class HandsRBT {
    
    private HandsRBTNode root;
    private static final boolean RED = false;
    private static final boolean BLACK = true;

    // This inline class is typically required in certain post-deletion correction cases
    private static class NilNode extends HandsRBTNode
    {
        private NilNode()
        {
            super(null);
            this.colour = BLACK;
        }
    }

    // Constructor: Instantiates an empty RBT
    public HandsRBT()
    {
        root = null;
    }
  
    private HandsRBTNode getRoot() {return root;}
    
    public boolean isEmpty() { return root==null; }
    
    private boolean isBlack(HandsRBTNode thisNode)
    {
        return (thisNode == null || thisNode.colour == BLACK);
    }

    // Required helper function for fixing RBT properties
    private HandsRBTNode getSibling(HandsRBTNode thisNode)
    {
        HandsRBTNode parent = thisNode.parent;

        if(thisNode == parent.left)         return parent.right;
        else if(thisNode == parent.right)   return parent.left;
        else
            throw new IllegalStateException("Parent and Children are Unrelated.");            
    }    

    // Required helper function for fixing RBT properties
    private HandsRBTNode getUncle(HandsRBTNode parent)
    {
        HandsRBTNode grandparent = parent.parent;
        if(grandparent.left == parent)
            return grandparent.right;
        else if(grandparent.right == parent)
            return grandparent.left;
        else
            throw new IllegalStateException("Provided Parent is NOT a child of the Grandparent");
    }


    // Activity 1 - Design Rotation Algorithm
    /////////////////////////////////////////////////

    //rotateLeft performs the Leftward Node Rotation
    //Time Complexity: O(1)
    //Space Complexity: O(1)
    private void rotateLeft(HandsRBTNode thisNode)
    {
        HandsRBTNode y = thisNode.right;
        thisNode.right = y.left;
        if(y.left != null)
            y.left.parent = thisNode;
        y.parent = thisNode.parent;
        if(thisNode.parent == null)
            root = y;
        else if(thisNode == thisNode.parent.left)
            thisNode.parent.left = y;
        else
            thisNode.parent.right = y;
        y.left = thisNode;
        thisNode.parent = y;



    }

    //rotateRight performs the Rightward Node Rotation
    //Time Complexity: O(1)
    //Space Complexity: O(1)
    private void rotateRight(HandsRBTNode thisNode)
    {
        HandsRBTNode y = thisNode.left;
        thisNode.left = y.right;
        if(y.right != null)
            y.right.parent = thisNode;
        y.parent = thisNode.parent;
        if(thisNode.parent == null)
            root = y;
        else if(thisNode == thisNode.parent.right)
            thisNode.parent.right = y;
        else
            thisNode.parent.left = y;
        y.right = thisNode;
        thisNode.parent = y;
    }    

    // Search for a specific Hands object in the RBT
    //Time Complexity: O(log n)
    //Space Complexity: O(1)
    public HandsRBTNode findNode(Hands thisHand)
    {
        HandsRBTNode node = root;      

        while(node != null)
        {

            if(node.myHand.isMyHandLarger(thisHand)) 
            {
                node = node.left;
            }
            else if(node.myHand.isMyHandSmaller(thisHand)) 
            {
                node = node.right;
            }
            else
            {
                return node;
            }
        }
        return null;        
    }

    // Activity 2 - Design Insertion and Red-Violation Correction
    /////////////////////////////////////////////////////////////////////////
    
    //Insert into RBT
    //Time Complexity: O(log n)
    //Space Complexity: O(1)
    public void insert(Hands thisHand)
    {
        HandsRBTNode newNode = new HandsRBTNode(thisHand);
        HandsRBTNode parent = null;
        HandsRBTNode node = root;
        //Step 1: Traverse from the root to find the insertion point as in a BST
        while(node != null)
        {
            parent = node;
            if(node.myHand.isMyHandLarger(thisHand))
            {
                node = node.left;
            }
            else if(node.myHand.isMyHandSmaller(thisHand))
            {
                node = node.right;
            }
            else // If thisHand is already in the RBT, exit 
            {
                return;
            }
        }
        // Else go to step 2
        // Step 2: Insert the new node
        newNode.parent = parent;
        if(parent == null) //Root
        {
            root = newNode;
            root.colour = BLACK;
        }
        //Else find right spot and colour it RED
        else if(parent.myHand.isMyHandLarger(thisHand))
        {
            parent.left = newNode;
        }
        else
        {
            parent.right = newNode;
        }

        newNode.left = null;
        newNode.right = null;
        newNode.colour = RED;

        // If a red violation occurs fix it by invoking the private method fixRedViolation
        // Else exit (Handled inside of fixRedViolation)
        fixRedViolation(newNode);
    }
    
    //Fix the Red Violation
    //Time Complexity: O(log n)
    //Space Complexity: O(1)
    private void fixRedViolation(HandsRBTNode thisNode)
    {
        while (thisNode != root && thisNode.parent.colour == RED) {
            HandsRBTNode parent = thisNode.parent;
            HandsRBTNode grandparent = parent.parent;
            //Left child of grandparent case
            if (parent == grandparent.left) {
                HandsRBTNode uncle = grandparent.right;
                if (uncle != null && uncle.colour == RED) {
                    // Case 1: Red Uncle
                    parent.colour = BLACK;
                    uncle.colour = BLACK;
                    grandparent.colour = RED;
                    thisNode = grandparent;
                } else {
                    if (thisNode == parent.right) {
                        // Case 3: Black Uncle & Inner Grandchild
                        rotateLeft(parent);
                        thisNode = parent;
                        parent = thisNode.parent;
                    }
                    // Case 2: Black Uncle & Outer Grandchild
                    rotateRight(grandparent);
                    parent.colour = BLACK;
                    grandparent.colour = RED;
                    break;
                }
            } else { //Right child of grandparent case
                HandsRBTNode uncle = grandparent.left;
                if (uncle != null && uncle.colour == RED) {
                    // Case 1: Red Uncle
                    parent.colour = BLACK;
                    uncle.colour = BLACK;
                    grandparent.colour = RED;
                    thisNode = grandparent;
                } else {
                    if (thisNode == parent.left) {
                        // Case 3: Black Uncle & Inner Grandchild
                        rotateRight(parent);
                        thisNode = parent;
                        parent = thisNode.parent;
                    }
                    // Case 2: Black Uncle & Outer Grandchild
                    rotateLeft(grandparent);
                    parent.colour = BLACK;
                    grandparent.colour = RED;
                    break;
                }
            }
        }
        root.colour = BLACK; // Ensure the root is always black    
    }

    // Activity 3 - Delete All Hands from RBT with cards from the consumed hand
    /////////////////////////////////////////////////////////////////////////

    // Invoke this method to delete all the Nodes with Hands containing any card from the consumedHand
    // This method invokes deleteHandsWithCard() for all 5 cards in the consumedHand
    public void deleteInvalidHands(Hands consumedHand)
    {
        for (int i = 0; i < 5; i++) {
            deleteHandsWithCard(consumedHand.getCard(i));
        }
    }

    // Delete all Hands from RBT with the specific card
    //Time Complexity: O(n)
    //Space Complexity: O(n)
    private void deleteHandsWithCard(Card thisCard)
    {    
        Vector<Hands> handsToDelete = new Vector<>();
        traverseRBTforVec(root, thisCard, handsToDelete); // Recursively collect hands to delete, this could have been done manually but recursion was more interesting

        for (Hands hand : handsToDelete) {
            delete(hand);
        }
    
    }    
    
    //Helper function to enable recursion for deleteHandsWithCard
    //Time Complexity: O(n)
    //Space Complexity: O(n)
    private void traverseRBTforVec(HandsRBTNode currentNode, Card targetCard, Vector<Hands> handsToDelete) {
        //Base
        if (currentNode == null) {
            return;
        }
        // Traverse the left subtree
        traverseRBTforVec(currentNode.left, targetCard, handsToDelete);
    
        // Check current node for the target card
        if (currentNode.myHand.hasCard(targetCard)) {
            handsToDelete.add(currentNode.myHand);
        }    
        // Traverse the right subtree
        traverseRBTforVec(currentNode.right, targetCard, handsToDelete);
    }
    
    // Deletion Method 1 - promote the smallest of the right tree
    private HandsRBTNode findMin(HandsRBTNode thisNode)
    {
        while(thisNode.left != null)    
            thisNode = thisNode.left;

        return thisNode;
    }

    // Deletion Method 2 - promote the largest of the left tree
    private HandsRBTNode findMax(HandsRBTNode thisNode)
    {
        while(thisNode.right != null)    
            thisNode = thisNode.right;

        return thisNode;
    }

    // Helper Function 
    private void replaceParentsChild(HandsRBTNode parent, HandsRBTNode oldChild, HandsRBTNode newChild)
    {
        if(parent == null)  root = newChild;
        else if(parent.left == oldChild) parent.left = newChild;
        else if(parent.right == oldChild) parent.right = newChild;
        else throw new IllegalStateException("Parents and Children are unrelated...");

        if(newChild != null) newChild.parent = parent;
    }
    
    public void delete(Hands thisHand)
    {        
        HandsRBTNode node = root;

        // First, search for node
        while( node != null )
        {
            if(node.myHand.isMyHandLarger(thisHand))
            {
                node = node.left;
            }
            else if(node.myHand.isMyHandSmaller(thisHand))
            {
                node = node.right;
            }
            else 
            {                
                break;  // equal
            }
        }
        // If not found, don't do anything
        if(node == null) return;

        // If node found, take the respective action
        HandsRBTNode promotedNode;
        boolean deletedNodeColour;

        //  Either the node has zero or one child
        if(node.left == null || node.right == null)
        {
            promotedNode = deleteNodeWithMaxOneChild(node);
            deletedNodeColour = node.colour;
        }
        //  Or the node has two children
        else
        {
            HandsRBTNode successorNode = findMin(node.right);   // OR, = findMax(node.left);            
            node.myHand = successorNode.myHand;

            promotedNode = deleteNodeWithMaxOneChild(successorNode);
            deletedNodeColour = successorNode.colour;
        }

        // Finally, fix the RBT
        if(deletedNodeColour == BLACK)
        {
            PostDeleteRBTCorrection(promotedNode);

            if(promotedNode.getClass() == NilNode.class)
            {
                replaceParentsChild(promotedNode.parent, promotedNode, null);
            }
        }
    }

    private HandsRBTNode deleteNodeWithMaxOneChild(HandsRBTNode thisNode)
    {
        // Only left child
        if(thisNode.left != null)
        {
            replaceParentsChild(thisNode.parent, thisNode, thisNode.left);
            return thisNode.left;
        }
        // Only right child
        else if(thisNode.right != null)
        {
            replaceParentsChild(thisNode.parent, thisNode, thisNode.right);
            return thisNode.right;
        }
        // No child
        else
        {
            HandsRBTNode newChild = null;
            if(thisNode.colour == BLACK)
                newChild = new NilNode();
            
            replaceParentsChild(thisNode.parent, thisNode, newChild);
            return newChild;
        }


    }

    // 6 Cases to handle.  4 in this method, and 2 in its helper function
    private void PostDeleteRBTCorrection(HandsRBTNode thisNode)
    {
        // Case 1 - thisNode is root.
        if(thisNode == root) return;  
        // may want to optionally colour the root node BLACK

        HandsRBTNode sibling = getSibling(thisNode);

        // Case 2 - Red Sibling
        if(sibling.colour == RED)
        {
            handleRedSibling(thisNode, sibling);
            sibling = getSibling(thisNode); // keep track of the new sibling for subsequent cases
        }

        // Case 3 + 4 - Black sibling with two black children
        if(isBlack(sibling.left) && isBlack(sibling.right))
        {
            sibling.colour = RED;

            // Case 3 - Black sibling with two black children + red parent
            if(thisNode.parent.colour == RED)
            {
                thisNode.parent.colour = BLACK;
            }
            // Case 4 - Black sibling with two black children + black parent
            else
            {
                PostDeleteRBTCorrection(thisNode.parent);
                // recursively correct the colour upwards
            }
        }

        // Case 5 + 6 - Black Sibling with at least One Child
        else
        {
            handleBlackSiblingWithMinOneRedChild(thisNode, sibling);
        }
    }

    private void handleRedSibling(HandsRBTNode thisNode, HandsRBTNode sibling)
    {
        // First, recolour
        sibling.colour = BLACK;
        thisNode.parent.colour = RED;

        // Then, rotate
        if(thisNode == thisNode.parent.left)
        {
            rotateLeft(thisNode.parent);
        }
        else
        {
            rotateRight(thisNode.parent);
        }
    }

    private void handleBlackSiblingWithMinOneRedChild(HandsRBTNode thisNode, HandsRBTNode sibling)
    {
        boolean isLeftChild = (thisNode == thisNode.parent.left);

        // Case 5: Black Sibling with min one red child + outer nephew is black
        if(isLeftChild && isBlack(sibling.right))
        {
            sibling.left.colour = BLACK;
            sibling.colour = RED;
            rotateRight(sibling);
            sibling = thisNode.parent.right;
        }
        else if(!isLeftChild && isBlack(sibling.left))
        {
            sibling.right.colour = BLACK;
            sibling.colour = RED;
            rotateLeft(sibling);
            sibling = thisNode.parent.left;
        }

        // Case 6: Black Sibling with min one red child + outer nephew is red
        sibling.colour = thisNode.parent.colour;
        thisNode.parent.colour = BLACK;

        if(isLeftChild)
        {
            sibling.right.colour = BLACK;
            rotateLeft(thisNode.parent);
        }
        else
        {
            sibling.left.colour = BLACK;
            rotateRight(thisNode.parent);
        }
    }
    
    public void printRBT()
    {        
        // First, get the height of the tree
        int height = getHeight(root);   

        // Then, print using leveled order 
        for(int i = 1; i <= height; i++)
        {
            for(int j = 0; j < (height - i); j++)
            {
                System.out.print("  ");
            }
            printCurrentLevel(root, i);
            System.out.println(">>");
        }
            
    }

    private int getHeight(HandsRBTNode thisNode)
    {
        if(thisNode == null) return 0;
        
        int leftHeight = getHeight( thisNode.left );
        int rightHeight = getHeight( thisNode.right );

        if(leftHeight > rightHeight) 
            return (leftHeight + 1);
        else 
            return (rightHeight + 1);
    }

    private void printCurrentLevel(HandsRBTNode thisNode, int level)
    {
        if(thisNode == null) 
        {
            System.out.print("==NIL==");
            return;
        }

        if(level == 1) 
        {
            printNode(thisNode);
        }
        else if(level > 1)
        {
            printCurrentLevel(thisNode.left, level - 1);
            System.out.print("\t");
            printCurrentLevel(thisNode.right, level - 1);
            System.out.print("\t");
        }
    }

    private void printNode(HandsRBTNode thisNode)
    {
        if(thisNode.colour == BLACK)
            System.out.printf("(B)");
        else
            System.out.printf("(R)");
        thisNode.myHand.printMyHand();
        System.out.printf(" ");
    }



    
    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {
        testRotateLeft();        
        testRotateRight();

        testInsertCase1NR();
        testInsertCase1BP();        
        testInsertCase2A();
        testInsertCase2B();
        testInsertCase3();
        testInsertCase4L();
        testInsertCase4R();
        testInsertCase5L();
        testInsertCase5R();
        
        testCustomInsertCase4();
        testCustomInsertCase5();

        testDeleteHandsWithCard();
        testCustomDeleteHandsWithCard();

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

    private static void testRotateLeft()
    {
        // Setup
        System.out.println("============testRotateLeft=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        Hands myHandsArray[] = new Hands[6];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        
        HandsRBTNode nodeArray[] = new HandsRBTNode[6];
        nodeArray[0] = new HandsRBTNode(myHandsArray[0]);
        nodeArray[1] = new HandsRBTNode(myHandsArray[1]);
        nodeArray[2] = new HandsRBTNode(myHandsArray[2]);
        nodeArray[3] = new HandsRBTNode(myHandsArray[3]);
        nodeArray[4] = new HandsRBTNode(myHandsArray[4]);
        nodeArray[5] = new HandsRBTNode(myHandsArray[5]);

        HandsRBTNode root = nodeArray[0];
        nodeArray[0].parent = null;
        nodeArray[0].left = nodeArray[1];
        nodeArray[0].right = null;

        nodeArray[1].parent = nodeArray[0];
        nodeArray[1].left = nodeArray[2];
        nodeArray[1].right = nodeArray[3];

        nodeArray[2].parent = nodeArray[1];
        nodeArray[2].left = null;
        nodeArray[2].right = null;

        nodeArray[3].parent = nodeArray[1];
        nodeArray[3].left = nodeArray[4];;
        nodeArray[3].right = nodeArray[5];

        nodeArray[4].parent = nodeArray[3];
        nodeArray[4].left = null;
        nodeArray[4].right = null;

        nodeArray[5].parent = nodeArray[3];
        nodeArray[5].left = null;
        nodeArray[5].right = null;

        HandsRBT dummyRBT = new HandsRBT();

        dummyRBT.rotateLeft(root.left);

        System.out.println(">> Test Root (Not Rotated)");
        passed &= assertEquals(root.myHand, nodeArray[0].myHand);        
        System.out.println(">> Test Rotated SubRoot (root.left)");
        passed &= assertEquals(root.left.myHand, nodeArray[3].myHand);
        System.out.println(">> Test Rotated Left Child");
        passed &= assertEquals(root.left.left.myHand, nodeArray[1].myHand);
        System.out.println(">> Test Rotated Right Child");
        passed &= assertEquals(root.left.right.myHand, nodeArray[5].myHand);
        System.out.println(">> Test Rotated Left Grandchild of Left Child");
        passed &= assertEquals(root.left.left.left.myHand, nodeArray[2].myHand);
        System.out.println(">> Test Rotated Right Grandchild of Left Child");
        passed &= assertEquals(root.left.left.right.myHand, nodeArray[4].myHand);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testRotateRight()
    {
        // Setup
        System.out.println("============testRotateRight=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        Hands myHandsArray[] = new Hands[6];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'C'), new Card(11, 'C'), new Card(12, 'D'), new Card(10, 'H'), new Card(8, 'H'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(14, 'H'), new Card(14, 'D'), new Card(10, 'C'), new Card(10, 'D'));
        myHandsArray[4] = new Hands(new Card(14, 'D'), new Card(12, 'D'), new Card(10, 'D'), new Card(11, 'D'), new Card(13, 'D'));
        myHandsArray[5] = new Hands(new Card(10, 'S'), new Card(10, 'D'), new Card(8, 'H'), new Card(10, 'H'), new Card(8, 'S'));
        
        HandsRBTNode nodeArray[] = new HandsRBTNode[6];
        nodeArray[0] = new HandsRBTNode(myHandsArray[0]);
        nodeArray[1] = new HandsRBTNode(myHandsArray[1]);
        nodeArray[2] = new HandsRBTNode(myHandsArray[2]);
        nodeArray[3] = new HandsRBTNode(myHandsArray[3]);
        nodeArray[4] = new HandsRBTNode(myHandsArray[4]);
        nodeArray[5] = new HandsRBTNode(myHandsArray[5]);

        HandsRBTNode root = nodeArray[0];
        nodeArray[0].parent = null;
        nodeArray[0].left = nodeArray[1];
        nodeArray[0].right = null;

        nodeArray[1].parent = nodeArray[0];
        nodeArray[1].left = nodeArray[2];
        nodeArray[1].right = nodeArray[3];

        nodeArray[2].parent = nodeArray[1];
        nodeArray[2].left = nodeArray[4];
        nodeArray[2].right = nodeArray[5];

        nodeArray[3].parent = nodeArray[1];
        nodeArray[3].left = null;
        nodeArray[3].right = null;

        nodeArray[4].parent = nodeArray[2];
        nodeArray[4].left = null;
        nodeArray[4].right = null;

        nodeArray[5].parent = nodeArray[2];
        nodeArray[5].left = null;
        nodeArray[5].right = null;

        HandsRBT dummyRBT = new HandsRBT();

        dummyRBT.rotateRight(root.left);

        System.out.println(">> Test Root (Not Rotated)");
        passed &= assertEquals(root.myHand, nodeArray[0].myHand);        
        System.out.println(">> Test Rotated SubRoot (root.left)");
        passed &= assertEquals(root.left.myHand, nodeArray[2].myHand);
        System.out.println(">> Test Rotated Left Child");
        passed &= assertEquals(root.left.left.myHand, nodeArray[4].myHand);
        System.out.println(">> Test Rotated Right Child");
        passed &= assertEquals(root.left.right.myHand, nodeArray[1].myHand);
        System.out.println(">> Test Rotated Left Grandchild of Right Child");
        passed &= assertEquals(root.left.right.left.myHand, nodeArray[5].myHand);
        System.out.println(">> Test Rotated Right Grandchild of Right Child");
        passed &= assertEquals(root.left.right.right.myHand, nodeArray[3].myHand);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }


    private static void testInsertCase1NR()
    {
        // Setup
        System.out.println("============testInsertCase1NR=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands thisHand = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        testRBT.insert(thisHand);

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Node Insertion");
        passed &= assertEquals(testRoot.myHand, thisHand);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right, null);              

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase1BP()
    {
        // Setup
        System.out.println("============testInsertCase1BP=============");
        boolean passed = true;
        totalTestCount++;

        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[3];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED); 
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left, null);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null);  

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase2A()
    {
        // Setup
        System.out.println("============testInsertCase2A=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[2];
        myHandsArray[0] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[1]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right, null);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED); 
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase2B()
    {
        // Setup
        System.out.println("============testInsertCase2B=============");
        boolean passed = true;
        totalTestCount++;
        
        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[2];
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left, null);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null);   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase3()
    {
        // Setup
        System.out.println("============testInsertCase3=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[4];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[1]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[3]);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right, null); 

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase4L()
    {
        // Setup
        System.out.println("============testInsertCase4L=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(13, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(13, 'H'), new Card(13, 'H'));
        myHandsArray[4] = new Hands(new Card(14, 'S'), new Card(6, 'D'), new Card(6, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3
        testRBT.insert(myHandsArray[4]); // this will trigger Case 4L

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);         
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[3]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[1]); 
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);        
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase4R()
    {
        // Setup
        System.out.println("============testInsertCase4R=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(7, 'S'), new Card(7, 'D'), new Card(7, 'H'), new Card(7, 'C'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 4R

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);         
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[3]); 
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);        
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase5L()
    {
        // Setup
        System.out.println("============testInsertCase5L=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(14, 'S'), new Card(12, 'D'), new Card(12, 'C'), new Card(14, 'H'), new Card(14, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(6, 'D'), new Card(6, 'C'), new Card(12, 'H'), new Card(12, 'H'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]); // this will trigger Case 3
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5L

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);         
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[4]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[1]); 
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);        
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testInsertCase5R()
    {
        // Setup
        System.out.println("============testInsertCase5R=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(11, 'S'), new Card(13, 'S'), new Card(14, 'S'), new Card(10, 'S'));
        
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5R

        HandsRBTNode testRoot = testRBT.getRoot();
        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);        
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);         
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);  
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK); 
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[4]); 
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);        
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testCustomInsertCase4()
    {
        // Setup
        System.out.println("============testCustomInsertCase4=============");
        boolean passed = true;
        totalTestCount++;
        
        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(10, 'S'), new Card(9, 'D'), new Card(10, 'C'), new Card(10, 'H'), new Card(9, 'H'));
        myHandsArray[1] = new Hands(new Card(10, 'D'), new Card(10, 'S'), new Card(9, 'H'), new Card(10, 'C'), new Card(10, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'H'), new Card(8, 'D'), new Card(7, 'C'), new Card(10, 'H'), new Card(6, 'S'));
        myHandsArray[3] = new Hands(new Card(11, 'S'), new Card(11, 'D'), new Card(11, 'C'), new Card(11, 'H'), new Card(10, 'H'));
        myHandsArray[4] = new Hands(new Card(13, 'S'), new Card(12, 'S'), new Card(14, 'S'), new Card(11, 'S'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testCustomInsertCase5()
    {
        // Setup
        System.out.println("============testCustomInsertCase5=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(10, 'S'), new Card(9, 'D'), new Card(10, 'C'), new Card(10, 'H'), new Card(9, 'H'));
        myHandsArray[1] = new Hands(new Card(10, 'D'), new Card(10, 'S'), new Card(9, 'H'), new Card(10, 'C'), new Card(10, 'H'));
        myHandsArray[2] = new Hands(new Card(9, 'H'), new Card(8, 'D'), new Card(7, 'C'), new Card(10, 'H'), new Card(6, 'S'));
        myHandsArray[3] = new Hands(new Card(11, 'S'), new Card(11, 'D'), new Card(11, 'C'), new Card(11, 'H'), new Card(10, 'H'));
        myHandsArray[4] = new Hands(new Card(13, 'S'), new Card(12, 'S'), new Card(14, 'S'), new Card(11, 'S'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]);

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[0]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[3]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Left");
        passed &= assertEquals(testRoot.left.left, null);
        System.out.println(">> Check Right Grandchild of Left");
        passed &= assertEquals(testRoot.left.right, null);
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, BLACK);
        System.out.println(">> Check Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.myHand, myHandsArray[1]);
        System.out.println(">> Check Colour of Left Grandchild of Right");
        passed &= assertEquals(testRoot.right.left.colour, RED);
        System.out.println(">> Check Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.myHand, myHandsArray[4]);
        System.out.println(">> Check Colour of Right Grandchild of Right");
        passed &= assertEquals(testRoot.right.right.colour, RED);

        System.out.println(">> Check Left Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.left, null);
        System.out.println(">> Check Right Child of Root-Right-Left Path");
        passed &= assertEquals(testRoot.right.left.right, null);
        System.out.println(">> Check Left Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.left, null);
        System.out.println(">> Check Right Child of Root-Right-Right Path");
        passed &= assertEquals(testRoot.right.right.right, null);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testDeleteHandsWithCard()
    {
        // Setup
        System.out.println("============testDeleteHandsWithCard=============");
        boolean passed = true;
        totalTestCount++;

        // Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];        
        myHandsArray[0] = new Hands(new Card(6, 'S'), new Card(3, 'D'), new Card(6, 'C'), new Card(6, 'H'), new Card(3, 'H'));
        myHandsArray[1] = new Hands(new Card(6, 'D'), new Card(6, 'S'), new Card(3, 'H'), new Card(6, 'C'), new Card(6, 'H'));
        myHandsArray[2] = new Hands(new Card(3, 'H'), new Card(5, 'D'), new Card(4, 'C'), new Card(6, 'H'), new Card(2, 'S'));
        myHandsArray[3] = new Hands(new Card(8, 'S'), new Card(8, 'D'), new Card(8, 'C'), new Card(8, 'H'), new Card(7, 'H'));
        myHandsArray[4] = new Hands(new Card(12, 'S'), new Card(11, 'S'), new Card(13, 'S'), new Card(14, 'S'), new Card(10, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]); // this will trigger Case 5R

        testRBT.deleteHandsWithCard(new Card(6, 'H'));

        HandsRBTNode testRoot = testRBT.getRoot();

        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[3]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK); 
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left, null);        
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right.myHand, myHandsArray[4]);                                
        System.out.println(">> Check Right Child Colour");
        passed &= assertEquals(testRoot.right.colour, RED); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testCustomDeleteHandsWithCard()
    {
        // Setup
        System.out.println("============testCustomDeleteHandsWithCard=============");
        boolean passed = true;
        totalTestCount++;

        //Insert Here
        HandsRBT testRBT = new HandsRBT();
        Hands myHandsArray[] = new Hands[5];
        myHandsArray[0] = new Hands(new Card(7, 'S'), new Card(4, 'D'), new Card(7, 'C'), new Card(7, 'H'), new Card(4, 'H'));
        myHandsArray[1] = new Hands(new Card(7, 'D'), new Card(7, 'S'), new Card(4, 'H'), new Card(7, 'C'), new Card(7, 'H'));
        myHandsArray[2] = new Hands(new Card(4, 'H'), new Card(6, 'D'), new Card(5, 'C'), new Card(7, 'H'), new Card(3, 'S'));
        myHandsArray[3] = new Hands(new Card(9, 'S'), new Card(9, 'D'), new Card(9, 'C'), new Card(9, 'H'), new Card(8, 'H'));
        myHandsArray[4] = new Hands(new Card(13, 'S'), new Card(12, 'S'), new Card(14, 'S'), new Card(11, 'S'), new Card(7, 'S'));
        testRBT.insert(myHandsArray[0]);
        testRBT.insert(myHandsArray[1]);
        testRBT.insert(myHandsArray[2]);
        testRBT.insert(myHandsArray[3]);
        testRBT.insert(myHandsArray[4]);

        testRBT.deleteHandsWithCard(new Card(7, 'S'));

        HandsRBTNode testRoot = testRBT.getRoot();

        
        System.out.println(">> Check Root Node");
        passed &= assertEquals(testRoot.myHand, myHandsArray[3]);
        System.out.println(">> Check Root Colour (Always Black)");
        passed &= assertEquals(testRoot.colour, BLACK);
        System.out.println(">> Check Left Child");
        passed &= assertEquals(testRoot.left.myHand, myHandsArray[2]);
        System.out.println(">> Check Left Child Colour");
        passed &= assertEquals(testRoot.left.colour, RED);
        System.out.println(">> Check Right Child");
        passed &= assertEquals(testRoot.right, null);        

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    


    // Assertions

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

    private static boolean assertEquals(boolean a, boolean b)
    {
        if(a != b)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", a, b);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(HandsRBTNode a, HandsRBTNode b)
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
