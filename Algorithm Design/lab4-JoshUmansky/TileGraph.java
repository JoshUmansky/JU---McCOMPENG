import java.util.*;

// This is the upgraded TileGraph from Lab 3 with weighted edges and directivity selection

class WeightedEdge {
    
    private Tile myTile;
    private int penalty;

    WeightedEdge(Tile thisTile, int pen)
    {
        myTile = thisTile;
        penalty = pen;
    }

    public Tile getTile() { return myTile; }
    public int getPenalty() { return penalty; }

    public boolean hasTile(Tile thisTile)
    {
        return myTile.equals(thisTile);  // shallow comparison by reference only
    }

    public boolean isEqual(WeightedEdge thisWEdge)
    {
        boolean result = myTile.isEqual(thisWEdge.getTile());
        result &= (penalty == thisWEdge.getPenalty());
        return result;
    }
}

public class TileGraph {
        
    private Map<Tile, LinkedList<WeightedEdge>> adjList;

    public TileGraph()
    {
        adjList = new HashMap<>();
    }

    public TileGraph(TileMap thisMap)
    {
        adjList = new HashMap<>();
        buildGraph(thisMap);
    }

    public void addVertex(Tile thisTile)
    {
        adjList.putIfAbsent(thisTile, new LinkedList<WeightedEdge>());
    }

    //Function from Lab 3 changed to allow for weightededges
    //time complexity: O(1)
    //space complexity: O(1)
    public void addEdge(Tile src, Tile dst, int penalty)
    {   
        // Implement the weighted edge insertion method
        LinkedList<WeightedEdge> srcList = adjList.get(src); 
        srcList.add(new WeightedEdge(dst, penalty));
        
    }
    //Function to verify topSort
    public static int getTileIndex(Tile[] tileArray, Tile tile) {
        for (int i = 0; i < tileArray.length; i++) {
            if (tileArray[i].isEqual(tile)) {
                return i;
            }
        }
        return -1; // Return -1 if the tile is not found
    }
    
    //Function from Lab 3 changed to allow for weightededges
    //Time Complexity: O(V + E)
    //Space Complexity: O(V + E)
    private LinkedList<Tile> getAdjacentVertices(Tile thisTile) {
        LinkedList<Tile> adjVertices = new LinkedList<Tile>();
        LinkedList<WeightedEdge> edges = adjList.get(thisTile);
        if (edges != null) {
            for (WeightedEdge edge : edges) {
                adjVertices.add(edge.getTile());
            }
        }
        return adjVertices;
    }

    // returns the path with the smallest number of edges
    // Mostly from Lab3
    // time complexity: O(V + E)
    // space complexity: O(V)
    private LinkedList<Tile> UnweightedShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> path = new LinkedList<Tile>();  // front end to start

        Queue<Tile> queue = new LinkedList<Tile>();
        Set<Tile> visitedSet = new HashSet<Tile>();
        Map<Tile, Tile> parentMap = new HashMap<Tile, Tile>();//parent pointers for the BFS tree
                
        queue.add(start);
        visitedSet.add(start);

        while(!queue.isEmpty())
        {
            Tile current = queue.remove();
            if(current.isEqual(end))
            {
                //FOUND                
                Tile backTrack = current;
                while(!backTrack.isEqual(start))
                {
                    path.addFirst(backTrack);
                    backTrack = parentMap.get(backTrack);
                }
                path.addFirst(start);
                Collections.reverse(path);
                
                return path;
            }
            //Not found, look at adj verticies
            for(Tile adj : getAdjacentVertices(current))
            {
                if(!visitedSet.contains(adj))
                {
                    queue.add(adj);
                    visitedSet.add(adj);
                    parentMap.put(adj, current);
                }
            }
        }
        return path;
    }


    // dijkastra for path with lowest penalties
    // time complexity: O(V^2)
    // space complexity: O(V)
    private LinkedList<Tile> DijkstraShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        Map<Tile, Integer> distance = new HashMap<>();
        Map<Tile, Tile> parentMap = new HashMap<>();
        PriorityQueue<Tile> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        //Init vertex's to infinite distance
        for (Tile tile : adjList.keySet()) {
            distance.put(tile, Integer.MAX_VALUE);
        }
        distance.put(start, 0); //start
        priorityQueue.add(start);

        
        while (!priorityQueue.isEmpty()) {
            Tile current = priorityQueue.poll();

            //Check path length, update if shorter
            LinkedList<WeightedEdge> edges = adjList.get(current);
            if (edges != null) {
                for (WeightedEdge edge : edges) {
                    Tile neighbor = edge.getTile();
                    int newDist = distance.get(current) + edge.getPenalty();
                    if (newDist < distance.get(neighbor)) {
                        distance.put(neighbor, newDist);
                        parentMap.put(neighbor, current);
                        priorityQueue.add(neighbor);
                    }
                }
            }
        }

        //Go backwards, create path
        Tile current = end;
        while (current != null && parentMap.containsKey(current)) {
            shortestPath.add(current);
            current = parentMap.get(current);
        }
        if (current == start) {
            shortestPath.add(start);
        } else {
            System.out.println("No path found from start to end.");
            return new LinkedList<>(); // Return an empty list if no path found
        }
        
        return shortestPath;
        
    }


    // Bellman-Ford Lowest Penalty Path - returns null if negative weight cycles are detected
    // time complexity: O(V * E)
    // space complexity: O(V)
    private LinkedList<Tile> BellmanShortestPath(Tile start, Tile end)
    {
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        Map<Tile, Integer> distance = new HashMap<>();
        Map<Tile, Tile> parentMap = new HashMap<>();
    
        //Same Init as Dijkastra
        for (Tile tile : adjList.keySet()) {
            distance.put(tile, Integer.MAX_VALUE);
        }
        distance.put(start, 0);
    
        //Relax all edges |V| - 1 times
        int V = adjList.size();
        for (int i = 0; i < V - 1; i++) {
            for (Tile u : adjList.keySet()) {
                LinkedList<WeightedEdge> edges = adjList.get(u);
                if (edges != null) {
                    for (WeightedEdge edge : edges) {
                        Tile v = edge.getTile();
                        int weight = edge.getPenalty();
                        if (distance.get(u) != Integer.MAX_VALUE && distance.get(u) + weight < distance.get(v)) {
                            distance.put(v, distance.get(u) + weight);
                            parentMap.put(v, u);
                        }
                    }
                }
            }
        }
    
        //Negative-Weight Cycle Check
        for (Tile u : adjList.keySet()) {
            LinkedList<WeightedEdge> edges = adjList.get(u);
            if (edges != null) {
                for (WeightedEdge edge : edges) {
                    Tile v = edge.getTile();
                    int weight = edge.getPenalty();
                    if (distance.get(u) != Integer.MAX_VALUE && distance.get(u) + weight < distance.get(v)) {
                        System.out.println("Graph contains a negative-weight cycle.");
                        return null; // Return null if a negative-weight cycle is detected
                    }
                }
            }
        }
    
        //Go backwards, create path
        Tile current = end;
        while (current != null && parentMap.containsKey(current)) {
            shortestPath.add(current);
            current = parentMap.get(current);
        }
        if (current == start) {
            shortestPath.add(start);
        } else {
            System.out.println("No path found from start to end.");
            return new LinkedList<>(); // Return an empty list if no path found
        }
        return shortestPath;
    }
    
    // DAG Shortest Path
    // time complexity: O(V + E)
    // space complexity: O(V)
    private LinkedList<Tile> DAGShortestPath(Tile start, Tile end){        
        
        LinkedList<Tile> shortestPath = new LinkedList<Tile>();
        Map<Tile, Integer> distance = new HashMap<>();
        Map<Tile, Tile> parentMap = new HashMap<>();

        //Topo sort
        LinkedList<Tile> topologicalOrder = topologicalSort();
        if (topologicalOrder.isEmpty()) {
            System.out.println("Graph has a cycle, shortest path not possible.");
            return shortestPath; // Return an empty list if there is a cycle
        }

        //Init
        for (Tile tile : adjList.keySet()) {
            distance.put(tile, Integer.MAX_VALUE);
        }
        distance.put(start, 0);

        //Loop through topological order, update distances
        for (Tile current : topologicalOrder) {
            if (distance.get(current) != Integer.MAX_VALUE) {
                LinkedList<WeightedEdge> edges = adjList.get(current);
                if (edges != null) {
                    for (WeightedEdge edge : edges) {
                        Tile neighbor = edge.getTile();
                        int newDist = distance.get(current) + edge.getPenalty();
                        if (newDist < distance.get(neighbor)) {
                            distance.put(neighbor, newDist);
                            parentMap.put(neighbor, current);
                        }
                    }
                }
            }
        }

        //Go backwards, create path
        Tile current = end;
        while (current != null && parentMap.containsKey(current)) {
            shortestPath.add(current);
            current = parentMap.get(current);
        }
        if (current == start) {
            shortestPath.add(start);
        } else {
            System.out.println("No path found from start to end.");
            return new LinkedList<>(); // Return an empty list if no path found
        }

        return shortestPath;
    }
    
    // Kahn's Topological Sorting
    // time complexity: O(V + E)
    // space complexity: O(V)
    private LinkedList<Tile> topologicalSort()  // helper method for DAG Shortest Path
    {
        LinkedList<Tile> sortedList = new LinkedList<>();
        Map<Tile, Integer> inDegree = new HashMap<>();

        // Initialize in-degree of all vertices to 0
        for (Tile tile : adjList.keySet()) {
            inDegree.put(tile, 0);
        }

        // Calculate in-degree of each vertex
        for (LinkedList<WeightedEdge> edges : adjList.values()) {
            for (WeightedEdge edge : edges) {
                Tile neighbor = edge.getTile();
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
        }

        // Queue for vertices with in-degree 0
        Queue<Tile> queue = new LinkedList<>();
        for (Map.Entry<Tile, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // Process vertices with in-degree 0
        while (!queue.isEmpty()) {
            Tile current = queue.poll();
            sortedList.add(current);

            // Decrease in-degree of neighbors
            LinkedList<WeightedEdge> edges = adjList.get(current);
            if (edges != null) {
                for (WeightedEdge edge : edges) {
                    Tile neighbor = edge.getTile();
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Check if there was a cycle
        if (sortedList.size() != adjList.size()) {
            System.out.println("Graph has a cycle, topological sort not possible.");
            return new LinkedList<>(); // Return an empty list if there is a cycle
        }

        return sortedList;
    }

    public LinkedList<Tile> findShortestPath(Tile start, Tile end, int complexity)
    {
        switch(complexity)
        {
            default:
            case 0:
                return UnweightedShortestPath(start, end);  // Lab 3
             
            case 1:
                return DAGShortestPath(start, end);    // DAG
                
            case 2:
                return DijkstraShortestPath(start, end);    // Dijkstra

            case 3:
                return BellmanShortestPath(start, end);    // Bellman
        }
    }

    public void printGraph()
    {
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

    //Private Method to calculate the edge weight of a given path
    private int calculateEdgeWeight(Tile[][] map, int row, int col) {
        int weight = 0;
        char tileType = map[row][col].getTileType();
        if (tileType == 'x') {
            weight += 5;
        } else if (tileType == '$') {
            weight -= 2;
        }
        return weight;
    }

    private void buildGraph(TileMap mapRef)
    {
        Tile[][] map = mapRef.getMapRef();

        int rows = map.length;
        int cols = map[0].length;

        Queue<Tile> queue = new LinkedList<>();
        Set<Tile> visited = new HashSet<>();

        Tile start = map[1][1];
        queue.add(start); // queue for BFS
        visited.add(start); // list of visited vertices
        addVertex(start);

        while (!queue.isEmpty()) {
            Tile current = queue.poll();
            int x = current.getX();
            int y = current.getY();

            // Check adjacent tiles (right, down)
            if (x + 1 < cols && map[y][x+1].getTileType() != '#') { // right
                Tile rightTile = map[y][x + 1];
                if (!visited.contains(rightTile)) {
                    addVertex(rightTile);
                    int weight = calculateEdgeWeight(map, y, x+1);
                    addEdge(current, rightTile, weight);
                    queue.add(rightTile);
                    visited.add(rightTile);
                }
            }

            if (y + 1 < rows && map[y+1][x].getTileType() != '#') { // down
                Tile downTile = map[y + 1][x];
                if (!visited.contains(downTile)) {
                    addVertex(downTile);
                    int weight = calculateEdgeWeight(map, y+1, x);
                    addEdge(current, downTile, weight);
                    queue.add(downTile);
                    visited.add(downTile);
                }
            }
        }
    }
    
    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {        
        testAddWeightedEdge1();
        testAddWeightedEdge2();
        testAddWeightedEdgeCustom();

        //testTopologicalSort1(); All passed, confirmed with custom function to print the vertex index of each tile
        //testTopologicalSort2(); and the information that the professor posted on avenue
        //testTopologicalSortCustom();

        testShortestPathDAG1();
        testShortestPathDAG2();
        testShortestPathDAGCustom();

        testShortestPathDijkastra1();
        testShortestPathDijkastra2();
        testShortestPathDijkastraCustom();

        testShortestPathBellmanFord1();
        testShortestPathBellmanFord2();
        testNegativeCycleBellmanFord1();
        testNegativeCycleBellmanFord2();
        testShortestPathBellmanFordCustom();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n", 
                          totalPassCount, 
                          totalTestCount);
        if(totalPassed)  
        {
            System.out.println("All Tests Passed.");
            System.exit(0);
        }
        else
        {   
            System.out.println("Tests Failed.");
            System.exit(-1);
        }        
    }


    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)

    private static void testAddWeightedEdge1()
    {
        // Setup
        System.out.println("============testAddWeightedEdge1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<WeightedEdge> tempList;              
        boolean tempResult;
        
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);                
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
        }        

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);              
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[5]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);                      
        passed &= assertEquals(1, tempList.size());
        
        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testAddWeightedEdge2()
    {
        // Setup
        System.out.println("============testAddWeightedEdge2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<WeightedEdge> tempList;              
        boolean tempResult;
        
        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);                
        passed &= assertEquals(3, tempList.size());      
        
        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[3]) || 
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) || 
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) || 
                         testWEdge.isEqual(wEdge[1]) || 
                         testWEdge.isEqual(wEdge[2]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
        }        

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);              
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[6]))
                passed &= assertEquals(wEdge[6].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);              
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[7]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[7]))
                passed &= assertEquals(wEdge[7].getPenalty(), testWEdge.getPenalty());
            
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);                      
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[7]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[8]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[8]))
                passed &= assertEquals(wEdge[8].getPenalty(), testWEdge.getPenalty());
            
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[5].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[5]);                      
        passed &= assertEquals(2, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) || 
                         testWEdge.isEqual(wEdge[3]) ||
                         testWEdge.isEqual(wEdge[4]) ||
                         testWEdge.isEqual(wEdge[5]) || 
                         testWEdge.isEqual(wEdge[6]) ||
                         testWEdge.isEqual(wEdge[7]) || 
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[11]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[9]))
                passed &= assertEquals(wEdge[9].getPenalty(), testWEdge.getPenalty());
            else if(testWEdge.isEqual(wEdge[10]))
                passed &= assertEquals(wEdge[10].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[6]);                      
        passed &= assertEquals(1, tempList.size());

        for(WeightedEdge testWEdge : tempList)
        {
            tempResult = testWEdge.isEqual(wEdge[0]) ||  
                         testWEdge.isEqual(wEdge[1]) ||  
                         testWEdge.isEqual(wEdge[2]) ||
                         testWEdge.isEqual(wEdge[3]) ||                          
                         testWEdge.isEqual(wEdge[4]) || 
                         testWEdge.isEqual(wEdge[5]) ||
                         testWEdge.isEqual(wEdge[6]) || 
                         testWEdge.isEqual(wEdge[7]) ||
                         testWEdge.isEqual(wEdge[8]) ||
                         testWEdge.isEqual(wEdge[9]) ||
                         testWEdge.isEqual(wEdge[10]);
            
            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(true, tempResult);

            if(testWEdge.isEqual(wEdge[11]))
                passed &= assertEquals(wEdge[11].getPenalty(), testWEdge.getPenalty());
            
        } 

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[7].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[7]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    //Just a function to check if the adjacency list is correct
    private static boolean checkAdjacencyList(TileGraph graph, Tile[] tiles, WeightedEdge[] edges) {
        boolean result = true;
        for (int i = 0; i < tiles.length; i++) {
            LinkedList<WeightedEdge> list = graph.adjList.get(tiles[i]);
            for (int j = 0; j < list.size(); j++) {
                WeightedEdge we = list.get(j);
                boolean matchFound = false;
                for (WeightedEdge edge : edges) {
                    if (we.isEqual(edge) && we.getTile().equals(edge.getTile()) && we.getPenalty() == edge.getPenalty()) {
                        matchFound = true;
                        break;
                    }
                }
                result &= matchFound;
            }
        }
        return result;
    }

    private static void testAddWeightedEdgeCustom()
    {
        // Setup
        System.out.println("============testAddWeightedEdgeCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile[] tileArray = {
            new Tile(0, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(4, 0, 'I', -5),
            new Tile(4, 8, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5),
            new Tile(10, 16, 'I', -5),
            new Tile(10, 23, 'I', -5)
        };

        WeightedEdge[] wEdge = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for (Tile tile : tileArray) {
            testGraph.addVertex(tile);
        }

        // Adding edges
        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Check adjacency lists and validate edges
        passed &= checkAdjacencyList(testGraph, tileArray, wEdge);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }
    

    // Topological Sort
    // Topological Sort
    // Topological Sort

    private static void testTopologicalSort1()
    {
        // Setup
        System.out.println("============testTopologicalSort1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        /*for(Tile tile : tileArray){
            System.out.print(getTileIndex(tileArray, tile));
            System.out.print(" ");
        }
        System.out.println();*/
            

        if(sorted != null)
        {
            
            passed &= assertEquals(tileArray[0], sorted.get(0));
            passed &= assertEquals(tileArray[2], sorted.get(1));
            passed &= assertEquals(tileArray[1], sorted.get(2));
            passed &= assertEquals(tileArray[3], sorted.get(3));
            passed &= assertEquals(tileArray[4], sorted.get(4));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testTopologicalSort2()
    {
        // Setup
        System.out.println("============testTopologicalSort2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        /*for(Tile tile : tileArray){
            System.out.print(getTileIndex(tileArray, tile));
            System.out.print(" ");
        }
        System.out.println();*/

        if(sorted != null)
        {
            passed &= assertEquals(tileArray[0], sorted.get(0));
            passed &= assertEquals(tileArray[2], sorted.get(1));
            passed &= assertEquals(tileArray[1], sorted.get(2));
            passed &= assertEquals(tileArray[3], sorted.get(3));
            passed &= assertEquals(tileArray[4], sorted.get(4));        
            passed &= assertEquals(tileArray[6], sorted.get(5));
            passed &= assertEquals(tileArray[5], sorted.get(6));
            passed &= assertEquals(tileArray[7], sorted.get(7));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testTopologicalSortCustom()
    {
        // Setup
        System.out.println("============testTopologicalSortCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile[] tileArray = {
            new Tile(0, 0, 'I', -5),
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge[] wEdge = {
            new WeightedEdge(tileArray[1], 4), // Edge from 0 to 1
            new WeightedEdge(tileArray[2], 2), // Edge from 0 to 2
            new WeightedEdge(tileArray[4], 8), // Edge from 1 to 4
            new WeightedEdge(tileArray[3], 7), // Edge from 1 to 3
            new WeightedEdge(tileArray[3], 2), // Edge from 2 to 3
            new WeightedEdge(tileArray[4], 1)  // Edge from 3 to 4
        };

        for (Tile tile : tileArray) {
            testGraph.addVertex(tile);
        }

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if (sorted != null) {
            passed &= assertEquals(tileArray[0], sorted.get(0));
            passed &= assertEquals(tileArray[2], sorted.get(1));
            passed &= assertEquals(tileArray[1], sorted.get(2));
            passed &= assertEquals(tileArray[3], sorted.get(3));
            passed &= assertEquals(tileArray[4], sorted.get(4));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }
    

    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort

    private static void testShortestPathDAG1()
    {
        // Setup
        System.out.println("============testShortestPathDAG1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[4], 8), //1
            new WeightedEdge(tileArray[3], 7), //1
            new WeightedEdge(tileArray[3], 2), //2
            new WeightedEdge(tileArray[4], 1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[2], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDAG2()
    {
        // Setup
        System.out.println("============testShortestPathDAG2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 2), //0
            new WeightedEdge(tileArray[3], 8), //0
            new WeightedEdge(tileArray[4], 3), //1
            new WeightedEdge(tileArray[5], 1), //1            
            new WeightedEdge(tileArray[3], 4), //2
            new WeightedEdge(tileArray[4], 2), //2
            new WeightedEdge(tileArray[6], 6), //3
            new WeightedEdge(tileArray[5], 5), //4
            new WeightedEdge(tileArray[6], 1), //5            
            new WeightedEdge(tileArray[7], 3), //5
            new WeightedEdge(tileArray[7], 1)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[5], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));        
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDAGCustom()
    {
        // Setup
        System.out.println("============testShortestPathDAGCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0, 0, 'I', -5),
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5),
            new Tile(10, 0, 'I', -5),
            new Tile(10, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), // Edge from 0 to 1
            new WeightedEdge(tileArray[2], 2), // Edge from 0 to 2
            new WeightedEdge(tileArray[4], 8), // Edge from 1 to 4
            new WeightedEdge(tileArray[3], 7), // Edge from 1 to 3
            new WeightedEdge(tileArray[3], 2), // Edge from 2 to 3
            new WeightedEdge(tileArray[4], 1), // Edge from 3 to 4
            new WeightedEdge(tileArray[5], 3), // Edge from 0 to 5
            new WeightedEdge(tileArray[6], 6)  // Edge from 5 to 6
        };

        for (int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);
        
        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[7].getTile(), wEdge[7].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[6]);

        if (path != null) {
            passed &= assertEquals(tileArray[6], path.get(0));
            passed &= assertEquals(tileArray[5], path.get(1));
            passed &= assertEquals(tileArray[0], path.get(2));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }
    

    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph

    private static void testShortestPathDijkastra1()
    {
        // Setup
        System.out.println("============testShortestPathDijkastra1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 4), //0
            new WeightedEdge(tileArray[2], 6), //0
            new WeightedEdge(tileArray[4], 5), //1
            new WeightedEdge(tileArray[3], 1), //1
            new WeightedEdge(tileArray[3], 10), //2
            new WeightedEdge(tileArray[4], 7)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[1], path.get(1));
            passed &= assertEquals(tileArray[0], path.get(2));
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDijkastra2()
    {
        // Setup
        System.out.println("============testShortestPathDijkastra2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], 7), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], 2), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[7], 6), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[7]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[3], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));   
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathDijkastraCustom()
    {
        // Setup
        System.out.println("============testShortestPathDijkastraCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0, 0, 'I', -5),
            new Tile(2, 2, 'I', -5),
            new Tile(4, 0, 'I', -5),
            new Tile(6, 6, 'I', -5),
            new Tile(5, 3, 'I', -5),
            new Tile(7, 2, 'I', -5),
            new Tile(9, 9, 'I', -5),
            new Tile(10, 4, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 3), // Edge from 0 to 1
            new WeightedEdge(tileArray[2], 6), // Edge from 0 to 2
            new WeightedEdge(tileArray[3], 9), // Edge from 0 to 3
            new WeightedEdge(tileArray[4], 5), // Edge from 1 to 4
            new WeightedEdge(tileArray[5], 7), // Edge from 1 to 5
            new WeightedEdge(tileArray[3], 4), // Edge from 2 to 3
            new WeightedEdge(tileArray[4], 3), // Edge from 2 to 4
            new WeightedEdge(tileArray[6], 2), // Edge from 3 to 6
            new WeightedEdge(tileArray[5], 1), // Edge from 4 to 5
            new WeightedEdge(tileArray[6], 3), // Edge from 5 to 6
            new WeightedEdge(tileArray[7], 4), // Edge from 5 to 7
            new WeightedEdge(tileArray[7], 6)  // Edge from 6 to 7
        };

        for (int i = 0; i < tileArray.length; i++)
            testGraph.addVertex(tileArray[i]);
        
        for (int i = 0; i < wEdge.length; i++)
            testGraph.addEdge(tileArray[i / 3], wEdge[i].getTile(), wEdge[i].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[7]);

        if (path != null) {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[1], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }
    

    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph

    private static void testShortestPathBellmanFord1()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 2), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[3], -4), //1
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], -1)  //3
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);
        
        if(path != null)
        {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[1], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        }
        else
        {
            passed = false;
        }   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathBellmanFord2()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFord2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], -2), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], -4), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[7], -1), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);

        if(path != null)
        {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[5], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));   
            passed &= assertEquals(tileArray[0], path.get(4));   
        }
        else
        {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testNegativeCycleBellmanFord1()
    {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { 
            new Tile(0, 0, 'I', -5), 
            new Tile(4, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 2), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[3], -4), //1
            new WeightedEdge(tileArray[3], 3), //2
            new WeightedEdge(tileArray[4], -1),  //3
            new WeightedEdge(tileArray[1], -4)  //4
        };

        for(int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);
            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[6].getTile(), wEdge[6].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);        
        passed &= assertEquals(true, path == null);
        
        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }

    private static void testNegativeCycleBellmanFord2()
    {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] =  { new Tile(0, 0, 'I', -5), 
                              new Tile(0, 4, 'I', -5),
                              new Tile(4, 0, 'I', -5),
                              new Tile(4, 8, 'I', -5),                              
                              new Tile(5, 5, 'I', -5),
                              new Tile(5, 10, 'I', -5),
                              new Tile(10, 16, 'I', -5), 
                              new Tile(10, 23, 'I', -5)};

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), //0
            new WeightedEdge(tileArray[2], 4), //0
            new WeightedEdge(tileArray[3], 2), //0
            new WeightedEdge(tileArray[4], -3), //1
            new WeightedEdge(tileArray[5], 11), //1            
            new WeightedEdge(tileArray[3], -2), //2
            new WeightedEdge(tileArray[4], 5), //2
            new WeightedEdge(tileArray[6], 1), //3
            new WeightedEdge(tileArray[5], -4), //4
            new WeightedEdge(tileArray[6], 7), //5            
            new WeightedEdge(tileArray[1], 2), //5  
            new WeightedEdge(tileArray[7], -1), //5
            new WeightedEdge(tileArray[7], 10)  //6
        };

        for(int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);            

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[11].getTile(), wEdge[11].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[12].getTile(), wEdge[12].getPenalty());

        
        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);
        passed &= assertEquals(true, path == null);
        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testShortestPathBellmanFordCustom()
    {
        // Setup
        System.out.println("============testShortestPathBellmanFordCustom=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
            new Tile(0, 0, 'I', -5),
            new Tile(0, 4, 'I', -5),
            new Tile(4, 0, 'I', -5),
            new Tile(4, 8, 'I', -5),
            new Tile(5, 5, 'I', -5),
            new Tile(5, 10, 'I', -5),
            new Tile(10, 16, 'I', -5),
            new Tile(10, 23, 'I', -5)
        };

        WeightedEdge wEdge[] = {
            new WeightedEdge(tileArray[1], 5), // Edge from 0 to 1
            new WeightedEdge(tileArray[2], 4), // Edge from 0 to 2
            new WeightedEdge(tileArray[3], 2), // Edge from 0 to 3
            new WeightedEdge(tileArray[4], -3), // Edge from 1 to 4
            new WeightedEdge(tileArray[5], 11), // Edge from 1 to 5
            new WeightedEdge(tileArray[3], -2), // Edge from 2 to 3
            new WeightedEdge(tileArray[4], 5), // Edge from 2 to 4
            new WeightedEdge(tileArray[6], 1), // Edge from 3 to 6
            new WeightedEdge(tileArray[5], -4), // Edge from 4 to 5
            new WeightedEdge(tileArray[6], 7), // Edge from 5 to 6
            new WeightedEdge(tileArray[7], -1), // Edge from 5 to 7
            new WeightedEdge(tileArray[7], 10)  // Edge from 6 to 7
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);

        if (path != null) {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[5], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }
    

    


    ////// ASSERTIONS //////
    ////// ASSERTIONS //////
    ////// ASSERTIONS //////

    private static boolean assertEquals(Tile expected, Tile actual)
    {
        if(!expected.isEqual(actual))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected:");
            expected.printTile();
            expected.printTileCoord();
            System.out.printf("\tActual:");
            actual.printTile();
            actual.printTileCoord();
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean expected, boolean actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", expected, actual);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(int expected, int actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", expected, actual);
            return false;
        }

        return true;
    }
}
