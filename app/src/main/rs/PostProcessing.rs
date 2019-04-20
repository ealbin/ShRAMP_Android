//
// @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
// @version: ShRAMP v0.0
//
// @objective: To detect extensive air shower radiation using smartphones
//             for the scientific study of ultra-high energy cosmic rays
//
// @institution: University of California, Irvine
// @department:  Physics and Astronomy
//
// @author: Eric Albin
// @email:  Eric.K.Albin@gmail.com
//
// @updated: 20 April 2019
//

#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)

// TODO: check if there is substantial performance increase with relaxed
#pragma rs_fp_full
//#pragma rs_fp_relaxed

// Enable debugging
//#include "rs_debug.rsh"

// Global Variables
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// gMax8bitValue / gMax16bitValue...................................................................
// Statistics (mean, stddev) are saved as normalized values, i.e. mean = gMean * gMax_bitValue
const float gMax8bitValue  = 255.;
const float gMax16bitValue = 1023.;

// gIs8bit..........................................................................................
// "1" to compute statistics for 8-bit data, "0" for 16-bit data
int gIs8bit;

// gNframes.........................................................................................
// Total number of image frames
long gNframes;

// Running Sums.....................................................................................
// Sum of pixel value (for mean computation)
// Sum of pixel value**2 (for standard deviation computation)
rs_allocation gValueSum;
rs_allocation gValue2Sum;

// Statistics.......................................................................................
// gMean:   average pixel value
// gStdDev: standard deviation of the pixel value
// gStdErr: standard deviation / sqrt(N frames)
rs_allocation gMean;
rs_allocation gStdDev;
rs_allocation gStdErr;

// gAnomalousStdDev.................................................................................
// In the process of determining the mean and standard deviation, an unlikely overflow in
// the summing variables might have occured under extreme conditions, if this happens the number of
// pixels with this problem are recorded in this variable.
rs_allocation gAnomalousStdDev;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// getMean......................................................................................
// Actually computes all the statistics at once, but returns only the mean back to Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return normalized pixel mean value (mean / gMax_bitValue)
float RS_KERNEL getMean(uint32_t x, uint32_t y) {

    // Max pixel value to normalize to
    float maxValue = gMax8bitValue;
    if (gIs8bit == 0) {
        maxValue = gMax16bitValue;
    }

    // Mean Pixel 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    uint val_sum = rsGetElementAt_uint(gValueSum, x, y);
    double mean_pixel_val = val_sum / (double) gNframes;

    rsSetElementAt_float(gMean, (float) mean_pixel_val / maxValue, x, y);

    // Standard Deviation 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    uint val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);
    double var = ( val2_sum / (double) gNframes) - ( mean_pixel_val * mean_pixel_val );

    float stddev;
    if (var < 0.) {
        // An overflow has happened in one of the running sums
        long count = rsGetElementAt_long(gAnomalousStdDev, 0, 0);
        rsSetElementAt_long(gAnomalousStdDev, count + 1, 0, 0);
        stddev = 0.;
    }
    else {
        // Everything is good
        stddev = sqrt((float) var) / maxValue;
    }

    rsSetElementAt_float(gStdDev, stddev, x, y);

    // Standard Error 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    float stderr = stddev / sqrt((float) gNframes);

    rsSetElementAt_float(gStdErr, stderr, x, y);

    //----------------------------------------------------------------------------------------------

    return (float) mean_pixel_val / maxValue;
}

// getStdDev........................................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return normalized pixel standard deviation (standard deviation / gMax_bitValue)
float RS_KERNEL getStdDev(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdDev, x, y);
}

// getStdErr........................................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return normalized pixel standard error (standard error / gMax_bitValue)
float RS_KERNEL getStdErr(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdErr, x, y);
}

// getAnomalousStdDev...............................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return number of pixels that experianced an overflow in their running sums
long RS_KERNEL getAnomalousStdDev(uint32_t x, uint32_t y) {
    return rsGetElementAt_long(gAnomalousStdDev, 0, 0);
}