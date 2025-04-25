package Lab3Model;

// This is the Graph element 
//      Graph Building in Lab 3
//      A* Path Finding in Lab 4
// Also with placeholder member of at least two weight parameters to
// enable the application of Greedy Algorithm in Lab 5.
public class Tile {
    
    private char tileType;  // ' ' for Road (L3)
                            // '#' for Wall (L3)                            
                            // 'I' for intersection (L3)
                            // 'S' for Starting Point (L3)
                            // 'D' for Destination (L3)                           

                            // 'v' for reduced speed (L5)
                            // '^' for express speed (L5)
                            // '?' for speed camera (L5)
                            // '!' for police check (L5)                            

    private int speedChange;  // {-10, 10} depending on v or ^ (L5)

    private int riskFactor; // +1 for speed camera
                            // +5 for police check

    private int xPos;  // x-y coordinate of the Tile
    private int yPos;

    public Tile(int x, int y, char type, int sc)  // build RNG to here instead of scattering it out there
    {
        tileType = type;
        xPos = x;
        yPos = y;
        speedChange = sc;
        if(type == '?') riskFactor = 1;
        else if(type == '!') riskFactor = 5;
        else riskFactor = 0;
    }

    public int getX() { return xPos; }
    public int getY() { return yPos; }
    public char getTileType() { return tileType; }
    public int getSpeedChange() { return speedChange; }
    public int getRiskFactor() { return riskFactor; }
    
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
