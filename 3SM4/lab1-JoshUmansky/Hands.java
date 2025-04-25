public class Hands {
    
    private Card[] thisHand = new Card[5];
    private int HandType = 4;    // -1   Not a Hand, 
                                // 0    Straight (S): consecutive ranks, not the same suit
                                // 1    Full House (FH): 3 cards with same rank (the triplet) and 2 other cards of same rank (the pair)
                                // 2    Four-of-a-Kind (FK): 4 cards of same rank (the quadruplet) + a singlet
                                // 3    Straight Flush (SF): same suit and consecutive ranks, but no Ace
                                // 4    Royal Flush (RF): same suit and consecutive ranks, one being an Ace
                                // 5    Invalid Set

    // constructor 1: constructs an invalid hand
    public Hands()
    {
        thisHand[0] = new Card(0, 'C');
        thisHand[1] = new Card(0, 'C');
        thisHand[2] = new Card(0, 'C');
        thisHand[3] = new Card(0, 'C');
        thisHand[4] = new Card(0, 'C');

        HandType = 5;  // invalid
    } // end constructor 1

    
    // constructor 2: constructs a hand containing the input cards
    // assumes that all cards are different
    // sorts the cards in the hand as follows: S, SF, RF are sorted in decreasing order of ranks; 
    // FK has the quadruplet at the beginning sorted alphabetically by suit: C (Club) < D (Diamond) < H (Heart) < S (Spade)
    // RF has the triplet first and the pair last; the cards of equal rank are sorted alphabetically by suit
    
    public Hands(Card c1, Card c2, Card c3, Card c4, Card c5)
    {              
        thisHand[0] = c1;
        thisHand[1] = c2;
        thisHand[2] = c3;
        thisHand[3] = c4;
        thisHand[4] = c5;             

        // Internally sorted in Descending Order        
        HandType = checkHandType();

        // need to correct for four-of-a-kind - leading 4 are checked before checking the singlet
        if(HandType == 2 && thisHand[0].rank != thisHand[1].rank)  // four of a kind
        {
            Card temp = thisHand[0];
            for(int i = 1; i < 5; i++)
                thisHand[i-1] = thisHand[i];
            thisHand[4] = temp;
        }

        // need to correct for full house - leading 3 is checked before checking the pair 
        if(HandType == 1 && thisHand[1].rank != thisHand[2].rank)  // full house
        {
            Card temp0 = thisHand[0];
            Card temp1 = thisHand[1];
            for(int i = 2; i < 5; i++)
                thisHand[i-2] = thisHand[i];
            thisHand[3] = temp0;
            thisHand[4] = temp1;
        }      
    } // end constructor 2
    
    private int checkHandType()
    {
        // First Check Straight
        sortHandByRank();        

        if(thisHand[0].rank == (thisHand[1].rank + 1) && thisHand[1].rank == (thisHand[2].rank + 1) &&
           thisHand[2].rank == (thisHand[3].rank + 1) && thisHand[3].rank == (thisHand[4].rank + 1))    // checks if ranks are consecutive
        {            
            // If yes, then check if all suits are equal
            if(thisHand[0].suite == thisHand[1].suite && thisHand[0].suite == thisHand[2].suite &&
               thisHand[0].suite == thisHand[3].suite && thisHand[0].suite == thisHand[4].suite)
            {
                if(thisHand[0].rank == 14) 
                    return 4; // if leading by Ace, Royal Straight
                else 
                    return 3; // return 3 if Straight Flush
            }

            // return 0 if not Straight Flush or Royal
            return 0;    
        }
        // If no, move on                   
            
        
        // Then check Full House or Four-of-a-Kind
        sortHandBySuite();
        
        // Check if Leading 3 or Trailing 3 are identical
        if( thisHand[0].rank == thisHand[1].rank && thisHand[0].rank == thisHand[2].rank )
        {            
            // if yes, Check if Trailing two or Leading 2 are identical
            if( thisHand[4].rank == thisHand[3].rank )
            {
                return 1; // If yes, return 1
            }
                
            // If no, Check if Second-Last or Second are identical to the first / last 3
            if( thisHand[0].rank == thisHand[3].rank )
            {
                return 2; // If yes, return 2
            }                
            
            // If no, move on
        }

        else if( thisHand[4].rank == thisHand[2].rank && thisHand[4].rank == thisHand[3].rank )
        {            
            // if yes, Check if Trailing two or Leading 2 are identical
            if( thisHand[0].rank == thisHand[1].rank )
            {
                return 1; // If yes, return 1
            }
                
            // If no, Check if Second-Last or Second are identical to the first / last 3
            if( thisHand[4].rank == thisHand[1].rank )
            {
                return 2; // If yes, return 2
            }                
        }        
        
        // If no, move on
            
        // If reaching the end, the hand is NOT our interested 5-card hand
        return -1;
    }// end method checkHandType

    private void sortHandBySuite()  // insertion sort
    {
        int j;
        Card temp;

        for(int i = 1; i < 5; i++) 
        { 
            temp = thisHand[i];		
            for(j = i; j > 0 && thisHand[j-1].isMyCardLarger(temp); j--) 
                thisHand[j] = thisHand[j-1]; 
            thisHand[j] = temp;
        }        
    }

    private void sortHandByRank()  // insertion sort
    {
        int j;
        Card temp;

        for(int i = 1; i < 5; i++) 
        { 
            temp = thisHand[i];		
            for(j = i; j > 0 && thisHand[j-1].rank < temp.rank; j--) 
                thisHand[j] = thisHand[j-1]; 
            thisHand[j] = temp;
        }        
    }
    

    public int getHandType()
    {
        return HandType;
    }

    public boolean isAValidHand()
    {
        return (HandType < 5 && HandType > -1);
    }

    public boolean isMyHandLarger(Hands otherHand)
    {
        boolean result = false;
        if(otherHand == null) return false;
        
        if(this.HandType > otherHand.HandType) result = true;
        else if(this.HandType < otherHand.HandType) result = false;
        
        else
        {
            for(int i = 0; i < 5; i++)
            {
                if(thisHand[i].isMyCardLarger(otherHand.thisHand[i])) 
                {
                    result = true;
                    break;
                }
                else if(thisHand[i].isMyCardSmaller(otherHand.thisHand[i]))
                {
                    result = false;
                    break;
                }
                // otherwise, equal card, continue comparing
            }            
        }
        return result;
    }// end method

    public boolean isMyHandSmaller(Hands otherHand)
    {
        boolean result = false;
        if(otherHand == null) return false;

        if(this.HandType < otherHand.HandType) result = true;
        else if(this.HandType > otherHand.HandType) result = false;
        else
        {
            for(int i = 0; i < 5; i++)
            {
                if(thisHand[i].isMyCardSmaller(otherHand.thisHand[i])) 
                {
                    result = true;
                    break;
                }
                else if(thisHand[i].isMyCardLarger(otherHand.thisHand[i]))
                {
                    result = false;
                    break;
                }
            }                
        }
        return result;
    }// end method

    public boolean isMyHandEqual(Hands otherHand)
    {
        if(this.HandType != otherHand.HandType) return false;

        for(int i = 0; i < 5; i++)
            if(!thisHand[i].isMyCardEqual(otherHand.thisHand[i])) return false;
        
        return true;
    }

    public Card getCard(int index)
    {        
        if(index < 0 || index > 4)
        {
            Card invalidCard = new Card(0, 'K');
            return invalidCard;
        }

        return thisHand[index];
    }

    // Added function for Lab 1 to return an array of cards
    public Card[] getCards()
    {        
        return thisHand;
    }


    public void printMyHand()
    {
        for(int i = 0; i < 5; i++)
            thisHand[i].printCard();        

        if(HandType == 0)
            System.out.print("[S]");
        else if(HandType == 1)
            System.out.print("[FH]");
        else if(HandType == 2)
            System.out.print("[FK]");
        else if(HandType == 3)
            System.out.print("[SF]");
        else if(HandType == 4)
            System.out.print("[RF]");
        else
            System.out.print("[IV]");
    }

    // Lab 2: Required Helper Function
    public boolean hasCard(Card thisCard)
    {
        for(int i = 0; i < 5; i++)
        {
            if(thisHand[i].isMyCardEqual(thisCard))
                return true;
        }
        return false;
    }



}// end class
