import java.util.Random;

public class CardPool 
{    
    private static final int DECKCOUNT = 1;    

    private static final int SUITESIZE = 4;
    private static final int RANKSIZE = 13;
    private static final int SINGLEDECKSIZE = 52;
        
    private Card[] myPool = new Card[DECKCOUNT * SINGLEDECKSIZE];
    boolean[] bitVec = new boolean[DECKCOUNT * SINGLEDECKSIZE];

    public CardPool()
    {
        char suiteTemp;

        for(int i = 0; i < SUITESIZE; i++)
        {            
            if(i == 0) suiteTemp = 'C';
            else if(i == 1) suiteTemp = 'D';
            else if(i == 2) suiteTemp = 'H';
            else suiteTemp = 'S';

            for(int j = 0; j < RANKSIZE; j++)
            {
                for(int k = 0; k < DECKCOUNT; k++)
                {
                    myPool[RANKSIZE*i + j + SINGLEDECKSIZE*k] = new Card(j + 2, suiteTemp);
                }
            }
        }
    }


    public void printPool()
    {
        for(int i = 0; i < DECKCOUNT * SINGLEDECKSIZE; i++)
        {            
            myPool[i].printCard();
            if((i + 1) % SINGLEDECKSIZE == 0) System.out.println();
        }
    }

    public Card[] getRandomCards(int n)
    {
        Card[] tempCards = new Card[n];        
        Random rand = new Random();
        int index = 0, count = 0;                

        while(count < n)
        {
            index = rand.nextInt(DECKCOUNT * SINGLEDECKSIZE);
            if(bitVec[index]) continue;
            bitVec[index] = true;            
            tempCards[count++] = myPool[index];                        
        }       

        // for(int i = 0; i < n; i++)
        //     tempCards[i].printCard();

        return tempCards;        
    }    
}
