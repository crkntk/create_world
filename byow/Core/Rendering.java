package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.Random;
import byow.Core.Graph;
import java.time.Instant;
import java.util.function.IntToDoubleFunction;
import java.time.Instant;
import byow.Core.RandomUtils;
import byow.Core.Room;
import edu.princeton.cs.algs4.StdDraw;



public class Rendering implements Serializable {
    public static final int WIDTH = 75;
    public static final int HEIGHT = 43 ;
    public long SEED;


    private static final int MIN_NUM_ROOMS = 15;
    private static final int MAX_NUM_ROOMS = 30;
    private static final int MIN_ROOM_WIDTH = 4;
    private static final int MAX_ROOM_WIDTH = 12;
    private static final int MIN_ROOM_HEIGHT = 4;
    private static final int MAX_ROOM_HEIGHT = 12;
    private static final int HUD_SPACE = 5;
    public static final int AmountFlowers = 30 ;
    public Avatar MainCharacter;
    public Avatar EvilSquirrel;
    TETile[][] world;
    TERenderer ter;
    public int GoalCounter;
    int TrackingMainX;
    int TrackingMainY;
    int EvilSquirrelTrkX;
    int EvilSquirrelTrkY;
    int MainSQDist;
    boolean Win;
    Graph MasterGraph;
    HashMap<Integer, Point> GraphToVertex;
    ArrayList<Room> rooms;
    List<Room> roomsMaster;
    int EvilSpeed;
    int EvilUpdate;

    public Rendering(long SEEDINPUT, boolean Show){
        this.EvilSpeed = 1000;
        this.EvilUpdate = 1050;
        this.Win = false;
        this.SEED = SEEDINPUT;
        this.ter = new TERenderer();
        if(Show == true) {
            ter.initialize(WIDTH, HEIGHT + 2);
        }
        GoalCounter = 0;
        // initialize tiles
        this.world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        // make rooms
        this.rooms = new ArrayList<>();
        Random r = new Random(SEED);
        int numRooms = MIN_NUM_ROOMS + r.nextInt(MAX_NUM_ROOMS - MIN_NUM_ROOMS);
        int i = 0;
        while (i < numRooms) {
            // find a place in the map with enough room for a room
            int x = r.nextInt(WIDTH - MIN_ROOM_WIDTH);
            int y = r.nextInt(HEIGHT - MIN_ROOM_HEIGHT - HUD_SPACE);

            // find the dimensions for the room
            int h = Math.min(HEIGHT - y, MIN_ROOM_HEIGHT
                    + r.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT));
            int w = Math.min(WIDTH - x, MIN_ROOM_WIDTH
                    + r.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH));
            Room room = new Room(h, w, x, y);

            if (!isOverlap(room, world)) {
                drawRoom(room, world);
                rooms.add(room);
                i++;
            }
        }

        UnionFind uf = new UnionFind(rooms.size());

        this.roomsMaster = new ArrayList<>(rooms.size());
        roomsMaster.addAll(rooms);

        Room virtualMiddle = new Room(0, 0, WIDTH / 2, HEIGHT / 2);
        sortByDistTo(roomsMaster, virtualMiddle);

        for (int roomIndex1 = 0; roomIndex1 < roomsMaster.size(); roomIndex1++) {
            sortByDistTo(rooms, roomsMaster.get(roomIndex1));
            int numConnections = 0;
            for (int roomIndex2 = 1; roomIndex2 < rooms.size(); roomIndex2++) {
                int masterIndex1 = roomIndex1;
                int masterIndex2 = roomsMaster.indexOf(rooms.get(roomIndex2));
                if (uf.isConnected(masterIndex1, masterIndex2)) {
                    continue;
                }
                connect(roomsMaster.get(masterIndex1), roomsMaster.get(masterIndex2), world);
                uf.connect(masterIndex1, masterIndex2);
                numConnections++;
                if (numConnections > 0) {
                    break;
                }
            }
        }
        int RandomAvR = r.nextInt(rooms.size());
        Room RandomRoom = rooms.get(RandomAvR);
        int Avatarx = RandomUtils.uniform(r,RandomRoom.getX(),RandomRoom.getX() + RandomRoom.getWidth());
        int Avatary = RandomUtils.uniform(r,RandomRoom.getY(),RandomRoom.getY() + RandomRoom.getHeight());
        TrackingMainX = Avatarx;
        TrackingMainY = Avatary;
        world[Avatarx][Avatary] = Tileset.Knight;
        this.MainCharacter = new Avatar(Avatarx,Avatary, Tileset.Knight);
        RandomAvR = r.nextInt(rooms.size());
        RandomRoom = rooms.get(RandomAvR);
        int EvilSQx = RandomUtils.uniform(r,RandomRoom.getX(),RandomRoom.getX() + RandomRoom.getWidth());
        int EvilSQy = RandomUtils.uniform(r,RandomRoom.getY(),RandomRoom.getY() + RandomRoom.getHeight());
        EvilSquirrelTrkX = EvilSQx;
        EvilSquirrelTrkY = EvilSQy;
        world[EvilSQx][EvilSQy] = Tileset.EvilSquirell;
        this.EvilSquirrel = new Avatar(EvilSQx,EvilSQy, Tileset.EvilSquirell);
        int Flowercount = 0;
        HashSet RoomsFlowersin = new HashSet();
        while(Flowercount < AmountFlowers){
            RandomAvR = r.nextInt(rooms.size());
            RandomRoom = rooms.get(RandomAvR);
            int FlowerXpos = RandomUtils.uniform(r,RandomRoom.getX(),RandomRoom.getX() + RandomRoom.getWidth());
            int flowerYpos = RandomUtils.uniform(r,RandomRoom.getY(),RandomRoom.getY() + RandomRoom.getHeight());
            if(world[FlowerXpos][flowerYpos] != Tileset.FLOWER && !RoomsFlowersin.contains(RandomRoom)) {
                world[FlowerXpos][flowerYpos] = Tileset.FLOWER;
                Flowercount+=1;
            }
        }
        this.GraphToVertex = ScanVert();
        this.MasterGraph = new Graph(GraphToVertex.size());
        /** add Edges**/
        for(int curr: GraphToVertex.keySet()){
            ConnectEdge(curr);
        }
        if(Show == true) {
            ter.renderFrame(world);
        }
    }
    public void Win(){
        return;
    }

    public void ConnectEdge(int curr ){
        Point CurrentPoint = FindPointToVertex(curr);
        if(CurrentPoint.x - 1>=0 && this.world[CurrentPoint.x - 1][CurrentPoint.y] != Tileset.NOTHING && this.world[CurrentPoint.x - 1][CurrentPoint.y] != Tileset.WALL ) {
            MasterGraph.addEdge(curr,FindVertexToPoint(CurrentPoint.x - 1,CurrentPoint.y));
        }
        if(CurrentPoint.x + 1 < WIDTH &&this.world[CurrentPoint.x + 1][CurrentPoint.y] != Tileset.NOTHING && this.world[CurrentPoint.x + 1][CurrentPoint.y] != Tileset.WALL) {
            MasterGraph.addEdge(curr,FindVertexToPoint(CurrentPoint.x + 1,CurrentPoint.y));
        }
        if(CurrentPoint.y - 1 >= 0 && this.world[CurrentPoint.x][CurrentPoint.y - 1] != Tileset.NOTHING && this.world[CurrentPoint.x][CurrentPoint.y- 1] != Tileset.WALL) {
            MasterGraph.addEdge(curr,FindVertexToPoint(CurrentPoint.x,CurrentPoint.y-1));

        }
        if(CurrentPoint.y + 1 < HEIGHT && this.world[CurrentPoint.x][CurrentPoint.y + 1] != Tileset.NOTHING && this.world[CurrentPoint.x][CurrentPoint.y+1] != Tileset.WALL) {
            MasterGraph.addEdge(curr,FindVertexToPoint(CurrentPoint.x,CurrentPoint.y+1));
        }
    }



    public Point FindPointToVertex(int index){
        for(int curr : GraphToVertex.keySet()){
            if(curr == index){
                return GraphToVertex.get(curr);
            }
        }
        return null;
    }

    public int FindVertexToPoint(int x, int y){
        for(int curr : GraphToVertex.keySet()){
            if(GraphToVertex.get(curr).x == x && GraphToVertex.get(curr).y == y){
                return curr;
            }
        }
        return -10;
    }
    public double Distance(int x1i, int y1i, int x2i, int y2i){
        double x1 = (double) x1i;
        double x2 = (double) x2i;
        double y1 = (double) y1i;
        double y2 = (double) y2i;
        return Math.sqrt(((x1 -x2)*(x1 -x2))+((y1 -y2) *(y1 -y2)));
    }
    /*Moves Following npc depednign on distance**/
    public void moveFollower(Avatar Character, Iterator<Integer> PathnextOne){
        int nextVertexTomove = PathnextOne.next();
        Point moveTo = FindPointToVertex(nextVertexTomove);
        if(!this.world[Character.x][Character.y].equals( Tileset.FLOWER )) {
            this.world[Character.x][Character.y] = Tileset.BrownFloor;
        }
        if(!this.world[moveTo.x][moveTo.y].equals(Tileset.FLOWER)) {
            this.world[moveTo.x][moveTo.y] = Character.tile;
        }
        this.EvilSquirrel.x = moveTo.x;
        this.EvilSquirrel.y = moveTo.y;
        this.ter.renderFrame(this.world);
        return;
    }
    public void ShowFollowerPath(Avatar Dest, Avatar Source){
        int Destin = FindVertexToPoint(Dest.x, Dest.y);
        int Sourin = FindVertexToPoint(Source.x, Source.y);
        ArrayList<Integer> FollowShortestPath = MasterGraph.shortestPath(Destin,Sourin);
        ArrayList<TETile> ChangeBack = new ArrayList();

        for(int i = 1; i < FollowShortestPath.size() - 1 ; i++) {
            int Pathint = FollowShortestPath.get(i);
            Point moveTo = FindPointToVertex(Pathint);
            ChangeBack.add(this.world[moveTo.x][moveTo.y]);
            this.world[moveTo.x][moveTo.y] = Tileset.SAND;
        }
        this.ter.renderFrame(this.world);
        StdDraw.pause(3000);
        int i = 1;
        for(TETile curr : ChangeBack) {
            int Pathint = FollowShortestPath.get(i);
            Point moveTo = FindPointToVertex(Pathint);
            this.world[moveTo.x][moveTo.y] = curr;
            i +=1;
        }
        this.ter.renderFrame(this.world);
    }
    public Iterator updatePath(Avatar Dest, Avatar Source){
        int Destin = FindVertexToPoint(Dest.x, Dest.y);
        int Sourin = FindVertexToPoint(Source.x, Source.y);
        ArrayList FollowShortestPath = MasterGraph.shortestPath(Destin,Sourin);
        Iterator PathnextOne = FollowShortestPath.iterator();
        return PathnextOne;
    }
    public class Point implements Serializable{
        int x ;
        int y ;
        Point(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    public HashMap ScanVert(){
        HashMap<Integer, Point> AllVertices = new HashMap<>();
        int vertcount = 0;
        for(int x =0; x < WIDTH; x+=1){
            for(int y =0; y < HEIGHT; y+=1){
                if(this.world[x][y] == Tileset.NOTHING|| this.world[x][y] == Tileset.WALL){
                    continue;
                }
                Point curr = new Point(x,y);
                AllVertices.put(vertcount , curr);
                vertcount += 1;
            }
        }
        return AllVertices;
    }

    public void getPointGraphCorr(){

    }



    /** 0 = left, 1 == right, 2 == up 3 == down**/

    public void moveAvatar(Avatar Character,int Direction, Boolean show){
        if(this.MainCharacter.x - 1>=0&&  Direction == 0 && !this.world[Character.x - 1][Character.y].equals(Tileset.NOTHING) && !this.world[Character.x - 1][Character.y].equals (Tileset.WALL) ) {
            if(this.world[Character.x - 1][Character.y].equals(Tileset.FLOWER)){
                this.GoalCounter+=1;
            }
            this.world[Character.x][Character.y] = Tileset.BrownFloor;
            this.world[Character.x - 1][Character.y] = Character.tile;
            this.MainCharacter.x = this.MainCharacter.x - 1;
        }
        if(Character.x + 1 < WIDTH && Direction == 1 && !this.world[Character.x + 1][Character.y].equals(Tileset.NOTHING) && !this.world[Character.x + 1][Character.y].equals(Tileset.WALL)) {
            if(this.world[Character.x + 1][Character.y].equals(Tileset.FLOWER)){
                this.GoalCounter+=1;
            }
            this.world[Character.x][Character.y] = Tileset.BrownFloor;
            this.world[Character.x + 1][Character.y] = Character.tile;
            this.MainCharacter.x = this.MainCharacter.x + 1;
        }
        if(Character.y - 1>=0&& Direction == 3 && !this.world[Character.x][Character.y - 1].equals(Tileset.NOTHING) && !this.world[Character.x][Character.y- 1].equals(Tileset.WALL)) {
            if(this.world[Character.x][Character.y - 1].equals(Tileset.FLOWER)){
                this.GoalCounter+=1;
            }
            this.world[Character.x][Character.y] = Tileset.BrownFloor;
            this.world[Character.x][Character.y - 1] = Character.tile;
            Character.y = Character.y - 1;

        }
        if(Character.y + 1 < HEIGHT && Direction == 2 && !this.world[Character.x][Character.y + 1].equals(Tileset.NOTHING) && !this.world[Character.x][Character.y+1].equals(Tileset.WALL)) {
            if(this.world[Character.x][Character.y + 1].equals(Tileset.FLOWER)){
                GoalCounter+=1;
            }
            this.world[Character.x][Character.y] = Tileset.BrownFloor;
            this.world[Character.x][Character.y + 1] = Character.tile;
            Character.y = Character.y + 1;
        }
        if(show == true) {
            this.ter.renderFrame(this.world);
        }
    }





    public class Avatar implements Serializable{
        int x;
        int y;
        TETile tile;
        String name;
        public Avatar(int x, int y, TETile tile){
            this.x = x;
            this.y = y;
            this.tile = tile;
            this.name = tile.description();
        }
    }
    /** This will have to go for phase 2 too redundant*/

    private List<Room> connect(Room r1, Room r2, TETile[][] world) {

        if (verticallyAligned(r1, r2)) {
            List<Room> newRoom = new ArrayList<>();
            newRoom.add(drawVerticalHallway(r1, r2, world));
            return newRoom;
        } else if (horizontallyAligned(r1, r2)) {
            List<Room> newRoom = new ArrayList<>();
            newRoom.add(drawHorizontalHallway(r1, r2, world));
            return newRoom;
        } else {
            return drawBentHallway(r1, r2, world);
        }
    }

    private List<Room> drawBentHallway(Room r1, Room r2, TETile[][] world) {
        Room topRoom;
        Room bottomRoom;
        if (r1.getY() > r2.getY()) {
            topRoom = r1;
            bottomRoom = r2;
        } else {
            topRoom = r2;
            bottomRoom = r1;
        }

        List<Room> newRooms = new ArrayList<>();
        int virtualX = bottomRoom.getX() + (bottomRoom.getWidth() - 1) / 2;
        int virtualY = topRoom.getY() + (topRoom.getHeight() - 1) / 2;

        Room virtualRoom = new Room(3, 3, virtualX, virtualY);
        drawRoom(virtualRoom, world);
        newRooms.add(virtualRoom);

        Room horizontalSection = drawHorizontalHallway(topRoom, virtualRoom, world);
        newRooms.add(horizontalSection);

        Room verticalSection = drawVerticalHallway(bottomRoom, virtualRoom, world);
        newRooms.add(verticalSection);

        return newRooms;
    }

    private Room drawVerticalHallway(Room r1, Room r2, TETile [][] world) {
        Room topRoom;
        Room bottomRoom;
        if (r1.getY() > r2.getY()) {
            topRoom = r1;
            bottomRoom = r2;
        } else {
            topRoom = r2;
            bottomRoom = r1;
        }
        for (int i = r1.getX() + 1; i < r1.getX() + r1.getWidth() - 1; i++) {
            if (i > r2.getX() && i < (r2.getX() + r2.getWidth() - 1)) {
                int h = 2 + topRoom.getY() - (bottomRoom.getY() + bottomRoom.getHeight());
                int w = 3;
                int x = i - 1;
                int y = bottomRoom.getY() + bottomRoom.getHeight() - 1;
                Room hallway = new Room(h, w, x, y);
                drawRoom(hallway, world);
                return hallway;
            }
        }
        return null;
    }

    private Room drawHorizontalHallway(Room r1, Room r2, TETile[][] world) {
        Room rightRoom;
        Room leftRoom;
        if (r1.getX() > r2.getX()) {
            rightRoom = r1;
            leftRoom = r2;
        } else {
            rightRoom = r2;
            leftRoom = r1;
        }
        for (int i = r1.getY() + 1; i < r1.getY() + r1.getHeight() - 1; i++) {
            if (i > r2.getY() && i < (r2.getY() + r2.getHeight() - 1)) {
                int h = 3;
                int w = 2 + rightRoom.getX() - (leftRoom.getX() + leftRoom.getWidth());
                int x = leftRoom.getX() + leftRoom.getWidth() - 1;
                int y = i - 1;
                Room hallway = new Room(h, w, x, y);
                drawRoom(hallway, world);
                return hallway;
            }
        }
        return null;
    }

    private static boolean verticallyAligned(Room r1, Room r2) {
        return r1.getX() >= r2.getX() && r1.getX() < r2.getX() + r2.getWidth() - 2
                || r2.getX() >= r1.getX() && r2.getX() < r1.getX() + r1.getWidth() - 2;
    }

    private static boolean horizontallyAligned(Room r1, Room r2) {
        return r1.getY() >= r2.getY() && r1.getY() < r2.getY() + r2.getHeight() - 2
                || r2.getY() >= r1.getY() && r2.getY() < r1.getY() + r1.getHeight() - 2;
    }

    private static void sortByDistTo(List<Room> rs, Room room) {
        Comparator<Room> c = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Room r1 = (Room) o1;
                Room r2 = (Room) o2;
                return (int) (r1.roughDist(room) - r2.roughDist(room));
            }
        };
        rs.sort(c);
    }

    private void drawRoom(Room room, TETile[][] world) {
        int x = room.getX();
        int y = room.getY();
        int width = room.getWidth();
        int height = room.getHeight();

        //bottom wall
        drawLineHorizontal(x, y, width, world, Tileset.BrownFloor);
        //top wall
        drawLineHorizontal(x, y + height - 1, width, world, Tileset.BrownFloor);
        //left wall
        drawLineVertical(x, y + 1, height - 2, world, Tileset.BrownFloor);
        //right wall
        drawLineVertical(x + width - 1, y + 1, height - 2, world, Tileset.BrownFloor);
        //draw floor
        for (int i = 0; i < height - 2; i++) {
            drawLineHorizontal(x + 1, y + 1 + i, width - 2, world, Tileset.BrownFloor);
        }
        if (x>=0 && y-1>=0) {
            drawLineHorizontal(x, y-1, width, world, Tileset.WALL);
        }
        if (y + height+1 <=HEIGHT) {
            drawLineHorizontal(x, y + height, width, world, Tileset.WALL);
        }
        if (x-1>=0 && y>=0 ) {
            drawLineVertical(x - 1, y, height, world, Tileset.WALL);
        }
        if (x + width + 1 <= WIDTH) {

            drawLineVertical(x + width, y, height, world, Tileset.WALL);
        }
        /**if(RandomUtils.bernoulli()== true){

         }**/
    }

    private static boolean isOverlap(Room room, TETile[][] world) {
        int x = room.getX();
        int y = room.getY();
        int width = room.getWidth();
        int height = room.getHeight();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (world[i][j].equals(Tileset.BrownFloor) || world[i][j].equals(Tileset.WALL) || world[i][j].equals(Tileset.FLOWER)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void drawLineHorizontal(int x, int y, int l, TETile[][] world, TETile tile) {
        for (int i = 0; i < l; i++) {
            if(world[x+i][y] != Tileset.BrownFloor) {
                world[x + i][y] = tile;
            }
        }
    }

    private static void drawLineVertical(int x, int y, int l, TETile[][] world, TETile tile) {
        for (int i = 0; i < l; i++) {
            if(world[x][y+i] != Tileset.BrownFloor) {
                world[x][y+i] = tile;
            }
        }
    }

    private void drawPixel(int x, int y, TETile tile) {
        world[x][y] = tile;
    }
}
