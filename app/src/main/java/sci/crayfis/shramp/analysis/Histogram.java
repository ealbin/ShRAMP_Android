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
 * @updated: 24 April 2019
 */

package sci.crayfis.shramp.analysis;

import android.annotation.TargetApi;

/**
 * Represents a histogram and related functions
 */
@TargetApi(21)
public class Histogram {

    // Private Instance Fields
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // mBins........................................................................................
    // Histogram bin left edges
    int[] mBins;

    // mNbins.......................................................................................
    // Number of bins
    int mNbins;

    // mValues......................................................................................
    // Histogram values for each bin
    int[] mValues;

    // mUnderflow...................................................................................
    // Histogram value for underflow
    int mUnderflow;

    // mOverflow....................................................................................
    // Histogram value for overflow
    int mOverflow;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructors
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // Histogram....................................................................................
    /**
     * Disabled
     */
    private Histogram() {}

    // Histogram....................................................................................
    /**
     * Creates a new histogram from low to high in integer pixel steps
     * @param low Low limit in pixel value units
     * @param high High limit in pixel value units
     */
    public Histogram(int low, int high) {
        mNbins  = high - low;
        mBins   = new int[mNbins];
        mValues = new int[mNbins];

        int index = 0;
        for (int i = low; i < high; i++) {
            mBins[index]   = i;
            mValues[index] = 0;
            index++;
        }

        mUnderflow = 0;
        mOverflow  = 0;
    }

    // Public Instance Methods
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // add..........................................................................................
    /**
     * Add the value to the histogram
     * @param value Value to add
     * @return The bin number it was added to, -1 = underflow, Nbins = overflow
     */
    public int add(double value) {
      int bin = getBinNumber(value);
      if (bin == -1) {
          mUnderflow++;
      }
      else if (bin == mNbins) {
          mOverflow++;
      }
      else {
          mValues[bin]++;
      }
      return bin;
    }

    // getBinCenter.................................................................................
    /**
     * @param bin Bin number
     * @return The value for the center of the bin, Double.NaN if bin number is beyond [0, nBins - 1]
     */
    public double getBinCenter(int bin) {
        if (bin < 0 || bin > mNbins - 1) {
            return Double.NaN;
        }
        return mBins[bin] + 0.5;
    }

    // getBinNumber.................................................................................
    /**
     * @param value Value to find the bin number
     * @return The bin number where value lies, -1 if underflow, Nbins if overflow
     */
    public int getBinNumber(double value) {
        if (value < mBins[0]) {
            return -1;
        }

        for (int i = 0; i < mNbins; i++) {
            if (value >= mBins[i] && value < mBins[i] + 1) {
                return i;
            }
        }

        return mNbins;
    }

    // getValue.....................................................................................
    /**
     * @param bin Bin number for the histogram value wanted
     * @return The value at that bin (bin number = -1 is underflow, = Nbins is overflow)
     */
    public int getValue(int bin) {
        if (bin == -1) {
            return mUnderflow;
        }
        if (bin == mNbins) {
            return mOverflow;
        }
        return mValues[bin];
    }

    // getMaxBin....................................................................................
    /**
     * @return The bin number where the maximum histogram value is, if there are more than one equal
     *         maximum, returns the first occurrence (does not search underflow/overflow bins)
     */
    public int getMaxBin() {
        int maxIndex = 0;
        int maxValue = mValues[0];
        for (int i = 1; i < mNbins; i++) {
            if (mValues[i] > maxValue) {
                maxIndex = i;
                maxValue = mValues[i];
            }
        }
        return maxIndex;
    }

    // getMaxStdDev.................................................................................
    /**
     * @return The standard deviation immediately surrounding the max bin (+/- 10 pixel values)
     */
    public double getMaxStdDev() {
        int delta   = 10;
        int maxBin  = getMaxBin();
        int lowBin  = Math.max(0, maxBin - delta);
        int highbin = Math.min(mNbins - 1, maxBin + delta);

        int N = 0;
        double stddev = 0.;
        for (int i = lowBin; i <= highbin; i++) {
            int val = getValue(i);
            stddev += val * (getBinCenter(i) - getBinCenter(maxBin)) * (getBinCenter(i) - getBinCenter(maxBin));
            N += val;
        }
        return Math.sqrt( stddev / ( (double) N) );
    }

    // reset........................................................................................
    /**
     * Resets (clears) histogram values including overflow/underflow but keeps the same bins
     */
    public void reset() {
        mUnderflow = 0;
        mOverflow  = 0;
        for (int i = 0; i < mNbins; i++) {
            mValues[i] = 0;
        }
    }

}