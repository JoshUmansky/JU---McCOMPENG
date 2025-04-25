package stage2;

import java.util.LinkedList;

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

    private int score;
    private boolean completed;
    private int complexity;

    private TileGraph myGraph;
    private LinkedList<Tile> myPath;
    
    public Tracer(int x, int y, TileMap mRef, int comp, int starterScore)
    {
        myDir = Direction.STOP;
        xPos = x;
        yPos = y;
        mapRef = mRef.getMapRef();
        startTile = mRef.getStartTile();
        endTile = mRef.getEndTile();

        score = starterScore;
        completed = false;
        complexity = comp;

        //myGraph = new TileGraph();
        switch(complexity)
        {
            default:
            case 0:
                myGraph = new TileGraph(true, false, false, mapRef);
                break;
            case 1:
            case 2:
                myGraph = new TileGraph(true, true, false, mapRef);
                break;
            case 3:           
                myGraph = new TileGraph(true, true, true, mapRef);     
                break;
        }
                
        generatePath(); // then invoke the breadth-first 
                        // traversal algorithm
                        // to generate a path to D with least number of Intersections
    }

    public int getX() { return xPos; }
    public int getY() { return yPos; }
    public boolean isCompleted() { return completed; }

    public void updateScore( int delta ) { score += delta; }
    public int getScore() { return score; }

    public void makeAMove() // TBD
    { 
        if(myPath == null) return;

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

        // check score
        if(mapRef[yPos][xPos].getTileType() == 'x' || mapRef[yPos][xPos].getTileType() == '$')
        {            
            score -= mapRef[yPos][xPos].getScorePenalty();
        }
        // check score

        // 2) Check direction and update x / y Pos
        //    - and increment Tile count
        if      (myDir == Direction.LEFT)  xPos--;
        else if (myDir == Direction.RIGHT) xPos++;
        else if (myDir == Direction.UP)    yPos--;
        else if (myDir == Direction.DOWN)  yPos++;
    }     

    private void generatePath() 
    {         
        myPath = myGraph.findShortestPath(startTile, endTile, complexity);  // facade pattern!!
        //printPath();
    }

    public void printPath() 
    { 
        if(myPath != null)
        {
            for(int i = myPath.size() - 1; i >= 0; i--)
            {
                myPath.get(i).printTileCoord();
                System.out.printf(" => ");
            }
            System.out.println();
        }    
        else
            System.out.println("No Valid Path");        
    }

    public LinkedList<Tile> getFullPath()
    {
        if(myPath == null) return null;

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
