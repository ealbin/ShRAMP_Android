package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
abstract public class ArrayToList {

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Boolean> convert(@NonNull boolean[] array) {
        List<Boolean> list = new ArrayList<>();
        for (boolean val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Byte> convert(@NonNull byte[] array) {
        List<Byte> list = new ArrayList<>();
        for (byte val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Character> convert(@NonNull char[] array) {
        List<Character> list = new ArrayList<>();
        for (char val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Short> convert(@NonNull short[] array) {
        List<Short> list = new ArrayList<>();
        for (short val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Integer> convert(@NonNull int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Long> convert(@NonNull long[] array) {
        List<Long> list = new ArrayList<>();
        for (long val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Float> convert(@NonNull float[] array) {
        List<Float> list = new ArrayList<>();
        for (float val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static List<Double> convert(@NonNull double[] array) {
        List<Double> list = new ArrayList<>();
        for (double val : array) {
            list.add(val);
        }
        return list;
    }

    // convert......................................................................................
    /**
     * TODO: description, comments and logging
     * @param array bla
     * @return bla
     */
    @NonNull
    public static <T> List<T> convert(@NonNull T[] array) {
        List<T> list = new ArrayList<>();
        for (T val : array) {
            list.add(val);
        }
        return list;
    }

}