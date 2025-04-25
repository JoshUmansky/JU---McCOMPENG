
public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;

    public static CardPool myCardPool;
    public static HandsMaxHeap myMaxHeap;

    public static Card[] myCards, tempCards;
    public static int pocketSize = POCKETSIZE;

    // [Problem 2] Generate All Possible Valid Hands from the Pocket Cards and store them in myMaxHeap
    // Time Complexity;
    // O(C^5 / 120 * logn) where C is the number of cards in the pocket and n is the number of cards in the heap
    // C^5 is divided by 120 as not all hands are valid, and not all interations use the insert function
    // Space Complexity;
    // O(C^5 / 120) where C is the number of cards in the pocket
    public static void generateHands(Card[] thisPocket)
    {
        // If thisPocket has less than 5 cards, no hand can be generated, thus the heap will be empty
        
        // Otherwise, generate all possible valid hands from thisPocket and store them in myMaxHeap

        // Pay attention that memory needs to be allocated for the heap!

        if(thisPocket.length < 5) return;
        int size = thisPocket.length;
        tempCards = new Card[5];
        myMaxHeap = new HandsMaxHeap(size*(size-1)*(size-2)*(size-3)*(size-4)/120); //53130 possible hands but not all hands are valid, this could be refinded to be more efficient
        for(int i = 0; i < size; i++)
        {
            for(int j = i+1; j < size; j++)
            {
                for(int k = j+1; k < size; k++)
                {
                    for(int l = k+1; l < size; l++)
                    {
                        for(int m = l+1; m < size; m++)
                        {
                            tempCards[0] = thisPocket[i];
                            tempCards[1] = thisPocket[j];
                            tempCards[2] = thisPocket[k];
                            tempCards[3] = thisPocket[l];
                            tempCards[4] = thisPocket[m];
                            Hands thisHand = new Hands(tempCards[0], tempCards[1], tempCards[2], tempCards[3], tempCards[4]);
                            if(thisHand.isAValidHand()) //Check if hand is valid
                            {
                                myMaxHeap.insert(thisHand);
                            }
                        }
                    }
                }
            }
        }
    }

    // Sorts the array of Cards in ascending order: ascending order of ranks; cards of equal ranks are sorted in ascending order of suits
    public static void sortCards(Card[] cards)
    {
        int j;
        Card temp;        
        int size = cards.length;
        
        for(int i = 1; i < size; i++) 
        { 
            temp = cards[i];		
            for(j = i; j > 0 && cards[j-1].isMyCardLarger(temp); j--) 
                cards[j] = cards[j-1]; 
            cards[j] = temp;
        }  
    }

    public static void main(String args[]) throws Exception
    {
        Hands myMove;        
        
        myCardPool = new CardPool();        
        myCardPool.printPool();

        myCards = new Card[pocketSize];
        myCards = myCardPool.getRandomCards(POCKETSIZE);  
        sortCards(myCards);

        // print cards
        System.out.println("My Pocket Cards:");
        for(int j = 0; j < pocketSize; j++)
        {            
            myCards[j].printCard();
        }
        System.out.println();
        System.out.println("Possible Hands:");
        
        generateHands(myCards); // generates all valid hands from myCards and stores them in myMaxHeap
        myMaxHeap.printHeap(); // prints the contents of the initial heap
        System.out.println();
        
        // [Problem 3] Implementing Game Logic Part 1 - Aggresive AI: Always Picks the Strongest Hand
        for(int i = 0; pocketSize > 4; i++)
        {            
                                   
            // Step 1:
            // - Check if the Max Heap contains any valid hands 
            //   - if yes, remove the Largest Hand from the heap as the current Move
            //   - otherwise, you are out of valid hands.  Just pick any 5 cards from the pocket as a "Pass Move"
            // - Once a move is chosen, print the Hand for confirmation. MUST PRINT FOR VALIDATION!!

            if(myMaxHeap.isEmpty()) //IE no valid hands left
            {
                tempCards = new Card[5];
                for(int j = 0; j < 5; j++) //Grabs first 5 cards from pocket
                {
                    tempCards[j] = myCards[j];
                }
                myMove = new Hands(tempCards[0], tempCards[1], tempCards[2], tempCards[3], tempCards[4]);
                System.out.println("Pass Move:");
                myMove.printMyHand();
                System.out.println();
            }
            else
            {
                myMove = myMaxHeap.removeMax(); //take best hand from heap
                System.out.println("Move " + i + ": ");
                myMove.printMyHand();
                System.out.println();
            }
            
            // Step 2:
            // - Remove the Cards used in the move from the pocket cards and update the Max Heap
            // - Print the remaining cards and the contents of the heap
            Card[] newPocket = new Card[pocketSize - 5]; //makes new pocket to hold the cards not used in the move
            int index = 0;
            //Loops through pocket and adds cards not used in the move to new pocket
            for (int j = 0; j < pocketSize; j++) {
                boolean found = false;
                for (Card card : myMove.getCards()) { //For each card in move (Uses custom getCards function that returns an array of cards)
                    if (myCards[j].isMyCardEqual(card)) {
                        found = true;
                        break;
                    }
                }
                if (!found) { //If card not found in move, add to new pocket
                    newPocket[index++] = myCards[j];
                }
            }
            pocketSize -= 5;
            myCards = newPocket; //update pocket
            generateHands(myCards); // generates all new valid hands from myCards and stores them in myMaxHeap
            //This could be optimized potentially to remove hands that used the cards just played instead of generating all new hands
            
            //myMaxHeap.printHeap(); // prints the contents of the next heap
            //System.out.println("\n");
                      
        }
        
    }

}
