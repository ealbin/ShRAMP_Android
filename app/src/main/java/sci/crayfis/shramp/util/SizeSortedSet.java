/*******************************************************************************
 *                                                                             *
 * @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
 * @version: ShRAMP v0.0                                                       *
 *                                                                             *
 * @objective: To detect extensive air shower radiation using smartphones      *
 *             for the scientific study of ultra-high energy cosmic rays       *
 *                                                                             *
 * @institution: University of California, Irvine                              *
 * @department:  Physics and Astronomy                                         *
 *                                                                             *
 * @author: Eric Albin                                                         *
 * @email:  Eric.K.Albin@gmail.com                                             *
 *                                                                             *
 * @updated: 25 March 2019                                                     *
 *                                                                             *
 ******************************************************************************/

package sci.crayfis.shramp.util;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * TODO: description, comments and logging
 */
@TargetApi(21)
public final class SizeSortedSet implements SortedSet<Size> {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mSortedSet...................................................................................
    // TODO: description
    private List<Size> mSortedSet = new ArrayList<>();

    // mSorter......................................................................................
    // TODO: description
    private Sorter mSorter = new Sorter();

    //**********************************************************************************************
    // Private Inner Classes
    //----------------------

    // SortByArea...................................................................................
    /**
     * Sort sizes by area from smallest to biggest
     */
    private class SortByArea implements Comparator<Size> {

        // Public Overriding Instance Methods
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // compare..................................................................................
        /**
         * TODO: description, comments and logging
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
     * Sort sizes by longest side: shortest to longest
     */
    private class SortByLongestSide implements Comparator<Size> {

        // Public Overriding Instance Methods
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // compare..................................................................................
        /**
         * TODO: description, comments and logging
         * @param s1 bla
         * @param s2 bla
         * @return bla
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
     * Master sorter
     */
    private class Sorter implements Comparator<Size> {

        // Public Overriding Instance Methods
        //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // compare..................................................................................
        /**
         * TODO: description, comments and logging
         * @param s1 bla
         * @param s2 bla
         * @return bla
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
     * TODO: description, comments and logging
     */
    public SizeSortedSet() { super(); }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // add..........................................................................................
    /**
     * TODO: description, comments and logging
     * @param size bla
     * @return bla
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
     * TODO: description, coments and logging
     * @param c bla
     * @return bla
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
     * TODO: description, comments and logging
     */
    @Override
    public void clear() {
        mSortedSet.clear();
    }

    // comparator...................................................................................
    /**
     * TODO: description, comments and longging
     * @return bla
     */
    @NonNull
    @Override
    public Comparator<? super Size> comparator() {
        return mSorter;
    }

    // contains.....................................................................................
    /**
     * TODO: description, comments and logging
     * @param o bla
     * @return bla
     */
    @Override
    public boolean contains(@Nullable Object o) {
        return mSortedSet.contains(o);
    }

    // containsAll..................................................................................
    /**
     * TODO: description, comments and logging
     * @param c bla
     * @return bla
     */
    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return mSortedSet.containsAll(c);
    }

    // first........................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Override
    public Size first() {
        if (mSortedSet.size() > 0) {
            return mSortedSet.get(0);
        }
        return null;
    }

    // headSet......................................................................................
    /**
     * TODO: description, comments and logging
     * @param toElement bla
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Override
    public boolean isEmpty() {
        return mSortedSet.size() == 0;
    }

    // iterator.....................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    @Override
    public Iterator<Size> iterator() {
        return mSortedSet.iterator();
    }

    // last.........................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @NonNull
    @Override
    public Size last() {
        return mSortedSet.get(mSortedSet.size() - 1);
    }

    // remove.......................................................................................
    /**
     * TODO: description, comments and logging
     * @param o bla
     * @return bla
     */
    @Override
    public boolean remove(@Nullable Object o) {
        return mSortedSet.remove(o);
    }

    // removeAll....................................................................................
    /**
     * TODO: description, comments and logging
     * @param c bla
     * @return bla
     */
    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return mSortedSet.removeAll(c);
    }

    // retainAll....................................................................................
    /**
     * TODO: description, comments and logging
     * @param c bla
     * @return bla
     */
    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return mSortedSet.retainAll(c);
    }

    // size.........................................................................................
    /**
     * TODO: description, comments and logging
     * @return bla
     */
    @Override
    public int size() {
        return mSortedSet.size();
    }

    // subSet.......................................................................................
    /**
     * TODO: description, comments and logging
     * @param fromElement bla
     * @param toElement bla
     * @return bla
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
     * TODO: description, comments and logging
     * @param fromElement bla
     * @return bla
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
     * TODO: description, comments and logging
     * @return bla
     */
    @Nullable
    @Override
    public Object[] toArray() {
        return mSortedSet.toArray();
    }

    // toArray......................................................................................
    /**
     * TODO: description, comments and logging
     * @param a bla
     * @param <T> bla
     * @return bla
     */
    @Nullable
    @Override
    public <T> T[] toArray(@Nullable T[] a) {
        return mSortedSet.toArray(a);
    }

}