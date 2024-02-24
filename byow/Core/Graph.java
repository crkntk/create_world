package byow.Core;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.*;
import java.util.ArrayList;
import java.awt.Point;

    public class Graph implements Iterable<Integer>, Serializable {

        private LinkedList<Edge>[] adjLists;
        private int vertexCount;

        /* Initializes a graph with NUMVERTICES vertices and no Edges. */
        public Graph(int numVertices) {
            adjLists = (LinkedList<Edge>[]) new LinkedList[numVertices];
            for (int k = 0; k < numVertices; k++) {
                adjLists[k] = new LinkedList<Edge>();
            }
            vertexCount = numVertices;
        }

        /* Adds a directed Edge (V1, V2) to the graph. That is, adds an edge
           in ONE directions, from v1 to v2. */
        public void addEdge(int v1, int v2) {
            addEdge(v1, v2, 1);
        }

        /* Adds an undirected Edge (V1, V2) to the graph. That is, adds an edge
           in BOTH directions, from v1 to v2 and from v2 to v1. */
        public void addUndirectedEdge(int v1, int v2) {
            addUndirectedEdge(v1, v2, 1);
        }

        /* Adds a directed Edge (V1, V2) to the graph with weight WEIGHT. If the
           Edge already exists, replaces the current Edge with a new Edge with
           weight WEIGHT. */
        public void addEdge(int v1, int v2, int weight) {
            Edge Add = new Edge(v1, v2, weight);
            if(adjLists[v1] == null){
                adjLists[v1] = new LinkedList<>();
            }
            if(adjLists[v1].contains(Add)){
                adjLists[v1].remove(Add);
                adjLists[v1].add(Add);
                return;
            }
            adjLists[v1].add(Add);
        }

        /* Adds an undirected Edge (V1, V2) to the graph with weight WEIGHT. If the
           Edge already exists, replaces the current Edge with a new Edge with
           weight WEIGHT. */
        public void addUndirectedEdge(int v1, int v2, int weight) {
            // TODO: YOUR CODE HERE
            addEdge(v1, v2, weight);
            addEdge(v2, v1, weight);
        }

        /* Returns true if there exists an Edge from vertex FROM to vertex TO.
           Returns false otherwise. */
        public boolean isAdjacent(int from, int to) {
            // TODO: YOUR CODE HERE
            Iterator it = adjLists[from].iterator();
            while (it.hasNext()){
                Edge nextEdge = (Edge) it.next();
                if (nextEdge.to==to)
                    return true;
            }
            return false;
        }


        /* Returns a list of all the vertices u such that the Edge (V, u)
           exists in the graph. */
        public List<Integer> neighbors(int v) {
            // TODO: YOUR CODE HERE
            List neighborList = new ArrayList();
            for(Edge Curr : adjLists[v]){
                neighborList.add(Curr.to);
            }
            return neighborList;
        }
        /* Returns the number of incoming Edges for vertex V. */
        public int inDegree(int v) {
            // TODO: YOUR CODE HERE
            int count = 0;
            for(LinkedList<Edge> lists : adjLists){
                for(int i = 0; i < lists.size() ; i += 1 ){
                    if(lists.get(i).to == v){
                        count += 1;
                    }
                }
            }
            return count;
        }

        /* Returns an Iterator that outputs the vertices of the graph in topological
           sorted order. */
        public Iterator<Integer> iterator() {
            return new TopologicalIterator();
        }

        /**
         *  A class that iterates through the vertices of this graph,
         *  starting with a given vertex. Does not necessarily iterate
         *  through all vertices in the graph: if the iteration starts
         *  at a vertex v, and there is no path from v to a vertex w,
         *  then the iteration will not include w.
         */
        private class DFSIterator implements Iterator<Integer>, Serializable {

            private Stack<Integer> fringe;
            private HashSet<Integer> visited;

            public DFSIterator(Integer start) {
                fringe = new Stack<>();
                visited = new HashSet<>();
                fringe.push(start);
            }

            public boolean hasNext() {
                if (!fringe.isEmpty()) {
                    int i = fringe.pop();
                    while (visited.contains(i)) {
                        if (fringe.isEmpty()) {
                            return false;
                        }
                        i = fringe.pop();
                    }
                    fringe.push(i);
                    return true;
                }
                return false;
            }

            public Integer next() {
                int curr = fringe.pop();
                ArrayList<Integer> lst = new ArrayList<>();
                for (int i : neighbors(curr)) {
                    lst.add(i);
                }
                lst.sort((Integer i1, Integer i2) -> -(i1 - i2));
                for (Integer e : lst) {
                    fringe.push(e);
                }
                visited.add(curr);
                return curr;
            }

            //ignore this method
            public void remove() {
                throw new UnsupportedOperationException(
                        "vertex removal not implemented");
            }
        }

        /* Returns the collected result of performing a depth-first search on this
           graph's vertices starting from V. */
        public List<Integer> dfs(int v) {
            ArrayList<Integer> result = new ArrayList<Integer>();
            Iterator<Integer> iter = new DFSIterator(v);

            while (iter.hasNext()) {
                result.add(iter.next());
            }
            return result;
        }

        /* Returns true iff there exists a path from START to STOP. Assumes both
           START and STOP are in this graph. If START == STOP, returns true. */
        public boolean pathExists(int start, int stop) {
            List path = dfs(start);
            if(path.contains(stop)){
                return true;
            }
            return false;
        }


        /* Returns the path from START to STOP. If no path exists, returns an empty
           List. If START == STOP, returns a List with START. */
        public List<Integer> path(int start, int stop) {
            List path = new ArrayList<Integer>();
            int stopindex = stop;
            if (isAdjacent(start, stop)) {
                path.add(start);
                path.add(stop);
                return path;
            }
            if (pathExists(start, stop)) {
                ArrayList<Integer> result = new ArrayList<Integer>();
                Iterator<Integer> iter = new DFSIterator(start);
                while (iter.hasNext()) {
                    int ToAdd = iter.next();
                    result.add(ToAdd);
                    if (ToAdd == stop) {
                        break;
                    }
                }
                path.add(stop);
                int ChePathAft = result.size() - 1;
                while(result.get(ChePathAft) != stop){
                    ChePathAft -=1;
                }
                int CheckingBreakage = ChePathAft - 1;
                while (result.get(CheckingBreakage) != start) {
                    if (isAdjacent(result.get(CheckingBreakage), result.get(ChePathAft)) && pathExists(start, CheckingBreakage)) {
                        path.add(result.get(CheckingBreakage));
                        ChePathAft = CheckingBreakage;
                        CheckingBreakage -=1;
                    }
                    else{
                        CheckingBreakage -= 1;
                    }
                }
                path.add(start);
            }
            Collections.reverse(path);
            return path;
        }

        public List<Integer> topologicalSort() {
            ArrayList<Integer> result = new ArrayList<Integer>();
            Iterator<Integer> iter = new TopologicalIterator();
            while (iter.hasNext()) {
                result.add(iter.next());
            }
            return result;
        }

        private class TopologicalIterator implements Iterator<Integer>,Serializable {

            private Stack<Integer> fringe;
            LinkedList<Edge>[] newList;
            Graph newGraph;
            // TODO: Instance variables here!

            TopologicalIterator() {
                this.newGraph = new Graph(vertexCount);
                newGraph.adjLists =  adjLists.clone();
                fringe = new Stack<Integer>();
                for(int i =0 ; i < newGraph.adjLists.length; i+=1){
                    if(adjLists[i] != null && newGraph.inDegree(i) == 0){
                        fringe.push(i);
                    }
                }
            }

            public boolean hasNext() {
                if (!fringe.isEmpty()) {
                    while (this.newGraph.vertexCount > 0) {
                        for (int i = 0; i < newGraph.adjLists.length; i += 1) {
                            if (adjLists[i] != null && newGraph.inDegree(i) == 0) {
                                fringe.push(i);
                            }
                            if (!fringe.isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            public Integer next() {
                if(hasNext()){
                    newGraph.vertexCount -= 1;
                    return fringe.pop();
                }
                // TODO: YOUR CODE HERE
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        private class Edge implements Serializable {

            private int from;
            private int to;
            private int weight;

            Edge(int from, int to, int weight) {
                this.from = from;
                this.to = to;
                this.weight = weight;
            }

            public String toString() {
                return "(" + from + ", " + to + ", weight = " + weight + ")";
            }

        }

        private void generateG1() {
            addEdge(0, 1);
            addEdge(0, 2);
            addEdge(0, 4);
            addEdge(1, 2);
            addEdge(2, 0);
            addEdge(2, 3);
            addEdge(4, 3);
        }

        private void generateG2() {
            addEdge(0, 1);
            addEdge(0, 2);
            addEdge(0, 4);
            addEdge(1, 2);
            addEdge(2, 3);
            addEdge(4, 3);
        }

        private void generateG3() {
            addUndirectedEdge(0, 2);
            addUndirectedEdge(0, 3);
            addUndirectedEdge(1, 4);
            addUndirectedEdge(1, 5);
            addUndirectedEdge(2, 3);
            addUndirectedEdge(2, 6);
            addUndirectedEdge(4, 5);
        }

        private void generateG4() {
            addEdge(0, 1);
            addEdge(1, 2);
            addEdge(2, 0);
            addEdge(2, 3);
            addEdge(4, 2);
        }

        private void printDFS(int start) {
            System.out.println("DFS traversal starting at " + start);
            List<Integer> result = dfs(start);
            Iterator<Integer> iter = result.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next() + " ");
            }
            System.out.println();
            System.out.println();
        }

        private void printPath(int start, int end) {
            System.out.println("Path from " + start + " to " + end);
            List<Integer> result = path(start, end);
            if (result.size() == 0) {
                System.out.println("No path from " + start + " to " + end);
                return;
            }
            Iterator<Integer> iter = result.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next() + " ");
            }
            System.out.println();
            System.out.println();
        }

        private void printTopologicalSort() {
            System.out.println("Topological sort");
            List<Integer> result = topologicalSort();
            Iterator<Integer> iter = result.iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next() + " ");
            }
        }
        public ArrayList<Integer> shortestPath(int start, int stop) {
            HashMap <Integer, Integer> Distance = new HashMap<>();
            PriorityQueue<Integer> fringe = new PriorityQueue((a, b) -> (int) (Distance.get(a) - Distance.get(b)));
            HashSet <Integer> visited = new HashSet<>();
            Distance.put(start, 0);
            fringe.add(start);
            HashMap<Integer, Integer> ShortestMapPRED = new HashMap<>();
            for (int i = 0; i < vertexCount; i++)
                Distance.put(i, Integer.MAX_VALUE);
            while(!fringe.isEmpty()) {
                int currentvisit = fringe.poll();
                visited.add(currentvisit);
                for (int curr : neighbors(currentvisit)) {
                    if (Distance.get(curr) > Distance.get(currentvisit) + getEdge(currentvisit, curr).weight) {
                        Distance.put(curr, Distance.get(currentvisit) + getEdge(currentvisit, curr).weight);
                        ShortestMapPRED.put(curr, currentvisit);
                    }
                    if (!fringe.contains(curr) && !visited.contains(curr)) {
                        fringe.add(curr);
                    }
                }
            }
            ArrayList <Integer> path = new ArrayList<>();
            for(int i = stop; i != start; i = ShortestMapPRED.get(i)){
                path.add(i);
                if(ShortestMapPRED.get(i) == null){
                    break;
                }
            }
            path.add(start);
            Collections.reverse(path);
            return path;
        }


        public Edge getEdge(int u, int v) {
            // TODO: YOUR CODE HERE
            for(Edge curr : adjLists[u] ){
                if(curr.to == v){
                    return curr;
                }
            }
            return null;
        }
        public static void main(String[] args) {
            Graph g1 = new Graph(5);
            g1.generateG1();
            g1.printDFS(0);
            g1.printDFS(2);
            g1.printDFS(3);
            g1.printDFS(4);

            g1.printPath(0, 3);
            g1.printPath(0, 4);
            g1.printPath(1, 3);
            g1.printPath(1, 4);
            g1.printPath(4, 0);
            Graph g = new Graph(7);
            g.addUndirectedEdge(0, 1);
            g.addUndirectedEdge(0, 2);
            g.addUndirectedEdge(1, 3);
            g.addUndirectedEdge(1, 4);
            g.addUndirectedEdge(2, 5);
            g.addUndirectedEdge(2, 6);
            System.out.println("This");
            g.printPath(0,3);
            System.out.println("This");
            g.printPath(0,2);
            System.out.println("This");
            g.printPath(0,2);
            System.out.println("This");
            g.printPath(2,4);





            Graph g2 = new Graph(5);
            g2.generateG2();
            g2.printTopologicalSort();
        }
    }
