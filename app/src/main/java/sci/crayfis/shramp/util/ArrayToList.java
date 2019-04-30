/*
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
 * @version: ShRAMP v0.0
 *
 * @objective: To detect extensive air shower radiation using smartphones
 *             for the scientific study of ultra-high energy cosmic rays
 *
 * @institution: University of California, Irvine
 * @department:  Physics and Astronomy
 *
 * @author: Eric Albin
 * @email:  Eric.K.Albin@gmail.com
 *
 * @updated: 29 April 2019
 */

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for reading camera abilities, turns a primitive-type array (or object array) into
 * a List<Object> array.
 */
@TargetApi(21)
abstract public class ArrayToList {

    // Public Class Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // convert......................................................................................
    /**
     * Turns a boolean[] array into a List<Boolean> array
     * @param array input
     * @return output
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
     * Turns a byte[] array into a List<Byte> array
     * @param array input
     * @return output
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
     * Turns a char[] array into a List<Char> array
     * @param array input
     * @return output
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
     * Turns a short[] array into a List<Short> array
     * @param array input
     * @return output
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
     * Turns an int[] array into a List<Integer> array
     * @param array input
     * @return output
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
     * Turns a long[] array into a List<Long> array
     * @param array input
     * @return output
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
     * Turns a float[] array into a List<Float> array
     * @param array input
     * @return output
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
     * Turns a double[] array into a List<Double> array
     * @param array input
     * @return output
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
     * Turns an Object[] array into a List<Object> array
     * @param array input
     * @return output
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