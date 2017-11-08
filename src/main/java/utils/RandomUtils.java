package utils;

import java.util.Random;

public class RandomUtils {
    private static Random r = new Random(System.currentTimeMillis());

    // Generate the first number randomly, convert it to a positive number
    private static long currentNumber = Math.abs(r.nextInt());

    /**
     * Return the next ID and increment the current value.
     */
    public static long getNextID() {
        currentNumber++;
        return currentNumber;
    }
}
