package byow.InputDemo;

/**
 * Created by hug.
 */


import java.io.Serializable;
import edu.princeton.cs.algs4.StdDraw;
public class KeyboardInputSource implements InputSource, Serializable {
    private static final boolean PRINT_TYPED_KEYS = false;
    public KeyboardInputSource() {

    }

    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (PRINT_TYPED_KEYS) {
                    System.out.print(c);
                }
                return c;
            }
        }
    }

    public boolean possibleNextInput() {
        return true;
    }
}
