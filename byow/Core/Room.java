package byow.Core;
import java.io.Serializable;
import java.util.HashSet;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Room implements Serializable {
    private int height;
    private int width;
    private int xcoord;
    private int ycoord;
    private HashSet<Room> neighbors;

    public Room(int h, int w, int x, int y) {
        this.height = h;
        this.width = w;
        this.xcoord = x;
        this.ycoord = y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return xcoord;
    }

    public int getY() {
        return ycoord;
    }

    public double roughDist(Room r) {
        return Math.pow((double) r.getX() - (double) xcoord, 2)
                + Math.pow((double) r.getY() - (double) ycoord, 2);
    }

    public void addNeighbor(Room r) {
        neighbors.add(r);
    }

    public boolean isNeighbor(Room r) {
        return neighbors.contains(r);
    }

    public void addDoor(int x, int y, TETile[][] world) {
        world[xcoord][ycoord] = Tileset.FLOWER;
    }
}
