package byow.InputDemo;

import java.io.Serializable;

/**
 * Created by hug.
 */
public class StringInputDevice implements InputSource, Serializable {
    private String input;
    private int index;

    public StringInputDevice(String s) {
        index = 0;
        input = s;
    }

    public char getNextKey() {
        char returnChar = input.charAt(index);
        index += 1;
        return returnChar;
    }

    public boolean possibleNextInput() {
        return index < input.length();
    }
}
