
package byow.Core;
import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.RandomInputSource;
import edu.princeton.cs.algs4.StdDraw;




import byow.Core.Rendering;
import byow.Core.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Engine implements Serializable {
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */

    public static final int WIDTH = 75;
    public static final int HEIGHT = 43;
    private StringBuilder actions;
    private boolean isPlaying;
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private static final HashMap<Long, TETile[][]> MemoryWorlds = new HashMap();
    public static final ArrayList <Rendering> Previous = new ArrayList<>();
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {

        InputSource inputSource;
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(0.5, 0.8, "CS61B: THE GAME");
        StdDraw.text(0.5, 0.3, "New Game (N)");
        StdDraw.text(0.5, 0.27, "Load Game(L)");
        StdDraw.text(0.5, 0.24, "Quit (Q)");
        inputSource = new KeyboardInputSource();
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            if (c == 'N' || c == 'n') {
                HandlingNewWorldKey();
                break;
            }
            if (c == 'L' || c == 'l') {
                Rendering PreviosWorld = HandleLoadingWorld();
                PreviosWorld.ter.initialize(Rendering.WIDTH,  Rendering.HEIGHT +2);
                HandleGameInteraction(PreviosWorld);
                break;
            }
            if (c == 'Q') {
                System.exit(0);
                break;
            }
        }
    }

    public Rendering  HandleLoadingWorld(){
        File FilePrev = FileUtils.join(CWD, "Previous.txt");
        Rendering PrevWorld = FileUtils.readObject(FilePrev, Rendering.class);
        return PrevWorld;
    }
    public long HandleLoadingSeed(){
        File FilePrev = FileUtils.join(CWD, "SEED");
        String SeedString = FileUtils.readContentsAsString(FilePrev);
        long Seed = Long.valueOf(SeedString);
        return Seed;
    }
    public void drawHUDFrame(String status, int statusX, TETile[][] world) {
        Font currentFont = StdDraw.getFont();
        float size = 10;
        currentFont = currentFont.deriveFont(size);
        StdDraw.setFont(currentFont);
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.text(5, HEIGHT + 1.2, "Flowers: " + status);
        StdDraw.text(19, HEIGHT + .5, "PLAYER COORDINATES: ");
        //StdDraw.text(25, HEIGHT + 2, "(" + avatar.x + ", " + avatar.y + ")");

        int mouseX = Math.min((int) StdDraw.mouseX(), WIDTH - 1);
        int mouseY = Math.min((int) StdDraw.mouseY(), HEIGHT - 1);
        TETile mouseTile = world[mouseX][mouseY];
        if(mouseTile != null) {
            StdDraw.text(5, HEIGHT + .5, "MOUSE OVER: " + mouseTile.description());
        }

        StdDraw.text(35.5, HEIGHT + .5, "INSTRUCTIONS: ");
        StdDraw.text(47, HEIGHT + .5, "Use [W][A][S][D] to move around");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String date = (this.getTime());
        StdDraw.text(WIDTH - 8, HEIGHT+ .5, date);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }
    public void HandlingNewWorldKey(){
        StdDraw.clear(Color.black);
        StdDraw.text(0.5, 0.8, "Please Enter Seed");
        String SeedStr = seedHandleinput();
        StdDraw.clear();
        long Seed = InputtoLong(SeedStr);
        Rendering NewWorld = new Rendering(Seed, true);
        NewWorld.updatePath(NewWorld.EvilSquirrel, NewWorld.MainCharacter);
        HandleGameInteraction(NewWorld);
    }
    public void HandleGameInteraction(Rendering NewWorld) {
        Iterator<Integer> PathnextOne = NewWorld.updatePath(NewWorld.EvilSquirrel, NewWorld.MainCharacter);
        Instant start = Instant.now();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        char Beforekey = 'x';
        int Coolean = 0;
        Instant startGameTimer = Instant.now();
        Instant startUpdateHud = Instant.now();
        Instant startIncreaseSpeed = Instant.now();
        ArrayList KeysPressed = new ArrayList();
        while (NewWorld.Win == false) {
            Instant FinishUpdateHUD = Instant.now();
            Instant FinishIncreaseSpeed = Instant.now();
            long timeElapsedUpdateHud = Duration.between(startUpdateHud, FinishUpdateHUD).toMillis();
            long IncreaseSpeedElaps = Duration.between(startIncreaseSpeed, FinishIncreaseSpeed).toMillis();
            if(IncreaseSpeedElaps >= 20000){
                startIncreaseSpeed = Instant.now();
                NewWorld.EvilSpeed = NewWorld.EvilSpeed/2;
                NewWorld.EvilUpdate = NewWorld.EvilSpeed + 50;
            }
            if(timeElapsedUpdateHud >= 0) {
                drawHUDFrame(Integer.toString(NewWorld.GoalCounter), 2, NewWorld.world);
                startUpdateHud = Instant.now();
            }
            if(Coolean == 2){
                SaveWorld(NewWorld,"Previous.txt", CWD);
                System.exit(0);
                break;
            }
            if (NewWorld.GoalCounter == NewWorld.AmountFlowers) {
                System.exit(0);
                break;
            }
            if (NewWorld.MainCharacter.x == NewWorld.EvilSquirrel.x && NewWorld.MainCharacter.y == NewWorld.EvilSquirrel.y) {
                System.exit(0);
                break;
            }
            finish = Instant.now();
            timeElapsed = Duration.between(start, finish).toMillis();
            if (timeElapsed >= NewWorld.EvilUpdate || !PathnextOne.hasNext()) {
                PathnextOne = NewWorld.updatePath(NewWorld.EvilSquirrel, NewWorld.MainCharacter);
            }
            Coolean = WorldKeyInteract(NewWorld, Coolean);
            if (timeElapsed >= NewWorld.EvilSpeed) {
                start = Instant.now();
                NewWorld.moveFollower(NewWorld.EvilSquirrel,PathnextOne);
            }
        }
        Instant startGamefinish = Instant.now();
    }
    public void SaveWorld(Rendering AddedThing, String ObjectID, File DestinFolder){
        File ObjectDestin = FileUtils.join(DestinFolder, ObjectID);
        FileUtils.writeObject(ObjectDestin, (Serializable) AddedThing);
    }
    /**  0 = havent seen any; 1 == seen colon; 2=== seen colon and Q**/
    public int WorldKeyInteract(Rendering WorldState, int Coolean) {
        if (StdDraw.hasNextKeyTyped()) {
            char TypedChar = StdDraw.nextKeyTyped();
            if (TypedChar == 'Q'|| TypedChar == 'q' && Coolean == 1) {
                return 2;
            }
            if (TypedChar == ':') {
                return 1;
            }
            if (TypedChar == 'w'|| TypedChar == 'W') {
                WorldState.moveAvatar(WorldState.MainCharacter, 2, true);
            }
            if (TypedChar == 'p'|| TypedChar == 'P') {
                WorldState.ShowFollowerPath(WorldState.MainCharacter, WorldState.EvilSquirrel);
            }
            else if (TypedChar == 's' || TypedChar == 'S') {
                WorldState.moveAvatar(WorldState.MainCharacter, 3, true);
            } else if (TypedChar == 'a' || TypedChar == 'A') {
                WorldState.moveAvatar(WorldState.MainCharacter, 0, true);
            } else if (TypedChar == 'd'|| TypedChar == 'D') {
                WorldState.moveAvatar(WorldState.MainCharacter, 1,true);
            }
            return 0;
        }
        return 0;
    }

    public long InputtoLong(String input){
        String TheSeed = "";
        for(int i = 1; i < input.length() - 1; i++){
            TheSeed += input.charAt(i);
        }
        long TheSeedNum = Long.parseLong(TheSeed);
        return TheSeedNum;
    }
    public String seedHandleinput(){
        int counter = 0;
        String Word = "";
        StdDraw.text(0.5, 0.8, "Please Enter Seed");
        char TypedChar = 'x';
        while(Word.length() < 3 ){      //|| TypedChar != 'S'&& TypedChar != 's'||Word.charAt(0) != 'N'&& Word.charAt(0) != 'n'){
            if(StdDraw.hasNextKeyTyped()){
                counter+=1;
                TypedChar = StdDraw.nextKeyTyped();
                Word += TypedChar;
            }
           // if((counter > 1 && (Word.charAt(0) != 'N'&& Word.charAt(0) != 'n'|| TypedChar == 'S' && TypedChar == 's')) || counter>22){
               // counter = 0;
               // Word = "";
           // }
            StdDraw.text(0.5, 0.8, "Please Enter Seed");
            StdDraw.clear(Color.black);
            StdDraw.text(.5, .7,Word);
            StdDraw.show();
        }
        return Word;
    }
    public String SeedHandle() {
        int counter = 0;
        String Word = "";
        while(counter != 0){
            if(StdDraw.hasNextKeyTyped()){
                counter+=1;
                char TypedChar = StdDraw.nextKeyTyped();
                Word += TypedChar;
                StdDraw.clear();
            }
        }
        return Word;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     * @source https://stackoverflow.com/questions/7683448/
     * in-java-how-to-get-substring-from-a-string-till-a-character-c
     */

    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        StringInputDevice inputs = new StringInputDevice(input);
        if(input.length() <=1){
            Rendering PreviosWorld = HandleLoadingWorld();
            return PreviosWorld.world;
        }
        if(input.charAt(0)!= 'n' && input.charAt(0)!= 'N'){
            Rendering PreviosWorld = HandleLoadingWorld();
            return PreviosWorld.world;
        }
        String TheSeed = "";
        int index = 0;
        HashSet<Character> NumberCHar =  new HashSet();
        NumberCHar.add('0');
        NumberCHar.add('1');
        NumberCHar.add('2');
        NumberCHar.add('3');
        NumberCHar.add('4');
        NumberCHar.add('5');
        NumberCHar.add('6');
        NumberCHar.add('7');
        NumberCHar.add('8');
        NumberCHar.add('9');
        int startformove = input.length() - 1;
        for(int i = 1; i < input.length(); i++){
            if(!NumberCHar.contains(input.charAt(i))){
                startformove= i;
                break;
            }
            TheSeed += input.charAt(i);
        }
        long TheSeedNum = Long.parseLong(TheSeed);
        Rendering NewWorld = new Rendering(TheSeedNum,false);
        WorldInteractString(NewWorld, input,startformove);
        return NewWorld.world;
    }
    public void WorldInteractString(Rendering WorldState, String Keyspressed, int start) {
        for(int i = start; i< Keyspressed.length();i++) {
            char TypedChar = Keyspressed.charAt(i);
            char TypedBefore= Keyspressed.charAt(i - 1);
            if (TypedBefore == ':'&& TypedChar == 'Q' || TypedChar == 'q' ) {
                SaveWorld(WorldState, "Previous.txt", CWD);
                break;
            }
            if (TypedChar == 'w' || TypedChar == 'W') {
                WorldState.moveAvatar(WorldState.MainCharacter, 2, false);
            } else if (TypedChar == 's' || TypedChar == 'S') {
                WorldState.moveAvatar(WorldState.MainCharacter, 3, false);
            } else if (TypedChar == 'a' || TypedChar == 'A') {
                WorldState.moveAvatar(WorldState.MainCharacter, 0, false);
            } else if (TypedChar == 'd' || TypedChar == 'D') {
                WorldState.moveAvatar(WorldState.MainCharacter, 1, false);
            }
        }
    }

    private TETile[][] play(InputSource input, boolean renderEnabled) {
        TERenderer ter = null;
        long seed = 0;
        boolean command = false;
        if (renderEnabled) {
            ter = new TERenderer();
            ter.initialize(WIDTH, HEIGHT);
        }
        return null;
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        return new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(date);
    }
}