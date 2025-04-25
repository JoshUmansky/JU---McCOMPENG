package stage1;

import java.util.LinkedList;
import java.util.Random;

public class TreasureMap {

    public static final int BOARDSIZEX = 50;  // 8 * 6 + 2, 5 per row
    public static final int BOARDSIZEY = 22;  // 5 * 4 + 2, 4 per row
    public static final int TREASURENUM = 20; // 5 * 4 = 20 in total
    public static final int RANDRANGE = 2000;
    
    private char mapRef[][];
    private LinkedList<TreasureTile> treasureList;
    private boolean splittable;
    private TreasureHunter myHunter;

    public TreasureMap(boolean split)
    {        
        Random rand = new Random();
        splittable = split;        
        
        mapRef = new char[BOARDSIZEY][BOARDSIZEX];
        for(int i = 0; i < BOARDSIZEY; i++)
        {
            for(int j = 0; j < BOARDSIZEX; j++)
            {
                if(i == 0 || i == BOARDSIZEY - 1 || j == 0 || j == BOARDSIZEX - 1)
                    mapRef[i][j] = '#';
                else if(i == 1 && j == 1)
                    mapRef[i][j] = 'S';
                else if(i == BOARDSIZEY - 2 && j == BOARDSIZEX - 2)
                    mapRef[i][j] = 'D';                
                else 
                    mapRef[i][j] = ' ';
            }
        }

        treasureList = new LinkedList<TreasureTile>();
        for(int i = 1; i < 5; i++)
        {
            for(int j = 1; j < 6; j++)
            {
                int temp = rand.nextInt(RANDRANGE);
                TreasureTile tempTile;

                if(temp < 1800) // regular treasure
                {
                    tempTile = new TreasureTile((BOARDSIZEX - 2) * j / 6 + 1, 
                                                (BOARDSIZEY - 2) * i / 5 + 2, 
                                                '+', splittable);
                }
                else if(temp >= 1800 && temp < 1950) // better treasure
                {
                    tempTile = new TreasureTile((BOARDSIZEX - 2) * j / 6 + 1, 
                                                (BOARDSIZEY - 2) * i / 5 + 2, 
                                                '$', splittable);
                }
                else // best treasure
                {
                    tempTile = new TreasureTile((BOARDSIZEX - 2) * j / 6 + 1, 
                                                (BOARDSIZEY - 2) * i / 5 + 2, 
                                                '?', splittable);
                }

                treasureList.add(tempTile);

                // Treasure Type Display
                mapRef[tempTile.getY()][tempTile.getX()] = tempTile.getTileType();
                // Score Display
                mapRef[tempTile.getY() - 2][tempTile.getX() - 1] = 's';
                mapRef[tempTile.getY() - 2][tempTile.getX()] = (char)(tempTile.getScore() / 10 + 48);
                mapRef[tempTile.getY() - 2][tempTile.getX() + 1] = (char)(tempTile.getScore() % 10 + 48);
                // Weight Display
                mapRef[tempTile.getY() - 1][tempTile.getX() - 1] = 'w';
                mapRef[tempTile.getY() - 1][tempTile.getX()] = (char)(tempTile.getWeight() / 10 + 48);
                mapRef[tempTile.getY() - 1][tempTile.getX() + 1] = (char)(tempTile.getWeight() % 10 + 48);                
            }
        }
    }

    public void setHunterRef(TreasureHunter hunter)
    {
        myHunter = hunter;
    }

    public char[][] getMapRef()
    {
        return mapRef;
    }

    public void updateTreasureStatus(TreasureTile thisTile)
    {
        // Treasure Type Display
        if(thisTile.getScore() != 0 && thisTile.getWeight() != 0)
        {
            mapRef[thisTile.getY()][thisTile.getX()] = thisTile.getTileType();
            mapRef[thisTile.getY() - 2][thisTile.getX() - 1] = 's';
            mapRef[thisTile.getY() - 2][thisTile.getX()] = (char)(thisTile.getScore() / 10 + 48);
            mapRef[thisTile.getY() - 2][thisTile.getX() + 1] = (char)(thisTile.getScore() % 10 + 48);
            mapRef[thisTile.getY() - 1][thisTile.getX() - 1] = 'w';
            mapRef[thisTile.getY() - 1][thisTile.getX()] = (char)(thisTile.getWeight() / 10 + 48);
            mapRef[thisTile.getY() - 1][thisTile.getX() + 1] = (char)(thisTile.getWeight() % 10 + 48);
        }
        else
        {
            mapRef[thisTile.getY()][thisTile.getX()] = ' ';
            mapRef[thisTile.getY() - 2][thisTile.getX() - 1] = ' ';
            mapRef[thisTile.getY() - 2][thisTile.getX()] = ' ';
            mapRef[thisTile.getY() - 2][thisTile.getX() + 1] = ' ';
            mapRef[thisTile.getY() - 1][thisTile.getX() - 1] = ' ';
            mapRef[thisTile.getY() - 1][thisTile.getX()] = ' ';
            mapRef[thisTile.getY() - 1][thisTile.getX() + 1] = ' ';
        }        
    }

    public LinkedList<TreasureTile> getTreasureList()
    {
        return treasureList;
    }
    
    public boolean isSplittable()
    {
        return splittable;
    }

    public void printMap()
    {   
        for(int i = 0; i < BOARDSIZEY; i++)
        {
            for(int j = 0; j < BOARDSIZEX; j++)
            {   
                if(i == myHunter.getY() && j == myHunter.getX())
                    System.out.printf("P");
                else             
                    System.out.printf("%c", mapRef[i][j]);
            }
            System.out.printf("\n");
        }

        System.out.printf("Score: %d \t Weight: %d\n", myHunter.getInstScore(), myHunter.getInstWeight());
        if(splittable)
            System.out.println("Splittable Treasure Mode.");
        else    
            System.out.println("Non-Splittable Treasure Mode.");
    }

}

