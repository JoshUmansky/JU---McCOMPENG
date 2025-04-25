public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;

    public static CardPool myCardPool; 
    public static Card[] myCards;
    public static int pocketSize;
      
    public static void sortCards()
    {
        // implement your favourite sorting algorithm to sort 
        // all the cards in their RANK in ASCENDING ORDER

        // must use pocketSize (the variable), NOT the POCKETSIZE (the constant) for sorting iteration bound

        //BubbleSort Implementation
        for (int i = 0; i < pocketSize; i++) {
            for (int j = i + 1; j < pocketSize; j++) {
                if (myCards[i].rank > myCards[j].rank) {
                    Card temp = myCards[i];
                    myCards[i] = myCards[j];
                    myCards[j] = temp;
                }
            }
        }
    }

    public static void main(String args[]) throws Exception
    {
        pocketSize = POCKETSIZE;

        myCardPool = new CardPool();        
        myCardPool.printPool();

        myCards = myCardPool.getRandomCards(pocketSize);    
        
        sortCards();

        System.out.println("My Pocket Cards are:");
        for(int j = 0; j < pocketSize; j++)
        {            
            myCards[j].printCard();
        }
        System.out.println();
    }

}
