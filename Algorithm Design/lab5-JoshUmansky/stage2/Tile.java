package stage2;

// This is the Graph element 
//      Graph Building in Lab 3
// Also with placeholder member of at least two weight parameters to
// enable the application of Greedy Algorithm in Lab 5.
public class Tile {
    
    protected char tileType;  // ' ' for Road (L3)
                            // '#' for Wall (L3)                            
                            // 'I' for intersection (L3)
                            // 'S' for Starting Point (L3)
                            // 'D' for Destination (L3)                           

                            // 'x' for score penalty (L5)
                            // '$' for score reward (L5)                          

    private int scorePenalty;  // x = 5, $ = -2 (L5)

    protected int xPos;  // x-y coordinate of the Tile
    protected int yPos;

    public Tile(int x, int y, char type, int sp)  // build RNG to here instead of scattering it out there
    {
        tileType = type;
        xPos = x;
        yPos = y;
        scorePenalty = sp;
    }

    public int getX() { return xPos; }
    public int getY() { return yPos; }
    public char getTileType() { return tileType; }
    public int getScorePenalty() { return scorePenalty; }    
    
    public void printTile()
    {
        System.out.printf("%c", tileType);
    }

    public void printTileCoord()
    {
        System.out.printf("T(%d, %d)", xPos, yPos);
    }

    // comapres x, y and tile type only.
    public boolean isEqual(Tile thisTile)
    {
        boolean type = (tileType == thisTile.tileType);
        boolean xEq = (xPos == thisTile.xPos);
        boolean yEq = (yPos == thisTile.yPos);

        return type && xEq && yEq;
    }
}
