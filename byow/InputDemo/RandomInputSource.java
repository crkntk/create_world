package byow.InputDemo;



import java.io.Serializable;
import java.util.Random;

/**
 * Created by hug.
 */
public class RandomInputSource implements InputSource, Serializable {
    Random r;

    public RandomInputSource(Long seed) {
        r = new Random(seed);
    }

    /** Returns a random letter between a and z.*/
    public char getNextKey() {
        return (char) (r.nextInt(26) + 'A');
    }

    public boolean possibleNextInput() {
        return true;
    }
}
