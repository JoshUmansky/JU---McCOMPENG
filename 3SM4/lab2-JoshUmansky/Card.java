public class Card {
    public int rank;        // 2, 3, 4, 5, 6, 7, 8, 9, 10, 11(J), 12(Q), 13(K), 14(A)
    public char suite;      // (C)lub, (D)iamond, (H)eart, (S)pade

    public Card(int r, char s)
    {
        if(r < 2) r = 2;
        else if(r > 14) r = 14;

        if(s != 'C' && s != 'D' && s != 'H' && s != 'S') s = 'C';

        rank = r;
        suite = s;
    }

    
    public boolean isMyCardLarger(Card otherCard)
    {
        if(this.rank > otherCard.rank) return true;
        else if(this.rank < otherCard.rank) return false;
        
        if(this.suite > otherCard.suite) return true;
        else return false;
    }

    public boolean isMyCardSmaller(Card otherCard)
    {
        if(this.rank < otherCard.rank) return true;
        else if(this.rank > otherCard.rank) return false;
        
        if(this.suite < otherCard.suite) return true;
        else return false;
    }

    public boolean isMyCardEqual(Card otherCard)
    {
        return (this.suite == otherCard.suite && this.rank == otherCard.rank);
    }

    public void printCard()
    {
        if(rank < 11)
            System.out.printf("%c%d ", suite, rank);
        else
        {
            char charRank;
            if(rank == 11) charRank = 'J';
            else if(rank == 12) charRank = 'Q';
            else if(rank == 13) charRank = 'K';
            else charRank = 'A';
            System.out.printf("%c%c ", suite, charRank);
        }
    }
}
