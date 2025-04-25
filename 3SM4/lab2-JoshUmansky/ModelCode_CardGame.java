import java.util.*;

public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;
    public static Scanner myInputScanner;
    public static HandsMaxHeap aiHandsMaxHeap;
    
    public static void main(String args[]) throws Exception
    {        
        CardPool myCardPool;
        Card[] aiCards, myCards, tempCards;        
        Hands aiHand, myHand;
        
        //HandsBST aiBST; // replace this with your own HandsMaxHeap for improved performance.
        HandsRBT myRBT = new HandsRBT();
        int rounds = 5; //Configurable number of rounds, pocketsize should be 5 * rounds for a modular game
        int aiPocketSize = POCKETSIZE, myPocketSize = POCKETSIZE;
        int aiScore = 0, playerScore = 0; 
        
        
        myCardPool = new CardPool();         
        
        aiCards = myCardPool.getRandomCards(aiPocketSize);
        myCards = myCardPool.getRandomCards(myPocketSize);

        sortCards(aiCards);
        sortCards(myCards);
        //This shouldnt be needed but was giving error otherwise
        aiHandsMaxHeap = new HandsMaxHeap(aiPocketSize*(aiPocketSize-1)*(aiPocketSize-2)*(aiPocketSize-3)*(aiPocketSize-4)/120);

        // Lab 2 - Turn-base AI (Aggresive) vs Player
        generateHands(aiCards);
        generateHandsIntoRBT(myCards, myRBT);

        // Step 2 - Game Loop Logic
        for (int round = 0; round < rounds; round++) {
            // Step 2-1: Print Both AI and Player Pocket Cards for Strategy Analysis        
            System.out.println("AI Pocket Cards: ");
            for (Card card : aiCards) {
                card.printCard();
            }
            System.out.println("\nPlayer Pocket Cards:");
            for (int i = 0; i < myCards.length; i++) {
                System.out.printf("[%d]", i + 1);
                myCards[i].printCard();
            }
            System.out.println();

            if (myRBT.isEmpty()) {
                System.out.println("No more valid hands available. Player is out of moves, chose any 5 cards to continue.");            
            }

            // Step 2-2: Use the provided getUserHand() method to allow player to pick the 5-card hand from the pocket cards
            
            while (true) {
                myHand = getUserHand(myCards, POCKETSIZE);
                if (myRBT.isEmpty() || myRBT.findNode(myHand) != null) { //If the hand is valid (IE in the RBT) or the RBT is empty, break out of the loop
                    break;
                } else { //Otherwise continue asking for a valid hand
                    System.out.println("Invalid hand. There are still valid 5-card hands. Please choose another hand.");
                }
            }

            // Step 2-3: Save the chosen hand as "PLAYERHAND", and update pocket card and RBT
            myRBT.deleteInvalidHands(myHand);
            myCards = removeCardsFromPocket(myCards, myHand);        
            myPocketSize -= 5;

            // Step 2-4: Using the logic from Lab 1, construct the Aggressive AI Logic
            //Lab 1 AI logic, takes best hand, removes from pocket, and generates new hands 
            if(aiHandsMaxHeap.isEmpty()){

                tempCards = new Card[5];
                for(int i = 0; i < 5; i++)
                {
                    tempCards[i] = aiCards[i];
                }
                aiHand = new Hands(tempCards[0], tempCards[1], tempCards[2], tempCards[3], tempCards[4]);
                aiCards = removeCardsFromPocket(aiCards, aiHand);
                aiPocketSize -= 5;
                generateHands(aiCards);
            }
            else
            {
                aiHand = aiHandsMaxHeap.removeMax();
                aiCards = removeCardsFromPocket(aiCards, aiHand);
                aiPocketSize -= 5;
                generateHands(aiCards);
            }       

            // Step 2-5: Determine the Win/Lose result for this round, and update the scores for AI or Player
            System.out.printf("My Hand: ");
            myHand.printMyHand();
            System.out.println();
            System.out.printf("AI Hand: ");
            aiHand.printMyHand();
            System.out.println();

            if (myHand.isMyHandLarger(aiHand)) {
                playerScore++;
                System.out.println("[Result] Player wins this round!");
            } else if (aiHand.isMyHandLarger(myHand)) {
                aiScore++;
                System.out.println("[Result] AI wins this round!");
            } else {
                System.out.println("[Result] It's a draw!");
            }
        }

            // Step 3 - Report the Results
            System.out.println("Game Over!");
            System.out.printf("Final Scores - Player: %d, AI: %d\n", playerScore, aiScore);
            if (playerScore > aiScore) {
                System.out.println("Player wins the game!");
            } else if (aiScore > playerScore) {
                System.out.println("AI wins the game!");
            } else {
                System.out.println("It's a draw!");
            }

            myInputScanner.close();
    }

    public static void generateHands(Card[] thisPocket)
    {   
        //Code from Lab1
        Card[] tempCards;        
        if(thisPocket.length < 5) return;
        int size = thisPocket.length;
        tempCards = new Card[5];
        aiHandsMaxHeap = new HandsMaxHeap(size*(size-1)*(size-2)*(size-3)*(size-4)/120); //53130 possible hands but not all hands are valid, this could be refinded to be more efficient
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
                                aiHandsMaxHeap.insert(thisHand);
                            }
                        }
                    }
                }
            }
        }
    }

    //Helper Function to remove cards from the Pocket
    public static Card[] removeCardsFromPocket(Card[] pocket, Hands hand) {
        Card[] newPocket = new Card[pocket.length - 5];
        int index = 0;
        for (Card card : pocket) {
            if (!hand.hasCard(card)) {
                newPocket[index++] = card;
            }
        }
        return newPocket;
    }

    public static void generateHandsIntoRBT(Card[] cards, HandsRBT thisRBT)
    {
        //This isnt the most effiecient way to generate hands, but I couldn't figure out a better way to solve
        //Modelled after my generateHands code from Lab1, just using the RBT instead instead of MaxHeap
        Card[] tempCards;
        if(cards.length < 5) return;
        int size = cards.length;
        tempCards = new Card[5];
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
                            tempCards[0] = cards[i];
                            tempCards[1] = cards[j];
                            tempCards[2] = cards[k];
                            tempCards[3] = cards[l];
                            tempCards[4] = cards[m];
                            Hands thisHand = new Hands(tempCards[0], tempCards[1], tempCards[2], tempCards[3], tempCards[4]);
                            if(thisHand.isAValidHand())
                            {
                                thisRBT.insert(thisHand);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void sortCards(Card[] cards)
    {
        //Lab 1 code
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


    // This method enables Player to use the numerical key entries to select
    // the 5 cards to form a hand as a tentative move.
    
    public static Hands getUserHand(Card[] myCards, int size)
    {
        int[] mySelection = new int[5];  
        myInputScanner = new Scanner(System.in);

        System.out.println();
        for(int i = 0; i < 5; i++)
        {            
            System.out.printf("Card Choice #%d: ", i + 1);
            mySelection[i] = myInputScanner.nextInt() - 1;
            if(mySelection[i] > size) mySelection[i] = size - 1;
            if(mySelection[i] < 0) mySelection[i] = 0;            
        }
        
        Hands newHand = new Hands(myCards[mySelection[0]], myCards[mySelection[1]], myCards[mySelection[2]], myCards[mySelection[3]], myCards[mySelection[4]]);

        return newHand;
    }

}
