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
 * @updated: 15 April 2019
 */

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Helper set to sort Size objects describing output surface resolutions.
 * List sorts unique resolutions by area (smallest to biggest).
 */
@TargetApi(21)
public final class SizeSortedSet implements SortedSet<Size> {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mSortedSet...................................................................................
    // Container of Sizes
    private List<Size> mSortedSet = new ArrayList<>();

    // mSorter......................................................................................
    // Sort algorithm
    private Sorter mSorter = new Sorter();

    // Private Inner Classes
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // SortByArea...................................................................................
    /**
     * Sort sizes by area from smallest to biggest, primary sorting method
     */
    private class SortByArea implements Comparator<Size> {

        // compare..................................................................................
        /**
         * @param s1 first Size to be compared
         * @param s2 second Size to be compared
         * @return a negative integer, zero, or a positive integer as the first argument is less
         * than, equal to, or greater than the second.
         */
        @Override
        public int compare(@NonNull Size s1, @NonNull Size s2) {
            long area1 = s1.getHeight() * s1.getWidth();
            long area2 = s2.getHeight() * s2.getWidth();
            return Long.compare(area1, area2);
        }
    }

    // SortByLongestSide............................................................................
    /**
     * Sort sizes by longest side from shortest to longest, if SortByArea ends in a tie, this is the
     * tie breaker
     */
    private class SortByLongestSide implements Comparator<Size> {

        // compare..................................................................................
        /**
         * @param s1 first Size to be compared
         * @param s2 second Size to be compared
         * @return a negative integer, zero, or a positive integer as the first argument is less
         * than, equal to, or greater than the second
         */
        @Override
        public int compare(@NonNull Size s1, @NonNull Size s2) {
            int longest1 = Math.max(s1.getHeight(), s1.getWidth());
            int longest2 = Math.max(s2.getHeight(), s2.getWidth());
            return Integer.compare(longest1, longest2);
        }
    }

    // Sorter.......................................................................................
    /**
     * Master sorter, calls on SortByArea and SortByLongestSide as needed
     */
    private class Sorter implements Comparator<Size> {

        // compare..................................................................................
        /**
         * @param s1 first Size to be compared
         * @param s2 second Size to be compared
         * @return a negative integer, zero, or a positive integer as the first argument is less
         * than, equal to, or greater than the second
         */
        @Override
        public int compare(@NonNull Size s1, @NonNull Size s2) {
            SortByArea        sortByArea        = new SortByArea();
            SortByLongestSide sortByAspectRatio = new SortByLongestSide();

            int areaResult = sortByArea.compare(s1, s2);
            if (areaResult != 0) {
                return areaResult;
            }
            return sortByAspectRatio.compare(s1, s2);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // SizeSortedSet................................................................................
    /**
     * Create a new SizeSortedSet
     */
    public SizeSortedSet() { super(); }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // add..........................................................................................
    /**
     * Add an element to the set (only unique Sizes are kept)
     * @param size Size object to add
     * @return true if added to the set, false if a Size like size is already contained in the set
     */
    @Override
    public boolean add(Size size) {
        if (mSortedSet.contains(size)) {
            return false;
        }
        mSortedSet.add(size);
        Collections.sort(mSortedSet, comparator());
        return true;
    }

    // addAll.......................................................................................
    /**
     * Adds a collection to the set (keeping only unique Sizes)
     * @param c Any collection that is a Size object or a subclass
     * @return true if at least one element has been added, false if at least one element hasn't
     */
    @Override
    public boolean addAll(@NonNull Collection<? extends Size> c) {
        boolean val = false;
        for (Size s : c) {
            if (mSortedSet.contains(s)) {
                continue;
            }
            mSortedSet.add(s);
            val = true;
        }
        Collections.sort(mSortedSet, comparator());
        return val;
    }

    // clear........................................................................................
    /**
     * Clear the set and start over from scratch
     */
    @Override
    public void clear() {
        mSortedSet.clear();
    }

    // comparator...................................................................................
    /**
     * @return Comparator used in sorting
     */
    @NonNull
    @Override
    @Contract(pure = true)
    public Comparator<? super Size> comparator() {
        return mSorter;
    }

    // contains.....................................................................................
    /**
     * @param o Object under test if it is contained in the set
     * @return true if Size object already in the set, false if not
     */
    @Override
    @Contract(pure = true)
    public boolean contains(@Nullable Object o) {
        return mSortedSet.contains(o);
    }

    // containsAll..................................................................................
    /**
     * @param c A collection of objects under test if they are contained in the set
     * @return true if all objects in the collection are also in the set, false otherwise
     */
    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return mSortedSet.containsAll(c);
    }

    // first........................................................................................
    /**
     * @return first element in the set (null if set is empty)
     */
    @Nullable
    @Override
    @Contract(pure = true)
    public Size first() {
        if (mSortedSet.size() > 0) {
            return mSortedSet.get(0);
        }
        return null;
    }

    // headSet......................................................................................
    /**
     * @param toElement Reference Size
     * @return a set of all Sizes less than (not including) the reference Size
     */
    @NonNull
    @Override
    public SortedSet<Size> headSet(@NonNull Size toElement) {
        SizeSortedSet headSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(s, toElement) < 0) {
                headSet.add(s);
            }
        }
        return headSet;
    }

    // isEmpty......................................................................................
    /**
     * @return true if set is empty, false if set has elements
     */
    @Override
    @Contract(pure = true)
    public boolean isEmpty() {
        return mSortedSet.size() == 0;
    }

    // iterator.....................................................................................
    /**
     * @return Set iterator
     */
    @NonNull
    @Override
    public Iterator<Size> iterator() {
        return mSortedSet.iterator();
    }

    // last.........................................................................................
    /**
     * @return last Size in set (null if empty)
     */
    @Nullable
    @Override
    @Contract(pure = true)
    public Size last() {
        if(isEmpty()) {
            return null;
        }
        return mSortedSet.get(mSortedSet.size() - 1);
    }

    // remove.......................................................................................
    /**
     * @param o Size element to remove from set
     * @return true if successfully removed, false if wasn't found / removed
     */
    @Override
    public boolean remove(@Nullable Object o) {
        return mSortedSet.remove(o);
    }

    // removeAll....................................................................................
    /**
     * @param c Collection of Size (or subclass) objects to remove from set
     * @return true if all were removed, false if not all were removed
     */
    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return mSortedSet.removeAll(c);
    }

    // retainAll....................................................................................
    /**
     * @param c Collection of Size objects to retain if present, discarding all the rest
     * @return true if at least one object has been retained
     */
    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return mSortedSet.retainAll(c);
    }

    // size.........................................................................................
    /**
     * @return Get the size (length) of the set of Size objects
     */
    @Override
    public int size() {
        return mSortedSet.size();
    }

    // subSet.......................................................................................
    /**
     * @param fromElement Non-inclusive start Size
     * @param toElement Non-inclusive stop Size
     * @return All Size objects between from and to
     */
    @NonNull
    @Override
    public SortedSet<Size> subSet(@NonNull Size fromElement, @NonNull Size toElement) {
        SizeSortedSet subSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(fromElement, s) < 0
             && mSorter.compare(s, toElement)   < 0) {
                subSet.add(s);
            }
        }
        return subSet;
    }

    // tailSet......................................................................................
    /**
     * @param fromElement Reference Size
     * @return the set of elements greater than (not including) the reference Size
     */
    @NonNull
    @Override
    public SortedSet<Size> tailSet(@NonNull Size fromElement) {
        SizeSortedSet tailSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(fromElement, s) < 0) {
                tailSet.add(s);
            }
        }
        return tailSet;
    }

    // toArray......................................................................................
    /**
     * @return The sorted Size set as an Object[] array
     */
    @Nullable
    @Override
    public Object[] toArray() {
        return mSortedSet.toArray();
    }

    // toArray......................................................................................
    /**
     * @param a Array object to populate
     * @param <T> Object type for the return array
     * @return Sorted Size set as a T[] array
     */
    @Nullable
    @Override
    public <T> T[] toArray(@Nullable T[] a) {
        return mSortedSet.toArray(a);
    }

}