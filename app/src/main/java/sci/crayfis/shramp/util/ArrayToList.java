package sci.crayfis.shramp.util;

import android.annotation.TargetApi;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class ArrayToList {

    //**********************************************************************************************
    // Static Class Methods
    //---------------------

    // Public
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Boolean> convert(boolean[] array) {
        List<Boolean> list = new ArrayList<>();
        for (boolean val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Byte> convert(byte[] array) {
        List<Byte> list = new ArrayList<>();
        for (byte val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Character> convert(char[] array) {
        List<Character> list = new ArrayList<>();
        for (char val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Short> convert(short[] array) {
        List<Short> list = new ArrayList<>();
        for (short val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Integer> convert(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Long> convert(long[] array) {
        List<Long> list = new ArrayList<>();
        for (long val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Float> convert(float[] array) {
        List<Float> list = new ArrayList<>();
        for (float val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static List<Double> convert(double[] array) {
        List<Double> list = new ArrayList<>();
        for (double val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array
     * @return
     */
    public static <T> List<T> convert(T[] array) {
        List<T> list = new ArrayList<>();
        for (T val : array) {
            list.add(val);
        }
        return list;
    }

}