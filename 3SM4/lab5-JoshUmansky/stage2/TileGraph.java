package stage2;

import java.util.*;

// This is the upgraded TileGraph from Lab 3 with weighted edges and directivity selection

class TileIntPair
{
    private Tile myTile;
    private int myInt;

    public TileIntPair(Tile thisTile, int thisInt)
    {
        myTile = thisTile;
        myInt = thisInt;
    }

    public Tile getTile() { return myTile; }
    public int getInt() { return myInt; }
    public void setInt(int newInt) { myInt = newInt; }
    public void setTile(Tile newTile) { myTile = newTile; }
}

public class TileGraph {
        
    private Map<Tile, LinkedList<WeightedEdge>> adjList;
    private boolean directed;

    public TileGraph()
    {
        adjList = new HashMap<>();
        directed = true;
    }

    public TileGraph(boolean dir, boolean wgh, boolean neg, Tile[][] thisMap)
    {
        adjList = new HashMap<>();
        directed = dir;
        buildGraph(thisMap);
    }

    public void addVertex(Tile thisTile)
    {
        adjList.putIfAbsent(thisTile, new LinkedList<WeightedEdge>());
    }

    // Lab 4: Must be updated, with spd only in Lab 4
    // Need three overload: src + dst, src + dst + spd, src + dst + spd + rsk (Lab 5)
    public void addEdge(Tile src, Tile dst)
    {
        LinkedList<WeightedEdge> srcList = adjList.get(src);        
        boolean containsDst = false;       

        for(WeightedEdge w : srcList) {
            containsDst &= w.hasTile(dst);
        }        

        if(!containsDst)
        {
            adjList.get(src).add(new WeightedEdge(dst, 0, 0));  // keep only this line for directed graph                    
        }

        if(!directed)
        {
            LinkedList<WeightedEdge> dstList = adjList.get(dst);
            boolean containsSrc = false;
            for(WeightedEdge w : dstList) {
                containsSrc &= w.hasTile(src);
            }
        
            if(!containsSrc)
            {
                adjList.get(dst).add(new WeightedEdge(src, 0, 0));  // keep this line for undirected graph        
            }
        }        
    }

    public void addEdge(Tile src, Tile dst, int spd)
    {   
        LinkedList<WeightedEdge> srcList = adjList.get(src);        
        boolean containsDst = false;       

        for(WeightedEdge w : srcList) {
            containsDst &= w.hasTile(dst);
        }        

        if(!containsDst)
        {
            adjList.get(src).add(new WeightedEdge(dst, spd, 0));  // keep only this line for directed graph                    
        }

        if(!directed)
        {
            LinkedList<WeightedEdge> dstList = adjList.get(dst);
            boolean containsSrc = false;
            for(WeightedEdge w : dstList) {
                containsSrc &= w.hasTile(src);
            }
        
            if(!containsSrc)
            {
                adjList.get(dst).add(new WeightedEdge(src, spd, 0));  // keep this line for undirected graph        
            }
        }        
    }

    public void addEdge(Tile src, Tile dst, int spd, int rsk)
    {   
        LinkedList<WeightedEdge> srcList = adjList.get(src);        
        boolean containsDst = false;       

        for(WeightedEdge w : srcList) {
            containsDst &= w.hasTile(dst);
        }        

        if(!containsDst)
        {
            adjList.get(src).add(new WeightedEdge(dst, spd, rsk));  // keep only this line for directed graph                    
        }

        if(!directed)
        {
            LinkedList<WeightedEdge> dstList = adjList.get(dst);
            boolean containsSrc = false;
            for(WeightedEdge w : dstList) {
                containsSrc &= w.hasTile(src);
            }
        
            if(!containsSrc)
            {
                adjList.get(dst).add(new WeightedEdge(src, spd, rsk));  // keep this line for undirected graph        
            }
        }        
    }

    public LinkedList<WeightedEdge> getAdjacentVertices(Tile thisTile)
    {
        return adjList.get(thisTile);
    }

    public LinkedList<Tile> depthFirstTraversal(Tile start)
    {
        LinkedList<Tile> visited = new LinkedList<Tile>();
        Stack<Tile> stack = new Stack<Tile>();
        stack.push(start);

        while(!stack.isEmpty())
        {
            Tile currTile = stack.pop();
            if(!visited.contains(currTile))
            {
                visited.add(currTile);
                for(WeightedEdge t : getAdjacentVertices(currTile))
                {
                    stack.push(t.getTile());
                }
            }
        }

        return visited;
    }

    // using Breadth-First search algorithm 
    private LinkedList<Tile> BFSshortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> path = new LinkedList<Tile>();  // front end to start

        // Breadth-First contents
        Set<Tile> visited = new LinkedHashSet<Tile>();
        Queue<Tile> queue = new LinkedList<Tile>();

        // Simple Shortest-Path requirement (track the parents and distances)
        Map<Tile, Tile> parent = new HashMap<Tile, Tile>();
        Map<Tile, Integer> distance = new HashMap<Tile, Integer>();

        // Start from START point
        distance.putIfAbsent(start, 0);

        // Standard Breadth-First Searching Algorithm (with parent and distance tracker)
        queue.add(start);
        while(!queue.isEmpty())
        {
            Tile currTile = queue.poll();  // get and remove the front element
            for(WeightedEdge t : getAdjacentVertices(currTile))
            {
                Tile visitedTile = t.getTile();
                if(!visited.contains(visitedTile))
                {
                    visited.add(visitedTile);
                    queue.add(visitedTile);

                    // if this neighbouring node has not been visited...
                    // 1. record currTile as the parent of this neighbour node
                    parent.putIfAbsent(visitedTile, currTile);
                    // 2. then, record the nodal distance from the parent node
                    distance.putIfAbsent(visitedTile, distance.get(currTile) + 1);                    
                }
            }
        }

        if(!distance.containsKey(end))
        {            
            return null;  // indicating no path from start to end is possible.
        }

        // Back-Track from END to START
        Tile currTile = end;
        path.add(end);
        while(parent.get(currTile) != null)
        {
            path.add(parent.get(currTile));
            currTile = parent.get(currTile);
        }

        return path;
    }

    // or, dijkastra for path with lowest penalties
    private LinkedList<Tile> DijkstraShortestPath(Tile start, Tile end)
    {
        // TileIntPair - Tile: current Tile, Int: accumulated penalty 
        // Custom comparator to make PQ a min-heap, hence guarnateed minimizing
        PriorityQueue<TileIntPair> processQ 
          = new PriorityQueue<TileIntPair>(Comparator.comparingInt(TileIntPair::getInt));      

        Map<Tile, TileIntPair> penaltyTable = new HashMap<>();  // TileIntPair - Tile: Parent, Int: accumulated penalty 
        
        processQ.add(new TileIntPair(start, 0));
        penaltyTable.putIfAbsent(start, new TileIntPair(null, 0)); // i.e. no parent
        
        while(!processQ.isEmpty())
        {
            Tile currTile = processQ.peek().getTile();
            processQ.poll();

            for(WeightedEdge wEdge : adjList.get(currTile))
            {
                int newPenalty = penaltyTable.get(currTile).getInt() + wEdge.getPenalty();
                Tile dstTile = wEdge.getTile();
                
                if(!penaltyTable.containsKey(dstTile))  // i.e. INF distance
                {
                    // Add entry to the boost table
                    penaltyTable.put(dstTile, new TileIntPair(currTile, newPenalty));

                    // Add the new tile to the process queue
                    processQ.add(new TileIntPair(dstTile, newPenalty));
                }
                else
                {
                    int oldPenalty = penaltyTable.get(dstTile).getInt();
                    if(newPenalty < oldPenalty)
                    {
                        penaltyTable.get(dstTile).setInt(newPenalty);
                        penaltyTable.get(dstTile).setTile(currTile);
                        processQ.add(new TileIntPair(dstTile, newPenalty));
                    }
                }
            }
        } 
        
        for(Tile thisTile: penaltyTable.keySet())
        {
            Tile parentTile = penaltyTable.get(thisTile).getTile();
            int penality = penaltyTable.get(thisTile).getInt();

            thisTile.printTileCoord();
            System.out.printf("\nTotal Penality: %d\n", penality);
            if(parentTile != null)
            {
                System.out.printf("Parent Tile: ");
                parentTile.printTileCoord();
            }
            else
            {
                System.out.printf("No Parent Tile.");
            }
           
            System.out.println();
        }

        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        shortestPath.addLast(end);
        Tile pathTile = end;   
        
        if(!penaltyTable.containsKey(pathTile)) return null; // no path leading to the end.

        do
        {            
            pathTile = penaltyTable.get(pathTile).getTile(); // get parent tile
            shortestPath.addLast(pathTile);
        } while(pathTile != start);
        
        return shortestPath;
    }

    // Bellman-Ford Lowest Penalty Path - will return null for negative weight cycles or shortest path not found
    private LinkedList<Tile> BellmanShortestPath(Tile start, Tile end)
    {
        Map<Tile, TileIntPair> penaltyTable = new HashMap<>();  // TileIntPair - Tile: Parent, Int: Lowest Penalty
        penaltyTable.putIfAbsent(start, new TileIntPair(null, 0)); // i.e. no parent
        Boolean done = false;

        // for all entries (total N) in the adjacency list...
        for(int i = 0; i < adjList.size() && !done; i++) // Carry out N iterations at max
        {
            done = true; //      If the table is never updated in a full iteration, end the algorithm

            for(Tile currTile: adjList.keySet())
            {            
            //      Visit each entry, update boostTable using the known weighted edges (update when smaller)
                for(WeightedEdge wEdge : adjList.get(currTile))
                {
                    if(!penaltyTable.containsKey(currTile)) 
                        continue;
                        //      Do not update if the tile is not in the boostTable 
                        //      (i.e. never visited from other nodes)

                    Tile dstTile = wEdge.getTile();
                    int newPenalty = penaltyTable.get(currTile).getInt() + wEdge.getPenalty();
                    
                    if(!penaltyTable.containsKey(dstTile))  // INF
                    {
                        penaltyTable.put(dstTile, new TileIntPair(currTile, newPenalty));
                        done = false;
                    }
                    else
                    {
                        int oldPenalty = penaltyTable.get(dstTile).getInt();
                        if(newPenalty < oldPenalty)
                        {
                            penaltyTable.get(dstTile).setInt(newPenalty);
                            penaltyTable.get(dstTile).setTile(currTile);
                            done = false;
                        }
                    }
                    
                }                        
            }

            if(i == adjList.size() - 1 && !done)
            {
                System.out.println("!! Negative Weight Cycle Detected !!");
                return null;  // indicating negative weight cycles
            }

        }


        for(Tile thisTile: penaltyTable.keySet())
        {
            Tile parentTile = penaltyTable.get(thisTile).getTile();
            int penalty = penaltyTable.get(thisTile).getInt();

            thisTile.printTileCoord();
            System.out.printf("\nTotal Penalty: %d\n", penalty);
            if(parentTile != null)
            {
                System.out.printf("Parent Tile: ");
                parentTile.printTileCoord();
            }
            else
            {
                System.out.printf("No Parent Tile.");
            }
           
            System.out.println();
        }



        // Then build the shortest path using parent information.
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        shortestPath.addLast(end);
        Tile pathTile = end;   
        
        if(!penaltyTable.containsKey(pathTile)) return null; // no path leading to the end.

        do
        {            
            pathTile = penaltyTable.get(pathTile).getTile(); // get parent tile
            shortestPath.addLast(pathTile);
        } while(pathTile != start);
        
        return shortestPath;
    }

    private LinkedList<Tile> DAGShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> sortedList = topologicalSort();
        Map<Tile, TileIntPair> penaltyTable = new HashMap<>(); 

        System.out.printf("\nStart Tile: ");
        start.printTileCoord();
        System.out.printf("\nEnd Tile: ");
        end.printTileCoord();

        penaltyTable.putIfAbsent(start, new TileIntPair(null, 0));
        
        for(Tile t : sortedList)
        {
            if(penaltyTable.containsKey(t))  // not INF
            {
                for(WeightedEdge wEdge : adjList.get(t))
                {
                    Tile dstTile = wEdge.getTile();
                    int newPenalty = penaltyTable.get(t).getInt() + wEdge.getPenalty();
         
                    if(!penaltyTable.containsKey(dstTile)) // INF
                    {
                        penaltyTable.put(dstTile, new TileIntPair(t, newPenalty));
                    }
                    else
                    {
                        int oldPenalty = penaltyTable.get(dstTile).getInt();
                        if(newPenalty < oldPenalty)
                        {
                            penaltyTable.get(dstTile).setInt(newPenalty);
                            penaltyTable.get(dstTile).setTile(t);                            
                        }
                    }
                }
            }
        }

        LinkedList<Tile> shortestPath = new LinkedList<>();
        shortestPath.addLast(end);
        Tile pathTile = end;   
        
        if(!penaltyTable.containsKey(pathTile)) return null; // no path leading to the end.

        do
        {            
            pathTile = penaltyTable.get(pathTile).getTile(); // get parent tile
            shortestPath.addLast(pathTile);
        } while(pathTile != start);        

        return shortestPath;
    }
    
    // Kahn's Topological Sorting
    private LinkedList<Tile> topologicalSort()  // helper method for DAG Shortest Path
    {
        Map<Tile, Integer> degreeMap = new HashMap<>();
        Queue<Tile> tileQ = new LinkedList<>();
        LinkedList<Tile> sortedList = new LinkedList<>();        

        // Figure out each vertices
        for(Tile t : adjList.keySet())        
        {
            degreeMap.putIfAbsent(t,0);

            for(WeightedEdge wEdge : adjList.get(t))
            {
                Tile dstTile = wEdge.getTile();                
                if(!degreeMap.containsKey(dstTile))
                {
                    degreeMap.putIfAbsent(dstTile, 0);
                }
                else
                {
                    int oldValue = degreeMap.get(dstTile);
                    degreeMap.replace(dstTile, oldValue + 1);
                }
            }
        }

        // Enqueue all vertices with indegree 0
        for(Tile t: degreeMap.keySet())
        {
            if(degreeMap.get(t) == 0)
            {
                tileQ.offer(t);
            }
        }

        while(!tileQ.isEmpty())
        {
            Tile currTile = tileQ.poll();
            sortedList.add(currTile);

            for(WeightedEdge wEdge : adjList.get(currTile))
            {
                Tile dstTile = wEdge.getTile();
                int oldValue = degreeMap.get(dstTile);

                degreeMap.replace(dstTile, oldValue - 1);
                if(oldValue - 1 == 0)
                {
                    tileQ.offer(dstTile);
                }
            }
        }

        for(Tile t : sortedList)
        {
            t.printTileCoord();
            System.out.println();
        }

        return sortedList;
    }

    public LinkedList<Tile> findShortestPath(Tile start, Tile end, int complexity)
    {
        switch(complexity)
        {
            default:
            case 0:
                return BFSshortestPath(start, end);  // Lab 3
             
            case 1:
                System.out.println("DAG Triggered!");
                return DAGShortestPath(start, end);    // DAG
                
            case 2:
                return DijkstraShortestPath(start, end);    // Dijkstra

            case 3:
                return BellmanShortestPath(start, end);    // Bellman

            case 4:
                return null; // TBA in lab 5

            case 5:
                return null; // TBA in lab 5
        }
    }

    public void printGraph()
    {
        // Need Documentations on Set<>, Map<>, LinkedList<>, Collection<>, and Iterator<>

        Set<Tile> keySet = adjList.keySet();
        Collection<LinkedList<WeightedEdge>> valueLists = adjList.values();      

        Iterator<Tile> keySetIter = keySet.iterator();  // so to iterate through map
        Iterator<LinkedList<WeightedEdge>> valueListsIter = valueLists.iterator();  // so to iterate through map
        int size = keySet.size();
        
        for(int i = 0; i < size; i++)
        {
            keySetIter.next().printTileCoord();
            System.out.printf(" >>\t");
            valueListsIter.next().forEach(e -> {e.getTile().printTileCoord(); System.out.printf(" : ");});
            System.out.println();
        }
    }

    private void buildGraph(Tile[][] mapRef)
    {
        // will also use breadth-first approach, assuming Tracer always starts at "S"
        // starting point (1,1) on an intersection, NOT a road between two intersections.

        // Use a Set to record all visited intersections
        // Use a Queue to record the current intersection to test out
        Set<Tile> visited = new LinkedHashSet<Tile>();
        Queue<Tile> queue = new LinkedList<Tile>();        
        
        //  No need to access a list - because an intersection can have at max 4 directions to traverse
        //  We further want to "run away" from the starting point, hence we are prohibiting
        //   UP and LEFT directional movement.  Hence, just test the remaining two directions.
        // (1, 1) is always the starting position
        addVertex(mapRef[1][1]);
        queue.add(mapRef[1][1]);
        visited.add(mapRef[1][1]);

        while(!queue.isEmpty())
        {
            Tile currTile = queue.poll();
        
            LinkedList<Tile> neighbourTile = new LinkedList<Tile>();
            int testXPos = currTile.getX();
            int testYPos = currTile.getY();

            // measuring the two weights in the path
            int score = 0;
            int risk = 0;

            // Find neighbouring nodes in DOWN direction
            while(mapRef[++testYPos][testXPos].getTileType() != '#')
            {
                if(mapRef[testYPos][testXPos].getTileType() == 'x' || mapRef[testYPos][testXPos].getTileType() == '$')
                {
                    score += mapRef[testYPos][testXPos].getScorePenalty();                    
                }                               
                else if(mapRef[testYPos][testXPos].getTileType() == 'I' || mapRef[testYPos][testXPos].getTileType() == 'D')
                {
                    // for tracking visited record
                    neighbourTile.add(mapRef[testYPos][testXPos]);

                    addVertex(mapRef[testYPos][testXPos]);
                    addEdge(currTile, mapRef[testYPos][testXPos], score, risk);
                    break;
                }                
            }                
        

            // reset for the next path
            score = 0;
            risk = 0;

            // Then RIGHT direction
            testYPos = currTile.getY();  // remember to reset the test position!!
            while(mapRef[testYPos][++testXPos].getTileType() != '#')
            {
                if(mapRef[testYPos][testXPos].getTileType() == '$' || mapRef[testYPos][testXPos].getTileType() == 'x')
                {
                    score += mapRef[testYPos][testXPos].getScorePenalty();                    
                }               
                else if(mapRef[testYPos][testXPos].getTileType() == 'I' || mapRef[testYPos][testXPos].getTileType() == 'D')
                {
                    // for tracking visited record
                    neighbourTile.add(mapRef[testYPos][testXPos]);

                    addVertex(mapRef[testYPos][testXPos]);
                    addEdge(currTile, mapRef[testYPos][testXPos], score, risk);
                    break;
                }  
            }             
            
            for(Tile t : neighbourTile)
            {
                if(!visited.contains(t))
                {
                    visited.add(t);
                    if(t.getTileType() == 'I')
                    {                        
                        queue.add(t);
                    }
                }
            }                
        }

        printGraph();
    }


    // public static void main(String args[])
    // {
    //     TileGraph myGraph = new TileGraph();
    //     Tile intersections[] = { new Tile(0, 0, 'I', -5), 
    //                              new Tile(4, 0, 'I', -5),
    //                              new Tile(0, 4, 'I', -5),
    //                              new Tile(5, 5, 'I', -5),
    //                              new Tile(5, 10, 'I', -5)};

    //     LinkedList<Tile> recommendedPath;

    //     myGraph.addVertex(intersections[0]);
    //     myGraph.addVertex(intersections[1]);
    //     myGraph.addVertex(intersections[2]);
    //     myGraph.addVertex(intersections[3]);
    //     myGraph.addVertex(intersections[4]);        

    //     myGraph.addEdge(intersections[0], intersections[1], 4);
    //     myGraph.addEdge(intersections[0], intersections[2], 2);
    //     myGraph.addEdge(intersections[4], intersections[1], -8);
    //     myGraph.addEdge(intersections[2], intersections[3], -3);
    //     myGraph.addEdge(intersections[3], intersections[4], 2);
    //     myGraph.addEdge(intersections[1], intersections[3], -3);

    //     myGraph.printGraph();
    //     recommendedPath = myGraph.BellmanShortestPath(intersections[0], intersections[4]);

    //     // so, we print from START to END (reversed)
    //     if(recommendedPath != null)
    //     {
    //         for(int i = 0; i < recommendedPath.size(); i++)
    //         {
    //             recommendedPath.get(i).printTileCoord();
    //             System.out.printf(" => ");
    //         }
    //         System.out.println();
    //     }    
    //     else
    //         System.out.println("No Valid Path");

    // }
}
