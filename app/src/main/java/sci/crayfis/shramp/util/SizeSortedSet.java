package sci.crayfis.shramp.util;

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

import sci.crayfis.shramp.camera2.ShrampCamManager;

public class SizeSortedSet implements SortedSet<Size> {
    private List<Size> mSortedSet = new ArrayList<>();
    private Sorter     mSorter    = new Sorter();

    @Override
    public Comparator<? super Size> comparator() {
        return mSorter;
    }

    @Override
    public SortedSet<Size> subSet(Size fromElement, Size toElement) {
        SizeSortedSet subSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(fromElement, s) < 0
             && mSorter.compare(s, toElement)   < 0) {
                subSet.add(s);
            }
        }
        return subSet;
    }

    @Override
    public SortedSet<Size> headSet(Size toElement) {
        SizeSortedSet headSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(s, toElement) < 0) {
                headSet.add(s);
            }
        }
        return headSet;
    }

    @Override
    public SortedSet<Size> tailSet(Size fromElement) {
        SizeSortedSet tailSet = new SizeSortedSet();

        for (Size s : mSortedSet) {
            if (mSorter.compare(fromElement, s) < 0) {
                tailSet.add(s);
            }
        }
        return tailSet;
    }

    @Override
    public Size first() {
        if (mSortedSet.size() > 0) {
            return mSortedSet.get(0);
        }
        return null;
    }

    @Override
    public Size last() {
        if (mSortedSet.size() > 0) {
            return mSortedSet.get(mSortedSet.size() - 1);
        }
        return null;
    }

    @Override
    public int size() {
        return mSortedSet.size();
    }

    @Override
    public boolean isEmpty() {
        return mSortedSet.size() == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return mSortedSet.contains(o);
    }

    @NonNull
    @Override
    public Iterator<Size> iterator() {
        return mSortedSet.iterator();
    }

    @Nullable
    @Override
    public Object[] toArray() {
        return mSortedSet.toArray();
    }

    @Override
    public <T> T[] toArray(@Nullable T[] a) {
        return mSortedSet.toArray(a);
    }

    @Override
    public boolean add(Size size) {
        if (mSortedSet.contains(size)) {
            return false;
        }
        mSortedSet.add(size);
        Collections.sort(mSortedSet, comparator());
        return true;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return mSortedSet.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return mSortedSet.containsAll(c);
    }

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

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return mSortedSet.retainAll(c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return mSortedSet.removeAll(c);
    }

    @Override
    public void clear() {
        mSortedSet.clear();
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Sort sizes by area from smallest to biggest
     */
    private class SortByArea implements Comparator<Size> {
        /**
         * @param s1 first Size to be compared
         * @param s2 second Size to be compared
         * @return a negative integer, zero, or a positive integer as the first argument is less
         * than, equal to, or greater than the second.
         */
        @Override
        public int compare(Size s1, Size s2) {
            long area1 = s1.getHeight() * s1.getWidth();
            long area2 = s2.getHeight() * s2.getWidth();
            return Long.compare(area1, area2);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Sort sizes by longest side: shortest to longest
     */
    private class SortByLongestSide implements Comparator<Size> {

        @Override
        public int compare(Size s1, Size s2) {
            int longest1 = Math.max(s1.getHeight(), s1.getWidth());
            int longest2 = Math.max(s2.getHeight(), s2.getWidth());
            return Integer.compare(longest1, longest2);
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Master sorter
     */
    private class Sorter implements Comparator<Size> {

        @Override
        public int compare(Size s1, Size s2) {
            SortByArea        sortByArea        = new SortByArea();
            SortByLongestSide sortByAspectRatio = new SortByLongestSide();

            int areaResult = sortByArea.compare(s1, s2);
            if (areaResult != 0) {
                return areaResult;
            }
            return sortByAspectRatio.compare(s1, s2);
        }
    }

}