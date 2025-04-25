package Lab3Model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// This is the Tracer that will track the Graph Map using
// Lab 3: Manual Player Control (Synchronous)
// Lab 4: Automated A* Path Finding Algorithm
// Lab 5: Automated Greedy Algorithm A*

enum Direction {STOP, UP, DOWN, LEFT, RIGHT}

// At least 2 instances will be created on the Map in the model code
// to demonstrate the implemented AI logic.
public class Tracer 
{
    public static final char SYMBOL = 'P';
    
    private int xPos;
    private int yPos;
    private Direction myDir;
    private Tile[][] mapRef;
    private Tile startTile;
    private Tile endTile;

    private int tileCount;
    private int speed;
    private int riskScore;
    private boolean completed;

    private TileGraph myGraph;
    private LinkedList<Tile> myPath;

    //constructor
    public Tracer(int x, int y, TileMap mRef)
    {
        myDir = Direction.STOP;
        xPos = x;
        yPos = y;
        mapRef = mRef.getMapRef();
        startTile = mRef.getStartTile();
        endTile = mRef.getEndTile();

        tileCount = 0;
        speed = 100;
        riskScore = 0;
        completed = false;

        myGraph = new TileGraph(mRef); // construct the graph

        generatePath(); // generate a path from S to D with the least number of Intersections
    }

    public int getX() { return xPos; }
    public int getY() { return yPos; }
    public int getTileCount() { return tileCount; }
    public boolean isCompleted() { return completed; }

    public void makeAMove() // TBD
    { 
        // 1) Check the landed Tile property
        //    - Speed UP / DOWN: update the speed param
        //    - Speed Cam / Police: update the risk score
        //    - At Intersection, follow path and change the direction
        //    - if at Destination, set completed to true
        if(mapRef[yPos][xPos].getTileType() == 'D')
        {
            completed = true;
            return;
        }

        if(mapRef[yPos][xPos].getTileType() == 'I' || mapRef[yPos][xPos].getTileType() == 'S')
        {            
            // remember, reversed path
            Tile nextTile = myPath.get(myPath.indexOf(mapRef[yPos][xPos]) - 1);
            if(xPos > nextTile.getX()) myDir = Direction.LEFT;
            else if(xPos < nextTile.getX()) myDir = Direction.RIGHT;
            else if(yPos > nextTile.getY()) myDir = Direction.UP;
            else if(yPos < nextTile.getY()) myDir = Direction.DOWN;
            
        }

        // 2) Check direction and update x / y Pos
        //    - and increment Tile count
        if      (myDir == Direction.LEFT)  xPos--;
        else if (myDir == Direction.RIGHT) xPos++;
        else if (myDir == Direction.UP)    yPos--;
        else if (myDir == Direction.DOWN)  yPos++;
        tileCount++;

        // 3) If Map Updates Every 10 seconds 
        //    (not used in Lab 3.  Will implement in Lab 4 / 5)
            updatePath();
    } 
    

       
    private void generatePath() 
    {         
        myPath = myGraph.findShortestPath(startTile, endTile);        
    }

    private void updatePath() 
    { 
        // TBA in Lab 4 / 5   
    }  

    public void printPath() 
    { 
        if(myPath != null)
        {
            for(int i = myPath.size() - 1, j = 0; i >= 0; i--, j++)
            {
                myPath.get(i).printTileCoord();
                if(i != 0) System.out.printf(" > ");
                if(j % 8 == 0) System.out.println();
            }
            System.out.println();
        }    
        else
            System.out.println("No Valid Path");        
    }

    public LinkedList<Tile> getFullPath()
    {
        LinkedList<Tile> fullPath = new LinkedList<Tile>();

        for(int i = myPath.size() - 1; i > 0; i--)
        {
            Tile currTile = myPath.get(i); 
            Tile prevTile = myPath.get(i - 1);

            fullPath.add(currTile);
            fullPath.add(prevTile);

            if(currTile.getY() == prevTile.getY())  // x different - fill x direction
            {
                for(int j = 1; j < prevTile.getX() - currTile.getX(); j++)
                {
                    fullPath.add(mapRef[currTile.getY()][currTile.getX() + j]);
                }
            }
            else // y different - fill y direction
            {
                for(int j = 1; j < prevTile.getY() - currTile.getY(); j++)
                {
                    fullPath.add(mapRef[currTile.getY() + j][currTile.getX()]);
                }
            }
        }

        return fullPath;
    }
}
