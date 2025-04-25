package stage2;

public class WeightedEdge {
    
    private Tile myTile;
    private int penalty;
    private int risk;

    WeightedEdge(Tile thisTile, int spd, int rsk)
    {
        myTile = thisTile;
        penalty = spd;
        risk = rsk;
    }

    public Tile getTile() { return myTile; }
    public int getPenalty() { return penalty; }
    public int getRisk() { return risk; }

    public boolean hasTile(Tile thisTile)
    {
        return myTile.equals(thisTile);  // shallow comparison by reference only
    }
}
